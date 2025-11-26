package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.exception.ResourceNotFoundException;
import com.commandlinecommandos.campusmarketplace.exception.BadRequestException;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.Product;
import com.commandlinecommandos.campusmarketplace.model.UserFavorite;
import com.commandlinecommandos.campusmarketplace.repository.UserFavoriteRepository;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for Favorites/Wishlist management
 * Allows users to save products to their favorites
 */
@Service
@Transactional
public class FavoriteService {
    
    @Autowired
    private UserFavoriteRepository favoriteRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Add product to favorites
     */
    public UserFavorite addToFavorites(User user, UUID productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if already favorited
        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            throw new BadRequestException("Product already in favorites");
        }
        
        // Don't allow users to favorite their own products
        if (product.getSeller().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Cannot favorite your own product");
        }
        
        UserFavorite favorite = new UserFavorite(user, product);
        
        // Update product favorite count
        product.setFavoriteCount(product.getFavoriteCount() + 1);
        productRepository.save(product);
        
        return favoriteRepository.save(favorite);
    }
    
    /**
     * Remove product from favorites
     */
    public void removeFromFavorites(User user, UUID productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        UserFavorite favorite = favoriteRepository.findByUserAndProduct(user, product)
            .orElseThrow(() -> new ResourceNotFoundException("Product not in favorites"));
        
        // Update product favorite count
        if (product.getFavoriteCount() > 0) {
            product.setFavoriteCount(product.getFavoriteCount() - 1);
            productRepository.save(product);
        }
        
        favoriteRepository.delete(favorite);
    }
    
    /**
     * Get user's favorites
     */
    public Page<UserFavorite> getUserFavorites(User user, Pageable pageable) {
        return favoriteRepository.findByUserWithProduct(user, pageable);
    }
    
    /**
     * Check if product is favorited by user
     */
    public boolean isFavorited(User user, UUID productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return favoriteRepository.existsByUserAndProduct(user, product);
    }
    
    /**
     * Count user's favorites
     */
    public long countUserFavorites(User user) {
        return favoriteRepository.countByUser(user);
    }
    
    /**
     * Count favorites for a product
     */
    public long countProductFavorites(UUID productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return favoriteRepository.countByProduct(product);
    }
    
    /**
     * Clear all favorites for user
     */
    public void clearAllFavorites(User user) {
        Page<UserFavorite> favorites = favoriteRepository.findByUserWithProduct(user, Pageable.unpaged());
        
        // Update product counts
        favorites.forEach(fav -> {
            Product product = fav.getProduct();
            if (product.getFavoriteCount() > 0) {
                product.setFavoriteCount(product.getFavoriteCount() - 1);
            }
        });
        
        favoriteRepository.deleteAll(favorites);
    }
}
