package com.commandlinecommandos.listingapi.exception;

public class ListingException extends RuntimeException {

    public ListingException(String message) {
        super(message);
    }

    public ListingException(String message, Throwable cause) {
        super(message, cause);
    }
}