package com.commandlinecommandos.campusmarketplace.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ProcessPaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;
    
    @NotNull(message = "Payment method ID is required")
    private UUID paymentMethodId;
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
    
    public UUID getPaymentMethodId() {
        return paymentMethodId;
    }
    
    public void setPaymentMethodId(UUID paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}
