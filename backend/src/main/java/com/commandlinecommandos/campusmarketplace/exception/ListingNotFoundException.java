package com.commandlinecommandos.campusmarketplace.exception;

/**
 * Exception thrown when a listing is not found
 * Consolidated from listing-api
 */
public class ListingNotFoundException extends RuntimeException {
    public ListingNotFoundException(Long listingId) {
        super("Listing with ID " + listingId + " not found");
    }

    public ListingNotFoundException(String message) {
        super(message);
    }
}
