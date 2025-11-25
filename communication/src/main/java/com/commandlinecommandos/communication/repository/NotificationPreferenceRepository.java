package com.commandlinecommandos.communication.repository;

import com.commandlinecommandos.communication.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    
    /**
     * Find notification preference by user ID.
     * 
     * @param userId The user ID
     * @return Optional notification preference
     */
    Optional<NotificationPreference> findByUserId(Long userId);
}

