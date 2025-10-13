package com.commandlinecommandos.listingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.commandlinecommandos.listingapi.model.Report;
import com.commandlinecommandos.listingapi.model.ReportStatus;
import com.commandlinecommandos.listingapi.model.ReportType;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Page<Report>> findByStatus(ReportStatus status, Pageable pageable);

    Optional<Page<Report>> findByReporterId(Long reporterId, Pageable pageable);

    Optional<Page<Report>> findByListingId(Long listingId, Pageable pageable);

    Optional<Page<Report>> findByReportType(ReportType reportType, Pageable pageable);

    // Find reports by status ordered by creation date (newest first)
    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.createdAt DESC")
    Optional<Page<Report>> findByStatusOrderByCreatedAtDesc(@Param("status") ReportStatus status, Pageable pageable);

    Optional<Page<Report>> findByReviewedBy(Long reviewedBy, Pageable pageable);

    // Find reports that need review (PENDING status)
    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    Optional<Page<Report>> findPendingReportsOrderByCreatedAtAsc(Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.status = :status AND " +
           "(:reportType IS NULL OR r.reportType = :reportType) AND " +
           "(:reporterId IS NULL OR r.reporterId = :reporterId) AND " +
           "(:listingId IS NULL OR r.listingId = :listingId)")
    Optional<Page<Report>> findWithFilters(ReportStatus status, ReportType reportType, Long reporterId, Long listingId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    Long countByStatus(@Param("status") ReportStatus status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.reporterId = :reporterId")
    Long countByReporterId(@Param("reporterId") Long reporterId);

}
