package com.commandlinecommandos.listingapi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<?>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting all listings with pagination - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                page, size, sortBy, sortDirection);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Listing> listings = listingService.getAllListings(pageable);
        
        log.info("Successfully retrieved {} listings (page {} of {})", 
                listings.getContent().size(), page + 1, listings.getTotalPages());
        
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

        log.info("Searching listings with filters - status: {}, keyword: {}, category: {}, condition: {}, " +
                "minPrice: {}, maxPrice: {}, location: {}, page: {}, size: {}", 
                status, keyword, category, condition, minPrice, maxPrice, location, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Listing> listings = listingService.getListingsWithFilters(status, keyword, category, condition, minPrice, maxPrice, location, pageable);
        
        log.info("Search completed - found {} listings matching criteria (page {} of {})", 
                listings.getContent().size(), page + 1, listings.getTotalPages());
        
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<?>> getListingsBySellerId(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Getting listings for seller ID: {} with pagination - page: {}, size: {}", 
                sellerId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Listing> listings = listingService.getListingsBySellerId(sellerId, pageable);
        
        log.info("Successfully retrieved {} listings for seller ID: {} (page {} of {})", 
                listings.getContent().size(), sellerId, page + 1, listings.getTotalPages());
        
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<?> getListingById(@PathVariable Long listingId) {
        log.info("Getting listing by ID: {}", listingId);
        
        Listing listing = listingService.getListingById(listingId);
        listingService.incrementViewCount(listingId);
        
        log.info("Successfully retrieved listing ID: {} - title: '{}', seller: {}", 
                listingId, listing.getTitle(), listing.getSellerId());
        
        return ResponseEntity.ok(listing);
    }

    @PostMapping("/")
    public ResponseEntity<?> createListing(@Valid @RequestBody CreateListingRequest request) {
        // TODO: retrieve seller id
        Long sellerId = 1L;
        
        log.info("Creating new listing - title: '{}', category: {}, condition: {}, price: {}, seller: {}", 
                request.getTitle(), request.getCategory(), request.getCondition(), request.getPrice(), sellerId);
        
        Listing createdListing = listingService.createListing(request.getTitle(), request.getDescription(), request.getPrice(),
            request.getCategory(), request.getCondition(), request.getLocation(), sellerId);

        log.info("Successfully created listing ID: {} with title: '{}'", 
                createdListing.getListingId(), createdListing.getTitle());
        
        return ResponseEntity.ok(createdListing);
    }

    @PostMapping("/{listingId}/images")
    public ResponseEntity<?> uploadImages(@PathVariable Long listingId,
            @RequestParam("images") List<MultipartFile> images, @RequestParam("displayOrders") int[] displayOrders) {
        // TODO: retrieve seller id
        Long sellerId = 1L;
        
        log.info("Uploading {} images for listing ID: {} by seller: {}", 
                images.size(), listingId, sellerId);
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            log.warn("Unauthorized image upload attempt - listing ID: {}, seller: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to upload images for this listing");
        }

        Listing listing = listingService.getListingById(listingId);
        List<ListingImage> storedImages = fileStorageService.storeFiles(images, listing, displayOrders);
        Listing updatedListing = listingService.addImagesToListing(listingId, storedImages);
        
        log.info("Successfully uploaded {} images for listing ID: {}", 
                storedImages.size(), listingId);
        
        return ResponseEntity.ok(updatedListing);
    }

    @PutMapping("/{listingId}")
    public ResponseEntity<?> updateListing(@PathVariable Long listingId, @Valid @RequestBody UpdateListingRequest request) {
        // TODO: retrieve seller id
        Long sellerId = 1L;
        
        log.info("Updating listing ID: {} by seller: {} - title: '{}'", 
                listingId, sellerId, request.getTitle());
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            log.warn("Unauthorized listing update attempt - listing ID: {}, seller: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to update this listing");
        }

        Listing updatedListing = listingService.updateListing(listingId, request.getTitle(), request.getDescription(), request.getPrice(),
            request.getCategory(), request.getCondition(), request.getLocation(), request.getImages());
        
        log.info("Successfully updated listing ID: {} with new title: '{}'", 
                listingId, updatedListing.getTitle());
        
        return ResponseEntity.ok(updatedListing);
    }

    @PutMapping("/{listingId}/sold")
    public ResponseEntity<?> markAsSold(@PathVariable Long listingId) {
        // TODO: retrieve seller id
        Long sellerId = 1L;
        
        log.info("Marking listing ID: {} as sold by seller: {}", listingId, sellerId);
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            log.warn("Unauthorized mark as sold attempt - listing ID: {}, seller: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to mark this listing as sold");
        }

        Listing updatedListing = listingService.markAsSold(listingId);
        
        log.info("Successfully marked listing ID: {} as sold", listingId);
        
        return ResponseEntity.ok(updatedListing);
    }

    @PutMapping("/{listingId}/cancel")
    public ResponseEntity<?> cancelListing(@PathVariable Long listingId) {
        // TODO: retrieve seller id
        Long sellerId = 1L;
        
        log.info("Cancelling listing ID: {} by seller: {}", listingId, sellerId);
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            log.warn("Unauthorized listing cancellation attempt - listing ID: {}, seller: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to cancel this listing");
        }

        Listing updatedListing = listingService.cancelListing(listingId);
        
        log.info("Successfully cancelled listing ID: {}", listingId);
        
        return ResponseEntity.ok(updatedListing);
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<String> deleteListing(@PathVariable Long listingId) {
        // TODO: retrieve seller id
        Long sellerId = 1L;
        
        log.info("Deleting listing ID: {} by seller: {}", listingId, sellerId);
        
        if (!listingService.isListingOwner(listingId, sellerId)) {
            log.warn("Unauthorized listing deletion attempt - listing ID: {}, seller: {}", listingId, sellerId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to delete this listing");
        }

        listingService.deleteListing(listingId);
        
        log.info("Successfully deleted listing ID: {}", listingId);
        
        return ResponseEntity.ok("Listing deleted successfully");
    }

    public static class CreateListingRequest {
        private String title;
        private String description;
        private BigDecimal price;
        private Category category;
        private ItemCondition condition;
        private String location;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
        
        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }
        
        public ItemCondition getCondition() {
            return condition;
        }

        public void setCondition(ItemCondition condition) {
            this.condition = condition;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class UpdateListingRequest {
        private String title;
        private String description;
        private BigDecimal price;
        private Category category;
        private ItemCondition condition;
        private String location;
        private List<ListingImage> images;
        
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public ItemCondition getCondition() {
            return condition;
        }

        public void setCondition(ItemCondition condition) {
            this.condition = condition;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public List<ListingImage> getImages() {
            return images;
        }

        public void setImages(List<ListingImage> images) {
            this.images = images;
        }
    }
}