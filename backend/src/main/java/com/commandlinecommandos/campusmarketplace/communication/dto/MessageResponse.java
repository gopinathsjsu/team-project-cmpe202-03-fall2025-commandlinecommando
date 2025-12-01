package com.commandlinecommandos.campusmarketplace.communication.dto;

import com.commandlinecommandos.campusmarketplace.communication.model.Message;
import java.time.LocalDateTime;
import java.util.UUID;

public class MessageResponse {

    private String messageId;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public MessageResponse() {
    }

    public MessageResponse(Message message) {
        this.messageId = message.getMessageId().toString();
        this.conversationId = message.getConversation().getConversationId().toString();
        this.senderId = message.getSenderId().toString();

        if (message.getSender() != null) {
            String firstName = message.getSender().getFirstName();
            String lastName = message.getSender().getLastName();
            if (firstName != null && lastName != null) {
                this.senderName = firstName + " " + lastName;
            } else {
                this.senderName = message.getSender().getUsername();
            }
        } else {
            this.senderName = "Unknown User";
        }

        this.content = message.getContent();
        this.isRead = message.getIsRead();
        this.createdAt = message.getCreatedAt();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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
