package com.commandlinecommandos.campusmarketplace.model;

/**
 * User roles for Campus Marketplace
 * BUYER - Students who purchase items
 * SELLER - Students who list items for sale  
 * ADMIN - Platform administrators
 * 
 * Note: Students can be both BUYER and SELLER by having appropriate permissions
 * This enum represents their primary role in the system
 */
public enum UserRole {
    BUYER,    // Student purchasing items
    SELLER,   // Student selling items
    ADMIN     // Platform administrator
}