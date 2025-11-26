package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.Transaction;
import com.commandlinecommandos.campusmarketplace.model.TransactionStatus;
import com.commandlinecommandos.campusmarketplace.model.Order;
import com.commandlinecommandos.campusmarketplace.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction entity
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    /**
     * Find transactions by order
     */
    List<Transaction> findByOrderOrderByCreatedAtDesc(Order order);
    
    /**
     * Find transactions by user
     */
    Page<Transaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Find transactions by status
     */
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
    
    /**
     * Find transaction by gateway transaction ID
     */
    Optional<Transaction> findByGatewayTransactionId(String gatewayTransactionId);
    
    /**
     * Find completed transactions by order
     */
    Optional<Transaction> findByOrderAndStatus(Order order, TransactionStatus status);
    
    /**
     * Count transactions by user and status
     */
    long countByUserAndStatus(User user, TransactionStatus status);
}
