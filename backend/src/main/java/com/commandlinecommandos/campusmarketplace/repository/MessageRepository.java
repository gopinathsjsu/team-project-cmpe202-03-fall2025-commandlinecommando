package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Message entity
 * Consolidated from communication service
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages in a conversation, ordered by creation time
     */
    List<Message> findByConversation_ConversationIdOrderByCreatedAtAsc(Long conversationId);

    /**
     * Count unread messages in a conversation for a specific user
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.conversationId = :conversationId " +
           "AND m.senderId != :userId AND m.isRead = false")
    long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") UUID userId);

    /**
     * Mark all messages in a conversation as read for a specific user
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.conversationId = :conversationId " +
           "AND m.senderId != :userId AND m.isRead = false")
    int markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") UUID userId);

    /**
     * Find unread messages for a user across all conversations
     */
    @Query("SELECT m FROM Message m WHERE (m.conversation.buyerId = :userId OR m.conversation.sellerId = :userId) " +
           "AND m.senderId != :userId AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessagesForUser(@Param("userId") UUID userId);
}
