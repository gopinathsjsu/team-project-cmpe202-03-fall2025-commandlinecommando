package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.CreateMessageRequest;
import com.commandlinecommandos.campusmarketplace.dto.MessageDTO;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import com.commandlinecommandos.campusmarketplace.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Message endpoints
 * Handles sending and retrieving messages within conversations
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    @Autowired
    public MessageController(MessageService messageService, JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get all unread messages for the current user
     * GET /api/messages/unread
     * NOTE: This must come BEFORE /{conversationId} to avoid path matching conflicts
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages(
            @RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        List<MessageDTO> messages = messageService.getUnreadMessages(userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Send a message in a conversation
     * POST /api/messages/{conversationId}
     */
    @PostMapping("/{conversationId}")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable Long conversationId,
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateMessageRequest request) {
        UUID senderId = extractUserIdFromToken(token);
        MessageDTO message = messageService.sendMessage(conversationId, senderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * Get all messages in a conversation
     * GET /api/messages/{conversationId}
     */
    @GetMapping("/{conversationId}")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(
            @PathVariable Long conversationId,
            @RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        List<MessageDTO> messages = messageService.getConversationMessages(conversationId, userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark messages as read in a conversation
     * PUT /api/messages/{conversationId}/mark-read
     */
    @PutMapping("/{conversationId}/mark-read")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long conversationId,
            @RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        messageService.markMessagesAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a message
     * DELETE /api/messages/{messageId}
     */
    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        messageService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        return UUID.fromString(jwtUtil.extractClaim(jwt, claims -> claims.get("userId", String.class)));
    }
}
