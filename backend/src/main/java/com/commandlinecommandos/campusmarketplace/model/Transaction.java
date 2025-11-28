package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction entity - Payment records
 * Tracks all payment attempts and results
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transactions_order", columnList = "order_id"),
    @Index(name = "idx_transactions_user", columnList = "user_id"),
    @Index(name = "idx_transactions_status", columnList = "status"),
    @Index(name = "idx_transactions_created", columnList = "created_at")
})
public class Transaction {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private UUID transactionId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "payment_method_id")
    private UUID paymentMethodId;
    
    @NotNull
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway; // e.g., "stripe", "paypal"
    
    @Column(name = "gateway_transaction_id", length = 255)
    private String gatewayTransactionId;
    
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Transaction() {
    }
    
    public Transaction(Order order, User user, BigDecimal amount) {
        this.order = order;
        this.user = user;
        this.amount = amount;
        this.status = TransactionStatus.PENDING;
    }
    
    // Business methods
    public void markAsCompleted(String gatewayTransactionId) {
        this.status = TransactionStatus.COMPLETED;
        this.gatewayTransactionId = gatewayTransactionId;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }
    
    public void markAsRefunded(BigDecimal refundAmount) {
        this.status = TransactionStatus.REFUNDED;
        this.refundAmount = refundAmount;
        this.refundedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }
    
    public void setPaymentMethodId(UUID paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public TransactionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
    
    public String getPaymentGateway() {
        return paymentGateway;
    }
    
    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
    
    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }
    
    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }
    
    public String getGatewayResponse() {
        return gatewayResponse;
    }
    
    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }
    
    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
