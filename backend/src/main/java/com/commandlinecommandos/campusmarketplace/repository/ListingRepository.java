package com.commandlinecommandos.campusmarketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.commandlinecommandos.campusmarketplace.model.*;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Listing entity
 * Consolidated from listing-api
 */
@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    Page<Listing> findBySellerId(UUID sellerId, Pageable pageable);

    Page<Listing> findByCategory(Category category, Pageable pageable);

    Page<Listing> findByStatus(ListingStatus status, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.status = :status ORDER BY l.createdAt DESC")
    Page<Listing> findByStatusOrderByCreatedAtDesc(@Param("status") ListingStatus status, Pageable pageable);

    Page<Listing> findBySellerIdAndStatus(UUID sellerId, ListingStatus status, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.title LIKE %:keyword% OR l.description LIKE %:keyword%")
    Page<Listing> findByTitleOrDescriptionContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.status = :status AND " +
           "(:keyword IS NULL OR l.title LIKE %:keyword% OR l.description LIKE %:keyword%) AND " +
           "(:category IS NULL OR l.category = :category) AND " +
           "(:condition IS NULL OR l.condition = :condition) AND " +
           "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.price <= :maxPrice) AND " +
           "(:location IS NULL OR l.location = :location)")
    Page<Listing> findWithFilters(
            @Param("status") ListingStatus status,
            @Param("keyword") String keyword,
            @Param("category") Category category,
            @Param("condition") ItemCondition condition,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("location") String location,
            Pageable pageable);

    @Query("SELECT COUNT(l) FROM Listing l WHERE l.sellerId = :sellerId AND l.status = :status")
    Long countBySellerIdAndStatus(@Param("sellerId") UUID sellerId, @Param("status") ListingStatus status);

    @Query("SELECT l FROM Listing l WHERE l.sellerId = :sellerId ORDER BY l.createdAt DESC")
    java.util.List<Listing> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") UUID sellerId);
}
