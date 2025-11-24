package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@Transactional
public class ListingsService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all active listings with pagination
     */
    public Page<Product> getAllListings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt"));
        return productRepository.findByIsActiveTrueAndModerationStatus(
            ModerationStatus.APPROVED, 
            pageable
        );
    }
    
    /**
     * Get listings by category
     */
    public Page<Product> getListingsByCategory(ProductCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return productRepository.findByCategoryAndIsActiveTrueAndModerationStatus(
            category,
            ModerationStatus.APPROVED,
            pageable
        );
    }
    
    /**
     * Search listings
     */
    public Page<Product> searchListings(String keyword, ProductCategory category, 
                                       BigDecimal minPrice, BigDecimal maxPrice,
                                       int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        
        // For now, return all active listings (can be enhanced with proper search)
        return productRepository.findByIsActiveTrueAndModerationStatus(
            ModerationStatus.APPROVED,
            pageable
        );
    }
    
    /**
     * Get listing by ID
     */
    public Product getListingById(UUID id) {
        return productRepository.findById(id).orElse(null);
    }
    
    /**
     * Create a new listing (uses current authenticated user as seller)
     */
    public Product createListing(Map<String, Object> listingData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new RuntimeException("User not authenticated");
        }
        User seller = (User) authentication.getPrincipal();
        
        return createListing(seller, listingData);
    }
    
    /**
     * Create a new listing with specified seller
     */
    public Product createListing(User seller, Map<String, Object> listingData) {
        Product product = new Product();
        product.setSeller(seller);
        product.setUniversity(seller.getUniversity());
        product.setTitle((String) listingData.get("title"));
        product.setDescription((String) listingData.get("description"));
        
        // Parse category
        String categoryStr = (String) listingData.get("category");
        if (categoryStr != null) {
            try {
                product.setCategory(ProductCategory.valueOf(categoryStr));
            } catch (IllegalArgumentException e) {
                product.setCategory(ProductCategory.OTHER);
            }
        } else {
            product.setCategory(ProductCategory.OTHER);
        }
        
        // Parse condition
        String conditionStr = (String) listingData.get("condition");
        if (conditionStr != null) {
            try {
                product.setCondition(ProductCondition.valueOf(conditionStr));
            } catch (IllegalArgumentException e) {
                product.setCondition(ProductCondition.GOOD);
            }
        } else {
            product.setCondition(ProductCondition.GOOD);
        }
        
        // Parse price
        Object priceObj = listingData.get("price");
        if (priceObj instanceof Number) {
            product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
        } else if (priceObj instanceof String) {
            product.setPrice(new BigDecimal((String) priceObj));
        }
        
        // Parse location/pickup location
        if (listingData.containsKey("location")) {
            product.setPickupLocation((String) listingData.get("location"));
        }
        
        product.setModerationStatus(ModerationStatus.APPROVED); // Auto-approve for now
        product.setActive(true);
        product.publish();
        
        return productRepository.save(product);
    }
    
    /**
     * Convert Product to DTO format for API response
     */
    public Map<String, Object> productToDto(Product product) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", product.getProductId().toString());
        dto.put("title", product.getTitle());
        dto.put("description", product.getDescription());
        dto.put("category", product.getCategory().name());
        dto.put("condition", product.getCondition().name());
        dto.put("price", product.getPrice().doubleValue());
        dto.put("location", product.getPickupLocation() != null ? product.getPickupLocation() : "Campus");
        dto.put("date", product.getPublishedAt() != null ? product.getPublishedAt().toString() : product.getCreatedAt().toString());
        dto.put("createdAt", product.getCreatedAt() != null ? product.getCreatedAt().toString() : "");
        String sellerId = product.getSeller().getUserId() != null ? product.getSeller().getUserId().toString() : "";
        String sellerName = (product.getSeller().getFirstName() != null ? product.getSeller().getFirstName() : "") + 
                           " " + (product.getSeller().getLastName() != null ? product.getSeller().getLastName() : "");
        sellerName = sellerName.trim().isEmpty() ? product.getSeller().getUsername() : sellerName;
        
        dto.put("sellerId", sellerId);
        Map<String, Object> sellerInfo = new HashMap<>();
        sellerInfo.put("id", sellerId);
        sellerInfo.put("name", sellerName);
        sellerInfo.put("username", product.getSeller().getUsername());
        dto.put("seller", sellerInfo);
        dto.put("imageUrl", null); // Can be added later
        dto.put("viewCount", product.getViewCount() != null ? product.getViewCount() : 0);
        dto.put("favoriteCount", product.getFavoriteCount() != null ? product.getFavoriteCount() : 0);
        dto.put("negotiable", product.isNegotiable());
        dto.put("quantity", product.getQuantity());
        return dto;
    }
}

