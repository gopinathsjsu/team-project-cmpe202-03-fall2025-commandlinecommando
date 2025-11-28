package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.AddPaymentMethodRequest;
import com.commandlinecommandos.campusmarketplace.dto.ProcessPaymentRequest;
import com.commandlinecommandos.campusmarketplace.dto.ProcessRefundRequest;
import com.commandlinecommandos.campusmarketplace.model.PaymentMethod;
import com.commandlinecommandos.campusmarketplace.model.Transaction;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for Payment processing
 * Handles payment methods and transactions
 */
@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private UserRepository userRepository;
    
    private User getCurrentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Get user's payment methods
     */
    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods(Authentication auth) {
        User user = getCurrentUser(auth);
        List<PaymentMethod> methods = paymentService.getPaymentMethods(user);
        return ResponseEntity.ok(methods);
    }
    
    /**
     * Add payment method
     */
    @PostMapping("/methods")
    public ResponseEntity<PaymentMethod> addPaymentMethod(@Valid @RequestBody AddPaymentMethodRequest request,
                                                          Authentication auth) {
        User user = getCurrentUser(auth);
        PaymentMethod method = paymentService.addPaymentMethod(
            user,
            request.getMethodType(),
            request.getToken(),
            request.getLastFour(),
            request.getCardBrand(),
            request.getExpiryMonth(),
            request.getExpiryYear(),
            request.getBillingName(),
            request.getBillingZip()
        );
        return ResponseEntity.ok(method);
    }
    
    /**
     * Get payment method by ID
     */
    @GetMapping("/methods/{paymentMethodId}")
    public ResponseEntity<PaymentMethod> getPaymentMethod(@PathVariable UUID paymentMethodId,
                                                          Authentication auth) {
        User user = getCurrentUser(auth);
        PaymentMethod method = paymentService.getPaymentMethod(paymentMethodId, user);
        return ResponseEntity.ok(method);
    }
    
    /**
     * Set default payment method
     */
    @PutMapping("/methods/{paymentMethodId}/default")
    public ResponseEntity<PaymentMethod> setDefaultPaymentMethod(@PathVariable UUID paymentMethodId,
                                                                 Authentication auth) {
        User user = getCurrentUser(auth);
        PaymentMethod method = paymentService.setDefaultPaymentMethod(paymentMethodId, user);
        return ResponseEntity.ok(method);
    }
    
    /**
     * Delete payment method
     */
    @DeleteMapping("/methods/{paymentMethodId}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable UUID paymentMethodId,
                                                    Authentication auth) {
        User user = getCurrentUser(auth);
        paymentService.deletePaymentMethod(paymentMethodId, user);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Process payment for order
     */
    @PostMapping("/process")
    public ResponseEntity<Transaction> processPayment(@Valid @RequestBody ProcessPaymentRequest request,
                                                      Authentication auth) {
        User user = getCurrentUser(auth);
        Transaction transaction = paymentService.processPayment(
            request.getOrderId(),
            request.getPaymentMethodId(),
            user
        );
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Get transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<Transaction>> getTransactionHistory(Authentication auth, 
                                                                   Pageable pageable) {
        User user = getCurrentUser(auth);
        Page<Transaction> transactions = paymentService.getTransactionHistory(user, pageable);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transaction by ID
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID transactionId,
                                                      Authentication auth) {
        User user = getCurrentUser(auth);
        Transaction transaction = paymentService.getTransaction(transactionId, user);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Get transactions for order
     */
    @GetMapping("/orders/{orderId}/transactions")
    public ResponseEntity<List<Transaction>> getOrderTransactions(@PathVariable UUID orderId,
                                                                  Authentication auth) {
        User user = getCurrentUser(auth);
        List<Transaction> transactions = paymentService.getOrderTransactions(orderId, user);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Admin: Process refund
     */
    @PostMapping("/refund")
    public ResponseEntity<Transaction> processRefund(@Valid @RequestBody ProcessRefundRequest request,
                                                     Authentication auth) {
        User admin = getCurrentUser(auth);
        Transaction refund = paymentService.processRefund(
            request.getOrderId(),
            request.getRefundAmount(),
            admin
        );
        return ResponseEntity.ok(refund);
    }
}
