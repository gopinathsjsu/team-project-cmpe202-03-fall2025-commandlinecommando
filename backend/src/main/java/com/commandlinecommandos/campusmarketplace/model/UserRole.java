package com.commandlinecommandos.campusmarketplace.model;

/**
 * User roles for Campus Marketplace
 * STUDENT - Students who can both buy and sell items
 * ADMIN - Platform administrators
 * 
 * Note: Students have a single role but can perform both buying and selling activities
 */
public enum UserRole {
    STUDENT,  // Students who can both buy and sell items
    ADMIN     // Platform administrator
}