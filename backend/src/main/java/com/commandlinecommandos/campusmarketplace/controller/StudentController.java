package com.commandlinecommandos.campusmarketplace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for User-specific dashboard and operations
 * Accessible by BUYER and SELLER roles (previously STUDENT)
 * Note: Listing management has been moved to ListingController at /api/listings
 */
@RestController
@RequestMapping("/student")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentController {

    /**
     * Get user dashboard with stats and quick info
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> getStudentDashboard(Authentication authentication) {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Welcome to Student Dashboard");
        dashboard.put("username", authentication.getName());

        // Get actual stats
        // Note: These can be expanded with real data from services
        dashboard.put("myListings", 0); // Can query listingsService
        dashboard.put("watchlist", 0);
        dashboard.put("messages", 0);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get user profile information
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> getStudentProfile(Authentication authentication) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", authentication.getName());
        profile.put("message", "Student profile endpoint");
        return ResponseEntity.ok(profile);
    }

    /**
     * Get user listings
     * Note: This is a test endpoint. Real listing management is at /api/listings
     */
    @GetMapping({"/listings", "/my-listings"})
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> getStudentListings(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Student listings endpoint");
        response.put("username", authentication.getName());
        response.put("listings", java.util.Collections.emptyList()); // Return empty list for now
        return ResponseEntity.ok(response);
    }

    /**
     * Create user listing
     * Note: This is a test endpoint. Real listing creation is at /api/listings
     */
    @PostMapping("/listings")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> createStudentListing(Authentication authentication, @RequestBody Map<String, Object> listing) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Listing created successfully");
        response.put("username", authentication.getName());
        response.put("listing", listing);
        return ResponseEntity.ok(response);
    }
}
