package com.commandlinecommandos.communication.service;

import com.commandlinecommandos.communication.model.NotificationPreference;
import com.commandlinecommandos.communication.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing notification preferences.
 */
@Service
public class NotificationPreferenceService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferenceService.class);

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    /**
     * Gets notification preferences for a user.
     * Creates default preferences if they don't exist.
     * 
     * @param userId The user ID
     * @return Notification preference (defaults to disabled)
     */
    @Transactional(readOnly = true)
    public NotificationPreference getPreference(Long userId) {
        Optional<NotificationPreference> preference = preferenceRepository.findByUserId(userId);
        if (preference.isPresent()) {
            return preference.get();
        }
        // Return default preference (disabled)
        return new NotificationPreference(userId, false, null, null);
    }

    /**
     * Updates notification preferences for a user.
     * Creates preferences if they don't exist.
     * 
     * @param userId The user ID
     * @param emailNotificationsEnabled Whether email notifications are enabled
     * @param email The email of the user
     * @param firstName The first name of the user
     * @return The updated or created preference
     */
    @Transactional
    public NotificationPreference updatePreference(Long userId, Boolean emailNotificationsEnabled, String email, String firstName) {
        Optional<NotificationPreference> existing = preferenceRepository.findByUserId(userId);
        
        NotificationPreference preference;
        if (existing.isPresent()) {
            preference = existing.get();
            preference.setEmailNotificationsEnabled(emailNotificationsEnabled);
            preference.setEmail(email);
            preference.setFirstName(firstName);
            logger.info("Updated notification preference for user {}: emailNotificationsEnabled={}, email={}, firstName={}", 
                userId, emailNotificationsEnabled, email, firstName);
        } else {
            preference = new NotificationPreference(userId, emailNotificationsEnabled, email, firstName);
            logger.info("Created notification preference for user {}: emailNotificationsEnabled={}, email={}, firstName={}", 
                userId, emailNotificationsEnabled, email, firstName);
        }
        
        return preferenceRepository.save(preference);
    }
}

