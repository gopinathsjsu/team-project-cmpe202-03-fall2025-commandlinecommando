package com.commandlinecommandos.campusmarketplace.listing.controller;

import com.commandlinecommandos.campusmarketplace.dto.ErrorResponse;
import com.commandlinecommandos.campusmarketplace.dto.ListingDetailResponse;
import com.commandlinecommandos.campusmarketplace.model.Product;
import com.commandlinecommandos.campusmarketplace.model.ProductCategory;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.service.ListingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Listing/Product Management
 * Consolidated from listing-api microservice
 * Handles CRUD operations for marketplace listings
 */
@RestController
@RequestMapping("/listings")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Listings", description = "Marketplace listing management endpoints")
public class ListingController {

    private static final Logger log = LoggerFactory.getLogger(ListingController.class);

    @Autowired
    private ListingsService listingsService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Get all listings with pagination and optional filtering
     */
    @GetMapping
    @Operation(summary = "Get all listings", description = "Retrieve paginated list of all active listings")
    public ResponseEntity<?> getAllListings(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by category")
            @RequestParam(required = false) ProductCategory category) {
        try {
            log.info("Fetching listings - page: {}, size: {}, category: {}", page, size, category);

            Page<Product> productsPage;
            if (category != null) {
                productsPage = listingsService.getListingsByCategory(category, page, size);
            } else {
                productsPage = listingsService.getAllListings(page, size);
            }

            // Convert to new DTO format matching frontend mockdata
            List<ListingDetailResponse> listings = productsPage.getContent().stream()
                .map(listingsService::toListingDetailResponse)
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
            log.error("Error fetching listings: {}", e.getMessage(), e);
            ErrorResponse error = new ErrorResponse(
                "LISTING_FETCH_ERROR",
                "Failed to fetch listings: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "/listings"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get a specific listing by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get listing by ID", description = "Retrieve detailed information about a specific listing")
    public ResponseEntity<?> getListingById(
            @Parameter(description = "Listing UUID")
            @PathVariable UUID id) {
        try {
            log.info("Fetching listing with ID: {}", id);
            Product product = listingsService.getListingById(id);
            
            // Use new DTO format matching frontend mockdata
            ListingDetailResponse listing = listingsService.toListingDetailResponse(product);
            return ResponseEntity.ok(listing);
        } catch (Exception e) {
            log.error("Error fetching listing {}: {}", id, e.getMessage());
            ErrorResponse error = new ErrorResponse(
                "LISTING_NOT_FOUND",
                "Listing not found: " + e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                "/listings/" + id
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Create a new listing
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'SELLER', 'ADMIN')")
    @Operation(summary = "Create new listing", description = "Create a new marketplace listing")
    public ResponseEntity<?> createListing(
            @Valid @RequestBody Map<String, Object> listing,
            Authentication authentication) {
        try {
            log.info("Creating new listing by user: {}", authentication.getName());

            // Add authenticated user ID to listing
            listing.put("sellerId", authentication.getName());

            Product product = listingsService.createListing(listing);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Listing created successfully");
            response.put("listing", listingsService.toListingDetailResponse(product));

            log.info("Listing created successfully with ID: {}", product.getProductId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating listing: {}", e.getMessage(), e);
            ErrorResponse error = new ErrorResponse(
                "LISTING_CREATE_ERROR",
                "Failed to create listing: " + e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                "/listings"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update an existing listing
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'SELLER', 'ADMIN')")
    @Operation(summary = "Update listing", description = "Update an existing listing")
    public ResponseEntity<?> updateListing(
            @Parameter(description = "Listing UUID")
            @PathVariable UUID id,
            @Valid @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        try {
            log.info("Updating listing {} by user: {}", id, authentication.getName());

            Product product = listingsService.updateListing(id, updates, authentication.getName());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Listing updated successfully");
            response.put("listing", listingsService.toListingDetailResponse(product));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating listing {}: {}", id, e.getMessage());
            ErrorResponse error = new ErrorResponse(
                "LISTING_UPDATE_ERROR",
                "Failed to update listing: " + e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                "/listings/" + id
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete a listing
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'SELLER', 'ADMIN')")
    @Operation(summary = "Delete listing", description = "Delete a listing (soft delete)")
    public ResponseEntity<?> deleteListing(
            @Parameter(description = "Listing UUID")
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            log.info("Deleting listing {} by user: {}", id, authentication.getName());

            listingsService.deleteListing(id, authentication.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Listing deleted successfully");
            response.put("listingId", id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting listing {}: {}", id, e.getMessage());
            ErrorResponse error = new ErrorResponse(
                "LISTING_DELETE_ERROR",
                "Failed to delete listing: " + e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                "/listings/" + id
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get listings by seller
     */
    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get listings by seller", description = "Retrieve all listings from a specific seller")
    public ResponseEntity<?> getListingsBySeller(
            @Parameter(description = "Seller user UUID")
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Fetching listings for seller: {}", sellerId);

            Page<Product> productsPage = listingsService.getListingsBySeller(sellerId, page, size);

            List<ListingDetailResponse> listings = productsPage.getContent().stream()
                .map(listingsService::toListingDetailResponse)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", listings);
            response.put("totalElements", productsPage.getTotalElements());
            response.put("totalPages", productsPage.getTotalPages());
            response.put("number", productsPage.getNumber());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching listings for seller {}: {}", sellerId, e.getMessage());
            ErrorResponse error = new ErrorResponse(
                "LISTING_FETCH_ERROR",
                "Failed to fetch seller listings: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "/listings/seller/" + sellerId
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get my listings (authenticated user)
     */
    @GetMapping("/my-listings")
    @PreAuthorize("hasAnyRole('STUDENT', 'SELLER', 'ADMIN')")
    @Operation(summary = "Get my listings", description = "Retrieve all listings created by the authenticated user")
    public ResponseEntity<?> getMyListings(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            String username = authentication.getName();
            log.info("Fetching listings for authenticated user: {}", username);
            
            // Find user by username to get their ID
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            return getListingsBySeller(user.getUserId(), page, size);
        } catch (Exception e) {
            log.error("Error fetching user's listings: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(
                "LISTING_FETCH_ERROR",
                "Failed to fetch your listings: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "/listings/my-listings"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
