package com.commandlinecommandos.campusmarketplace.communication.service;

import com.commandlinecommandos.campusmarketplace.communication.model.Conversation;
import com.commandlinecommandos.campusmarketplace.communication.model.Message;
import com.commandlinecommandos.campusmarketplace.communication.repository.ConversationRepository;
import com.commandlinecommandos.campusmarketplace.communication.repository.MessageRepository;
import com.commandlinecommandos.campusmarketplace.exception.ConversationNotFoundException;
import com.commandlinecommandos.campusmarketplace.exception.UnauthorizedAccessException;
import com.commandlinecommandos.campusmarketplace.exception.ConversationException;
import com.commandlinecommandos.campusmarketplace.service.ListingsService;
import com.commandlinecommandos.campusmarketplace.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ListingsService listingsService;

    @Autowired(required = false)
    private EmailNotificationService emailNotificationService;

    /**
     * Creates or gets an existing conversation for a listing between buyer and seller.
     * 
     * @param listingId The listing ID
     * @param buyerId The buyer's user ID
     * @return The conversation
     */
    @Transactional
    public Conversation getOrCreateConversation(UUID listingId, UUID buyerId) {
        // Verify listing exists and get seller ID
        Product listing = listingsService.getListingById(listingId);
        if (listing == null) {
            throw new ConversationException("Listing not found with ID: " + listingId);
        }
        
        UUID sellerId = listing.getSeller().getUserId();

        // Prevent users from messaging themselves
        if (buyerId.equals(sellerId)) {
            throw new ConversationException("Cannot create a conversation with yourself");
        }

        // Try to find existing conversation
        Optional<Conversation> existing = conversationRepository.findByListingIdAndBuyerIdAndSellerId(
            listingId, buyerId, sellerId
        );

        if (existing.isPresent()) {
            logger.info("Found existing conversation {} for listing {} between buyer {} and seller {}",
                existing.get().getConversationId(), listingId, buyerId, sellerId);
            return existing.get();
        }

        // Create new conversation
        Conversation conversation = new Conversation(listingId, buyerId, sellerId);
        conversation = conversationRepository.save(conversation);
        logger.info("Created new conversation {} for listing {} between buyer {} and seller {}",
            conversation.getConversationId(), listingId, buyerId, sellerId);
        
        return conversation;
    }

    /**
     * Sends a message in a conversation.
     * 
     * @param conversationId The conversation ID
     * @param senderId The sender's user ID
     * @param content The message content
     * @return The created message
     */
    @Transactional
    public Message sendMessage(UUID conversationId, UUID senderId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        // Verify sender is a participant
        if (!conversation.isParticipant(senderId)) {
            throw new UnauthorizedAccessException(
                "User " + senderId + " is not a participant in conversation " + conversationId
            );
        }

        Message message = new Message(conversation, senderId, content);
        message = messageRepository.save(message);
        
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        logger.info("Message {} sent in conversation {} by user {}", 
            message.getMessageId(), conversationId, senderId);
        
        if (emailNotificationService != null) {
            try {
                emailNotificationService.sendMessageNotification(conversation, message, senderId);
            } catch (Exception e) {
                logger.error("Failed to send email notification for message {}: {}", 
                    message.getMessageId(), e.getMessage(), e);
            }
        }
        
        return message;
    }

    /**
     * Sends a message to a listing (creates conversation if needed).
     * 
     * @param listingId The listing ID
     * @param buyerId The buyer's user ID
     * @param content The message content
     * @return The created message
     */
    @Transactional
    public Message sendMessageToListing(UUID listingId, UUID buyerId, String content) {
        Conversation conversation = getOrCreateConversation(listingId, buyerId);
        return sendMessage(conversation.getConversationId(), buyerId, content);
    }

    /**
     * Gets all messages in a conversation.
     * 
     * @param conversationId The conversation ID
     * @param userId The user ID requesting the messages (for authorization)
     * @return List of messages
     */
    @Transactional(readOnly = true)
    public List<Message> getMessages(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        // Verify user is a participant
        if (!conversation.isParticipant(userId)) {
            throw new UnauthorizedAccessException(
                "User " + userId + " is not a participant in conversation " + conversationId
            );
        }

        return messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * Gets a conversation with all its messages.
     * 
     * @param conversationId The conversation ID
     * @param userId The user ID requesting the conversation (for authorization)
     * @return The conversation with messages
     */
    @Transactional(readOnly = true)
    public Conversation getConversation(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        // Verify user is a participant
        if (!conversation.isParticipant(userId)) {
            throw new UnauthorizedAccessException(
                "User " + userId + " is not a participant in conversation " + conversationId
            );
        }

        // Load messages
        List<Message> messages = messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(conversationId);
        conversation.setMessages(messages);

        return conversation;
    }

    /**
     * Gets all conversations for a user.
     * 
     * @param userId The user ID
     * @return List of conversations
     */
    @Transactional(readOnly = true)
    public List<Conversation> getUserConversations(UUID userId) {
        return conversationRepository.findByParticipantId(userId);
    }

    /**
     * Marks messages in a conversation as read.
     * 
     * @param conversationId The conversation ID
     * @param userId The user ID marking messages as read
     * @return Number of messages marked as read
     */
    @Transactional
    public int markMessagesAsRead(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        // Verify user is a participant
        if (!conversation.isParticipant(userId)) {
            throw new UnauthorizedAccessException(
                "User " + userId + " is not a participant in conversation " + conversationId
            );
        }

        int count = messageRepository.markMessagesAsRead(conversationId, userId);
        logger.info("Marked {} messages as read in conversation {} for user {}", 
            count, conversationId, userId);
        
        return count;
    }

    /**
     * Gets the count of unread messages for a user in a conversation.
     * 
     * @param conversationId The conversation ID
     * @param userId The user ID
     * @return Count of unread messages
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        // Verify user is a participant
        if (!conversation.isParticipant(userId)) {
            throw new UnauthorizedAccessException(
                "User " + userId + " is not a participant in conversation " + conversationId
            );
        }

        return messageRepository.countUnreadMessages(conversationId, userId);
    }
    
    /**
     * Marks a single message as read.
     * 
     * @param messageId The message ID
     * @param userId The user ID marking message as read
     */
    @Transactional
    public void markSingleMessageAsRead(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        
        // Verify user is a participant in the conversation
        Conversation conversation = message.getConversation();
        if (!conversation.isParticipant(userId)) {
            throw new UnauthorizedAccessException(
                "User " + userId + " is not a participant in conversation " + conversation.getConversationId()
            );
        }
        
        // Only mark as read if user is not the sender
        if (!message.getSenderId().equals(userId) && !message.getIsRead()) {
            messageRepository.markSingleMessageAsRead(messageId, userId);
            logger.info("Marked message {} as read for user {}", messageId, userId);
        }
    }
}
