package com.commandlinecommandos.listingapi.exception;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(Long reportId) {
        super("Report with ID " + reportId + " not found");
    }
    
    public ReportNotFoundException(String message) {
        super(message);
    }
}
