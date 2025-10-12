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
import com.commandlinecommandos.listingapi.exception.ReportException;
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
        
        try {
            Report report = new Report(reporterId, listingId, reportType, description);
            Report savedReport = reportRepository.save(report);
            
            logger.info("Successfully created report ID: {} for listing ID: {} by reporter ID: {} with type: {}", 
                       savedReport.getReportId(), listingId, reporterId, reportType);
            
            return savedReport;
        } catch (Exception e) {
            logger.error("Error creating report - reporterId: {}, listingId: {}, reportType: {}, error: {}", 
                        reporterId, listingId, reportType, e.getMessage(), e);
            throw new ReportException("Error creating report - reporterId: " + reporterId + ", listingId: " + listingId + ", reportType: " + reportType + ", error: " + e.getMessage(), e);
        }
    }

    public Report getReportById(Long reportId) {
        logger.debug("Retrieving report by ID: {}", reportId);
        
        try {
            Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException("Report not found with id: " + reportId));
            
            logger.debug("Successfully retrieved report ID: {} - status: {}, type: {}, reporter: {}", 
                        reportId, report.getStatus(), report.getReportType(), report.getReporterId());
            
            return report;
        } catch (ReportException e) {
            logger.warn("Report not found with ID: {}", reportId);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving report ID: {} - error: {}", 
                        reportId, e.getMessage(), e);
            throw new ReportException("Error retrieving report ID: " + reportId + " - error: " + e.getMessage(), e);
        }
    }

    public Page<Report> getAllReports(Pageable pageable) {
        logger.debug("Retrieving all reports - page: {}, size: {}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Report> reports = reportRepository.findAll(pageable);
            
            logger.info("Successfully retrieved {} reports (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), pageable.getPageNumber() + 1, 
                       reports.getTotalPages(), reports.getTotalElements());
            
            return reports;
        } catch (Exception e) {
            logger.error("Error retrieving all reports - error: {}", 
                        e.getMessage(), e);
            throw new ReportException("Error retrieving all reports - error: " + e.getMessage(), e);
        }
    }

    public Page<Report> getReportsByStatus(ReportStatus status, Pageable pageable) {
        logger.debug("Retrieving reports by status: {} - page: {}, size: {}", 
                   status, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Report> reports = reportRepository.findByStatus(status, pageable)
                .orElseThrow(() -> new ReportException("No reports found with status: " + status));
            
            logger.info("Successfully retrieved {} reports with status: {} (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), status, pageable.getPageNumber() + 1, 
                       reports.getTotalPages(), reports.getTotalElements());
            
            return reports;
        } catch (ReportException e) {
            logger.warn("No reports found with status: {}", status);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving reports by status: {} - error: {}", 
                        status, e.getMessage(), e);
            throw new ReportException("Error retrieving reports by status: " + status + " - error: " + e.getMessage(), e);
        }
    }

    public Page<Report> getReportsByReporterId(Long reporterId, Pageable pageable) {
        logger.debug("Retrieving reports by reporter ID: {} - page: {}, size: {}", 
                   reporterId, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Report> reports = reportRepository.findByReporterId(reporterId, pageable)
                .orElseThrow(() -> new ReportException("No reports found for reporter with id: " + reporterId));
            
            logger.info("Successfully retrieved {} reports for reporter ID: {} (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), reporterId, pageable.getPageNumber() + 1, 
                       reports.getTotalPages(), reports.getTotalElements());
            
            return reports;
        } catch (ReportException e) {
            logger.warn("No reports found for reporter ID: {}", reporterId);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving reports for reporter ID: {} - error: {}", 
                        reporterId, e.getMessage(), e);
            throw new ReportException("Error retrieving reports for reporter ID: " + reporterId + " - error: " + e.getMessage(), e);
        }
    }

    public Page<Report> getReportsByListingId(Long listingId, Pageable pageable) {
        logger.debug("Retrieving reports by listing ID: {} - page: {}, size: {}", 
                   listingId, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Report> reports = reportRepository.findByListingId(listingId, pageable)
                .orElseThrow(() -> new ReportException("No reports found for listing with id: " + listingId));
            
            logger.info("Successfully retrieved {} reports for listing ID: {} (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), listingId, pageable.getPageNumber() + 1, 
                       reports.getTotalPages(), reports.getTotalElements());
            
            return reports;
        } catch (ReportException e) {
            logger.warn("No reports found for listing ID: {}", listingId);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving reports for listing ID: {} - error: {}", 
                        listingId, e.getMessage(), e);
            throw new ReportException("Error retrieving reports for listing ID: " + listingId + " - error: " + e.getMessage(), e);
        }
    }

    public Page<Report> getReportsByReportType(ReportType reportType, Pageable pageable) {
        logger.debug("Retrieving reports by type: {} - page: {}, size: {}", 
                   reportType, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Report> reports = reportRepository.findByReportType(reportType, pageable)
                .orElseThrow(() -> new ReportException("No reports found with type: " + reportType));
            
            logger.info("Successfully retrieved {} reports with type: {} (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), reportType, pageable.getPageNumber() + 1, 
                       reports.getTotalPages(), reports.getTotalElements());
            
            return reports;
        } catch (ReportException e) {
            logger.warn("No reports found with type: {}", reportType);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving reports by type: {} - error: {}", 
                        reportType, e.getMessage(), e);
            throw new ReportException("Error retrieving reports by type: " + reportType + " - error: " + e.getMessage(), e);
        }
    }

    public Page<Report> getReportsByReviewedBy(Long reviewedBy, Pageable pageable) {
        logger.debug("Retrieving reports by reviewer ID: {} - page: {}, size: {}", 
                   reviewedBy, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Report> reports = reportRepository.findByReviewedBy(reviewedBy, pageable)
                .orElseThrow(() -> new ReportException("No reports found reviewed by user with id: " + reviewedBy));
            
            logger.info("Successfully retrieved {} reports reviewed by ID: {} (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), reviewedBy, pageable.getPageNumber() + 1, 
                       reports.getTotalPages(), reports.getTotalElements());
            
            return reports;
        } catch (ReportException e) {
            logger.warn("No reports found reviewed by ID: {}", reviewedBy);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving reports reviewed by ID: {} - error: {}", 
                        reviewedBy, e.getMessage(), e);
            throw new ReportException("Error retrieving reports reviewed by ID: " + reviewedBy + " - error: " + e.getMessage(), e);
        }
    }

    public Page<Report> getPendingReports(Pageable pageable) {
        logger.debug("Retrieving pending reports - page: {}, size: {}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Report> reports = reportRepository.findPendingReportsOrderByCreatedAtAsc(pageable)
                .orElseThrow(() -> new ReportException("No pending reports found"));
            
            logger.info("Successfully retrieved {} pending reports (page {}/{} with {} total elements)", 
                       reports.getNumberOfElements(), pageable.getPageNumber() + 1, 
                       reports.getTotalPages(), reports.getTotalElements());
            
            return reports;
        } catch (ReportException e) {
            logger.warn("No pending reports found");
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving pending reports - error: {}", 
                        e.getMessage(), e);
            throw new ReportException("Error retrieving pending reports - error: " + e.getMessage(), e);
        }
    }

    public Report updateReport(Long reportId, ReportType reportType, String description) {
        logger.debug("Updating report ID: {} - type: {}, description length: {}", 
                   reportId, reportType, description != null ? description.length() : 0);
        
        try {
            Report report = getReportById(reportId);
            if (report == null) {
                logger.warn("Report not found for update - ID: {}", reportId);
                throw new ReportException("Report not found with id: " + reportId);
            }

            ReportType oldType = report.getReportType();
            String oldDescription = report.getDescription();
            
            report.setReportType(reportType);
            report.setDescription(description);
            
            Report updatedReport = reportRepository.save(report);
            
            logger.info("Successfully updated report ID: {} - type changed from {} to {}, description updated", 
                       reportId, oldType, reportType);
            
            return updatedReport;
        } catch (Exception e) {
            logger.error("Error updating report ID: {} - error: {}", 
                        reportId, e.getMessage(), e);
            throw new ReportException("Error updating report ID: " + reportId + " - error: " + e.getMessage(), e);
        }
    }

    public Report markAsReviewed(Long reportId, Long reviewerId) {
        logger.debug("Marking report ID: {} as reviewed by reviewer ID: {}", reportId, reviewerId);
        
        try {
            Report report = getReportById(reportId);
            if (report == null) {
                logger.warn("Report not found for review - ID: {}", reportId);
                throw new ReportException("Report not found with id: " + reportId);
            }

            ReportStatus oldStatus = report.getStatus();
            report.markAsReviewedBy(reviewerId);
            Report updatedReport = reportRepository.save(report);
            
            logger.info("Successfully marked report ID: {} as reviewed by ID: {} - status changed from {} to {}", 
                       reportId, reviewerId, oldStatus, updatedReport.getStatus());
            
            return updatedReport;
        } catch (Exception e) {
            logger.error("Error marking report ID: {} as reviewed by ID: {} - error: {}", 
                        reportId, reviewerId, e.getMessage(), e);
            throw new ReportException("Error marking report ID: " + reportId + " as reviewed by ID: " + reviewerId + " - error: " + e.getMessage(), e);
        }
    }

    public Report markAsResolved(Long reportId) {
        logger.debug("Resolving report ID: {}", reportId);
        
        try {
            Report report = getReportById(reportId);
            if (report == null) {
                logger.warn("Report not found for resolution - ID: {}", reportId);
                throw new ReportException("Report not found with id: " + reportId);
            }

            ReportStatus oldStatus = report.getStatus();
            report.markAsResolved();
            Report updatedReport = reportRepository.save(report);
            
            logger.info("Successfully resolved report ID: {} - status changed from {} to {}", 
                       reportId, oldStatus, updatedReport.getStatus());
            
            return updatedReport;
        } catch (Exception e) {
            logger.error("Error resolving report ID: {} - error: {}", 
                        reportId, e.getMessage(), e);
            throw new ReportException("Error resolving report ID: " + reportId + " - error: " + e.getMessage(), e);
        }
    }

    public Report markAsDismissed(Long reportId) {
        logger.debug("Dismissing report ID: {}", reportId);
        
        try {
            Report report = getReportById(reportId);
            if (report == null) {
                logger.warn("Report not found for dismissal - ID: {}", reportId);
                throw new ReportException("Report not found with id: " + reportId);
            }

            ReportStatus oldStatus = report.getStatus();
            report.markAsDismissed();
            Report updatedReport = reportRepository.save(report);
            
            logger.info("Successfully dismissed report ID: {} - status changed from {} to {}", 
                       reportId, oldStatus, updatedReport.getStatus());
            
            return updatedReport;
        } catch (Exception e) {
            logger.error("Error dismissing report ID: {} - error: {}", 
                        reportId, e.getMessage(), e);
            throw new ReportException("Error dismissing report ID: " + reportId + " - error: " + e.getMessage(), e);
        }
    }

    public void deleteReport(Long reportId) {
        logger.debug("Deleting report ID: {}", reportId);
        
        try {
            Report report = getReportById(reportId);
            if (report == null) {
                logger.warn("Report not found for deletion - ID: {}", reportId);
                throw new ReportException("Report not found with id: " + reportId);
            }
            
            Long reporterId = report.getReporterId();
            Long listingId = report.getListingId();
            ReportType reportType = report.getReportType();
            reportRepository.delete(report);
            
            logger.info("Successfully deleted report ID: {} (type: {}, reporter: {}, listing: {})", 
                       reportId, reportType, reporterId, listingId);
        } catch (Exception e) {
            logger.error("Error deleting report ID: {} - error: {}", 
                        reportId, e.getMessage(), e);
            throw new ReportException("Error deleting report ID: " + reportId + " - error: " + e.getMessage(), e);
        }
    }

    public Long countReportsByStatus(ReportStatus status) {
        logger.debug("Counting reports by status: {}", status);
        
        try {
            Long count = reportRepository.countByStatus(status);
            
            logger.debug("Successfully counted {} reports with status: {}", count, status);
            
            return count;
        } catch (Exception e) {
            logger.error("Error counting reports by status: {} - error: {}", 
                        status, e.getMessage(), e);
            throw new ReportException("Error counting reports by status: " + status + " - error: " + e.getMessage(), e);
        }
    }

    public Long countReportsByReporterId(Long reporterId) {
        logger.debug("Counting reports by reporter ID: {}", reporterId);
        
        try {
            Long count = reportRepository.countByReporterId(reporterId);
            
            logger.debug("Successfully counted {} reports for reporter ID: {}", count, reporterId);
            
            return count;
        } catch (Exception e) {
            logger.error("Error counting reports by reporter ID: {} - error: {}", 
                        reporterId, e.getMessage(), e);
            throw new ReportException("Error counting reports by reporter ID: " + reporterId + " - error: " + e.getMessage(), e);
        }
    }
}
