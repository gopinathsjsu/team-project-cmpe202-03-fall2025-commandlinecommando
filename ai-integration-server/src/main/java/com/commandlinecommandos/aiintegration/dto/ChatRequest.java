package com.commandlinecommandos.aiintegration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private List<ChatMessage> messages;
    private String listingsContext;
    private String reportsContext;
}
