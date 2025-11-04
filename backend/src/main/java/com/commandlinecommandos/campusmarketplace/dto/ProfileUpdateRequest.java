package com.commandlinecommandos.campusmarketplace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for updating user profile information
 */
@Data
public class ProfileUpdateRequest {
    
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;
    
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    
    private String avatarUrl;
    
    // Student-specific fields
    private String studentId;
    private String universityEmail;
    private Integer graduationYear;
    private String major;
    
    // Preferences
    private Boolean notifications;
    private Boolean emailUpdates;
    private Integer searchRadiusMiles;
}

