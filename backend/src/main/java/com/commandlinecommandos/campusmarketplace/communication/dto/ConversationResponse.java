package com.commandlinecommandos.campusmarketplace.communication.dto;

import com.commandlinecommandos.campusmarketplace.communication.model.Conversation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConversationResponse {
    
    private String conversationId;
    private String listingId;
    private String buyerId;
    private String sellerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MessageResponse> messages;
    private Long unreadCount;
    
    public ConversationResponse() {
    }
    
    public ConversationResponse(Conversation conversation) {
        this.conversationId = conversation.getConversationId().toString();
        this.listingId = conversation.getListingId().toString();
        this.buyerId = conversation.getBuyerId().toString();
        this.sellerId = conversation.getSellerId().toString();
        this.createdAt = conversation.getCreatedAt();
        this.updatedAt = conversation.getUpdatedAt();
        if (conversation.getMessages() != null) {
            this.messages = conversation.getMessages().stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
        }
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public String getListingId() {
        return listingId;
    }
    
    public void setListingId(String listingId) {
        this.listingId = listingId;
    }
    
    public String getBuyerId() {
        return buyerId;
    }
    
    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }
    
    public String getSellerId() {
        return sellerId;
    }
    
    public void setSellerId(String sellerId) {
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
