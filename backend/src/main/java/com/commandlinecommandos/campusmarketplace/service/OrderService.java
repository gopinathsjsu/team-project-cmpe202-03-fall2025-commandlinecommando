package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.exception.ResourceNotFoundException;
import com.commandlinecommandos.campusmarketplace.exception.BadRequestException;
import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.OrderRepository;
import com.commandlinecommandos.campusmarketplace.repository.OrderItemRepository;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service for Order management
 * Handles cart, checkout, and order lifecycle
 */
@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Get or create user's shopping cart
     */
    public Order getOrCreateCart(User buyer) {
        return orderRepository.findByBuyerAndStatus(buyer, OrderStatus.CART)
            .orElseGet(() -> {
                Order cart = new Order(buyer, buyer.getUniversity());
                return orderRepository.save(cart);
            });
    }
    
    /**
     * Add product to cart
     */
    public Order addToCart(User buyer, UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (!product.isActive()) {
            throw new BadRequestException("Product is not available");
        }
        
        if (product.getSeller().getUserId().equals(buyer.getUserId())) {
            throw new BadRequestException("Cannot purchase your own product");
        }
        
        Order cart = getOrCreateCart(buyer);
        
        // Check if product already in cart
        OrderItem existingItem = cart.getOrderItems().stream()
            .filter(item -> item.getProduct().getProductId().equals(productId))
            .findFirst()
            .orElse(null);
        
        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + quantity);
        } else {
            OrderItem newItem = new OrderItem(cart, product, quantity);
            cart.addItem(newItem);
        }
        
        return orderRepository.save(cart);
    }
    
    /**
     * Update cart item quantity
     */
    public Order updateCartItemQuantity(User buyer, UUID orderItemId, Integer quantity) {
        Order cart = getOrCreateCart(buyer);
        
        OrderItem item = cart.getOrderItems().stream()
            .filter(oi -> oi.getOrderItemId().equals(orderItemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));
        
        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            item.updateQuantity(quantity);
        }
        
        cart.recalculateTotal();
        return orderRepository.save(cart);
    }
    
    /**
     * Remove item from cart
     */
    public Order removeFromCart(User buyer, UUID orderItemId) {
        Order cart = getOrCreateCart(buyer);
        
        OrderItem item = cart.getOrderItems().stream()
            .filter(oi -> oi.getOrderItemId().equals(orderItemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));
        
        cart.removeItem(item);
        return orderRepository.save(cart);
    }
    
    /**
     * Clear entire cart
     */
    public void clearCart(User buyer) {
        Order cart = getOrCreateCart(buyer);
        cart.getOrderItems().clear();
        cart.recalculateTotal();
        orderRepository.save(cart);
    }
    
    /**
     * Checkout - convert cart to order
     */
    public Order checkout(User buyer, DeliveryMethod deliveryMethod, UUID deliveryAddressId, String buyerNotes) {
        Order cart = orderRepository.findByBuyerAndStatus(buyer, OrderStatus.CART)
            .orElseThrow(() -> new BadRequestException("Cart is empty"));
        
        if (cart.getOrderItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        
        // Validate products still available
        for (OrderItem item : cart.getOrderItems()) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new BadRequestException("Product " + product.getTitle() + " is no longer available");
            }
        }
        
        cart.setDeliveryMethod(deliveryMethod);
        cart.setDeliveryAddressId(deliveryAddressId);
        cart.setBuyerNotes(buyerNotes);
        
        // Calculate delivery fee based on method
        BigDecimal deliveryFee = calculateDeliveryFee(deliveryMethod);
        cart.setDeliveryFee(deliveryFee);
        cart.recalculateTotal();
        
        cart.placeOrder();
        return orderRepository.save(cart);
    }
    
    /**
     * Mark order as paid (called by PaymentService)
     */
    public Order markAsPaid(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.markAsPaid();
        return orderRepository.save(order);
    }
    
    /**
     * Seller marks order as processing
     */
    public Order markAsProcessing(UUID orderId, User seller) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Verify seller owns at least one item
        boolean isSeller = order.getOrderItems().stream()
            .anyMatch(item -> item.getSeller().getUserId().equals(seller.getUserId()));
        
        if (!isSeller) {
            throw new BadRequestException("You are not the seller for this order");
        }
        
        order.markAsProcessing();
        return orderRepository.save(order);
    }
    
    /**
     * Seller marks order as shipped
     */
    public Order markAsShipped(UUID orderId, User seller, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        boolean isSeller = order.getOrderItems().stream()
            .anyMatch(item -> item.getSeller().getUserId().equals(seller.getUserId()));
        
        if (!isSeller) {
            throw new BadRequestException("You are not the seller for this order");
        }
        
        order.markAsShipped(trackingNumber);
        return orderRepository.save(order);
    }
    
    /**
     * Mark order as delivered
     */
    public Order markAsDelivered(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.markAsDelivered();
        return orderRepository.save(order);
    }
    
    /**
     * Mark order as completed
     */
    public Order markAsCompleted(UUID orderId, User buyer) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (!order.getBuyer().getUserId().equals(buyer.getUserId())) {
            throw new BadRequestException("Not your order");
        }
        
        order.markAsCompleted();
        return orderRepository.save(order);
    }
    
    /**
     * Cancel order
     */
    public Order cancelOrder(UUID orderId, User user) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Only buyer can cancel before shipping
        if (!order.getBuyer().getUserId().equals(user.getUserId())) {
            throw new BadRequestException("Not authorized to cancel this order");
        }
        
        if (order.getStatus() == OrderStatus.SHIPPED || 
            order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel order at this stage");
        }
        
        order.cancel();
        return orderRepository.save(order);
    }
    
    /**
     * Get order by ID
     */
    public Order getOrder(UUID orderId, User user) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Verify user is buyer or seller
        boolean isBuyer = order.getBuyer().getUserId().equals(user.getUserId());
        boolean isSeller = order.getOrderItems().stream()
            .anyMatch(item -> item.getSeller().getUserId().equals(user.getUserId()));
        
        if (!isBuyer && !isSeller) {
            throw new BadRequestException("Not authorized to view this order");
        }
        
        return order;
    }
    
    /**
     * Get user's order history
     */
    public Page<Order> getOrderHistory(User buyer, Pageable pageable) {
        return orderRepository.findOrderHistory(buyer, pageable);
    }
    
    /**
     * Get orders for seller
     */
    public Page<Order> getSellerOrders(User seller, Pageable pageable) {
        return orderRepository.findOrdersBySeller(seller, pageable);
    }
    
    /**
     * Get orders by status (admin)
     */
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }
    
    /**
     * Calculate delivery fee
     */
    private BigDecimal calculateDeliveryFee(DeliveryMethod method) {
        return switch (method) {
            case CAMPUS_PICKUP -> BigDecimal.ZERO;
            case DORM_DELIVERY -> new BigDecimal("3.00");
            case SHIPPING -> new BigDecimal("8.99");
            case DIGITAL -> BigDecimal.ZERO;
        };
    }
}
