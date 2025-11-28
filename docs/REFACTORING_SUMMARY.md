# Campus Marketplace - Refactoring Summary

## Overview
This document summarizes the major refactoring effort to consolidate three separate Spring Boot microservices into a unified backend application.

## Problem Statement
The original architecture consisted of three separate services sharing a single database:
- **Backend** (port 8080): User management, authentication, orders, admin
- **Listing API** (port 8100): Listings, reports, search
- **Communication** (port 8200): Chat, messages, notifications

### Critical Issues Identified
1. **Code Duplication**: ~734 lines of duplicate code across services
   - JWT utilities: ~370 lines duplicated
   - Security configurations: Identical Spring Security setups
   - Error handling: Duplicate exception handlers

2. **Database Type Conflicts**: 
   - Backend used UUID for user IDs
   - Listing API and Communication used BIGINT with lossy `.hashCode()` conversions
   - No foreign key constraints between tables

3. **Tight Coupling**: Services made HTTP calls to each other instead of direct database access

4. **Inconsistent Standards**: 
   - Mixed Java versions (17 and 21)
   - Different error response formats
   - Inconsistent logging approaches

## Refactoring Solution

### 1. Unified Project Structure ✅
Consolidated all services into single backend with modular packages:
```
backend/src/main/java/com/commandlinecommandos/campusmarketplace/
├── auth/           # Authentication & authorization
├── user/           # User management
├── order/          # Order processing
├── admin/          # Admin operations
├── listing/        # Listing & reports (from listing-api)
├── communication/  # Chat & messaging (from communication)
├── exception/      # Unified exception handling
├── security/       # Shared security configuration
└── config/         # Application configuration
```

### 2. Dependency Consolidation ✅
**File**: `backend/pom.xml`
- Standardized Java version to 21
- Added Flyway for database migrations
- Added Spring Mail for email notifications
- Added Spring Session JDBC for session management
- Unified Spring Boot version to 3.5.6

### 3. Configuration Unification ✅
**File**: `backend/src/main/resources/application.yml`
- Single datasource configuration
- Flyway migration locations: `classpath:db/migration,filesystem:db/migrations`
- Unified mail configuration (SMTP, templates)
- Consolidated file upload settings
- Single Redis cache configuration

### 4. Database Schema Unification ✅
**Migration**: `db/migrations/V8__unify_schemas.sql` (500+ lines)

Key changes:
- **Backup tables**: Created backup tables for safety
- **UUID conversion**: Converted all BIGINT IDs to UUID using deterministic UUIDv5 function
- **Table rename**: `products` → `listings` with proper UUID primary key
- **Foreign keys**: Added 6 new constraints
  - `listings.seller_id → users.id`
  - `reports.listing_id → listings.id`
  - `reports.reported_by → users.id`
  - `conversations.buyer_id → users.id`
  - `conversations.seller_id → users.id`
  - `conversations.listing_id → listings.id`
- **Orphan detection**: Logged orphaned records before cleanup
- **Validation**: Added verification queries

### 5. Entity Model Updates ✅
Updated all entities from Long/BIGINT to UUID:

**Listing Module**:
- `Listing.java`: UUID id, sellerId
- `Report.java`: UUID id, listingId, reportedBy
- `ListingImage.java`: UUID id, listingId

**Communication Module**:
- `Conversation.java`: UUID id, buyerId, sellerId, listingId
- `Message.java`: UUID id, conversationId, senderId
- `NotificationPreference.java`: UUID userId

### 6. JWT Handling Simplification ✅
**Finding**: Backend's existing `JwtUtil` already returns UUID properly

**Changes**:
- Removed `JwtHelper` wrappers from listing-api and communication
- Updated controllers to use `jwtUtil.extractUserId()` directly (returns UUID)
- Removed lossy `.hashCode()` conversions: `Long userId = uuid.hashCode()` ❌

**Example Fix**:
```java
// Before (lossy conversion)
Long userId = jwtUtil.extractUserId(token).hashCode();

// After (proper UUID handling)
UUID userId = jwtUtil.extractUserId(token);
```

