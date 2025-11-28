package com.commandlinecommandos.campusmarketplace.communication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Entity
@Table(name = "conversations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"listing_id", "buyer_id", "seller_id"}))
public class Conversation {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "conversation_id", updatable = false, nullable = false)
    private UUID conversationId;
    
    @Column(name = "listing_id", nullable = false)
    private UUID listingId;
    
    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;
    
    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<Message> messages = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public Conversation() {
    }
    
    public Conversation(UUID listingId, UUID buyerId, UUID sellerId) {
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
    }
    
    public UUID getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }
    
    public UUID getListingId() {
        return listingId;
    }
    
    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }
    
    public UUID getBuyerId() {
        return buyerId;
    }
    
    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }
    
    public UUID getSellerId() {
        return sellerId;
    }
    
    public void setSellerId(UUID sellerId) {
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
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public void addMessage(Message message) {
        this.messages.add(message);
        message.setConversation(this);
    }
    
    public boolean isParticipant(UUID userId) {
        return buyerId.equals(userId) || sellerId.equals(userId);
    }
    
    public UUID getOtherParticipant(UUID userId) {
        if (buyerId.equals(userId)) {
            return sellerId;
        } else if (sellerId.equals(userId)) {
            return buyerId;
        }
        return null;
    }
}
