package com.commandlinecommandos.communication.service;

import com.commandlinecommandos.communication.model.Conversation;
import com.commandlinecommandos.communication.model.Message;
import com.commandlinecommandos.communication.repository.ConversationRepository;
import com.commandlinecommandos.communication.repository.MessageRepository;
import com.commandlinecommandos.communication.exception.ConversationNotFoundException;
import com.commandlinecommandos.communication.exception.UnauthorizedAccessException;
import com.commandlinecommandos.communication.exception.ConversationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ListingService listingService;

    /**
     * Creates or gets an existing conversation for a listing between buyer and seller.
     * 
     * @param listingId The listing ID
     * @param buyerId The buyer's user ID
     * @return The conversation
     */
    @Transactional
    public Conversation getOrCreateConversation(Long listingId, Long buyerId) {
        // Verify listing exists and get seller ID
        Long sellerId = listingService.getSellerId(listingId);
        if (sellerId == null) {
            throw new ConversationException("Listing not found with ID: " + listingId);
        }

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
    public Message sendMessage(Long conversationId, Long senderId, String content) {
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
        
        logger.info("Message {} sent in conversation {} by user {}", 
            message.getMessageId(), conversationId, senderId);
        
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
    public Message sendMessageToListing(Long listingId, Long buyerId, String content) {
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
    public List<Message> getMessages(Long conversationId, Long userId) {
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
    public Conversation getConversation(Long conversationId, Long userId) {
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
    public List<Conversation> getUserConversations(Long userId) {
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
    public int markMessagesAsRead(Long conversationId, Long userId) {
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
    public long getUnreadCount(Long conversationId, Long userId) {
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
}

