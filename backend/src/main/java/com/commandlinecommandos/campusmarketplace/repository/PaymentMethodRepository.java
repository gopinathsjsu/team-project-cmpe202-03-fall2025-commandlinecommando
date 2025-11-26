package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.PaymentMethod;
import com.commandlinecommandos.campusmarketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PaymentMethod entity
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    
    /**
     * Find all active payment methods for a user
     */
    List<PaymentMethod> findByUserAndIsActiveOrderByIsDefaultDescCreatedAtDesc(User user, Boolean isActive);
    
    /**
     * Find user's default payment method
     */
    Optional<PaymentMethod> findByUserAndIsDefaultAndIsActive(User user, Boolean isDefault, Boolean isActive);
    
    /**
     * Find all payment methods for a user (including inactive)
     */
    List<PaymentMethod> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Count active payment methods for user
     */
    long countByUserAndIsActive(User user, Boolean isActive);
}
