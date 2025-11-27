package com.commandlinecommandos.aiintegration.service;

import com.commandlinecommandos.aiintegration.dto.ChatMessage;
import com.commandlinecommandos.aiintegration.dto.ChatRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AIService {

    @Autowired(required = false)
    private OpenAiService openAiService;

    @Autowired
    private boolean hasApiKey;

    public String processChat(ChatRequest chatRequest) {
        if (!hasApiKey || openAiService == null) {
            throw new IllegalStateException("OpenAI API key is not configured. Please add OPENAI_API_KEY to your environment variables.");
        }

        try {
            String systemPrompt = buildSystemPrompt(chatRequest.getListingsContext(), chatRequest.getReportsContext());

            List<com.theokanning.openai.completion.chat.ChatMessage> messages = new ArrayList<>();

            // Add system message
            messages.add(new com.theokanning.openai.completion.chat.ChatMessage("system", systemPrompt));

            // Add conversation messages
            if (chatRequest.getMessages() != null) {
                for (ChatMessage msg : chatRequest.getMessages()) {
                    messages.add(new com.theokanning.openai.completion.chat.ChatMessage(msg.getRole(), msg.getContent()));
                }
            }

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(1000)
                    .build();

            List<ChatCompletionChoice> choices = openAiService.createChatCompletion(completionRequest).getChoices();

            if (choices != null && !choices.isEmpty()) {
                return choices.get(0).getMessage().getContent();
            }

            return "Sorry, I could not generate a response.";

        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to get AI response: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String listingsContext, String reportsContext) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a helpful AI assistant for CampusConnect, a university campus marketplace. ");
        prompt.append("You help students find products, answer questions about available listings, and provide information about reported listings.\n\n");

        prompt.append("Here are the current marketplace listings:\n");
        prompt.append(listingsContext != null ? listingsContext : "No listings available.");

        if (reportsContext != null && !reportsContext.trim().isEmpty()) {
            prompt.append("\n\nHere are the reported listings (these are listings that users have flagged for various reasons):\n");
            prompt.append(reportsContext);
            prompt.append("\n\nReport types include: SPAM, INAPPROPRIATE, SCAM, WRONG_CATEGORY\n");
            prompt.append("Report statuses include: PENDING, UNDER_REVIEW, RESOLVED, DISMISSED");
        }

        prompt.append("\n\nBased on this data, answer the user's questions helpfully. ");
        prompt.append("If they ask about products, prices, or recommendations, use the listing data to provide accurate answers. ");
        prompt.append("If they ask about reported listings or reports, provide information from the reports data. ");
        prompt.append("Format your responses nicely with clear information. ");
        prompt.append("If a question cannot be answered with the available data, let them know politely.");

        return prompt.toString();
    }
}
