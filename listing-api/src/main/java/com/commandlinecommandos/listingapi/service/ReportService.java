package com.commandlinecommandos.listingapi.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.commandlinecommandos.listingapi.model.Report;
import com.commandlinecommandos.listingapi.model.ReportStatus;
import com.commandlinecommandos.listingapi.model.ReportType;
import com.commandlinecommandos.listingapi.repository.ReportRepository;
import com.commandlinecommandos.listingapi.exception.ReportNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportRepository reportRepository;

    public Report createReport(Long reporterId, Long listingId, ReportType reportType, String description) {
        logger.debug("Creating report - reporterId: {}, listingId: {}, reportType: {}", 
                   reporterId, listingId, reportType);
        
        Report report = new Report(reporterId, listingId, reportType, description);
        Report savedReport = reportRepository.save(report);
        
        logger.info("Successfully created report ID: {} for listing ID: {} by reporter ID: {} with type: {}", 
                   savedReport.getReportId(), listingId, reporterId, reportType);
        
        return savedReport;
    }

    public Report getReportById(Long reportId) {
        logger.debug("Retrieving report by ID: {}", reportId);
        
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));
        
        logger.debug("Successfully retrieved report ID: {} - status: {}, type: {}, reporter: {}", 
                    reportId, report.getStatus(), report.getReportType(), report.getReporterId());
        
        return report;
    }

    public Page<Report> getAllReports(Pageable pageable) {
        logger.debug("Retrieving all reports - page: {}, size: {}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Report> reports = reportRepository.findAll(pageable);
        
        logger.info("Successfully retrieved {} reports (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), pageable.getPageNumber() + 1, 
                   reports.getTotalPages(), reports.getTotalElements());
        
        return reports;
    }

    public Page<Report> getReportsByStatus(ReportStatus status, Pageable pageable) {
        logger.debug("Retrieving reports by status: {} - page: {}, size: {}", 
                   status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Report> reports = reportRepository.findByStatus(status, pageable)
            .orElseThrow(() -> new ReportNotFoundException("No reports found with status: " + status));
        
        logger.info("Successfully retrieved {} reports with status: {} (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), status, pageable.getPageNumber() + 1, 
                   reports.getTotalPages(), reports.getTotalElements());
        
        return reports;
    }

    public Page<Report> getReportsByReporterId(Long reporterId, Pageable pageable) {
        logger.debug("Retrieving reports by reporter ID: {} - page: {}, size: {}", 
                   reporterId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Report> reports = reportRepository.findByReporterId(reporterId, pageable)
            .orElseThrow(() -> new ReportNotFoundException("No reports found for reporter with id: " + reporterId));
        
        logger.info("Successfully retrieved {} reports for reporter ID: {} (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), reporterId, pageable.getPageNumber() + 1, 
                   reports.getTotalPages(), reports.getTotalElements());
        
        return reports;
    }

    public Page<Report> getReportsByListingId(Long listingId, Pageable pageable) {
        logger.debug("Retrieving reports by listing ID: {} - page: {}, size: {}", 
                   listingId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Report> reports = reportRepository.findByListingId(listingId, pageable)
            .orElseThrow(() -> new ReportNotFoundException("No reports found for listing with id: " + listingId));
        
        logger.info("Successfully retrieved {} reports for listing ID: {} (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), listingId, pageable.getPageNumber() + 1, 
                   reports.getTotalPages(), reports.getTotalElements());
        
        return reports;
    }

    public Page<Report> getReportsByReportType(ReportType reportType, Pageable pageable) {
        logger.debug("Retrieving reports by type: {} - page: {}, size: {}", 
                   reportType, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Report> reports = reportRepository.findByReportType(reportType, pageable)
            .orElseThrow(() -> new ReportNotFoundException("No reports found with type: " + reportType));
        
        logger.info("Successfully retrieved {} reports with type: {} (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), reportType, pageable.getPageNumber() + 1, 
                   reports.getTotalPages(), reports.getTotalElements());
        
        return reports;
    }

    public Page<Report> getReportsByReviewedBy(Long reviewedBy, Pageable pageable) {
        logger.debug("Retrieving reports by reviewer ID: {} - page: {}, size: {}", 
                   reviewedBy, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Report> reports = reportRepository.findByReviewedBy(reviewedBy, pageable)
            .orElseThrow(() -> new ReportNotFoundException("No reports found reviewed by user with id: " + reviewedBy));
        
        logger.info("Successfully retrieved {} reports reviewed by ID: {} (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), reviewedBy, pageable.getPageNumber() + 1, 
                   reports.getTotalPages(), reports.getTotalElements());
        
        return reports;
    }

    public Page<Report> searchReports(ReportStatus status, Long reporterId, Long listingId, ReportType reportType, Long reviewedBy, Pageable pageable) {
        logger.debug("Searching reports - status: {}, reporterId: {}, listingId: {}, reportType: {}, reviewedBy: {}, page: {}, size: {}", 
                   status, reporterId, listingId, reportType, reviewedBy, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Report> reports = reportRepository.findWithFilters(status, reportType, reporterId, listingId, pageable)
                .orElseThrow(() -> new ReportNotFoundException("No reports found with the specified filters"));
            
            logger.info("Successfully searched reports - found {} reports (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), pageable.getPageNumber() + 1, 
                       reports.getTotalPages(), reports.getTotalElements());
            
            return reports;
        } catch (ReportNotFoundException e) {
            // Re-throw ReportNotFoundException as-is (for empty results)
            throw e;
        } catch (RuntimeException e) {
            logger.error("Repository error while searching reports - status: {}, reporterId: {}, listingId: {}, reportType: {}, reviewedBy: {}", 
                        status, reporterId, listingId, reportType, reviewedBy, e);
            throw new ReportNotFoundException("Error searching reports: " + e.getMessage() + 
                                            " (status=" + status + ", reporterId=" + reporterId + 
                                            ", listingId=" + listingId + ", reportType=" + reportType + 
                                            ", reviewedBy=" + reviewedBy + ")");
        }
    }

    public Page<Report> getPendingReports(Pageable pageable) {
        logger.debug("Retrieving pending reports - page: {}, size: {}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Report> reports = reportRepository.findPendingReportsOrderByCreatedAtAsc(pageable)
            .orElseThrow(() -> new ReportNotFoundException("No pending reports found"));
        
        logger.info("Successfully retrieved {} pending reports (page {}/{} with {} total elements)", 
                   reports.getNumberOfElements(), pageable.getPageNumber() + 1, 
                   reports.getTotalPages(), reports.getTotalElements());
        
        return reports;
    }

    public Report updateReport(Long reportId, ReportType reportType, String description) {
        logger.debug("Updating report ID: {} - type: {}, description length: {}", 
                   reportId, reportType, description != null ? description.length() : 0);
        
        Report report = getReportById(reportId);

        ReportType oldType = report.getReportType();
        String oldDescription = report.getDescription();
        
        report.setReportType(reportType);
        report.setDescription(description);
        
        Report updatedReport = reportRepository.save(report);
        
        logger.info("Successfully updated report ID: {} - type changed from {} to {}, description updated", 
                   reportId, oldType, reportType);
        
        return updatedReport;
    }

    public Report markAsReviewed(Long reportId, Long reviewerId) {
        logger.debug("Marking report ID: {} as reviewed by reviewer ID: {}", reportId, reviewerId);
        
        Report report = getReportById(reportId);

        ReportStatus oldStatus = report.getStatus();
        report.markAsReviewedBy(reviewerId);
        Report updatedReport = reportRepository.save(report);
        
        logger.info("Successfully marked report ID: {} as reviewed by ID: {} - status changed from {} to {}", 
                   reportId, reviewerId, oldStatus, updatedReport.getStatus());
        
        return updatedReport;
    }

    public Report markAsResolved(Long reportId) {
        logger.debug("Resolving report ID: {}", reportId);
        
        Report report = getReportById(reportId);

        ReportStatus oldStatus = report.getStatus();
        report.markAsResolved();
        Report updatedReport = reportRepository.save(report);
        
        logger.info("Successfully resolved report ID: {} - status changed from {} to {}", 
                   reportId, oldStatus, updatedReport.getStatus());
        
        return updatedReport;
    }

    public Report markAsDismissed(Long reportId) {
        logger.debug("Dismissing report ID: {}", reportId);
        
        Report report = getReportById(reportId);

        ReportStatus oldStatus = report.getStatus();
        report.markAsDismissed();
        Report updatedReport = reportRepository.save(report);
        
        logger.info("Successfully dismissed report ID: {} - status changed from {} to {}", 
                   reportId, oldStatus, updatedReport.getStatus());
        
        return updatedReport;
    }

    public void deleteReport(Long reportId) {
        logger.debug("Deleting report ID: {}", reportId);
        
        Report report = getReportById(reportId);
        
        Long reporterId = report.getReporterId();
        Long listingId = report.getListingId();
        ReportType reportType = report.getReportType();
        reportRepository.delete(report);
        
        logger.info("Successfully deleted report ID: {} (type: {}, reporter: {}, listing: {})", 
                   reportId, reportType, reporterId, listingId);
    }

    public Long countReportsByStatus(ReportStatus status) {
        logger.debug("Counting reports by status: {}", status);
        
        Long count = reportRepository.countByStatus(status);
        
        logger.debug("Successfully counted {} reports with status: {}", count, status);
        
        return count;
    }

    public Long countReportsByReporterId(Long reporterId) {
        logger.debug("Counting reports by reporter ID: {}", reporterId);
        
        Long count = reportRepository.countByReporterId(reporterId);
        
        logger.debug("Successfully counted {} reports for reporter ID: {}", count, reporterId);
        
        return count;
    }
}
