package com.commandlinecommandos.campusmarketplace.dto;

/**
 * DTO for nested seller information in listing responses
 * Matches frontend mockdata seller object structure
 */
public class SellerSummary {

    private String id;
    private String name;
    private String username;
    private String avatarUrl;

    // Constructors
    public SellerSummary() {
    }

    public SellerSummary(String id, String name, String username, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
