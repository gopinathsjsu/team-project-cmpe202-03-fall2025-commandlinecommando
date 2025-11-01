package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for University entity
 */
@Repository
public interface UniversityRepository extends JpaRepository<University, UUID> {
    
    /**
     * Find university by domain (e.g., "sjsu.edu")
     */
    Optional<University> findByDomain(String domain);
    
    /**
     * Find university by domain (case-insensitive)
     */
    Optional<University> findByDomainIgnoreCase(String domain);
    
    /**
     * Check if university exists by domain
     */
    boolean existsByDomain(String domain);
    
    /**
     * Find all active universities
     */
    java.util.List<University> findByIsActive(boolean isActive);
}

