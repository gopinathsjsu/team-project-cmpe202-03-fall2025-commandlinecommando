package com.commandlinecommandos.campusmarketplace.exception;

public class ConversationException extends RuntimeException {
    
    public ConversationException(String message) {
        super(message);
    }
    
    public ConversationException(String message, Throwable cause) {
        super(message, cause);
    }
}
