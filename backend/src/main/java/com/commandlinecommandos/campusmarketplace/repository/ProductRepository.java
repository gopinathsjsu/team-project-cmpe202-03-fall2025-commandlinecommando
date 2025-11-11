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
     * Find top products by views
     */
    @Query("SELECT p FROM Product p WHERE p.university = :university " +
           "AND p.isActive = true " +
           "ORDER BY COALESCE(p.viewCount, 0) DESC, p.createdAt DESC")
    List<Product> findTopByViews(@Param("university") University university, Pageable pageable);
    
    // ========================================================================
    // ENHANCED SEARCH METHODS FOR EPIC 3
    // ========================================================================
    
    /**
     * Full-text search using PostgreSQL ts_rank for relevance scoring
     * Searches title and description using search_vector column
     * 
     * @param universityId University UUID
     * @param query Search query
     * @return List of products ordered by relevance
     */
    @Query(value = "SELECT p.*, ts_rank(p.search_vector, plainto_tsquery('english', :query)) AS rank " +
           "FROM products p " +
           "WHERE p.university_id = :universityId " +
           "AND p.is_active = true " +
           "AND p.moderation_status = 'APPROVED' " +
           "AND p.search_vector @@ plainto_tsquery('english', :query) " +
           "ORDER BY rank DESC, p.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM products p " +
                       "WHERE p.university_id = :universityId " +
                       "AND p.is_active = true " +
                       "AND p.moderation_status = 'APPROVED' " +
                       "AND p.search_vector @@ plainto_tsquery('english', :query)",
           nativeQuery = true)
    Page<Product> searchWithFullText(@Param("universityId") UUID universityId,
                                      @Param("query") String query,
                                      Pageable pageable);
    
    /**
     * Autocomplete suggestions using trigram similarity
     * Returns distinct product titles similar to the query
     * 
     * @param universityId University UUID
     * @param query Search query (minimum 2 characters)
     * @return List of title suggestions
     */
    @Query(value = "SELECT DISTINCT p.title " +
           "FROM products p " +
           "WHERE p.university_id = :universityId " +
           "AND p.is_active = true " +
           "AND p.moderation_status = 'APPROVED' " +
           "AND similarity(p.title, :query) > 0.3 " +
           "ORDER BY similarity(p.title, :query) DESC " +
           "LIMIT 10",
           nativeQuery = true)
    List<String> findTitleSuggestions(@Param("universityId") UUID universityId,
                                       @Param("query") String query);
    
    /**
     * Fuzzy search with typo tolerance using trigram similarity
     * Uses % operator for similarity matching
     * 
     * @param universityId University UUID
     * @param query Search query
     * @param pageable Pagination
     * @return List of products matching with typo tolerance
     */
    @Query(value = "SELECT p.* FROM products p " +
           "WHERE p.university_id = :universityId " +
           "AND p.is_active = true " +
           "AND p.moderation_status = 'APPROVED' " +
           "AND (p.title % :query OR p.description % :query) " +
           "ORDER BY similarity(p.title, :query) DESC, p.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM products p " +
                       "WHERE p.university_id = :universityId " +
                       "AND p.is_active = true " +
                       "AND p.moderation_status = 'APPROVED' " +
                       "AND (p.title % :query OR p.description % :query)",
           nativeQuery = true)
    Page<Product> fuzzySearch(@Param("universityId") UUID universityId,
                               @Param("query") String query,
                               Pageable pageable);
    
    /**
     * Find products created after a certain date
     * Used for "recent" filters (last 24h, 7d, 30d, 90d)
     * 
     * @param university University
     * @param dateFrom Minimum creation date
     * @param pageable Pagination
     * @return Page of products
     */
    @Query("SELECT p FROM Product p WHERE p.university = :university " +
           "AND p.isActive = true " +
           "AND p.moderationStatus = 'APPROVED' " +
           "AND p.createdAt >= :dateFrom " +
           "ORDER BY p.createdAt DESC")
    Page<Product> findRecentProducts(@Param("university") University university,
                                     @Param("dateFrom") LocalDateTime dateFrom,
                                     Pageable pageable);
}

