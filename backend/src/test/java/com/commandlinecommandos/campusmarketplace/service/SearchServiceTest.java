package com.commandlinecommandos.campusmarketplace.service;

import com.commandlinecommandos.campusmarketplace.dto.SearchRequest;
import com.commandlinecommandos.campusmarketplace.dto.SearchResponse;
import com.commandlinecommandos.campusmarketplace.model.*;
import com.commandlinecommandos.campusmarketplace.repository.ProductRepository;
import com.commandlinecommandos.campusmarketplace.repository.UniversityRepository;
import com.commandlinecommandos.campusmarketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SearchService
 * Tests full-text search, filtering, sorting, and performance
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SearchServiceTest {
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UniversityRepository universityRepository;
    
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
        testUser.setPassword("hashedpassword123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setUniversity(testUniversity);
        testUser.setRole(UserRole.BUYER);
        testUser = userRepository.save(testUser);
        
        // Create test products
        createTestProducts();
    }
    
    private void createTestProducts() {
        // Product 1: Laptop
        Product laptop = new Product();
        laptop.setSeller(testUser);
        laptop.setUniversity(testUniversity);
        laptop.setTitle("MacBook Pro 13-inch");
        laptop.setDescription("Excellent condition laptop, barely used");
        laptop.setCategory(ProductCategory.ELECTRONICS);
        laptop.setCondition(ProductCondition.LIKE_NEW);
        laptop.setPrice(new BigDecimal("1200.00"));
        laptop.setPickupLocation("San Jose");
        laptop.setActive(true);
        laptop.setModerationStatus(ModerationStatus.APPROVED);
        productRepository.save(laptop);
        
        // Product 2: Textbook
        Product textbook = new Product();
        textbook.setSeller(testUser);
        textbook.setUniversity(testUniversity);
        textbook.setTitle("Java Programming Textbook");
        textbook.setDescription("CS textbook for beginners");
        textbook.setCategory(ProductCategory.TEXTBOOKS);
        textbook.setCondition(ProductCondition.GOOD);
        textbook.setPrice(new BigDecimal("50.00"));
        textbook.setPickupLocation("San Jose");
        textbook.setActive(true);
        textbook.setModerationStatus(ModerationStatus.APPROVED);
        productRepository.save(textbook);
        
        // Product 3: Furniture
        Product desk = new Product();
        desk.setSeller(testUser);
        desk.setUniversity(testUniversity);
        desk.setTitle("Study Desk");
        desk.setDescription("Wooden desk in good condition");
        desk.setCategory(ProductCategory.FURNITURE);
        desk.setCondition(ProductCondition.GOOD);
        desk.setPrice(new BigDecimal("80.00"));
        desk.setPickupLocation("Mountain View");
        desk.setActive(true);
        desk.setModerationStatus(ModerationStatus.APPROVED);
        productRepository.save(desk);
    }
    
    @Test
    @Disabled("Requires PostgreSQL - H2 doesn't support ts_rank() and plainto_tsquery()")
    void testBasicSearch() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setQuery("laptop");
        
        // When
        long startTime = System.currentTimeMillis();
        SearchResponse response = searchService.search(request, testUser);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertNotNull(response);
        assertTrue(response.getTotalResults() >= 1, "Should find at least one laptop");
        assertTrue(duration < 200, "Search should complete in <200ms, took: " + duration + "ms");
        assertNotNull(response.getMetadata());
        assertTrue(response.getMetadata().getSearchTimeMs() < 200);
    }
    
    @Test
    void testSearchWithCategoryFilter() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setCategories(List.of(ProductCategory.ELECTRONICS));
        
        // When
        SearchResponse response = searchService.search(request, testUser);
        
        // Then
        assertTrue(response.getTotalResults() >= 1);
        response.getResults().forEach(product -> 
            assertEquals(ProductCategory.ELECTRONICS, product.getCategory())
        );
    }
    
    @Test
    void testSearchWithPriceFilter() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setMinPrice(new BigDecimal("40.00"));
        request.setMaxPrice(new BigDecimal("100.00"));
        
        // When
        SearchResponse response = searchService.search(request, testUser);
        
        // Then
        assertTrue(response.getTotalResults() >= 1);
        response.getResults().forEach(product -> {
            assertTrue(product.getPrice().compareTo(new BigDecimal("40.00")) >= 0);
            assertTrue(product.getPrice().compareTo(new BigDecimal("100.00")) <= 0);
        });
    }
    
    @Test
    void testSearchWithMultipleFilters() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setCategories(List.of(ProductCategory.ELECTRONICS));
        request.setMinPrice(new BigDecimal("1000.00"));
        request.setLocation("San Jose");
        
        // When
        SearchResponse response = searchService.search(request, testUser);
        
        // Then
        assertNotNull(response);
        // Laptop should match all filters
        if (response.getTotalResults() > 0) {
            response.getResults().forEach(product -> {
                assertEquals(ProductCategory.ELECTRONICS, product.getCategory());
                assertTrue(product.getPrice().compareTo(new BigDecimal("1000.00")) >= 0);
            });
        }
    }
    
    @Test
    void testSortByPrice() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setSortBy("price_asc");
        request.setSize(10);
        
        // When
        SearchResponse response = searchService.search(request, testUser);
        
        // Then
        assertTrue(response.getTotalResults() >= 2);
        // Verify ascending price order
        for (int i = 0; i < response.getResults().size() - 1; i++) {
            BigDecimal current = response.getResults().get(i).getPrice();
            BigDecimal next = response.getResults().get(i + 1).getPrice();
            assertTrue(current.compareTo(next) <= 0, 
                "Prices should be in ascending order");
        }
    }
    
    @Test
    void testAutocomplete() {
        // Given
        String query = "lap";
        
        // When
        List<String> suggestions = searchService.autocomplete(query, 
            testUniversity.getUniversityId());
        
        // Then
        assertNotNull(suggestions);
        // Should suggest "laptop" or similar
        assertTrue(suggestions.size() <= 10, "Should return max 10 suggestions");
    }
    
    @Test
    void testAutocompleteTooShort() {
        // Given
        String query = "a";  // Only 1 character
        
        // When
        List<String> suggestions = searchService.autocomplete(query,
            testUniversity.getUniversityId());
        
        // Then
        assertTrue(suggestions.isEmpty(), "Should return empty for queries < 2 chars");
    }
    
    @Test
    @Disabled("Requires PostgreSQL - H2 doesn't support full-text search functions")
    void testSearchMetadata() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setQuery("laptop");
        request.setCategories(List.of(ProductCategory.ELECTRONICS));
        request.setMinPrice(new BigDecimal("1000.00"));
        
        // When
        SearchResponse response = searchService.search(request, testUser);
        
        // Then
        assertNotNull(response.getMetadata());
        assertEquals("laptop", response.getMetadata().getSearchQuery());
        assertTrue(response.getMetadata().getTotalFilters() >= 2);
        assertNotNull(response.getMetadata().getAppliedFilters());
    }
    
    @Test
    void testPagination() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setPage(0);
        request.setSize(2);
        
        // When
        SearchResponse response = searchService.search(request, testUser);
        
        // Then
        assertTrue(response.getResults().size() <= 2);
        assertEquals(0, response.getCurrentPage());
        assertEquals(2, response.getPageSize());
    }
    
    @Test
    void testDateFilter() {
        // Given
        SearchRequest request = new SearchRequest();
        request.setDateFrom(LocalDateTime.now().minusDays(1));  // Last 24 hours
        
        // When
        SearchResponse response = searchService.search(request, testUser);
        
        // Then
        assertNotNull(response);
        response.getResults().forEach(product ->
            assertTrue(product.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1)))
        );
    }
    
    @Test
    void testEmptySearch() {
        // Given
        SearchRequest request = new SearchRequest();
        // No query, no filters
        
        // When
        SearchResponse response = searchService.search(request, testUser);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getTotalResults() >= 0);
    }
}

