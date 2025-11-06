package com.commandlinecommandos.listingapi.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtHelper {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public Long extractUserIdFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        final String jwt = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(jwt)) {
            return null;
        }
        
        return jwtUtil.extractUserId(jwt);
    }

    public String extractRoleFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        final String jwt = authHeader.substring(7);

        if (!jwtUtil.validateToken(jwt)) {
            return null;
        }

        return jwtUtil.extractRole(jwt);
    }
}

