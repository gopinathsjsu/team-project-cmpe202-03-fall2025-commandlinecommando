package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.*;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.service.UserManagementService;
import com.commandlinecommandos.campusmarketplace.service.VerificationTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for user profile management
 * Handles profile viewing, updating, password changes, account deactivation
 */
@RestController
@RequestMapping("/users")
@Validated
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, maxAge = 3600)
public class UserProfileController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    
    @Autowired
    private UserManagementService userManagementService;
    
    @Autowired
    private VerificationTokenService verificationTokenService;
    
    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            User user = getCurrentUser();
            UserResponse profile = userManagementService.getUserProfile(user.getUserId());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error getting user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve profile", "message", e.getMessage()));
        }
    }
    
    /**
     * Get user profile by ID (requires authentication)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable UUID userId) {
        try {
            UserResponse profile = userManagementService.getUserProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            logger.error("Error getting user profile for ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found", "message", e.getMessage()));
        }
    }
    
    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        try {
            User user = getCurrentUser();
            UserResponse updatedProfile = userManagementService.updateUserProfile(user.getUserId(), request);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Profile update failed");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Unknown error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        try {
            User user = getCurrentUser();
            userManagementService.changePassword(user.getUserId(), request);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            logger.error("Error changing password", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Password change failed");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Unknown error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Deactivate account (soft delete with 30-day recovery)
     */
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount() {
        try {
            User user = getCurrentUser();
            userManagementService.deactivateAccount(user.getUserId());
            return ResponseEntity.ok(Map.of(
                "message", "Account deactivated successfully",
                "note", "You have 30 days to recover your account by contacting support"
            ));
        } catch (Exception e) {
            logger.error("Error deactivating account", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Account deactivation failed");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Unknown error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Helper method to get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }
}

