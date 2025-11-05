package com.commandlinecommandos.listingapi.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException(String resource, Long resourceId, Long userId) {
        super("User " + userId + " is not authorized to access " + resource + " with ID " + resourceId);
    }
}
