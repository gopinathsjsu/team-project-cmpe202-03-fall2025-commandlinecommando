package com.commandlinecommandos.campusmarketplace.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete listing response DTO that matches frontend mockdata structure
 * Includes nested seller object, images array, and all listing fields
 */
public class ListingDetailResponse {

    private String id;  // UUID as string
    private String title;
    private String description;
    private String category;  // ProductCategory enum as string
    private String condition;  // ProductCondition enum as string
    private Double price;
    private String location;
    private String sellerId;
    private SellerSummary seller;  // Nested seller object
    private String imageUrl;  // Primary image
    private List<ListingImage> images = new ArrayList<>();  // All images
    private String status;  // Mapped from moderationStatus: "ACTIVE", "PENDING", "SOLD"
    private Integer viewCount;
    private Integer favoriteCount;
    private Boolean negotiable;
    private Boolean favorite;  // User-specific field
    private String createdAt;  // ISO-8601 formatted
    private String updatedAt;  // ISO-8601 formatted

    // Constructors
    public ListingDetailResponse() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public SellerSummary getSeller() {
        return seller;
    }

    public void setSeller(SellerSummary seller) {
        this.seller = seller;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<ListingImage> getImages() {
        return images;
    }

    public void setImages(List<ListingImage> images) {
        this.images = images;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Boolean getNegotiable() {
        return negotiable;
    }

    public void setNegotiable(Boolean negotiable) {
        this.negotiable = negotiable;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
