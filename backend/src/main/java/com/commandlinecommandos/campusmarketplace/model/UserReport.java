package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserReport entity - Content flagging and moderation
 * Users can report inappropriate listings, reviews, or other users
 */
@Entity
@Table(name = "user_reports", indexes = {
    @Index(name = "idx_user_reports_reporter", columnList = "reporter_id"),
    @Index(name = "idx_user_reports_product", columnList = "reported_product_id"),
    @Index(name = "idx_user_reports_user", columnList = "reported_user_id"),
    @Index(name = "idx_user_reports_status", columnList = "status"),
    @Index(name = "idx_user_reports_created", columnList = "created_at")
})
public class UserReport {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "report_id", updatable = false, nullable = false)
    private UUID reportId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    
    @NotNull
    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType; // PRODUCT, USER, REVIEW, MESSAGE
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_product_id")
    private Product reportedProduct;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;
    
    @Column(name = "reported_review_id")
    private UUID reportedReviewId;
    
    @NotNull
    @Column(name = "reason", nullable = false, length = 100)
    private String reason; // SPAM, INAPPROPRIATE, SCAM, FAKE, OTHER
    
    @NotNull
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModerationStatus status = ModerationStatus.PENDING;
    
    @Column(name = "priority", length = 20)
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserReport() {
    }
    
    public UserReport(User reporter, String reportType, String reason, String description) {
        this.reporter = reporter;
        this.reportType = reportType;
        this.reason = reason;
        this.description = description;
        this.status = ModerationStatus.PENDING;
    }
    
    // Business methods
    public void approve(User admin, String notes) {
        this.status = ModerationStatus.APPROVED;
        this.reviewedBy = admin;
        this.resolutionNotes = notes;
        this.reviewedAt = LocalDateTime.now();
    }
    
    public void reject(User admin, String notes) {
        this.status = ModerationStatus.REJECTED;
        this.reviewedBy = admin;
        this.resolutionNotes = notes;
        this.reviewedAt = LocalDateTime.now();
    }
    
    public void flag(User admin, String notes) {
        this.status = ModerationStatus.FLAGGED;
        this.reviewedBy = admin;
        this.resolutionNotes = notes;
        this.reviewedAt = LocalDateTime.now();
    }
    
    public boolean isPending() {
        return this.status == ModerationStatus.PENDING;
    }
    
    // Getters and Setters
    public UUID getReportId() {
        return reportId;
    }
    
    public void setReportId(UUID reportId) {
        this.reportId = reportId;
    }
    
    public User getReporter() {
        return reporter;
    }
    
    public void setReporter(User reporter) {
        this.reporter = reporter;
    }
    
    public String getReportType() {
        return reportType;
    }
    
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
    public Product getReportedProduct() {
        return reportedProduct;
    }
    
    public void setReportedProduct(Product reportedProduct) {
        this.reportedProduct = reportedProduct;
    }
    
    public User getReportedUser() {
        return reportedUser;
    }
    
    public void setReportedUser(User reportedUser) {
        this.reportedUser = reportedUser;
    }
    
    public UUID getReportedReviewId() {
        return reportedReviewId;
    }
    
    public void setReportedReviewId(UUID reportedReviewId) {
        this.reportedReviewId = reportedReviewId;
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
    
    public ModerationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ModerationStatus status) {
        this.status = status;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public User getReviewedBy() {
        return reviewedBy;
    }
    
    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }
    
    public String getResolutionNotes() {
        return resolutionNotes;
    }
    
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
    
    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
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
}
