package com.commandlinecommandos.communication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing user email notification preferences.
 * Users can enable/disable email notifications for new messages.
 */
@Entity
@Table(name = "notification_preferences", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id"}))
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long preferenceId;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "email", nullable = true)
    private String email;
    
    @Column(name = "first_name", nullable = true)
    private String firstName;
    
    @Column(name = "email_notifications_enabled", nullable = false)
    private Boolean emailNotificationsEnabled;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.emailNotificationsEnabled == null) {
            this.emailNotificationsEnabled = false; // Default to disabled
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public NotificationPreference() {
        this.emailNotificationsEnabled = false; // Default to disabled
        this.email = null;
        this.firstName = null;
    }
    
    public NotificationPreference(Long userId, Boolean emailNotificationsEnabled, String email, String firstName) {
        this.userId = userId;
        this.emailNotificationsEnabled = emailNotificationsEnabled != null ? emailNotificationsEnabled : false;
        this.email = email;
        this.firstName = firstName;
    }
    
    public Long getPreferenceId() {
        return preferenceId;
    }
    
    public void setPreferenceId(Long preferenceId) {
        this.preferenceId = preferenceId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public Boolean getEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }
    
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

