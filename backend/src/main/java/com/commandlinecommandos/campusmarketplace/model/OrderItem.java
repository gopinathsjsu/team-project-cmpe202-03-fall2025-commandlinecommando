package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OrderItem entity - Line items in an order
 * Snapshots product data at time of purchase for historical accuracy
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order", columnList = "order_id"),
    @Index(name = "idx_order_items_seller", columnList = "seller_id,fulfillment_status"),
    @Index(name = "idx_order_items_product", columnList = "product_id")
})
public class OrderItem {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "order_item_id", updatable = false, nullable = false)
    private UUID orderItemId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    // Snapshot product details at time of order
    @NotNull
    @Column(name = "product_title", nullable = false)
    private String productTitle;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition", nullable = false)
    private ProductCondition productCondition;
    
    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @NotNull
    @Column(nullable = false)
    private Integer quantity = 1;
    
    @NotNull
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    // Fulfillment
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status")
    private OrderStatus fulfillmentStatus = OrderStatus.PENDING_PAYMENT;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public OrderItem() {
    }
    
    public OrderItem(Order order, Product product, Integer quantity) {
        this.order = order;
        this.product = product;
        this.seller = product.getSeller();
        this.quantity = quantity;
        
        // Snapshot product data
        this.productTitle = product.getTitle();
        this.productCondition = product.getCondition();
        this.unitPrice = product.getPrice();
        this.totalPrice = product.getPrice().multiply(new BigDecimal(quantity));
    }
    
    // Business methods
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
        this.totalPrice = this.unitPrice.multiply(new BigDecimal(newQuantity));
    }
    
    public void markAsShipped() {
        this.fulfillmentStatus = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }
    
    public void markAsDelivered() {
        this.fulfillmentStatus = OrderStatus.DELIVERED;
    }
    
    public void markAsCompleted() {
        this.fulfillmentStatus = OrderStatus.COMPLETED;
    }
    
    // Getters and Setters
    public UUID getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(UUID orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public User getSeller() {
        return seller;
    }
    
    public void setSeller(User seller) {
        this.seller = seller;
    }
    
    public String getProductTitle() {
        return productTitle;
    }
    
    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }
    
    public ProductCondition getProductCondition() {
        return productCondition;
    }
    
    public void setProductCondition(ProductCondition productCondition) {
        this.productCondition = productCondition;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public OrderStatus getFulfillmentStatus() {
        return fulfillmentStatus;
    }
    
    public void setFulfillmentStatus(OrderStatus fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }
    
    public LocalDateTime getShippedAt() {
        return shippedAt;
    }
    
    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
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

