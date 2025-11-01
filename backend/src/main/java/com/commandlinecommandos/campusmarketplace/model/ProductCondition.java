package com.commandlinecommandos.campusmarketplace.model;

/**
 * Product condition for marketplace listings
 * Matches PostgreSQL enum: product_condition
 */
public enum ProductCondition {
    NEW("New"),
    LIKE_NEW("Like New"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor");
    
    private final String displayName;
    
    ProductCondition(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

