package com.commandlinecommandos.campusmarketplace.model;

/**
 * Delivery methods for Campus Marketplace
 * Matches PostgreSQL enum: delivery_method
 */
public enum DeliveryMethod {
    CAMPUS_PICKUP("Campus Pickup"),
    DORM_DELIVERY("Dorm Delivery"),
    SHIPPING("Shipping"),
    DIGITAL("Digital");
    
    private final String displayName;
    
    DeliveryMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

