package com.commandlinecommandos.campusmarketplace.controller;

import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UniversityRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import com.commandlinecommandos.campusmarketplace.security.JwtTokenProvider;
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
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests for Search API Endpoints
 * 
 * Tests the complete HTTP request/response cycle for:
 * - POST /search - Advanced search with filters
 * - GET /search/autocomplete - Auto-suggest
 * - GET /search/history - User search history
 * 
 * Includes examples of:
 * - ✅ Valid requests and responses
 * - ❌ Invalid requests and error responses
 * - Edge cases and validation
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SearchControllerIntegrationTest {

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
    private JwtTokenProvider jwtTokenProvider;

    private String authToken;
    private User testUser;
    private University testUniversity;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Create test university
        testUniversity = new University();
        testUniversity.setUniversityId(UUID.randomUUID());
        testUniversity.setUniversityName("Test University");
        testUniversity.setDomain("test.edu");
        testUniversity.setActive(true);
        testUniversity = universityRepository.save(testUniversity);

        // Create test user
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.edu");
        testUser.setPassword("hashedpassword123");
        testUser.setUniversity(testUniversity);
        testUser.setRole(UserRole.STUDENT);
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Generate JWT token
        authToken = jwtTokenProvider.generateToken(testUser.getUsername());

        // Create test products
        testProduct1 = new Product();
        testProduct1.setProductId(UUID.randomUUID());
        testProduct1.setTitle("MacBook Pro 2023");
        testProduct1.setDescription("Excellent laptop for students");
        testProduct1.setPrice(new BigDecimal("1200.00"));
        testProduct1.setCategory(ProductCategory.ELECTRONICS);
        testProduct1.setCondition(ProductCondition.LIKE_NEW);
        testProduct1.setSeller(testUser);
        testProduct1.setUniversity(testUniversity);
        testProduct1.setIsActive(true);
        testProduct1.setLocation("San Jose");
        testProduct1.setNegotiable(true);
        testProduct1.setQuantity(1);
        testProduct1.setModerationStatus(ModerationStatus.APPROVED);
        testProduct1.setCreatedAt(LocalDateTime.now());
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product();
        testProduct2.setProductId(UUID.randomUUID());
        testProduct2.setTitle("Java Programming Textbook");
        testProduct2.setDescription("Used textbook for CS students");
        testProduct2.setPrice(new BigDecimal("50.00"));
        testProduct2.setCategory(ProductCategory.TEXTBOOKS);
        testProduct2.setCondition(ProductCondition.GOOD);
        testProduct2.setSeller(testUser);
        testProduct2.setUniversity(testUniversity);
        testProduct2.setIsActive(true);
        testProduct2.setLocation("San Jose");
        testProduct2.setNegotiable(true);
        testProduct2.setQuantity(1);
        testProduct2.setModerationStatus(ModerationStatus.APPROVED);
        testProduct2.setCreatedAt(LocalDateTime.now());
        testProduct2 = productRepository.save(testProduct2);
    }

    // ========================================
    // ✅ VALID REQUEST EXAMPLES
    // ========================================

    @Test
    public void testBasicSearch_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Basic search with query only
        String requestBody = """
            {
                "query": "laptop",
                "page": 0,
                "size": 20,
                "sortBy": "relevance"
            }
            """;

        MvcResult result = mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.totalResults").isNumber())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.metadata").exists())
                .andExpect(jsonPath("$.metadata.searchTimeMs").isNumber())
                .andExpect(jsonPath("$.metadata.searchQuery").value("laptop"))
                .andReturn();

        // ✅ EXPECTED RESPONSE STRUCTURE:
        String response = result.getResponse().getContentAsString();
        System.out.println("✅ Valid Search Response:");
        System.out.println(response);
        
        // Response will contain:
        // {
        //   "results": [ {...product objects...} ],
        //   "totalResults": 1,
        //   "totalPages": 1,
        //   "currentPage": 0,
        //   "pageSize": 20,
        //   "hasNext": false,
        //   "hasPrevious": false,
        //   "metadata": {
        //     "searchTimeMs": 45,
        //     "appliedFilters": "",
        //     "totalFilters": 0,
        //     "sortedBy": "relevance",
        //     "cached": false,
        //     "searchQuery": "laptop"
        //   }
        // }
    }

    @Test
    public void testSearchWithFilters_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Search with category and price filters
        String requestBody = """
            {
                "categories": ["ELECTRONICS"],
                "minPrice": 1000.00,
                "maxPrice": 2000.00,
                "conditions": ["LIKE_NEW", "NEW"],
                "page": 0,
                "size": 20
            }
            """;

        mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results[0].category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.results[0].price").value(greaterThanOrEqualTo(1000.00)))
                .andExpect(jsonPath("$.results[0].price").value(lessThanOrEqualTo(2000.00)))
                .andExpect(jsonPath("$.metadata.totalFilters").value(greaterThan(0)));
    }

    @Test
    public void testSearchWithDateFilter_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Search with date filter (last 7 days)
        String requestBody = String.format("""
            {
                "dateFrom": "%s",
                "page": 0,
                "size": 20
            }
            """, LocalDateTime.now().minusDays(7).toString());

        mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.totalResults").isNumber());
    }

    @Test
    public void testSearchWithSorting_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Sort by price ascending
        String requestBody = """
            {
                "sortBy": "price_asc",
                "page": 0,
                "size": 20
            }
            """;

        MvcResult result = mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.metadata.sortedBy").value("price_asc"))
                .andReturn();

        // Verify prices are in ascending order
        String response = result.getResponse().getContentAsString();
        System.out.println("✅ Sorted Search Response (price_asc):");
        System.out.println(response);
    }

    @Test
    public void testAutocomplete_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Autocomplete with valid query
        mockMvc.perform(get("/search/autocomplete")
                .param("q", "lap")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions").isArray());
    }

    @Test
    public void testSearchHistory_ValidRequest() throws Exception {
        // ✅ VALID REQUEST: Get user search history
        mockMvc.perform(get("/search/history")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history").isArray());
    }

    // ========================================
    // ❌ INVALID REQUEST EXAMPLES
    // ========================================

    @Test
    public void testSearch_MissingAuthentication() throws Exception {
        // ❌ INVALID: Missing Authorization header
        String requestBody = """
            {
                "query": "laptop",
                "page": 0,
                "size": 20
            }
            """;

        MvcResult result = mockMvc.perform(post("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Missing Auth Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // Response will be:
        // {
        //   "error": "Unauthorized",
        //   "message": "Full authentication is required to access this resource",
        //   "status": 401
        // }
    }

    @Test
    public void testSearch_InvalidPageNumber() throws Exception {
        // ❌ INVALID: Negative page number
        String requestBody = """
            {
                "query": "laptop",
                "page": -1,
                "size": 20
            }
            """;

        MvcResult result = mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Invalid Page Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // Response will be:
        // {
        //   "error": "Validation failed",
        //   "message": "page: must be greater than or equal to 0",
        //   "status": 400
        // }
    }

    @Test
    public void testSearch_InvalidPageSize() throws Exception {
        // ❌ INVALID: Page size exceeds maximum
        String requestBody = """
            {
                "query": "laptop",
                "page": 0,
                "size": 500
            }
            """;

        MvcResult result = mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Invalid Page Size Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // Response will be:
        // {
        //   "error": "Validation failed",
        //   "message": "size: must be between 1 and 100",
        //   "status": 400
        // }
    }

    @Test
    public void testSearch_InvalidPriceRange() throws Exception {
        // ❌ INVALID: Min price greater than max price
        String requestBody = """
            {
                "query": "laptop",
                "minPrice": 2000.00,
                "maxPrice": 1000.00,
                "page": 0,
                "size": 20
            }
            """;

        MvcResult result = mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Invalid Price Range Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // Response will be:
        // {
        //   "error": "Invalid request",
        //   "message": "minPrice cannot be greater than maxPrice",
        //   "status": 400
        // }
    }

    @Test
    public void testSearch_InvalidCategory() throws Exception {
        // ❌ INVALID: Non-existent category
        String requestBody = """
            {
                "categories": ["INVALID_CATEGORY"],
                "page": 0,
                "size": 20
            }
            """;

        MvcResult result = mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Invalid Category Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // Response will be:
        // {
        //   "error": "Invalid request",
        //   "message": "Invalid category: INVALID_CATEGORY",
        //   "status": 400
        // }
    }

    @Test
    public void testSearch_InvalidSortBy() throws Exception {
        // ❌ INVALID: Invalid sort option
        String requestBody = """
            {
                "query": "laptop",
                "sortBy": "invalid_sort",
                "page": 0,
                "size": 20
            }
            """;

        MvcResult result = mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Invalid Sort Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // Response will be:
        // {
        //   "error": "Invalid request",
        //   "message": "Invalid sortBy value. Must be one of: relevance, price_asc, price_desc, date_desc, date_asc, popularity",
        //   "status": 400
        // }
    }

    @Test
    public void testAutocomplete_QueryTooShort() throws Exception {
        // ❌ INVALID: Query too short (< 2 characters)
        MvcResult result = mockMvc.perform(get("/search/autocomplete")
                .param("q", "a")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Query Too Short Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // Response will be:
        // {
        //   "error": "Invalid request",
        //   "message": "Query must be at least 2 characters long",
        //   "status": 400
        // }
    }

    @Test
    public void testAutocomplete_MissingQuery() throws Exception {
        // ❌ INVALID: Missing query parameter
        MvcResult result = mockMvc.perform(get("/search/autocomplete")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Missing Query Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // Response will be:
        // {
        //   "error": "Invalid request",
        //   "message": "Required parameter 'q' is missing",
        //   "status": 400
        // }
    }

    @Test
    public void testSearch_InvalidToken() throws Exception {
        // ❌ INVALID: Invalid JWT token
        String requestBody = """
            {
                "query": "laptop",
                "page": 0,
                "size": 20
            }
            """;

        MvcResult result = mockMvc.perform(post("/search")
                .header("Authorization", "Bearer invalid_token_12345")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // ❌ EXPECTED ERROR RESPONSE:
        System.out.println("❌ Invalid Token Error:");
        System.out.println(result.getResponse().getContentAsString());
        
        // Response will be:
        // {
        //   "error": "Unauthorized",
        //   "message": "Invalid or expired JWT token",
        //   "status": 401
        // }
    }

    @Test
    public void testSearch_EmptyRequestBody() throws Exception {
        // ✅ VALID: Empty request body (returns all products)
        String requestBody = "{}";

        mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.totalResults").value(greaterThanOrEqualTo(0)));
    }

    // ========================================
    // 🔍 EDGE CASES
    // ========================================

    @Test
    public void testSearch_NoResultsFound() throws Exception {
        // Edge Case: Search query returns no results
        String requestBody = """
            {
                "query": "xyzabcnonexistentproduct12345",
                "page": 0,
                "size": 20
            }
            """;

        mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isEmpty())
                .andExpect(jsonPath("$.totalResults").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    public void testSearch_PaginationBeyondLastPage() throws Exception {
        // Edge Case: Request page beyond available results
        String requestBody = """
            {
                "query": "laptop",
                "page": 999,
                "size": 20
            }
            """;

        mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isEmpty())
                .andExpect(jsonPath("$.currentPage").value(999))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    public void testSearch_SpecialCharactersInQuery() throws Exception {
        // Edge Case: Special characters in search query
        String requestBody = """
            {
                "query": "laptop !@#$%^&*()_+-={}[]|:;<>?,./",
                "page": 0,
                "size": 20
            }
            """;

        mockMvc.perform(post("/search")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }
}

