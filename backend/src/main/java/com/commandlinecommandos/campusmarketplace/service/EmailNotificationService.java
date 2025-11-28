package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.model.Message;
import com.commandlinecommandos.campusmarketplace.model.NotificationPreference;
import com.commandlinecommandos.campusmarketplace.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for sending email notifications
 * Sends notifications for new messages and important events
 * Consolidated from communication service
 */
@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final boolean emailNotificationsEnabled;
    private final String fromEmail;

    public EmailNotificationService(
            @Autowired(required = false) JavaMailSender mailSender,
            NotificationPreferenceRepository notificationPreferenceRepository,
            @Value("${app.email-notifications.enabled:false}") boolean emailNotificationsEnabled,
            @Value("${spring.mail.from:noreply@campusmarketplace.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.emailNotificationsEnabled = emailNotificationsEnabled && mailSender != null;
        this.fromEmail = fromEmail;

        if (mailSender == null) {
            logger.warn("JavaMailSender not available - email notifications will be disabled");
        }
    }

    /**
     * Send email notification for a new message
     * Checks user preferences before sending
     *
     * @param recipientId ID of the message recipient
     * @param message the message that was sent
     */
    @Async
    public void sendNewMessageNotification(Long recipientId, Message message) {
        if (!emailNotificationsEnabled) {
            logger.debug("Email notifications are disabled globally");
            return;
        }

        try {
            Optional<NotificationPreference> preferenceOpt =
                notificationPreferenceRepository.findByUserId(recipientId);

            if (preferenceOpt.isEmpty()) {
                logger.debug("No notification preferences found for user {}", recipientId);
                return;
            }

            NotificationPreference preference = preferenceOpt.get();

            // Check if user has email notifications enabled
            if (preference.getEmailNotificationsEnabled() == null || !preference.getEmailNotificationsEnabled()) {
                logger.debug("Email notifications disabled for user {}", recipientId);
                return;
            }

            // Check if email is set
            if (preference.getEmail() == null || preference.getEmail().isEmpty()) {
                logger.warn("No email address set for user {}", recipientId);
                return;
            }

            // Send the email
            sendEmail(
                preference.getEmail(),
                "New Message on Campus Marketplace",
                buildMessageNotificationBody(preference.getFirstName(), message)
            );

            logger.info("Sent new message notification to user {}", recipientId);

        } catch (Exception e) {
            logger.error("Failed to send new message notification to user {}: {}",
                recipientId, e.getMessage(), e);
        }
    }

    /**
     * Send a generic email notification
     *
     * @param recipientId ID of the recipient
     * @param subject email subject
     * @param body email body
     */
    @Async
    public void sendNotification(Long recipientId, String subject, String body) {
        if (!emailNotificationsEnabled) {
            logger.debug("Email notifications are disabled globally");
            return;
        }

        try {
            Optional<NotificationPreference> preferenceOpt =
                notificationPreferenceRepository.findByUserId(recipientId);

            if (preferenceOpt.isEmpty()) {
                logger.debug("No notification preferences found for user {}", recipientId);
                return;
            }

            NotificationPreference preference = preferenceOpt.get();

            if (preference.getEmailNotificationsEnabled() == null || !preference.getEmailNotificationsEnabled()) {
                logger.debug("Email notifications disabled for user {}", recipientId);
                return;
            }

            if (preference.getEmail() == null || preference.getEmail().isEmpty()) {
                logger.warn("No email address set for user {}", recipientId);
                return;
            }

            sendEmail(preference.getEmail(), subject, body);
            logger.info("Sent notification to user {}: {}", recipientId, subject);

        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}",
                recipientId, e.getMessage(), e);
        }
    }

    /**
     * Send email directly to an email address (bypasses preferences)
     * Use for critical notifications like password resets
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param body email body
     */
    @Async
    public void sendEmail(String toEmail, String subject, String body) {
        if (!emailNotificationsEnabled || mailSender == null) {
            logger.debug("Email notifications are disabled or mail sender not available");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Email sent successfully to {}", toEmail);

        } catch (MailException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Build email body for new message notification
     */
    private String buildMessageNotificationBody(String recipientName, Message message) {
        return String.format(
            "Hi %s,\n\n" +
            "You have a new message on Campus Marketplace:\n\n" +
            "\"%s\"\n\n" +
            "Log in to Campus Marketplace to view and respond to this message.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team\n\n" +
            "---\n" +
            "To stop receiving these notifications, update your notification preferences in your account settings.",
            recipientName != null ? recipientName : "there",
            truncateMessage(message.getContent(), 200)
        );
    }

    /**
     * Truncate message content for email preview
     */
    private String truncateMessage(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * Send welcome email to new user
     *
     * @param email user's email address
     * @param firstName user's first name
     * @param username user's username
     */
    @Async
    public void sendWelcomeEmail(String email, String firstName, String username) {
        String subject = "Welcome to Campus Marketplace!";
        String body = String.format(
            "Hi %s,\n\n" +
            "Welcome to Campus Marketplace! Your account has been created successfully.\n\n" +
            "Username: %s\n\n" +
            "You can now:\n" +
            "- Browse and search for items\n" +
            "- Create listings to sell your items\n" +
            "- Chat with other students\n" +
            "- Manage your profile and preferences\n\n" +
            "Get started by logging in at our platform.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            firstName != null ? firstName : "there",
            username
        );

        sendEmail(email, subject, body);
    }

    /**
     * Send listing notification email
     *
     * @param sellerId ID of the seller
     * @param listingTitle title of the listing
     * @param eventType type of event (e.g., "created", "sold", "flagged")
     */
    @Async
    public void sendListingNotification(Long sellerId, String listingTitle, String eventType) {
        String subject = String.format("Your listing \"%s\" has been %s", listingTitle, eventType);
        String body = String.format(
            "Hi,\n\n" +
            "Your listing \"%s\" has been %s.\n\n" +
            "Log in to Campus Marketplace to view details.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            listingTitle,
            eventType
        );

        sendNotification(sellerId, subject, body);
    }
}
