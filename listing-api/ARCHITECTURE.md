# Listing API Architecture Documentation

## Overview
This document describes the architecture, design patterns, and technical implementation of the Listing API microservice.

## Architecture Overview

### High-Level Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Database      │
│   (React/Vue)   │◄──►│   (Spring Boot) │◄──►│   (PostgreSQL)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   File Storage  │
                       │   (Local/Disk)  │
                       └─────────────────┘
```

### Microservice Architecture
The Listing API follows microservice principles:
- **Single Responsibility**: Handles only listing-related operations
- **Loose Coupling**: Minimal dependencies on other services
- **Independent Deployment**: Can be deployed and scaled independently
- **Technology Agnostic**: Uses standard REST APIs for communication

## Design Patterns

### 1. Layered Architecture
```
┌─────────────────────────────────────┐
│           Controller Layer          │  ← REST API Endpoints
├─────────────────────────────────────┤
│           Service Layer             │  ← Business Logic
├─────────────────────────────────────┤
│          Repository Layer           │  ← Data Access
├─────────────────────────────────────┤
│           Model Layer               │  ← Data Entities
└─────────────────────────────────────┘
```

### 2. MVC Pattern
- **Model**: JPA entities representing data structures
- **View**: JSON responses for API consumers
- **Controller**: REST controllers handling HTTP requests

### 3. Repository Pattern
- Abstracts data access logic
- Provides type-safe query methods
- Enables easy testing and mocking

### 4. Service Layer Pattern
- Encapsulates business logic
- Provides transaction management
- Acts as a facade for complex operations

## Component Architecture

### Core Components

#### 1. Controllers
**Location**: `com.commandlinecommandos.listingapi.controller`

**Responsibilities**:
- Handle HTTP requests and responses
- Input validation and sanitization
- Authentication and authorization
- Error handling and status codes

**Key Controllers**:
- `ListingController`: Main listing operations
- `FileUploadController`: Image management
- `HomeController`: Basic health checks
- `TestController`: Development testing

**Design Principles**:
- Thin controllers with minimal business logic
- Consistent error handling
- Proper HTTP status codes
- Input validation using Jakarta Validation

#### 2. Services
**Location**: `com.commandlinecommandos.listingapi.service`

**Responsibilities**:
- Business logic implementation
- Transaction management
- Data transformation
- Cross-cutting concerns

**Key Services**:
- `ListingService`: Core listing business logic
- `FileStorageService`: File upload and management

**Design Principles**:
- Stateless services
- Clear separation of concerns
- Comprehensive error handling
- Transactional operations

#### 3. Repositories
**Location**: `com.commandlinecommandos.listingapi.repository`

**Responsibilities**:
- Data persistence operations
- Query optimization
- Database interaction abstraction

**Key Repositories**:
- `ListingRepository`: Listing data operations
- `ListingImageRepository`: Image data operations

**Design Principles**:
- Spring Data JPA integration
- Custom query methods
- Optimized database queries
- Proper indexing strategies

#### 4. Models
**Location**: `com.commandlinecommandos.listingapi.model`

**Responsibilities**:
- Data structure definition
- Business rules enforcement
- Validation constraints

**Key Models**:
- `Listing`: Main listing entity
- `ListingImage`: Image metadata
- Enums: `Category`, `ItemCondition`, `ListingStatus`

**Design Principles**:
- JPA annotations for persistence
- Jakarta Validation constraints
- Immutable value objects where appropriate
- Proper relationship mapping

#### 5. Exceptions & Global Exception Handling
**Location**: `com.commandlinecommandos.listingapi.exception`

**Responsibilities**:
- Custom exception handling
- Error categorization
- Consistent error responses
- Global exception management

**Key Components**:
- `GlobalExceptionHandler`: Centralized exception handling with `@ControllerAdvice`
- `ErrorResponse`: Standardized error response DTO
- Custom business exceptions for domain-specific errors

**Key Exceptions**:
- `ListingException`: General listing errors
- `FileStorageException`: File operation errors
- `ListingNotFoundException`: Listing not found errors
- `ReportNotFoundException`: Report not found errors
- `UnauthorizedAccessException`: Authorization errors
- `ValidationException`: Custom validation errors
- `FileUploadException`: File upload specific errors

## Data Flow Architecture

### 1. Request Processing Flow
```
HTTP Request → Controller → Validation → Service → Repository → Database
     ↓
