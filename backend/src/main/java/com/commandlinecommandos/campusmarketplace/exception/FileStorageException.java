package com.commandlinecommandos.campusmarketplace.exception;

/**
 * Exception thrown when file storage operations fail
 * Consolidated from listing-api
 */
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
