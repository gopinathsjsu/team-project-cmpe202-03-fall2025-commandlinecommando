package com.commandlinecommandos.campusmarketplace.dto;

import jakarta.validation.constraints.NotBlank;

public class ResolveReportRequest {
    
    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;
    
    public String getResolutionNotes() {
        return resolutionNotes;
    }
    
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
