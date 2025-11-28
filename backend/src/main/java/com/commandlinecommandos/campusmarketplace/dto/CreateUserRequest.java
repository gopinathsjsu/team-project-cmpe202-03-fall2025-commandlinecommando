package com.commandlinecommandos.campusmarketplace.dto;

import com.commandlinecommandos.campusmarketplace.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for admin to create new user accounts.
 * Implements BaseUserFields interface for common user profile fields.
 * 
 * Supports the many-to-many user-role relationship:
 * - For student accounts: assign BUYER and/or SELLER roles
 * - For admin accounts: assign only ADMIN role (exclusive)
 * 
 * Validation rules:
 * - ADMIN role cannot be combined with BUYER or SELLER roles
 * - Student accounts must have at least one of BUYER or SELLER roles
 */
public class CreateUserRequest implements BaseUserFields {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String phone;
    
    @NotEmpty(message = "At least one role is required")
    private Set<UserRole> roles;
    
    @NotNull(message = "University ID is required")
    private UUID universityId;
    
    // A temporary password will be generated and sent via email
    private boolean sendWelcomeEmail = true;
    
    // Student-specific fields (required when roles contain BUYER or SELLER)
    private String studentId;
    private Integer graduationYear;
    private String major;

    // Constructors
    public CreateUserRequest() {
    }

    // BaseUserFields interface implementation
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    // Setters and additional getters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public UUID getUniversityId() {
        return universityId;
    }

    public void setUniversityId(UUID universityId) {
        this.universityId = universityId;
    }

    public boolean isSendWelcomeEmail() {
        return sendWelcomeEmail;
    }

    public void setSendWelcomeEmail(boolean sendWelcomeEmail) {
        this.sendWelcomeEmail = sendWelcomeEmail;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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
    
    /**
     * Check if this request is for creating an admin user
     */
    public boolean isAdminUser() {
        return roles != null && roles.contains(UserRole.ADMIN);
    }
    
    /**
     * Check if this request is for creating a student user
     */
    public boolean isStudentUser() {
        return roles != null && (roles.contains(UserRole.BUYER) || roles.contains(UserRole.SELLER));
    }
}
