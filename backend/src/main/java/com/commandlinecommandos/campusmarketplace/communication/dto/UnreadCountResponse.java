package com.commandlinecommandos.campusmarketplace.communication.dto;

public class UnreadCountResponse {
    
    private long unreadCount;
    
    public UnreadCountResponse() {
    }
    
    public UnreadCountResponse(long unreadCount) {
        this.unreadCount = unreadCount;
    }
    
    public long getUnreadCount() {
        return unreadCount;
    }
    
    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
