package com.commandlinecommandos.listingapi.exception;

public class ListingNotFoundException extends RuntimeException {
    public ListingNotFoundException(Long listingId) {
        super("Listing with ID " + listingId + " not found");
    }
    
    public ListingNotFoundException(String message) {
        super(message);
    }
}
