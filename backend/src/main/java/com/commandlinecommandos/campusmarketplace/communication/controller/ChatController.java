package com.commandlinecommandos.campusmarketplace.communication.controller;

import com.commandlinecommandos.campusmarketplace.communication.dto.*;
import com.commandlinecommandos.campusmarketplace.communication.model.Conversation;
import com.commandlinecommandos.campusmarketplace.communication.model.Message;
import com.commandlinecommandos.campusmarketplace.communication.service.ChatService;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Send a message to a listing (creates conversation if needed)
     * POST /api/chat/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody CreateMessageRequest request,
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

        logger.info("User {} sending message to listing {}", userId, request.getListingId());

        Message message = chatService.sendMessageToListing(
                request.getListingId(),
                userId,
                request.getContent());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse(message));
    }

    /**
     * Send a message in an existing conversation
     * POST /api/chat/conversations/{conversationId}/messages
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageResponse> sendMessageInConversation(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request,
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

        logger.info("User {} sending message in conversation {}", userId, conversationId);

        Message message = chatService.sendMessage(
                conversationId,
                userId,
                request.getContent());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse(message));
    }

    /**
     * Get all conversations for the current user
     * GET /api/chat/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        token = token.substring(7);

        UUID userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("User {} requesting all conversations", userId);

        List<Conversation> conversations = chatService.getUserConversations(userId);

        List<ConversationResponse> responses = conversations.stream()
                .map(conv -> {
                    ConversationResponse response = new ConversationResponse(conv);
                    long unreadCount = chatService.getUnreadCount(conv.getConversationId(), userId);
                    response.setUnreadCount(unreadCount);
                    return response;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get a specific conversation with all messages
     * GET /api/chat/conversations/{conversationId}
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable UUID conversationId,
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

        logger.info("User {} requesting conversation {}", userId, conversationId);

        Conversation conversation = chatService.getConversation(conversationId, userId);
        ConversationResponse response = new ConversationResponse(conversation);

        // Set unread count
        long unreadCount = chatService.getUnreadCount(conversationId, userId);
        response.setUnreadCount(unreadCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Get messages in a conversation
     * GET /api/chat/conversations/{conversationId}/messages
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable UUID conversationId,
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

        logger.info("User {} requesting messages in conversation {}", userId, conversationId);

        List<Message> messages = chatService.getMessages(conversationId, userId);

        List<MessageResponse> responses = messages.stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Mark messages in a conversation as read
     * PUT /api/chat/conversations/{conversationId}/read
     */
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<MessageCountResponse> markAsRead(
            @PathVariable UUID conversationId,
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

        logger.info("User {} marking messages as read in conversation {}", userId, conversationId);

        int count = chatService.markMessagesAsRead(conversationId, userId);

        return ResponseEntity.ok(new MessageCountResponse(count));
    }

    /**
     * Get total unread message count for user across all conversations
     * GET /api/chat/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        token = token.substring(7);

        UUID userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("User {} getting total unread count", userId);

        // Get all conversations for user and sum up unread counts
        List<Conversation> conversations = chatService.getUserConversations(userId);
        long totalUnread = conversations.stream()
                .mapToLong(conv -> chatService.getUnreadCount(conv.getConversationId(), userId))
                .sum();

        return ResponseEntity.ok(new UnreadCountResponse(totalUnread));
    }

    /**
     * Mark a specific message as read
     * PUT /api/chat/messages/{messageId}/read
     */
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(
            @PathVariable UUID messageId,
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

        logger.info("User {} marking message {} as read", userId, messageId);

        chatService.markSingleMessageAsRead(messageId, userId);

        return ResponseEntity.ok().build();
    }

    /**
     * Get or create a conversation for a listing
     * GET /api/chat/conversations/listing/{listingId}
     */
    @GetMapping("/conversations/listing/{listingId}")
    public ResponseEntity<ConversationResponse> getOrCreateConversationForListing(
            @PathVariable UUID listingId,
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

        logger.info("User {} requesting conversation for listing {}", userId, listingId);

        Conversation conversation = chatService.getOrCreateConversation(listingId, userId);
        ConversationResponse response = new ConversationResponse(conversation);

        // Set unread count
        long unreadCount = chatService.getUnreadCount(conversation.getConversationId(), userId);
        response.setUnreadCount(unreadCount);

        return ResponseEntity.ok(response);
    }
}
