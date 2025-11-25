package com.commandlinecommandos.communication.service;

import com.commandlinecommandos.communication.model.Conversation;
import com.commandlinecommandos.communication.model.Message;
import com.commandlinecommandos.communication.model.NotificationPreference;
import com.commandlinecommandos.communication.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ListingService listingService;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private Conversation testConversation;
    private Message testMessage;
    private Long testListingId = 123L;
    private Long testBuyerId = 456L;
    private Long testSellerId = 789L;
    private String testListingTitle = "Test Listing";
    private String testFromEmail = "noreply@campusmarketplace.com";

    @BeforeEach
    void setUp() {
        testConversation = new Conversation(testListingId, testBuyerId, testSellerId);
        testConversation.setConversationId(1L);
        testConversation.setCreatedAt(LocalDateTime.now());
        testConversation.setUpdatedAt(LocalDateTime.now());

        testMessage = new Message(testConversation, testBuyerId, "Test message content");
        testMessage.setMessageId(1L);
        testMessage.setCreatedAt(LocalDateTime.now());

        // Set default configuration values
        ReflectionTestUtils.setField(emailNotificationService, "fromEmail", testFromEmail);
        ReflectionTestUtils.setField(emailNotificationService, "emailNotificationsEnabled", true);
    }

    @Test
    void testSendMessageNotification_Success() {
        // Arrange
        NotificationPreference preference = new NotificationPreference(
            testSellerId, true, "seller@example.com", "John"
        );
        when(preferenceRepository.findByUserId(testSellerId)).thenReturn(Optional.of(preference));
        when(listingService.getListingTitle(testListingId)).thenReturn(testListingTitle);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailNotificationService.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(preferenceRepository).findByUserId(testSellerId);
        verify(listingService, times(2)).getListingTitle(testListingId); // Called twice: for subject and body
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendMessageNotification_EmailNotificationsDisabled() {
        // Arrange
        ReflectionTestUtils.setField(emailNotificationService, "emailNotificationsEnabled", false);

        // Act
        emailNotificationService.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(preferenceRepository, never()).findByUserId(anyLong());
        verify(listingService, never()).getListingTitle(anyLong());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendMessageNotification_MailSenderNotConfigured() {
        // Arrange
        EmailNotificationService serviceWithoutMailSender = new EmailNotificationService();
        ReflectionTestUtils.setField(serviceWithoutMailSender, "listingService", listingService);
        ReflectionTestUtils.setField(serviceWithoutMailSender, "preferenceRepository", preferenceRepository);
        ReflectionTestUtils.setField(serviceWithoutMailSender, "fromEmail", testFromEmail);
        ReflectionTestUtils.setField(serviceWithoutMailSender, "emailNotificationsEnabled", true);
        // mailSender is null (not injected)

        // Act
        serviceWithoutMailSender.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(preferenceRepository, never()).findByUserId(anyLong());
        verify(listingService, never()).getListingTitle(anyLong());
    }

    @Test
    void testSendMessageNotification_RecipientNotificationsDisabled() {
        // Arrange
        NotificationPreference preference = new NotificationPreference(
            testSellerId, false, "seller@example.com", "John"
        );
        when(preferenceRepository.findByUserId(testSellerId)).thenReturn(Optional.of(preference));

        // Act
        emailNotificationService.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(preferenceRepository).findByUserId(testSellerId);
        verify(listingService, never()).getListingTitle(anyLong());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendMessageNotification_NoPreferenceFound() {
        // Arrange
        when(preferenceRepository.findByUserId(testSellerId)).thenReturn(Optional.empty());

        // Act
        emailNotificationService.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(preferenceRepository).findByUserId(testSellerId);
        verify(listingService, never()).getListingTitle(anyLong());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendMessageNotification_RecipientIsNull() {
        // Arrange
        // Create a conversation where getOtherParticipant returns null
        // This happens when the userId is neither buyer nor seller
        Conversation conversation = new Conversation(testListingId, testBuyerId, testSellerId);
        Long nonParticipantId = 999L; // User who is not a participant

        // Act
        emailNotificationService.sendMessageNotification(conversation, testMessage, nonParticipantId);

        // Assert
        verify(preferenceRepository, never()).findByUserId(anyLong());
        verify(listingService, never()).getListingTitle(anyLong());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendMessageNotification_MailException() {
        // Arrange
        NotificationPreference preference = new NotificationPreference(
            testSellerId, true, "seller@example.com", "John"
        );
        when(preferenceRepository.findByUserId(testSellerId)).thenReturn(Optional.of(preference));
        when(listingService.getListingTitle(testListingId)).thenReturn(testListingTitle);
        doThrow(new MailException("Mail server error") {}).when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailNotificationService.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(preferenceRepository).findByUserId(testSellerId);
        verify(listingService, times(2)).getListingTitle(testListingId); // Called twice: for subject and body
        verify(mailSender).send(any(SimpleMailMessage.class));
        // Should not throw exception, just log error
    }

    @Test
    void testSendMessageNotification_SellerSendsToBuyer() {
        // Arrange
        NotificationPreference preference = new NotificationPreference(
            testBuyerId, true, "buyer@example.com", "Jane"
        );
        when(preferenceRepository.findByUserId(testBuyerId)).thenReturn(Optional.of(preference));
        when(listingService.getListingTitle(testListingId)).thenReturn(testListingTitle);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        Message sellerMessage = new Message(testConversation, testSellerId, "Reply from seller");
        sellerMessage.setMessageId(2L);

        // Act
        emailNotificationService.sendMessageNotification(testConversation, sellerMessage, testSellerId);

        // Assert
        verify(preferenceRepository).findByUserId(testBuyerId);
        verify(listingService, times(2)).getListingTitle(testListingId); // Called twice: for subject and body
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendMessageNotification_NoFirstName() {
        // Arrange
        NotificationPreference preference = new NotificationPreference(
            testSellerId, true, "seller@example.com", null
        );
        when(preferenceRepository.findByUserId(testSellerId)).thenReturn(Optional.of(preference));
        when(listingService.getListingTitle(testListingId)).thenReturn(testListingTitle);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailNotificationService.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(preferenceRepository).findByUserId(testSellerId);
        verify(listingService, times(2)).getListingTitle(testListingId); // Called twice: for subject and body
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendMessageNotification_EmptyFirstName() {
        // Arrange
        NotificationPreference preference = new NotificationPreference(
            testSellerId, true, "seller@example.com", "   "
        );
        when(preferenceRepository.findByUserId(testSellerId)).thenReturn(Optional.of(preference));
        when(listingService.getListingTitle(testListingId)).thenReturn(testListingTitle);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailNotificationService.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(preferenceRepository).findByUserId(testSellerId);
        verify(listingService, times(2)).getListingTitle(testListingId); // Called twice: for subject and body
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void testBuildSubject() {
        // Arrange
        NotificationPreference preference = new NotificationPreference(
            testSellerId, true, "seller@example.com", "John"
        );
        when(preferenceRepository.findByUserId(testSellerId)).thenReturn(Optional.of(preference));
        when(listingService.getListingTitle(testListingId)).thenReturn(testListingTitle);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailNotificationService.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(listingService, times(2)).getListingTitle(testListingId); // Called twice: for subject and body
        verify(mailSender).send(argThat((SimpleMailMessage email) -> 
            email.getSubject().equals("New message about your " + testListingTitle + " listing")
        ));
    }

    @Test
    void testBuildMessageBody() {
        // Arrange
        NotificationPreference preference = new NotificationPreference(
            testSellerId, true, "seller@example.com", "John"
        );
        when(preferenceRepository.findByUserId(testSellerId)).thenReturn(Optional.of(preference));
        when(listingService.getListingTitle(testListingId)).thenReturn(testListingTitle);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailNotificationService.sendMessageNotification(testConversation, testMessage, testBuyerId);

        // Assert
        verify(listingService, times(2)).getListingTitle(testListingId); // Called twice: for subject and body
        verify(mailSender).send(argThat((SimpleMailMessage email) -> {
            String body = email.getText();
            return body.contains("Hi John,") &&
                   body.contains(testListingTitle) &&
                   body.contains("Test message content") &&
                   body.contains("Campus Marketplace Team");
        }));
    }
}

