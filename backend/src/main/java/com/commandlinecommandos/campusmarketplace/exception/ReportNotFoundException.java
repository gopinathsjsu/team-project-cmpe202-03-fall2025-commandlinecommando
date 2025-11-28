package com.commandlinecommandos.campusmarketplace.exception;

import java.util.UUID;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(UUID reportId) {
        super("Report with ID " + reportId + " not found");
    }
    
    public ReportNotFoundException(String message) {
        super(message);
    }
}
