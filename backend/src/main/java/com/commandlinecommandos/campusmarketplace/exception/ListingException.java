package com.commandlinecommandos.campusmarketplace.exception;

public class ListingException extends RuntimeException {

    public ListingException(String message) {
        super(message);
    }

    public ListingException(String message, Throwable cause) {
        super(message, cause);
    }
}
