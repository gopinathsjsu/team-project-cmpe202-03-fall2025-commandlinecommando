package com.commandlinecommandos.campusmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.commandlinecommandos.campusmarketplace.model.Report;
import com.commandlinecommandos.campusmarketplace.model.ReportStatus;
import com.commandlinecommandos.campusmarketplace.model.ReportType;

/**
 * Repository for Report entity
 * Consolidated from listing-api
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    Page<Report> findByReporterId(Long reporterId, Pageable pageable);

    Page<Report> findByListingId(Long listingId, Pageable pageable);

    Page<Report> findByReportType(ReportType reportType, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.createdAt DESC")
    Page<Report> findByStatusOrderByCreatedAtDesc(@Param("status") ReportStatus status, Pageable pageable);

    Page<Report> findByReviewedBy(Long reviewedBy, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    Page<Report> findPendingReportsOrderByCreatedAtAsc(Pageable pageable);

    @Query("SELECT r FROM Report r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:reportType IS NULL OR r.reportType = :reportType) AND " +
           "(:reporterId IS NULL OR r.reporterId = :reporterId) AND " +
           "(:listingId IS NULL OR r.listingId = :listingId)")
    Page<Report> findWithFilters(
            @Param("status") ReportStatus status,
            @Param("reportType") ReportType reportType,
            @Param("reporterId") Long reporterId,
            @Param("listingId") Long listingId,
            Pageable pageable);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    Long countByStatus(@Param("status") ReportStatus status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.reporterId = :reporterId")
    Long countByReporterId(@Param("reporterId") Long reporterId);
}
