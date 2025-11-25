package com.commandlinecommandos.communication.service;

import com.commandlinecommandos.communication.model.Conversation;
import com.commandlinecommandos.communication.model.Message;
import com.commandlinecommandos.communication.model.NotificationPreference;
import com.commandlinecommandos.communication.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for sending email notifications when messages are sent.
 */
@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private ListingService listingService;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Value("${spring.mail.from:noreply@campusmarketplace.com}")
    private String fromEmail;

    @Value("${app.email-notifications.enabled:true}")
    private boolean emailNotificationsEnabled;

    /**
     * Sends an email notification to the recipient when a new message is received.
     * Only sends if the recipient has email notifications enabled.
     * 
     * @param conversation The conversation
     * @param message The message that was sent
     * @param senderId The ID of the user who sent the message
     */
    @Transactional(readOnly = true)
    public void sendMessageNotification(Conversation conversation, Message message, Long senderId) {
        if (!emailNotificationsEnabled || mailSender == null) {
            logger.debug("Email notifications disabled or mail sender not configured");
            return;
        }

        // Determine recipient (the other participant)
        Long recipientId = conversation.getOtherParticipant(senderId);
        if (recipientId == null) {
            logger.warn("Could not determine recipient for conversation {}", conversation.getConversationId());
            return;
        }

        // Check if recipient has email notifications enabled
        Optional<NotificationPreference> preference = preferenceRepository.findByUserId(recipientId);
        boolean notificationsEnabled = preference.map(NotificationPreference::getEmailNotificationsEnabled).orElse(false);

        if (!notificationsEnabled) {
            logger.debug("User {} has email notifications disabled", recipientId);
            return;
        }

        // Get recipient email and firstname
        String recipientEmail = preference.map(NotificationPreference::getEmail).orElse(null);
        String recipientFirstName = preference.map(NotificationPreference::getFirstName).orElse(null);

        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(recipientEmail);
            email.setSubject(buildSubject(listingService.getListingTitle(conversation.getListingId())));
            email.setText(buildMessageBody(recipientFirstName, listingService.getListingTitle(conversation.getListingId()), message.getContent()));
            
            mailSender.send(email);
            logger.info("Email notification sent to {} for message {}", recipientEmail, message.getMessageId());
        } catch (MailException e) {
            logger.error("Failed to send email notification to user {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    /**
     * Builds the email subject line.
     */
    private String buildSubject(String listingTitle) {
        return String.format("New message about your %s listing", listingTitle);
    }

    /**
     * Builds the email message body.
     */
    private String buildMessageBody(String recipientFirstName, String listingTitle, String messageContent) {
        String recipient = recipientFirstName != null && !recipientFirstName.trim().isEmpty() ? recipientFirstName : "there";
        
        return String.format(
            "Hi %s,\n\n" +
            "You have received a new message about your %s listing:\n\n" +
            "\"%s\"\n\n" +
            "You can reply to this message by visiting the conversation in the Campus Marketplace.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            recipient, listingTitle, messageContent
        );
    }
}

