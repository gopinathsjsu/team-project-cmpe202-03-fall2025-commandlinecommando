package com.commandlinecommandos.listingapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;
    
    @Column(name = "reporter_id", nullable = false)
    @NotNull
    private Long reporterId;
    
    @Column(name = "listing_id", nullable = false)
    @NotNull
    private Long listingId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    @NotNull
    private ReportType reportType;
    
    @Size(max = 1000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private ReportStatus status;
    
    @Column(name = "created_at", nullable = false)
    @NotNull
    private LocalDateTime createdAt;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
    }
    
    public Report() {
    }
    
    public Report(Long reporterId, Long listingId, ReportType reportType, String description) {
        this.reporterId = reporterId;
        this.listingId = listingId;
        this.reportType = reportType;
        this.description = description;
        this.status = ReportStatus.PENDING;
    }
    
    public Long getReportId() {
        return reportId;
    }
    
    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }
    
    public Long getReporterId() {
        return reporterId;
    }
    
    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }
    
    public Long getListingId() {
        return listingId;
    }
    
    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }
    
    public ReportType getReportType() {
        return reportType;
    }
    
    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ReportStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReportStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
    
    public Long getReviewedBy() {
        return reviewedBy;
    }
    
    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }
    
    public void markAsReviewedBy(Long reviewerId) {
        this.status = ReportStatus.UNDER_REVIEW;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = reviewerId;
    }
    
    public void markAsResolved() {
        this.status = ReportStatus.RESOLVED;
        if (this.reviewedAt == null) {
            this.reviewedAt = LocalDateTime.now();
        }
    }
    
    public void markAsDismissed() {
        this.status = ReportStatus.DISMISSED;
        if (this.reviewedAt == null) {
            this.reviewedAt = LocalDateTime.now();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Report report = (Report) obj;
        return reportId.equals(report.reportId);
    }
    
    @Override
    public int hashCode() {
        return reportId.hashCode();
    }
    
    @Override
    public String toString() {
        return "Report [reportId=" + reportId + ", reporterId=" + reporterId + 
               ", listingId=" + listingId + ", reportType=" + reportType + 
               ", description=" + description + ", status=" + status + 
               ", createdAt=" + createdAt + ", reviewedAt=" + reviewedAt + 
               ", reviewedBy=" + reviewedBy + "]";
    }
}
