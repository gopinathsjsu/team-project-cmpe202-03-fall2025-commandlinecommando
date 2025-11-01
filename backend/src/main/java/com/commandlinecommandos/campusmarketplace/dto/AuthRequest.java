package com.commandlinecommandos.campusmarketplace.dto;

import jakarta.validation.constraints.NotBlank;
// Removed Lombok dependencies - using manual getters/setters

public class AuthRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private String deviceInfo;
    
    // Constructors
    public AuthRequest() {
    }
    
    public AuthRequest(String username, String password, String deviceInfo) {
        this.username = username;
        this.password = password;
        this.deviceInfo = deviceInfo;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
