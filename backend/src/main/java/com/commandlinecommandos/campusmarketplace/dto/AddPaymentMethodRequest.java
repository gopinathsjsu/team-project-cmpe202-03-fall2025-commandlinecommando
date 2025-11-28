package com.commandlinecommandos.campusmarketplace.dto;

import com.commandlinecommandos.campusmarketplace.model.PaymentMethodType;
import jakarta.validation.constraints.NotNull;

public class AddPaymentMethodRequest {
    
    @NotNull(message = "Payment method type is required")
    private PaymentMethodType methodType;
    
    @NotNull(message = "Token is required")
    private String token;
    
    private String lastFour;
    private String cardBrand;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String billingName;
    private String billingZip;
    
    public PaymentMethodType getMethodType() {
        return methodType;
    }
    
    public void setMethodType(PaymentMethodType methodType) {
        this.methodType = methodType;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
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
    
    public String getBillingName() {
        return billingName;
    }
    
    public void setBillingName(String billingName) {
        this.billingName = billingName;
    }
    
    public String getBillingZip() {
        return billingZip;
    }
    
    public void setBillingZip(String billingZip) {
        this.billingZip = billingZip;
    }
}
