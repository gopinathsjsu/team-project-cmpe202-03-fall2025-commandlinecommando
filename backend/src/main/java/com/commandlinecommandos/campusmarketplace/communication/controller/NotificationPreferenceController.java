package com.commandlinecommandos.campusmarketplace.communication.controller;

import com.commandlinecommandos.campusmarketplace.communication.dto.NotificationPreferenceResponse;
import com.commandlinecommandos.campusmarketplace.communication.dto.UpdateNotificationPreferenceRequest;
import com.commandlinecommandos.campusmarketplace.communication.model.NotificationPreference;
import com.commandlinecommandos.campusmarketplace.communication.service.NotificationPreferenceService;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationPreferenceController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceController.class);

    @Autowired
    private NotificationPreferenceService preferenceService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get notification preferences for the current user
     * GET /api/notifications/preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        token = token.substring(7);
        
        UUID userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("User {} requesting notification preferences", userId);
        
        NotificationPreference preference = preferenceService.getPreference(userId);
        return ResponseEntity.ok(new NotificationPreferenceResponse(preference));
    }

    /**
     * Update notification preferences for the current user
     * PUT /api/notifications/preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @Valid @RequestBody UpdateNotificationPreferenceRequest request,
            HttpServletRequest httpRequest) {
        
        String token = httpRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        token = token.substring(7);
        
        UUID userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("User {} updating notification preferences: emailNotificationsEnabled={}, email={}, firstName={}", 
            userId, request.getEmailNotificationsEnabled(), request.getEmail(), request.getFirstName());
        
        NotificationPreference preference = preferenceService.updatePreference(
            userId, 
            request.getEmailNotificationsEnabled(),
            request.getEmail(),
            request.getFirstName()
        );
        
        return ResponseEntity.ok(new NotificationPreferenceResponse(preference));
    }
}
