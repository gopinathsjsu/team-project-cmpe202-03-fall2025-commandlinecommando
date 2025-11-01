package com.commandlinecommandos.campusmarketplace.model;

/**
 * Order lifecycle states for Campus Marketplace
 * Matches PostgreSQL enum: order_status
 */
public enum OrderStatus {
    CART,              // Shopping cart (not yet ordered)
    PENDING_PAYMENT,   // Order placed, awaiting payment
    PAID,              // Payment confirmed
    PROCESSING,        // Seller processing order
    SHIPPED,           // Order shipped/in transit
    DELIVERED,         // Order delivered
    COMPLETED,         // Transaction completed
    CANCELLED,         // Order cancelled
    REFUNDED           // Payment refunded
}

