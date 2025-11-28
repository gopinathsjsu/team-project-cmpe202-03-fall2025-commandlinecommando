package com.commandlinecommandos.campusmarketplace.model;

/**
 * User roles for Campus Marketplace.
 * 
 * Users can have multiple roles through a many-to-many relationship:
 * - Students typically have both BUYER and SELLER roles
 * - Students can choose to have only BUYER, only SELLER, or both
 * - ADMIN role is exclusive (cannot be combined with BUYER/SELLER)
 * 
 * Role Capabilities:
 * - BUYER: Search listings, purchase items, chat with sellers
 * - SELLER: Create/manage listings, mark items as sold, chat with buyers
 * - ADMIN: Moderate listings, manage users, view reports
 */
public enum UserRole {
    BUYER,    // Students who can buy items
    SELLER,   // Students who can sell items
    ADMIN     // Platform administrator (exclusive role, cannot buy/sell)
}