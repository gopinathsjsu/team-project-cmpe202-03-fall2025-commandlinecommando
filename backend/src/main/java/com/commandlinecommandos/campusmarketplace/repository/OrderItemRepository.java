package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.OrderItem;
import com.commandlinecommandos.campusmarketplace.model.OrderStatus;
import com.commandlinecommandos.campusmarketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository for OrderItem entity
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    
    /**
     * Find order items by seller and fulfillment status
     */
    List<OrderItem> findBySellerAndFulfillmentStatus(User seller, OrderStatus status);
    
    /**
     * Find all order items by seller
     */
    List<OrderItem> findBySeller(User seller);
    
    /**
     * Count total sales by seller
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.seller = :seller " +
           "AND oi.fulfillmentStatus IN ('COMPLETED', 'DELIVERED')")
    long countSalesBySeller(@Param("seller") User seller);
    
    /**
     * Calculate total revenue by seller
     */
    @Query("SELECT COALESCE(SUM(oi.totalPrice), 0) FROM OrderItem oi WHERE oi.seller = :seller " +
           "AND oi.fulfillmentStatus IN ('COMPLETED', 'DELIVERED')")
    BigDecimal calculateRevenueBySeller(@Param("seller") User seller);
}

