package com.commandlinecommandos.campusmarketplace.model;

/**
 * Status of reports for listing moderation
 * Consolidated from listing-api
 */
public enum ReportStatus {
    PENDING,       // Report submitted, awaiting review
    UNDER_REVIEW,  // Report being reviewed by moderator
    RESOLVED,      // Report resolved, listing may be removed
    DISMISSED      // Report dismissed by moderator
}
