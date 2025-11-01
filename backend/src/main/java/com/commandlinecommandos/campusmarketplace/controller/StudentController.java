package com.commandlinecommandos.campusmarketplace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.commandlinecommandos.campusmarketplace.security.RequireRole;
import com.commandlinecommandos.campusmarketplace.model.UserRole;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/student")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentController {
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<?> getStudentDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Welcome to Student Dashboard");
        dashboard.put("myListings", 5);
        dashboard.put("watchlist", 12);
        dashboard.put("messages", 3);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/listings")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN')")
    public ResponseEntity<?> getMyListings() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Student's listings");
        response.put("listings", "List of student's listings would go here");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listings")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER')")
    public ResponseEntity<?> createListing(@RequestBody Map<String, Object> listing) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Listing created successfully");
        response.put("listingId", 123);
        response.put("title", listing.get("title"));
        return ResponseEntity.ok(response);
    }
}
