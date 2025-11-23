# Communication Service

A Spring Boot microservice for managing real-time messaging and communication features in the Campus Marketplace application. This service enables buyers and sellers to communicate about listings through a secure, conversation-based messaging system.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Security & Authentication](#security--authentication)
- [Service Integration](#service-integration)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Development](#development)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

## Overview

The Communication Service is a microservice within the Campus Marketplace ecosystem that handles all messaging functionality. It provides:

- **Conversation Management**: Creates and manages conversations between buyers and sellers
- **Message Exchange**: Enables real-time messaging within conversations
- **Read/Unread Tracking**: Tracks message read status for better user experience
- **Listing Integration**: Automatically links conversations to marketplace listings
- **Security**: JWT-based authentication with authorization checks

### Key Design Principles

- **Microservice Architecture**: Independent service that can be scaled separately
- **Stateless**: Uses JWT tokens for authentication (no server-side sessions)
- **Database-Driven**: Uses PostgreSQL for persistence with Flyway migrations
- **RESTful API**: Clean REST endpoints following REST principles
- **Error Handling**: Comprehensive exception handling with meaningful error messages

## Architecture

```
┌─────────────────┐
│   Frontend      │
│   Application   │
└────────┬────────┘
         │ HTTP/REST
         │ JWT Token
         ▼
┌─────────────────────────────────────┐
│     Communication Service           │
│  ┌───────────────────────────────┐  │
│  │   ChatController              │  │
│  │   - Message Endpoints         │  │
│  │   - Conversation Endpoints    │  │
│  └───────────┬───────────────────┘  │
│              │                      │
│  ┌───────────▼───────────────────┐  │
│  │   ChatService                 │  │
│  │   - Business Logic            │  │
│  │   - Authorization Checks      │  │
│  └───────────┬───────────────────┘  │
│              │                      │
│  ┌───────────▼───────────────────┐  │
│  │   Repository Layer            │  │
│  │   - ConversationRepository    │  │
│  │   - MessageRepository         │  │
│  └───────────┬───────────────────┘  │
└──────────────┼──────────────────────┘
               │
               ▼
        ┌──────────────┐
        │  PostgreSQL  │
        │   Database   │
        └──────────────┘
               │
               ▼
        ┌──────────────┐      ┌──────────────┐
        │ Listing API  │      │  Backend API │
        │  (Verify     │      │  (User Info) │
        │  Listings)   │      │              │
        └──────────────┘      └──────────────┘
```

## Features

### Core Features

1. **Conversation Management**
   - Automatic conversation creation when messaging a listing
   - One conversation per listing per buyer-seller pair
   - Conversation retrieval with message history
   - Conversation listing for authenticated users

2. **Messaging**
   - Send messages to listings (auto-creates conversation)
   - Send messages in existing conversations
   - Message content validation (1-5000 characters)
   - Timestamp tracking for all messages

3. **Read/Unread Tracking**
   - Automatic unread status for new messages
   - Mark messages as read (bulk operation)
   - Unread message count per conversation
   - User-specific read status (buyer vs seller)

4. **Authorization & Security**
   - JWT token validation
   - Participant verification (only conversation participants can access)
   - Prevents self-messaging (buyers can't message their own listings)
   - Listing existence verification

5. **Integration**
   - Listing API integration for listing verification
   - Backend service integration for user management
   - Database schema compatibility with main marketplace

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: 
  - PostgreSQL 14+ (production)
  - H2 (development/testing)
- **ORM**: Spring Data JPA with Hibernate 6.6.29
- **Migration**: Flyway
- **Validation**: Jakarta Validation API
- **Authentication**: JWT (JSON Web Tokens)
- **HTTP Client**: Spring RestTemplate
- **Build Tool**: Maven 3.6+
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+ or use included `mvnw`
- PostgreSQL 14+ (for production)
- Access to Listing API service (default: `http://localhost:8100/api`)
- Access to Backend API service (default: `http://localhost:8080/api`)

### Quick Start

1. **Clone and Navigate**
   ```bash
   cd communication
   ```

2. **Build the Application**
   ```bash
   ./mvnw clean install
   # or
   mvn clean install
   ```

3. **Run in Development Mode** (uses H2 in-memory database)
   ```bash
   ./mvnw spring-boot:run
   # or
   make run
   ```

4. **Run in Production Mode** (requires PostgreSQL)
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=campus_marketplace
   export DB_APP_USER=cm_app_user
   export DB_APP_PASSWORD=your_password
   export JWT_SECRET=your_jwt_secret_key
   export LISTING_API_URL=http://localhost:8100/api
   
   ./mvnw spring-boot:run
   ```

The service will be available at `http://localhost:8200`

### Verify Installation

Check the health endpoint:
```bash
curl http://localhost:8200/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## API Documentation

### Base URL

```
http://localhost:8200/api/chat
```

### Authentication

All endpoints require JWT authentication. Include the token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

**JWT Token Requirements:**
- Must be signed with the same secret as the backend service
- Must contain `userId` claim (UUID, converted to Long)
- Must contain `role` claim (e.g., "STUDENT", "ADMIN")

### Endpoints

#### 1. Send Message to Listing

Creates a conversation (if needed) and sends the first message to the seller.

**Endpoint**: `POST /api/chat/messages`

**Request Body**:
```json
{
  "listingId": 123,
  "content": "Is this item still available?"
}
```

**Validation**:
- `listingId`: Required, must be a positive number
- `content`: Required, 1-5000 characters

**Response**: `201 Created`
```json
{
  "messageId": 1,
  "conversationId": 1,
  "senderId": 456,
  "content": "Is this item still available?",
  "isRead": false,
  "createdAt": "2025-11-23T15:30:00"
}
```

**Error Responses**:
- `400 Bad Request`: Invalid request body or validation errors
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Listing does not exist
- `400 Bad Request`: Cannot message your own listing

**Example**:
```bash
curl -X POST http://localhost:8200/api/chat/messages \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "listingId": 123,
    "content": "Is this item still available?"
  }'
```

---

#### 2. Send Message in Conversation

Sends a message in an existing conversation.

**Endpoint**: `POST /api/chat/conversations/{conversationId}/messages`

**Path Parameters**:
- `conversationId`: The conversation ID (required)

**Request Body**:
```json
{
  "content": "Yes, it's still available!"
}
```

**Validation**:
- `content`: Required, 1-5000 characters

**Response**: `201 Created`
```json
{
  "messageId": 2,
  "conversationId": 1,
  "senderId": 789,
  "content": "Yes, it's still available!",
  "isRead": false,
  "createdAt": "2025-11-23T15:35:00"
}
```

**Error Responses**:
- `400 Bad Request`: Invalid request body
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not a participant in the conversation
- `404 Not Found`: Conversation does not exist

**Example**:
```bash
curl -X POST http://localhost:8200/api/chat/conversations/1/messages \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Yes, it's still available!"
  }'
```

---

#### 3. Get All Conversations

Retrieves all conversations for the authenticated user (both as buyer and seller).

**Endpoint**: `GET /api/chat/conversations`

**Response**: `200 OK`
```json
[
  {
    "conversationId": 1,
    "listingId": 123,
    "buyerId": 456,
    "sellerId": 789,
    "createdAt": "2025-11-23T15:30:00",
    "updatedAt": "2025-11-23T15:35:00",
    "messages": [
      {
        "messageId": 1,
        "conversationId": 1,
        "senderId": 456,
        "content": "Is this item still available?",
        "isRead": true,
        "createdAt": "2025-11-23T15:30:00"
      },
      {
        "messageId": 2,
        "conversationId": 1,
        "senderId": 789,
        "content": "Yes, it's still available!",
        "isRead": false,
        "createdAt": "2025-11-23T15:35:00"
      }
    ],
    "unreadCount": 1
  }
]
```

**Error Responses**:
- `401 Unauthorized`: Missing or invalid JWT token

**Example**:
```bash
curl -X GET http://localhost:8200/api/chat/conversations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

#### 4. Get Conversation

Retrieves a specific conversation with all messages.

**Endpoint**: `GET /api/chat/conversations/{conversationId}`

**Path Parameters**:
- `conversationId`: The conversation ID (required)

**Response**: `200 OK`
```json
{
  "conversationId": 1,
  "listingId": 123,
  "buyerId": 456,
  "sellerId": 789,
  "createdAt": "2025-11-23T15:30:00",
  "updatedAt": "2025-11-23T15:35:00",
  "messages": [...],
  "unreadCount": 1
}
```

**Error Responses**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not a participant in the conversation
- `404 Not Found`: Conversation does not exist

**Example**:
```bash
curl -X GET http://localhost:8200/api/chat/conversations/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

#### 5. Get Messages in Conversation

Retrieves all messages in a conversation, ordered by creation time (oldest first).

**Endpoint**: `GET /api/chat/conversations/{conversationId}/messages`

**Path Parameters**:
- `conversationId`: The conversation ID (required)

**Response**: `200 OK`
```json
[
  {
    "messageId": 1,
    "conversationId": 1,
    "senderId": 456,
    "content": "Is this item still available?",
    "isRead": true,
    "createdAt": "2025-11-23T15:30:00"
  },
  {
    "messageId": 2,
    "conversationId": 1,
    "senderId": 789,
    "content": "Yes, it's still available!",
    "isRead": false,
    "createdAt": "2025-11-23T15:35:00"
  }
]
```

**Error Responses**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not a participant in the conversation
- `404 Not Found`: Conversation does not exist

**Example**:
```bash
curl -X GET http://localhost:8200/api/chat/conversations/1/messages \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

#### 6. Get or Create Conversation for Listing

Retrieves an existing conversation for a listing, or creates a new one if it doesn't exist.

**Endpoint**: `GET /api/chat/conversations/listing/{listingId}`

**Path Parameters**:
- `listingId`: The listing ID (required)

**Response**: `200 OK`
```json
{
  "conversationId": 1,
  "listingId": 123,
  "buyerId": 456,
  "sellerId": 789,
  "createdAt": "2025-11-23T15:30:00",
  "updatedAt": "2025-11-23T15:30:00",
  "messages": [],
  "unreadCount": 0
}
```

**Error Responses**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Listing does not exist
- `400 Bad Request`: Cannot create conversation with yourself

**Example**:
```bash
curl -X GET http://localhost:8200/api/chat/conversations/listing/123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

#### 7. Mark Messages as Read

Marks all unread messages in a conversation as read for the authenticated user.

**Endpoint**: `PUT /api/chat/conversations/{conversationId}/read`

**Path Parameters**:
- `conversationId`: The conversation ID (required)

**Response**: `200 OK`
```json
{
  "count": 3
}
```

**Error Responses**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not a participant in the conversation
- `404 Not Found`: Conversation does not exist

**Example**:
```bash
curl -X PUT http://localhost:8200/api/chat/conversations/1/read \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Database Schema

### Tables

#### `conversations`

Stores conversation metadata between buyers and sellers for specific listings.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `conversation_id` | BIGSERIAL | PRIMARY KEY | Auto-generated conversation ID |
| `listing_id` | BIGINT | NOT NULL | Reference to the listing |
| `buyer_id` | BIGINT | NOT NULL | Buyer's user ID |
| `seller_id` | BIGINT | NOT NULL | Seller's user ID |
| `created_at` | TIMESTAMP | NOT NULL | Conversation creation timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

**Unique Constraint**: `(listing_id, buyer_id, seller_id)` - Ensures one conversation per listing per buyer-seller pair

**Indexes**:
- `idx_conversations_listing` on `listing_id`
- `idx_conversations_buyer` on `buyer_id`
- `idx_conversations_seller` on `seller_id`
- `idx_conversations_updated` on `updated_at DESC`

#### `messages`

Stores individual messages within conversations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `message_id` | BIGSERIAL | PRIMARY KEY | Auto-generated message ID |
| `conversation_id` | BIGINT | NOT NULL, FK | Reference to conversation |
| `sender_id` | BIGINT | NOT NULL | Sender's user ID |
| `content` | TEXT | NOT NULL | Message content (1-5000 chars) |
| `is_read` | BOOLEAN | NOT NULL, DEFAULT false | Read status |
| `created_at` | TIMESTAMP | NOT NULL | Message creation timestamp |

**Foreign Key**: `conversation_id` → `conversations.conversation_id` ON DELETE CASCADE

**Indexes**:
- `idx_messages_conversation` on `conversation_id`
- `idx_messages_sender` on `sender_id`
- `idx_messages_created` on `created_at DESC`
- `idx_messages_unread` on `(conversation_id, is_read)` WHERE `is_read = false`

### Database Migrations

The service uses Flyway for database migrations. Migration files are located in:
```
src/main/resources/db/migration/
```

**Current Migration**:
- `V6__communication_chat_tables.sql` - Creates conversations and messages tables with indexes and triggers

### Triggers

The database includes triggers to automatically update `updated_at` timestamps:

```sql
CREATE TRIGGER update_conversation_timestamp
BEFORE UPDATE ON conversations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
```

## Security & Authentication

### JWT Authentication

The service uses JWT (JSON Web Token) authentication with the following flow:

1. **Token Extraction**: `JwtHelper` extracts the token from the `Authorization` header
2. **Token Validation**: `JwtUtil` validates and parses the token using the shared secret
3. **User ID Extraction**: User ID is extracted from the `userId` claim in the token
4. **Authorization**: Service methods verify user participation in conversations

### Security Configuration

- **Spring Security**: Configured to allow all requests (JWT validation at controller level)
- **CORS**: Configured for cross-origin requests (adjust for production)
- **No Session Storage**: Stateless authentication using JWT tokens only

### Authorization Rules

1. **Conversation Access**: Only participants (buyer or seller) can access a conversation
2. **Message Sending**: Only participants can send messages in a conversation
3. **Self-Messaging Prevention**: Users cannot create conversations for their own listings
4. **Listing Verification**: All listing IDs are verified against the Listing API

### JWT Token Structure

The JWT token must contain:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",  // UUID
  "role": "STUDENT",                                   // or "ADMIN"
  "iat": 1234567890,                                  // Issued at
  "exp": 1234571490                                   // Expiration
}
```

## Service Integration

### Listing API Integration

The service integrates with the Listing API to:
- Verify listing existence
- Retrieve seller information
- Validate listing IDs before creating conversations

**Configuration**:
```yaml
listing:
  api:
    url: ${LISTING_API_URL:http://localhost:8100/api}
```

**Endpoints Used**:
- `GET /api/listings/{listingId}` - Retrieves listing information

**Error Handling**:
- If Listing API is unavailable, conversation creation fails with appropriate error
- Logs warnings for Listing API connection issues

### Backend Service Integration

The service integrates with the Backend API for:
- User authentication (JWT token validation)
- User information retrieval (future enhancement)

**Configuration**:
```yaml
backend:
  url: ${BACKEND_URL:http://localhost:8080/api}
```

## Configuration

### Application Properties

Key configuration options in `application.yml`:

#### Server Configuration
```yaml
server:
  port: ${PORT:8200}
```

#### Database Configuration (Production)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:campus_marketplace}
    username: ${DB_APP_USER:cm_app_user}
    password: ${DB_APP_PASSWORD}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 20000
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway for schema management
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

#### JWT Configuration
```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-here}
```

#### Service URLs
```yaml
backend:
  url: ${BACKEND_URL:http://localhost:8080/api}

listing:
  api:
    url: ${LISTING_API_URL:http://localhost:8100/api}
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `PORT` | Server port | `8200` |
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database name | `campus_marketplace` |
| `DB_APP_USER` | Database username | `cm_app_user` |
| `DB_APP_PASSWORD` | Database password | (required) |
| `JWT_SECRET` | JWT signing secret | (required) |
| `BACKEND_URL` | Backend API URL | `http://localhost:8080/api` |
| `LISTING_API_URL` | Listing API URL | `http://localhost:8100/api` |
| `LOG_LEVEL` | Logging level | `INFO` |

### Profiles

#### Development Profile (`dev`)
- Uses H2 in-memory database
- Auto-creates schema on startup
- Enables H2 console at `/h2-console`
- Debug logging enabled

#### Production Profile (`prod`)
- Uses PostgreSQL database
- Flyway migrations enabled
- Production-optimized connection pooling
- Info-level logging

#### Test Profile (`test`)
- Uses H2 in-memory database
- Auto-creates schema for tests
- Security auto-configuration disabled
- Test-optimized logging

## Deployment

### Docker Deployment

#### Build Docker Image
```bash
docker build -t communication-service:latest .
```

#### Run Container
```bash
docker run -d \
  --name communication-service \
  -p 8200:8200 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=campus_marketplace \
  -e DB_APP_USER=cm_app_user \
  -e DB_APP_PASSWORD=your_password \
  -e JWT_SECRET=your_jwt_secret \
  -e LISTING_API_URL=http://listing-api:8100/api \
  communication-service:latest
```

### Docker Compose

The service is included in the main `docker-compose.yml`:

```yaml
communication:
  build: ./communication
  ports:
    - "8200:8200"
  environment:
    SPRING_PROFILES_ACTIVE: prod
    DB_HOST: postgres
    DB_PORT: 5432
    DB_NAME: campus_marketplace
    DB_APP_USER: cm_app_user
    DB_APP_PASSWORD: ${DB_APP_PASSWORD}
    JWT_SECRET: ${JWT_SECRET}
    LISTING_API_URL: http://listing-api:8100/api
  depends_on:
    - postgres
    - listing-api
```

Start with:
```bash
docker-compose up communication
```

### Health Checks

The service exposes health check endpoints:

```bash
# Health check
curl http://localhost:8200/actuator/health

# Detailed health (requires authentication in production)
curl http://localhost:8200/actuator/health/readiness
curl http://localhost:8200/actuator/health/liveness
```

## Development

### Project Structure

```
communication/
├── src/
│   ├── main/
│   │   ├── java/com/commandlinecommandos/communication/
│   │   │   ├── CommunicationApplication.java      # Main application class
│   │   │   ├── config/
│   │   │   │   └── RestTemplateConfig.java        # REST client configuration
│   │   │   ├── controller/
│   │   │   │   ├── ChatController.java            # REST API endpoints
│   │   │   │   └── HomeController.java            # Health check endpoint
│   │   │   ├── service/
│   │   │   │   ├── ChatService.java               # Business logic
│   │   │   │   └── ListingService.java            # Listing API integration
│   │   │   ├── repository/
│   │   │   │   ├── ConversationRepository.java    # Conversation data access
│   │   │   │   └── MessageRepository.java        # Message data access
│   │   │   ├── model/
│   │   │   │   ├── Conversation.java              # Conversation entity
│   │   │   │   └── Message.java                   # Message entity
│   │   │   ├── dto/
│   │   │   │   ├── ConversationResponse.java     # Conversation DTO
│   │   │   │   ├── MessageResponse.java           # Message DTO
│   │   │   │   ├── CreateMessageRequest.java     # Request DTO
│   │   │   │   ├── SendMessageRequest.java        # Request DTO
│   │   │   │   └── MessageCountResponse.java      # Response DTO
│   │   │   ├── security/
│   │   │   │   ├── JwtUtil.java                   # JWT parsing utilities
│   │   │   │   ├── JwtHelper.java                 # JWT extraction helper
│   │   │   │   └── SecurityConfig.java            # Spring Security config
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java    # Exception handling
│   │   │       ├── ConversationException.java     # Custom exceptions
│   │   │       ├── ConversationNotFoundException.java
│   │   │       ├── UnauthorizedAccessException.java
│   │   │       └── ErrorResponse.java             # Error DTO
│   │   └── resources/
│   │       ├── application.yml                    # Configuration
│   │       └── db/migration/
│   │           └── V6__communication_chat_tables.sql  # Database migration
│   └── test/
│       └── java/com/commandlinecommandos/communication/
│           ├── controller/
│           │   └── ChatControllerTest.java        # Controller tests
│           └── service/
│               ├── ChatServiceTest.java            # Service tests
│               └── ListingServiceTest.java         # Integration tests
├── Dockerfile
├── Makefile
├── pom.xml
└── README.md
```

### Building

```bash
# Clean and build
./mvnw clean install

# Skip tests
./mvnw clean install -DskipTests

# Build with specific profile
./mvnw clean install -Pprod
```

### Running Locally

```bash
# Development mode (H2 database)
./mvnw spring-boot:run

# Production mode (PostgreSQL)
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods focused and single-purpose
- Use dependency injection (avoid `new` for services)

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ChatControllerTest

# Run with coverage
./mvnw test jacoco:report
```

### Test Structure

- **Unit Tests**: Test individual service methods with mocked dependencies
- **Integration Tests**: Test controller endpoints with in-memory database
- **Repository Tests**: Test database queries and operations

### Test Coverage

Current test coverage includes:
- ✅ ChatController endpoints
- ✅ ChatService business logic
- ✅ ListingService integration
- ✅ Exception handling
- ✅ Authorization checks

### Example Test

```java
@Test
void testSendMessageToListing() {
    // Given
    Long listingId = 123L;
    Long buyerId = 456L;
    String content = "Is this available?";
    
    // When
    Message message = chatService.sendMessageToListing(listingId, buyerId, content);
    
    // Then
    assertNotNull(message);
    assertEquals(content, message.getContent());
    assertEquals(buyerId, message.getSenderId());
}
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Errors

**Symptoms**: `Connection refused` or `Authentication failed`

**Solutions**:
- Verify PostgreSQL is running: `pg_isready`
- Check database credentials in environment variables
- Verify database exists: `psql -l`
- Check network connectivity to database host
- Verify user has proper permissions

#### 2. JWT Authentication Failures

**Symptoms**: `401 Unauthorized` responses

**Solutions**:
- Verify JWT secret matches backend service secret
- Check token expiration (tokens expire after configured time)
- Ensure token is in correct format: `Bearer <token>`
- Verify token contains `userId` and `role` claims
- Check token signature is valid

#### 3. Listing API Connection Issues

**Symptoms**: `ConversationException: Listing not found`

**Solutions**:
- Verify Listing API is running: `curl http://localhost:8100/actuator/health`
- Check `LISTING_API_URL` environment variable
- Verify network connectivity between services
- Check Listing API logs for errors
- Ensure listing ID exists in Listing API

#### 4. Port Already in Use

**Symptoms**: `Port 8200 is already in use`

**Solutions**:
- Find process using port: `lsof -i :8200` (macOS/Linux) or `netstat -ano | findstr :8200` (Windows)
- Kill the process or change port: `export PORT=8201`

#### 5. Flyway Migration Errors

**Symptoms**: `FlywayException` or schema validation errors

**Solutions**:
- Check database schema matches migration files
- Verify Flyway baseline is set correctly
- Check for conflicting migrations
- Review Flyway history table: `SELECT * FROM flyway_schema_history`
- Reset Flyway if needed (development only)

#### 6. Message Content Validation Errors

**Symptoms**: `400 Bad Request` with validation errors

**Solutions**:
- Ensure message content is 1-5000 characters
- Check request body format is valid JSON
- Verify `content` field is present and not empty
- Check for special characters that might cause issues

### Debugging

#### Enable Debug Logging

```yaml
logging:
  level:
    com.commandlinecommandos.communication: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

#### Check Application Logs

```bash
# View logs
tail -f logs/communication.log

# Search for errors
grep ERROR logs/communication.log

# Search for specific user
grep "userId: 456" logs/communication.log
```

#### Database Queries

```sql
-- Check conversations
SELECT * FROM conversations WHERE buyer_id = 456 OR seller_id = 456;

-- Check messages
SELECT * FROM messages WHERE conversation_id = 1 ORDER BY created_at;

-- Check unread messages
SELECT COUNT(*) FROM messages 
WHERE conversation_id = 1 
  AND sender_id != 456 
  AND is_read = false;
```

### Performance Tuning

#### Database Connection Pool

Adjust HikariCP settings in `application.yml`:
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      connection-timeout: 30000
```

#### Query Optimization

- Use indexes for frequently queried columns
- Consider pagination for large message lists
- Use `@Transactional(readOnly = true)` for read operations

## License

This project is part of the CMPE-202 Campus Marketplace application.

## Support

For issues and questions:
1. Check this documentation
2. Review application logs
3. Check the main project documentation
4. Contact the development team

---

**Last Updated**: November 2025  
**Version**: 0.0.1-SNAPSHOT  
**Spring Boot Version**: 3.5.6
