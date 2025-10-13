package com.commandlinecommandos.listingapi.service;

import com.commandlinecommandos.listingapi.exception.ReportException;
import com.commandlinecommandos.listingapi.model.*;
import com.commandlinecommandos.listingapi.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private Report testReport;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testPageable = PageRequest.of(0, 10);
        
        // Create test report
        testReport = new Report();
        testReport.setReportId(1L);
        testReport.setReporterId(100L);
        testReport.setListingId(200L);
        testReport.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        testReport.setDescription("This listing contains inappropriate content");
        testReport.setStatus(ReportStatus.PENDING);
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setReviewedAt(null);
        testReport.setReviewedBy(null);
    }

    // Test createReport method
    @Test
    void testCreateReport_Success() {
        // Arrange
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);

        // Act
        Report result = reportService.createReport(
            100L,
            200L,
            ReportType.INAPPROPRIATE_CONTENT,
            "This listing contains inappropriate content"
        );

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getReporterId());
        assertEquals(200L, result.getListingId());
        assertEquals(ReportType.INAPPROPRIATE_CONTENT, result.getReportType());
        assertEquals("This listing contains inappropriate content", result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt());

        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testCreateReport_WithNullDescription() {
        // Arrange
        testReport.setDescription(null);
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);

        // Act
        Report result = reportService.createReport(
            100L,
            200L,
            ReportType.INAPPROPRIATE_CONTENT,
            null
        );

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getReporterId());
        assertEquals(200L, result.getListingId());
        assertEquals(ReportType.INAPPROPRIATE_CONTENT, result.getReportType());
        assertNull(result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());

        verify(reportRepository).save(any(Report.class));
    }

    // Test getReportById method
    @Test
    void testGetReportById_Success() {
        // Arrange
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        // Act
        Report result = reportService.getReportById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testReport, result);
        verify(reportRepository).findById(1L);
    }

    @Test
    void testGetReportById_NotFound() {
        // Arrange
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.getReportById(999L);
        });

        // Assert
        assertEquals("Report not found with id: 999", exception.getMessage());
        verify(reportRepository).findById(999L);
    }

    // Test getAllReports method
    @Test
    void testGetAllReports_Success() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findAll(testPageable)).thenReturn(expectedPage);

        // Act
        Page<Report> result = reportService.getAllReports(testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findAll(testPageable);
    }

    // Test getReportsByStatus method
    @Test
    void testGetReportsByStatus_Success() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findByStatus(ReportStatus.PENDING, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.getReportsByStatus(ReportStatus.PENDING, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findByStatus(ReportStatus.PENDING, testPageable);
    }

    @Test
    void testGetReportsByStatus_NotFound() {
        // Arrange
        when(reportRepository.findByStatus(ReportStatus.RESOLVED, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.getReportsByStatus(ReportStatus.RESOLVED, testPageable);
        });

        // Assert
        assertEquals("No reports found with status: RESOLVED", exception.getMessage());
        verify(reportRepository).findByStatus(ReportStatus.RESOLVED, testPageable);
    }

    // Test getReportsByReporterId method
    @Test
    void testGetReportsByReporterId_Success() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findByReporterId(100L, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.getReportsByReporterId(100L, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findByReporterId(100L, testPageable);
    }

    @Test
    void testGetReportsByReporterId_NotFound() {
        // Arrange
        when(reportRepository.findByReporterId(999L, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.getReportsByReporterId(999L, testPageable);
        });

        // Assert
        assertEquals("No reports found for reporter with id: 999", exception.getMessage());
        verify(reportRepository).findByReporterId(999L, testPageable);
    }

    // Test getReportsByListingId method
    @Test
    void testGetReportsByListingId_Success() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findByListingId(200L, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.getReportsByListingId(200L, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findByListingId(200L, testPageable);
    }

    @Test
    void testGetReportsByListingId_NotFound() {
        // Arrange
        when(reportRepository.findByListingId(999L, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.getReportsByListingId(999L, testPageable);
        });

        // Assert
        assertEquals("No reports found for listing with id: 999", exception.getMessage());
        verify(reportRepository).findByListingId(999L, testPageable);
    }

    // Test getReportsByReportType method
    @Test
    void testGetReportsByReportType_Success() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findByReportType(ReportType.INAPPROPRIATE_CONTENT, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.getReportsByReportType(ReportType.INAPPROPRIATE_CONTENT, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findByReportType(ReportType.INAPPROPRIATE_CONTENT, testPageable);
    }

    @Test
    void testGetReportsByReportType_NotFound() {
        // Arrange
        when(reportRepository.findByReportType(ReportType.FAKE_LISTING, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.getReportsByReportType(ReportType.FAKE_LISTING, testPageable);
        });

        // Assert
        assertEquals("No reports found with type: FAKE_LISTING", exception.getMessage());
        verify(reportRepository).findByReportType(ReportType.FAKE_LISTING, testPageable);
    }

    // Test getReportsByReviewedBy method
    @Test
    void testGetReportsByReviewedBy_Success() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findByReviewedBy(300L, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.getReportsByReviewedBy(300L, testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findByReviewedBy(300L, testPageable);
    }

    @Test
    void testGetReportsByReviewedBy_NotFound() {
        // Arrange
        when(reportRepository.findByReviewedBy(999L, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.getReportsByReviewedBy(999L, testPageable);
        });

        // Assert
        assertEquals("No reports found reviewed by user with id: 999", exception.getMessage());
        verify(reportRepository).findByReviewedBy(999L, testPageable);
    }

    // Test getPendingReports method
    @Test
    void testGetPendingReports_Success() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findPendingReportsOrderByCreatedAtAsc(testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.getPendingReports(testPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findPendingReportsOrderByCreatedAtAsc(testPageable);
    }

    @Test
    void testGetPendingReports_NotFound() {
        // Arrange
        when(reportRepository.findPendingReportsOrderByCreatedAtAsc(testPageable))
            .thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.getPendingReports(testPageable);
        });

        // Assert
        assertEquals("No pending reports found", exception.getMessage());
        verify(reportRepository).findPendingReportsOrderByCreatedAtAsc(testPageable);
    }

    // Test updateReport method
    @Test
    void testUpdateReport_Success() {
        // Arrange
        Report updatedReport = new Report();
        updatedReport.setReportId(1L);
        updatedReport.setReporterId(100L);
        updatedReport.setListingId(200L);
        updatedReport.setReportType(ReportType.SPAM);
        updatedReport.setDescription("Updated description");
        updatedReport.setStatus(ReportStatus.PENDING);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(Report.class))).thenReturn(updatedReport);

        // Act
        Report result = reportService.updateReport(
            1L,
            ReportType.SPAM,
            "Updated description"
        );

        // Assert
        assertNotNull(result);
        assertEquals(ReportType.SPAM, result.getReportType());
        assertEquals("Updated description", result.getDescription());

        verify(reportRepository).findById(1L);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testUpdateReport_NotFound() {
        // Arrange
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.updateReport(
                999L,
                ReportType.SPAM,
                "Updated description"
            );
        });

        // Assert
        assertEquals("Error updating report ID: 999 - error: Report not found with id: 999", exception.getMessage());
        verify(reportRepository).findById(999L);
        verify(reportRepository, never()).save(any(Report.class));
    }

    // Test markAsReviewed method
    @Test
    void testMarkAsReviewed_Success() {
        // Arrange
        Report reviewedReport = new Report();
        reviewedReport.setReportId(1L);
        reviewedReport.setReporterId(100L);
        reviewedReport.setListingId(200L);
        reviewedReport.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        reviewedReport.setDescription("This listing contains inappropriate content");
        reviewedReport.setStatus(ReportStatus.UNDER_REVIEW);
        reviewedReport.setReviewedAt(LocalDateTime.now());
        reviewedReport.setReviewedBy(300L);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(Report.class))).thenReturn(reviewedReport);

        // Act
        Report result = reportService.markAsReviewed(1L, 300L);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.UNDER_REVIEW, result.getStatus());
        assertEquals(300L, result.getReviewedBy());
        assertNotNull(result.getReviewedAt());

        verify(reportRepository).findById(1L);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testMarkAsReviewed_NotFound() {
        // Arrange
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.markAsReviewed(999L, 300L);
        });

        // Assert
        assertEquals("Error marking report ID: 999 as reviewed by ID: 300 - error: Report not found with id: 999", exception.getMessage());
        verify(reportRepository).findById(999L);
        verify(reportRepository, never()).save(any(Report.class));
    }

    // Test markAsResolved method
    @Test
    void testMarkAsResolved_Success() {
        // Arrange
        Report resolvedReport = new Report();
        resolvedReport.setReportId(1L);
        resolvedReport.setReporterId(100L);
        resolvedReport.setListingId(200L);
        resolvedReport.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        resolvedReport.setDescription("This listing contains inappropriate content");
        resolvedReport.setStatus(ReportStatus.RESOLVED);
        resolvedReport.setReviewedAt(LocalDateTime.now());

        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(Report.class))).thenReturn(resolvedReport);

        // Act
        Report result = reportService.markAsResolved(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.RESOLVED, result.getStatus());
        assertNotNull(result.getReviewedAt());

        verify(reportRepository).findById(1L);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testMarkAsResolved_NotFound() {
        // Arrange
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.markAsResolved(999L);
        });

        // Assert
        assertEquals("Error resolving report ID: 999 - error: Report not found with id: 999", exception.getMessage());
        verify(reportRepository).findById(999L);
        verify(reportRepository, never()).save(any(Report.class));
    }

    // Test markAsDismissed method
    @Test
    void testMarkAsDismissed_Success() {
        // Arrange
        Report dismissedReport = new Report();
        dismissedReport.setReportId(1L);
        dismissedReport.setReporterId(100L);
        dismissedReport.setListingId(200L);
        dismissedReport.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        dismissedReport.setDescription("This listing contains inappropriate content");
        dismissedReport.setStatus(ReportStatus.DISMISSED);
        dismissedReport.setReviewedAt(LocalDateTime.now());

        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(Report.class))).thenReturn(dismissedReport);

        // Act
        Report result = reportService.markAsDismissed(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.DISMISSED, result.getStatus());
        assertNotNull(result.getReviewedAt());

        verify(reportRepository).findById(1L);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testMarkAsDismissed_NotFound() {
        // Arrange
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.markAsDismissed(999L);
        });

        // Assert
        assertEquals("Error dismissing report ID: 999 - error: Report not found with id: 999", exception.getMessage());
        verify(reportRepository).findById(999L);
        verify(reportRepository, never()).save(any(Report.class));
    }

    // Test deleteReport method
    @Test
    void testDeleteReport_Success() {
        // Arrange
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        doNothing().when(reportRepository).delete(testReport);

        // Act
        reportService.deleteReport(1L);

        // Assert
        verify(reportRepository).findById(1L);
        verify(reportRepository).delete(testReport);
    }

    @Test
    void testDeleteReport_NotFound() {
        // Arrange
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.deleteReport(999L);
        });

        // Assert
        assertEquals("Error deleting report ID: 999 - error: Report not found with id: 999", exception.getMessage());
        verify(reportRepository).findById(999L);
        verify(reportRepository, never()).delete(any(Report.class));
    }

    // Test countReportsByStatus method
    @Test
    void testCountReportsByStatus_Success() {
        // Arrange
        Long expectedCount = 5L;
        when(reportRepository.countByStatus(ReportStatus.PENDING))
            .thenReturn(expectedCount);

        // Act
        Long result = reportService.countReportsByStatus(ReportStatus.PENDING);

        // Assert
        assertEquals(expectedCount, result);
        verify(reportRepository).countByStatus(ReportStatus.PENDING);
    }

    @Test
    void testCountReportsByStatus_ZeroCount() {
        // Arrange
        when(reportRepository.countByStatus(ReportStatus.RESOLVED))
            .thenReturn(0L);

        // Act
        Long result = reportService.countReportsByStatus(ReportStatus.RESOLVED);

        // Assert
        assertEquals(0L, result);
        verify(reportRepository).countByStatus(ReportStatus.RESOLVED);
    }

    // Test countReportsByReporterId method
    @Test
    void testCountReportsByReporterId_Success() {
        // Arrange
        Long expectedCount = 3L;
        when(reportRepository.countByReporterId(100L))
            .thenReturn(expectedCount);

        // Act
        Long result = reportService.countReportsByReporterId(100L);

        // Assert
        assertEquals(expectedCount, result);
        verify(reportRepository).countByReporterId(100L);
    }

    @Test
    void testCountReportsByReporterId_ZeroCount() {
        // Arrange
        when(reportRepository.countByReporterId(999L))
            .thenReturn(0L);

        // Act
        Long result = reportService.countReportsByReporterId(999L);

        // Assert
        assertEquals(0L, result);
        verify(reportRepository).countByReporterId(999L);
    }

    // Edge cases and additional scenarios
    @Test
    void testUpdateReport_WithNullDescription() {
        // Arrange
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);

        // Act
        Report result = reportService.updateReport(
            1L,
            ReportType.SPAM,
            null
        );

        // Assert
        assertNotNull(result);
        verify(reportRepository).findById(1L);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testCreateReport_WithEmptyDescription() {
        // Arrange
        testReport.setDescription("");
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);

        // Act
        Report result = reportService.createReport(
            100L,
            200L,
            ReportType.INAPPROPRIATE_CONTENT,
            ""
        );

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getReporterId());
        assertEquals(200L, result.getListingId());
        assertEquals(ReportType.INAPPROPRIATE_CONTENT, result.getReportType());
        assertEquals("", result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());

        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testGetReportsByStatus_WithAllStatusTypes() {
        // Test all possible status types
        ReportStatus[] statuses = {ReportStatus.PENDING, ReportStatus.UNDER_REVIEW, ReportStatus.RESOLVED, ReportStatus.DISMISSED};
        
        for (ReportStatus status : statuses) {
            // Arrange
            List<Report> reports = Arrays.asList(testReport);
            Page<Report> expectedPage = new PageImpl<>(reports);
            when(reportRepository.findByStatus(status, testPageable))
                .thenReturn(Optional.of(expectedPage));

            // Act
            Page<Report> result = reportService.getReportsByStatus(status, testPageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(reportRepository).findByStatus(status, testPageable);
        }
    }

    @Test
    void testGetReportsByReportType_WithAllReportTypes() {
        // Test all possible report types
        ReportType[] types = {ReportType.SPAM, ReportType.INAPPROPRIATE_CONTENT, ReportType.FAKE_LISTING,
                ReportType.HARASSMENT, ReportType.COPYRIGHT_VIOLATION, ReportType.PRICE_MANIPULATION, ReportType.OTHER};
        
        for (ReportType type : types) {
            // Arrange
            List<Report> reports = Arrays.asList(testReport);
            Page<Report> expectedPage = new PageImpl<>(reports);
            when(reportRepository.findByReportType(type, testPageable))
                .thenReturn(Optional.of(expectedPage));

            // Act
            Page<Report> result = reportService.getReportsByReportType(type, testPageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(reportRepository).findByReportType(type, testPageable);
        }
    }

    @Test
    void testMarkAsReviewed_WithAlreadyReviewedReport() {
        // Arrange - Report that is already under review
        Report alreadyReviewedReport = new Report();
        alreadyReviewedReport.setReportId(1L);
        alreadyReviewedReport.setReporterId(100L);
        alreadyReviewedReport.setListingId(200L);
        alreadyReviewedReport.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        alreadyReviewedReport.setDescription("This listing contains inappropriate content");
        alreadyReviewedReport.setStatus(ReportStatus.UNDER_REVIEW);
        alreadyReviewedReport.setReviewedAt(LocalDateTime.now().minusHours(1));
        alreadyReviewedReport.setReviewedBy(300L);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(alreadyReviewedReport));
        when(reportRepository.save(any(Report.class))).thenReturn(alreadyReviewedReport);

        // Act
        Report result = reportService.markAsReviewed(1L, 400L);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.UNDER_REVIEW, result.getStatus());
        assertEquals(400L, result.getReviewedBy()); // Should update to new reviewer
        assertNotNull(result.getReviewedAt()); // Should update to new review time

        verify(reportRepository).findById(1L);
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testMarkAsResolved_WithAlreadyResolvedReport() {
        // Arrange - Report that is already resolved
        Report alreadyResolvedReport = new Report();
        alreadyResolvedReport.setReportId(1L);
        alreadyResolvedReport.setReporterId(100L);
        alreadyResolvedReport.setListingId(200L);
        alreadyResolvedReport.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        alreadyResolvedReport.setDescription("This listing contains inappropriate content");
        alreadyResolvedReport.setStatus(ReportStatus.RESOLVED);
        alreadyResolvedReport.setReviewedAt(LocalDateTime.now().minusHours(1));

        when(reportRepository.findById(1L)).thenReturn(Optional.of(alreadyResolvedReport));
        when(reportRepository.save(any(Report.class))).thenReturn(alreadyResolvedReport);

        // Act
        Report result = reportService.markAsResolved(1L);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.RESOLVED, result.getStatus());
        assertNotNull(result.getReviewedAt()); // Should remain the same

        verify(reportRepository).findById(1L);
        verify(reportRepository).save(any(Report.class));
    }

    // Test searchReports method
    @Test
    void testSearchReports_Success_WithAllFilters() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findWithFilters(ReportStatus.PENDING, ReportType.INAPPROPRIATE_CONTENT, 100L, 200L, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.searchReports(
            ReportStatus.PENDING,
            100L,
            200L,
            ReportType.INAPPROPRIATE_CONTENT,
            300L,
            testPageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findWithFilters(ReportStatus.PENDING, ReportType.INAPPROPRIATE_CONTENT, 100L, 200L, testPageable);
    }

    @Test
    void testSearchReports_Success_WithOnlyStatus() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findWithFilters(ReportStatus.PENDING, null, null, null, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.searchReports(
            ReportStatus.PENDING,
            null,
            null,
            null,
            null,
            testPageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findWithFilters(ReportStatus.PENDING, null, null, null, testPageable);
    }

    @Test
    void testSearchReports_Success_WithStatusAndReportType() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findWithFilters(ReportStatus.UNDER_REVIEW, ReportType.SPAM, null, null, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.searchReports(
            ReportStatus.UNDER_REVIEW,
            null,
            null,
            ReportType.SPAM,
            null,
            testPageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findWithFilters(ReportStatus.UNDER_REVIEW, ReportType.SPAM, null, null, testPageable);
    }

    @Test
    void testSearchReports_Success_WithStatusAndReporterId() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findWithFilters(ReportStatus.RESOLVED, null, 150L, null, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.searchReports(
            ReportStatus.RESOLVED,
            150L,
            null,
            null,
            null,
            testPageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findWithFilters(ReportStatus.RESOLVED, null, 150L, null, testPageable);
    }

    @Test
    void testSearchReports_Success_WithStatusAndListingId() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findWithFilters(ReportStatus.DISMISSED, null, null, 250L, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.searchReports(
            ReportStatus.DISMISSED,
            null,
            250L,
            null,
            null,
            testPageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findWithFilters(ReportStatus.DISMISSED, null, null, 250L, testPageable);
    }

    @Test
    void testSearchReports_Success_WithMultipleFilters() {
        // Arrange
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findWithFilters(ReportStatus.PENDING, ReportType.FAKE_LISTING, 100L, null, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.searchReports(
            ReportStatus.PENDING,
            100L,
            null,
            ReportType.FAKE_LISTING,
            null,
            testPageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReport, result.getContent().get(0));
        verify(reportRepository).findWithFilters(ReportStatus.PENDING, ReportType.FAKE_LISTING, 100L, null, testPageable);
    }

    @Test
    void testSearchReports_NoResultsFound() {
        // Arrange
        when(reportRepository.findWithFilters(ReportStatus.RESOLVED, null, null, null, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.searchReports(
                ReportStatus.RESOLVED,
                null,
                null,
                null,
                null,
                testPageable
            );
        });

        // Assert
        assertEquals("No reports found with the specified filters", exception.getMessage());
        verify(reportRepository).findWithFilters(ReportStatus.RESOLVED, null, null, null, testPageable);
    }

    @Test
    void testSearchReports_RepositoryException() {
        // Arrange
        RuntimeException repositoryException = new RuntimeException("Database connection error");
        when(reportRepository.findWithFilters(ReportStatus.PENDING, null, null, null, testPageable))
            .thenThrow(repositoryException);

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.searchReports(
                ReportStatus.PENDING,
                null,
                null,
                null,
                null,
                testPageable
            );
        });

        // Assert
        assertTrue(exception.getMessage().contains("Error searching reports"));
        assertTrue(exception.getMessage().contains("Database connection error"));
        assertTrue(exception.getMessage().contains("status=PENDING, reporterId=null, listingId=null, reportType=null, reviewedBy=null"));
        verify(reportRepository).findWithFilters(ReportStatus.PENDING, null, null, null, testPageable);
    }

    @Test
    void testSearchReports_WithAllReportTypes() {
        // Test with all possible report types
        ReportType[] types = {ReportType.SPAM, ReportType.INAPPROPRIATE_CONTENT, ReportType.FAKE_LISTING,
                ReportType.HARASSMENT, ReportType.COPYRIGHT_VIOLATION, ReportType.PRICE_MANIPULATION, ReportType.OTHER};
        
        for (ReportType type : types) {
            // Arrange
            List<Report> reports = Arrays.asList(testReport);
            Page<Report> expectedPage = new PageImpl<>(reports);
            when(reportRepository.findWithFilters(ReportStatus.PENDING, type, null, null, testPageable))
                .thenReturn(Optional.of(expectedPage));

            // Act
            Page<Report> result = reportService.searchReports(
                ReportStatus.PENDING,
                null,
                null,
                type,
                null,
                testPageable
            );

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(reportRepository).findWithFilters(ReportStatus.PENDING, type, null, null, testPageable);
        }
    }

    @Test
    void testSearchReports_WithAllStatusTypes() {
        // Test with all possible status types
        ReportStatus[] statuses = {ReportStatus.PENDING, ReportStatus.UNDER_REVIEW, ReportStatus.RESOLVED, ReportStatus.DISMISSED};
        
        for (ReportStatus status : statuses) {
            // Arrange
            List<Report> reports = Arrays.asList(testReport);
            Page<Report> expectedPage = new PageImpl<>(reports);
            when(reportRepository.findWithFilters(status, null, null, null, testPageable))
                .thenReturn(Optional.of(expectedPage));

            // Act
            Page<Report> result = reportService.searchReports(
                status,
                null,
                null,
                null,
                null,
                testPageable
            );

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(reportRepository).findWithFilters(status, null, null, null, testPageable);
        }
    }

    @Test
    void testSearchReports_WithLargePageSize() {
        // Arrange
        Pageable largePageable = PageRequest.of(0, 100);
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports);
        when(reportRepository.findWithFilters(ReportStatus.PENDING, null, null, null, largePageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.searchReports(
            ReportStatus.PENDING,
            null,
            null,
            null,
            null,
            largePageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findWithFilters(ReportStatus.PENDING, null, null, null, largePageable);
    }

    @Test
    void testSearchReports_WithSecondPage() {
        // Arrange
        Pageable secondPagePageable = PageRequest.of(1, 10);
        List<Report> reports = Arrays.asList(testReport);
        Page<Report> expectedPage = new PageImpl<>(reports, secondPagePageable, 25); // 25 total elements
        when(reportRepository.findWithFilters(ReportStatus.PENDING, null, null, null, secondPagePageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.searchReports(
            ReportStatus.PENDING,
            null,
            null,
            null,
            null,
            secondPagePageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(25, result.getTotalElements());
        assertEquals(1, result.getNumber()); // Page 1 (0-indexed)
        assertEquals(3, result.getTotalPages()); // 25 elements / 10 per page = 3 pages
        verify(reportRepository).findWithFilters(ReportStatus.PENDING, null, null, null, secondPagePageable);
    }

    @Test
    void testSearchReports_WithEmptyResults() {
        // Arrange
        List<Report> emptyReports = new ArrayList<>();
        Page<Report> expectedPage = new PageImpl<>(emptyReports);
        when(reportRepository.findWithFilters(ReportStatus.PENDING, ReportType.SPAM, 999L, null, testPageable))
            .thenReturn(Optional.of(expectedPage));

        // Act
        Page<Report> result = reportService.searchReports(
            ReportStatus.PENDING,
            999L,
            null,
            ReportType.SPAM,
            null,
            testPageable
        );

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(reportRepository).findWithFilters(ReportStatus.PENDING, ReportType.SPAM, 999L, null, testPageable);
    }

    @Test
    void testSearchReports_WithZeroResultsAndEmptyOptional() {
        // Arrange
        when(reportRepository.findWithFilters(ReportStatus.RESOLVED, ReportType.FAKE_LISTING, 100L, 200L, testPageable))
            .thenReturn(Optional.empty());

        // Act
        ReportException exception = assertThrows(ReportException.class, () -> {
            reportService.searchReports(
                ReportStatus.RESOLVED,
                100L,
                200L,
                ReportType.FAKE_LISTING,
                null,
                testPageable
            );
        });

        // Assert
        assertEquals("No reports found with the specified filters", exception.getMessage());
        verify(reportRepository).findWithFilters(ReportStatus.RESOLVED, ReportType.FAKE_LISTING, 100L, 200L, testPageable);
    }
}
