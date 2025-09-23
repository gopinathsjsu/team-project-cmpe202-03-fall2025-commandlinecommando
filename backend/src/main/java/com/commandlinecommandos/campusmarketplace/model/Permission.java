package com.commandlinecommandos.campusmarketplace.model;

public enum Permission {
    // User management permissions
    CREATE_USER,
    READ_USER,
    UPDATE_USER,
    DELETE_USER,
    
    // Listing management permissions
    CREATE_LISTING,
    READ_LISTING,
    UPDATE_LISTING,
    DELETE_LISTING,
    MODERATE_LISTING,
    
    // Report management permissions
    VIEW_REPORTS,
    RESOLVE_REPORTS,
    
    // System administration
    SYSTEM_CONFIG,
    VIEW_ANALYTICS,
    MANAGE_CATEGORIES
}
