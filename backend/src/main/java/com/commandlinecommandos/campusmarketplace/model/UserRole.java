package com.commandlinecommandos.campusmarketplace.model;

/**
 * User roles for Campus Marketplace
 * SELLER - Users who can create and manage listings
 * BUYER - Users who can search items and chat with sellers
 * ADMIN - Platform administrators for moderation
 *
 * Note: Per professor's requirements, these are distinct roles
 */
public enum UserRole {
    SELLER,  // Can create and manage listings
    BUYER,   // Can search items and chat with sellers
    ADMIN    // Platform administrator for moderation
}