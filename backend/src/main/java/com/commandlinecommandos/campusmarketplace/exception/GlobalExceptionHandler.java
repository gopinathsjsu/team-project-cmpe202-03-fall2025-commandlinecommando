package com.commandlinecommandos.campusmarketplace.exception;

import com.commandlinecommandos.campusmarketplace.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for the unified backend
 * Consolidates exception handling from backend, listing-api, and communication services
 * Handles common exceptions and provides consistent error responses
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // ===== Custom Business Exceptions =====
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "UNAUTHORIZED",
            ex.getMessage(),
            HttpStatus.UNAUTHORIZED.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(ListingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleListingNotFound(ListingNotFoundException ex, WebRequest request) {
        log.warn("Listing not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "LISTING_NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReportNotFound(ReportNotFoundException ex, WebRequest request) {
        log.warn("Report not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "REPORT_NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConversationNotFound(ConversationNotFoundException ex, WebRequest request) {
        log.warn("Conversation not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "CONVERSATION_NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex, WebRequest request) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "UNAUTHORIZED_ACCESS",
            ex.getMessage(),
            HttpStatus.FORBIDDEN.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "ACCESS_DENIED",
            ex.getMessage(),
            HttpStatus.FORBIDDEN.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(ListingException.class)
    public ResponseEntity<ErrorResponse> handleListingException(ListingException ex, WebRequest request) {
        log.error("Listing operation error: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            "LISTING_ERROR",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @ExceptionHandler(ReportException.class)
    public ResponseEntity<ErrorResponse> handleReportException(ReportException ex, WebRequest request) {
        log.error("Report operation error: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            "REPORT_ERROR",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @ExceptionHandler(ConversationException.class)
    public ResponseEntity<ErrorResponse> handleConversationException(ConversationException ex, WebRequest request) {
        log.error("Conversation exception: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            "CONVERSATION_ERROR",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUploadException(FileUploadException ex, WebRequest request) {
        log.error("File upload error: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            "FILE_UPLOAD_ERROR",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex, WebRequest request) {
        log.error("File storage error: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            "FILE_STORAGE_ERROR",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            ex.getValidationErrors()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    // ===== Spring Validation Exceptions =====
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed",
            HttpStatus.BAD_REQUEST.value(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        log.warn("Binding error: {}", ex.getMessage());
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse(
            "BINDING_ERROR",
            "Request binding failed",
            HttpStatus.BAD_REQUEST.value(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());
        List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
            "CONSTRAINT_VIOLATION",
            "Constraint validation failed",
            HttpStatus.BAD_REQUEST.value(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    // ===== HTTP Request Exceptions =====
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());
        
        String message;
        if (ex.getRequiredType() != null && ex.getRequiredType().equals(UUID.class)) {
            message = String.format("Invalid UUID format for parameter '%s': '%s'. Expected format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", 
                ex.getName(), ex.getValue());
        } else {
            message = String.format("Parameter '%s' should be of type %s", 
                ex.getName(), 
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TYPE_MISMATCH",
            message,
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("Malformed request: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "MALFORMED_JSON",
            "Request body is malformed or contains invalid JSON",
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Missing parameter: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "MISSING_PARAMETER",
            String.format("Required parameter '%s' is missing", ex.getParameterName()),
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("HTTP method not supported: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "METHOD_NOT_ALLOWED",
            "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, WebRequest request) {
        log.warn("No handler found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "NOT_FOUND",
            "Endpoint not found: " + ex.getRequestURL(),
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    // ===== File Upload Exceptions =====
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, WebRequest request) {
        log.warn("File upload size exceeded: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "FILE_TOO_LARGE",
            "File size exceeds the maximum allowed limit",
            HttpStatus.PAYLOAD_TOO_LARGE.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }
    
    // ===== Generic Exception Handler (catch-all) =====
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
            "BAD_REQUEST",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later.",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

