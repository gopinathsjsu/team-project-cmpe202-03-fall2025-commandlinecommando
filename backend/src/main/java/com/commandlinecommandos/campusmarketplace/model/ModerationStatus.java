package com.commandlinecommandos.campusmarketplace.model;

/**
 * Moderation status for content review
 * Matches PostgreSQL enum: moderation_status
 */
public enum ModerationStatus {
    PENDING,    // Awaiting moderation
    APPROVED,   // Approved for display
    REJECTED,   // Rejected by moderator
    FLAGGED     // Flagged for review
}

