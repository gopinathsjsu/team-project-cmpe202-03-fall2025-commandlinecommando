package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.CreateAdminRequest;
import com.commandlinecommandos.campusmarketplace.dto.UserResponse;
import com.commandlinecommandos.campusmarketplace.model.ModerationStatus;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserReportRepository;
import com.commandlinecommandos.campusmarketplace.security.RequireRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserReportRepository reportRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    
    /**
     * Get admin dashboard with real statistics
     */
    @GetMapping("/dashboard")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Admin dashboard loaded");
        
        // Total users
        dashboard.put("totalUsers", userRepository.count());
        
        // Total listings (all products)
        dashboard.put("totalListings", productRepository.count());
        
        // Pending approvals (products with PENDING moderation status)
        dashboard.put("pendingApprovals", productRepository.findByModerationStatus(ModerationStatus.PENDING).size());
        
        // Pending reports
        dashboard.put("pendingReports", reportRepository.countByStatus(ModerationStatus.PENDING));
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Moderate a listing (approve/reject/flag)
     */
    @PostMapping("/moderate/{listingId}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> moderateListing(
            @PathVariable UUID listingId, 
            @RequestParam String action) {
        try {
            var product = productRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
            
            ModerationStatus newStatus;
            switch (action.toLowerCase()) {
                case "approve":
                    newStatus = ModerationStatus.APPROVED;
                    break;
                case "reject":
                    newStatus = ModerationStatus.REJECTED;
                    break;
                case "flag":
                    newStatus = ModerationStatus.FLAGGED;
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid action. Use: approve, reject, or flag"));
            }
            
            product.setModerationStatus(newStatus);
            productRepository.save(product);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", String.format("Listing %s has been %s", listingId, action + "d"));
            response.put("listingId", listingId.toString());
            response.put("action", action);
            response.put("status", newStatus.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to moderate listing", "message", e.getMessage()));
        }
    }
    
    /**
     * Create a new admin user (admin only).
     * 
     * Admin accounts have exclusive ADMIN role and cannot buy/sell.
     * Only existing admins can create new admin accounts.
     */
    @PostMapping("/users/admin")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        logger.info("Admin creation attempt for username: {}", request.getUsername());
        
        try {
            // Check if username already exists
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                logger.warn("Admin creation failed - username already exists: {}", request.getUsername());
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username already exists"));
            }
            
            // Check if email already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                logger.warn("Admin creation failed - email already exists: {}", request.getEmail());
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email already exists"));
            }
            
            // Create admin user
            User admin = new User();
            admin.setUsername(request.getUsername());
            admin.setEmail(request.getEmail());
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
            admin.setFirstName(request.getFirstName());
            admin.setLastName(request.getLastName());
            admin.setPhone(request.getPhone());
            
            // Admin gets exclusive ADMIN role (cannot be BUYER or SELLER)
            // Use mutable HashSet for JPA @ElementCollection compatibility
            admin.setRoles(new HashSet<>(Set.of(UserRole.ADMIN)));
            admin.setActive(true);
            
            // Store admin-specific info in preferences
            Map<String, Object> preferences = new HashMap<>();
            if (request.getAdminLevel() != null) {
                preferences.put("admin_level", request.getAdminLevel());
            }
            if (request.getDepartment() != null) {
                preferences.put("department", request.getDepartment());
            }
            admin.setPreferences(preferences);
            
            admin = userRepository.save(admin);
            
            // Get the creating admin for audit log
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String creatorUsername = authentication != null ? authentication.getName() : "system";
            
            logger.info("Admin account created successfully: {} by {}", 
                admin.getUsername(), creatorUsername);
            
            // Build response
            UserResponse response = UserResponse.builder()
                .userId(admin.getUserId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .phone(admin.getPhone())
                .roles(admin.getRoles())
                .isActive(admin.isActive())
                .createdAt(admin.getCreatedAt())
                .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error during admin creation for username: {}", 
                request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create admin", "message", e.getMessage()));
        }
    }
}
