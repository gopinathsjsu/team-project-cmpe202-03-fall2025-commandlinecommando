package com.commandlinecommandos.campusmarketplace.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserRole;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AOP Aspect for role-based authorization.
 * 
 * Supports many-to-many user-role relationship. A user can have multiple roles,
 * and authorization passes if the user has ANY of the required roles.
 */
@Aspect
@Component
public class RoleAuthorizationAspect {
    
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Access denied: User not authenticated");
        }
        
        Set<UserRole> userRoles = new HashSet<>();
        
        // Handle different types of principals (real User vs test UserDetails)
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            // Real authentication with our custom User
            User user = (User) principal;
            userRoles = user.getRoles();
        } else {
            // Test authentication with @WithMockUser - extract roles from authorities
            userRoles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .map(roleString -> {
                    try {
                        return UserRole.valueOf(roleString);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(role -> role != null)
                .collect(Collectors.toSet());
        }
        
        if (userRoles == null || userRoles.isEmpty()) {
            throw new AccessDeniedException("Access denied: No valid roles found");
        }
        
        UserRole[] requiredRoles = requireRole.value();
        Set<UserRole> requiredRoleSet = new HashSet<>(Arrays.asList(requiredRoles));
        
        // Check if user has ANY of the required roles (intersection)
        boolean hasRequiredRole = userRoles.stream()
            .anyMatch(requiredRoleSet::contains);
        
        if (!hasRequiredRole) {
            throw new AccessDeniedException(
                String.format("Access denied: User roles '%s' are not authorized. Required roles: %s", 
                    userRoles.stream().map(UserRole::name).collect(Collectors.joining(", ")), 
                    Arrays.toString(requiredRoles))
            );
        }
    }
}
