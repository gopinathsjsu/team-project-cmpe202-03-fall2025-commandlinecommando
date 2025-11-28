package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.UserFavorite;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserFavorite entity
 */
@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UUID> {
    
    /**
     * Find user's favorites with product details
     */
    @Query("SELECT f FROM UserFavorite f JOIN FETCH f.product WHERE f.user = :user ORDER BY f.createdAt DESC")
    Page<UserFavorite> findByUserWithProduct(@Param("user") User user, Pageable pageable);
    
    /**
     * Check if user favorited a product
     */
    boolean existsByUserAndProduct(User user, Product product);
    
    /**
     * Find specific favorite
     */
    Optional<UserFavorite> findByUserAndProduct(User user, Product product);
    
    /**
     * Count user's favorites
     */
    long countByUser(User user);
    
    /**
     * Count favorites for a product
     */
    long countByProduct(Product product);
    
    /**
     * Delete favorite by user and product
     */
    void deleteByUserAndProduct(User user, Product product);
}
