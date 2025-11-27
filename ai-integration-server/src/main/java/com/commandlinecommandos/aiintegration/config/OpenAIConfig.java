package com.commandlinecommandos.aiintegration.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Bean
    public OpenAiService openAiService() {
        if (apiKey == null || apiKey.isEmpty()) {
            return null;
        }
        return new OpenAiService(apiKey, Duration.ofSeconds(60));
    }

    @Bean
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }
}
