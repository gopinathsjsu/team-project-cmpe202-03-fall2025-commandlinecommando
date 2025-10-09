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

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    public Listing createListing(String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, Long sellerId) {
        Listing listing = new Listing(title, description, price, category, condition, location, sellerId);
        return listingRepository.save(listing);
    }

    public Listing createListing(String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, Long sellerId, List<ListingImage> images) {
        Listing listing = new Listing(title, description, price, category, condition, location, sellerId, images);
        return listingRepository.save(listing);
    }

    public Listing getListingById(Long listingId) {
        return listingRepository.findById(listingId)
            .orElseThrow(() -> new ListingException("Listing not found with id: " + listingId));
    }

    public Page<Listing> getAllListings(Pageable pageable) {
        return listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE, pageable)
            .orElseThrow(() -> new ListingException("No listings found"));
    }

    public Page<Listing> getListingsBySellerId(Long sellerId, Pageable pageable) {
        return listingRepository.findBySellerId(sellerId, pageable)
            .orElseThrow(() -> new ListingException("No listings found for seller with id: " + sellerId));
    }

    public Page<Listing> getListingsBySellerIdAndStatus(Long sellerId, ListingStatus status, Pageable pageable) {
        return listingRepository.findBySellerIdAndStatus(sellerId, status, pageable)
            .orElseThrow(() -> new ListingException("No listings found for seller with id: " + sellerId + " and status: " + status));
    }

    public Page<Listing> getListingsByCategory(Category category, Pageable pageable) {
        return listingRepository.findByCategory(category, pageable)
            .orElseThrow(() -> new ListingException("No listings found for category: " + category));
    }

    public Page<Listing> getListingsByStatus(ListingStatus status, Pageable pageable) {
        return listingRepository.findByStatus(status, pageable)
            .orElseThrow(() -> new ListingException("No listings found for status: " + status));
    }

    public Page<Listing> searchListings(String keyword, Pageable pageable) {
        return listingRepository.findByTitleOrDescriptionContaining(keyword, pageable)
            .orElseThrow(() -> new ListingException("No listings found with keyword: " + keyword));
    }

    public Page<Listing> getListingsWithFilters(ListingStatus status, String keyword, Category category, ItemCondition condition,
            BigDecimal minPrice, BigDecimal maxPrice, String location, Pageable pageable) {
        return listingRepository.findWithFilters(status, keyword, category, condition, minPrice, maxPrice, location, pageable)
            .orElseThrow(() -> new ListingException("No listings found with filters"));
    }
    
    public Listing updateListing(Long listingId, String title, String description, BigDecimal price, Category category,
            ItemCondition condition, String location, List<ListingImage> images) {
        Listing listing = getListingById(listingId);
        if (listing == null) {
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
        return listingRepository.save(listing);
    }

    public Listing addImagesToListing(Long listingId, List<ListingImage> images) {
        Listing listing = getListingById(listingId);
        if (listing == null) {
            throw new ListingException("Listing not found with id: " + listingId);
        }

        listing.addImages(images);
        return listingRepository.save(listing);
    }

    public Listing markAsSold(Long listingId) {
        Listing listing = getListingById(listingId);
        if (listing == null) {
            throw new ListingException("Listing not found with id: " + listingId);
        }

        listing.markAsSold();
        return listingRepository.save(listing);
    }

    public Listing cancelListing(Long listingId) {
        Listing listing = getListingById(listingId);
        if (listing == null) {
            throw new ListingException("Listing not found with id: " + listingId);
        }

        listing.setStatus(ListingStatus.CANCELLED);
        return listingRepository.save(listing);
    }

    public void deleteListing(Long listingId) {
        Listing listing = getListingById(listingId);
        if (listing == null) {
            throw new ListingException("Listing not found with id: " + listingId);
        }
        
        listingRepository.delete(listing);
    }

    public Long countListingsBySellerIdAndStatus(Long sellerId, ListingStatus status) {
        return listingRepository.countBySellerIdAndStatus(sellerId, status);
    }

    public boolean isListingOwner(Long listingId, Long sellerId) {
        Listing listing = getListingById(listingId);
        if (listing == null) {
            throw new ListingException("Listing not found with id: " + listingId);
        }

        return listing.getSellerId().equals(sellerId);
    }

    public int incrementViewCount(Long listingId) {
        Listing listing = getListingById(listingId);
        if (listing == null) {
            throw new ListingException("Listing not found with id: " + listingId);
        }

        listing.incrementViewCount();
        return listingRepository.save(listing).getViewCount();
    }
}