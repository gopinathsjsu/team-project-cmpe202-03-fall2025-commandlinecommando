package com.commandlinecommandos.campusmarketplace.dto;

/**
 * Base interface for common user fields shared across DTOs.
 * Implements Interface Segregation Principle (ISP) - defines only
 * the common user profile fields that are needed across different contexts.
 * 
 * Used by:
 * - StudentRegisterRequest (student self-registration)
 * - CreateUserRequest (admin user creation)
 * - ProfileUpdateRequest (user profile updates)
 */
public interface BaseUserFields {
    
    /**
     * Get the username for the user account
     * @return the username
     */
    String getUsername();
    
    /**
     * Get the email address
     * @return the email
     */
    String getEmail();
    
    /**
     * Get the user's first name
     * @return the first name
     */
    String getFirstName();
    
    /**
     * Get the user's last name
     * @return the last name
     */
    String getLastName();
    
    /**
     * Get the user's phone number
     * @return the phone number (optional)
     */
    String getPhone();
}
