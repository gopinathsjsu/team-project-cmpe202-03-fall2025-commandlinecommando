package com.commandlinecommandos.campusmarketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for student self-registration.
 * Implements BaseUserFields interface for common user profile fields.
 * 
 * Students automatically receive both BUYER and SELLER roles upon registration.
 * They can later modify their roles in profile settings to be:
 * - BUYER only
 * - SELLER only  
 * - Both BUYER and SELLER (default)
 * 
 * Note: This DTO does NOT include a role field - roles are auto-assigned.
 * Admin accounts cannot be created through this registration flow.
 */
public class StudentRegisterRequest implements BaseUserFields {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
             message = "Password must be 8+ characters with at least one letter and one number")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String phone;
    
    // Student-specific fields
    @NotBlank(message = "Student ID is required for registration")
    private String studentId;
    
    private String major;
    private Integer graduationYear;
    private String campusLocation;
    
    // Constructors
    public StudentRegisterRequest() {
    }
    
    public StudentRegisterRequest(String username, String email, String password, 
                                   String firstName, String lastName, String studentId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentId = studentId;
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
    
    // Setters
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
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
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getMajor() {
        return major;
    }
    
    public void setMajor(String major) {
        this.major = major;
    }
    
    public Integer getGraduationYear() {
        return graduationYear;
    }
    
    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }
    
    public String getCampusLocation() {
        return campusLocation;
    }
    
    public void setCampusLocation(String campusLocation) {
        this.campusLocation = campusLocation;
    }
}
