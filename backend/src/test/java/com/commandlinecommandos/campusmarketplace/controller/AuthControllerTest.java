package com.commandlinecommandos.campusmarketplace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.commandlinecommandos.campusmarketplace.dto.AuthRequest;
import com.commandlinecommandos.campusmarketplace.dto.AuthResponse;
import com.commandlinecommandos.campusmarketplace.dto.RefreshTokenRequest;
import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.service.AuthService;
import com.commandlinecommandos.campusmarketplace.config.WebSecurityConfig;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private AuthRequest authRequest;
    private AuthResponse authResponse;
    private RefreshTokenRequest refreshTokenRequest;
    
    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");
        authRequest.setDeviceInfo("Test Device");
        
        authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
        authResponse.setTokenType("Bearer");
        authResponse.setExpiresIn(3600L);
        authResponse.setRole(UserRole.STUDENT);
        authResponse.setUsername("testuser");
        authResponse.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        
        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-token");
    }
    
    @Test
    void testSuccessfulLogin() throws Exception {
        // Given
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.userId").value("00000000-0000-0000-0000-000000000001"));
        
        verify(authService).login(any(AuthRequest.class));
    }
    
    @Test
    void testFailedLogin() throws Exception {
        // Given
        when(authService.login(any(AuthRequest.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
    
    @Test
    void testLoginWithInvalidInput() throws Exception {
        // Given
        AuthRequest invalidRequest = new AuthRequest();
        invalidRequest.setUsername(""); // Invalid: empty username
        invalidRequest.setPassword("password123");
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest());
        
        verify(authService, never()).login(any(AuthRequest.class));
    }
    
    @Test
    void testSuccessfulRefreshToken() throws Exception {
        // Given
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);
        
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
        
        verify(authService).refreshToken(any(RefreshTokenRequest.class));
    }
    
    @Test
    void testFailedRefreshToken() throws Exception {
        // Given
        when(authService.refreshToken(any(RefreshTokenRequest.class)))
            .thenThrow(new BadCredentialsException("Invalid refresh token"));
        
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token refresh failed"))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
    
    @Test
    void testLogout() throws Exception {
        // Given
        doNothing().when(authService).logout(anyString());
        
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
        
        verify(authService).logout("refresh-token");
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    void testLogoutAllDevices() throws Exception {
        // Given
        doNothing().when(authService).logoutAllDevices(anyString());
        
        // When & Then
        mockMvc.perform(post("/api/auth/logout-all")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out from all devices successfully"));
        
        verify(authService).logoutAllDevices("testuser");
    }
    
    @Test
    void testLogoutAllDevicesWithoutAuth() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout-all")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(authService, never()).logoutAllDevices(anyString());
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    void testGetCurrentUser() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    @Test
    void testGetCurrentUserWithoutAuth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"STUDENT"})
    void testValidateToken() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    @Test
    void testValidateTokenWithoutAuth() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
