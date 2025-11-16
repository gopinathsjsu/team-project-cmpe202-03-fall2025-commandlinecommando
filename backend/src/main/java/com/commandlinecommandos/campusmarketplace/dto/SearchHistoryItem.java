package com.commandlinecommandos.campusmarketplace.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for search history items
 * Used to avoid Hibernate proxy serialization issues
 */
public class SearchHistoryItem {
    private UUID id;
    private String searchQuery;
    private Integer resultsCount;
    private LocalDateTime createdAt;
    
    public SearchHistoryItem() {
    }
    
    public SearchHistoryItem(UUID id, String searchQuery, Integer resultsCount, LocalDateTime createdAt) {
        this.id = id;
        this.searchQuery = searchQuery;
        this.resultsCount = resultsCount;
        this.createdAt = createdAt;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    public Integer getResultsCount() {
        return resultsCount;
    }
    
    public void setResultsCount(Integer resultsCount) {
        this.resultsCount = resultsCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

