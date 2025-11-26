package com.commandlinecommandos.campusmarketplace.model;

/**
 * Payment method type enum
 * Matches payment_method_type ENUM in PostgreSQL
 */
public enum PaymentMethodType {
    CREDIT_CARD,
    DEBIT_CARD,
    PAYPAL,
    VENMO,
    CAMPUS_CARD
}
