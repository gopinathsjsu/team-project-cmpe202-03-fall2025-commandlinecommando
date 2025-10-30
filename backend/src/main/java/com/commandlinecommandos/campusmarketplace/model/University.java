package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * University entity for multi-tenant support
 * Each university has its own marketplace instance
 */
@Entity
@Table(name = "universities", indexes = {
    @Index(name = "idx_university_domain", columnList = "domain"),
    @Index(name = "idx_university_active", columnList = "is_active")
})
public class University {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "university_id", updatable = false, nullable = false)
    private UUID universityId;
    
    @NotNull
    @Column(nullable = false)
    private String name;
    
    @NotNull
    @Column(unique = true, nullable = false)
    private String domain;  // e.g., "sjsu.edu"
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 50)
    private String state;
    
    @Column(length = 50)
    private String country = "USA";
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Lob
    @Type(JsonType.class)
    @Column(name = "settings", columnDefinition = "TEXT")
    private Map<String, Object> settings = new HashMap<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public University() {
    }
    
    public University(String name, String domain, String city, String state) {
        this.name = name;
        this.domain = domain;
        this.city = city;
        this.state = state;
    }
    
    // Getters and Setters
    public UUID getUniversityId() {
        return universityId;
    }
    
    public void setUniversityId(UUID universityId) {
        this.universityId = universityId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public Map<String, Object> getSettings() {
        return settings;
    }
    
    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
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
    
    // Business methods
    public boolean isValidDomain(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        String emailDomain = email.substring(email.indexOf("@") + 1);
        return this.domain.equalsIgnoreCase(emailDomain);
    }
}

