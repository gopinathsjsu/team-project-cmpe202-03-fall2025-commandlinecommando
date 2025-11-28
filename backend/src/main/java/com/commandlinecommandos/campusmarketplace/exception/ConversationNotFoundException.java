package com.commandlinecommandos.campusmarketplace.exception;

/**
 * Exception thrown when a conversation is not found
 * Consolidated from communication service
 */
public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(Long conversationId) {
        super("Conversation with ID " + conversationId + " not found");
    }

    public ConversationNotFoundException(String message) {
        super(message);
    }
}
