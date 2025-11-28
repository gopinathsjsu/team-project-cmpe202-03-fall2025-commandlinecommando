package com.commandlinecommandos.campusmarketplace.model;

/**
 * Listing status for Campus Marketplace listings
 * Consolidated from listing-api
 */
public enum ListingStatus {
    PENDING,   // Awaiting approval
    ACTIVE,    // Live and visible
    SOLD,      // Marked as sold
    CANCELLED  // Cancelled by seller or admin
}
