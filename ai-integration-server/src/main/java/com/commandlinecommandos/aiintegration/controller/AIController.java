package com.commandlinecommandos.aiintegration.controller;

import com.commandlinecommandos.aiintegration.dto.ChatRequest;
import com.commandlinecommandos.aiintegration.dto.ChatResponse;
import com.commandlinecommandos.aiintegration.dto.ErrorResponse;
import com.commandlinecommandos.aiintegration.dto.HealthResponse;
import com.commandlinecommandos.aiintegration.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private boolean hasApiKey;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest chatRequest) {
        try {
            log.info("Received chat request with {} messages",
                    chatRequest.getMessages() != null ? chatRequest.getMessages().size() : 0);

            String response = aiService.processChat(chatRequest);
            return ResponseEntity.ok(new ChatResponse(response));

        } catch (IllegalStateException e) {
            log.error("Configuration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("ok", hasApiKey));
    }
}
