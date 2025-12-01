package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.exception.ResourceNotFoundException;
import com.commandlinecommandos.campusmarketplace.exception.BadRequestException;
import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.TransactionRepository;
import com.commandlinecommandos.campusmarketplace.repository.PaymentMethodRepository;
import com.commandlinecommandos.campusmarketplace.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service for Payment processing
 * Handles payment methods and transaction processing
 */
@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Add payment method
     */
    public PaymentMethod addPaymentMethod(User user, PaymentMethodType methodType,
                                         String paymentToken, String lastFour, String cardBrand,
                                         Integer expiryMonth, Integer expiryYear,
                                         UUID billingAddressId) {
        PaymentMethod paymentMethod = new PaymentMethod(user, methodType);
        paymentMethod.setPaymentToken(paymentToken);
        paymentMethod.setLastFour(lastFour);
        paymentMethod.setCardBrand(cardBrand);
        paymentMethod.setExpiryMonth(expiryMonth);
        paymentMethod.setExpiryYear(expiryYear);
        paymentMethod.setBillingAddressId(billingAddressId);
        
        // If this is the first payment method, make it default
        long count = paymentMethodRepository.countByUserAndIsActive(user, true);
        if (count == 0) {
            paymentMethod.setIsDefault(true);
        }
        
        return paymentMethodRepository.save(paymentMethod);
    }
    
    /**
     * Get user's payment methods
     */
    public List<PaymentMethod> getPaymentMethods(User user) {
        return paymentMethodRepository.findByUserAndIsActiveOrderByIsDefaultDescCreatedAtDesc(user, true);
    }
    
    /**
     * Get payment method by ID
     */
    public PaymentMethod getPaymentMethod(UUID paymentMethodId, User user) {
        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        
        if (!method.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Not your payment method");
        }
        
        return method;
    }
    
    /**
     * Set default payment method
     */
    public PaymentMethod setDefaultPaymentMethod(UUID paymentMethodId, User user) {
        PaymentMethod method = getPaymentMethod(paymentMethodId, user);
        
        // Remove default flag from other methods
        List<PaymentMethod> methods = paymentMethodRepository.findByUserOrderByCreatedAtDesc(user);
        methods.forEach(m -> m.setIsDefault(false));
        paymentMethodRepository.saveAll(methods);
        
        // Set new default
        method.setIsDefault(true);
        return paymentMethodRepository.save(method);
    }
    
    /**
     * Delete payment method
     */
    public void deletePaymentMethod(UUID paymentMethodId, User user) {
        PaymentMethod method = getPaymentMethod(paymentMethodId, user);
        method.setIsActive(false);
        paymentMethodRepository.save(method);
    }
    
    /**
     * Process payment for order (MOCK IMPLEMENTATION)
     * In production, integrate with Stripe, PayPal, etc.
     */
    public Transaction processPayment(UUID orderId, UUID paymentMethodId, User user) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (!order.getBuyer().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Not your order");
        }
        
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Order is not pending payment");
        }
        
        PaymentMethod paymentMethod = getPaymentMethod(paymentMethodId, user);
        
        if (paymentMethod.isExpired()) {
            throw new BadRequestException("Payment method is expired");
        }
        
        // Create transaction record
        Transaction transaction = new Transaction(order, user, order.getTotalAmount());
        transaction.setPaymentMethodId(paymentMethodId);
        transaction.setPaymentGateway("MOCK"); // In production: "stripe", "paypal", etc.
        
        try {
            // MOCK PAYMENT PROCESSING
            // In production, call payment gateway API here
            String mockGatewayId = "TXN-" + UUID.randomUUID().toString().substring(0, 8);
            transaction.markAsCompleted(mockGatewayId);
            transaction.setGatewayResponse("MOCK: Payment successful");
            
            // Mark order as paid
            orderService.markAsPaid(orderId);
            
        } catch (Exception e) {
            transaction.markAsFailed(e.getMessage());
            throw new BadRequestException("Payment failed: " + e.getMessage());
        }
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Process refund (MOCK IMPLEMENTATION)
     */
    public Transaction processRefund(UUID orderId, BigDecimal refundAmount, User admin) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Find completed transaction for this order
        Transaction originalTransaction = transactionRepository.findByOrderAndStatus(order, TransactionStatus.COMPLETED)
            .orElseThrow(() -> new ResourceNotFoundException("No completed transaction found for this order"));
        
        if (refundAmount.compareTo(originalTransaction.getAmount()) > 0) {
            throw new BadRequestException("Refund amount exceeds original payment");
        }
        
        // Create refund transaction
        Transaction refundTransaction = new Transaction(order, order.getBuyer(), refundAmount.negate());
        refundTransaction.setPaymentMethodId(originalTransaction.getPaymentMethodId());
        refundTransaction.setPaymentGateway(originalTransaction.getPaymentGateway());
        
        try {
            // MOCK REFUND PROCESSING
            String mockRefundId = "RFD-" + UUID.randomUUID().toString().substring(0, 8);
            refundTransaction.markAsCompleted(mockRefundId);
            refundTransaction.setGatewayResponse("MOCK: Refund successful");
            
            // Mark original transaction as refunded
            originalTransaction.markAsRefunded(refundAmount);
            transactionRepository.save(originalTransaction);
            
            // Update order status
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
            
        } catch (Exception e) {
            refundTransaction.markAsFailed(e.getMessage());
            throw new BadRequestException("Refund failed: " + e.getMessage());
        }
        
        return transactionRepository.save(refundTransaction);
    }
    
    /**
     * Get transaction history for user
     */
    public Page<Transaction> getTransactionHistory(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * Get transactions for order
     */
    public List<Transaction> getOrderTransactions(UUID orderId, User user) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (!order.getBuyer().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Not your order");
        }
        
        return transactionRepository.findByOrderOrderByCreatedAtDesc(order);
    }
    
    /**
     * Get transaction by ID
     */
    public Transaction getTransaction(UUID transactionId, User user) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        
        if (!transaction.getUser().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Not your transaction");
        }
        
        return transaction;
    }
}
