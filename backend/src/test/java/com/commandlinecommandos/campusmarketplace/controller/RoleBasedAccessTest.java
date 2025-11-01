package com.commandlinecommandos.campusmarketplace.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.commandlinecommandos.campusmarketplace.config.WebSecurityConfig;
import com.commandlinecommandos.campusmarketplace.security.RoleAuthorizationAspect;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAccessTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    // Admin Endpoint Tests
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to Admin Dashboard"));
    }
    
    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testStudentCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testUnauthenticatedUserCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessUsersList() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Admin access: All users data"));
    }
    
    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testStudentCannotAccessUsersList() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanModerateListing() throws Exception {
        mockMvc.perform(post("/admin/moderate/123")
                .param("action", "approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Listing 123 has been approve"))
                .andExpect(jsonPath("$.listingId").value(123))
                .andExpect(jsonPath("$.action").value("approve"));
    }
    
    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testStudentCannotModerateListing() throws Exception {
        mockMvc.perform(post("/admin/moderate/123")
                .param("action", "approve"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanDeleteUser() throws Exception {
        mockMvc.perform(delete("/admin/users/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User 123 has been deleted"))
                .andExpect(jsonPath("$.userId").value(123));
    }
    
    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testStudentCannotDeleteUser() throws Exception {
        mockMvc.perform(delete("/admin/users/123"))
                .andExpect(status().isForbidden());
    }
    
    // Student Endpoint Tests
    
    @Test
    @WithMockUser(username = "student", roles = {"BUYER"})
    void testStudentCanAccessStudentDashboard() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to Student Dashboard"));
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCannotAccessStudentDashboard() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testUnauthenticatedUserCannotAccessStudentDashboard() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(username = "student", roles = {"SELLER"})
    void testStudentCanAccessOwnListings() throws Exception {
        mockMvc.perform(get("/student/listings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Student's listings"));
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCanAccessStudentListings() throws Exception {
        // Admin should be able to access student listings (has ADMIN role which includes STUDENT permissions)
        mockMvc.perform(get("/student/listings"))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "student", roles = {"SELLER"})
    void testStudentCanCreateListing() throws Exception {
        String listingJson = "{\"title\":\"Test Listing\",\"description\":\"Test Description\",\"price\":100}";
        
        mockMvc.perform(post("/student/listings")
                .contentType("application/json")
                .content(listingJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Listing created successfully"))
                .andExpect(jsonPath("$.listingId").value(123))
                .andExpect(jsonPath("$.title").value("Test Listing"));
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminCannotCreateStudentListing() throws Exception {
        String listingJson = "{\"title\":\"Test Listing\",\"description\":\"Test Description\",\"price\":100}";
        
        mockMvc.perform(post("/student/listings")
                .contentType("application/json")
                .content(listingJson))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testUnauthenticatedUserCannotCreateListing() throws Exception {
        String listingJson = "{\"title\":\"Test Listing\",\"description\":\"Test Description\",\"price\":100}";
        
        mockMvc.perform(post("/student/listings")
                .contentType("application/json")
                .content(listingJson))
                .andExpect(status().isUnauthorized());
    }
}
