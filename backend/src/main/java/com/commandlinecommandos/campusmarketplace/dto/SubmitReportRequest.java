package com.commandlinecommandos.campusmarketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class SubmitReportRequest {
    
    @NotBlank(message = "Report type is required")
    private String reportType; // PRODUCT, USER, REVIEW, MESSAGE
    
    @NotNull(message = "Target ID is required")
    private UUID targetId;
    
    @NotBlank(message = "Reason is required")
    private String reason; // SPAM, INAPPROPRIATE, SCAM, FAKE, OTHER
    
    @NotBlank(message = "Description is required")
    private String description;
    
    public String getReportType() {
        return reportType;
    }
    
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
    public UUID getTargetId() {
        return targetId;
    }
    
    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
