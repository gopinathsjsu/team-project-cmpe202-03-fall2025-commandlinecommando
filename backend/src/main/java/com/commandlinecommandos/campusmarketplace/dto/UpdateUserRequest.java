package com.commandlinecommandos.campusmarketplace.dto;

import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.model.VerificationStatus;

import java.util.Set;

/**
 * DTO for admin to update user accounts.
 * Supports the many-to-many user-role relationship.
 * 
 * Validation rules enforced by service layer:
 * - ADMIN role cannot be combined with BUYER or SELLER roles
 * - Student accounts must have at least one of BUYER or SELLER roles
 * - Cannot change a student to admin or vice versa (must deactivate and create new)
 */
public class UpdateUserRequest {
    
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Set<UserRole> roles;
    private VerificationStatus verificationStatus;
    private Boolean isActive;
    private String studentId;
    private Integer graduationYear;
    private String major;

    // Constructors
    public UpdateUserRequest() {
    }

    // Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
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

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
}
