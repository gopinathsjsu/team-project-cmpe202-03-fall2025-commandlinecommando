package com.commandlinecommandos.campusmarketplace.communication.dto;

import com.commandlinecommandos.campusmarketplace.communication.model.NotificationPreference;
import java.util.UUID;

/**
 * DTO for notification preference response.
 */
public class NotificationPreferenceResponse {
    
    private UUID userId;
    private Boolean emailNotificationsEnabled;
    private String email;
    private String firstName;

    public NotificationPreferenceResponse() {
    }
    
    public NotificationPreferenceResponse(NotificationPreference preference) {
        this.userId = preference.getUserId();
        this.emailNotificationsEnabled = preference.getEmailNotificationsEnabled();
        this.email = preference.getEmail();
        this.firstName = preference.getFirstName();
    }
    
    public NotificationPreferenceResponse(UUID userId, Boolean emailNotificationsEnabled, String email, String firstName) {
        this.userId = userId;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
        this.email = email;
        this.firstName = firstName;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
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
