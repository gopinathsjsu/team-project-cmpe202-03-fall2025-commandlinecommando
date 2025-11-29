package com.commandlinecommandos.campusmarketplace.dto;

import com.commandlinecommandos.campusmarketplace.model.UserReport;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for UserReport response
 * Used to avoid Hibernate proxy serialization issues
 */
public class ReportResponse {

    private String reportId;
    private String reportType;
    private String reason;
    private String description;
    private String status;
    private String priority;
    private String reportedEntityId;
    private String listingId;  // Alias for reportedEntityId for frontend compatibility
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
    private String resolutionNotes;

    // Nested objects for frontend display
    private Map<String, Object> listing;
    private Map<String, Object> reporter;

    public ReportResponse() {
    }

    public ReportResponse(UserReport report) {
        this.reportId = report.getReportId() != null ? report.getReportId().toString() : null;
        this.reportType = report.getReportType();
        this.reason = report.getReason();
        this.description = report.getDescription();
        this.status = report.getStatus() != null ? report.getStatus().toString() : "PENDING";
        this.priority = report.getPriority();
        this.reportedEntityId = report.getReportedEntityId() != null ? report.getReportedEntityId().toString() : null;
        this.listingId = this.reportedEntityId;  // Alias for frontend compatibility
        this.createdAt = report.getCreatedAt();
        this.updatedAt = report.getUpdatedAt();
        this.reviewedAt = report.getReviewedAt();
        this.resolutionNotes = report.getResolutionNotes();

        // Populate listing details if available
        if (report.getReportedProduct() != null) {
            this.listing = new HashMap<>();
            this.listing.put("id", report.getReportedProduct().getProductId() != null ? report.getReportedProduct().getProductId().toString() : null);
            this.listing.put("title", report.getReportedProduct().getTitle());
            this.listing.put("description", report.getReportedProduct().getDescription());
            this.listing.put("price", report.getReportedProduct().getPrice());
            this.listing.put("category", report.getReportedProduct().getCategory() != null ? report.getReportedProduct().getCategory().toString() : null);
            this.listing.put("condition", report.getReportedProduct().getCondition() != null ? report.getReportedProduct().getCondition().toString() : null);

            // Add seller info if available
            if (report.getReportedProduct().getSeller() != null) {
                Map<String, Object> seller = new HashMap<>();
                seller.put("id", report.getReportedProduct().getSeller().getUserId() != null ? report.getReportedProduct().getSeller().getUserId().toString() : null);
                seller.put("username", report.getReportedProduct().getSeller().getUsername());
                seller.put("name", report.getReportedProduct().getSeller().getFirstName() + " " + report.getReportedProduct().getSeller().getLastName());
                this.listing.put("seller", seller);
            }
        }

        // Populate reporter details if available
        if (report.getReporter() != null) {
            this.reporter = new HashMap<>();
            this.reporter.put("id", report.getReporter().getUserId() != null ? report.getReporter().getUserId().toString() : null);
            this.reporter.put("username", report.getReporter().getUsername());
            this.reporter.put("email", report.getReporter().getEmail());
            this.reporter.put("firstName", report.getReporter().getFirstName());
            this.reporter.put("lastName", report.getReporter().getLastName());
        }
    }
    
    // Getters and Setters
    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
    public String getReportType() {
        return reportType;
    }
    
    public void setReportType(String reportType) {
        this.reportType = reportType;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getReportedEntityId() {
        return reportedEntityId;
    }
    
    public void setReportedEntityId(String reportedEntityId) {
        this.reportedEntityId = reportedEntityId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
    
    public String getResolutionNotes() {
        return resolutionNotes;
    }
    
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public Map<String, Object> getListing() {
        return listing;
    }

    public void setListing(Map<String, Object> listing) {
        this.listing = listing;
    }

    public Map<String, Object> getReporter() {
        return reporter;
    }

    public void setReporter(Map<String, Object> reporter) {
        this.reporter = reporter;
    }
}

