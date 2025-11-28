package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.dto.CreateMessageRequest;
import com.commandlinecommandos.campusmarketplace.dto.MessageDTO;
import com.commandlinecommandos.campusmarketplace.model.Conversation;
import com.commandlinecommandos.campusmarketplace.model.Message;
import com.commandlinecommandos.campusmarketplace.repository.ConversationRepository;
import com.commandlinecommandos.campusmarketplace.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Autowired
    public MessageService(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    public MessageDTO sendMessage(Long conversationId, UUID senderId, CreateMessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        if (!conversation.isParticipant(senderId)) {
            throw new RuntimeException("Unauthorized: You are not a participant in this conversation");
        }

        Message message = new Message(conversation, senderId, request.getContent());
        Message saved = messageRepository.save(message);

        conversation.setUpdatedAt(saved.getCreatedAt());
        conversationRepository.save(conversation);

        return convertToDTO(saved);
    }

    public List<MessageDTO> getConversationMessages(Long conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        if (!conversation.isParticipant(userId)) {
            throw new RuntimeException("Unauthorized: You are not a participant in this conversation");
        }

        List<Message> messages = messageRepository
                .findByConversation_ConversationIdOrderByCreatedAtAsc(conversationId);

        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void markMessagesAsRead(Long conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        if (!conversation.isParticipant(userId)) {
            throw new RuntimeException("Unauthorized: You are not a participant in this conversation");
        }

        messageRepository.markMessagesAsRead(conversationId, userId);
    }

    public List<MessageDTO> getUnreadMessages(UUID userId) {
        List<Message> unreadMessages = messageRepository.findUnreadMessagesForUser(userId);
        return unreadMessages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteMessage(Long messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));

        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own messages");
        }

        messageRepository.delete(message);
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setMessageId(message.getMessageId());
        dto.setConversationId(message.getConversation().getConversationId());
        dto.setSenderId(message.getSenderId());
        dto.setContent(message.getContent());
        dto.setIsRead(message.getIsRead());
        dto.setSentAt(message.getCreatedAt());
        // TODO: Add senderName lookup once User ID type mismatch is resolved
        dto.setSenderName("User#" + message.getSenderId());
        return dto;
    }
}
