package com.commandlinecommandos.campusmarketplace.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

/**
 * Email Service for sending emails via SendGrid SMTP.
 * 
 * Handles all email notifications including:
 * - Listing creation confirmations
 * - Message notifications
 * - Listing rejection notifications
 * - Account-related emails
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.from:no-reply@seasonsanta.com}")
    private String fromEmail;
    
    @Value("${app.email-notifications.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${spring.mail.host:not-configured}")
    private String smtpHost;
    
    private static final String SUPPORT_EMAIL = "support@campusmarketplace.edu";
    
    @PostConstruct
    public void init() {
        if (mailSender != null) {
            logger.info("✅ EmailService initialized - SendGrid SMTP configured (host: {})", smtpHost);
            logger.info("📧 Email notifications enabled: {}, From: {}", emailEnabled, fromEmail);
        } else {
            logger.warn("⚠️  EmailService initialized - Mail sender NOT available (SMTP_PASSWORD may be missing)");
            logger.warn("📧 Email notifications will be logged but NOT sent");
        }
    }
    
    /**
     * Send email verification email
     */
    public void sendVerificationEmail(String to, String username, String token) {
        String verificationLink = buildVerificationLink(token);
        String subject = "Campus Marketplace - Email Verification";
        String body = buildVerificationEmailBody(username, verificationLink);
        
        sendEmail(to, subject, body);
        logger.info("Verification email sent to: {}", to);
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String username, String token) {
        String resetLink = buildPasswordResetLink(token);
        String subject = "Campus Marketplace - Password Reset Request";
        String body = buildPasswordResetEmailBody(username, resetLink);
        
        sendEmail(to, subject, body);
        logger.info("Password reset email sent to: {}", to);
        
        // Enhanced logging for development/testing
        logger.info("=====================================");
        logger.info("PASSWORD RESET TOKEN FOR TESTING");
        logger.info("Email: {}", to);
        logger.info("Token: {}", token);
        logger.info("Reset Link: {}", resetLink);
        logger.info("Token expires in 1 hour");
        logger.info("=====================================");
    }
    
    /**
     * Send welcome email with temporary password
     */
    public void sendWelcomeEmail(String to, String username, String temporaryPassword) {
        String subject = "Welcome to Campus Marketplace";
        String body = buildWelcomeEmailBody(username, temporaryPassword);
        
        sendEmail(to, subject, body);
        logger.info("Welcome email sent to: {}", to);
    }
    
    /**
     * Send account suspension notification
     */
    public void sendAccountSuspensionEmail(String to, String username, String reason) {
        String subject = "Campus Marketplace - Account Suspended";
        String body = buildAccountSuspensionEmailBody(username, reason);
        
        sendEmail(to, subject, body);
        logger.info("Account suspension email sent to: {}", to);
    }
    
    /**
     * Send account reactivation notification
     */
    public void sendAccountReactivationEmail(String to, String username) {
        String subject = "Campus Marketplace - Account Reactivated";
        String body = buildAccountReactivationEmailBody(username);
        
        sendEmail(to, subject, body);
        logger.info("Account reactivation email sent to: {}", to);
    }
    
    /**
     * Send login from new device notification
     */
    public void sendNewDeviceLoginEmail(String to, String username, String deviceInfo, String ipAddress) {
        String subject = "Campus Marketplace - New Device Login";
        String body = buildNewDeviceLoginEmailBody(username, deviceInfo, ipAddress);
        
        sendEmail(to, subject, body);
        logger.info("New device login email sent to: {}", to);
    }
    
    /**
     * Send password changed notification
     */
    public void sendPasswordChangedEmail(String to, String username) {
        String subject = "Campus Marketplace - Password Changed";
        String body = buildPasswordChangedEmailBody(username);
        
        sendEmail(to, subject, body);
        logger.info("Password changed email sent to: {}", to);
    }
    
    /**
     * Send notification when a new listing is created
     */
    public void sendListingCreatedEmail(String to, String username, String listingTitle) {
        String subject = "Your listing has been created - " + listingTitle;
        String body = buildListingCreatedEmailBody(username, listingTitle);
        sendEmail(to, subject, body);
        logger.info("Listing created email sent to: {}", to);
    }
    
    /**
     * Send notification when a message is received
     */
    public void sendMessageReceivedEmail(String to, String recipientName, String senderName, String messageContent) {
        String subject = "New message received from " + senderName;
        String body = buildMessageReceivedEmailBody(recipientName, senderName, messageContent);
        sendEmail(to, subject, body);
        logger.info("Message notification email sent to: {}", to);
    }
    
    /**
     * Send notification when a listing is rejected
     */
    public void sendListingRejectedEmail(String to, String username, String listingTitle, String reason) {
        String subject = "Your listing has been rejected - " + listingTitle;
        String body = buildListingRejectedEmailBody(username, listingTitle, reason);
        sendEmail(to, subject, body);
        logger.info("Listing rejected email sent to: {}", to);
    }
    
    /**
     * Core email sending method using SendGrid SMTP
     */
    private void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            logger.debug("Email notifications disabled, skipping email to: {}", to);
            return;
        }
        
        if (mailSender == null) {
            logger.warn("Mail sender not configured. Email to {} not sent. Subject: {}", to, subject);
            logger.info("=== EMAIL (NOT SENT - NO MAIL SENDER) ===");
            logger.info("To: {}", to);
            logger.info("From: {}", fromEmail);
            logger.info("Subject: {}", subject);
            logger.info("Body: {}", body);
            logger.info("==========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            logger.info("Email sent successfully to: {} with subject: {}", to, subject);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            // Log the email content for debugging
            logger.info("=== EMAIL (FAILED TO SEND) ===");
            logger.info("To: {}", to);
            logger.info("From: {}", fromEmail);
            logger.info("Subject: {}", subject);
            logger.info("==============================");
        }
    }
    
    // Email template builders
    
    private String buildVerificationLink(String token) {
        // TODO: Replace with actual frontend URL from configuration
        return "http://localhost:3000/verify-email?token=" + token;
    }
    
    private String buildPasswordResetLink(String token) {
        // TODO: Replace with actual frontend URL from configuration
        return "http://localhost:3000/reset-password?token=" + token;
    }
    
    private String buildVerificationEmailBody(String username, String verificationLink) {
        return String.format(
            "Hello %s,\n\n" +
            "Thank you for registering with Campus Marketplace!\n\n" +
            "Please verify your email address by clicking the link below:\n%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you did not create this account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            username, verificationLink
        );
    }
    
    private String buildPasswordResetEmailBody(String username, String resetLink) {
        return String.format(
            "Hello %s,\n\n" +
            "We received a request to reset your password.\n\n" +
            "Click the link below to reset your password:\n%s\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request a password reset, please ignore this email and ensure your account is secure.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            username, resetLink
        );
    }
    
    private String buildWelcomeEmailBody(String username, String temporaryPassword) {
        return String.format(
            "Hello %s,\n\n" +
            "Welcome to Campus Marketplace!\n\n" +
            "Your account has been created by an administrator.\n\n" +
            "Username: %s\n" +
            "Temporary Password: %s\n\n" +
            "Please log in and change your password immediately.\n\n" +
            "Login at: http://localhost:3000/login\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            username, username, temporaryPassword
        );
    }
    
    private String buildAccountSuspensionEmailBody(String username, String reason) {
        return String.format(
            "Hello %s,\n\n" +
            "Your Campus Marketplace account has been suspended.\n\n" +
            "Reason: %s\n\n" +
            "If you believe this is an error, please contact support at %s.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            username, reason, SUPPORT_EMAIL
        );
    }
    
    private String buildAccountReactivationEmailBody(String username) {
        return String.format(
            "Hello %s,\n\n" +
            "Great news! Your Campus Marketplace account has been reactivated.\n\n" +
            "You can now log in and continue using our services.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            username
        );
    }
    
    private String buildNewDeviceLoginEmailBody(String username, String deviceInfo, String ipAddress) {
        return String.format(
            "Hello %s,\n\n" +
            "We detected a login to your account from a new device:\n\n" +
            "Device: %s\n" +
            "IP Address: %s\n\n" +
            "If this was you, you can safely ignore this email.\n\n" +
            "If you did not log in from this device, please:\n" +
            "1. Change your password immediately\n" +
            "2. Contact support at %s\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            username, deviceInfo, ipAddress, SUPPORT_EMAIL
        );
    }
    
    private String buildPasswordChangedEmailBody(String username) {
        return String.format(
            "Hello %s,\n\n" +
            "Your password has been successfully changed.\n\n" +
            "If you did not make this change, please contact support immediately at %s.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            username, SUPPORT_EMAIL
        );
    }
    
    private String buildListingCreatedEmailBody(String username, String listingTitle) {
        return String.format(
            "Hello %s,\n\n" +
            "Your listing \"%s\" has been successfully created on Campus Marketplace!\n\n" +
            "Your listing is now visible to other users and they can contact you if interested.\n\n" +
            "You can manage your listings from your dashboard.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            username, listingTitle
        );
    }
    
    private String buildMessageReceivedEmailBody(String recipientName, String senderName, String messageContent) {
        String recipient = (recipientName != null && !recipientName.trim().isEmpty()) ? recipientName : "there";
        return String.format(
            "Hi %s,\n\n" +
            "You have received a new message from %s:\n\n" +
            "---\n" +
            "%s\n" +
            "---\n\n" +
            "Log in to Campus Marketplace to reply to this message.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            recipient, senderName, messageContent
        );
    }
    
    private String buildListingRejectedEmailBody(String username, String listingTitle, String reason) {
        String rejectionReason = (reason != null && !reason.trim().isEmpty()) 
            ? reason 
            : "The listing did not meet our marketplace guidelines.";
        return String.format(
            "Hello %s,\n\n" +
            "We regret to inform you that your listing \"%s\" has been rejected from Campus Marketplace.\n\n" +
            "Reason: %s\n\n" +
            "If you believe this was a mistake or have questions, please contact our support team at %s.\n\n" +
            "You can create a new listing that follows our guidelines.\n\n" +
            "Best regards,\n" +
            "Campus Marketplace Team",
            username, listingTitle, rejectionReason, SUPPORT_EMAIL
        );
    }
}

