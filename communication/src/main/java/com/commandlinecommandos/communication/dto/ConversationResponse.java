package com.commandlinecommandos.communication.dto;

import com.commandlinecommandos.communication.model.Conversation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ConversationResponse {
    
    private Long conversationId;
    private Long listingId;
    private Long buyerId;
    private Long sellerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MessageResponse> messages;
    private Long unreadCount;
    
    public ConversationResponse() {
    }
    
    public ConversationResponse(Conversation conversation) {
        this.conversationId = conversation.getConversationId();
        this.listingId = conversation.getListingId();
        this.buyerId = conversation.getBuyerId();
        this.sellerId = conversation.getSellerId();
        this.createdAt = conversation.getCreatedAt();
        this.updatedAt = conversation.getUpdatedAt();
        if (conversation.getMessages() != null) {
            this.messages = conversation.getMessages().stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
        }
    }
    
    public Long getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    
    public Long getListingId() {
        return listingId;
    }
    
    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }
    
    public Long getBuyerId() {
        return buyerId;
    }
    
    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }
    
    public Long getSellerId() {
        return sellerId;
    }
    
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<MessageResponse> getMessages() {
        return messages;
    }
    
    public void setMessages(List<MessageResponse> messages) {
        this.messages = messages;
    }
    
    public Long getUnreadCount() {
        return unreadCount;
    }
    
    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}

