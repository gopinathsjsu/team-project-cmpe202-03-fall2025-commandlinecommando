package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;

/**
 * Listing image entity for storing multiple images per listing
 * Consolidated from listing-api
 */
@Entity
@Table(name = "listing_images")
public class ListingImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "alt_text", nullable = false)
    private String altText;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public ListingImage() {
    }

    public ListingImage(Long listingId, String imageUrl, String altText, Integer displayOrder) {
        this.listingId = listingId;
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

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ListingImage that = (ListingImage) obj;
        return imageId != null && imageId.equals(that.imageId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ListingImage{" +
                "imageId=" + imageId +
                ", listingId=" + listingId +
                ", imageUrl='" + imageUrl + '\'' +
                ", displayOrder=" + displayOrder +
                '}';
    }
}
