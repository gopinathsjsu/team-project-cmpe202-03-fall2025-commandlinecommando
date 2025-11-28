package com.commandlinecommandos.campusmarketplace.dto;

/**
 * DTO for listing image information
 * Matches frontend mockdata images array structure
 */
public class ListingImage {

    private Long imageId;
    private String imageUrl;
    private String altText;
    private Integer displayOrder;

    // Constructors
    public ListingImage() {
    }

    public ListingImage(Long imageId, String imageUrl, String altText, Integer displayOrder) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.displayOrder = displayOrder;
    }

    // Getters and Setters
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