HTTP Response ← JSON Serialization ← Business Logic ← Data Processing
```

### 2. Listing Creation Flow
```
POST /api/listings/
    ↓
CreateListingRequest → Validation → ListingService.createListing()
    ↓
Listing Entity → Repository.save() → Database
    ↓
Listing Response ← JSON Serialization ← Created Entity
```

### 3. Search and Filtering Flow
```
GET /api/listings/search?keyword=MacBook&category=ELECTRONICS
    ↓
Query Parameters → Validation → ListingService.getListingsWithFilters()
    ↓
Repository.findWithFilters() → Database Query → Results
    ↓
Page<Listing> ← Pagination ← Sorted Results ← JSON Response
```

### 4. Image Upload Flow
```
POST /api/files/upload/{listingId}
    ↓
MultipartFile → FileStorageService.storeFile() → File System
    ↓
ListingImage Entity → Repository.save() → Database
    ↓
Success Response ← File Metadata ← Stored File
```

## Database Architecture

### Entity Relationships
```
Listing (1) ←→ (N) ListingImage
    ↓
Seller (Future Integration)
```

### Database Schema
```sql
-- Listings table
CREATE TABLE listings (
    listing_id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    condition VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    view_count INTEGER DEFAULT 0
);

-- Listing Images table
CREATE TABLE listing_images (
    image_id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(listing_id),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Indexing Strategy
```sql
-- Performance indexes
CREATE INDEX idx_listings_status_created ON listings(status, created_at DESC);
CREATE INDEX idx_listings_seller_id ON listings(seller_id);
CREATE INDEX idx_listings_category ON listings(category);
CREATE INDEX idx_listings_price ON listings(price);
CREATE INDEX idx_listings_location ON listings(location);
CREATE INDEX idx_listings_title_desc ON listings USING gin(to_tsvector('english', title || ' ' || description));
CREATE INDEX idx_listing_images_listing_id ON listing_images(listing_id);
```

## Exception Handling Architecture

### 1. Global Exception Handler
The application implements a comprehensive global exception handling strategy using Spring's `@ControllerAdvice` pattern.

**Architecture**:
```
Exception → GlobalExceptionHandler → ErrorResponse → JSON Response
```

**Key Features**:
- **Centralized Handling**: Single point for all exception processing
- **Consistent Format**: All errors return standardized `ErrorResponse` structure
- **Comprehensive Coverage**: Handles business, validation, HTTP, and file upload exceptions
- **Detailed Logging**: Structured logging with appropriate log levels
- **Security**: Prevents information leakage in error responses

### 2. Error Response Structure
```java
public class ErrorResponse {
    private String error;           // Machine-readable error code
    private String message;         // Human-readable description
    private int status;            // HTTP status code
    private LocalDateTime timestamp; // Auto-generated timestamp
    private String path;           // Request path where error occurred
    private List<String> validationErrors; // Field-level validation errors
}
```

### 3. Exception Categories

#### Business Exceptions
- **Domain-Specific**: Custom exceptions for business logic violations
- **HTTP Status Mapping**: Appropriate status codes (400, 403, 404, 500)
- **User-Friendly Messages**: Clear, actionable error messages

#### Validation Exceptions
- **Field-Level Details**: Specific validation error messages
- **Multiple Errors**: Support for multiple validation failures
- **Constraint Violations**: Jakarta Validation constraint handling

#### HTTP Exceptions
- **Request Processing**: Malformed requests, missing parameters
- **Method Validation**: Unsupported HTTP methods
- **Content Type**: Invalid JSON, file upload issues

#### File Upload Exceptions
- **Size Limits**: File size exceeded handling
- **Type Validation**: Unsupported file types
- **Storage Issues**: File system errors

### 4. Error Handling Flow
```
Controller Method
    ↓ (throws exception)
GlobalExceptionHandler
    ↓ (catches and processes)
ErrorResponse Creation
    ↓ (logs exception)
JSON Response
    ↓ (returns to client)
Client receives structured error
```

### 5. Logging Strategy
- **Error Level**: Business exceptions and validation errors
- **Warn Level**: Client errors (400-level)
- **Debug Level**: Detailed stack traces for debugging
- **Structured Logging**: Consistent log format with context

## Security Architecture

### 1. Authentication & Authorization
**Current State**: Placeholder authentication
**Future Integration**: JWT-based authentication with main auth service

**Security Layers**:
```
Request → CORS → Authentication → Authorization → Controller → Service
```

### 2. Error Response Security
- **Information Hiding**: Prevents sensitive data leakage in error responses
- **Consistent Format**: Standardized responses prevent information disclosure
- **Path Information**: Includes request path for debugging without exposing internals

### 3. Input Validation
- Jakarta Validation annotations
- Request body validation
- File upload validation
- SQL injection prevention (JPA/Hibernate)

### 3. File Upload Security
- File type validation
- File size limits
- Secure file storage paths
- Filename sanitization

## Performance Architecture

### 1. Caching Strategy
**Current**: No caching implemented
**Recommendations**:
- Redis for frequently accessed listings
- Database query result caching
- File serving optimization

### 2. Database Optimization
- Proper indexing
- Query optimization
- Connection pooling
- Pagination for large datasets

### 3. File Storage Optimization
- Local file system (current)
- Future: Cloud storage (AWS S3, Google Cloud Storage)
- CDN integration for image serving

## Scalability Considerations

### 1. Horizontal Scaling
- Stateless service design
- Database connection pooling
- Load balancer compatibility
- Container deployment ready

### 2. Database Scaling
- Read replicas for search operations
- Partitioning by seller_id or date
- Database sharding considerations

### 3. File Storage Scaling
- Distributed file storage
- CDN integration
- Image optimization and resizing

## Monitoring and Observability

### 1. Logging
- Structured logging with SLF4J
- Request/response logging
- Error tracking and alerting
- Performance metrics

### 2. Health Checks
- Spring Boot Actuator endpoints
- Database connectivity checks
- File storage availability
- Custom health indicators

### 3. Metrics
- Request count and latency
- Database query performance
- File upload metrics
- Error rates and types

## Deployment Architecture

### 1. Container Deployment
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/listingapi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8100
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 2. Configuration Management
- Environment-based configuration
- Externalized properties
- Secrets management
- Feature flags

### 3. Service Discovery
- Kubernetes service discovery
- Load balancer configuration
- Health check endpoints
- Circuit breaker patterns

## Integration Architecture

### 1. API Gateway Integration
```
Client → API Gateway → Listing API
                ↓
           Authentication Service
                ↓
           Database Service
```

### 2. Event-Driven Architecture
**Future Enhancements**:
- Listing creation events
- Status change notifications
- Search index updates
- Analytics events

### 3. Inter-Service Communication
- REST API communication
- Event publishing/subscribing
- Circuit breaker patterns
- Retry mechanisms

## Technology Stack

### Core Technologies
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL (production), H2 (development)
- **ORM**: Spring Data JPA
- **Validation**: Jakarta Validation
- **Session**: Spring Session JDBC

### Development Tools
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Documentation**: Spring Boot Actuator
- **Development**: Spring Boot DevTools

### Production Tools
- **Monitoring**: Spring Boot Actuator
- **Logging**: SLF4J with Logback
- **Security**: Spring Security (future)
- **Deployment**: Docker, Kubernetes

## Future Architecture Considerations

### 1. Event Sourcing
- Audit trail for listing changes
- Event replay capabilities
- Temporal queries

### 2. CQRS (Command Query Responsibility Segregation)
- Separate read and write models
- Optimized query performance
- Event-driven updates

### 3. Microservice Patterns
- Saga pattern for distributed transactions
- API composition
- Service mesh integration

### 4. Advanced Features
- Real-time search with Elasticsearch
- Recommendation engine integration
- Machine learning for price optimization
- Advanced analytics and reporting

This architecture provides a solid foundation for the Listing API while maintaining flexibility for future enhancements and scalability requirements.
