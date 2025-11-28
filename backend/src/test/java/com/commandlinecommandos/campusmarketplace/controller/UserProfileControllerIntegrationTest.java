package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.dto.PasswordChangeRequest;
import com.commandlinecommandos.campusmarketplace.dto.ProfileUpdateRequest;
import com.commandlinecommandos.campusmarketplace.model.User;
import com.commandlinecommandos.campusmarketplace.model.UserRole;
import com.commandlinecommandos.campusmarketplace.model.University;
import com.commandlinecommandos.campusmarketplace.repository.UniversityRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserProfileController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserProfileControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private User testUser;
    private University testUniversity;
    
    @BeforeEach
    void setUp() {
        // Create test university
        testUniversity = new University();
        testUniversity.setName("Test University");
        testUniversity.setDomain("test.edu");
        testUniversity = universityRepository.save(testUniversity);
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.edu");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRoles(new HashSet<>(Set.of(UserRole.BUYER, UserRole.SELLER)));
        testUser.setUniversity(testUniversity);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);
    }
    
    private UsernamePasswordAuthenticationToken createAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
    
    @Test
    void testGetCurrentUserProfile_Success() throws Exception {
        mockMvc.perform(get("/users/profile")
                .with(authentication(createAuthentication(testUser))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@test.edu"));
    }
    
    @Test
    void testGetCurrentUserProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/users/profile"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testUpdateProfile_Success() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");
        request.setPhone("4085551234"); // No hyphens - must match validation pattern
        
        mockMvc.perform(put("/users/profile")
                .with(authentication(createAuthentication(testUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Updated"))
            .andExpect(jsonPath("$.lastName").value("Name"));
    }
    
    @Test
    void testChangePassword_Success() throws Exception {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword("Password123!");
        request.setNewPassword("NewPassword123!");
        request.setConfirmPassword("NewPassword123!");
        
        mockMvc.perform(post("/users/change-password")
                .with(authentication(createAuthentication(testUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }
    
    @Test
    void testDeactivateAccount_Success() throws Exception {
        mockMvc.perform(post("/users/deactivate")
                .with(authentication(createAuthentication(testUser))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Account deactivated successfully"));
    }
}

