package com.commandlinecommandos.campusmarketplace.communication.service;

import com.commandlinecommandos.campusmarketplace.communication.model.Conversation;
import com.commandlinecommandos.campusmarketplace.communication.model.Message;
import com.commandlinecommandos.campusmarketplace.communication.model.NotificationPreference;
import com.commandlinecommandos.campusmarketplace.communication.repository.NotificationPreferenceRepository;
import com.commandlinecommandos.campusmarketplace.service.EmailService;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for sending email notifications when messages are sent.
 * Uses the main EmailService for actual email delivery via SendGrid.
 */
@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.email-notifications.enabled:true}")
    private boolean emailNotificationsEnabled;

    /**
     * Sends an email notification to the recipient when a new message is received.
     * Sends by default unless user has explicitly disabled notifications.
     * Falls back to user's email from users table if no notification preference exists.
     * 
     * @param conversation The conversation
     * @param message The message that was sent
     * @param senderId The ID of the user who sent the message
     */
    @Transactional(readOnly = true)
    public void sendMessageNotification(Conversation conversation, Message message, UUID senderId) {
        if (!emailNotificationsEnabled || emailService == null) {
            logger.debug("Email notifications disabled or email service not configured");
            return;
        }

        // Determine recipient (the other participant)
        UUID recipientId = conversation.getOtherParticipant(senderId);
        if (recipientId == null) {
            logger.warn("Could not determine recipient for conversation {}", conversation.getConversationId());
            return;
        }

        // Get sender information
        Optional<User> senderOpt = userRepository.findById(senderId);
        String senderName = senderOpt.map(this::getDisplayName).orElse("Someone");

        // Get recipient user
        Optional<User> recipientOpt = userRepository.findById(recipientId);
        if (recipientOpt.isEmpty()) {
            logger.warn("Recipient user {} not found", recipientId);
            return;
        }
        User recipient = recipientOpt.get();

        // Check notification preferences (default to ENABLED if no preference exists)
        Optional<NotificationPreference> preference = preferenceRepository.findByUserId(recipientId);
        
        // Only skip if user has EXPLICITLY disabled notifications
        if (preference.isPresent() && !preference.get().getEmailNotificationsEnabled()) {
            logger.debug("User {} has explicitly disabled email notifications", recipientId);
            return;
        }

        // Get email: prefer notification_preferences email, fall back to user's email
        String recipientEmail = preference
            .map(NotificationPreference::getEmail)
            .filter(email -> email != null && !email.trim().isEmpty())
            .orElse(recipient.getEmail());
        
        String recipientFirstName = preference
            .map(NotificationPreference::getFirstName)
            .filter(name -> name != null && !name.trim().isEmpty())
            .orElse(recipient.getFirstName());

        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            logger.warn("No email address found for user {}", recipientId);
            return;
        }

        try {
            logger.info("Sending message notification to {} from {}", recipientEmail, senderName);
            emailService.sendMessageReceivedEmail(
                recipientEmail,
                recipientFirstName,
                senderName,
                message.getContent()
            );
            logger.info("✅ Message notification email sent to {} for message {} from {}", 
                recipientEmail, message.getMessageId(), senderName);
        } catch (Exception e) {
            logger.error("❌ Failed to send message notification email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    /**
     * Gets the display name for a user (firstName lastName or username).
     */
    private String getDisplayName(User user) {
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
                return user.getFirstName() + " " + user.getLastName();
            }
            return user.getFirstName();
        }
        return user.getUsername();
    }
}
