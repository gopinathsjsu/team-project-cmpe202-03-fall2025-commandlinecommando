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

@Aspect
@Component
public class RoleAuthorizationAspect {
    
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Access denied: User not authenticated");
        }
        
        UserRole userRole = null;
        
        // Handle different types of principals (real User vs test UserDetails)
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            // Real authentication with our custom User
            User user = (User) principal;
            userRole = user.getRole();
        } else {
            // Test authentication with @WithMockUser - extract role from authorities
            String roleString = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElse(null);
                
            if (roleString != null) {
                try {
                    userRole = UserRole.valueOf(roleString);
                } catch (IllegalArgumentException e) {
                    throw new AccessDeniedException("Access denied: Invalid role '" + roleString + "'");
                }
            }
        }
        
        if (userRole == null) {
            throw new AccessDeniedException("Access denied: No valid role found");
        }
        
        UserRole[] requiredRoles = requireRole.value();
        boolean hasRequiredRole = Arrays.asList(requiredRoles).contains(userRole);
        
        if (!hasRequiredRole) {
            throw new AccessDeniedException(
                String.format("Access denied: User role '%s' is not authorized. Required roles: %s", 
                    userRole, Arrays.toString(requiredRoles))
            );
        }
    }
}
