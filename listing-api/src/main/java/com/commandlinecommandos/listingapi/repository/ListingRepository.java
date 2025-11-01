package com.commandlinecommandos.listingapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commandlinecommandos.listingapi.model.Listing;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.commandlinecommandos.listingapi.model.*;
import java.math.BigDecimal;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    Optional<Page<Listing>> findBySellerId(Long sellerId, Pageable pageable);

    Optional<Page<Listing>> findByCategory(Category category, Pageable pageable);

    Optional<Page<Listing>> findByStatus(ListingStatus status, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.status = :status ORDER BY l.createdAt DESC")
    Optional<Page<Listing>> findByStatusOrderByCreatedAtDesc(@Param("status") ListingStatus status, Pageable pageable);

    Optional<Page<Listing>> findBySellerIdAndStatus(Long sellerId, ListingStatus status, Pageable pageable);
    
    @Query("SELECT l FROM Listing l WHERE l.title LIKE %:keyword% OR l.description LIKE %:keyword%")
    Optional<Page<Listing>> findByTitleOrDescriptionContaining(String keyword, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.status = :status AND " +
           "(:keyword IS NULL OR l.title LIKE %:keyword% OR l.description LIKE %:keyword%) AND " +
           "(:category IS NULL OR l.category = :category) AND " +
           "(:condition IS NULL OR l.condition = :condition) AND " +
           "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.price <= :maxPrice) AND " +
           "(:location IS NULL OR l.location = :location)")
    Optional<Page<Listing>> findWithFilters(@Param("status") ListingStatus status, @Param("keyword") String keyword,
            @Param("category") Category category, @Param("condition") ItemCondition condition,
            @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
            @Param("location") String location, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Listing l WHERE l.sellerId = :sellerId AND l.status = :status")
    Long countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") ListingStatus status);
    
}