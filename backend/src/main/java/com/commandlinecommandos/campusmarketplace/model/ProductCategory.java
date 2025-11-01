package com.commandlinecommandos.campusmarketplace.model;

/**
 * Product categories for Campus Marketplace
 * Matches PostgreSQL enum: product_category
 */
public enum ProductCategory {
    TEXTBOOKS("Textbooks"),
    ELECTRONICS("Electronics"),
    FURNITURE("Furniture"),
    CLOTHING("Clothing"),
    SPORTS_EQUIPMENT("Sports Equipment"),
    SERVICES("Services"),
    OTHER("Other");
    
    private final String displayName;
    
    ProductCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

