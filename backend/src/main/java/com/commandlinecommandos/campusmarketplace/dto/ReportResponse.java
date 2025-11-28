package com.commandlinecommandos.campusmarketplace.dto;

import com.commandlinecommandos.campusmarketplace.model.UserReport;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
    private String resolutionNotes;
    
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
        this.createdAt = report.getCreatedAt();
        this.updatedAt = report.getUpdatedAt();
        this.reviewedAt = report.getReviewedAt();
        this.resolutionNotes = report.getResolutionNotes();
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
}

