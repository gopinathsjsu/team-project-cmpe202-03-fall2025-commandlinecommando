package com.commandlinecommandos.listingapi.model;

public enum ReportStatus {
    PENDING, // Report has been submitted and is waiting for review
    UNDER_REVIEW, // Report is being reviewed by a moderator
    RESOLVED, // Report has been resolved by a moderator and the listing has been removed
    DISMISSED, // Report has been dismissed by a moderator
}
