package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

/**
 * User entity for Campus Marketplace
 * Supports STUDENT and ADMIN roles
 * Students can both buy and sell items with a single role
 * Aligned with PostgreSQL schema using UUID primary keys
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_username", columnList = "username"),
    @Index(name = "idx_users_university", columnList = "university_id"),
    @Index(name = "idx_users_role", columnList = "role"),
    @Index(name = "idx_users_verification", columnList = "verification_status")
})
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = true)
    private University university;
    
    // Authentication
    @NotNull
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotNull
    @Email
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotNull
    @Length(min = 8)
    @Column(name = "password_hash", nullable = false)
    private String password;
    
    // Profile Information
    @NotNull
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @NotNull
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    // Role & Status
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    // University Verification (for students)
    @Column(name = "student_id", length = 50)
    private String studentId;
    
    @Column(name = "university_email")
    private String universityEmail;
    
    @Column(name = "graduation_year")
    private Integer graduationYear;
    
    @Column(length = 100)
    private String major;
    
    // Preferences (JSON storage for flexibility)
    @Lob
    @Type(JsonType.class)
    @Column(name = "preferences", columnDefinition = "TEXT")
    private Map<String, Object> preferences = new HashMap<>();
    
    // Security & Tracking
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public User() {
        initializeDefaultPreferences();
    }
    
    public User(String username, String email, String password, UserRole role, University university) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.university = university;
        this.isActive = true;
        initializeDefaultPreferences();
    }
    
    private void initializeDefaultPreferences() {
        if (this.preferences == null) {
            this.preferences = new HashMap<>();
        }
        this.preferences.putIfAbsent("notifications", true);
        this.preferences.putIfAbsent("email_updates", true);
        this.preferences.putIfAbsent("search_radius_miles", 10);
        this.preferences.putIfAbsent("preferred_categories", List.of());
        this.preferences.putIfAbsent("blocked_users", List.of());
    }
    
    // Spring Security UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return verificationStatus != VerificationStatus.SUSPENDED;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return isActive;
    }
    
    // Business methods
    public boolean authenticate(String rawPassword) {
        // Implementation delegated to AuthenticationManager
        return true;
    }
    
    public void updateProfile(String firstName, String lastName, String phone, String avatarUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }
    
    public void deactivateAccount() {
        this.isActive = false;
    }
    
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
    
    public void verifyEmail() {
        this.emailVerifiedAt = LocalDateTime.now();
        this.verificationStatus = VerificationStatus.VERIFIED;
    }
    
    public boolean isVerified() {
        return this.verificationStatus == VerificationStatus.VERIFIED;
    }
    
    public boolean isStudent() {
        return this.role == UserRole.STUDENT;
    }
    
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public University getUniversity() {
        return university;
    }
    
    public void setUniversity(University university) {
        this.university = university;
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
    
    public void setPassword(String password) {
        this.password = password;
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
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }
    
    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
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
    
    public Map<String, Object> getPreferences() {
        return preferences;
    }
    
    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
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
    
    // Alias for compatibility
    public UUID getId() {
        return userId;
    }
    
    public void setId(UUID id) {
        this.userId = id;
    }
}
