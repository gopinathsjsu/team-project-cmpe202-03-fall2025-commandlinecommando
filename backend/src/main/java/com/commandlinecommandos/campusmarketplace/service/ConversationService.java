package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.dto.ConversationDTO;
import com.commandlinecommandos.campusmarketplace.model.Conversation;
import com.commandlinecommandos.campusmarketplace.model.Listing;
import com.commandlinecommandos.campusmarketplace.model.Message;
import com.commandlinecommandos.campusmarketplace.repository.ConversationRepository;
import com.commandlinecommandos.campusmarketplace.repository.ListingRepository;
import com.commandlinecommandos.campusmarketplace.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ListingRepository listingRepository;

    @Autowired
    public ConversationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ListingRepository listingRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.listingRepository = listingRepository;
    }

    public ConversationDTO getOrCreateConversation(Long listingId, UUID buyerId, UUID sellerId) {
        Optional<Conversation> existing = conversationRepository
                .findByListingIdAndBuyerIdAndSellerId(listingId, buyerId, sellerId);

        if (existing.isPresent()) {
            return convertToDTO(existing.get());
        }

        // Verify listing exists before creating conversation
        listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + listingId));

        Conversation conversation = new Conversation(listingId, buyerId, sellerId);
        Conversation saved = conversationRepository.save(conversation);
        return convertToDTO(saved);
    }

    public List<ConversationDTO> getUserConversations(UUID userId) {
        List<Conversation> conversations = conversationRepository.findByParticipantId(userId);
        return conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ConversationDTO getConversationById(Long conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        if (!conversation.isParticipant(userId)) {
            throw new RuntimeException("Unauthorized: You are not a participant in this conversation");
        }

        return convertToDTO(conversation);
    }

    public void deleteConversation(Long conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));

        if (!conversation.isParticipant(userId)) {
            throw new RuntimeException("Unauthorized: You are not a participant in this conversation");
        }

        conversationRepository.delete(conversation);
    }

    private ConversationDTO convertToDTO(Conversation conversation) {
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conversation.getConversationId());
        dto.setListingId(conversation.getListingId());
        dto.setBuyerId(conversation.getBuyerId());
        dto.setSellerId(conversation.getSellerId());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());

        Optional<Listing> listing = listingRepository.findById(conversation.getListingId());
        listing.ifPresent(l -> dto.setListingTitle(l.getTitle()));

        List<Message> messages = messageRepository
                .findByConversation_ConversationIdOrderByCreatedAtAsc(conversation.getConversationId());

        if (!messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            dto.setLastMessageContent(lastMessage.getContent());
            dto.setLastMessageTime(lastMessage.getCreatedAt());
        }

        long unreadCount = messageRepository.countUnreadMessages(
                conversation.getConversationId(),
                conversation.getBuyerId()
        );
        dto.setUnreadCount((int) unreadCount);

        return dto;
    }
}
