package com.commandlinecommandos.campusmarketplace.dto;

import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.model.VerificationStatus;
import lombok.Data;

/**
 * DTO for admin to update user accounts
 */
@Data
public class UpdateUserRequest {
    
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private VerificationStatus verificationStatus;
    private Boolean isActive;
    private String studentId;
    private Integer graduationYear;
    private String major;
}

