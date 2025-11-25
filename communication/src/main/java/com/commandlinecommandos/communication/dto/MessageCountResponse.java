package com.commandlinecommandos.communication.dto;

public class MessageCountResponse {
    
    private int count;
    
    public MessageCountResponse() {
    }
    
    public MessageCountResponse(int count) {
        this.count = count;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
}

