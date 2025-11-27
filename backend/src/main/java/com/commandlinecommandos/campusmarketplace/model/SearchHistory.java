package com.commandlinecommandos.campusmarketplace.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Search History entity
 * Tracks user search queries for recent searches and analytics
 */
@Entity
@Table(name = "search_history", indexes = {
    @Index(name = "idx_search_history_user", columnList = "user_id,created_at"),
    @Index(name = "idx_search_history_query", columnList = "search_query"),
    @Index(name = "idx_search_history_created", columnList = "created_at")
})
public class SearchHistory {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "search_id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "search_query", nullable = false, length = 500)
    private String searchQuery;
    
    @Column(name = "results_count")
    private Integer resultsCount;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public SearchHistory() {
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
