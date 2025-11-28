package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.*;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.security.RequireRole;
import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.service.UserManagementService;
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
 * Admin controller for comprehensive user management
 * Includes search, create, update, suspend, reactivate, delete, bulk operations
 * 
 * Note: Frontend expects endpoints at /admin/users, so we'll add those as well
 */
@RestController
@RequestMapping({"/admin/user-management", "/admin/users"})
@Validated
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, maxAge = 3600)
public class AdminUserManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminUserManagementController.class);
    
    @Autowired
    private UserManagementService userManagementService;
    
    /**
     * Get all users (frontend expects GET /admin/users)
     */
    @GetMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            UserSearchRequest request = new UserSearchRequest();
            request.setPage(page);
            request.setSize(size);
            request.setSortBy("createdAt");
            request.setSortDirection("DESC");
            
            PagedResponse<UserResponse> users = userManagementService.searchUsers(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Users retrieved successfully");
            response.put("userCount", users.getTotalElements());
            response.put("users", users.getContent());
            response.put("totalElements", users.getTotalElements());
            response.put("totalPages", users.getTotalPages());
            response.put("currentPage", users.getPage());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve users", "message", e.getMessage()));
        }
    }
    
    /**
     * Search and filter users with pagination (GET version with query params)
     */
    @GetMapping("/search")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> searchUsersGet(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            // Build UserSearchRequest from query parameters
            UserSearchRequest request = new UserSearchRequest();
            request.setSearchTerm(searchTerm);
            request.setRole(role);
            request.setIsActive(isActive);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDirection(sortDirection);
            
            logger.info("Admin searching users with criteria: {}", request);
            PagedResponse<UserResponse> users = userManagementService.searchUsers(request);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Search failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Search and filter users with pagination (POST version with request body)
     */
    @PostMapping("/search")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> searchUsers(@Valid @RequestBody UserSearchRequest request) {
        try {
            logger.info("Admin searching users with criteria: {}", request);
            PagedResponse<UserResponse> users = userManagementService.searchUsers(request);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Search failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Get user by ID (admin view with full details)
     */
    @GetMapping("/{userId}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        try {
            UserResponse user = userManagementService.getUserProfile(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error getting user by ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found", "message", e.getMessage()));
        }
    }
    
    /**
     * Create new user account
     */
    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User admin = getCurrentUser();
            logger.info("Admin {} creating new user: {}", admin.getUsername(), request.getUsername());
            UserResponse newUser = userManagementService.createUser(request, admin);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "User creation failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Update user account
     */
    @PutMapping("/{userId}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        try {
            User admin = getCurrentUser();
            logger.info("Admin {} updating user: {}", admin.getUsername(), userId);
            UserResponse updatedUser = userManagementService.updateUser(userId, request, admin);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "User update failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Suspend user account
     */
    @PostMapping("/{userId}/suspend")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> suspendUser(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> body) {
        try {
            User admin = getCurrentUser();
            String reason = body.getOrDefault("reason", "Suspended by administrator");
            logger.info("Admin {} suspending user: {}", admin.getUsername(), userId);
            userManagementService.suspendUser(userId, reason, admin);
            return ResponseEntity.ok(Map.of(
                "message", "User suspended successfully",
                "userId", userId,
                "reason", reason
            ));
        } catch (Exception e) {
            logger.error("Error suspending user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "User suspension failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Reactivate user account
     */
    @PostMapping("/{userId}/reactivate")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> reactivateUser(@PathVariable UUID userId) {
        try {
            User admin = getCurrentUser();
            logger.info("Admin {} reactivating user: {}", admin.getUsername(), userId);
            userManagementService.reactivateUser(userId, admin);
            return ResponseEntity.ok(Map.of(
                "message", "User reactivated successfully",
                "userId", userId
            ));
        } catch (Exception e) {
            logger.error("Error reactivating user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "User reactivation failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Delete user account (soft delete)
     */
    @DeleteMapping("/{userId}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> deleteUser(
            @PathVariable UUID userId,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            User admin = getCurrentUser();
            String reason = body != null ? body.getOrDefault("reason", "Deleted by administrator") : "Deleted by administrator";
            logger.info("Admin {} deleting user: {}", admin.getUsername(), userId);
            userManagementService.deleteUser(userId, reason, admin);
            return ResponseEntity.ok(Map.of(
                "message", "User deleted successfully",
                "userId", userId
            ));
        } catch (Exception e) {
            logger.error("Error deleting user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "User deletion failed", "message", e.getMessage()));
        }
    }
    
    /**
     * Bulk user operations
     */
    @PostMapping("/bulk-action")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> bulkUserAction(@Valid @RequestBody BulkUserActionRequest request) {
        try {
            User admin = getCurrentUser();
            logger.info("Admin {} performing bulk action: {} on {} users", 
                admin.getUsername(), request.getAction(), request.getUserIds().size());
            Map<String, Object> result = userManagementService.bulkUserAction(request, admin);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error performing bulk action", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Bulk action failed", "message", e.getMessage()));
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

