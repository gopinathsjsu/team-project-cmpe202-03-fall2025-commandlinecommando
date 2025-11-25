package com.commandlinecommandos.communication.service;

import com.commandlinecommandos.communication.model.Conversation;
import com.commandlinecommandos.communication.model.Message;
import com.commandlinecommandos.communication.repository.ConversationRepository;
import com.commandlinecommandos.communication.repository.MessageRepository;
import com.commandlinecommandos.communication.exception.ConversationNotFoundException;
import com.commandlinecommandos.communication.exception.UnauthorizedAccessException;
import com.commandlinecommandos.communication.exception.ConversationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ListingService listingService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private ChatService chatService;

    private Conversation testConversation;
    private Message testMessage;
    private Long testListingId = 123L;
    private Long testBuyerId = 456L;
    private Long testSellerId = 789L;

    @BeforeEach
    void setUp() {
        testConversation = new Conversation(testListingId, testBuyerId, testSellerId);
        testConversation.setConversationId(1L);
        testConversation.setCreatedAt(LocalDateTime.now());
        testConversation.setUpdatedAt(LocalDateTime.now());

        testMessage = new Message(testConversation, testBuyerId, "Test message content");
        testMessage.setMessageId(1L);
        testMessage.setCreatedAt(LocalDateTime.now());
    }

    // Test getOrCreateConversation method
    @Test
    void testGetOrCreateConversation_ExistingConversation() {
        // Arrange
        when(listingService.getSellerId(testListingId)).thenReturn(testSellerId);
        when(conversationRepository.findByListingIdAndBuyerIdAndSellerId(
            testListingId, testBuyerId, testSellerId))
            .thenReturn(Optional.of(testConversation));

        // Act
        Conversation result = chatService.getOrCreateConversation(testListingId, testBuyerId);

        // Assert
        assertNotNull(result);
        assertEquals(testConversation, result);
        assertEquals(testListingId, result.getListingId());
        assertEquals(testBuyerId, result.getBuyerId());
        assertEquals(testSellerId, result.getSellerId());
        verify(listingService).getSellerId(testListingId);
        verify(conversationRepository).findByListingIdAndBuyerIdAndSellerId(
            testListingId, testBuyerId, testSellerId);
        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void testGetOrCreateConversation_NewConversation() {
        // Arrange
        when(listingService.getSellerId(testListingId)).thenReturn(testSellerId);
        when(conversationRepository.findByListingIdAndBuyerIdAndSellerId(
            testListingId, testBuyerId, testSellerId))
            .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        // Act
        Conversation result = chatService.getOrCreateConversation(testListingId, testBuyerId);

        // Assert
        assertNotNull(result);
        assertEquals(testConversation, result);
        verify(listingService).getSellerId(testListingId);
        verify(conversationRepository).findByListingIdAndBuyerIdAndSellerId(
            testListingId, testBuyerId, testSellerId);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void testGetOrCreateConversation_ListingNotFound() {
        // Arrange
        when(listingService.getSellerId(testListingId)).thenReturn(null);

        // Act & Assert
        ConversationException exception = assertThrows(ConversationException.class, () -> {
            chatService.getOrCreateConversation(testListingId, testBuyerId);
        });

        assertEquals("Listing not found with ID: " + testListingId, exception.getMessage());
        verify(listingService).getSellerId(testListingId);
        verify(conversationRepository, never()).findByListingIdAndBuyerIdAndSellerId(
            anyLong(), anyLong(), anyLong());
    }

    @Test
    void testGetOrCreateConversation_CannotMessageYourself() {
        // Arrange
        when(listingService.getSellerId(testListingId)).thenReturn(testBuyerId);

        // Act & Assert
        ConversationException exception = assertThrows(ConversationException.class, () -> {
            chatService.getOrCreateConversation(testListingId, testBuyerId);
        });

        assertEquals("Cannot create a conversation with yourself", exception.getMessage());
        verify(listingService).getSellerId(testListingId);
        verify(conversationRepository, never()).findByListingIdAndBuyerIdAndSellerId(
            anyLong(), anyLong(), anyLong());
    }

    // Test sendMessage method
    @Test
    void testSendMessage_Success() {
        // Arrange
        String content = "Test message";
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        doNothing().when(emailNotificationService).sendMessageNotification(
            any(Conversation.class), any(Message.class), anyLong());

        // Act
        Message result = chatService.sendMessage(1L, testBuyerId, content);

        // Assert
        assertNotNull(result);
        assertEquals(testMessage, result);
        verify(conversationRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        verify(emailNotificationService).sendMessageNotification(
            eq(testConversation), eq(testMessage), eq(testBuyerId));
    }

    @Test
    void testSendMessage_ConversationNotFound() {
        // Arrange
        when(conversationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ConversationNotFoundException exception = assertThrows(ConversationNotFoundException.class, () -> {
            chatService.sendMessage(999L, testBuyerId, "Test message");
        });

        assertEquals("Conversation not found with ID: 999", exception.getMessage());
        verify(conversationRepository).findById(999L);
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void testSendMessage_UnauthorizedAccess() {
        // Arrange
        Long unauthorizedUserId = 999L;
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            chatService.sendMessage(1L, unauthorizedUserId, "Test message");
        });

        assertTrue(exception.getMessage().contains("is not a participant"));
        verify(conversationRepository).findById(1L);
        verify(messageRepository, never()).save(any(Message.class));
        verify(emailNotificationService, never()).sendMessageNotification(
            any(Conversation.class), any(Message.class), anyLong());
    }

    // Test sendMessageToListing method
    @Test
    void testSendMessageToListing_Success() {
        // Arrange
        String content = "Is this available?";
        when(listingService.getSellerId(testListingId)).thenReturn(testSellerId);
        when(conversationRepository.findByListingIdAndBuyerIdAndSellerId(
            testListingId, testBuyerId, testSellerId))
            .thenReturn(Optional.of(testConversation));
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        doNothing().when(emailNotificationService).sendMessageNotification(
            any(Conversation.class), any(Message.class), anyLong());

        // Act
        Message result = chatService.sendMessageToListing(testListingId, testBuyerId, content);

        // Assert
        assertNotNull(result);
        assertEquals(testMessage, result);
        verify(listingService).getSellerId(testListingId);
        verify(conversationRepository).findByListingIdAndBuyerIdAndSellerId(
            testListingId, testBuyerId, testSellerId);
        verify(messageRepository).save(any(Message.class));
        verify(emailNotificationService).sendMessageNotification(
            eq(testConversation), eq(testMessage), eq(testBuyerId));
    }

    // Test getMessages method
    @Test
    void testGetMessages_Success() {
        // Arrange
        List<Message> messages = Arrays.asList(testMessage);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(1L))
            .thenReturn(messages);

        // Act
        List<Message> result = chatService.getMessages(1L, testBuyerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage, result.get(0));
        verify(conversationRepository).findById(1L);
        verify(messageRepository).findByConversation_ConversationIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void testGetMessages_UnauthorizedAccess() {
        // Arrange
        Long unauthorizedUserId = 999L;
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            chatService.getMessages(1L, unauthorizedUserId);
        });

        assertTrue(exception.getMessage().contains("is not a participant"));
        verify(conversationRepository).findById(1L);
        verify(messageRepository, never()).findByConversation_ConversationIdOrderByCreatedAtAsc(anyLong());
    }

    // Test getConversation method
    @Test
    void testGetConversation_Success() {
        // Arrange
        List<Message> messages = Arrays.asList(testMessage);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(1L))
            .thenReturn(messages);

        // Act
        Conversation result = chatService.getConversation(1L, testBuyerId);

        // Assert
        assertNotNull(result);
        assertEquals(testConversation, result);
        assertNotNull(result.getMessages());
        assertEquals(1, result.getMessages().size());
        verify(conversationRepository).findById(1L);
        verify(messageRepository).findByConversation_ConversationIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void testGetConversation_EmptyMessages() {
        // Arrange
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(1L))
            .thenReturn(new ArrayList<>());

        // Act
        Conversation result = chatService.getConversation(1L, testBuyerId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMessages());
        assertTrue(result.getMessages().isEmpty());
        verify(conversationRepository).findById(1L);
        verify(messageRepository).findByConversation_ConversationIdOrderByCreatedAtAsc(1L);
    }

    // Test getUserConversations method
    @Test
    void testGetUserConversations_Success() {
        // Arrange
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(conversationRepository.findByParticipantId(testBuyerId)).thenReturn(conversations);

        // Act
        List<Conversation> result = chatService.getUserConversations(testBuyerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testConversation, result.get(0));
        verify(conversationRepository).findByParticipantId(testBuyerId);
    }

    @Test
    void testGetUserConversations_EmptyList() {
        // Arrange
        when(conversationRepository.findByParticipantId(testBuyerId)).thenReturn(new ArrayList<>());

        // Act
        List<Conversation> result = chatService.getUserConversations(testBuyerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(conversationRepository).findByParticipantId(testBuyerId);
    }

    // Test markMessagesAsRead method
    @Test
    void testMarkMessagesAsRead_Success() {
        // Arrange
        int expectedCount = 3;
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.markMessagesAsRead(1L, testBuyerId)).thenReturn(expectedCount);

        // Act
        int result = chatService.markMessagesAsRead(1L, testBuyerId);

        // Assert
        assertEquals(expectedCount, result);
        verify(conversationRepository).findById(1L);
        verify(messageRepository).markMessagesAsRead(1L, testBuyerId);
    }

    @Test
    void testMarkMessagesAsRead_NoUnreadMessages() {
        // Arrange
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.markMessagesAsRead(1L, testBuyerId)).thenReturn(0);

        // Act
        int result = chatService.markMessagesAsRead(1L, testBuyerId);

        // Assert
        assertEquals(0, result);
        verify(conversationRepository).findById(1L);
        verify(messageRepository).markMessagesAsRead(1L, testBuyerId);
    }

    @Test
    void testMarkMessagesAsRead_UnauthorizedAccess() {
        // Arrange
        Long unauthorizedUserId = 999L;
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            chatService.markMessagesAsRead(1L, unauthorizedUserId);
        });

        assertTrue(exception.getMessage().contains("is not a participant"));
        verify(conversationRepository).findById(1L);
        verify(messageRepository, never()).markMessagesAsRead(anyLong(), anyLong());
    }

    // Test getUnreadCount method
    @Test
    void testGetUnreadCount_Success() {
        // Arrange
        long expectedCount = 5L;
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.countUnreadMessages(1L, testBuyerId)).thenReturn(expectedCount);

        // Act
        long result = chatService.getUnreadCount(1L, testBuyerId);

        // Assert
        assertEquals(expectedCount, result);
        verify(conversationRepository).findById(1L);
        verify(messageRepository).countUnreadMessages(1L, testBuyerId);
    }

    @Test
    void testGetUnreadCount_Zero() {
        // Arrange
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.countUnreadMessages(1L, testBuyerId)).thenReturn(0L);

        // Act
        long result = chatService.getUnreadCount(1L, testBuyerId);

        // Assert
        assertEquals(0L, result);
        verify(conversationRepository).findById(1L);
        verify(messageRepository).countUnreadMessages(1L, testBuyerId);
    }

    @Test
    void testGetUnreadCount_UnauthorizedAccess() {
        // Arrange
        Long unauthorizedUserId = 999L;
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));

        // Act & Assert
        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () -> {
            chatService.getUnreadCount(1L, unauthorizedUserId);
        });

        assertTrue(exception.getMessage().contains("is not a participant"));
        verify(conversationRepository).findById(1L);
        verify(messageRepository, never()).countUnreadMessages(anyLong(), anyLong());
    }

    // Test edge cases
    @Test
    void testSendMessage_SellerCanSend() {
        // Arrange
        String content = "Yes, it's available!";
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        Message sellerMessage = new Message(testConversation, testSellerId, content);
        sellerMessage.setMessageId(2L);
        when(messageRepository.save(any(Message.class))).thenReturn(sellerMessage);
        doNothing().when(emailNotificationService).sendMessageNotification(
            any(Conversation.class), any(Message.class), anyLong());

        // Act
        Message result = chatService.sendMessage(1L, testSellerId, content);

        // Assert
        assertNotNull(result);
        assertEquals(testSellerId, result.getSenderId());
        verify(conversationRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        verify(emailNotificationService).sendMessageNotification(
            eq(testConversation), eq(sellerMessage), eq(testSellerId));
    }

    @Test
    void testSendMessage_EmailNotificationServiceNull() {
        // Arrange
        // Set emailNotificationService to null to test null handling
        org.springframework.test.util.ReflectionTestUtils.setField(
            chatService, "emailNotificationService", null);
        String content = "Test message";
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // Act
        Message result = chatService.sendMessage(1L, testBuyerId, content);

        // Assert
        assertNotNull(result);
        assertEquals(testMessage, result);
        // Email notification should not be called if service is null
        verify(conversationRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        // Reset the field for other tests
        org.springframework.test.util.ReflectionTestUtils.setField(
            chatService, "emailNotificationService", emailNotificationService);
    }

    @Test
    void testSendMessage_EmailNotificationThrowsException() {
        // Arrange
        String content = "Test message";
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);
        doThrow(new RuntimeException("Email service error")).when(emailNotificationService)
            .sendMessageNotification(any(Conversation.class), any(Message.class), anyLong());

        // Act
        Message result = chatService.sendMessage(1L, testBuyerId, content);

        // Assert
        // Should not throw exception, just log error
        assertNotNull(result);
        assertEquals(testMessage, result);
        verify(conversationRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        verify(emailNotificationService).sendMessageNotification(
            eq(testConversation), eq(testMessage), eq(testBuyerId));
    }

    @Test
    void testGetConversation_AsSeller() {
        // Arrange
        List<Message> messages = Arrays.asList(testMessage);
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(testConversation));
        when(messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(1L))
            .thenReturn(messages);

        // Act
        Conversation result = chatService.getConversation(1L, testSellerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isParticipant(testSellerId));
        verify(conversationRepository).findById(1L);
        verify(messageRepository).findByConversation_ConversationIdOrderByCreatedAtAsc(1L);
    }
}

