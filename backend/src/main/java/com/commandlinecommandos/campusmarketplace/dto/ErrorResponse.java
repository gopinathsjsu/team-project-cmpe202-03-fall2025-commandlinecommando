package com.commandlinecommandos.campusmarketplace.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response wrapper
 * Supports both simple message-only responses and detailed error responses
 */
public class ErrorResponse {
    private String message;
    private String error;
    private Integer status;
    private LocalDateTime timestamp;
    private String path;
    private List<String> validationErrors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message) {
        this();
        this.message = message;
    }

    public ErrorResponse(String error, String message, int status) {
        this();
        this.error = error;
        this.message = message;
        this.status = status;
    }

    public ErrorResponse(String error, String message, int status, String path) {
        this(error, message, status);
        this.path = path;
    }

    public ErrorResponse(String error, String message, int status, List<String> validationErrors) {
        this(error, message, status);
        this.validationErrors = validationErrors;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}

