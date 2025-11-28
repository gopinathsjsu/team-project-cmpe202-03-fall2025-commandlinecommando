package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UniversityRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests for Discovery API Endpoints
 * 
 * Tests:
 * - GET /discovery/trending
 * - GET /discovery/recommended
 * - GET /discovery/similar/{productId}
 * - GET /discovery/recently-viewed
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Disable Spring Security filters for testing
@ActiveProfiles("test")
@Transactional
public class DiscoveryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String authToken;
    private User testUser;
    private University testUniversity;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUniversity = new University();
        testUniversity.setName("Test University");
        testUniversity.setDomain("test.edu");
        testUniversity.setActive(true);
        testUniversity = universityRepository.save(testUniversity);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.edu");
        testUser.setPassword("hashedpassword123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setUniversity(testUniversity);
        testUser.setRoles(Set.of(UserRole.BUYER, UserRole.SELLER));
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        authToken = jwtUtil.generateAccessToken(testUser);

        testProduct = new Product();
        testProduct.setTitle("MacBook Pro 2023");
        testProduct.setDescription("Excellent laptop");
        testProduct.setPrice(new BigDecimal("1200.00"));
        testProduct.setCategory(ProductCategory.ELECTRONICS);
        testProduct.setCondition(ProductCondition.LIKE_NEW);
        testProduct.setSeller(testUser);
        testProduct.setUniversity(testUniversity);
        testProduct.setActive(true);
        testProduct.setPickupLocation("San Jose");
        testProduct.setNegotiable(true);
        testProduct.setQuantity(1);
        testProduct.setModerationStatus(ModerationStatus.APPROVED);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setViewCount(100);  // Make it trending
        testProduct = productRepository.save(testProduct);
    }

    // ========================================
    // ✅ VALID REQUEST EXAMPLES
    // ========================================

    @Test
    public void testGetTrending_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Get trending products
        MvcResult result = mockMvc.perform(get("/discovery/trending")
                .param("limit", "10")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trending").isArray())
                .andExpect(jsonPath("$.trending[*].productId").exists())
                .andExpect(jsonPath("$.trending[*].title").exists())
                .andExpect(jsonPath("$.trending[*].price").exists())
                .andExpect(jsonPath("$.trending[*].viewCount").exists())
                .andReturn();

        // ✅ EXPECTED RESPONSE:
        String response = result.getResponse().getContentAsString();
        System.out.println("✅ Trending Products Response:");
        System.out.println(response);
        
        // Response format:
        // {
        //   "trending": [
        //     {
        //       "productId": "uuid-here",
        //       "title": "MacBook Pro 2023",
        //       "description": "Excellent laptop",
        //       "price": 1200.00,
        //       "category": "ELECTRONICS",
        //       "condition": "LIKE_NEW",
        //       "viewCount": 100,
        //       "favoriteCount": 5,
        //       "createdAt": "2025-11-10T12:00:00",
        //       "sellerId": "uuid-here",
        //       "sellerUsername": "testuser",
        //       "location": "San Jose",
        //       "negotiable": true,
        //       "quantity": 1,
        //       "imageUrls": []
        //     }
        //   ]
        // }
    }

    @Test
    public void testGetRecommended_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Get personalized recommendations
        MvcResult result = mockMvc.perform(get("/discovery/recommended")
                .param("limit", "10")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommended").isArray())
                .andReturn();

        // ✅ EXPECTED RESPONSE:
        String response = result.getResponse().getContentAsString();
        System.out.println("✅ Recommended Products Response:");
        System.out.println(response);
        
        // Same format as trending response
    }

    @Test
    public void testGetSimilar_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Get similar products
        MvcResult result = mockMvc.perform(get("/discovery/similar/" + testProduct.getProductId())
                .param("limit", "10")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.similar").isArray())
                .andReturn();

        // ✅ EXPECTED RESPONSE:
        String response = result.getResponse().getContentAsString();
        System.out.println("✅ Similar Products Response:");
        System.out.println(response);
        
        // Same format, products in same category
    }

    @Test
    public void testGetRecentlyViewed_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Get recently viewed products
        MvcResult result = mockMvc.perform(get("/discovery/recently-viewed")
                .param("limit", "20")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recentlyViewed").isArray())
                .andReturn();

        // ✅ EXPECTED RESPONSE:
        String response = result.getResponse().getContentAsString();
        System.out.println("✅ Recently Viewed Response:");
        System.out.println(response);
        
        // Same format, ordered by most recent view
    }

    // ========================================
    // ❌ INVALID REQUEST EXAMPLES
    // ========================================

    @Test
    public void testGetTrending_MissingAuthentication() throws Exception {
        // ❌ INVALID: Missing Authorization header
        MvcResult result = mockMvc.perform(get("/discovery/trending")
                .param("limit", "10"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Missing Auth Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // {
        //   "error": "Unauthorized",
        //   "message": "Full authentication is required",
        //   "status": 401
        // }
    }

    @Test
    public void testGetTrending_InvalidLimit() throws Exception {
        // ❌ INVALID: Limit exceeds maximum
        MvcResult result = mockMvc.perform(get("/discovery/trending")
                .param("limit", "200")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Invalid Limit Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // {
        //   "error": "Invalid request",
        //   "message": "limit must be between 1 and 50",
        //   "status": 400
        // }
    }

    @Test
    public void testGetSimilar_InvalidProductId() throws Exception {
        // ❌ INVALID: Non-existent product ID
        UUID nonExistentId = UUID.randomUUID();
        
        MvcResult result = mockMvc.perform(get("/discovery/similar/" + nonExistentId)
                .param("limit", "10")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Product Not Found Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // {
        //   "error": "Not Found",
        //   "message": "Product not found with id: " + nonExistentId,
        //   "status": 404
        // }
    }

    @Test
    public void testGetSimilar_InvalidUUID() throws Exception {
        // ❌ INVALID: Malformed UUID
        MvcResult result = mockMvc.perform(get("/discovery/similar/invalid-uuid")
                .param("limit", "10")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Invalid UUID Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // {
        //   "error": "Invalid request",
        //   "message": "Invalid UUID format",
        //   "status": 400
        // }
    }

    @Test
    public void testGetRecommended_InvalidToken() throws Exception {
        // ❌ INVALID: Invalid JWT token
        MvcResult result = mockMvc.perform(get("/discovery/recommended")
                .param("limit", "10")
                .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Invalid Token Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // {
        //   "error": "Unauthorized",
        //   "message": "Invalid or expired JWT token",
        //   "status": 401
        // }
    }

    // ========================================
    // 🔍 EDGE CASES
    // ========================================

    @Test
    public void testGetTrending_NoTrendingItems() throws Exception {
        // Edge Case: No trending items (all products have low views)
        // Delete the high-view product
        productRepository.delete(testProduct);

        mockMvc.perform(get("/discovery/trending")
                .param("limit", "10")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trending").isArray())
                .andExpect(jsonPath("$.trending").isEmpty());
    }

    @Test
    public void testGetRecentlyViewed_NoHistory() throws Exception {
        // Edge Case: User hasn't viewed any products
        mockMvc.perform(get("/discovery/recently-viewed")
                .param("limit", "20")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recentlyViewed").isArray())
                .andExpect(jsonPath("$.recentlyViewed").isEmpty());
    }

    @Test
    public void testGetSimilar_NoSimilarProducts() throws Exception {
        // Edge Case: No other products in same category
        // Delete all products except one
        productRepository.deleteAll();
        
        Product singleProduct = new Product();
        singleProduct.setTitle("Unique Item");
        singleProduct.setDescription("One of a kind");
        singleProduct.setPrice(new BigDecimal("100.00"));
        singleProduct.setCategory(ProductCategory.OTHER);
        singleProduct.setCondition(ProductCondition.GOOD);
        singleProduct.setSeller(testUser);
        singleProduct.setUniversity(testUniversity);
        singleProduct.setActive(true);
        singleProduct.setPickupLocation("San Jose");
        singleProduct.setNegotiable(true);
        singleProduct.setQuantity(1);
        singleProduct.setModerationStatus(ModerationStatus.APPROVED);
        singleProduct.setCreatedAt(LocalDateTime.now());
        singleProduct = productRepository.save(singleProduct);

        mockMvc.perform(get("/discovery/similar/" + singleProduct.getProductId())
                .param("limit", "10")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.similar").isArray())
                .andExpect(jsonPath("$.similar").isEmpty());
    }

    @Test
    public void testGetTrending_LimitOne() throws Exception {
        // Edge Case: Request only 1 trending item
        mockMvc.perform(get("/discovery/trending")
                .param("limit", "1")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trending").isArray())
                .andExpect(jsonPath("$.trending", hasSize(lessThanOrEqualTo(1))));
    }

    @Test
    public void testGetTrending_NoLimitParameter() throws Exception {
        // Edge Case: No limit parameter (should use default 10)
        mockMvc.perform(get("/discovery/trending")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trending").isArray())
                .andExpect(jsonPath("$.trending", hasSize(lessThanOrEqualTo(10))));
    }
}

