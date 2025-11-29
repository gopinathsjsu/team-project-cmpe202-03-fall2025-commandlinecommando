package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.model.AuditLog;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for comprehensive audit logging
 * Tracks all user actions, admin operations, and security events
 */
@Service
@Transactional
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Log a generic audit event
     */
    @Async
    public void logAuditEvent(User user, String tableName, String action, String description) {
        AuditLog auditLog = new AuditLog(user, tableName, action, description);
        enrichWithRequestInfo(auditLog);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Log audit event with old and new values
     */
    @Async
    public void logAuditEvent(User user, String tableName, UUID recordId, String action, 
                              Map<String, Object> oldValues, Map<String, Object> newValues, String description) {
        AuditLog auditLog = new AuditLog(user, tableName, action, description);
        auditLog.setRecordId(recordId);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        enrichWithRequestInfo(auditLog);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Log security event with severity
     */
    @Async
    public void logSecurityEvent(User user, String action, String description, AuditLog.Severity severity) {
        AuditLog auditLog = new AuditLog(user, "SECURITY", action, description);
        auditLog.setSeverity(severity);
        enrichWithRequestInfo(auditLog);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Log user login
     */
    @Async
    public void logLogin(User user, boolean success) {
        String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        String description = success ? 
            "User logged in successfully" : 
            "User login failed";
        AuditLog.Severity severity = success ? 
            AuditLog.Severity.INFO : 
            AuditLog.Severity.WARNING;
        
        logSecurityEvent(user, action, description, severity);
    }
    
    /**
     * Log user logout
     */
    @Async
    public void logLogout(UUID userId, String username) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setTableName("AUTH");
        auditLog.setAction("LOGOUT");
        auditLog.setDescription("User logged out");
        auditLog.setSeverity(AuditLog.Severity.INFO);
        enrichWithRequestInfo(auditLog);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Log password change
     */
    @Async
    public void logPasswordChange(User user, boolean byAdmin) {
        String description = byAdmin ? 
            "Password changed by administrator" : 
            "User changed their password";
        logSecurityEvent(user, "PASSWORD_CHANGE", description, AuditLog.Severity.INFO);
    }
    
    /**
     * Log account status change
     */
    @Async
    public void logAccountStatusChange(User targetUser, User admin, String action, String reason) {
        String description = String.format(
            "Account %s by %s. Reason: %s",
            action.toLowerCase(),
            admin != null ? admin.getUsername() : "system",
            reason
        );
        
        AuditLog auditLog = new AuditLog(admin, "USER", action, description);
        auditLog.setRecordId(targetUser.getUserId());
        auditLog.setSeverity(AuditLog.Severity.WARNING);
        enrichWithRequestInfo(auditLog);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Log role change
     */
    @Async
    public void logRoleChange(User targetUser, User admin, String oldRole, String newRole) {
        String description = String.format(
            "User role changed from %s to %s by %s",
            oldRole,
            newRole,
            admin.getUsername()
        );
        
        AuditLog auditLog = new AuditLog(admin, "USER", "ROLE_CHANGE", description);
        auditLog.setRecordId(targetUser.getUserId());
        auditLog.setSeverity(AuditLog.Severity.WARNING);
        auditLog.setOldValues(Map.of("role", oldRole));
        auditLog.setNewValues(Map.of("role", newRole));
        enrichWithRequestInfo(auditLog);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Log failed login attempt
     */
    @Async
    public void logFailedLogin(String username, String reason) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setTableName("AUTH");
        auditLog.setAction("LOGIN_FAILED");
        auditLog.setDescription("Failed login attempt: " + reason);
        auditLog.setSeverity(AuditLog.Severity.WARNING);
        enrichWithRequestInfo(auditLog);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Log account lockout
     */
    @Async
    public void logAccountLockout(String username, int attemptCount) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setTableName("SECURITY");
        auditLog.setAction("ACCOUNT_LOCKED");
        auditLog.setDescription(String.format("Account locked after %d failed login attempts", attemptCount));
        auditLog.setSeverity(AuditLog.Severity.ERROR);
        enrichWithRequestInfo(auditLog);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Get audit logs for a user
     */
    public Page<AuditLog> getUserAuditLogs(User user, Pageable pageable) {
        return auditLogRepository.findByUser(user, pageable);
    }
    
    /**
     * Get audit logs by date range
     */
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable);
    }
    
    /**
     * Get audit logs by severity
     */
    public Page<AuditLog> getAuditLogsBySeverity(AuditLog.Severity severity, Pageable pageable) {
        return auditLogRepository.findBySeverity(severity, pageable);
    }
    
    /**
     * Get audit logs for a specific record
     */
    public java.util.List<AuditLog> getRecordHistory(UUID recordId) {
        return auditLogRepository.findByRecordId(recordId);
    }
    
    /**
     * Enrich audit log with request information (IP, user agent)
     */
    private void enrichWithRequestInfo(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // Ignore if request context is not available (e.g., in background tasks)
        }
    }
    
    /**
     * Get client IP address from request (handles proxies)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}

