package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.model.VerificationStatus;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.repository.LoginAttemptRepository;
import com.commandlinecommandos.campusmarketplace.repository.AuditLogRepository;
import com.commandlinecommandos.campusmarketplace.security.RequireRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin controller for user analytics and reporting
 */
@RestController
@RequestMapping("/api/admin/analytics")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, maxAge = 3600)
public class AdminAnalyticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAnalyticsController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Get user statistics dashboard
     */
    @GetMapping("/users")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> getUserStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Total users
            stats.put("totalUsers", userRepository.count());
            
            // Users by role
            Map<String, Long> usersByRole = new HashMap<>();
            usersByRole.put("BUYER", userRepository.countByRole(UserRole.BUYER));
            usersByRole.put("SELLER", userRepository.countByRole(UserRole.SELLER));
            usersByRole.put("ADMIN", userRepository.countByRole(UserRole.ADMIN));
            stats.put("usersByRole", usersByRole);
            
            // Users by verification status
            Map<String, Long> usersByStatus = new HashMap<>();
            usersByStatus.put("PENDING", userRepository.findByVerificationStatus(VerificationStatus.PENDING).size());
            usersByStatus.put("VERIFIED", userRepository.findByVerificationStatus(VerificationStatus.VERIFIED).size());
            usersByStatus.put("REJECTED", userRepository.findByVerificationStatus(VerificationStatus.REJECTED).size());
            usersByStatus.put("SUSPENDED", userRepository.findByVerificationStatus(VerificationStatus.SUSPENDED).size());
            stats.put("usersByStatus", usersByStatus);
            
            // Active vs inactive users
            stats.put("activeUsers", userRepository.findByIsActiveTrue().size());
            stats.put("inactiveUsers", userRepository.count() - userRepository.findByIsActiveTrue().size());
            
            // Registration trends (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long newUsersLast30Days = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();
            stats.put("newUsersLast30Days", newUsersLast30Days);
            
            // Recent registration trend (last 7 days)
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            long newUsersLast7Days = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(sevenDaysAgo))
                .count();
            stats.put("newUsersLast7Days", newUsersLast7Days);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting user statistics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve statistics", "message", e.getMessage()));
        }
    }
    
    /**
     * Get security analytics
     */
    @GetMapping("/security")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> getSecurityAnalytics() {
        try {
            Map<String, Object> security = new HashMap<>();
            
            // Failed login attempts (last 24 hours)
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            long failedLoginsLast24Hours = loginAttemptRepository.findAll().stream()
                .filter(la -> !la.isSuccess() && la.getCreatedAt().isAfter(last24Hours))
                .count();
            security.put("failedLoginsLast24Hours", failedLoginsLast24Hours);
            
            // Successful logins (last 24 hours)
            long successfulLoginsLast24Hours = loginAttemptRepository.findAll().stream()
                .filter(la -> la.isSuccess() && la.getCreatedAt().isAfter(last24Hours))
                .count();
            security.put("successfulLoginsLast24Hours", successfulLoginsLast24Hours);
            
            // Security events by severity
            Map<String, Long> eventsBySeverity = new HashMap<>();
            eventsBySeverity.put("CRITICAL", auditLogRepository.countBySeverity(com.commandlinecommandos.campusmarketplace.model.AuditLog.Severity.CRITICAL));
            eventsBySeverity.put("ERROR", auditLogRepository.countBySeverity(com.commandlinecommandos.campusmarketplace.model.AuditLog.Severity.ERROR));
            eventsBySeverity.put("WARNING", auditLogRepository.countBySeverity(com.commandlinecommandos.campusmarketplace.model.AuditLog.Severity.WARNING));
            security.put("eventsBySeverity", eventsBySeverity);
            
            // Account actions (last 30 days)
            long suspensionsLast30Days = auditLogRepository.findAll().stream()
                .filter(al -> "SUSPENDED".equals(al.getAction()) && al.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30)))
                .count();
            security.put("suspensionsLast30Days", suspensionsLast30Days);
            
            return ResponseEntity.ok(security);
        } catch (Exception e) {
            logger.error("Error getting security analytics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve security analytics", "message", e.getMessage()));
        }
    }
    
    /**
     * Get activity analytics
     */
    @GetMapping("/activity")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> getActivityAnalytics() {
        try {
            Map<String, Object> activity = new HashMap<>();
            
            // Users who logged in last 7 days
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            long activeUsersLast7Days = userRepository.findAll().stream()
                .filter(u -> u.getLastLoginAt() != null && u.getLastLoginAt().isAfter(sevenDaysAgo))
                .count();
            activity.put("activeUsersLast7Days", activeUsersLast7Days);
            
            // Users who logged in last 30 days
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long activeUsersLast30Days = userRepository.findAll().stream()
                .filter(u -> u.getLastLoginAt() != null && u.getLastLoginAt().isAfter(thirtyDaysAgo))
                .count();
            activity.put("activeUsersLast30Days", activeUsersLast30Days);
            
            // Users who never logged in
            long neverLoggedIn = userRepository.findAll().stream()
                .filter(u -> u.getLastLoginAt() == null)
                .count();
            activity.put("neverLoggedIn", neverLoggedIn);
            
            return ResponseEntity.ok(activity);
        } catch (Exception e) {
            logger.error("Error getting activity analytics", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve activity analytics", "message", e.getMessage()));
        }
    }
    
    /**
     * Get comprehensive dashboard data
     */
    @GetMapping("/dashboard")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> getDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Get all analytics in one call
            dashboard.put("users", getUserStatistics().getBody());
            dashboard.put("security", getSecurityAnalytics().getBody());
            dashboard.put("activity", getActivityAnalytics().getBody());
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Error getting dashboard data", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve dashboard", "message", e.getMessage()));
        }
    }
}

