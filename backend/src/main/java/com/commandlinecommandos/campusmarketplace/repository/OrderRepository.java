package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.Order;
import com.commandlinecommandos.campusmarketplace.model.OrderStatus;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.University;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    /**
     * Find buyer's active cart
     */
    Optional<Order> findByBuyerAndStatus(User buyer, OrderStatus status);
    
    /**
     * Find buyer's order history (excluding cart)
     */
    @Query("SELECT o FROM Order o WHERE o.buyer = :buyer AND o.status != 'CART' ORDER BY o.createdAt DESC")
    Page<Order> findOrderHistory(@Param("buyer") User buyer, Pageable pageable);
    
    /**
     * Find orders by status
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * Find orders by university
     */
    Page<Order> findByUniversity(University university, Pageable pageable);
    
    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Find buyer's orders by status
     */
    Page<Order> findByBuyerAndStatus(User buyer, OrderStatus status, Pageable pageable);
    
    /**
     * Count orders by buyer
     */
    long countByBuyer(User buyer);
    
    /**
     * Count completed orders by buyer
     */
    long countByBuyerAndStatus(User buyer, OrderStatus status);
    
    /**
     * Find orders containing items from a specific seller
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.seller = :seller")
    Page<Order> findOrdersBySeller(@Param("seller") User seller, Pageable pageable);
}

