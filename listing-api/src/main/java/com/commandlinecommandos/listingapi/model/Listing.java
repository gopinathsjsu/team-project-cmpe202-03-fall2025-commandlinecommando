package com.commandlinecommandos.listingapi.model;

import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.*;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Slf4j
@Entity
@Table(name = "listings")
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long listingId;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @Size(min = 2, max = 255)
    @Column(name = "title", nullable = false)
    private String title;
    
    @Size(min = 10, max = 1000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false)
    private ItemCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ListingStatus status;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListingImage> images;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public Listing() {
    }

    public Listing(String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, Long sellerId) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.status = ListingStatus.ACTIVE;
        this.location = location;
        this.sellerId = sellerId;
        this.viewCount = 0;
        this.images = new ArrayList<>();
    }

    public Listing(String title, String description, BigDecimal price, Category category, ItemCondition condition,
            String location, Long sellerId, List<ListingImage> images) {
        this(title, description, price, category, condition, location, sellerId);
        this.images = images;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
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
    
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }
    
    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public void markAsSold() {
        log.info("Marking listing ID: {} as sold - previous status: {}", 
                this.listingId, this.status);
        setStatus(ListingStatus.SOLD);
        log.info("Successfully marked listing ID: {} as sold", this.listingId);
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
    
    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void incrementViewCount() {
        if (getStatus() == ListingStatus.ACTIVE) {
            int previousCount = getViewCount();
            setViewCount(previousCount + 1);
            log.debug("Incremented view count for listing ID: {} from {} to {}", 
                    this.listingId, previousCount, getViewCount());
        } else {
            log.debug("View count not incremented for listing ID: {} - status is not ACTIVE: {}", 
                    this.listingId, this.status);
        }
    }
    
    public List<ListingImage> getImages() {
        return images;
    }

    public void setImages(List<ListingImage> images) {
        this.images = images;
    }

    public void addImage(ListingImage image) {
        log.debug("Adding image to listing ID: {} - image display order: {}", 
                this.listingId, image.getDisplayOrder());
        this.images.add(image);
        image.setListing(this);
        log.debug("Successfully added image to listing ID: {} - total images: {}", 
                this.listingId, this.images.size());
    }

    public void addImages(List<ListingImage> images) {
        log.debug("Adding {} images to listing ID: {}", images.size(), this.listingId);
        for (ListingImage image : images) {
            addImage(image);
        }
        log.debug("Successfully added {} images to listing ID: {} - total images: {}", 
                images.size(), this.listingId, this.images.size());
    }

    public void removeImage(ListingImage image) {
        log.debug("Removing image ID: {} from listing ID: {}", 
                image.getImageId(), this.listingId);
        this.images.remove(image);
        image.setListing(null);
        log.debug("Successfully removed image from listing ID: {} - remaining images: {}", 
                this.listingId, this.images.size());
    }

    public void removeImages(List<ListingImage> images) {
        log.debug("Removing {} images from listing ID: {}", images.size(), this.listingId);
        for (ListingImage image : images) {
            removeImage(image);
        }
        log.debug("Successfully removed {} images from listing ID: {} - remaining images: {}", 
                images.size(), this.listingId, this.images.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Listing listing = (Listing) obj;
        return listingId.equals(listing.listingId);
    }

    @Override
    public int hashCode() {
        return listingId.hashCode();
    }
    
    @Override
    public String toString() {
        return "Listing [listingId=" + listingId + ", \ntitle=" + title + 
        ", \ndescription=" + description + ", \ncategory=" + category + 
        ", \nprice=" + price + ", \ncondition=" + condition + ", \nstatus=" + status + 
        ", \nlocation=" + location + ", \ncreatedAt=" + createdAt + ", \nupdatedAt=" + updatedAt + 
        ", \nviewCount=" + viewCount + ", \nimages=" + images + "]";
    }
}
