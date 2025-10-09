package com.commandlinecommandos.listingapi.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileStorageException extends RuntimeException {
    
    public FileStorageException(String message) {
        super(message);
        log.error("FileStorageException created: {}", message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
        log.error("FileStorageException created with cause: {} - cause: {}", message, cause.getMessage(), cause);
    }
}
