package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.Product;
import com.commandlinecommandos.campusmarketplace.model.ProductCategory;
import com.commandlinecommandos.campusmarketplace.model.ModerationStatus;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.University;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Product entity
 * Supports marketplace search and filtering with full-text search capabilities
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    
    /**
     * Find all active products for a university
     */
    Page<Product> findByUniversityAndIsActiveTrue(University university, Pageable pageable);
    
    /**
     * Find products by category for a university
     */
    Page<Product> findByUniversityAndCategoryAndIsActiveTrueAndModerationStatus(
        University university, 
        ProductCategory category, 
        ModerationStatus moderationStatus, 
        Pageable pageable
    );
    
    /**
     * Find products by seller
     */
    Page<Product> findBySeller(User seller, Pageable pageable);
    
    /**
     * Find active products by seller
     */
    List<Product> findBySellerAndIsActiveTrue(User seller);
    
    /**
     * Find products by price range
     */
    @Query("SELECT p FROM Product p WHERE p.university = :university " +
           "AND p.isActive = true " +
           "AND p.moderationStatus = 'APPROVED' " +
           "AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(
        @Param("university") University university,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );
    
    /**
     * Search products by title or description (full-text search)
     */
    @Query("SELECT p FROM Product p WHERE p.university = :university " +
           "AND p.isActive = true " +
           "AND p.moderationStatus = 'APPROVED' " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProducts(
        @Param("university") University university,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
    
    /**
     * Find featured products
     */
    List<Product> findByUniversityAndIsFeaturedTrueAndIsActiveTrueAndModerationStatus(
        University university,
        ModerationStatus moderationStatus
    );
    
    /**
     * Find products pending moderation
     */
    List<Product> findByModerationStatus(ModerationStatus status);
    
    /**
     * Count active products by seller
     */
    long countBySellerAndIsActiveTrue(User seller);
    
    /**
     * Full-text search with PostgreSQL ts_rank (falls back gracefully for H2)
     */
    @Query(value = "SELECT p.* FROM listings p " +
           "WHERE p.university_id = :universityId " +
           "AND p.is_active = true " +
           "AND p.moderation_status = 'APPROVED' " +
           "AND (to_tsvector('english', p.title || ' ' || p.description) @@ plainto_tsquery('english', :query)) " +
           "ORDER BY ts_rank(to_tsvector('english', p.title || ' ' || p.description), plainto_tsquery('english', :query)) DESC",
           nativeQuery = true)
    Page<Product> searchWithFullText(
        @Param("universityId") UUID universityId,
        @Param("query") String query,
        Pageable pageable
    );
    
    /**
     * Fuzzy search using PostgreSQL similarity() function
     */
    @Query(value = "SELECT p.* FROM listings p " +
           "WHERE p.university_id = :universityId " +
           "AND p.is_active = true " +
           "AND p.moderation_status = 'APPROVED' " +
           "AND (similarity(p.title, :query) > 0.3 OR similarity(p.description, :query) > 0.2) " +
           "ORDER BY similarity(p.title, :query) DESC",
           nativeQuery = true)
    Page<Product> fuzzySearch(
        @Param("universityId") UUID universityId,
        @Param("query") String query,
        Pageable pageable
    );
    
    /**
     * Find title suggestions for autocomplete using PostgreSQL similarity()
     */
    @Query(value = "SELECT DISTINCT p.title FROM listings p " +
           "WHERE p.university_id = :universityId " +
           "AND p.is_active = true " +
           "AND similarity(p.title, :query) > 0.3 " +
           "ORDER BY similarity(p.title, :query) DESC " +
           "LIMIT 10",
           nativeQuery = true)
    List<String> findTitleSuggestions(
        @Param("universityId") UUID universityId,
        @Param("query") String query
    );
    
    /**
     * Find title suggestions using LIKE (H2 fallback)
     */
    @Query("SELECT DISTINCT p.title FROM Product p " +
           "WHERE p.university.universityId = :universityId " +
           "AND p.isActive = true " +
           "AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY p.title")
    List<String> findTitleSuggestionsLike(
        @Param("universityId") UUID universityId,
        @Param("query") String query
    );
    
    /**
     * Find top products by views
     */
    @Query("SELECT p FROM Product p WHERE p.university = :university " +
           "AND p.isActive = true " +
           "ORDER BY COALESCE(p.viewCount, 0) DESC, p.createdAt DESC")
    List<Product> findTopByViews(@Param("university") University university, Pageable pageable);
    
    /**
     * Find all active products with approved status
     */
    Page<Product> findByIsActiveTrueAndModerationStatus(ModerationStatus moderationStatus, Pageable pageable);
    
    /**
     * Find products by category with approved status
     */
    Page<Product> findByCategoryAndIsActiveTrueAndModerationStatus(
        ProductCategory category,
        ModerationStatus moderationStatus,
        Pageable pageable
    );

    /**
     * Find active products by seller ID
     */
    Page<Product> findBySellerUserIdAndIsActiveTrue(UUID sellerId, Pageable pageable);
}

