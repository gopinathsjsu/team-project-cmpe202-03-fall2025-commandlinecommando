package com.commandlinecommandos.listingapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.commandlinecommandos.listingapi.model.Report;
import com.commandlinecommandos.listingapi.model.ReportStatus;
import com.commandlinecommandos.listingapi.model.ReportType;
import com.commandlinecommandos.listingapi.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    // TODO: Add authentication and authorization, only admin can access this endpoint
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<Page<?>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Received request to get all reports - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                   page, size, sortBy, sortDirection);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
            Page<Report> reports = reportService.getAllReports(pageable);
            
            logger.info("Successfully retrieved {} reports (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), page + 1, reports.getTotalPages(), reports.getTotalElements());
            
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Error retrieving all reports - page: {}, size: {}, error: {}", 
                        page, size, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<?>> searchReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) Long listingId,
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(required = false) Long reviewedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Received search request - status: {}, reporterId: {}, listingId: {}, reportType: {}, reviewedBy: {}, page: {}, size: {}", 
                   status, reporterId, listingId, reportType, reviewedBy, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
            Page<Report> reports = reportService.searchReports(status, reporterId, listingId, reportType, reviewedBy, pageable);
            
            logger.info("Search completed - found {} reports (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), page + 1, reports.getTotalPages(), reports.getTotalElements());
            
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Error during search - filters: status={}, reporterId={}, listingId={}, reportType={}, error: {}", 
                        status, reporterId, listingId, reportType, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<?>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        logger.info("Received request to get pending reports - page: {}, size: {}, sortBy: {}", 
                   page, size, sortBy);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
            Page<Report> reports = reportService.getPendingReports(pageable);
            
            logger.info("Successfully retrieved {} pending reports (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), page + 1, reports.getTotalPages(), reports.getTotalElements());
            
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Error retrieving pending reports - page: {}, size: {}, error: {}", 
                        page, size, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<Page<?>> getReportsByReporterId(
            @PathVariable Long reporterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Received request to get reports for reporter ID: {} - page: {}, size: {}, sortBy: {}", 
                   reporterId, page, size, sortBy);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
            Page<Report> reports = reportService.getReportsByReporterId(reporterId, pageable);
            
            logger.info("Successfully retrieved {} reports for reporter ID: {} (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), reporterId, page + 1, reports.getTotalPages(), reports.getTotalElements());
            
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Error retrieving reports for reporter ID: {} - error: {}", 
                        reporterId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<Page<?>> getReportsByListingId(
            @PathVariable Long listingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Received request to get reports for listing ID: {} - page: {}, size: {}, sortBy: {}", 
                   listingId, page, size, sortBy);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
            Page<Report> reports = reportService.getReportsByListingId(listingId, pageable);
            
            logger.info("Successfully retrieved {} reports for listing ID: {} (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), listingId, page + 1, reports.getTotalPages(), reports.getTotalElements());
            
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Error retrieving reports for listing ID: {} - error: {}", 
                        listingId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/type/{reportType}")
    public ResponseEntity<Page<?>> getReportsByType(
            @PathVariable ReportType reportType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Received request to get reports for type: {} - page: {}, size: {}, sortBy: {}", 
                   reportType, page, size, sortBy);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
            Page<Report> reports = reportService.getReportsByReportType(reportType, pageable);
            
            logger.info("Successfully retrieved {} reports for type: {} (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), reportType, page + 1, reports.getTotalPages(), reports.getTotalElements());
            
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Error retrieving reports for type: {} - error: {}", 
                        reportType, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<?>> getReportsByStatus(
            @PathVariable ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        logger.info("Received request to get reports for status: {} - page: {}, size: {}, sortBy: {}", 
                   status, page, size, sortBy);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
            Page<Report> reports = reportService.getReportsByStatus(status, pageable);
            
            logger.info("Successfully retrieved {} reports for status: {} (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), status, page + 1, reports.getTotalPages(), reports.getTotalElements());
            
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            logger.error("Error retrieving reports for status: {} - error: {}", 
                        status, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<?> getReportById(@PathVariable Long reportId) {
        logger.info("Received request to get report by ID: {}", reportId);
        
        try {
            Report report = reportService.getReportById(reportId);
            logger.info("Successfully retrieved report ID: {} - type: {}, status: {}, reporter: {}", 
                       reportId, report.getReportType(), report.getStatus(), report.getReporterId());
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("Error retrieving report ID: {} - error: {}", 
                        reportId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> createReport(@Valid @RequestBody CreateReportRequest request) {
        logger.info("Received request to create report - type: {}, listingId: {}, reporterId: {}", 
                   request.getReportType(), request.getListingId(), request.getReporterId());
        
        try {
            Report createdReport = reportService.createReport(request.getReporterId(), request.getListingId(), 
                request.getReportType(), request.getDescription());

            logger.info("Successfully created report ID: {} with type: '{}' for listing ID: {} by reporter ID: {}", 
                       createdReport.getReportId(), createdReport.getReportType(), createdReport.getListingId(), createdReport.getReporterId());
            
            return ResponseEntity.ok(createdReport);
        } catch (Exception e) {
            logger.error("Error creating report - type: '{}', listingId: {}, error: {}", 
                        request.getReportType(), request.getListingId(), e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@PathVariable Long reportId, @Valid @RequestBody UpdateReportRequest request) {
        logger.info("Received request to update report ID: {} - type: '{}'", 
                   reportId, request.getReportType());
        
        try {
            Report updatedReport = reportService.updateReport(reportId, request.getReportType(), request.getDescription());
            
            logger.info("Successfully updated report ID: {} - new type: '{}', status: {}", 
                       reportId, updatedReport.getReportType(), updatedReport.getStatus());
            
            return ResponseEntity.ok(updatedReport);
        } catch (Exception e) {
            logger.error("Error updating report ID: {} - error: {}", 
                        reportId, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{reportId}/review")
    public ResponseEntity<?> markAsReviewed(@PathVariable Long reportId) {
        logger.info("Received request to mark report ID: {} as reviewed", reportId);
        
        try {
            // TODO: retrieve reviewer id from authentication
            Long reviewerId = 1L;
            logger.debug("Using temporary reviewer ID: {} for report review", reviewerId);
            
            Report updatedReport = reportService.markAsReviewed(reportId, reviewerId);
            logger.info("Successfully marked report ID: {} as reviewed by ID: {} - status changed to: {}", 
                       reportId, reviewerId, updatedReport.getStatus());
            
            return ResponseEntity.ok(updatedReport);
        } catch (Exception e) {
            logger.error("Error marking report ID: {} as reviewed - error: {}", 
                        reportId, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{reportId}/resolve")
    public ResponseEntity<?> markAsResolved(@PathVariable Long reportId) {
        logger.info("Received request to mark report ID: {} as resolved", reportId);
        
        try {
            Report updatedReport = reportService.markAsResolved(reportId);
            logger.info("Successfully marked report ID: {} as resolved - status changed to: {}", 
                       reportId, updatedReport.getStatus());
            
            return ResponseEntity.ok(updatedReport);
        } catch (Exception e) {
            logger.error("Error marking report ID: {} as resolved - error: {}", 
                        reportId, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{reportId}/dismiss")
    public ResponseEntity<?> markAsDismissed(@PathVariable Long reportId) {
        logger.info("Received request to mark report ID: {} as dismissed", reportId);
        
        try {
            Report updatedReport = reportService.markAsDismissed(reportId);
            logger.info("Successfully marked report ID: {} as dismissed - status changed to: {}", 
                       reportId, updatedReport.getStatus());
            
            return ResponseEntity.ok(updatedReport);
        } catch (Exception e) {
            logger.error("Error marking report ID: {} as dismissed - error: {}", 
                        reportId, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<String> deleteReport(@PathVariable Long reportId) {
        logger.info("Received request to delete report ID: {}", reportId);
        
        try {
            reportService.deleteReport(reportId);
            logger.info("Successfully deleted report ID: {}", reportId);
            
            return ResponseEntity.ok("Report deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting report ID: {} - error: {}", 
                        reportId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getReportCounts() {
        logger.info("Received request to get report counts");
        
        try {
            Long pendingCount = reportService.countReportsByStatus(ReportStatus.PENDING);
            Long underReviewCount = reportService.countReportsByStatus(ReportStatus.UNDER_REVIEW);
            Long resolvedCount = reportService.countReportsByStatus(ReportStatus.RESOLVED);
            Long dismissedCount = reportService.countReportsByStatus(ReportStatus.DISMISSED);
            
            ReportCounts counts = new ReportCounts(pendingCount, underReviewCount, resolvedCount, dismissedCount);
            
            logger.info("Successfully retrieved report counts - pending: {}, under review: {}, resolved: {}, dismissed: {}", 
                       pendingCount, underReviewCount, resolvedCount, dismissedCount);
            
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            logger.error("Error retrieving report counts - error: {}", 
                        e.getMessage(), e);
            throw e;
        }
    }

    public static class CreateReportRequest {
        private Long reporterId;
        private Long listingId;
        private ReportType reportType;
        private String description;

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
    }

    public static class UpdateReportRequest {
        private ReportType reportType;
        private String description;

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
    }

    public static class ReportCounts {
        private Long pending;
        private Long underReview;
        private Long resolved;
        private Long dismissed;

        public ReportCounts(Long pending, Long underReview, Long resolved, Long dismissed) {
            this.pending = pending;
            this.underReview = underReview;
            this.resolved = resolved;
            this.dismissed = dismissed;
        }

        public Long getPending() {
            return pending;
        }

        public void setPending(Long pending) {
            this.pending = pending;
        }

        public Long getUnderReview() {
            return underReview;
        }

        public void setUnderReview(Long underReview) {
            this.underReview = underReview;
        }

        public Long getResolved() {
            return resolved;
        }

        public void setResolved(Long resolved) {
            this.resolved = resolved;
        }

        public Long getDismissed() {
            return dismissed;
        }

        public void setDismissed(Long dismissed) {
            this.dismissed = dismissed;
        }
    }
}
