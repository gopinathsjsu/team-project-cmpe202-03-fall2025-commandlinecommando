package com.commandlinecommandos.communication.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for updating notification preferences.
 */
public class UpdateNotificationPreferenceRequest {
    
    @NotNull(message = "emailNotificationsEnabled is required")
    private Boolean emailNotificationsEnabled;

    @NotNull(message = "email is required")
    @Email(message = "invalid email address")
    private String email;

    @NotNull(message = "firstName is required")
    @Length(min = 2, max = 100, message = "firstName must be between 2 and 100 characters")
    private String firstName;
    
    public UpdateNotificationPreferenceRequest() {
    }
    
    public UpdateNotificationPreferenceRequest(Boolean emailNotificationsEnabled, String email, String firstName) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
        this.email = email;
        this.firstName = firstName;
    }
    
    public Boolean getEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }
    
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}

