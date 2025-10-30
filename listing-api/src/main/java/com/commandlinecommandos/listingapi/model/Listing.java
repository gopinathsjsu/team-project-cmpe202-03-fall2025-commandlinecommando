package com.commandlinecommandos.listingapi.model;

import jakarta.persistence.*;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

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

    @Transient
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
        setStatus(ListingStatus.SOLD);
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
            setViewCount(getViewCount() + 1);
        }
    }
    
    public List<ListingImage> getImages() {
        return images;
    }

    public void setImages(List<ListingImage> images) {
        this.images = images;
    }

    public void addImage(ListingImage image) {
        this.images.add(image);
    }

    public void addImages(List<ListingImage> images) {
        for (ListingImage image : images) {
            addImage(image);
        }
    }

    public void removeImage(ListingImage image) {
        this.images.remove(image);
    }

    public void removeImages(List<ListingImage> images) {
        for (ListingImage image : images) {
            removeImage(image);
        }
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
