package com.commandlinecommandos.campusmarketplace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.commandlinecommandos.campusmarketplace.security.RequireRole;
import com.commandlinecommandos.campusmarketplace.model.UserRole;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {
    
    @GetMapping("/dashboard")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> getAdminDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Welcome to Admin Dashboard");
        dashboard.put("totalUsers", 150);
        dashboard.put("totalListings", 450);
        dashboard.put("pendingApprovals", 12);
        return ResponseEntity.ok(dashboard);
    }
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin access: All users data");
        response.put("userCount", 150);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/moderate/{listingId}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> moderateListing(@PathVariable Long listingId, @RequestParam String action) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format("Listing %d has been %s", listingId, action));
        response.put("listingId", listingId);
        response.put("action", action);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/users/{userId}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", String.format("User %d has been deleted", userId));
        response.put("userId", userId);
        return ResponseEntity.ok(response);
    }
}
