package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order entity for Campus Marketplace
 * Handles both shopping cart and completed orders
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_buyer", columnList = "buyer_id,status"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_date", columnList = "created_at")
})
public class Order {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID orderId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;
    
    // Order Details
    @Column(name = "order_number", unique = true, length = 50)
    private String orderNumber;  // Human-readable order number (generated on PAID status)
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CART;
    
    // Pricing
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    
    @Column(name = "platform_fee", precision = 10, scale = 2)
    private BigDecimal platformFee = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    // Delivery Information
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method")
    private DeliveryMethod deliveryMethod;
    
    @Column(name = "delivery_address_id")
    private UUID deliveryAddressId;  // Reference to user_addresses
    
    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;
    
    // Tracking
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;
    
    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    // Notes
    @Column(name = "buyer_notes", columnDefinition = "TEXT")
    private String buyerNotes;
    
    @Column(name = "seller_notes", columnDefinition = "TEXT")
    private String sellerNotes;
    
    // Lifecycle Timestamps
    @CreationTimestamp
    @Column(name = "cart_created_at", updatable = false)
    private LocalDateTime cartCreatedAt;
    
    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    // Constructors
    public Order() {
    }
    
    public Order(User buyer, University university) {
        this.buyer = buyer;
        this.university = university;
        this.status = OrderStatus.CART;
    }
    
    // Business methods
    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
        recalculateTotal();
    }
    
    public void removeItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
        recalculateTotal();
    }
    
    public void recalculateTotal() {
        this.subtotal = orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate tax (9% for California)
        this.taxAmount = this.subtotal.multiply(new BigDecimal("0.09"));
        
        // Calculate platform fee (2.5% of subtotal)
        this.platformFee = this.subtotal.multiply(new BigDecimal("0.025"));
        
        this.totalAmount = this.subtotal
            .add(this.taxAmount)
            .add(this.deliveryFee)
            .add(this.platformFee);
    }
    
    public void placeOrder() {
        if (this.status == OrderStatus.CART && !orderItems.isEmpty()) {
            this.status = OrderStatus.PENDING_PAYMENT;
            this.orderedAt = LocalDateTime.now();
        }
    }
    
    public void markAsPaid() {
        if (this.status == OrderStatus.PENDING_PAYMENT) {
            this.status = OrderStatus.PAID;
            this.paidAt = LocalDateTime.now();
            // Generate order number
            this.orderNumber = generateOrderNumber();
        }
    }
    
    public void markAsProcessing() {
        if (this.status == OrderStatus.PAID) {
            this.status = OrderStatus.PROCESSING;
        }
    }
    
    public void markAsShipped(String trackingNumber) {
        if (this.status == OrderStatus.PROCESSING) {
            this.status = OrderStatus.SHIPPED;
            this.shippedAt = LocalDateTime.now();
            this.trackingNumber = trackingNumber;
        }
    }
    
    public void markAsDelivered() {
        if (this.status == OrderStatus.SHIPPED) {
            this.status = OrderStatus.DELIVERED;
            this.deliveredAt = LocalDateTime.now();
            this.actualDeliveryDate = LocalDateTime.now();
        }
    }
    
    public void markAsCompleted() {
        if (this.status == OrderStatus.DELIVERED) {
            this.status = OrderStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }
    
    public void cancel() {
        if (this.status != OrderStatus.COMPLETED && this.status != OrderStatus.REFUNDED) {
            this.status = OrderStatus.CANCELLED;
            this.cancelledAt = LocalDateTime.now();
        }
    }
    
    private String generateOrderNumber() {
        // Format: ORD-YYYYMMDD-XXXXXX
        String datePart = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        String randomPart = String.format("%06d", (int)(Math.random() * 1000000));
        return "ORD-" + datePart + "-" + randomPart;
    }
    
    public boolean isCart() {
        return this.status == OrderStatus.CART;
    }
    
    public boolean isActive() {
        return this.status != OrderStatus.CANCELLED && 
               this.status != OrderStatus.COMPLETED && 
               this.status != OrderStatus.REFUNDED;
    }
    
    public int getItemCount() {
        return orderItems.size();
    }
    
    // Getters and Setters
    public UUID getOrderId() {
        return orderId;
    }
    
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
    
    public User getBuyer() {
        return buyer;
    }
    
    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }
    
    public University getUniversity() {
        return university;
    }
    
    public void setUniversity(University university) {
        this.university = university;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }
    
    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
    
    public BigDecimal getPlatformFee() {
        return platformFee;
    }
    
    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }
    
    public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }
    
    public UUID getDeliveryAddressId() {
        return deliveryAddressId;
    }
    
    public void setDeliveryAddressId(UUID deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }
    
    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }
    
    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }
    
    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
    
    public LocalDateTime getActualDeliveryDate() {
        return actualDeliveryDate;
    }
    
    public void setActualDeliveryDate(LocalDateTime actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }
    
    public String getBuyerNotes() {
        return buyerNotes;
    }
    
    public void setBuyerNotes(String buyerNotes) {
        this.buyerNotes = buyerNotes;
    }
    
    public String getSellerNotes() {
        return sellerNotes;
    }
    
    public void setSellerNotes(String sellerNotes) {
        this.sellerNotes = sellerNotes;
    }
    
    public LocalDateTime getCartCreatedAt() {
        return cartCreatedAt;
    }
    
    public void setCartCreatedAt(LocalDateTime cartCreatedAt) {
        this.cartCreatedAt = cartCreatedAt;
    }
    
    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }
    
    public void setOrderedAt(LocalDateTime orderedAt) {
        this.orderedAt = orderedAt;
    }
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    public LocalDateTime getShippedAt() {
        return shippedAt;
    }
    
    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
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
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}

