package com.commandlinecommandos.campusmarketplace.communication.repository;

import com.commandlinecommandos.campusmarketplace.communication.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {
    
    /**
     * Find notification preference by user ID.
     * 
     * @param userId The user ID
     * @return Optional notification preference
     */
    Optional<NotificationPreference> findByUserId(UUID userId);
}
