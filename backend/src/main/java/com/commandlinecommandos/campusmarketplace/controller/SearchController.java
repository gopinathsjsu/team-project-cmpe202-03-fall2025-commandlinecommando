package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.*;
import com.commandlinecommandos.campusmarketplace.exception.UnauthorizedException;
import com.commandlinecommandos.campusmarketplace.model.SearchHistory;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.repository.SearchHistoryRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import com.commandlinecommandos.campusmarketplace.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for product search and autocomplete
 * Provides endpoints for searching, filtering, and discovery
 */
@RestController
@RequestMapping("/search")
@Tag(name = "Search", description = "Product search and discovery endpoints")
public class SearchController {
    
    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    /**
     * Main search endpoint
     * Supports full-text search, filtering, sorting, and pagination
     * 
     * @param request Search request with query and filters
     * @param token JWT authorization token
     * @return Search response with results and metadata
     */
    @PostMapping
    @Operation(summary = "Search products", 
               description = "Search products with full-text search, filters, sorting, and pagination")
    public ResponseEntity<?> search(
            @RequestBody SearchRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        try {
            // Check authentication FIRST
            User user = getCurrentUser(token);
            
            // Then validate request
            if (request.getPage() < 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("page: must be greater than or equal to 0"));
            }
            if (request.getSize() < 1 || request.getSize() > 100) {
                return ResponseEntity.badRequest().body(new ErrorResponse("size: must be between 1 and 100"));
            }
            if (request.getMinPrice() != null && request.getMaxPrice() != null && 
                request.getMinPrice().compareTo(request.getMaxPrice()) > 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("minPrice cannot be greater than maxPrice"));
            }
            if (request.getSortBy() != null && !isValidSortBy(request.getSortBy())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid sortBy parameter: " + request.getSortBy()));
            }
            SearchResponse response = searchService.search(request, user);
            
            log.info("Search request: user={}, query='{}', results={}",
                    user.getUsername(), request.getQuery(), response.getTotalResults());
            
            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized search attempt: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid search request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Search error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    private boolean isValidSortBy(String sortBy) {
        return sortBy.matches("^(relevance|price_(asc|desc)|date_(asc|desc))$");
    }
    
    /**
     * Autocomplete endpoint
     * Provides search suggestions as user types
     * 
     * @param query Search query (minimum 2 characters)
     * @param token JWT authorization token
     * @return List of title suggestions
     */
    @GetMapping("/autocomplete")
    @Operation(summary = "Get autocomplete suggestions",
               description = "Get search suggestions based on product titles")
    public ResponseEntity<?> autocomplete(
            @Parameter(description = "Search query (min 2 characters)") 
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "query", required = false) String query,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        // Support both 'q' and 'query' parameters
        String searchQuery = q != null ? q : query;
        
        try {
            if (searchQuery == null || searchQuery.length() < 2) {
                return ResponseEntity.badRequest().body(new ErrorResponse("query parameter must be at least 2 characters"));
            }
            
            User user = getCurrentUser(token);
            List<String> suggestions = searchService.autocomplete(searchQuery, 
                user.getUniversity().getUniversityId());
            
            log.debug("Autocomplete: query='{}', suggestions={}", searchQuery, suggestions.size());
            return ResponseEntity.ok(new AutocompleteResponse(suggestions));
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized autocomplete attempt: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Autocomplete error: query='{}', error={}", searchQuery, e.getMessage(), e);
            return ResponseEntity.ok(new AutocompleteResponse(List.of()));
        }
    }
    
    /**
     * Get search history for current user
     * Returns recent search queries
     * 
     * @param token JWT authorization token
     * @return List of recent search queries
     */
    @GetMapping("/history")
    @Operation(summary = "Get search history",
               description = "Get recent search queries for the current user")
    public ResponseEntity<?> getSearchHistory(
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        try {
            User user = getCurrentUser(token);
            List<SearchHistory> history = searchHistoryRepository.findByUserOrderByCreatedAtDesc(user);
            
            // Convert to DTOs to avoid Hibernate proxy serialization issues
            List<SearchHistoryItem> historyItems = history.stream()
                .map(sh -> new SearchHistoryItem(
                    sh.getId(),
                    sh.getSearchQuery(),
                    sh.getResultsCount(),
                    sh.getCreatedAt()
                ))
                .toList();
            
            log.debug("Search history: user={}, items={}", user.getUsername(), historyItems.size());
            return ResponseEntity.ok(new SearchHistoryResponse(historyItems));
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized search history attempt: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Search history error: {}", e.getMessage(), e);
            return ResponseEntity.ok(new SearchHistoryResponse(List.of()));
        }
    }
    
    /**
     * Helper method to extract user from JWT token
     * 
     * @param authHeader Authorization header with Bearer token
     * @return User object
     * @throws UnauthorizedException if token is invalid or user not found
     */
    private User getCurrentUser(String authHeader) throws UnauthorizedException {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Invalid authorization header");
            }
            
            String token = authHeader.substring(7);  // Remove "Bearer " prefix
            String username = jwtUtil.extractUsername(token);
            
            if (username == null) {
                throw new UnauthorizedException("Invalid token");
            }
            
            return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        } catch (UnauthorizedException e) {
            // Re-throw UnauthorizedException as is
            throw e;
        } catch (Exception e) {
            // Wrap any other exception (JWT parsing errors, etc.) in UnauthorizedException
            log.error("Token validation failed: {}", e.getMessage());
            throw new UnauthorizedException("Token validation failed");
        }
    }
}

