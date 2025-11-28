package com.commandlinecommandos.campusmarketplace.communication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CreateMessageRequest {
    
    @NotNull(message = "Listing ID is required")
    private UUID listingId;
    
    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
    private String content;
    
    public CreateMessageRequest() {
    }
    
    public CreateMessageRequest(UUID listingId, String content) {
        this.listingId = listingId;
        this.content = content;
    }
    
    public UUID getListingId() {
        return listingId;
    }
    
    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}
