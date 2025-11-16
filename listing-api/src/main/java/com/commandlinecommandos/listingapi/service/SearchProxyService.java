package com.commandlinecommandos.listingapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

/**
 * Service for proxying search requests to the main backend
 * Implements the Proxy Pattern for backward compatibility
 */
@Service
public class SearchProxyService {
    
    private final RestTemplate restTemplate;
    private final String backendUrl;
    
    /**
     * Constructor with RestTemplate configuration
     * Sets connection and read timeouts
     * 
     * @param backendUrl Backend base URL
     * @param restTemplateBuilder RestTemplateBuilder for configuration
     */
    public SearchProxyService(
            @Value("${backend.url}") String backendUrl,
            RestTemplateBuilder restTemplateBuilder) {
        
        this.backendUrl = backendUrl;
        
        // Configure timeouts using SimpleClientHttpRequestFactory
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
        
        this.restTemplate = restTemplateBuilder
            .rootUri(backendUrl)
            .requestFactory(() -> factory)
            .build();
    }
    
    /**
     * Proxy search request to backend /api/search endpoint
     * 
     * @param request Search request body (as Map to avoid tight coupling)
     * @param token Authorization token
     * @return Search response from backend
     * @throws RestClientException if proxy request fails
     */
    public Map<String, Object> proxySearchRequest(Map<String, Object> request, String token) 
            throws RestClientException {
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.set("Content-Type", "application/json");
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
            "/search",
            entity,
            (Class<Map<String, Object>>) (Class<?>) Map.class
        );
        
        return response.getBody();
    }
    
    /**
     * Proxy autocomplete request to backend
     * 
     * @param query Search query
     * @param token Authorization token
     * @return List of suggestions
     * @throws RestClientException if proxy request fails
     */
    public Object proxyAutocompleteRequest(String query, String token) 
            throws RestClientException {
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Object> response = restTemplate.exchange(
            "/search/autocomplete?q=" + query,
            HttpMethod.GET,
            entity,
            Object.class
        );
        
        return response.getBody();
    }
    
    /**
     * Proxy discovery request to backend
     * 
     * @param endpoint Discovery endpoint (trending, recommended, etc.)
     * @param params Query parameters
     * @param token Authorization token
     * @return Discovery response from backend
     * @throws RestClientException if proxy request fails
     */
    public Object proxyDiscoveryRequest(String endpoint, Map<String, String> params, String token) 
            throws RestClientException {
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        // Build query string from params
        StringBuilder queryString = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            queryString.append("?");
            params.forEach((key, value) -> 
                queryString.append(key).append("=").append(value).append("&")
            );
            // Remove trailing &
            if (queryString.length() > 1) {
                queryString.deleteCharAt(queryString.length() - 1);
            }
        }
        
        ResponseEntity<Object> response = restTemplate.exchange(
            "/discovery/" + endpoint + queryString.toString(),
            HttpMethod.GET,
            entity,
            Object.class
        );
        
        return response.getBody();
    }
}

