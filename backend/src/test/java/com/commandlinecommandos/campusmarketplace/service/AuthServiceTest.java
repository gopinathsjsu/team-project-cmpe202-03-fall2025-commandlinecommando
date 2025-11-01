package com.commandlinecommandos.campusmarketplace.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.commandlinecommandos.campusmarketplace.dto.AuthRequest;
import com.commandlinecommandos.campusmarketplace.dto.AuthResponse;
import com.commandlinecommandos.campusmarketplace.dto.RefreshTokenRequest;
import com.commandlinecommandos.campusmarketplace.model.RefreshToken;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.repository.RefreshTokenRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private AuthRequest authRequest;
    private Authentication authentication;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.BUYER);
        testUser.setActive(true);
        
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");
        authRequest.setDeviceInfo("Test Device");
        
        authentication = mock(Authentication.class);
        // Remove the stubbing here - will be done in individual tests as needed
    }
    
    @Test
    void testSuccessfulLogin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtil.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(3600L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());
        
        // When
        AuthResponse response = authService.login(authRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(UserRole.BUYER, response.getRole());
        assertEquals("testuser", response.getUsername());
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), response.getUserId());
        
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
    
    @Test
    void testFailedLogin() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));
        
        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(authRequest);
        });
        
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
    
    @Test
    void testLoginWithInactiveUser() {
        // Given
        testUser.setActive(false);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        
        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(authRequest);
        });
    }
    
    @Test
    void testSuccessfulRefreshToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("valid-refresh-token");
        refreshToken.setUser(testUser);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        refreshToken.setRevoked(false);
        
        when(jwtUtil.validateToken("valid-refresh-token")).thenReturn(true);
        when(refreshTokenRepository.findByTokenAndIsRevokedFalse("valid-refresh-token"))
            .thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateAccessToken(testUser)).thenReturn("new-access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(3600L);
        
        // When
        AuthResponse response = authService.refreshToken(request);
        
        // Then
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("valid-refresh-token", response.getRefreshToken());
        assertEquals(UserRole.BUYER, response.getRole());
    }
    
    @Test
    void testRefreshTokenWithInvalidToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");
        
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);
        
        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(request);
        });
    }
    
    @Test
    void testRefreshTokenNotFound() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("non-existent-token");
        
        when(jwtUtil.validateToken("non-existent-token")).thenReturn(true);
        when(refreshTokenRepository.findByTokenAndIsRevokedFalse("non-existent-token"))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(request);
        });
    }
    
    @Test
    void testRefreshTokenExpired() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");
        
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-token");
        expiredToken.setUser(testUser);
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired
        expiredToken.setRevoked(false);
        
        when(jwtUtil.validateToken("expired-token")).thenReturn(true);
        when(refreshTokenRepository.findByTokenAndIsRevokedFalse("expired-token"))
            .thenReturn(Optional.of(expiredToken));
        
        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.refreshToken(request);
        });
        
        verify(refreshTokenRepository).delete(expiredToken);
    }
    
    @Test
    void testLogout() {
        // Given
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("token-to-revoke");
        refreshToken.setRevoked(false);
        
        when(refreshTokenRepository.findByToken("token-to-revoke"))
            .thenReturn(Optional.of(refreshToken));
        
        // When
        authService.logout("token-to-revoke");
        
        // Then
        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository).save(refreshToken);
    }
    
    @Test
    void testLogoutAllDevices() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        authService.logoutAllDevices("testuser");
        
        // Then
        verify(refreshTokenRepository).revokeAllTokensByUser(testUser);
    }
    
    @Test
    void testGetCurrentUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        User result = authService.getCurrentUser("testuser");
        
        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }
    
    @Test
    void testGetCurrentUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.getCurrentUser("nonexistent");
        });
    }
}
