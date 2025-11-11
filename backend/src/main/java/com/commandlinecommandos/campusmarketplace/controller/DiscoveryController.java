package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.ProductSummary;
import com.commandlinecommandos.campusmarketplace.dto.TrendingResponse;
import com.commandlinecommandos.campusmarketplace.dto.RecommendedResponse;
import com.commandlinecommandos.campusmarketplace.dto.SimilarResponse;
import com.commandlinecommandos.campusmarketplace.dto.RecentlyViewedResponse;
import com.commandlinecommandos.campusmarketplace.dto.ErrorResponse;
import com.commandlinecommandos.campusmarketplace.exception.NotFoundException;
import com.commandlinecommandos.campusmarketplace.exception.UnauthorizedException;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import com.commandlinecommandos.campusmarketplace.service.DiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for product discovery features
 * Provides endpoints for trending, recommended, similar, and recently viewed items
 */
@RestController
@RequestMapping("/discovery")
@Tag(name = "Discovery", description = "Product discovery and recommendation endpoints")
public class DiscoveryController {
    
    private static final Logger log = LoggerFactory.getLogger(DiscoveryController.class);
    
    @Autowired
    private DiscoveryService discoveryService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get trending products
     * Returns most popular products based on views and favorites
     * 
     * @param limit Maximum number of products (default: 10)
     * @param token JWT authorization token
     * @return List of trending products
     */
    @GetMapping("/trending")
    @Operation(summary = "Get trending products",
               description = "Get trending products based on views and favorites")
    public ResponseEntity<?> getTrending(
            @Parameter(description = "Maximum number of products") 
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        try {
            // Validate limit
            if (limit < 1 || limit > 50) {
                return ResponseEntity.badRequest().body(new ErrorResponse("limit must be between 1 and 50"));
            }
            
            // Get user and their university (will throw UnauthorizedException if token is missing/invalid)
            User user = getCurrentUser(token);
            List<ProductSummary> trending = discoveryService.getTrendingItems(
                user.getUniversity().getUniversityId(), limit);
            
            log.debug("Trending items: user={}, universityId={}, count={}", 
                user.getUsername(), user.getUniversity().getUniversityId(), trending.size());
            return ResponseEntity.ok(new TrendingResponse(trending));
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized trending request: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Trending error: {}", e.getMessage(), e);
            return ResponseEntity.ok(new TrendingResponse(List.of()));
        }
    }
    
    /**
     * Get recommended products for current user
     * Based on browsing history and interests
     * 
     * @param limit Maximum number of products (default: 10)
     * @param token JWT authorization token
     * @return List of recommended products
     */
    @GetMapping("/recommended")
    @Operation(summary = "Get recommended products",
               description = "Get personalized product recommendations based on browsing history")
    public ResponseEntity<?> getRecommended(
            @Parameter(description = "Maximum number of products") 
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        try {
            User user = getCurrentUser(token);
            List<ProductSummary> recommended = discoveryService.getRecommendedItems(user, limit);
            
            log.debug("Recommended items: user={}, count={}", user.getUsername(), recommended.size());
            return ResponseEntity.ok(new RecommendedResponse(recommended));
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized recommended request: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Recommended error: {}", e.getMessage(), e);
            return ResponseEntity.ok(new RecommendedResponse(List.of()));
        }
    }
    
    /**
     * Get similar products to a given product
     * Based on category and other attributes
     * 
     * @param productId Product UUID
     * @param limit Maximum number of products (default: 6)
     * @param token JWT authorization token (optional for public access)
     * @return List of similar products
     */
    @GetMapping("/similar/{productId}")
    @Operation(summary = "Get similar products",
               description = "Get products similar to a specific product")
    public ResponseEntity<SimilarResponse> getSimilar(
            @Parameter(description = "Product UUID") 
            @PathVariable UUID productId,
            @Parameter(description = "Maximum number of products") 
            @RequestParam(defaultValue = "6") int limit,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        try {
            // Similar items don't strictly require auth, but we validate if token is provided
            if (token != null && !token.isEmpty()) {
                getCurrentUser(token);  // Validate token if provided
            }
            
            List<ProductSummary> similar = discoveryService.getSimilarItems(productId, limit);
            
            log.debug("Similar items: productId={}, count={}", productId, similar.size());
            return ResponseEntity.ok(new SimilarResponse(similar));
        } catch (UnauthorizedException e) {
            log.warn("Invalid token for similar request: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (NotFoundException e) {
            log.warn("Product not found for similar request: {}", e.getMessage());
            return ResponseEntity.status(404).body(new SimilarResponse(List.of()));
        } catch (Exception e) {
            log.error("Similar items error: productId={}, error={}", productId, e.getMessage(), e);
            return ResponseEntity.ok(new SimilarResponse(List.of()));
        }
    }
    
    /**
     * Get recently viewed products for current user
     * 
     * @param limit Maximum number of products (default: 10)
     * @param token JWT authorization token
     * @return List of recently viewed products
     */
    @GetMapping("/recently-viewed")
    @Operation(summary = "Get recently viewed products",
               description = "Get products recently viewed by the current user")
    public ResponseEntity<RecentlyViewedResponse> getRecentlyViewed(
            @Parameter(description = "Maximum number of products") 
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("Authorization") String token) {
        
        try {
            User user = getCurrentUser(token);
            List<ProductSummary> recentlyViewed = discoveryService.getRecentlyViewedItems(user, limit);
            
            log.debug("Recently viewed: user={}, count={}", user.getUsername(), recentlyViewed.size());
            return ResponseEntity.ok(new RecentlyViewedResponse(recentlyViewed));
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized recently-viewed request: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Recently viewed error: {}", e.getMessage(), e);
            return ResponseEntity.ok(new RecentlyViewedResponse(List.of()));
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

