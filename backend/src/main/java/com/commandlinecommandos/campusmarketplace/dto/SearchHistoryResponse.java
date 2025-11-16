package com.commandlinecommandos.campusmarketplace.dto;

import java.util.List;

/**
 * Response wrapper for search history
 */
public class SearchHistoryResponse {
    private List<SearchHistoryItem> history;

    public SearchHistoryResponse() {
    }

    public SearchHistoryResponse(List<SearchHistoryItem> history) {
        this.history = history;
    }

    public List<SearchHistoryItem> getHistory() {
        return history;
    }

    public void setHistory(List<SearchHistoryItem> history) {
        this.history = history;
    }
}

