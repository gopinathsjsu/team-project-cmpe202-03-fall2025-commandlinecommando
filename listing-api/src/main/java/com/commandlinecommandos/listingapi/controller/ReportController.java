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
import com.commandlinecommandos.listingapi.service.ListingService;
import com.commandlinecommandos.listingapi.security.JwtHelper;
import com.commandlinecommandos.listingapi.dto.CreateReportRequest;
import com.commandlinecommandos.listingapi.dto.UpdateReportRequest;
import com.commandlinecommandos.listingapi.dto.ReportCounts;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;
    
    @Autowired
    private ListingService listingService;

    @Autowired
    private JwtHelper jwtHelper;

    @GetMapping
    public ResponseEntity<Page<?>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {

        logger.info("Received request to get all reports - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                   page, size, sortBy, sortDirection);
        
        verifyAdmin(httpRequest);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Report> reports = reportService.getAllReports(pageable);
        
        logger.info("Successfully retrieved {} reports (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), page + 1, reports.getTotalPages(), reports.getTotalElements());
        
        return ResponseEntity.ok(reports);
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
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {

        logger.info("Received search request - status: {}, reporterId: {}, listingId: {}, reportType: {}, reviewedBy: {}, page: {}, size: {}", 
                   status, reporterId, listingId, reportType, reviewedBy, page, size);
        
        verifyAdmin(httpRequest);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Report> reports = reportService.searchReports(status, reporterId, listingId, reportType, reviewedBy, pageable);
        
        logger.info("Search completed - found {} reports (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), page + 1, reports.getTotalPages(), reports.getTotalElements());
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<?>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            HttpServletRequest httpRequest) {

        logger.info("Received request to get pending reports - page: {}, size: {}, sortBy: {}", 
                   page, size, sortBy);
        
        verifyAdmin(httpRequest);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Report> reports = reportService.getPendingReports(pageable);
        
        logger.info("Successfully retrieved {} pending reports (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), page + 1, reports.getTotalPages(), reports.getTotalElements());
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/reporter/{reporterId}")
    public ResponseEntity<Page<?>> getReportsByReporterId(
            @PathVariable Long reporterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {

        logger.info("Received request to get reports for reporter ID: {} - page: {}, size: {}, sortBy: {}", 
                   reporterId, page, size, sortBy);
        
        verifyAdmin(httpRequest);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Report> reports = reportService.getReportsByReporterId(reporterId, pageable);
        
        logger.info("Successfully retrieved {} reports for reporter ID: {} (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), reporterId, page + 1, reports.getTotalPages(), reports.getTotalElements());
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/listing/{listingId}")
    public ResponseEntity<Page<?>> getReportsByListingId(
            @PathVariable Long listingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {

        logger.info("Received request to get reports for listing ID: {} - page: {}, size: {}, sortBy: {}", 
                   listingId, page, size, sortBy);
        
        verifyAdmin(httpRequest);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Report> reports = reportService.getReportsByListingId(listingId, pageable);
        
        logger.info("Successfully retrieved {} reports for listing ID: {} (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), listingId, page + 1, reports.getTotalPages(), reports.getTotalElements());
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/type/{reportType}")
    public ResponseEntity<Page<?>> getReportsByType(
            @PathVariable ReportType reportType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {

        logger.info("Received request to get reports for type: {} - page: {}, size: {}, sortBy: {}", 
                   reportType, page, size, sortBy);
        
        verifyAdmin(httpRequest);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Report> reports = reportService.getReportsByReportType(reportType, pageable);
        
        logger.info("Successfully retrieved {} reports for type: {} (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), reportType, page + 1, reports.getTotalPages(), reports.getTotalElements());
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<?>> getReportsByStatus(
            @PathVariable ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {

        logger.info("Received request to get reports for status: {} - page: {}, size: {}, sortBy: {}", 
                   status, page, size, sortBy);
        
        verifyAdmin(httpRequest);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<Report> reports = reportService.getReportsByStatus(status, pageable);
        
        logger.info("Successfully retrieved {} reports for status: {} (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), status, page + 1, reports.getTotalPages(), reports.getTotalElements());
        
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<?> getReportById(@PathVariable Long reportId, HttpServletRequest httpRequest) {
        logger.info("Received request to get report by ID: {}", reportId);
        
        verifyAdmin(httpRequest);
        
        Report report = reportService.getReportById(reportId);
        logger.info("Successfully retrieved report ID: {} - type: {}, status: {}, reporter: {}", 
                   reportId, report.getReportType(), report.getStatus(), report.getReporterId());
        
        return ResponseEntity.ok(report);
    }

    // Any user can create a report, no need to verify admin
    @PostMapping("/")
    public ResponseEntity<?> createReport(@Valid @RequestBody CreateReportRequest request, HttpServletRequest httpRequest) {
        logger.info("Received request to create report - type: {}, listingId: {}", 
                   request.getReportType(), request.getListingId());
        
        Long reporterId = jwtHelper.extractUserIdFromRequest(httpRequest);
        if (reporterId == null) {
            logger.warn("Unauthorized report creation attempt - no valid JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        
        Report createdReport = reportService.createReport(reporterId, request.getListingId(), 
            request.getReportType(), request.getDescription());

        logger.info("Successfully created report ID: {} with type: '{}' for listing ID: {} by reporter ID: {}", 
                   createdReport.getReportId(), createdReport.getReportType(), createdReport.getListingId(), createdReport.getReporterId());
        
        return ResponseEntity.ok(createdReport);
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@PathVariable Long reportId, @Valid @RequestBody UpdateReportRequest request,
            HttpServletRequest httpRequest) {
        logger.info("Received request to update report ID: {} - type: '{}'", 
                   reportId, request.getReportType());
        
        verifyAdmin(httpRequest);
        
        Report updatedReport = reportService.updateReport(reportId, request.getReportType(), request.getDescription());
        
        logger.info("Successfully updated report ID: {} - new type: '{}', status: {}", 
                   reportId, updatedReport.getReportType(), updatedReport.getStatus());
        
        return ResponseEntity.ok(updatedReport);
    }

    @PutMapping("/{reportId}/review")
    public ResponseEntity<?> markAsReviewed(@PathVariable Long reportId, HttpServletRequest httpRequest) {
        logger.info("Received request to mark report ID: {} as reviewed", reportId);
        
        verifyAdmin(httpRequest);

        Long reviewerId = jwtHelper.extractUserIdFromRequest(httpRequest);
        if (reviewerId == null) {
            logger.warn("Unauthorized report review attempt - no valid JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        
        Report updatedReport = reportService.markAsReviewed(reportId, reviewerId);
        logger.info("Successfully marked report ID: {} as reviewed by ID: {} - status changed to: {}", 
                   reportId, reviewerId, updatedReport.getStatus());
        
        return ResponseEntity.ok(updatedReport);
    }

    @PutMapping("/{reportId}/resolve")
    public ResponseEntity<?> markAsResolved(@PathVariable Long reportId, HttpServletRequest httpRequest) {
        logger.info("Received request to mark report ID: {} as resolved", reportId);
        
        verifyAdmin(httpRequest);
        
        Report updatedReport = reportService.markAsResolved(reportId);
        logger.info("Successfully marked report ID: {} as resolved - status changed to: {}", 
                   reportId, updatedReport.getStatus());
        
        try {
            listingService.cancelListing(updatedReport.getListingId());
            logger.info("Successfully cancelled listing ID: {} due to resolved report ID: {}", 
                       updatedReport.getListingId(), reportId);
        } catch (Exception listingException) {
            logger.warn("Failed to cancel listing ID: {} when resolving report ID: {} - error: {}", 
                       updatedReport.getListingId(), reportId, listingException.getMessage());
        }
        
        return ResponseEntity.ok(updatedReport);
    }

    @PutMapping("/{reportId}/dismiss")
    public ResponseEntity<?> markAsDismissed(@PathVariable Long reportId, HttpServletRequest httpRequest) {
        logger.info("Received request to mark report ID: {} as dismissed", reportId);
        
        verifyAdmin(httpRequest);
        
        Report updatedReport = reportService.markAsDismissed(reportId);
        logger.info("Successfully marked report ID: {} as dismissed - status changed to: {}", 
                   reportId, updatedReport.getStatus());
        
        return ResponseEntity.ok(updatedReport);
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<String> deleteReport(@PathVariable Long reportId, HttpServletRequest httpRequest) {
        logger.info("Received request to delete report ID: {}", reportId);
        
        verifyAdmin(httpRequest);
        
        reportService.deleteReport(reportId);
        logger.info("Successfully deleted report ID: {}", reportId);
        
        return ResponseEntity.ok("Report deleted successfully");
    }

    @GetMapping("/count")
    public ResponseEntity<?> getReportCounts(HttpServletRequest httpRequest) {
        logger.info("Received request to get report counts");
        
        verifyAdmin(httpRequest);
        
        Long pendingCount = reportService.countReportsByStatus(ReportStatus.PENDING);
        Long underReviewCount = reportService.countReportsByStatus(ReportStatus.UNDER_REVIEW);
        Long resolvedCount = reportService.countReportsByStatus(ReportStatus.RESOLVED);
        Long dismissedCount = reportService.countReportsByStatus(ReportStatus.DISMISSED);
        
        ReportCounts counts = new ReportCounts(pendingCount, underReviewCount, resolvedCount, dismissedCount);
        
        logger.info("Successfully retrieved report counts - pending: {}, under review: {}, resolved: {}, dismissed: {}", 
                   pendingCount, underReviewCount, resolvedCount, dismissedCount);
        
        return ResponseEntity.ok(counts);
    }

    private boolean verifyAdmin(HttpServletRequest httpRequest) {
        String role = jwtHelper.extractRoleFromRequest(httpRequest);
        if (role == null || !role.equals("ADMIN")) {
            logger.warn("Unauthorized admin access attempt - no valid JWT token or role is not ADMIN");
            return false;
        }
        logger.debug("Using role: {} from JWT for admin access", role);
        return true;
    }
}