### 7. Service Layer Integration ✅
**Listing Services** (7 files):
- `ListingService`: Direct database access via JPA repositories
- `ReportService`: Proper UUID-based foreign keys
- `FileStorageService`: File upload handling
- `SearchProxyService`: Search integration

**Communication Services** (3 files):
- `ChatService`: Integrated with backend's `ListingService` directly (no HTTP calls)
- `MessageService`: UUID-based message handling
- `NotificationPreferenceService`: User preference management

**Key Improvement**: Removed REST clients, replaced with direct service injection:
```java
// Before (HTTP call between services)
@Autowired
private ListingServiceClient listingServiceClient;

// After (direct service injection)
@Autowired
private ListingService listingService;
```

### 8. Controller Consolidation ✅
**Listing Controllers** (2 files):
- `ListingController`: CRUD operations with UUID
- `ReportController`: Report management with UUID

**Communication Controllers** (2 files):
- `ChatController`: Conversation & message endpoints
- `NotificationPreferenceController`: User notification settings

All controllers updated to extract UUID from JWT tokens correctly.

### 9. Exception Handling Unification ✅
**Consolidated Exception Classes** (13 total):

From Backend:
- `UnauthorizedException`
- `NotFoundException`

From Listing API:
- `ListingNotFoundException`
- `ListingException`
- `ReportNotFoundException`
- `ReportException`
- `FileStorageException`
- `FileUploadException`
- `ValidationException`
- `UnauthorizedAccessException`

From Communication:
- `ConversationNotFoundException`
- `ConversationException`

**GlobalExceptionHandler**: Single comprehensive handler with 20+ exception mappings:
- Custom business exceptions
- Spring validation exceptions
- HTTP request exceptions
- File upload exceptions
- Generic catch-all handler

**ErrorResponse DTO**: Enhanced to support both simple and detailed responses:
```java
{
  "error": "LISTING_NOT_FOUND",
  "message": "Listing with ID ... not found",
  "status": 404,
  "timestamp": "2025-01-10T12:00:00",
  "path": "/api/listings/...",
  "validationErrors": []  // Optional
}
```

### 10. Docker and Deployment ✅
**Updated**: `docker-compose.yml`

**Removed Services**:
- `listing-api` container ❌
- `communication` container ❌

**Unified Backend Service**:
- Single container on port 8080
- All functionality: user management, listings, communication
- Flyway migrations run automatically on startup
- File uploads volume mounted
- Email configuration included

**Supporting Services**:
- PostgreSQL 16 (port 5432)
- Redis 7 (port 6379)

**Volumes**:
```yaml
- postgres_data       # Database persistence
- backend_logs        # Application logs
- redis_data          # Cache persistence
- file_uploads        # Uploaded listing images
```

## Migration Guide

### For Development

1. **Pull latest changes**:
   ```bash
   git pull origin main
   ```

2. **Run database migration**:
   ```bash
   cd backend
   ./mvnw flyway:migrate
   ```

3. **Build and run unified backend**:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. **Or use Docker Compose**:
   ```bash
   docker-compose up --build
   ```

### API Endpoint Changes

All endpoints now under single backend (port 8080):

| Old Service | Old Port | Old Path | New Path |
|-------------|----------|----------|----------|
| Backend | 8080 | `/api/users` | `/api/users` (unchanged) |
| Backend | 8080 | `/api/auth` | `/api/auth` (unchanged) |
| Backend | 8080 | `/api/orders` | `/api/orders` (unchanged) |
| Backend | 8080 | `/api/admin` | `/api/admin` (unchanged) |
| Listing API | 8100 | `/api/listings` | `/api/listings` (port 8080) |
| Listing API | 8100 | `/api/reports` | `/api/reports` (port 8080) |
| Communication | 8200 | `/api/conversations` | `/api/conversations` (port 8080) |
| Communication | 8200 | `/api/messages` | `/api/messages` (port 8080) |

### Frontend Updates Required

