package com.commandlinecommandos.listingapi.controller;

import com.commandlinecommandos.listingapi.model.*;
import com.commandlinecommandos.listingapi.service.ReportService;
import com.commandlinecommandos.listingapi.exception.ReportException;
import com.commandlinecommandos.listingapi.security.JwtHelper;
import com.commandlinecommandos.listingapi.dto.CreateReportRequest;
import com.commandlinecommandos.listingapi.dto.UpdateReportRequest;
import com.commandlinecommandos.listingapi.dto.ReportCounts;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private JwtHelper jwtHelper;

    @InjectMocks
    private ReportController reportController;

    private Report testReport;
    private Pageable testPageable;
    private Page<Report> testPage;
    private HttpServletRequest mockRequest;
    private HttpServletRequest mockRequestNoAuth;
    private HttpServletRequest mockRequestAdmin;
    private HttpServletRequest mockRequestNonAdmin;

    @BeforeEach
    void setUp() {
        // Create mock HTTP requests
        mockRequest = new MockHttpServletRequest();
        ((MockHttpServletRequest) mockRequest).addHeader("Authorization", "Bearer test-jwt-token");
        
        mockRequestNoAuth = new MockHttpServletRequest();
        
        mockRequestAdmin = new MockHttpServletRequest();
        ((MockHttpServletRequest) mockRequestAdmin).addHeader("Authorization", "Bearer admin-jwt-token");
        
        mockRequestNonAdmin = new MockHttpServletRequest();
        ((MockHttpServletRequest) mockRequestNonAdmin).addHeader("Authorization", "Bearer user-jwt-token");
        
        // Mock JWT helper - use lenient() since not all tests use all mocks
        lenient().when(jwtHelper.extractUserIdFromRequest(mockRequest)).thenReturn(1L);
        lenient().when(jwtHelper.extractUserIdFromRequest(mockRequestNoAuth)).thenReturn(null);
        lenient().when(jwtHelper.extractUserIdFromRequest(mockRequestAdmin)).thenReturn(1L);
        lenient().when(jwtHelper.extractUserIdFromRequest(mockRequestNonAdmin)).thenReturn(2L);
        
        lenient().when(jwtHelper.extractRoleFromRequest(mockRequestAdmin)).thenReturn("ADMIN");
        lenient().when(jwtHelper.extractRoleFromRequest(mockRequestNonAdmin)).thenReturn("STUDENT");
        lenient().when(jwtHelper.extractRoleFromRequest(mockRequestNoAuth)).thenReturn(null);
        lenient().when(jwtHelper.extractRoleFromRequest(mockRequest)).thenReturn("STUDENT");
        // Create test report
        testReport = new Report(
            1L,  // reporterId
            2L,  // listingId
            ReportType.SPAM,
            "Test spam report description"
        );
        testReport.setReportId(1L);
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setStatus(ReportStatus.PENDING);

        // Create test pageable and page
        testPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        testPage = new PageImpl<>(Arrays.asList(testReport));
    }

    // Tests for getAllReports method
    @Test
    void getAllReports_Success_ReturnsOkResponse() {
        // Arrange
        when(reportService.getAllReports(any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.getAllReports(0, 20, "createdAt", "desc", mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(reportService).getAllReports(any(Pageable.class));
    }

    @Test
    void getAllReports_WithCustomParameters_ReturnsOkResponse() {
        // Arrange
        when(reportService.getAllReports(any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.getAllReports(1, 10, "reportId", "asc", mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(reportService).getAllReports(any(Pageable.class));
    }

    @Test
    void getAllReports_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.getAllReports(any(Pageable.class)))
            .thenThrow(new ReportException("No reports found"));

        // Act & Assert
        assertThrows(ReportException.class, () -> {
            reportController.getAllReports(0, 20, "createdAt", "desc", mockRequestAdmin);
        });
        verify(reportService).getAllReports(any(Pageable.class));
    }

    // Tests for searchReports method
    @Test
    void searchReports_WithAllFilters_ReturnsOkResponse() {
        // Arrange
        when(reportService.searchReports(
            any(ReportStatus.class), anyLong(), anyLong(), any(ReportType.class),
            anyLong(), any(Pageable.class)))
            .thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.searchReports(
            ReportStatus.PENDING, 1L, 2L, ReportType.SPAM, 3L,
            0, 20, "createdAt", "desc", mockRequestAdmin
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(reportService).searchReports(
            eq(ReportStatus.PENDING), eq(1L), eq(2L), eq(ReportType.SPAM), eq(3L), any(Pageable.class));
    }

    @Test
    void searchReports_WithNullFilters_ReturnsOkResponse() {
        // Arrange
        when(reportService.searchReports(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.searchReports(
            null, null, null, null, null,
            0, 20, "createdAt", "desc", mockRequestAdmin
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(reportService).searchReports(
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void searchReports_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.searchReports(any(), any(), any(), any(), any(), any(Pageable.class)))
            .thenThrow(new ReportException("No reports found with filters"));

        // Act & Assert
        assertThrows(ReportException.class, () -> {
            reportController.searchReports(
                ReportStatus.PENDING, 1L, 2L, ReportType.SPAM, 3L,
                0, 20, "createdAt", "desc", mockRequestAdmin
            );
        });
    }

    // Tests for getPendingReports method
    @Test
    void getPendingReports_Success_ReturnsOkResponse() {
        // Arrange
        when(reportService.getPendingReports(any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.getPendingReports(0, 20, "createdAt", "asc", mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(reportService).getPendingReports(any(Pageable.class));
    }

    @Test
    void getPendingReports_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.getPendingReports(any(Pageable.class)))
            .thenThrow(new ReportException("No pending reports found"));

        // Act & Assert
        assertThrows(ReportException.class, () -> {
            reportController.getPendingReports(0, 20, "createdAt", "asc", mockRequestAdmin);
        });
    }

    // Tests for getReportsByReporterId method
    @Test
    void getReportsByReporterId_Success_ReturnsOkResponse() {
        // Arrange
        when(reportService.getReportsByReporterId(eq(1L), any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.getReportsByReporterId(1L, 0, 20, "createdAt", "desc", mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(reportService).getReportsByReporterId(eq(1L), any(Pageable.class));
    }

    @Test
    void getReportsByReporterId_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.getReportsByReporterId(eq(999L), any(Pageable.class)))
            .thenThrow(new ReportException("No reports found for reporter with id: 999"));

        // Act & Assert
        assertThrows(ReportException.class, () -> {
            reportController.getReportsByReporterId(999L, 0, 20, "createdAt", "desc", mockRequestAdmin);
        });
    }

    // Tests for getReportsByListingId method
    @Test
    void getReportsByListingId_Success_ReturnsOkResponse() {
        // Arrange
        when(reportService.getReportsByListingId(eq(2L), any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.getReportsByListingId(2L, 0, 20, "createdAt", "desc", mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(reportService).getReportsByListingId(eq(2L), any(Pageable.class));
    }

    @Test
    void getReportsByListingId_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.getReportsByListingId(eq(999L), any(Pageable.class)))
            .thenThrow(new ReportException("No reports found for listing with id: 999"));

        // Act & Assert
        assertThrows(ReportException.class, () -> {
            reportController.getReportsByListingId(999L, 0, 20, "createdAt", "desc", mockRequestAdmin);
        });
    }

    // Tests for getReportsByType method
    @Test
    void getReportsByType_Success_ReturnsOkResponse() {
        // Arrange
        when(reportService.getReportsByReportType(eq(ReportType.SPAM), any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.getReportsByType(ReportType.SPAM, 0, 20, "createdAt", "desc", mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(reportService).getReportsByReportType(eq(ReportType.SPAM), any(Pageable.class));
    }

    @Test
    void getReportsByType_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.getReportsByReportType(eq(ReportType.FAKE_LISTING), any(Pageable.class)))
            .thenThrow(new ReportException("No reports found for type: FAKE_LISTING"));

        // Act & Assert
        assertThrows(ReportException.class, () -> {
            reportController.getReportsByType(ReportType.FAKE_LISTING, 0, 20, "createdAt", "desc", mockRequestAdmin);
        });
    }

    // Tests for getReportsByStatus method
    @Test
    void getReportsByStatus_Success_ReturnsOkResponse() {
        // Arrange
        when(reportService.getReportsByStatus(eq(ReportStatus.PENDING), any(Pageable.class))).thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.getReportsByStatus(ReportStatus.PENDING, 0, 20, "createdAt", "desc", mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(reportService).getReportsByStatus(eq(ReportStatus.PENDING), any(Pageable.class));
    }

    @Test
    void getReportsByStatus_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.getReportsByStatus(eq(ReportStatus.RESOLVED), any(Pageable.class)))
            .thenThrow(new ReportException("No reports found for status: RESOLVED"));

        // Act & Assert
        assertThrows(ReportException.class, () -> {
            reportController.getReportsByStatus(ReportStatus.RESOLVED, 0, 20, "createdAt", "desc", mockRequestAdmin);
        });
    }

    // Tests for getReportById method
    @Test
    void getReportById_Success_ReturnsOkResponse() {
        // Arrange
        when(reportService.getReportById(1L)).thenReturn(testReport);

        // Act
        ResponseEntity<?> response = reportController.getReportById(1L, mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testReport, response.getBody());
        verify(reportService).getReportById(1L);
    }

    @Test
    void getReportById_ReportNotFound_PropagatesException() {
        // Arrange
        when(reportService.getReportById(999L))
            .thenThrow(new ReportException("Report not found with id: 999"));

        // Act & Assert
        assertThrows(ReportException.class, () -> {
            reportController.getReportById(999L, mockRequestAdmin);
        });
        verify(reportService).getReportById(999L);
    }

    // Tests for createReport method
    @Test
    void createReport_Success_ReturnsOkResponse() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setListingId(2L);
        request.setReportType(ReportType.SPAM);
        request.setDescription("Test spam report");

        Report createdReport = new Report(1L, 2L, ReportType.SPAM, "Test spam report");
        createdReport.setReportId(2L);

        when(reportService.createReport(anyLong(), anyLong(), any(ReportType.class), anyString()))
            .thenReturn(createdReport);

        // Act
        ResponseEntity<?> response = reportController.createReport(request, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdReport, response.getBody());
        verify(reportService).createReport(
            eq(1L), eq(2L), eq(ReportType.SPAM), eq("Test spam report"));
    }

    @Test
    void createReport_ServiceThrowsException_PropagatesException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setListingId(2L);
        request.setReportType(ReportType.SPAM);
        request.setDescription("Test spam report");

        when(reportService.createReport(anyLong(), anyLong(), any(ReportType.class), anyString()))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reportController.createReport(request, mockRequest);
        });
    }

    // Tests for updateReport method
    @Test
    void updateReport_Success_ReturnsOkResponse() {
        // Arrange
        UpdateReportRequest request = new UpdateReportRequest();
        request.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        request.setDescription("Updated inappropriate content report");

        Report updatedReport = new Report(testReport.getReporterId(), testReport.getListingId(), 
            ReportType.INAPPROPRIATE_CONTENT, "Updated inappropriate content report");
        updatedReport.setReportId(testReport.getReportId());

        when(reportService.updateReport(eq(1L), any(ReportType.class), anyString()))
            .thenReturn(updatedReport);

        // Act
        ResponseEntity<?> response = reportController.updateReport(1L, request, mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedReport, response.getBody());
        verify(reportService).updateReport(
            eq(1L), eq(ReportType.INAPPROPRIATE_CONTENT), eq("Updated inappropriate content report"));
    }

    @Test
    void updateReport_ServiceThrowsException_PropagatesException() {
        // Arrange
        UpdateReportRequest request = new UpdateReportRequest();
        request.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        request.setDescription("Updated report");

        when(reportService.updateReport(eq(1L), any(ReportType.class), anyString()))
            .thenThrow(new RuntimeException("Update error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reportController.updateReport(1L, request, mockRequestAdmin);
        });
    }

    // Tests for markAsReviewed method
    @Test
    void markAsReviewed_Success_ReturnsOkResponse() {
        // Arrange
        Report reviewedReport = new Report(testReport.getReporterId(), testReport.getListingId(), 
            testReport.getReportType(), testReport.getDescription());
        reviewedReport.setReportId(testReport.getReportId());
        reviewedReport.markAsReviewedBy(1L);

        when(reportService.markAsReviewed(eq(1L), anyLong())).thenReturn(reviewedReport);

        // Act
        ResponseEntity<?> response = reportController.markAsReviewed(1L, mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(reviewedReport, response.getBody());
        assertEquals(ReportStatus.UNDER_REVIEW, ((Report) response.getBody()).getStatus());
        verify(reportService).markAsReviewed(eq(1L), eq(1L));
    }

    @Test
    void markAsReviewed_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.markAsReviewed(eq(1L), anyLong()))
            .thenThrow(new RuntimeException("Mark as reviewed error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reportController.markAsReviewed(1L, mockRequestAdmin);
        });
    }

    // Tests for markAsResolved method
    @Test
    void markAsResolved_Success_ReturnsOkResponse() {
        // Arrange
        Report resolvedReport = new Report(testReport.getReporterId(), testReport.getListingId(), 
            testReport.getReportType(), testReport.getDescription());
        resolvedReport.setReportId(testReport.getReportId());
        resolvedReport.markAsResolved();

        when(reportService.markAsResolved(1L)).thenReturn(resolvedReport);

        // Act
        ResponseEntity<?> response = reportController.markAsResolved(1L, mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(resolvedReport, response.getBody());
        assertEquals(ReportStatus.RESOLVED, ((Report) response.getBody()).getStatus());
        verify(reportService).markAsResolved(1L);
    }

    @Test
    void markAsResolved_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.markAsResolved(1L))
            .thenThrow(new RuntimeException("Mark as resolved error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reportController.markAsResolved(1L, mockRequestAdmin);
        });
    }

    // Tests for markAsDismissed method
    @Test
    void markAsDismissed_Success_ReturnsOkResponse() {
        // Arrange
        Report dismissedReport = new Report(testReport.getReporterId(), testReport.getListingId(), 
            testReport.getReportType(), testReport.getDescription());
        dismissedReport.setReportId(testReport.getReportId());
        dismissedReport.markAsDismissed();

        when(reportService.markAsDismissed(1L)).thenReturn(dismissedReport);

        // Act
        ResponseEntity<?> response = reportController.markAsDismissed(1L, mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(dismissedReport, response.getBody());
        assertEquals(ReportStatus.DISMISSED, ((Report) response.getBody()).getStatus());
        verify(reportService).markAsDismissed(1L);
    }

    @Test
    void markAsDismissed_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.markAsDismissed(1L))
            .thenThrow(new RuntimeException("Mark as dismissed error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reportController.markAsDismissed(1L, mockRequestAdmin);
        });
    }

    // Tests for deleteReport method
    @Test
    void deleteReport_Success_ReturnsOkResponse() {
        // Arrange
        doNothing().when(reportService).deleteReport(1L);

        // Act
        ResponseEntity<String> response = reportController.deleteReport(1L, mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Report deleted successfully", response.getBody());
        verify(reportService).deleteReport(1L);
    }

    @Test
    void deleteReport_ServiceThrowsException_PropagatesException() {
        // Arrange
        doThrow(new RuntimeException("Delete error")).when(reportService).deleteReport(1L);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reportController.deleteReport(1L, mockRequestAdmin);
        });
    }

    // Tests for getReportCounts method
    @Test
    void getReportCounts_Success_ReturnsOkResponse() {
        // Arrange
        when(reportService.countReportsByStatus(ReportStatus.PENDING)).thenReturn(5L);
        when(reportService.countReportsByStatus(ReportStatus.UNDER_REVIEW)).thenReturn(3L);
        when(reportService.countReportsByStatus(ReportStatus.RESOLVED)).thenReturn(10L);
        when(reportService.countReportsByStatus(ReportStatus.DISMISSED)).thenReturn(2L);

        // Act
        ResponseEntity<?> response = reportController.getReportCounts(mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ReportCounts counts = (ReportCounts) response.getBody();
        assertEquals(5L, counts.getPending());
        assertEquals(3L, counts.getUnderReview());
        assertEquals(10L, counts.getResolved());
        assertEquals(2L, counts.getDismissed());
        
        verify(reportService).countReportsByStatus(ReportStatus.PENDING);
        verify(reportService).countReportsByStatus(ReportStatus.UNDER_REVIEW);
        verify(reportService).countReportsByStatus(ReportStatus.RESOLVED);
        verify(reportService).countReportsByStatus(ReportStatus.DISMISSED);
    }

    @Test
    void getReportCounts_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(reportService.countReportsByStatus(any(ReportStatus.class)))
            .thenThrow(new RuntimeException("Count error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            reportController.getReportCounts(mockRequestAdmin);
        });
    }

    // Tests for CreateReportRequest DTO
    @Test
    void createReportRequest_GettersAndSetters_WorkCorrectly() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();

        // Act & Assert
        request.setListingId(2L);
        assertEquals(2L, request.getListingId());

        request.setReportType(ReportType.SPAM);
        assertEquals(ReportType.SPAM, request.getReportType());

        request.setDescription("Test description");
        assertEquals("Test description", request.getDescription());
    }

    // Tests for UpdateReportRequest DTO
    @Test
    void updateReportRequest_GettersAndSetters_WorkCorrectly() {
        // Arrange
        UpdateReportRequest request = new UpdateReportRequest();

        // Act & Assert
        request.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        assertEquals(ReportType.INAPPROPRIATE_CONTENT, request.getReportType());

        request.setDescription("Updated description");
        assertEquals("Updated description", request.getDescription());
    }

    // Tests for ReportCounts DTO
    @Test
    void reportCounts_GettersAndSetters_WorkCorrectly() {
        // Arrange
        ReportCounts counts = new ReportCounts(1L, 2L, 3L, 4L);

        // Act & Assert
        assertEquals(1L, counts.getPending());
        assertEquals(2L, counts.getUnderReview());
        assertEquals(3L, counts.getResolved());
        assertEquals(4L, counts.getDismissed());

        counts.setPending(10L);
        assertEquals(10L, counts.getPending());

        counts.setUnderReview(20L);
        assertEquals(20L, counts.getUnderReview());

        counts.setResolved(30L);
        assertEquals(30L, counts.getResolved());

        counts.setDismissed(40L);
        assertEquals(40L, counts.getDismissed());
    }

    // Edge cases and additional scenarios
    @Test
    void searchReports_WithStatusOnly_ReturnsOkResponse() {
        // Arrange
        when(reportService.searchReports(
            any(ReportStatus.class), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.searchReports(
            ReportStatus.PENDING, null, null, null, null,
            0, 20, "createdAt", "desc", mockRequestAdmin
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reportService).searchReports(
            eq(ReportStatus.PENDING), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void searchReports_WithReporterIdOnly_ReturnsOkResponse() {
        // Arrange
        when(reportService.searchReports(
            isNull(), anyLong(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(testPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.searchReports(
            null, 1L, null, null, null,
            0, 20, "createdAt", "desc", mockRequestAdmin
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reportService).searchReports(
            isNull(), eq(1L), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void createReport_WithNullDescription_HandlesGracefully() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setListingId(2L);
        request.setReportType(ReportType.SPAM);
        request.setDescription(null);

        Report createdReport = new Report(1L, 2L, ReportType.SPAM, null);
        createdReport.setReportId(2L);

        when(reportService.createReport(anyLong(), anyLong(), any(ReportType.class), isNull()))
            .thenReturn(createdReport);

        // Act
        ResponseEntity<?> response = reportController.createReport(request, mockRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(reportService).createReport(eq(1L), eq(2L), eq(ReportType.SPAM), isNull());
    }

    @Test
    void updateReport_WithNullDescription_HandlesGracefully() {
        // Arrange
        UpdateReportRequest request = new UpdateReportRequest();
        request.setReportType(ReportType.INAPPROPRIATE_CONTENT);
        request.setDescription(null);

        Report updatedReport = new Report(testReport.getReporterId(), testReport.getListingId(), 
            ReportType.INAPPROPRIATE_CONTENT, null);
        updatedReport.setReportId(testReport.getReportId());

        when(reportService.updateReport(eq(1L), any(ReportType.class), isNull()))
            .thenReturn(updatedReport);

        // Act
        ResponseEntity<?> response = reportController.updateReport(1L, request, mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(reportService).updateReport(eq(1L), eq(ReportType.INAPPROPRIATE_CONTENT), isNull());
    }

    @Test
    void getAllReports_WithEmptyPage_ReturnsOkResponse() {
        // Arrange
        Page<Report> emptyPage = new PageImpl<>(new ArrayList<>());
        when(reportService.getAllReports(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        ResponseEntity<Page<?>> response = reportController.getAllReports(0, 20, "createdAt", "desc", mockRequestAdmin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
        verify(reportService).getAllReports(any(Pageable.class));
    }
}
