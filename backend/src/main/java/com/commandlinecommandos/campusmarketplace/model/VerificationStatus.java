package com.commandlinecommandos.campusmarketplace.model;

/**
 * Verification status for user accounts
 * Matches PostgreSQL enum: verification_status
 */
public enum VerificationStatus {
    PENDING,    // User registered, awaiting email/student verification
    VERIFIED,   // User verified with university email
    REJECTED,   // Verification rejected (invalid credentials)
    SUSPENDED   // Account suspended by admin
}

