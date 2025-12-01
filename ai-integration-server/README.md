# AI Integration Server

A Spring Boot service that provides AI-powered chat functionality for the Campus Marketplace using OpenAI's GPT models.

## Overview

Handles AI chat requests from the frontend. Users can ask about listings, get recommendations, or get help with the marketplace.

## Quick Start

### Prerequisites

- Java 17+
- Maven
- OpenAI API key

### Running Locally

```bash
# Set your OpenAI API key
export OPENAI_API_KEY=sk-your-api-key

# Run the server
./mvnw spring-boot:run
```

The server starts on port 3001.

### Using Docker

```bash
# From project root, start all services (AI service starts by default)
docker-compose -f docker-compose.prod.yml up -d

# Or start only the AI service
docker-compose -f docker-compose.prod.yml up -d ai-integration-server
```

## API Endpoints

### Health Check

```
GET /api/health
```

Returns service health status.

### Chat

```
POST /api/ai/chat
Content-Type: application/json

{
  "messages": [
    {"role": "user", "content": "What electronics are available?"}
  ]
}
```

Response:

```json
{
  "response": "I can help you find electronics...",
  "timestamp": "2024-11-30T12:00:00Z"
}
```

## Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| OPENAI_API_KEY | Your OpenAI API key | Required |
| PORT | Server port | 3001 |
| LOG_LEVEL | Logging level | INFO |

## Project Structure

```
src/main/java/com/commandlinecommandos/aiintegration/
├── AiIntegrationServerApplication.java   # Main application
├── config/
│   └── OpenAIConfig.java                 # OpenAI client configuration
├── controller/
│   └── AIController.java                 # REST endpoints
├── dto/
│   ├── ChatRequest.java                  # Request model
│   ├── ChatResponse.java                 # Response model
│   └── ChatMessage.java                  # Message model
└── service/
    └── AIService.java                    # OpenAI integration logic
```

## Frontend Integration

The frontend connects to this service via the `VITE_AI_API_SERVICE_URL` environment variable:

```typescript
// frontend/src/api/config.ts
AI_SERVICE_URL: import.meta.env.VITE_AI_API_SERVICE_URL || 'http://localhost:3001'
```

## Notes

- This service is optional. The main application works without it.
- OpenAI API calls are rate-limited and billed per token.
- Responses are cached where appropriate to reduce API costs.

