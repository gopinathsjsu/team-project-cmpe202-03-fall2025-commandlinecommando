package com.commandlinecommandos.communication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Service for integrating with the listing-api service to verify listings and get seller information.
 */
@Service
public class ListingService {

    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final RestTemplate restTemplate;
    private final String listingApiUrl;

    @Autowired
    public ListingService(
            RestTemplate restTemplate,
            @Value("${listing.api.url:http://localhost:8100/api}") String listingApiUrl) {
        this.restTemplate = restTemplate;
        this.listingApiUrl = listingApiUrl;
        logger.info("ListingService initialized with listing API URL: {}", listingApiUrl);
    }

    /**
     * Gets listing information from the listing-api service.
     * 
     * @param listingId The listing ID
     * @return Listing information as a Map, or null if not found
     */
    public Map<String, Object> getListing(Long listingId) {
        try {
            String url = String.format("%s/listings/%d", listingApiUrl, listingId);
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (RestClientException e) {
            logger.warn("Failed to get listing {}: {}", listingId, e.getMessage());
        }
        return null;
    }

    /**
     * Verifies that a listing exists and returns the seller ID.
     * 
     * @param listingId The listing ID to verify
     * @return The seller ID if listing exists, null otherwise
     */
    public Long getSellerId(Long listingId) {
        Map<String, Object> listing = getListing(listingId);
        if (listing != null && listing.containsKey("sellerId")) {
            Object sellerIdObj = listing.get("sellerId");
            if (sellerIdObj instanceof Number) {
                return ((Number) sellerIdObj).longValue();
            }
        }
        return null;
    }

    /**
     * Verifies that a listing exists.
     * 
     * @param listingId The listing ID to verify
     * @return true if listing exists, false otherwise
     */
    public boolean listingExists(Long listingId) {
        return getListing(listingId) != null;
    }

    /**
     * Gets the title of a listing.
     * 
     * @param listingId The listing ID to get the title of
     * @return The title of the listing, or null if not found
     */
    public String getListingTitle(Long listingId) {
        Map<String, Object> listing = getListing(listingId);
        if (listing != null && listing.containsKey("title")) {
            return (String) listing.get("title");
        }
        return null;
    }
}

