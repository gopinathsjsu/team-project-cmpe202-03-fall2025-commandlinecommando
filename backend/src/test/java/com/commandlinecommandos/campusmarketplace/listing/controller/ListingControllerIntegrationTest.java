package com.commandlinecommandos.campusmarketplace.listing.controller;

import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.repository.UniversityRepository;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for ListingController
 * Tests the full stack: Controller -> Service -> Repository -> Database
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ListingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private University testUniversity;
    private User testUser;
    private String testUserToken;
    private Product testListing;

    @BeforeEach
    public void setup() {
        // Clean up
        productRepository.deleteAll();
        userRepository.deleteAll();
        universityRepository.deleteAll();

        // Create test university
        testUniversity = new University();
        testUniversity.setName("Test University");
        testUniversity.setDomain("test.edu");
        testUniversity.setActive(true);
        testUniversity = universityRepository.save(testUniversity);

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@test.edu");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRoles(Set.of(UserRole.BUYER, UserRole.SELLER));
        testUser.setUniversity(testUniversity);
        testUser.setEmailVerifiedAt(java.time.LocalDateTime.now());
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Generate JWT token for test user
        testUserToken = jwtUtil.generateAccessToken(testUser);

        // Create test listing
        testListing = new Product();
        testListing.setSeller(testUser);
        testListing.setUniversity(testUniversity);
        testListing.setTitle("Test Listing");
        testListing.setDescription("Test Description");
        testListing.setCategory(ProductCategory.ELECTRONICS);
        testListing.setCondition(ProductCondition.NEW);
        testListing.setPrice(BigDecimal.valueOf(99.99));
        testListing.setPickupLocation("Campus");
        testListing.setModerationStatus(ModerationStatus.APPROVED);
        testListing.setActive(true);
        testListing.publish();
        testListing = productRepository.save(testListing);
    }

    @Test
    public void testGetAllListings_Success() throws Exception {
        mockMvc.perform(get("/listings")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title", is("Test Listing")))
                .andExpect(jsonPath("$.content[0].category", is("ELECTRONICS")))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    public void testGetAllListings_WithCategoryFilter() throws Exception {
        mockMvc.perform(get("/listings")
                .param("page", "0")
                .param("size", "20")
                .param("category", "ELECTRONICS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].category", is("ELECTRONICS")));
    }

    @Test
    public void testGetListingById_Success() throws Exception {
        mockMvc.perform(get("/listings/" + testListing.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Listing")))
                .andExpect(jsonPath("$.description", is("Test Description")))
                .andExpect(jsonPath("$.price", is(99.99)));
    }

    @Test
    public void testGetListingById_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/listings/" + randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("LISTING_NOT_FOUND")));
    }

    @Test
    public void testCreateListing_Success() throws Exception {
        Map<String, Object> newListing = new HashMap<>();
        newListing.put("title", "New Test Listing");
        newListing.put("description", "New Test Description");
        newListing.put("category", "TEXTBOOKS");
        newListing.put("condition", "LIKE_NEW");
        newListing.put("price", 50.00);
        newListing.put("location", "Library");

        mockMvc.perform(post("/listings")
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Listing created successfully")))
                .andExpect(jsonPath("$.listing.title", is("New Test Listing")))
                .andExpect(jsonPath("$.listing.category", is("TEXTBOOKS")));
    }

    @Test
    public void testCreateListing_Unauthorized() throws Exception {
        Map<String, Object> newListing = new HashMap<>();
        newListing.put("title", "Unauthorized Listing");
        newListing.put("price", 50.00);

        mockMvc.perform(post("/listings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newListing)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateListing_Success() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Title");
        updates.put("price", 79.99);

        mockMvc.perform(put("/listings/" + testListing.getProductId())
                .header("Authorization", "Bearer " + testUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Listing updated successfully")))
                .andExpect(jsonPath("$.listing.title", is("Updated Title")))
                .andExpect(jsonPath("$.listing.price", is(79.99)));
    }

    @Test
    public void testUpdateListing_NotOwner() throws Exception {
        // Create another user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@test.edu");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setRoles(Set.of(UserRole.BUYER, UserRole.SELLER));
        anotherUser.setUniversity(testUniversity);
        anotherUser.setEmailVerifiedAt(java.time.LocalDateTime.now());
        anotherUser.setActive(true);
        anotherUser = userRepository.save(anotherUser);

        String anotherUserToken = jwtUtil.generateAccessToken(anotherUser);

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Hacked Title");

        mockMvc.perform(put("/listings/" + testListing.getProductId())
                .header("Authorization", "Bearer " + anotherUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteListing_Success() throws Exception {
        mockMvc.perform(delete("/listings/" + testListing.getProductId())
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Listing deleted successfully")))
                .andExpect(jsonPath("$.listingId", is(testListing.getProductId().toString())));

        // Verify listing is soft-deleted (isActive = false)
        Product deletedListing = productRepository.findById(testListing.getProductId()).orElse(null);
        assert deletedListing != null;
        assert !deletedListing.isActive();
    }

    @Test
    public void testGetListingsBySeller_Success() throws Exception {
        mockMvc.perform(get("/listings/seller/" + testUser.getUserId())
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].sellerId", is(testUser.getUserId().toString())));
    }

    @Test
    public void testGetMyListings_Success() throws Exception {
        mockMvc.perform(get("/listings/my-listings")
                .header("Authorization", "Bearer " + testUserToken)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title", is("Test Listing")));
    }

    @Test
    public void testGetMyListings_Unauthorized() throws Exception {
        mockMvc.perform(get("/listings/my-listings")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isForbidden());
    }
}
