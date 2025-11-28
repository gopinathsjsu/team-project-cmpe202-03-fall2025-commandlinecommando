package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.CreateListingRequest;
import com.commandlinecommandos.campusmarketplace.dto.ListingDTO;
import com.commandlinecommandos.campusmarketplace.model.Category;
import com.commandlinecommandos.campusmarketplace.model.ItemCondition;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import com.commandlinecommandos.campusmarketplace.service.ListingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/listings")
public class ListingController {

    private final ListingService listingService;
    private final JwtUtil jwtUtil;

    public ListingController(ListingService listingService, JwtUtil jwtUtil) {
        this.listingService = listingService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ListingDTO> createListing(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateListingRequest request) {
        UUID userId = extractUserIdFromToken(token);
        ListingDTO listing = listingService.createListing(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(listing);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDTO> getListingById(@PathVariable Long id) {
        ListingDTO listing = listingService.getListingById(id);
        return ResponseEntity.ok(listing);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ListingDTO>> searchListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ItemCondition condition,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ListingDTO> listings = listingService.searchListings(
                keyword, category, condition, minPrice, maxPrice, location, page, size);
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/my-listings")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<List<ListingDTO>> getMyListings(@RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        List<ListingDTO> listings = listingService.getMyListings(userId);
        return ResponseEntity.ok(listings);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ListingDTO> updateListing(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateListingRequest request) {
        UUID userId = extractUserIdFromToken(token);
        ListingDTO listing = listingService.updateListing(id, userId, request);
        return ResponseEntity.ok(listing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteListing(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        listingService.deleteListing(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/mark-sold")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> markAsSold(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        listingService.markAsSold(id, userId);
        return ResponseEntity.ok().build();
    }

    private UUID extractUserIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        return UUID.fromString(jwtUtil.extractClaim(jwt, claims -> claims.get("userId", String.class)));
    }
}
