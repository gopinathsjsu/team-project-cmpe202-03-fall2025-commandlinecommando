package com.commandlinecommandos.campusmarketplace.communication.dto;

import com.commandlinecommandos.campusmarketplace.communication.model.Conversation;
import java.time.LocalDateTime;

public class ConversationResponse {

    private String conversationId;
    private String listingId;
    private ListingSummary listing;
    private UserSummary buyer;
    private UserSummary seller;
    private MessageSummary lastMessage;
    private Long unreadCount;
    private LocalDateTime updatedAt;

    public ConversationResponse(Conversation conversation) {
        this.conversationId = conversation.getConversationId().toString();
        this.listingId = conversation.getListingId().toString();
        this.updatedAt = conversation.getUpdatedAt();

        if (conversation.getListing() != null) {
            this.listing = new ListingSummary(
                    conversation.getListing().getTitle(),
                    conversation.getListing().getPrice().doubleValue(),
                    "https://via.placeholder.com/150" // Placeholder until ProductImage entity is linked
            );
        }

        if (conversation.getBuyer() != null) {
            this.buyer = new UserSummary(
                    conversation.getBuyer().getUserId().toString(),
                    conversation.getBuyer().getUsername(),
                    conversation.getBuyer().getFirstName(),
                    conversation.getBuyer().getLastName(),
                    conversation.getBuyer().getAvatarUrl());
        }

        if (conversation.getSeller() != null) {
            this.seller = new UserSummary(
                    conversation.getSeller().getUserId().toString(),
                    conversation.getSeller().getUsername(),
                    conversation.getSeller().getFirstName(),
                    conversation.getSeller().getLastName(),
                    conversation.getSeller().getAvatarUrl());
        }

        if (conversation.getMessages() != null && !conversation.getMessages().isEmpty()) {
            // Get last message (list is ordered by createdAt ASC, so last element)
            com.commandlinecommandos.campusmarketplace.communication.model.Message lastMsg = conversation.getMessages()
                    .get(conversation.getMessages().size() - 1);

            this.lastMessage = new MessageSummary(
                    lastMsg.getContent(),
                    lastMsg.getCreatedAt().toString(),
                    lastMsg.getIsRead(),
                    lastMsg.getSenderId().toString());
        }
    }

    // Getters and Setters
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

    public ListingSummary getListing() {
        return listing;
    }

    public void setListing(ListingSummary listing) {
        this.listing = listing;
    }

    public UserSummary getBuyer() {
        return buyer;
    }

    public void setBuyer(UserSummary buyer) {
        this.buyer = buyer;
    }

    public UserSummary getSeller() {
        return seller;
    }

    public void setSeller(UserSummary seller) {
        this.seller = seller;
    }

    public MessageSummary getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageSummary lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Nested Classes
    public static class ListingSummary {
        public String title;
        public double price;
        public String imageUrl;

        public ListingSummary(String title, double price, String imageUrl) {
            this.title = title;
            this.price = price;
            this.imageUrl = imageUrl;
        }
    }

    public static class UserSummary {
        public String userId;
        public String username;
        public String firstName;
        public String lastName;
        public String avatarUrl;

        public UserSummary(String userId, String username, String firstName, String lastName, String avatarUrl) {
            this.userId = userId;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
            this.avatarUrl = avatarUrl;
        }
    }

    public static class MessageSummary {
        public String content;
        public String createdAt;
        public boolean isRead;
        public String senderId;

        public MessageSummary(String content, String createdAt, boolean isRead, String senderId) {
            this.content = content;
            this.createdAt = createdAt;
            this.isRead = isRead;
            this.senderId = senderId;
        }
    }
}
