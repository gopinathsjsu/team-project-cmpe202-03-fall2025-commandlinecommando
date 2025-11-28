package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.ConversationDTO;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import com.commandlinecommandos.campusmarketplace.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Conversation endpoints
 * Handles buyer-seller conversations about listings
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final JwtUtil jwtUtil;

    @Autowired
    public ConversationController(ConversationService conversationService, JwtUtil jwtUtil) {
        this.conversationService = conversationService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Create or get existing conversation for a listing
     * POST /api/conversations?listingId={id}&sellerId={id}
     */
    @PostMapping
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestHeader("Authorization") String token,
            @RequestParam Long listingId,
            @RequestParam UUID sellerId) {
        UUID buyerId = extractUserIdFromToken(token);
        ConversationDTO conversation = conversationService.getOrCreateConversation(listingId, buyerId, sellerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    /**
     * Get all conversations for the current user
     * GET /api/conversations
     */
    @GetMapping
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(
            @RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        List<ConversationDTO> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get a specific conversation by ID
     * GET /api/conversations/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ConversationDTO> getConversation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        ConversationDTO conversation = conversationService.getConversationById(id, userId);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Delete a conversation
     * DELETE /api/conversations/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        UUID userId = extractUserIdFromToken(token);
        conversationService.deleteConversation(id, userId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        return UUID.fromString(jwtUtil.extractClaim(jwt, claims -> claims.get("userId", String.class)));
    }
}
