package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.AddToCartRequest;
import com.commandlinecommandos.campusmarketplace.dto.CheckoutRequest;
import com.commandlinecommandos.campusmarketplace.dto.UpdateOrderStatusRequest;
import com.commandlinecommandos.campusmarketplace.model.Order;
import com.commandlinecommandos.campusmarketplace.model.OrderStatus;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for Order management
 * Handles cart, checkout, and order lifecycle
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserRepository userRepository;
    
    private User getCurrentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Get current user's cart
     */
    @GetMapping("/cart")
    public ResponseEntity<Order> getCart(Authentication auth) {
        User user = getCurrentUser(auth);
        Order cart = orderService.getOrCreateCart(user);
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Add product to cart
     */
    @PostMapping("/cart/items")
    public ResponseEntity<Order> addToCart(@Valid @RequestBody AddToCartRequest request, 
                                          Authentication auth) {
        User user = getCurrentUser(auth);
        Order cart = orderService.addToCart(user, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Update cart item quantity
     */
    @PutMapping("/cart/items/{orderItemId}")
    public ResponseEntity<Order> updateCartItem(@PathVariable UUID orderItemId,
                                               @RequestParam Integer quantity,
                                               Authentication auth) {
        User user = getCurrentUser(auth);
        Order cart = orderService.updateCartItemQuantity(user, orderItemId, quantity);
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Remove item from cart
     */
    @DeleteMapping("/cart/items/{orderItemId}")
    public ResponseEntity<Order> removeFromCart(@PathVariable UUID orderItemId,
                                               Authentication auth) {
        User user = getCurrentUser(auth);
        Order cart = orderService.removeFromCart(user, orderItemId);
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Clear entire cart
     */
    @DeleteMapping("/cart")
    public ResponseEntity<Void> clearCart(Authentication auth) {
        User user = getCurrentUser(auth);
        orderService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Checkout - convert cart to order
     */
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@Valid @RequestBody CheckoutRequest request,
                                         Authentication auth) {
        User user = getCurrentUser(auth);
        Order order = orderService.checkout(user, request.getDeliveryMethod(), 
                                           request.getDeliveryAddressId(), 
                                           request.getBuyerNotes());
        return ResponseEntity.ok(order);
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable UUID orderId, 
                                         Authentication auth) {
        User user = getCurrentUser(auth);
        Order order = orderService.getOrder(orderId, user);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Get user's order history (excluding cart)
     */
    @GetMapping
    public ResponseEntity<Page<Order>> getOrderHistory(Authentication auth, Pageable pageable) {
        User user = getCurrentUser(auth);
        Page<Order> orders = orderService.getOrderHistory(user, pageable);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get seller's orders
     */
    @GetMapping("/seller")
    public ResponseEntity<Page<Order>> getSellerOrders(Authentication auth, Pageable pageable) {
        User user = getCurrentUser(auth);
        Page<Order> orders = orderService.getSellerOrders(user, pageable);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Update order status (seller operations)
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable UUID orderId,
                                                   @Valid @RequestBody UpdateOrderStatusRequest request,
                                                   Authentication auth) {
        User user = getCurrentUser(auth);
        Order order;
        
        switch (request.getStatus()) {
            case PROCESSING:
                order = orderService.markAsProcessing(orderId, user);
                break;
            case SHIPPED:
                order = orderService.markAsShipped(orderId, user, request.getTrackingNumber());
                break;
            case DELIVERED:
                order = orderService.markAsDelivered(orderId);
                break;
            case COMPLETED:
                order = orderService.markAsCompleted(orderId, user);
                break;
            case CANCELLED:
                order = orderService.cancelOrder(orderId, user);
                break;
            default:
                throw new RuntimeException("Invalid status transition");
        }
        
        return ResponseEntity.ok(order);
    }
    
    /**
     * Cancel order
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId, 
                                            Authentication auth) {
        User user = getCurrentUser(auth);
        Order order = orderService.cancelOrder(orderId, user);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Admin: Get orders by status
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<Page<Order>> getOrdersByStatus(@PathVariable OrderStatus status,
                                                         Pageable pageable) {
        Page<Order> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }
}
