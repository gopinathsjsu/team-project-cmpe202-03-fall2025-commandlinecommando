package com.commandlinecommandos.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {
    
    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
    private String content;
    
    public SendMessageRequest() {
    }
    
    public SendMessageRequest(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}

