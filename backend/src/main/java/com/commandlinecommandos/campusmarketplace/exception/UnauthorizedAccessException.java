package com.commandlinecommandos.campusmarketplace.exception;

import java.util.UUID;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException(String resource, UUID resourceId, UUID userId) {
        super("User " + userId + " is not authorized to access " + resource + " with ID " + resourceId);
    }
}
