package com.commandlinecommandos.campusmarketplace.dto;

import com.commandlinecommandos.campusmarketplace.model.DeliveryMethod;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CheckoutRequest {
    
    @NotNull(message = "Delivery method is required")
    private DeliveryMethod deliveryMethod;
    
    private UUID deliveryAddressId;
    
    private String buyerNotes;
    
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
    
    public String getBuyerNotes() {
        return buyerNotes;
    }
    
    public void setBuyerNotes(String buyerNotes) {
        this.buyerNotes = buyerNotes;
    }
}
