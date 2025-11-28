package com.commandlinecommandos.campusmarketplace.dto;

import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.model.VerificationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for user response (without sensitive information).
 * Supports many-to-many user-role relationship.
 */
public class UserResponse {
    
    @JsonProperty("id") // expose as `id` in JSON while keeping existing `userId` field name
    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatarUrl;
    private Set<UserRole> roles;
    private VerificationStatus verificationStatus;
    @JsonProperty("isActive")
    private boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Student-specific fields
    private String studentId;
    private String universityEmail;
    private Integer graduationYear;
    private String major;
    
    // University info
    private UUID universityId;
    private String universityName;
    
    // Preferences (selected fields)
    private Map<String, Object> preferences;

    // Constructors
    public UserResponse() {
    }

    // Builder pattern - manual implementation
    public static UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }

    public static class UserResponseBuilder {
        private final UserResponse response = new UserResponse();

        public UserResponseBuilder userId(UUID userId) {
            response.userId = userId;
            return this;
        }

        public UserResponseBuilder username(String username) {
            response.username = username;
            return this;
        }

        public UserResponseBuilder email(String email) {
            response.email = email;
            return this;
        }

        public UserResponseBuilder firstName(String firstName) {
            response.firstName = firstName;
            return this;
        }

        public UserResponseBuilder lastName(String lastName) {
            response.lastName = lastName;
            return this;
        }

        public UserResponseBuilder phone(String phone) {
            response.phone = phone;
            return this;
        }

        public UserResponseBuilder avatarUrl(String avatarUrl) {
            response.avatarUrl = avatarUrl;
            return this;
        }

        public UserResponseBuilder roles(Set<UserRole> roles) {
            response.roles = roles;
            return this;
        }

        public UserResponseBuilder verificationStatus(VerificationStatus verificationStatus) {
            response.verificationStatus = verificationStatus;
            return this;
        }

        public UserResponseBuilder isActive(boolean isActive) {
            response.isActive = isActive;
            return this;
        }

        public UserResponseBuilder lastLoginAt(LocalDateTime lastLoginAt) {
            response.lastLoginAt = lastLoginAt;
            return this;
        }

        public UserResponseBuilder emailVerifiedAt(LocalDateTime emailVerifiedAt) {
            response.emailVerifiedAt = emailVerifiedAt;
            return this;
        }

        public UserResponseBuilder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public UserResponseBuilder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }

        public UserResponseBuilder studentId(String studentId) {
            response.studentId = studentId;
            return this;
        }

        public UserResponseBuilder universityEmail(String universityEmail) {
            response.universityEmail = universityEmail;
            return this;
        }

        public UserResponseBuilder graduationYear(Integer graduationYear) {
            response.graduationYear = graduationYear;
            return this;
        }

        public UserResponseBuilder major(String major) {
            response.major = major;
            return this;
        }

        public UserResponseBuilder universityId(UUID universityId) {
            response.universityId = universityId;
            return this;
        }

        public UserResponseBuilder universityName(String universityName) {
            response.universityName = universityName;
            return this;
        }

        public UserResponseBuilder preferences(Map<String, Object> preferences) {
            response.preferences = preferences;
            return this;
        }

        public UserResponse build() {
            return response;
        }
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getUniversityEmail() {
        return universityEmail;
    }

    public void setUniversityEmail(String universityEmail) {
        this.universityEmail = universityEmail;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public UUID getUniversityId() {
        return universityId;
    }

    public void setUniversityId(UUID universityId) {
        this.universityId = universityId;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }
}
