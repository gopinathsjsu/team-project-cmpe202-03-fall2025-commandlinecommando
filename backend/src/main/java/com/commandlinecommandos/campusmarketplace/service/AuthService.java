package com.commandlinecommandos.campusmarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.commandlinecommandos.campusmarketplace.dto.AuthRequest;
import com.commandlinecommandos.campusmarketplace.dto.AuthResponse;
import com.commandlinecommandos.campusmarketplace.dto.RefreshTokenRequest;
import com.commandlinecommandos.campusmarketplace.dto.RegisterRequest;
import com.commandlinecommandos.campusmarketplace.model.RefreshToken;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.Student;
import com.commandlinecommandos.campusmarketplace.model.Admin;
import com.commandlinecommandos.campusmarketplace.model.AdminLevel;
import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.repository.RefreshTokenRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public AuthResponse login(AuthRequest authRequest) throws AuthenticationException {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(), 
                    authRequest.getPassword()
                )
            );
            
            User user = (User) authentication.getPrincipal();
            
            // Check if user is active
            if (!user.isActive()) {
                throw new BadCredentialsException("Account is disabled");
            }
            
            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshTokenValue = jwtUtil.generateRefreshToken(user);
            
            // Save refresh token to database
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken(refreshTokenValue);
            refreshToken.setUser(user);
            refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days
            refreshToken.setDeviceInfo(authRequest.getDeviceInfo());
            
            refreshTokenRepository.save(refreshToken);
            
            AuthResponse response = new AuthResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshTokenValue);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtUtil.getAccessTokenExpiration());
            response.setRole(user.getRole());
            response.setUsername(user.getUsername());
            response.setUserId(user.getUserId());
            return response;
            
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }
    
    public AuthResponse refreshToken(RefreshTokenRequest request) throws BadCredentialsException {
        String refreshTokenValue = request.getRefreshToken();
        
        // Validate refresh token format
        if (!jwtUtil.validateToken(refreshTokenValue)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        
        // Find refresh token in database
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository
            .findByTokenAndIsRevokedFalse(refreshTokenValue);
        
        if (refreshTokenOpt.isEmpty()) {
            throw new BadCredentialsException("Refresh token not found or revoked");
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        
        // Check if token is expired
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadCredentialsException("Refresh token expired");
        }
        
        User user = refreshToken.getUser();
        
        // Check if user is still active
        if (!user.isActive()) {
            throw new BadCredentialsException("Account is disabled");
        }
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user);
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshTokenValue); // Keep the same refresh token
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtUtil.getAccessTokenExpiration());
        response.setRole(user.getRole());
        response.setUsername(user.getUsername());
        response.setUserId(user.getUserId());
        return response;
    }
    
    public void logout(String refreshTokenValue) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenValue);
        refreshTokenOpt.ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
    
    @Transactional
    public void logoutAllDevices(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        userOpt.ifPresent(user -> {
            refreshTokenRepository.revokeAllTokensByUser(user);
        });
    }
    
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
    }
    
    // Cleanup expired tokens (can be called by scheduled task)
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
    }
    
    public AuthResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new BadCredentialsException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new BadCredentialsException("Email already exists");
        }
        
        // Create user based on role
        User user;
        if (registerRequest.getRole() == UserRole.STUDENT) {
            Student student = new Student();
            student.setUsername(registerRequest.getUsername());
            student.setEmail(registerRequest.getEmail());
            student.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            student.setFirstName(registerRequest.getFirstName());
            student.setLastName(registerRequest.getLastName());
            student.setPhone(registerRequest.getPhone());
            student.setStudentId(registerRequest.getStudentId());
            student.setMajor(registerRequest.getMajor());
            student.setGraduationYear(registerRequest.getGraduationYear());
            student.setCampusLocation(registerRequest.getCampusLocation());
            user = student;
        } else if (registerRequest.getRole() == UserRole.ADMIN) {
            Admin admin = new Admin();
            admin.setUsername(registerRequest.getUsername());
            admin.setEmail(registerRequest.getEmail());
            admin.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            admin.setFirstName(registerRequest.getFirstName());
            admin.setLastName(registerRequest.getLastName());
            admin.setPhone(registerRequest.getPhone());
            if (registerRequest.getAdminLevel() != null) {
                try {
                    admin.setAdminLevel(AdminLevel.valueOf(registerRequest.getAdminLevel()));
                } catch (IllegalArgumentException e) {
                    throw new BadCredentialsException("Invalid admin level: " + registerRequest.getAdminLevel());
                }
            }
            user = admin;
        } else {
            throw new BadCredentialsException("Invalid role");
        }
        
        // Save user to database
        user = userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshTokenValue = jwtUtil.generateRefreshToken(user);
        
        // Save refresh token to database
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days
        refreshToken.setDeviceInfo("Registration");
        
        refreshTokenRepository.save(refreshToken);
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshTokenValue);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtUtil.getAccessTokenExpiration());
        response.setRole(user.getRole());
        response.setUsername(user.getUsername());
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setActive(user.isActive());
        return response;
    }
}
