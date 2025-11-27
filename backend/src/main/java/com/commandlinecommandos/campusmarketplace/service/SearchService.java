package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.dto.ProductSearchResult;
import com.commandlinecommandos.campusmarketplace.dto.SearchMetadata;
import com.commandlinecommandos.campusmarketplace.dto.SearchRequest;
import com.commandlinecommandos.campusmarketplace.dto.SearchResponse;
import com.commandlinecommandos.campusmarketplace.model.Product;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.ProductSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling product search operations
 * Provides full-text search, filtering, sorting, and autocomplete
 */
@Service
public class SearchService {
    
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private SearchHistoryService searchHistoryService;
    
    /**
     * Perform comprehensive product search with filters, sorting, and caching
     * 
     * @param request Search request with filters and pagination
     * @param user Current user
     * @return Search response with results and metadata
     */
    @Cacheable(value = "searchResults", key = "#request.cacheKey()")
    public SearchResponse search(SearchRequest request, User user) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Check if user has a university assigned
            if (user.getUniversity() == null) {
                log.error("User {} does not have a university assigned", user.getUsername());
                throw new IllegalArgumentException("User must have a university assigned to perform searches");
            }
            
            UUID universityId = user.getUniversity().getUniversityId();
            
            // Determine search strategy based on query
            Page<Product> results;
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                // Full-text search with filters
                results = searchWithQuery(request, universityId);
            } else {
                // Filter-only search (no text query)
                results = searchWithFiltersOnly(request, universityId);
            }
            
            // Save search history asynchronously
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                searchHistoryService.saveSearchAsync(user.getUserId(), 
                    request.getQuery(), (int) results.getTotalElements());
            }
            
            // Transform to response
            SearchResponse response = transformToSearchResponse(results, request, startTime, false);
            
            log.info("Search completed: query='{}', filters={}, results={}, time={}ms",
                    request.getQuery(), countFilters(request), 
                    results.getTotalElements(), response.getMetadata().getSearchTimeMs());
            
            return response;
        } catch (Exception e) {
            log.error("Search error: query='{}', error={}", request.getQuery(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Search with full-text query
     * Falls back to simpler search if PostgreSQL full-text functions are unavailable (e.g., H2 tests)
     */
    private Page<Product> searchWithQuery(SearchRequest request, UUID universityId) {
        Pageable pageable = createPageable(request);
        Pageable textPageable = PageRequest.of(request.getPage(), request.getSize());
        
        // Try full-text search first (PostgreSQL only)
        Page<Product> textSearchResults;
        try {
            textSearchResults = productRepository.searchWithFullText(
                universityId, request.getQuery(), textPageable);
        } catch (Exception e) {
            // Fall back to simple search if full-text search fails (H2 compatibility)
            log.warn("Full-text search failed, falling back to simple search: {}", e.getMessage());
            Specification<Product> spec = ProductSpecifications.withFilters(
                universityId,
                request.getCategories(),
                request.getConditions(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getLocation(),
                request.getDateFrom()
            );
            // Add simple title/description search
            spec = spec.and((root, query, cb) -> {
                String searchPattern = "%" + request.getQuery().toLowerCase() + "%";
                return cb.or(
                    cb.like(cb.lower(root.get("title")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern)
                );
            });
            return productRepository.findAll(spec, pageable);
        }
        
        // If no results and query looks like it might have typos, try fuzzy search
        if (textSearchResults.isEmpty() && request.getQuery().length() > 3) {
            try {
                textSearchResults = productRepository.fuzzySearch(
                    universityId, request.getQuery(), textPageable);
            } catch (Exception e) {
                log.warn("Fuzzy search failed: {}", e.getMessage());
                // Already have empty results, just continue
            }
        }
        
        // Apply additional filters using Specifications if needed
        if (hasAdditionalFilters(request)) {
            Specification<Product> spec = ProductSpecifications.withFilters(
                universityId,
                request.getCategories(),
                request.getConditions(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getLocation(),
                request.getDateFrom()
            );
            
            return productRepository.findAll(spec, pageable);
        }
        
        return textSearchResults;
    }
    
    /**
     * Search with filters only (no text query)
     */
    private Page<Product> searchWithFiltersOnly(SearchRequest request, UUID universityId) {
        Pageable pageable = createPageable(request);
        
        Specification<Product> spec = ProductSpecifications.withFilters(
            universityId,
            request.getCategories(),
            request.getConditions(),
            request.getMinPrice(),
            request.getMaxPrice(),
            request.getLocation(),
            request.getDateFrom()
        );
        
        return productRepository.findAll(spec, pageable);
    }
    
    /**
     * Create pageable with appropriate sorting
     */
    private Pageable createPageable(SearchRequest request) {
        Sort sort = createSort(request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
    
    /**
     * Create sort criteria based on sort parameter
     */
    private Sort createSort(String sortBy) {
        if (sortBy == null) {
            sortBy = "relevance";
        }
        
        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popularity" -> Sort.by(Sort.Direction.DESC, "viewCount", "favoriteCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");  // Default for relevance
        };
    }
    
    /**
     * Check if request has additional filters beyond text search
     */
    private boolean hasAdditionalFilters(SearchRequest request) {
        return (request.getCategories() != null && !request.getCategories().isEmpty()) ||
               (request.getConditions() != null && !request.getConditions().isEmpty()) ||
               request.getMinPrice() != null ||
               request.getMaxPrice() != null ||
               (request.getLocation() != null && !request.getLocation().trim().isEmpty()) ||
               request.getDateFrom() != null;
    }
    
    /**
     * Count number of active filters
     */
    private int countFilters(SearchRequest request) {
        int count = 0;
        if (request.getCategories() != null && !request.getCategories().isEmpty()) count++;
        if (request.getConditions() != null && !request.getConditions().isEmpty()) count++;
        if (request.getMinPrice() != null || request.getMaxPrice() != null) count++;
        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) count++;
        if (request.getDateFrom() != null) count++;
        return count;
    }
    
    /**
     * Transform Page<Product> to SearchResponse
     */
    private SearchResponse transformToSearchResponse(Page<Product> page, 
                                                     SearchRequest request, 
                                                     long startTime,
                                                     boolean cached) {
        List<ProductSearchResult> results = page.getContent().stream()
            .map(this::transformToSearchResult)
            .collect(Collectors.toList());
        
        SearchMetadata metadata = new SearchMetadata(
            System.currentTimeMillis() - startTime,
            buildFilterDescription(request),
            countFilters(request),
            request.getSortBy(),
            cached,
            request.getQuery()
        );
        
        return new SearchResponse(
            results,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize(),
            page.hasNext(),
            page.hasPrevious(),
            metadata
        );
    }
    
    /**
     * Transform Product to ProductSearchResult
     */
    private ProductSearchResult transformToSearchResult(Product product) {
        ProductSearchResult result = new ProductSearchResult();
        result.setProductId(product.getProductId());
        result.setTitle(product.getTitle());
        result.setDescription(product.getDescription());
        result.setPrice(product.getPrice());
        result.setCategory(product.getCategory());
        result.setCondition(product.getCondition());
        result.setSellerId(product.getSeller().getUserId());
        result.setSellerName(product.getSeller().getFirstName() + " " + product.getSeller().getLastName());
        result.setSellerUsername(product.getSeller().getUsername());
        result.setLocation(product.getPickupLocation());
        result.setViewCount(product.getViewCount());
        result.setFavoriteCount(product.getFavoriteCount());
        result.setCreatedAt(product.getCreatedAt());
        result.setImageUrls(List.of());  // TODO: Add image URLs when image service is implemented
        result.setRelevanceScore(null);  // Set from ts_rank if available
        result.setNegotiable(product.isNegotiable());
        result.setQuantity(product.getQuantity());
        return result;
    }
    
    /**
     * Build human-readable filter description
     */
    private String buildFilterDescription(SearchRequest request) {
        List<String> filters = new ArrayList<>();
        
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            filters.add("Categories: " + request.getCategories());
        }
        if (request.getConditions() != null && !request.getConditions().isEmpty()) {
            filters.add("Conditions: " + request.getConditions());
        }
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            filters.add(String.format("Price: $%s - $%s", 
                request.getMinPrice() != null ? request.getMinPrice() : "0",
                request.getMaxPrice() != null ? request.getMaxPrice() : "∞"));
        }
        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
            filters.add("Location: " + request.getLocation());
        }
        if (request.getDateFrom() != null) {
            filters.add("Posted after: " + request.getDateFrom());
        }
        
        return filters.isEmpty() ? "No filters" : String.join(", ", filters);
    }
    
    /**
     * Get autocomplete suggestions
     * Cached for 10 minutes
     */
    @Cacheable(value = "autocomplete", key = "#query + '_' + #universityId")
    public List<String> autocomplete(String query, UUID universityId) {
        if (query == null || query.length() < 2) {
            return List.of();
        }
        
        try {
            // Try PostgreSQL similarity() first (for production)
            List<String> suggestions = productRepository.findTitleSuggestions(universityId, query);
            log.debug("Autocomplete: query='{}', suggestions={}", query, suggestions.size());
            return suggestions;
        } catch (Exception e) {
            // Fallback to LIKE for H2 or if similarity() fails
            log.debug("Autocomplete similarity() failed (likely H2), falling back to LIKE: {}", e.getMessage());
            try {
                List<String> suggestions = productRepository.findTitleSuggestionsLike(universityId, query);
                log.debug("Autocomplete (LIKE fallback): query='{}', suggestions={}", query, suggestions.size());
                return suggestions;
            } catch (Exception e2) {
                log.error("Autocomplete error: query='{}', error={}", query, e2.getMessage(), e2);
                return List.of();
            }
        }
    }
    
    /**
     * Get recent searches for a user
     */
    public List<String> getRecentSearches(UUID userId) {
        return searchHistoryService.getRecentSearches(userId, 10);
    }
    
    /**
     * Utility: Convert date filter string to LocalDateTime
     * Supports: "24h", "7d", "30d", "90d"
     */
    public static LocalDateTime parseDateFilter(String filter) {
        if (filter == null) return null;
        
        LocalDateTime now = LocalDateTime.now();
        return switch (filter.toLowerCase()) {
            case "24h" -> now.minusHours(24);
            case "7d" -> now.minusDays(7);
            case "30d" -> now.minusDays(30);
            case "90d" -> now.minusDays(90);
            default -> null;
        };
    }
}

