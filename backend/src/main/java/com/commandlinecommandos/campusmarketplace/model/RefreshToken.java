package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
// Removed Lombok dependencies - using manual getters/setters
import org.hibernate.annotations.CreationTimestamp;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_token", columnList = "token"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at"),
    @Index(name = "idx_is_revoked", columnList = "is_revoked")
})
public class RefreshToken {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    private java.util.UUID id;
    
    @NotNull
    @Column(unique = true, length = 1000)
    private String token;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @NotNull
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_revoked")
    private boolean isRevoked = false;
    
    @Column(name = "device_info")
    private String deviceInfo;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !isRevoked && !isExpired();
    }
    
    // Constructors
    public RefreshToken() {
    }
    
    public RefreshToken(String token, User user, LocalDateTime expiresAt, String deviceInfo) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
        this.isRevoked = false;
    }
    
    // Getters and Setters
    public java.util.UUID getId() {
        return id;
    }
    
    public void setId(java.util.UUID id) {
        this.id = id;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public boolean isRevoked() {
        return isRevoked;
    }
    
    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }
}
