package com.commandlinecommandos.campusmarketplace.exception;

import java.util.UUID;

public class ConversationNotFoundException extends RuntimeException {
    
    public ConversationNotFoundException(String message) {
        super(message);
    }
    
    public ConversationNotFoundException(UUID conversationId) {
        super("Conversation not found with ID: " + conversationId);
    }
}
