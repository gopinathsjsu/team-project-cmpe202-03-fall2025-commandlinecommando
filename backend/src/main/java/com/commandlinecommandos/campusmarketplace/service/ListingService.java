package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.dto.CreateListingRequest;
import com.commandlinecommandos.campusmarketplace.dto.ListingDTO;
import com.commandlinecommandos.campusmarketplace.exception.ListingNotFoundException;
import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.ListingImageRepository;
import com.commandlinecommandos.campusmarketplace.repository.ListingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;

    public ListingService(ListingRepository listingRepository, ListingImageRepository listingImageRepository) {
        this.listingRepository = listingRepository;
        this.listingImageRepository = listingImageRepository;
    }

    public ListingDTO createListing(UUID sellerId, CreateListingRequest request) {
        Listing listing = new Listing();
        listing.setSellerId(sellerId);
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(request.getCategory());
        listing.setPrice(request.getPrice());
        listing.setCondition(request.getCondition());
        listing.setLocation(request.getLocation());
        listing.setStatus(ListingStatus.ACTIVE);

        Listing saved = listingRepository.save(listing);
        return convertToDTO(saved);
    }

    public ListingDTO getListingById(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));

        listing.incrementViewCount();
        listingRepository.save(listing);

        return convertToDTO(listing);
    }

    public Page<ListingDTO> searchListings(String keyword, Category category, ItemCondition condition,
                                           Double minPrice, Double maxPrice, String location,
                                           int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Listing> listings = listingRepository.findWithFilters(
                ListingStatus.ACTIVE, keyword, category, condition,
                minPrice != null ? java.math.BigDecimal.valueOf(minPrice) : null,
                maxPrice != null ? java.math.BigDecimal.valueOf(maxPrice) : null,
                location, pageable
        );

        return listings.map(this::convertToDTO);
    }

    public List<ListingDTO> getMyListings(UUID sellerId) {
        List<Listing> listings = listingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
        return listings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ListingDTO updateListing(Long listingId, UUID sellerId, CreateListingRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized: You can only update your own listings");
        }

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setCategory(request.getCategory());
        listing.setPrice(request.getPrice());
        listing.setCondition(request.getCondition());
        listing.setLocation(request.getLocation());

        Listing updated = listingRepository.save(listing);
        return convertToDTO(updated);
    }

    public void deleteListing(Long listingId, UUID sellerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own listings");
        }

        listingRepository.delete(listing);
    }

    public void markAsSold(Long listingId, UUID sellerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException(listingId));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized: You can only update your own listings");
        }

        listing.markAsSold();
        listingRepository.save(listing);
    }

    private ListingDTO convertToDTO(Listing listing) {
        ListingDTO dto = new ListingDTO();
        dto.setListingId(listing.getListingId());
        dto.setSellerId(listing.getSellerId());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setCategory(listing.getCategory());
        dto.setPrice(listing.getPrice());
        dto.setCondition(listing.getCondition());
        dto.setStatus(listing.getStatus());
        dto.setLocation(listing.getLocation());
        dto.setViewCount(listing.getViewCount());
        dto.setCreatedAt(listing.getCreatedAt());
        dto.setUpdatedAt(listing.getUpdatedAt());

        // Get images
        List<ListingImage> images = listingImageRepository.findByListingIdOrderByDisplayOrder(listing.getListingId());
        dto.setImageUrls(images.stream().map(ListingImage::getImageUrl).collect(Collectors.toList()));

        return dto;
    }
}
