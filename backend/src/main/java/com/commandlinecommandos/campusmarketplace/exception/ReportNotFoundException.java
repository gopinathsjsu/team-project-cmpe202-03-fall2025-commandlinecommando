package com.commandlinecommandos.campusmarketplace.exception;

/**
 * Exception thrown when a report is not found
 * Consolidated from listing-api
 */
public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(Long reportId) {
        super("Report with ID " + reportId + " not found");
    }

    public ReportNotFoundException(String message) {
        super(message);
    }
}
