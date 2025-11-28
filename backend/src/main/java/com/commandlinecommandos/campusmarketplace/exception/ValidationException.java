package com.commandlinecommandos.campusmarketplace.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<String> validationErrors;
    
    public ValidationException(String message) {
        super(message);
        this.validationErrors = null;
    }
    
    public ValidationException(List<String> validationErrors) {
        super("Validation failed: " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
