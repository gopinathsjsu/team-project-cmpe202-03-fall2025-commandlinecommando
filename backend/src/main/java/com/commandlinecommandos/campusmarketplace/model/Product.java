package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Product entity for Campus Marketplace listings
 * Supports full-text search and flexible attributes
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_seller", columnList = "seller_id"),
    @Index(name = "idx_products_university", columnList = "university_id"),
    @Index(name = "idx_products_category", columnList = "category"),
    @Index(name = "idx_products_status", columnList = "moderation_status"),
    @Index(name = "idx_products_price", columnList = "price"),
    @Index(name = "idx_products_search", columnList = "university_id,category,is_active,price")
})
public class Product {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "product_id", updatable = false, nullable = false)
    private UUID productId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = true)
    private University university;
    
    // Product Information
    @NotNull
    @Column(nullable = false)
    private String title;
    
    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false)
    private ProductCondition condition;
    
    // Pricing
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;
    
    @Column(nullable = false)
    private boolean negotiable = false;
    
    // Inventory
    @Column(nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "sold_quantity")
    private Integer soldQuantity = 0;
    
    // Additional Attributes (flexible JSON for category-specific data)
    // e.g., {"isbn": "123", "edition": "5th", "author": "Smith"} for textbooks
    @Lob
    @Type(JsonType.class)
    @Column(name = "attributes", columnDefinition = "TEXT")
    private Map<String, Object> attributes = new HashMap<>();
    
    // Visibility & Status
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "is_featured")
    private boolean isFeatured = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status")
    private ModerationStatus moderationStatus = ModerationStatus.PENDING;
    
    // Analytics
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;
    
    // Delivery Options (stored as JSON array)
    @Lob
    @Type(JsonType.class)
    @Column(name = "delivery_methods", columnDefinition = "TEXT")
    private List<String> deliveryMethods = new ArrayList<>(List.of("CAMPUS_PICKUP"));
    
    @Column(name = "pickup_location")
    private String pickupLocation;
    
    // SEO & Search (search_vector handled by PostgreSQL trigger)
    
    // Timestamps
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Product() {
    }
    
    public Product(User seller, String title, String description, ProductCategory category, 
                   ProductCondition condition, BigDecimal price) {
        this.seller = seller;
        this.university = seller.getUniversity();
        this.title = title;
        this.description = description;
        this.category = category;
        this.condition = condition;
        this.price = price;
    }
    
    // Business methods
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void incrementFavoriteCount() {
        this.favoriteCount++;
    }
    
    public void decrementFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }
    
    public void publish() {
        this.publishedAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public boolean isAvailable() {
        return isActive && quantity > 0 && moderationStatus == ModerationStatus.APPROVED;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void decrementQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
            this.soldQuantity += amount;
        } else {
            throw new IllegalStateException("Insufficient quantity available");
        }
    }
    
    public BigDecimal getDiscountPercentage() {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = originalPrice.subtract(price);
            return discount.divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP)
                          .multiply(new BigDecimal(100));
        }
        return BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public UUID getProductId() {
        return productId;
    }
    
    public void setProductId(UUID productId) {
        this.productId = productId;
    }
    
    public User getSeller() {
        return seller;
    }
    
    public void setSeller(User seller) {
        this.seller = seller;
    }
    
    public University getUniversity() {
        return university;
    }
    
    public void setUniversity(University university) {
        this.university = university;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ProductCategory getCategory() {
        return category;
    }
    
    public void setCategory(ProductCategory category) {
        this.category = category;
    }
    
    public ProductCondition getCondition() {
        return condition;
    }
    
    public void setCondition(ProductCondition condition) {
        this.condition = condition;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public boolean isNegotiable() {
        return negotiable;
    }
    
    public void setNegotiable(boolean negotiable) {
        this.negotiable = negotiable;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Integer getSoldQuantity() {
        return soldQuantity;
    }
    
    public void setSoldQuantity(Integer soldQuantity) {
        this.soldQuantity = soldQuantity;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isFeatured() {
        return isFeatured;
    }
    
    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }
    
    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }
    
    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public Integer getFavoriteCount() {
        return favoriteCount;
    }
    
    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
    
    public List<String> getDeliveryMethods() {
        return deliveryMethods;
    }
    
    public void setDeliveryMethods(List<String> deliveryMethods) {
        this.deliveryMethods = deliveryMethods;
    }
    
    public String getPickupLocation() {
        return pickupLocation;
    }
    
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

