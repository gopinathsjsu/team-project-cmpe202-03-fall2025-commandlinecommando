package com.commandlinecommandos.campusmarketplace.exception;

import java.util.UUID;

public class ListingNotFoundException extends RuntimeException {
    public ListingNotFoundException(UUID listingId) {
        super("Listing with ID " + listingId + " not found");
    }
    
    public ListingNotFoundException(String message) {
        super(message);
    }
}
