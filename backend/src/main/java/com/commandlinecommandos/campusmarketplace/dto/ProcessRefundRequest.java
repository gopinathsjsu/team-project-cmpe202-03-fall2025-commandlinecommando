package com.commandlinecommandos.campusmarketplace.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public class ProcessRefundRequest {
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;
    
    @NotNull(message = "Refund amount is required")
    @Positive(message = "Refund amount must be positive")
    private BigDecimal refundAmount;
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
}
