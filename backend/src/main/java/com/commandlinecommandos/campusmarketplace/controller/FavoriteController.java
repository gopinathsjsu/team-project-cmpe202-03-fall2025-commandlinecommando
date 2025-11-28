package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.ListingDetailResponse;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserFavorite;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.service.FavoriteService;
import com.commandlinecommandos.campusmarketplace.service.ListingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for Favorites/Wishlist management
 * Allows users to save products to their favorites
 */
@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class FavoriteController {
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ListingsService listingsService;
    
    private User getCurrentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Get user's favorites - returns listings with favorite=true
     */
    @GetMapping
    public ResponseEntity<List<ListingDetailResponse>> getFavorites(Authentication auth) {
        User user = getCurrentUser(auth);
        Page<UserFavorite> favorites = favoriteService.getUserFavorites(user, Pageable.unpaged());
        
        // Convert to ListingDetailResponse with favorite=true
        List<ListingDetailResponse> listings = favorites.getContent().stream()
            .map(fav -> listingsService.toListingDetailResponse(fav.getProduct(), true))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(listings);
    }
    
    /**
     * Toggle favorite - matches mockdata API: POST /favorites/{id}
     * Returns { favorited: boolean }
     */
    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, Boolean>> toggleFavorite(@PathVariable UUID productId,
                                                                Authentication auth) {
        User user = getCurrentUser(auth);
        boolean isFavorited = favoriteService.isFavorited(user, productId);
        
        Map<String, Boolean> response = new HashMap<>();
        if (isFavorited) {
            // Remove from favorites
            favoriteService.removeFromFavorites(user, productId);
            response.put("favorited", false);
        } else {
            // Add to favorites
            favoriteService.addToFavorites(user, productId);
            response.put("favorited", true);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Remove product from favorites (legacy endpoint, kept for backward compatibility)
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable UUID productId,
                                                    Authentication auth) {
        User user = getCurrentUser(auth);
        favoriteService.removeFromFavorites(user, productId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Check if product is favorited
     */
    @GetMapping("/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> checkFavorited(@PathVariable UUID productId,
                                                               Authentication auth) {
        User user = getCurrentUser(auth);
        boolean isFavorited = favoriteService.isFavorited(user, productId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorited", isFavorited);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get favorite count for user
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getFavoriteCount(Authentication auth) {
        User user = getCurrentUser(auth);
        long count = favoriteService.countUserFavorites(user);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Clear all favorites
     */
    @DeleteMapping
    public ResponseEntity<Void> clearFavorites(Authentication auth) {
        User user = getCurrentUser(auth);
        favoriteService.clearAllFavorites(user);
        return ResponseEntity.noContent().build();
    }
}
