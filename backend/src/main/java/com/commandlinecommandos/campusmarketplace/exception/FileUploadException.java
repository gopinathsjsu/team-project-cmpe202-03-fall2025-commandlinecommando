package com.commandlinecommandos.campusmarketplace.exception;

/**
 * Exception thrown when file upload fails
 * Consolidated from listing-api
 */
public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
