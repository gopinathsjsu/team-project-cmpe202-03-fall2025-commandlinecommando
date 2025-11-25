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
import com.commandlinecommandos.campusmarketplace.model.University;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.model.VerificationStatus;
import com.commandlinecommandos.campusmarketplace.repository.RefreshTokenRepository;
import com.commandlinecommandos.campusmarketplace.repository.UniversityRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
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
    
    @Autowired(required = false)
    private LoginAttemptService loginAttemptService;
    
    @Autowired(required = false)
    private AuditService auditService;
    
    @Autowired
    private UniversityRepository universityRepository;
    
    // In-memory store for password reset tokens (for development)
    // In production, this should be stored in database with expiration
    private final Map<String, PasswordResetToken> resetTokens = new ConcurrentHashMap<>();
    
    private static class PasswordResetToken {
        String email;
        LocalDateTime expiresAt;
        
        PasswordResetToken(String email, LocalDateTime expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }
        
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
    
    public AuthResponse login(AuthRequest authRequest) throws AuthenticationException {
        String username = authRequest.getUsername();
        
        // Check if account is locked due to failed attempts
        if (loginAttemptService != null && loginAttemptService.isAccountLocked(username)) {
            int remainingTime = loginAttemptService.getRemainingLockoutTime(username);
            throw new BadCredentialsException(
                String.format("Account is temporarily locked due to multiple failed login attempts. Please try again in %d minutes.", remainingTime)
            );
        }
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    username, 
                    authRequest.getPassword()
                )
            );
            
            User user = (User) authentication.getPrincipal();
            
            // Check if user is active
            if (!user.isActive()) {
                if (loginAttemptService != null) {
                    loginAttemptService.recordFailedLogin(username, "Account is disabled");
                }
                throw new BadCredentialsException("Account is disabled");
            }
            
            // Check if user is suspended
            if (user.getVerificationStatus() == VerificationStatus.SUSPENDED) {
                if (loginAttemptService != null) {
                    loginAttemptService.recordFailedLogin(username, "Account is suspended");
                }
                throw new BadCredentialsException("Account is suspended");
            }
            
            // Record successful login
            if (loginAttemptService != null) {
                loginAttemptService.recordSuccessfulLogin(username);
            }
            
            // Update last login time
            user.recordLogin();
            userRepository.save(user);
            
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
            
            // Audit log
            if (auditService != null) {
                auditService.logLogin(user, true);
            }
            
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
            // Record failed login attempt
            if (loginAttemptService != null) {
                loginAttemptService.recordFailedLogin(username, "Invalid credentials");
            }
            
            // Audit log
            if (auditService != null) {
                auditService.logFailedLogin(username, e.getMessage());
            }
            
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
            
            // Audit log
            if (auditService != null) {
                auditService.logLogout(token.getUser());
            }
        });
    }
    
    @Transactional
    public void logoutAllDevices(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        userOpt.ifPresent(user -> {
            refreshTokenRepository.revokeAllTokensByUser(user);
            
            // Audit log
            if (auditService != null) {
                auditService.logAuditEvent(user, "AUTH", "LOGOUT_ALL_DEVICES", 
                    "User logged out from all devices");
            }
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
        
        // Create user with the unified User model
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());
        user.setRole(registerRequest.getRole());
        
        // Set student-specific fields if available
        if (registerRequest.getStudentId() != null) {
            user.setStudentId(registerRequest.getStudentId());
        }
        if (registerRequest.getMajor() != null) {
            user.setMajor(registerRequest.getMajor());
        }
        if (registerRequest.getGraduationYear() != null) {
            user.setGraduationYear(registerRequest.getGraduationYear());
        }
        
        // Set university based on email domain or use default
        University university = findOrCreateUniversityForEmail(registerRequest.getEmail());
        user.setUniversity(university);
        
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
    
    /**
     * Find or create university based on email domain
     */
    private University findOrCreateUniversityForEmail(String email) {
        // Extract domain from email
        String domain = email.substring(email.indexOf('@') + 1);
        
        // Try to find existing university by domain
        return universityRepository.findAll().stream()
            .filter(u -> domain.toLowerCase().contains(u.getName().toLowerCase()) ||
                        u.getName().toLowerCase().contains(domain.split("\\.")[0].toLowerCase()))
            .findFirst()
            .orElseGet(() -> {
                // Create default university if not found
                University defaultUniversity = universityRepository.findAll().stream()
                    .filter(u -> u.getName().equalsIgnoreCase("San Jose State University"))
                    .findFirst()
                    .orElse(null);
                
                if (defaultUniversity != null) {
                    return defaultUniversity;
                }
                
                // If no default exists, create one
                University newUniversity = new University();
                newUniversity.setName("San Jose State University");
                newUniversity.setDomain("sjsu.edu");
                return universityRepository.save(newUniversity);
            });
    }
    
    public String requestPasswordReset(String email) throws BadCredentialsException {
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists for security
            throw new BadCredentialsException("If an account exists with this email, a password reset link has been sent.");
        }
        
        User user = userOpt.get();
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        
        // Store token with expiration (1 hour)
        resetTokens.put(resetToken, new PasswordResetToken(email, LocalDateTime.now().plusHours(1)));
        
        // In production, send email with reset link
        // For development, we return the token directly
        logger.info("Password reset token generated for email: {}", email);
        
        return resetToken;
    }
    
    public void resetPassword(String token, String newPassword) throws BadCredentialsException {
        // Find token
        PasswordResetToken resetToken = resetTokens.get(token);
        if (resetToken == null || resetToken.isExpired()) {
            resetTokens.remove(token); // Clean up expired token
            throw new BadCredentialsException("Invalid or expired reset token");
        }
        
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(resetToken.email);
        if (userOpt.isEmpty()) {
            resetTokens.remove(token);
            throw new BadCredentialsException("User not found");
        }
        
        User user = userOpt.get();
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Remove used token
        resetTokens.remove(token);
        
        logger.info("Password reset successful for user: {}", user.getUsername());
    }
}
