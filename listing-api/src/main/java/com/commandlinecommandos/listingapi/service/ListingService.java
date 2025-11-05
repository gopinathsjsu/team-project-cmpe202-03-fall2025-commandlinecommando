package com.commandlinecommandos.listingapi.service;

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
import com.commandlinecommandos.listingapi.exception.ListingNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ListingService {

    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    @Autowired
    private ListingRepository listingRepository;

    public Listing createListing(String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, Long sellerId) {
        logger.debug("Creating listing - title: '{}', sellerId: {}, price: {}, category: {}", 
                   title, sellerId, price, category);
        
        Listing listing = new Listing(title, description, price, category, condition, location, sellerId);
        Listing savedListing = listingRepository.save(listing);
        
        logger.info("Successfully created listing ID: {} with title: '{}' for seller ID: {}", 
                   savedListing.getListingId(), savedListing.getTitle(), sellerId);
        
        return savedListing;
    }

    public Listing createListing(String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, Long sellerId, List<ListingImage> images) {
        logger.debug("Creating listing with images - title: '{}', sellerId: {}, price: {}, imageCount: {}", 
                   title, sellerId, price, images != null ? images.size() : 0);
        
        Listing listing = new Listing(title, description, price, category, condition, location, sellerId, images);
        Listing savedListing = listingRepository.save(listing);
        
        logger.info("Successfully created listing ID: {} with title: '{}' and {} images for seller ID: {}", 
                   savedListing.getListingId(), savedListing.getTitle(), 
                   images != null ? images.size() : 0, sellerId);
        
        return savedListing;
    }

    public Listing getListingById(Long listingId) {
        logger.debug("Retrieving listing by ID: {}", listingId);
        
        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ListingNotFoundException(listingId));
        
        logger.debug("Successfully retrieved listing ID: {} - title: '{}', status: {}, seller: {}", 
                    listingId, listing.getTitle(), listing.getStatus(), listing.getSellerId());
        
        return listing;
    }

    public Page<Listing> getAllListings(Pageable pageable) {
        logger.debug("Retrieving all active listings - page: {}, size: {}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Listing> listings = listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE, pageable)
            .orElseThrow(() -> new ListingNotFoundException("No active listings found"));
        
        logger.info("Successfully retrieved {} active listings (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), pageable.getPageNumber() + 1, 
                   listings.getTotalPages(), listings.getTotalElements());
        
        return listings;
    }

    public Page<Listing> getListingsBySellerId(Long sellerId, Pageable pageable) {
        logger.debug("Retrieving listings for seller ID: {} - page: {}, size: {}", 
                   sellerId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Listing> listings = listingRepository.findBySellerId(sellerId, pageable)
            .orElseThrow(() -> new ListingNotFoundException("No listings found for seller with id: " + sellerId));
        
        logger.info("Successfully retrieved {} listings for seller ID: {} (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), sellerId, pageable.getPageNumber() + 1, 
                   listings.getTotalPages(), listings.getTotalElements());
        
        return listings;
    }

    public Page<Listing> getListingsBySellerIdAndStatus(Long sellerId, ListingStatus status, Pageable pageable) {
        logger.debug("Retrieving listings for seller ID: {} with status: {} - page: {}, size: {}", 
                   sellerId, status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Listing> listings = listingRepository.findBySellerIdAndStatus(sellerId, status, pageable)
            .orElseThrow(() -> new ListingNotFoundException("No listings found for seller with id: " + sellerId + " and status: " + status));
        
        logger.info("Successfully retrieved {} listings for seller ID: {} with status: {} (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), sellerId, status, pageable.getPageNumber() + 1, 
                   listings.getTotalPages(), listings.getTotalElements());
        
        return listings;
    }

    public Page<Listing> getListingsByCategory(Category category, Pageable pageable) {
        logger.debug("Retrieving listings for category: {} - page: {}, size: {}", 
                   category, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Listing> listings = listingRepository.findByCategory(category, pageable)
            .orElseThrow(() -> new ListingNotFoundException("No listings found for category: " + category));
        
        logger.info("Successfully retrieved {} listings for category: {} (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), category, pageable.getPageNumber() + 1, 
                   listings.getTotalPages(), listings.getTotalElements());
        
        return listings;
    }

    public Page<Listing> getListingsByStatus(ListingStatus status, Pageable pageable) {
        logger.debug("Retrieving listings for status: {} - page: {}, size: {}", 
                   status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Listing> listings = listingRepository.findByStatus(status, pageable)
            .orElseThrow(() -> new ListingNotFoundException("No listings found for status: " + status));
        
        logger.info("Successfully retrieved {} listings for status: {} (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), status, pageable.getPageNumber() + 1, 
                   listings.getTotalPages(), listings.getTotalElements());
        
        return listings;
    }

    public Page<Listing> searchListings(String keyword, Pageable pageable) {
        logger.debug("Searching listings with keyword: '{}' - page: {}, size: {}", 
                   keyword, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Listing> listings = listingRepository.findByTitleOrDescriptionContaining(keyword, pageable)
            .orElseThrow(() -> new ListingNotFoundException("No listings found with keyword: " + keyword));
        
        logger.info("Search completed - found {} listings with keyword: '{}' (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), keyword, pageable.getPageNumber() + 1, 
                   listings.getTotalPages(), listings.getTotalElements());
        
        return listings;
    }

    public Page<Listing> getListingsWithFilters(ListingStatus status, String keyword, Category category, ItemCondition condition,
            BigDecimal minPrice, BigDecimal maxPrice, String location, Pageable pageable) {
        logger.debug("Searching listings with filters - keyword: '{}', status: {}, category: {}, condition: {}, " +
                   "priceRange: {} - {}, location: '{}', page: {}, size: {}", 
                   keyword, status, category, condition, minPrice, maxPrice, location, 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Listing> listings = listingRepository.findWithFilters(status, keyword, category, condition, minPrice, maxPrice, location, pageable)
            .orElseThrow(() -> new ListingNotFoundException("No listings found with filters"));
        
        logger.info("Search completed - found {} listings matching filters (page {}/{} with {} total elements)", 
                   listings.getNumberOfElements(), pageable.getPageNumber() + 1, 
                   listings.getTotalPages(), listings.getTotalElements());
        
        return listings;
    }
    
    public Listing updateListing(Long listingId, String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, List<ListingImage> images) {
        logger.debug("Updating listing ID: {} - title: '{}', price: {}, imageCount: {}", 
                   listingId, title, price, images != null ? images.size() : 0);
        
        Listing listing = getListingById(listingId);

        String oldTitle = listing.getTitle();
        BigDecimal oldPrice = listing.getPrice();
        
        listing.setTitle(title);
        listing.setDescription(description);
        listing.setPrice(price);
        listing.setCategory(category);
        listing.setCondition(condition);
        listing.setLocation(location);
        listing.setUpdatedAt(LocalDateTime.now());
        listing.setImages(images);
        
        Listing updatedListing = listingRepository.save(listing);
        
        logger.info("Successfully updated listing ID: {} - title changed from '{}' to '{}', " +
                   "price changed from {} to {}", 
                   listingId, oldTitle, title, oldPrice, price);
        
        return updatedListing;
    }

    public Listing addImagesToListing(Long listingId, List<ListingImage> images) {
        logger.debug("Adding images to listing ID: {} - image count: {}", listingId, images.size());

        Listing listing = getListingById(listingId);

        listing.addImages(images);
        Listing updatedListing = listingRepository.save(listing);

        logger.info("Successfully added {} images to listing ID: {} - total images now: {}", 
                   images.size(), listingId, updatedListing.getImages().size());

        return updatedListing;
    }

    public Listing markAsSold(Long listingId) {
        logger.debug("Marking listing ID: {} as sold", listingId);
        
        Listing listing = getListingById(listingId);

        ListingStatus oldStatus = listing.getStatus();
        listing.markAsSold();
        Listing updatedListing = listingRepository.save(listing);
        
        logger.info("Successfully marked listing ID: {} as sold - status changed from {} to {}", 
                   listingId, oldStatus, updatedListing.getStatus());
        
        return updatedListing;
    }

    public Listing cancelListing(Long listingId) {
        logger.debug("Cancelling listing ID: {}", listingId);
        
        Listing listing = getListingById(listingId);

        ListingStatus oldStatus = listing.getStatus();
        listing.setStatus(ListingStatus.CANCELLED);
        Listing updatedListing = listingRepository.save(listing);
        
        logger.info("Successfully cancelled listing ID: {} - status changed from {} to {}", 
                   listingId, oldStatus, updatedListing.getStatus());
        
        return updatedListing;
    }

    public void deleteListing(Long listingId) {
        logger.debug("Deleting listing ID: {}", listingId);
        
        Listing listing = getListingById(listingId);
        
        String title = listing.getTitle();
        Long sellerId = listing.getSellerId();
        listingRepository.delete(listing);
        
        logger.info("Successfully deleted listing ID: {} with title: '{}' for seller ID: {}", 
                   listingId, title, sellerId);
    }

    public Long countListingsBySellerIdAndStatus(Long sellerId, ListingStatus status) {
        logger.debug("Counting listings for seller ID: {} with status: {}", sellerId, status);
        
        Long count = listingRepository.countBySellerIdAndStatus(sellerId, status);
        
        logger.debug("Successfully counted {} listings for seller ID: {} with status: {}", 
                    count, sellerId, status);
        
        return count;
    }

    public boolean isListingOwner(Long listingId, Long sellerId) {
        logger.debug("Checking listing ownership - listing ID: {}, seller ID: {}", listingId, sellerId);
        
        Listing listing = getListingById(listingId);

        boolean isOwner = listing.getSellerId().equals(sellerId);
        logger.debug("Ownership check result - listing ID: {}, seller ID: {}, isOwner: {}", 
                    listingId, sellerId, isOwner);
        
        return isOwner;
    }

    public int incrementViewCount(Long listingId) {
        logger.debug("Incrementing view count for listing ID: {}", listingId);
        
        Listing listing = getListingById(listingId);

        int oldViewCount = listing.getViewCount();
        listing.incrementViewCount();
        Listing updatedListing = listingRepository.save(listing);
        int newViewCount = updatedListing.getViewCount();

        logger.debug("Successfully incremented view count for listing ID: {} from {} to {}", 
                   listingId, oldViewCount, newViewCount);
        
        return newViewCount;
    }
}