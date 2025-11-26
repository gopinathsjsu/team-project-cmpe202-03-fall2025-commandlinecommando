package com.commandlinecommandos.campusmarketplace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Student-specific dashboard and operations
 * Note: Listing management has been moved to ListingController at /api/listings
 */
@RestController
@RequestMapping("/student")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentController {

    /**
     * Get student dashboard with stats and quick info
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getStudentDashboard(Authentication authentication) {
        try {
            UUID userId = UUID.fromString(authentication.getName());

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("message", "Welcome to Student Dashboard");
            dashboard.put("userId", userId.toString());
            dashboard.put("username", authentication.getName());

            // Get actual stats
            // Note: These can be expanded with real data from services
            dashboard.put("myListings", 0); // Can query listingsService
            dashboard.put("watchlist", 0);
            dashboard.put("messages", 0);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to load dashboard");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Get student profile information
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getStudentProfile(Authentication authentication) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", authentication.getName());
        profile.put("message", "Student profile endpoint");
        return ResponseEntity.ok(profile);
    }
}
