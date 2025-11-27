package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.model.ModerationStatus;
import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserReportRepository;
import com.commandlinecommandos.campusmarketplace.security.RequireRole;
import com.commandlinecommandos.campusmarketplace.service.ListingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserReportRepository reportRepository;
    
    
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
}
