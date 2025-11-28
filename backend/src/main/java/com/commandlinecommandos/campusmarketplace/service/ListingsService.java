package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.dto.ListingDetailResponse;
import com.commandlinecommandos.campusmarketplace.dto.SellerSummary;
import com.commandlinecommandos.campusmarketplace.dto.ListingImage;
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
     * Get listings by seller
     */
    public Page<Product> getListingsBySeller(UUID sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findBySellerUserIdAndIsActiveTrue(sellerId, pageable);
    }

    /**
     * Update an existing listing
     */
    public Product updateListing(UUID listingId, Map<String, Object> updates, String username) {
        Product product = productRepository.findById(listingId)
            .orElseThrow(() -> new RuntimeException("Listing not found with id: " + listingId));

        // Verify ownership (unless admin)
        if (!product.getSeller().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: You can only update your own listings");
        }

        // Update fields if provided
        if (updates.containsKey("title")) {
            product.setTitle((String) updates.get("title"));
        }
        if (updates.containsKey("description")) {
            product.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("price")) {
            Object priceObj = updates.get("price");
            if (priceObj instanceof Number) {
                product.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
            } else if (priceObj instanceof String) {
                product.setPrice(new BigDecimal((String) priceObj));
            }
        }
        if (updates.containsKey("category")) {
            String categoryStr = (String) updates.get("category");
            try {
                product.setCategory(ProductCategory.valueOf(categoryStr));
            } catch (IllegalArgumentException e) {
                // Keep existing category if invalid
            }
        }
        if (updates.containsKey("condition")) {
            String conditionStr = (String) updates.get("condition");
            try {
                product.setCondition(ProductCondition.valueOf(conditionStr));
            } catch (IllegalArgumentException e) {
                // Keep existing condition if invalid
            }
        }
        if (updates.containsKey("location")) {
            product.setPickupLocation((String) updates.get("location"));
        }
        if (updates.containsKey("negotiable")) {
            product.setNegotiable((Boolean) updates.get("negotiable"));
        }
        if (updates.containsKey("quantity")) {
            Object quantityObj = updates.get("quantity");
            if (quantityObj instanceof Number) {
                product.setQuantity(((Number) quantityObj).intValue());
            }
        }

        return productRepository.save(product);
    }

    /**
     * Delete a listing (soft delete by setting isActive to false)
     */
    public void deleteListing(UUID listingId, String username) {
        Product product = productRepository.findById(listingId)
            .orElseThrow(() -> new RuntimeException("Listing not found with id: " + listingId));

        // Verify ownership (unless admin)
        if (!product.getSeller().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: You can only delete your own listings");
        }

        // Soft delete
        product.setActive(false);
        productRepository.save(product);
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

    /**
     * Convert Product to ListingDetailResponse DTO (new format matching frontend mockdata)
     */
    public ListingDetailResponse toListingDetailResponse(Product product) {
        return toListingDetailResponse(product, false);
    }

    /**
     * Convert Product to ListingDetailResponse DTO with favorite flag
     */
    public ListingDetailResponse toListingDetailResponse(Product product, boolean isFavorite) {
        ListingDetailResponse response = new ListingDetailResponse();
        
        // Basic fields
        response.setId(product.getProductId().toString());
        response.setTitle(product.getTitle());
        response.setDescription(product.getDescription());
        response.setCategory(product.getCategory().name());
        response.setCondition(product.getCondition().name());
        response.setPrice(product.getPrice().doubleValue());
        response.setLocation(product.getPickupLocation() != null ? product.getPickupLocation() : "Campus");
        
        // Seller information
        User seller = product.getSeller();
        SellerSummary sellerSummary = new SellerSummary();
        sellerSummary.setId(seller.getUserId().toString());
        sellerSummary.setUsername(seller.getUsername());
        String sellerName = (seller.getFirstName() != null ? seller.getFirstName() : "") +
                           " " + (seller.getLastName() != null ? seller.getLastName() : "");
        sellerSummary.setName(sellerName.trim().isEmpty() ? seller.getUsername() : sellerName.trim());
        sellerSummary.setAvatarUrl(seller.getAvatarUrl());
        response.setSeller(sellerSummary);
        response.setSellerId(seller.getUserId().toString());
        
        // Images - for now set main image URL and empty images array
        // TODO: Populate from ProductImage entity when available
        response.setImageUrl(null);
        response.setImages(new java.util.ArrayList<>());
        
        // Status mapping - convert isActive to status string
        response.setStatus(product.isActive() ? "ACTIVE" : "INACTIVE");
        
        // Metrics
        response.setViewCount(product.getViewCount() != null ? product.getViewCount() : 0);
        response.setFavoriteCount(product.getFavoriteCount() != null ? product.getFavoriteCount() : 0);
        response.setFavorite(isFavorite);
        
        // Additional fields
        response.setNegotiable(product.isNegotiable());
        
        // Timestamps
        response.setCreatedAt(product.getCreatedAt() != null ? product.getCreatedAt().toString() : "");
        response.setUpdatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : "");
        
        return response;
    }
}

