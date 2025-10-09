package com.commandlinecommandos.listingapi.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListingException extends RuntimeException {

    public ListingException(String message) {
        super(message);
        log.error("ListingException created: {}", message);
    }

    public ListingException(String message, Throwable cause) {
        super(message, cause);
        log.error("ListingException created with cause: {} - cause: {}", message, cause.getMessage(), cause);
    }
}