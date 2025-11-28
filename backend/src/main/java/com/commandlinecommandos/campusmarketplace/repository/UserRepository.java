package com.commandlinecommandos.campusmarketplace.repository;

import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.model.University;
import com.commandlinecommandos.campusmarketplace.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity
 * Updated to use UUID primary keys
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username or email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by university
     */
    List<User> findByUniversity(University university);
    
    /**
     * Find users by role (queries the roles collection)
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") UserRole role);
    
    /**
     * Find users by university and role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.university = :university AND r = :role")
    List<User> findByUniversityAndRole(@Param("university") University university, @Param("role") UserRole role);
    
    /**
     * Find users by verification status
     */
    List<User> findByVerificationStatus(VerificationStatus status);
    
    /**
     * Find active users
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Find by student ID
     */
    Optional<User> findByStudentId(String studentId);
    
    /**
     * Count users by role (queries the roles collection)
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") UserRole role);
    
    /**
     * Count users by university
     */
    long countByUniversity(University university);
}
