package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.exception.ResourceNotFoundException;
import com.commandlinecommandos.campusmarketplace.exception.BadRequestException;
import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.UserReportRepository;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for User Report management
 * Handles content flagging and moderation
 */
@Service
@Transactional
public class ReportService {
    
    @Autowired
    private UserReportRepository reportRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Submit a report
     */
    public UserReport submitReport(User reporter, String reportType, UUID targetId, 
                                   String reason, String description) {
        // Create report with reportedEntityId (required by DB)
        UserReport report = new UserReport(reporter, reportType, targetId, reason, description);
        
        // Set the reported entity based on type
        switch (reportType.toUpperCase()) {
            case "PRODUCT":
                Product product = productRepository.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                report.setReportedProduct(product);
                break;
                
            case "USER":
                User user = userRepository.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                report.setReportedUser(user);
                break;
                
            case "REVIEW":
                report.setReportedReviewId(targetId);
                break;
                
            default:
                throw new BadRequestException("Invalid report type: " + reportType);
        }
        
        // Set priority based on reason
        String priority = determinePriority(reason);
        report.setPriority(priority);
        
        return reportRepository.save(report);
    }
    
    /**
     * Get report by ID
     */
    public UserReport getReport(UUID reportId) {
        return reportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
    }
    
    /**
     * Get pending reports (for admin moderation queue)
     */
    public Page<UserReport> getPendingReports(Pageable pageable) {
        return reportRepository.findByStatusOrderByPriorityDescCreatedAtDesc(
            ModerationStatus.PENDING, pageable);
    }
    
    /**
     * Get reports by status
     */
    public Page<UserReport> getReportsByStatus(ModerationStatus status, Pageable pageable) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }
    
    /**
     * Get reports submitted by user
     */
    public Page<UserReport> getReportsByUser(User reporter, Pageable pageable) {
        return reportRepository.findByReporterOrderByCreatedAtDesc(reporter, pageable);
    }
    
    /**
     * Get reports about a product
     */
    public List<UserReport> getReportsForProduct(UUID productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return reportRepository.findByReportedProductOrderByCreatedAtDesc(product);
    }
    
    /**
     * Get reports about a user
     */
    public List<UserReport> getReportsForUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return reportRepository.findByReportedUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Approve report and take action
     * When a report is approved, it means the report is valid and action should be taken
     */
    public UserReport approveReport(UUID reportId, User admin, String resolutionNotes) {
        UserReport report = getReport(reportId);
        report.approve(admin, resolutionNotes);

        // Take action based on report type
        if (report.getReportedProduct() != null) {
            // Deactivate and reject the product to remove it from marketplace
            Product product = report.getReportedProduct();
            product.setActive(false);  // Hide from marketplace
            product.setModerationStatus(ModerationStatus.REJECTED);  // Mark as rejected by admin
            productRepository.save(product);
        }

        if (report.getReportedUser() != null) {
            // TODO: Implement action against reported user (e.g., suspension, warning)
            // For now, only the report status is updated
        }

        return reportRepository.save(report);
    }
    
    /**
     * Reject report and remove the listing
     * When admin rejects a listing, it should be removed from marketplace
     */
    public UserReport rejectReport(UUID reportId, User admin, String resolutionNotes) {
        UserReport report = getReport(reportId);
        report.reject(admin, resolutionNotes);

        // Take action to remove the listing from marketplace
        if (report.getReportedProduct() != null) {
            Product product = report.getReportedProduct();
            product.setActive(false);  // Hide from marketplace
            product.setModerationStatus(ModerationStatus.REJECTED);  // Mark as rejected by admin
            productRepository.save(product);
        }

        return reportRepository.save(report);
    }
    
    /**
     * Flag report for further review
     */
    public UserReport flagReport(UUID reportId, User admin, String notes) {
        UserReport report = getReport(reportId);
        report.flag(admin, notes);
        return reportRepository.save(report);
    }
    
    /**
     * Count pending reports
     */
    public long countPendingReports() {
        return reportRepository.countByStatus(ModerationStatus.PENDING);
    }
    
    /**
     * Count high priority pending reports
     */
    public long countHighPriorityReports() {
        return reportRepository.countByStatusAndPriority(ModerationStatus.PENDING, "HIGH");
    }
    
    /**
     * Get all reports with pagination
     */
    public Page<UserReport> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }
    
    /**
     * Determine report priority based on reason
     */
    private String determinePriority(String reason) {
        return switch (reason.toUpperCase()) {
            case "SCAM", "FRAUD", "ILLEGAL" -> "CRITICAL";
            case "INAPPROPRIATE", "HARASSMENT" -> "HIGH";
            case "SPAM", "FAKE" -> "MEDIUM";
            default -> "LOW";
        };
    }
}
