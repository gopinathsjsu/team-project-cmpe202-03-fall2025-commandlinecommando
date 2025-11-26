package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserFavorite entity - Wishlist/favorites feature
 * Users can save products to their favorites list
 */
@Entity
@Table(name = "user_favorites", 
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_product_favorite", columnNames = {"user_id", "product_id"})
    },
    indexes = {
        @Index(name = "idx_user_favorites_user", columnList = "user_id,created_at"),
        @Index(name = "idx_user_favorites_product", columnList = "product_id")
    }
)
public class UserFavorite {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "favorite_id", updatable = false, nullable = false)
    private UUID favoriteId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public UserFavorite() {
    }
    
    public UserFavorite(User user, Product product) {
        this.user = user;
        this.product = product;
    }
    
    // Getters and Setters
    public UUID getFavoriteId() {
        return favoriteId;
    }
    
    public void setFavoriteId(UUID favoriteId) {
        this.favoriteId = favoriteId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
