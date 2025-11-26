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
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserReport> submitReport(@Valid @RequestBody SubmitReportRequest request,
                                                   Authentication auth) {
        User reporter = getCurrentUser(auth);
        UserReport report = reportService.submitReport(
            reporter,
            request.getReportType(),
            request.getTargetId(),
            request.getReason(),
            request.getDescription()
        );
        return ResponseEntity.ok(report);
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
}
