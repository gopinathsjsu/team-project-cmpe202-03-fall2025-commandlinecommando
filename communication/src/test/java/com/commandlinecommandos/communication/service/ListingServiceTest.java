package com.commandlinecommandos.communication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ListingService listingService;

    private Long testListingId = 123L;
    private Long testSellerId = 789L;
    private String testListingApiUrl = "http://localhost:8100/api";

    @BeforeEach
    void setUp() {
        listingService = new ListingService(restTemplate, testListingApiUrl);
    }

    @Test
    void testGetListing_Success() {
        // Arrange
        Map<String, Object> listingData = new HashMap<>();
        listingData.put("listingId", testListingId);
        listingData.put("sellerId", testSellerId);
        listingData.put("title", "Test Listing");

        ResponseEntity<Map> response = new ResponseEntity<>(listingData, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
            .thenReturn(response);

        // Act
        Map<String, Object> result = listingService.getListing(testListingId);

        // Assert
        assertNotNull(result);
        assertEquals(testListingId, result.get("listingId"));
        assertEquals(testSellerId, result.get("sellerId"));
        assertEquals("Test Listing", result.get("title"));
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testGetListing_NotFound() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
            .thenThrow(new RestClientException("Listing not found"));

        // Act
        Map<String, Object> result = listingService.getListing(testListingId);

        // Assert
        assertNull(result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testGetSellerId_Success() {
        // Arrange
        Map<String, Object> listingData = new HashMap<>();
        listingData.put("listingId", testListingId);
        listingData.put("sellerId", testSellerId);

        ResponseEntity<Map> response = new ResponseEntity<>(listingData, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
            .thenReturn(response);

        // Act
        Long result = listingService.getSellerId(testListingId);

        // Assert
        assertNotNull(result);
        assertEquals(testSellerId, result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testGetSellerId_ListingNotFound() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
            .thenThrow(new RestClientException("Listing not found"));

        // Act
        Long result = listingService.getSellerId(testListingId);

        // Assert
        assertNull(result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testGetSellerId_NoSellerIdInResponse() {
        // Arrange
        Map<String, Object> listingData = new HashMap<>();
        listingData.put("listingId", testListingId);
        // No sellerId in response

        ResponseEntity<Map> response = new ResponseEntity<>(listingData, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
            .thenReturn(response);

        // Act
        Long result = listingService.getSellerId(testListingId);

        // Assert
        assertNull(result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testListingExists_True() {
        // Arrange
        Map<String, Object> listingData = new HashMap<>();
        listingData.put("listingId", testListingId);

        ResponseEntity<Map> response = new ResponseEntity<>(listingData, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
            .thenReturn(response);

        // Act
        boolean result = listingService.listingExists(testListingId);

        // Assert
        assertTrue(result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testListingExists_False() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
            .thenThrow(new RestClientException("Listing not found"));

        // Act
        boolean result = listingService.listingExists(testListingId);

        // Assert
        assertFalse(result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }
}

