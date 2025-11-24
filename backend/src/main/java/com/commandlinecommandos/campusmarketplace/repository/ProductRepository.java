package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.Product;
import com.commandlinecommandos.campusmarketplace.model.ProductCategory;
import com.commandlinecommandos.campusmarketplace.model.ModerationStatus;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.University;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Product entity
 * Supports marketplace search and filtering
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
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
           "AND p.moderationStatus = 'APPROVED' " +
           "ORDER BY p.viewCount DESC")
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
}