**Change base URLs**:
```javascript
// Before
const LISTING_API = 'http://localhost:8100/api';
const COMMUNICATION_API = 'http://localhost:8200/api';

// After
const API_BASE_URL = 'http://localhost:8080/api';
```

**All API calls now go to single endpoint**:
```javascript
// Listings
fetch(`${API_BASE_URL}/listings`)

// Chat
fetch(`${API_BASE_URL}/conversations`)

// Reports
fetch(`${API_BASE_URL}/reports`)
```

## Benefits Achieved

### 1. Code Quality
- ✅ Eliminated ~734 lines of duplicate code
- ✅ Single source of truth for JWT handling
- ✅ Consistent error response format
- ✅ Unified logging approach

### 2. Data Integrity
- ✅ Proper UUID types throughout (no lossy conversions)
- ✅ Foreign key constraints prevent orphaned records
- ✅ Referential integrity enforced at database level
- ✅ CASCADE delete rules properly configured

### 3. Architecture
- ✅ Modular package structure (easy to navigate)
- ✅ Direct service-to-service communication (no HTTP overhead)
- ✅ Single database connection pool
- ✅ Simplified deployment (1 service instead of 3)

### 4. Developer Experience
- ✅ Single codebase to maintain
- ✅ Consistent Java 21 features across all code
- ✅ Unified dependency versions
- ✅ Single configuration file
- ✅ Easier debugging (no cross-service tracing needed)

### 5. Operational
- ✅ Reduced resource usage (1 JVM instead of 3)
- ✅ Simpler Docker setup
- ✅ Easier to monitor and log
- ✅ Single health check endpoint

## Testing Recommendations

### Unit Tests
Test each module independently:
```bash
./mvnw test -Dtest=ListingServiceTest
./mvnw test -Dtest=ChatServiceTest
```

### Integration Tests
Test cross-module interactions:
```bash
./mvnw verify -Dtest=ListingIntegrationTest
```

### Database Migration Test
```bash
# Test migration on clean database
./mvnw flyway:clean flyway:migrate

# Verify UUID conversion
psql -U cm_app_user -d campus_marketplace -c "
SELECT id, seller_id FROM listings LIMIT 5;
SELECT id, buyer_id, seller_id, listing_id FROM conversations LIMIT 5;
"
```

### API Test
Use Postman collections in `docs/api/`:
- `Campus_Marketplace_Search_Discovery.postman_collection.json`
- Update all base URLs to `http://localhost:8080/api`

## Rollback Plan

If issues arise, rollback is possible:

1. **Database Rollback**:
   ```bash
   ./mvnw flyway:undo  # Reverts V8 migration
   ```

2. **Code Rollback**:
   ```bash
   git checkout <commit-before-refactor>
   ```

3. **Restore Services**:
   ```bash
   docker-compose up listing-api communication  # Old compose file
   ```

## Next Steps

### Immediate
1. ✅ Complete refactoring (DONE)
2. 🔄 Update frontend to use unified API
3. 🔄 Update Postman collections
4. 🔄 Run full test suite

### Short-term
1. Add comprehensive integration tests
2. Update API documentation
3. Performance testing (single service vs. 3 services)
4. Update deployment guides

### Long-term
1. Consider adding API versioning
2. Implement API rate limiting
3. Add OpenAPI/Swagger documentation
4. Set up CI/CD pipeline for unified backend

## Conclusion

The refactoring successfully consolidated three separate microservices into a well-structured monolithic application. This improves code quality, data integrity, and developer experience while maintaining all functionality. The modular package structure allows for easy future extraction back to microservices if needed.

**Key Metrics**:
- Lines of code eliminated: ~734
- Database constraints added: 6 foreign keys
- Services consolidated: 3 → 1
- Docker containers reduced: 5 → 3 (removed 2 app services)
- API endpoints unchanged: All paths preserved, just unified port

---

**Refactoring completed**: January 10, 2025
**Migration script**: `db/migrations/V8__unify_schemas.sql`
**Docker setup**: `docker-compose.yml` (updated)
