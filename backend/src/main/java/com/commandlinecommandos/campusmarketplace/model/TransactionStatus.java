package com.commandlinecommandos.campusmarketplace.model;

/**
 * Transaction status enum
 * Matches transaction_status ENUM in PostgreSQL
 */
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
