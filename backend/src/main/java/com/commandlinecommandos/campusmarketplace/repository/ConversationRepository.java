package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Conversation entity
 * Consolidated from communication service
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find a conversation by listing, buyer, and seller
     */
    Optional<Conversation> findByListingIdAndBuyerIdAndSellerId(
            Long listingId, UUID buyerId, UUID sellerId
    );

    /**
     * Find all conversations where the user is either buyer or seller
     */
    @Query("SELECT c FROM Conversation c WHERE c.buyerId = :userId OR c.sellerId = :userId ORDER BY c.updatedAt DESC")
    List<Conversation> findByParticipantId(@Param("userId") UUID userId);

    /**
     * Find conversations for a specific listing
     */
    List<Conversation> findByListingId(Long listingId);

    /**
     * Find conversations where user is the buyer
     */
    List<Conversation> findByBuyerIdOrderByUpdatedAtDesc(UUID buyerId);

    /**
     * Find conversations where user is the seller
     */
    List<Conversation> findBySellerIdOrderByUpdatedAtDesc(UUID sellerId);

    /**
     * Check if a conversation exists for a listing and buyer-seller pair
     */
    boolean existsByListingIdAndBuyerIdAndSellerId(
            Long listingId, UUID buyerId, UUID sellerId
    );
}
