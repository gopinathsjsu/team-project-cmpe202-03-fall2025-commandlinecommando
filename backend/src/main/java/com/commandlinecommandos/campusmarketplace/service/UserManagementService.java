package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.dto.*;
import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comprehensive User Management Service
 * Handles profile management, password operations, admin user management, etc.
 */
@Service
@Transactional
public class UserManagementService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private AccountActionRepository accountActionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private VerificationTokenService verificationTokenService;
    
    private static final String TEMP_PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%";
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private final SecureRandom random = new SecureRandom();
    
    // ==================== Profile Management ====================
    
    /**
     * Get user profile
     */
    public UserResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        return mapToUserResponse(user);
    }
    
    /**
     * Update user profile
     */
    public UserResponse updateUserProfile(UUID userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();
        
        if (request.getFirstName() != null && !request.getFirstName().equals(user.getFirstName())) {
            oldValues.put("firstName", user.getFirstName());
            user.setFirstName(request.getFirstName());
            newValues.put("firstName", request.getFirstName());
        }
        
        if (request.getLastName() != null && !request.getLastName().equals(user.getLastName())) {
            oldValues.put("lastName", user.getLastName());
            user.setLastName(request.getLastName());
            newValues.put("lastName", request.getLastName());
        }
        
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            oldValues.put("phone", user.getPhone());
            user.setPhone(request.getPhone());
            newValues.put("phone", request.getPhone());
        }
        
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().equals(user.getAvatarUrl())) {
            oldValues.put("avatarUrl", user.getAvatarUrl());
            user.setAvatarUrl(request.getAvatarUrl());
            newValues.put("avatarUrl", request.getAvatarUrl());
        }
        
        // Update student-specific fields
        if (request.getStudentId() != null) {
            user.setStudentId(request.getStudentId());
        }
        if (request.getUniversityEmail() != null) {
            user.setUniversityEmail(request.getUniversityEmail());
        }
        if (request.getGraduationYear() != null) {
            user.setGraduationYear(request.getGraduationYear());
        }
        if (request.getMajor() != null) {
            user.setMajor(request.getMajor());
        }
        
        // Update preferences
        Map<String, Object> preferences = user.getPreferences();
        if (request.getNotifications() != null) {
            preferences.put("notifications", request.getNotifications());
        }
        if (request.getEmailUpdates() != null) {
            preferences.put("email_updates", request.getEmailUpdates());
        }
        if (request.getSearchRadiusMiles() != null) {
            preferences.put("search_radius_miles", request.getSearchRadiusMiles());
        }
        user.setPreferences(preferences);
        
        user = userRepository.save(user);
        
        // Audit log
        auditService.logAuditEvent(user, "USER", user.getUserId(), "UPDATE_PROFILE", 
            oldValues, newValues, "User updated their profile");
        
        return mapToUserResponse(user);
    }
    
    /**
     * Change user password
     */
    public void changePassword(UUID userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        
        // Verify new password and confirmation match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadCredentialsException("New password and confirmation do not match");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Send notification email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());
        
        // Audit log
        auditService.logPasswordChange(user, false);
    }
    
    /**
     * Initiate password reset (forgot password)
     */
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        // Always return success to prevent email enumeration
        if (userOpt.isEmpty()) {
            return;
        }
        
        User user = userOpt.get();
        
        // Create password reset token
        VerificationToken token = verificationTokenService.createPasswordResetToken(user);
        
        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token.getToken());
        
        // Audit log
        auditService.logSecurityEvent(user, "PASSWORD_RESET_REQUESTED", 
            "Password reset requested", AuditLog.Severity.INFO);
    }
    
    /**
     * Reset password with token
     */
    public void resetPassword(ResetPasswordRequest request) {
        // Validate token
        VerificationToken token = verificationTokenService.validateToken(request.getToken())
            .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));
        
        if (token.getTokenType() != VerificationToken.TokenType.PASSWORD_RESET) {
            throw new BadCredentialsException("Invalid token type");
        }
        
        // Verify password and confirmation match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadCredentialsException("Password and confirmation do not match");
        }
        
        User user = token.getUser();
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Mark token as used
        verificationTokenService.markTokenAsUsed(token);
        
        // Send notification email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getUsername());
        
        // Audit log
        auditService.logPasswordChange(user, false);
    }
    
    /**
     * Deactivate user account (soft delete with 30-day recovery)
     */
    public void deactivateAccount(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        user.setActive(false);
        userRepository.save(user);
        
        // Audit log
        auditService.logAuditEvent(user, "USER", "DEACTIVATE", 
            "User deactivated their account (30-day recovery period)");
    }
    
    // ==================== Admin User Management ====================
    
    /**
     * Search and filter users (admin only)
     */
    public PagedResponse<UserResponse> searchUsers(UserSearchRequest request) {
        Specification<User> spec = buildUserSpecification(request);
        
        Sort sort = Sort.by(
            "DESC".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
            request.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        
        List<UserResponse> users = userPage.getContent().stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
        
        return PagedResponse.<UserResponse>builder()
            .content(users)
            .page(userPage.getNumber())
            .size(userPage.getSize())
            .totalElements(userPage.getTotalElements())
            .totalPages(userPage.getTotalPages())
            .first(userPage.isFirst())
            .last(userPage.isLast())
            .empty(userPage.isEmpty())
            .build();
    }
    
    /**
     * Create user (admin only)
     */
    public UserResponse createUser(CreateUserRequest request, User admin) {
        // Check for existing username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadCredentialsException("Username already exists");
        }
        
        // Check for existing email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadCredentialsException("Email already exists");
        }
        
        // Get university
        University university = universityRepository.findById(request.getUniversityId())
            .orElseThrow(() -> new BadCredentialsException("University not found"));
        
        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();
        
        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRoles(request.getRoles());
        user.setUniversity(university);
        user.setStudentId(request.getStudentId());
        user.setGraduationYear(request.getGraduationYear());
        user.setMajor(request.getMajor());
        user.setActive(true);
        
        user = userRepository.save(user);
        
        // Send welcome email with temporary password
        if (request.isSendWelcomeEmail()) {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername(), temporaryPassword);
        }
        
        // Audit log
        auditService.logAuditEvent(admin, "USER", user.getUserId(), "CREATE", 
            null, Map.of("username", user.getUsername(), "roles", user.getRoles().toString()), 
            "Admin created new user account");
        
        return mapToUserResponse(user);
    }
    
    /**
     * Update user (admin only)
     */
    public UserResponse updateUser(UUID userId, UpdateUserRequest request, User admin) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        Map<String, Object> oldValues = new HashMap<>();
        Map<String, Object> newValues = new HashMap<>();
        
        if (request.getFirstName() != null) {
            oldValues.put("firstName", user.getFirstName());
            user.setFirstName(request.getFirstName());
            newValues.put("firstName", request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            oldValues.put("lastName", user.getLastName());
            user.setLastName(request.getLastName());
            newValues.put("lastName", request.getLastName());
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadCredentialsException("Email already exists");
            }
            oldValues.put("email", user.getEmail());
            user.setEmail(request.getEmail());
            newValues.put("email", request.getEmail());
        }
        
        if (request.getPhone() != null) {
            oldValues.put("phone", user.getPhone());
            user.setPhone(request.getPhone());
            newValues.put("phone", request.getPhone());
        }
        
        if (request.getRoles() != null && !request.getRoles().equals(user.getRoles())) {
            oldValues.put("roles", user.getRoles().toString());
            user.setRoles(request.getRoles());
            newValues.put("roles", request.getRoles().toString());
            
            // Log role change separately
            auditService.logRoleChange(user, admin, 
                (String) oldValues.get("roles"), 
                (String) newValues.get("roles"));
        }
        
        if (request.getVerificationStatus() != null) {
            oldValues.put("verificationStatus", user.getVerificationStatus());
            user.setVerificationStatus(request.getVerificationStatus());
            newValues.put("verificationStatus", request.getVerificationStatus());
        }
        
        if (request.getIsActive() != null) {
            oldValues.put("isActive", user.isActive());
            user.setActive(request.getIsActive());
            newValues.put("isActive", request.getIsActive());
        }
        
        if (request.getStudentId() != null) {
            user.setStudentId(request.getStudentId());
        }
        if (request.getGraduationYear() != null) {
            user.setGraduationYear(request.getGraduationYear());
        }
        if (request.getMajor() != null) {
            user.setMajor(request.getMajor());
        }
        
        user = userRepository.save(user);
        
        // Audit log
        auditService.logAuditEvent(admin, "USER", user.getUserId(), "UPDATE", 
            oldValues, newValues, "Admin updated user account");
        
        return mapToUserResponse(user);
    }
    
    /**
     * Suspend user account (admin only)
     */
    public void suspendUser(UUID userId, String reason, User admin) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        user.setVerificationStatus(VerificationStatus.SUSPENDED);
        user.setActive(false);
        userRepository.save(user);
        
        // Record account action
        AccountAction action = new AccountAction(user, admin, AccountAction.ActionType.SUSPEND, reason);
        accountActionRepository.save(action);
        
        // Send notification
        emailService.sendAccountSuspensionEmail(user.getEmail(), user.getUsername(), reason);
        
        // Audit log
        auditService.logAccountStatusChange(user, admin, "SUSPENDED", reason);
    }
    
    /**
     * Reactivate user account (admin only)
     */
    public void reactivateUser(UUID userId, User admin) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        user.setActive(true);
        userRepository.save(user);
        
        // Record account action
        AccountAction action = new AccountAction(user, admin, AccountAction.ActionType.REACTIVATE, "Account reactivated by admin");
        accountActionRepository.save(action);
        
        // Send notification
        emailService.sendAccountReactivationEmail(user.getEmail(), user.getUsername());
        
        // Audit log
        auditService.logAccountStatusChange(user, admin, "REACTIVATED", "Admin reactivated account");
    }
    
    /**
     * Delete user (admin only) - Soft delete
     */
    public void deleteUser(UUID userId, String reason, User admin) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
        
        user.setActive(false);
        user.setVerificationStatus(VerificationStatus.SUSPENDED);
        userRepository.save(user);
        
        // Record account action
        AccountAction action = new AccountAction(user, admin, AccountAction.ActionType.DELETE, reason);
        accountActionRepository.save(action);
        
        // Audit log
        auditService.logAccountStatusChange(user, admin, "DELETED", reason);
    }
    
    /**
     * Bulk user operations (admin only)
     */
    public Map<String, Object> bulkUserAction(BulkUserActionRequest request, User admin) {
        if (request.getUserIds().size() > 100) {
            throw new BadCredentialsException("Cannot perform bulk action on more than 100 users at once");
        }
        
        List<User> users = userRepository.findAllById(request.getUserIds());
        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (User user : users) {
            try {
                switch (request.getAction()) {
                    case ACTIVATE:
                        user.setActive(true);
                        userRepository.save(user);
                        auditService.logAccountStatusChange(user, admin, "ACTIVATED", "Bulk activation");
                        break;
                        
                    case DEACTIVATE:
                        user.setActive(false);
                        userRepository.save(user);
                        auditService.logAccountStatusChange(user, admin, "DEACTIVATED", "Bulk deactivation");
                        break;
                        
                    case UPDATE_ROLE:
                        if (request.getNewRole() != null) {
                            Set<UserRole> oldRoles = user.getRoles();
                            user.setRoles(new HashSet<>(Set.of(request.getNewRole())));
                            userRepository.save(user);
                            auditService.logRoleChange(user, admin, oldRoles.toString(), request.getNewRole().name());
                        }
                        break;
                        
                    case UPDATE_VERIFICATION:
                        if (request.getNewVerificationStatus() != null) {
                            user.setVerificationStatus(request.getNewVerificationStatus());
                            userRepository.save(user);
                            auditService.logAccountStatusChange(user, admin, "VERIFICATION_UPDATED", "Bulk verification update");
                        }
                        break;
                        
                    case SUSPEND:
                        suspendUser(user.getUserId(), request.getReason(), admin);
                        break;
                        
                    case DELETE:
                        deleteUser(user.getUserId(), request.getReason(), admin);
                        break;
                }
                successCount++;
            } catch (Exception e) {
                failureCount++;
                errors.add("User " + user.getUsername() + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalProcessed", request.getUserIds().size());
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("errors", errors);
        
        return result;
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Build JPA Specification for user search
     */
    private Specification<User> buildUserSpecification(UserSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Search term (username, email, first name, last name)
            if (request.getSearchTerm() != null && !request.getSearchTerm().isEmpty()) {
                String searchPattern = "%" + request.getSearchTerm().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(root.get("username")), searchPattern),
                    cb.like(cb.lower(root.get("email")), searchPattern),
                    cb.like(cb.lower(root.get("firstName")), searchPattern),
                    cb.like(cb.lower(root.get("lastName")), searchPattern)
                );
                predicates.add(searchPredicate);
            }
            
            // Role filter (uses join on roles collection)
            if (request.getRole() != null) {
                predicates.add(cb.isMember(request.getRole(), root.get("roles")));
            }
            
            // Verification status filter
            if (request.getVerificationStatus() != null) {
                predicates.add(cb.equal(root.get("verificationStatus"), request.getVerificationStatus()));
            }
            
            // Active status filter
            if (request.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), request.getIsActive()));
            }
            
            // Created date range
            if (request.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedAfter()));
            }
            if (request.getCreatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedBefore()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .avatarUrl(user.getAvatarUrl())
            .roles(user.getRoles())
            .verificationStatus(user.getVerificationStatus())
            .isActive(user.isActive())
            .lastLoginAt(user.getLastLoginAt())
            .emailVerifiedAt(user.getEmailVerifiedAt())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .studentId(user.getStudentId())
            .universityEmail(user.getUniversityEmail())
            .graduationYear(user.getGraduationYear())
            .major(user.getMajor())
            .universityId(user.getUniversity() != null ? user.getUniversity().getUniversityId() : null)
            .universityName(user.getUniversity() != null ? user.getUniversity().getName() : null)
            .preferences(user.getPreferences())
            .build();
    }
    
    /**
     * Generate temporary password
     */
    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(TEMP_PASSWORD_CHARS.charAt(random.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return password.toString();
    }
}

