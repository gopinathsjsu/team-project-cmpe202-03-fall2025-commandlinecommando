package com.commandlinecommandos.campusmarketplace.model;

/**
 * User roles for Campus Marketplace
 * BUYER - Students who buy items
 * SELLER - Students who sell items
 * STUDENT - Students who can both buy and sell items (for backwards compatibility)
 * ADMIN - Platform administrators
 */
public enum UserRole {
    BUYER,    // Students who buy items
    SELLER,   // Students who sell items
    STUDENT,  // Students who can both buy and sell items (legacy/unified role)
    ADMIN     // Platform administrator
}