package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PaymentMethod entity - Tokenized payment information
 * Stores payment method tokens (NOT sensitive card data)
 */
@Entity
@Table(name = "payment_methods", indexes = {
    @Index(name = "idx_payment_methods_user", columnList = "user_id"),
    @Index(name = "idx_payment_methods_active", columnList = "is_active")
})
public class PaymentMethod {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "payment_method_id", updatable = false, nullable = false)
    private UUID paymentMethodId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false, length = 20)
    private PaymentMethodType methodType;
    
    @NotNull
    @Column(name = "payment_token", length = 255, nullable = false)
    private String paymentToken; // Payment gateway token

    @Column(name = "last_four", length = 4)
    private String lastFour; // Last 4 digits for display

    @Column(name = "card_brand", length = 50)
    private String cardBrand; // Visa, Mastercard, etc.

    @Column(name = "expiry_month")
    private Integer expiryMonth;

    @Column(name = "expiry_year")
    private Integer expiryYear;

    @Column(name = "billing_address_id")
    private UUID billingAddressId;
    
    @NotNull
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public PaymentMethod() {
    }
    
    public PaymentMethod(User user, PaymentMethodType methodType) {
        this.user = user;
        this.methodType = methodType;
    }
    
    // Business methods
    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return expiryYear < now.getYear() || 
               (expiryYear == now.getYear() && expiryMonth < now.getMonthValue());
    }
    
    public String getMaskedNumber() {
        if (lastFour == null || lastFour.isEmpty()) {
            return "****";
        }
        return "**** **** **** " + lastFour;
    }
    
    // Getters and Setters
    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }
    
    public void setPaymentMethodId(UUID paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public PaymentMethodType getMethodType() {
        return methodType;
    }
    
    public void setMethodType(PaymentMethodType methodType) {
        this.methodType = methodType;
    }
    
    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }
    
    public String getLastFour() {
        return lastFour;
    }
    
    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }
    
    public String getCardBrand() {
        return cardBrand;
    }
    
    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }
    
    public Integer getExpiryMonth() {
        return expiryMonth;
    }
    
    public void setExpiryMonth(Integer expiryMonth) {
        this.expiryMonth = expiryMonth;
    }
    
    public Integer getExpiryYear() {
        return expiryYear;
    }
    
    public void setExpiryYear(Integer expiryYear) {
        this.expiryYear = expiryYear;
    }
    
    public UUID getBillingAddressId() {
        return billingAddressId;
    }

    public void setBillingAddressId(UUID billingAddressId) {
        this.billingAddressId = billingAddressId;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
