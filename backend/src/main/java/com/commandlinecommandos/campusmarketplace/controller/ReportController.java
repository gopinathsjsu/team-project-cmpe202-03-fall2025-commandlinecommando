package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.SubmitReportRequest;
import com.commandlinecommandos.campusmarketplace.dto.ResolveReportRequest;
import com.commandlinecommandos.campusmarketplace.model.ModerationStatus;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserReport;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for Report management
 * Handles content flagging and moderation
 */
@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private UserRepository userRepository;
    
    private User getCurrentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Submit a report
     */
    @PostMapping
    public ResponseEntity<?> submitReport(@Valid @RequestBody SubmitReportRequest request,
                                                   Authentication auth) {
        try {
            User reporter = getCurrentUser(auth);
            UserReport report = reportService.submitReport(
                reporter,
                request.getReportType(),
                request.getTargetId(),
                request.getReason(),
                request.getDescription()
            );
            
            // Return a simplified response to avoid Hibernate proxy serialization issues
            Map<String, Object> response = new HashMap<>();
            response.put("reportId", report.getReportId() != null ? report.getReportId().toString() : null);
            response.put("reportType", report.getReportType());
            response.put("reason", report.getReason());
            response.put("description", report.getDescription());
            response.put("status", report.getStatus() != null ? report.getStatus().toString() : "PENDING");
            response.put("priority", report.getPriority());
            response.put("createdAt", report.getCreatedAt());
            response.put("reportedEntityId", report.getReportedEntityId() != null ? report.getReportedEntityId().toString() : null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create report", "message", e.getMessage()));
        }
    }
    
    /**
     * Get report by ID
     */
    @GetMapping("/{reportId}")
    public ResponseEntity<UserReport> getReport(@PathVariable UUID reportId) {
        UserReport report = reportService.getReport(reportId);
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get user's submitted reports
     */
    @GetMapping("/my-reports")
    public ResponseEntity<Page<UserReport>> getMyReports(Authentication auth, 
                                                         Pageable pageable) {
        User reporter = getCurrentUser(auth);
        Page<UserReport> reports = reportService.getReportsByUser(reporter, pageable);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Admin: Get pending reports (moderation queue)
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<Page<UserReport>> getPendingReports(Pageable pageable) {
        Page<UserReport> reports = reportService.getPendingReports(pageable);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Admin: Get reports by status
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<Page<UserReport>> getReportsByStatus(@PathVariable ModerationStatus status,
                                                               Pageable pageable) {
        Page<UserReport> reports = reportService.getReportsByStatus(status, pageable);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Admin: Get reports for a specific product
     */
    @GetMapping("/admin/product/{productId}")
    public ResponseEntity<List<UserReport>> getReportsForProduct(@PathVariable UUID productId) {
        List<UserReport> reports = reportService.getReportsForProduct(productId);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Admin: Get reports for a specific user
     */
    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<List<UserReport>> getReportsForUser(@PathVariable UUID userId) {
        List<UserReport> reports = reportService.getReportsForUser(userId);
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Admin: Approve report
     */
    @PostMapping("/{reportId}/approve")
    public ResponseEntity<UserReport> approveReport(@PathVariable UUID reportId,
                                                    @Valid @RequestBody ResolveReportRequest request,
                                                    Authentication auth) {
        User admin = getCurrentUser(auth);
        UserReport report = reportService.approveReport(reportId, admin, request.getResolutionNotes());
        return ResponseEntity.ok(report);
    }
    
    /**
     * Admin: Reject report
     */
    @PostMapping("/{reportId}/reject")
    public ResponseEntity<UserReport> rejectReport(@PathVariable UUID reportId,
                                                   @Valid @RequestBody ResolveReportRequest request,
                                                   Authentication auth) {
        User admin = getCurrentUser(auth);
        UserReport report = reportService.rejectReport(reportId, admin, request.getResolutionNotes());
        return ResponseEntity.ok(report);
    }
    
    /**
     * Admin: Flag report for further review
     */
    @PostMapping("/{reportId}/flag")
    public ResponseEntity<UserReport> flagReport(@PathVariable UUID reportId,
                                                 @Valid @RequestBody ResolveReportRequest request,
                                                 Authentication auth) {
        User admin = getCurrentUser(auth);
        UserReport report = reportService.flagReport(reportId, admin, request.getResolutionNotes());
        return ResponseEntity.ok(report);
    }
    
    /**
     * Admin: Get report statistics
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getReportStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", reportService.countPendingReports());
        stats.put("highPriorityCount", reportService.countHighPriorityReports());
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Admin: Get all reports (frontend expects GET /admin/reports)
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserReport> reports;
            
            if (status != null && !status.isEmpty()) {
                try {
                    ModerationStatus reportStatus = ModerationStatus.valueOf(status.toUpperCase());
                    reports = reportService.getReportsByStatus(reportStatus, pageable);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status", "validStatuses", 
                            List.of("PENDING", "APPROVED", "REJECTED", "FLAGGED")));
                }
            } else {
                // Return all reports, ordered by created date
                reports = reportService.getAllReports(pageable);
            }
            
            // Convert Page to Map for proper JSON serialization
            Map<String, Object> response = new HashMap<>();
            response.put("content", reports.getContent());
            response.put("totalElements", reports.getTotalElements());
            response.put("totalPages", reports.getTotalPages());
            response.put("number", reports.getNumber());
            response.put("size", reports.getSize());
            response.put("first", reports.isFirst());
            response.put("last", reports.isLast());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve reports", "message", e.getMessage()));
        }
    }
    
    /**
     * Admin: Update report status (frontend expects PUT /admin/reports/{reportId})
     */
    @PutMapping("/admin/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReport(
            @PathVariable UUID reportId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            User admin = getCurrentUser(auth);
            String status = body.get("status");
            
            if (status == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Status is required"));
            }
            
            UserReport report = reportService.getReport(reportId);
            String notes = body.getOrDefault("notes", "");
            
            switch (status.toUpperCase()) {
                case "APPROVED":
                    report = reportService.approveReport(reportId, admin, notes);
                    break;
                case "REJECTED":
                    report = reportService.rejectReport(reportId, admin, notes);
                    break;
                case "FLAGGED":
                    report = reportService.flagReport(reportId, admin, notes);
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status", "validStatuses", 
                            List.of("APPROVED", "REJECTED", "FLAGGED")));
            }
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update report", "message", e.getMessage()));
        }
    }
}
