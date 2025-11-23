package com.commandlinecommandos.communication.exception;

public class ConversationNotFoundException extends RuntimeException {
    
    public ConversationNotFoundException(String message) {
        super(message);
    }
    
    public ConversationNotFoundException(Long conversationId) {
        super("Conversation not found with ID: " + conversationId);
    }
}

