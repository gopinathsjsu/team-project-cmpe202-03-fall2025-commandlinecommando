package com.commandlinecommandos.listingapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.commandlinecommandos.listingapi.model.Listing;
import com.commandlinecommandos.listingapi.model.ListingImage;
import com.commandlinecommandos.listingapi.model.Category;
import com.commandlinecommandos.listingapi.model.ItemCondition;
import com.commandlinecommandos.listingapi.model.ListingStatus;
import com.commandlinecommandos.listingapi.service.ListingService;
import com.commandlinecommandos.listingapi.service.FileStorageService;
import com.commandlinecommandos.listingapi.security.JwtHelper;
import com.commandlinecommandos.listingapi.dto.CreateListingRequest;
import com.commandlinecommandos.listingapi.dto.UpdateListingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private static final Logger logger = LoggerFactory.getLogger(ListingController.class);

    @Autowired
    private ListingService listingService;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private com.commandlinecommandos.listingapi.service.SearchProxyService searchProxyService;

    @Autowired
    private JwtHelper jwtHelper;

    @GetMapping
    public ResponseEntity<Page<?>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Received request to get all listings - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                   page, size, sortBy, sortDirection);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Listing> listings = listingService.getAllListings(pageable);
        
        logger.info("Successfully retrieved {} listings (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), page + 1, listings.getTotalPages(), listings.getTotalElements());
        
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<?>> searchListings(
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ItemCondition condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Received search request - keyword: '{}', status: {}, category: {}, condition: {}, " +
                   "priceRange: {} - {}, location: '{}', page: {}, size: {}", 
                   keyword, status, category, condition, minPrice, maxPrice, location, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Listing> listings = listingService.getListingsWithFilters(status, keyword, category, condition, minPrice, maxPrice, location, pageable);
        
        logger.info("Search completed - found {} listings (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), page + 1, listings.getTotalPages(), listings.getTotalElements());
        
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<?>> getListingsBySellerId(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Received request to get listings for seller ID: {} - page: {}, size: {}, sortBy: {}", 
                   sellerId, page, size, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Listing> listings = listingService.getListingsBySellerId(sellerId, pageable);
        
        logger.info("Successfully retrieved {} listings for seller ID: {} (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), sellerId, page + 1, listings.getTotalPages(), listings.getTotalElements());
        
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<?> getListingById(@PathVariable Long listingId) {
        logger.info("Received request to get listing by ID: {}", listingId);
        
        Listing listing = listingService.getListingById(listingId);
        logger.info("Successfully retrieved listing ID: {} - title: '{}', seller: {}, status: {}", 
                   listingId, listing.getTitle(), listing.getSellerId(), listing.getStatus());
        
        int newViewCount = listingService.incrementViewCount(listingId);
        logger.debug("Incremented view count for listing ID: {} to {}", listingId, newViewCount);
        
        return ResponseEntity.ok(listing);
    }

    @PostMapping("/")
    public ResponseEntity<?> createListing(@Valid @RequestBody CreateListingRequest request, HttpServletRequest httpRequest) {
        logger.info("Received request to create listing - title: '{}', price: {}, category: {}, condition: {}, location: '{}'", 
                   request.getTitle(), request.getPrice(), request.getCategory(), request.getCondition(), request.getLocation());
        
        
        Long sellerId = jwtHelper.extractUserIdFromRequest(httpRequest);
        if (sellerId == null) {
            logger.warn("Unauthorized listing creation attempt - no valid JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        logger.debug("Using seller ID: {} from JWT for listing creation", sellerId);
        
        Listing createdListing = listingService.createListing(request.getTitle(), request.getDescription(), request.getPrice(),
            request.getCategory(), request.getCondition(), request.getLocation(), sellerId);

        logger.info("Successfully created listing ID: {} with title: '{}' for seller ID: {}", 
                   createdListing.getListingId(), createdListing.getTitle(), sellerId);
        
        return ResponseEntity.ok(createdListing);
    }

    @PostMapping("/{listingId}/images")
    public ResponseEntity<?> uploadImages(@PathVariable Long listingId,
            @RequestParam("images") List<MultipartFile> images, @RequestParam("displayOrders") int[] displayOrders,
            HttpServletRequest httpRequest) {
        logger.info("Received request to upload {} images for listing ID: {}", images.size(), listingId);
        
        Long sellerId = jwtHelper.extractUserIdFromRequest(httpRequest);
        if (sellerId == null) {
            logger.warn("Unauthorized image upload attempt - no valid JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        logger.debug("Using seller ID: {} from JWT for image upload authorization", sellerId);
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            logger.warn("Unauthorized image upload attempt - listing ID: {}, seller ID: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload images for this listing");
        }

        Listing listing = listingService.getListingById(listingId);
        logger.debug("Retrieved listing ID: {} for image upload", listingId);
        
        List<ListingImage> storedImages = fileStorageService.storeFiles(images, listing, displayOrders);
        logger.info("Successfully stored {} images for listing ID: {}", storedImages.size(), listingId);
        
        Listing updatedListing = listingService.addImagesToListing(listingId, storedImages);
        logger.info("Successfully added images to listing ID: {} - total images now: {}", 
                   listingId, updatedListing.getImages().size());
        
        return ResponseEntity.ok(updatedListing);
    }

    @PutMapping("/{listingId}")
    public ResponseEntity<?> updateListing(@PathVariable Long listingId, @Valid @RequestBody UpdateListingRequest request,
            HttpServletRequest httpRequest) {
        logger.info("Received request to update listing ID: {} - title: '{}', price: {}", 
                   listingId, request.getTitle(), request.getPrice());
        
        Long sellerId = jwtHelper.extractUserIdFromRequest(httpRequest);
        if (sellerId == null) {
            logger.warn("Unauthorized listing update attempt - no valid JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        logger.debug("Using seller ID: {} from JWT for listing update authorization", sellerId);
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            logger.warn("Unauthorized listing update attempt - listing ID: {}, seller ID: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this listing");
        }

        Listing updatedListing = listingService.updateListing(listingId, request.getTitle(), request.getDescription(), request.getPrice(),
            request.getCategory(), request.getCondition(), request.getLocation(), request.getImages());
        
        logger.info("Successfully updated listing ID: {} - new title: '{}', price: {}, status: {}", 
                   listingId, updatedListing.getTitle(), updatedListing.getPrice(), updatedListing.getStatus());
        
        return ResponseEntity.ok(updatedListing);
    }

    @PutMapping("/{listingId}/sold")
    public ResponseEntity<?> markAsSold(@PathVariable Long listingId, HttpServletRequest httpRequest) {
        logger.info("Received request to mark listing ID: {} as sold", listingId);
        
        Long sellerId = jwtHelper.extractUserIdFromRequest(httpRequest);
        if (sellerId == null) {
            logger.warn("Unauthorized mark as sold attempt - no valid JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        logger.debug("Using seller ID: {} from JWT for mark as sold authorization", sellerId);
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            logger.warn("Unauthorized mark as sold attempt - listing ID: {}, seller ID: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to mark this listing as sold");
        }

        Listing updatedListing = listingService.markAsSold(listingId);
        logger.info("Successfully marked listing ID: {} as sold - status changed to: {}", 
                   listingId, updatedListing.getStatus());
        
        return ResponseEntity.ok(updatedListing);
    }

    @PutMapping("/{listingId}/cancel")
    public ResponseEntity<?> cancelListing(@PathVariable Long listingId, HttpServletRequest httpRequest) {
        logger.info("Received request to cancel listing ID: {}", listingId);
        
        Long sellerId = jwtHelper.extractUserIdFromRequest(httpRequest);
        if (sellerId == null) {
            logger.warn("Unauthorized listing cancellation attempt - no valid JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        logger.debug("Using seller ID: {} from JWT for listing cancellation authorization", sellerId);
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            logger.warn("Unauthorized listing cancellation attempt - listing ID: {}, seller ID: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to cancel this listing");
        }

        Listing updatedListing = listingService.cancelListing(listingId);
        logger.info("Successfully cancelled listing ID: {} - status changed to: {}", 
                   listingId, updatedListing.getStatus());
        
        return ResponseEntity.ok(updatedListing);
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<String> deleteListing(@PathVariable Long listingId, HttpServletRequest httpRequest) {
        logger.info("Received request to delete listing ID: {}", listingId);
        
        Long sellerId = jwtHelper.extractUserIdFromRequest(httpRequest);
        if (sellerId == null) {
            logger.warn("Unauthorized listing deletion attempt - no valid JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        logger.debug("Using seller ID: {} from JWT for listing deletion authorization", sellerId);
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            logger.warn("Unauthorized listing deletion attempt - listing ID: {}, seller ID: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this listing");
        }

        listingService.deleteListing(listingId);
        logger.info("Successfully deleted listing ID: {}", listingId);
        
        return ResponseEntity.ok("Listing deleted successfully");
    }

    // ========================================================================
    // PROXY ENDPOINTS FOR ENHANCED SEARCH (EPIC 3)
    // ========================================================================
    
    /**
     * Enhanced search endpoint (v2) - Proxies to main backend
     * Provides full-text search, advanced filtering, and discovery features
     * 
     * @param request Search request body
     * @param token Authorization header
     * @return Enhanced search response from backend
     */
    @PostMapping("/search/v2")
    public ResponseEntity<?> searchV2(
            @RequestBody java.util.Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        try {
            logger.info("Proxying enhanced search request to backend: query={}",
                       request.get("query"));
            
            java.util.Map<String, Object> response = searchProxyService.proxySearchRequest(request, token);
            
            logger.info("Enhanced search proxy completed successfully");
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.client.RestClientException e) {
            logger.error("Error proxying search request to backend: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("Error communicating with backend service");
        } catch (Exception e) {
            logger.error("Unexpected error in search proxy: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
        }
    }
    
    /**
     * Autocomplete endpoint - Proxies to main backend
     * 
     * @param q Search query
     * @param token Authorization header
     * @return List of autocomplete suggestions
     */
    @GetMapping("/search/autocomplete")
    public ResponseEntity<?> autocomplete(
            @RequestParam String q,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        try {
            logger.debug("Proxying autocomplete request to backend: query={}", q);
            
            Object response = searchProxyService.proxyAutocompleteRequest(q, token);
            
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.client.RestClientException e) {
            logger.error("Error proxying autocomplete request to backend: {}", e.getMessage(), e);
            return ResponseEntity.ok(java.util.List.of());  // Return empty list on error
        } catch (Exception e) {
            logger.error("Unexpected error in autocomplete proxy: {}", e.getMessage(), e);
            return ResponseEntity.ok(java.util.List.of());  // Return empty list on error
        }
    }
    
    /**
     * Discovery endpoints - Proxy to main backend
     * Supports: trending, recommended, similar/{productId}, recently-viewed
     * 
     * @param endpoint Discovery endpoint type
     * @param token Authorization header
     * @return Discovery response from backend
     */
    @GetMapping("/discovery/{endpoint}")
    public ResponseEntity<?> discovery(
            @PathVariable String endpoint,
            @RequestParam java.util.Map<String, String> params,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        try {
            logger.debug("Proxying discovery request to backend: endpoint={}", endpoint);
            
            Object response = searchProxyService.proxyDiscoveryRequest(endpoint, params, token);
            
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.client.RestClientException e) {
            logger.error("Error proxying discovery request to backend: {}", e.getMessage(), e);
            return ResponseEntity.ok(java.util.List.of());  // Return empty list on error
        } catch (Exception e) {
            logger.error("Unexpected error in discovery proxy: {}", e.getMessage(), e);
            return ResponseEntity.ok(java.util.List.of());  // Return empty list on error
        }
    }
}