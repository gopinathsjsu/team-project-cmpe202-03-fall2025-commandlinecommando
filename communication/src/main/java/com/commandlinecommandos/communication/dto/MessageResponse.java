package com.commandlinecommandos.communication.dto;

import com.commandlinecommandos.communication.model.Message;
import java.time.LocalDateTime;

public class MessageResponse {
    
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    public MessageResponse() {
    }
    
    public MessageResponse(Message message) {
        this.messageId = message.getMessageId();
        this.conversationId = message.getConversation().getConversationId();
        this.senderId = message.getSenderId();
        this.content = message.getContent();
        this.isRead = message.getIsRead();
        this.createdAt = message.getCreatedAt();
    }
    
    public Long getMessageId() {
        return messageId;
    }
    
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
    
    public Long getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    
    public Long getSenderId() {
        return senderId;
    }
    
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

