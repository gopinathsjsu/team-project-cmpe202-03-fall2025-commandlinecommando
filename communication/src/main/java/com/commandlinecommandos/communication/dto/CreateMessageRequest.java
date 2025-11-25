package com.commandlinecommandos.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateMessageRequest {
    
    @NotNull(message = "Listing ID is required")
    private Long listingId;
    
    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
    private String content;
    
    public CreateMessageRequest() {
    }
    
    public CreateMessageRequest(Long listingId, String content) {
        this.listingId = listingId;
        this.content = content;
    }
    
    public Long getListingId() {
        return listingId;
    }
    
    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}

