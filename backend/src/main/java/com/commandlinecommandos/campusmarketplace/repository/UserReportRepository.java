package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.UserReport;
import com.commandlinecommandos.campusmarketplace.model.ModerationStatus;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for UserReport entity
 */
@Repository
public interface UserReportRepository extends JpaRepository<UserReport, UUID> {
    
    /**
     * Find reports by status
     */
    Page<UserReport> findByStatusOrderByCreatedAtDesc(ModerationStatus status, Pageable pageable);
    
    /**
     * Find pending reports
     */
    Page<UserReport> findByStatusOrderByPriorityDescCreatedAtDesc(ModerationStatus status, Pageable pageable);
    
    /**
     * Find reports by reporter
     */
    Page<UserReport> findByReporterOrderByCreatedAtDesc(User reporter, Pageable pageable);
    
    /**
     * Find reports about a specific product
     */
    List<UserReport> findByReportedProductOrderByCreatedAtDesc(Product product);
    
    /**
     * Find reports about a specific user
     */
    List<UserReport> findByReportedUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find reports by type
     */
    Page<UserReport> findByReportTypeOrderByCreatedAtDesc(String reportType, Pageable pageable);
    
    /**
     * Count pending reports
     */
    long countByStatus(ModerationStatus status);
    
    /**
     * Count reports by priority
     */
    @Query("SELECT COUNT(r) FROM UserReport r WHERE r.status = :status AND r.priority = :priority")
    long countByStatusAndPriority(@Param("status") ModerationStatus status, @Param("priority") String priority);
    
    /**
     * Find reports reviewed by admin
     */
    Page<UserReport> findByReviewedByOrderByReviewedAtDesc(User admin, Pageable pageable);
}
