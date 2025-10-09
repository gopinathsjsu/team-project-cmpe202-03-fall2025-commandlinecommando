package com.commandlinecommandos.listingapi.service;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.commandlinecommandos.listingapi.model.Listing;
import com.commandlinecommandos.listingapi.model.ListingImage;
import com.commandlinecommandos.listingapi.model.Category;
import com.commandlinecommandos.listingapi.model.ItemCondition;
import com.commandlinecommandos.listingapi.model.ListingStatus;
import com.commandlinecommandos.listingapi.repository.ListingRepository;
import com.commandlinecommandos.listingapi.exception.ListingException;

@Slf4j
@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    public Listing createListing(String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, Long sellerId) {
        log.info("Creating new listing - title: '{}', seller: {}, category: {}, price: {}", 
                title, sellerId, category, price);
        
        Listing listing = new Listing(title, description, price, category, condition, location, sellerId);
        Listing savedListing = listingRepository.save(listing);
        
        log.info("Successfully created listing ID: {} with title: '{}'", 
                savedListing.getListingId(), savedListing.getTitle());
        
        return savedListing;
    }

    public Listing createListing(String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, Long sellerId, List<ListingImage> images) {
        log.info("Creating new listing with {} images - title: '{}', seller: {}, category: {}, price: {}", 
                images.size(), title, sellerId, category, price);
        
        Listing listing = new Listing(title, description, price, category, condition, location, sellerId, images);
        Listing savedListing = listingRepository.save(listing);
        
        log.info("Successfully created listing ID: {} with title: '{}' and {} images", 
                savedListing.getListingId(), savedListing.getTitle(), images.size());
        
        return savedListing;
    }

    public Listing getListingById(Long listingId) {
        log.debug("Retrieving listing by ID: {}", listingId);
        
        return listingRepository.findById(listingId)
            .orElseThrow(() -> {
                log.warn("Listing not found with ID: {}", listingId);
                return new ListingException("Listing not found with id: " + listingId);
            });
    }

    public Page<Listing> getAllListings(Pageable pageable) {
        log.debug("Retrieving all active listings with pagination: {}", pageable);
        
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE, pageable)
            .orElseThrow(() -> {
                log.warn("No active listings found");
                return new ListingException("No listings found");
            });
    }

    public Page<Listing> getListingsBySellerId(Long sellerId, Pageable pageable) {
        log.debug("Retrieving listings for seller ID: {} with pagination: {}", sellerId, pageable);
        
        return listingRepository.findBySellerId(sellerId, pageable)
            .orElseThrow(() -> {
                log.warn("No listings found for seller ID: {}", sellerId);
                return new ListingException("No listings found for seller with id: " + sellerId);
            });
    }

    public Page<Listing> getListingsBySellerIdAndStatus(Long sellerId, ListingStatus status, Pageable pageable) {
        log.debug("Retrieving listings for seller ID: {} with status: {} and pagination: {}", 
                sellerId, status, pageable);
        
        return listingRepository.findBySellerIdAndStatus(sellerId, status, pageable)
            .orElseThrow(() -> {
                log.warn("No listings found for seller ID: {} with status: {}", sellerId, status);
                return new ListingException("No listings found for seller with id: " + sellerId + " and status: " + status);
            });
    }

    public Page<Listing> getListingsByCategory(Category category, Pageable pageable) {
        log.debug("Retrieving listings for category: {} with pagination: {}", category, pageable);
        
        return listingRepository.findByCategory(category, pageable)
            .orElseThrow(() -> {
                log.warn("No listings found for category: {}", category);
                return new ListingException("No listings found for category: " + category);
            });
    }

    public Page<Listing> getListingsByStatus(ListingStatus status, Pageable pageable) {
        log.debug("Retrieving listings for status: {} with pagination: {}", status, pageable);
        
        return listingRepository.findByStatus(status, pageable)
            .orElseThrow(() -> {
                log.warn("No listings found for status: {}", status);
                return new ListingException("No listings found for status: " + status);
            });
    }

    public Page<Listing> searchListings(String keyword, Pageable pageable) {
        log.debug("Searching listings with keyword: '{}' and pagination: {}", keyword, pageable);
        
        return listingRepository.findByTitleOrDescriptionContaining(keyword, pageable)
            .orElseThrow(() -> {
                log.warn("No listings found with keyword: '{}'", keyword);
                return new ListingException("No listings found with keyword: " + keyword);
            });
    }

    public Page<Listing> getListingsWithFilters(ListingStatus status, String keyword, Category category, ItemCondition condition,
            BigDecimal minPrice, BigDecimal maxPrice, String location, Pageable pageable) {
        log.debug("Searching listings with filters - status: {}, keyword: '{}', category: {}, condition: {}, " +
                "minPrice: {}, maxPrice: {}, location: '{}', pagination: {}", 
                status, keyword, category, condition, minPrice, maxPrice, location, pageable);
        
        return listingRepository.findWithFilters(status, keyword, category, condition, minPrice, maxPrice, location, pageable)
            .orElseThrow(() -> {
                log.warn("No listings found with applied filters");
                return new ListingException("No listings found with filters");
            });
    }
    
    public Listing updateListing(Long listingId, String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, List<ListingImage> images) {
        log.info("Updating listing ID: {} - new title: '{}', price: {}", listingId, title, price);
        
        Listing listing = getListingById(listingId);
        if (listing == null) {
            log.error("Listing not found for update - ID: {}", listingId);
            throw new ListingException("Listing not found with id: " + listingId);
        }

        listing.setTitle(title);
        listing.setDescription(description);
        listing.setPrice(price);
        listing.setCategory(category);
        listing.setCondition(condition);
        listing.setLocation(location);
        listing.setUpdatedAt(LocalDateTime.now());
        listing.setImages(images);
        
        Listing updatedListing = listingRepository.save(listing);
        
        log.info("Successfully updated listing ID: {} with new title: '{}'", 
                listingId, updatedListing.getTitle());
        
        return updatedListing;
    }

    public Listing addImagesToListing(Long listingId, List<ListingImage> images) {
        log.info("Adding {} images to listing ID: {}", images.size(), listingId);
        
        Listing listing = getListingById(listingId);
        if (listing == null) {
            log.error("Listing not found for adding images - ID: {}", listingId);
            throw new ListingException("Listing not found with id: " + listingId);
        }

        listing.addImages(images);
        Listing updatedListing = listingRepository.save(listing);
        
        log.info("Successfully added {} images to listing ID: {}", images.size(), listingId);
        
        return updatedListing;
    }

    public Listing markAsSold(Long listingId) {
        log.info("Marking listing ID: {} as sold", listingId);
        
        Listing listing = getListingById(listingId);
        if (listing == null) {
            log.error("Listing not found for marking as sold - ID: {}", listingId);
            throw new ListingException("Listing not found with id: " + listingId);
        }

        listing.markAsSold();
        Listing updatedListing = listingRepository.save(listing);
        
        log.info("Successfully marked listing ID: {} as sold", listingId);
        
        return updatedListing;
    }

    public Listing cancelListing(Long listingId) {
        log.info("Cancelling listing ID: {}", listingId);
        
        Listing listing = getListingById(listingId);
        if (listing == null) {
            log.error("Listing not found for cancellation - ID: {}", listingId);
            throw new ListingException("Listing not found with id: " + listingId);
        }

        listing.setStatus(ListingStatus.CANCELLED);
        Listing updatedListing = listingRepository.save(listing);
        
        log.info("Successfully cancelled listing ID: {}", listingId);
        
        return updatedListing;
    }

    public void deleteListing(Long listingId) {
        log.info("Deleting listing ID: {}", listingId);
        
        Listing listing = getListingById(listingId);
        if (listing == null) {
            log.error("Listing not found for deletion - ID: {}", listingId);
            throw new ListingException("Listing not found with id: " + listingId);
        }
        
        listingRepository.delete(listing);
        
        log.info("Successfully deleted listing ID: {}", listingId);
    }

    public Long countListingsBySellerIdAndStatus(Long sellerId, ListingStatus status) {
        log.debug("Counting listings for seller ID: {} with status: {}", sellerId, status);
        
        Long count = listingRepository.countBySellerIdAndStatus(sellerId, status);
        
        log.debug("Found {} listings for seller ID: {} with status: {}", count, sellerId, status);
        
        return count;
    }

    public boolean isListingOwner(Long listingId, Long sellerId) {
        log.debug("Checking ownership for listing ID: {} and seller ID: {}", listingId, sellerId);
        
        Listing listing = getListingById(listingId);
        if (listing == null) {
            log.error("Listing not found for ownership check - ID: {}", listingId);
            throw new ListingException("Listing not found with id: " + listingId);
        }

        boolean isOwner = listing.getSellerId().equals(sellerId);
        
        log.debug("Ownership check result for listing ID: {} and seller ID: {} - isOwner: {}", 
                listingId, sellerId, isOwner);
        
        return isOwner;
    }

    public int incrementViewCount(Long listingId) {
        log.debug("Incrementing view count for listing ID: {}", listingId);
        
        Listing listing = getListingById(listingId);
        if (listing == null) {
            log.error("Listing not found for view count increment - ID: {}", listingId);
            throw new ListingException("Listing not found with id: " + listingId);
        }

        int previousCount = listing.getViewCount();
        listing.incrementViewCount();
        int newCount = listingRepository.save(listing).getViewCount();
        
        log.debug("View count incremented for listing ID: {} from {} to {}", 
                listingId, previousCount, newCount);
        
        return newCount;
    }
}