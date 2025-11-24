package com.commandlinecommandos.campusmarketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.commandlinecommandos.campusmarketplace.model.Product;
import com.commandlinecommandos.campusmarketplace.model.ProductCategory;
import com.commandlinecommandos.campusmarketplace.service.ListingsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentController {
    
    @Autowired
    private ListingsService listingsService;
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getStudentDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("message", "Welcome to Student Dashboard");
        dashboard.put("myListings", 5);
        dashboard.put("watchlist", 12);
        dashboard.put("messages", 3);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/listings")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<?> getListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<Product> productsPage = listingsService.getAllListings(page, size);
            
            // Convert to DTO format
            List<Map<String, Object>> listings = productsPage.getContent().stream()
                .map(listingsService::productToDto)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", listings);
            response.put("totalElements", productsPage.getTotalElements());
            response.put("totalPages", productsPage.getTotalPages());
            response.put("number", productsPage.getNumber());
            response.put("size", productsPage.getSize());
            response.put("first", productsPage.isFirst());
            response.put("last", productsPage.isLast());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to fetch listings");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/listings")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> createListing(@RequestBody Map<String, Object> listing) {
        try {
            Product product = listingsService.createListing(listing);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Listing created successfully");
            response.put("listing", listingsService.productToDto(product));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create listing");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
