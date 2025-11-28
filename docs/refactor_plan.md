# Plan: Consolidate Backend Services into Unified System

This Campus Marketplace has 3 separate Spring Boot services (Backend:8080, Listing API:8100, Communication:8200) with significant code duplication (~555 lines), **critical database schema conflicts** (UUID vs Long IDs, duplicate product/listing tables, missing foreign keys), and architectural inconsistencies. The unified backend will eliminate duplications, **resolve all database conflicts through comprehensive schema unification**, standardize authentication, and provide a single cohesive API.

## Key Database Issues Addressed

**Critical Schema Conflicts:**
- ✅ **Duplicate Tables**: `products` (UUID-based) and `listings` (BIGINT-based) serving same purpose → Consolidated into single `listings` table
- ✅ **ID Type Inconsistency**: Backend uses UUID, other services use BIGINT with lossy conversion → All tables migrated to UUID
- ✅ **Missing Foreign Keys**: No referential integrity across services → 6 new FK constraints added
- ✅ **Duplicate Migrations**: V6/V7 exist in two locations → Consolidated to single source
- ✅ **Enum Mismatches**: Different category/condition types → Merged enum definitions

**Database Unification Impact:**
- 3 tables dropped (listings, listing_images, reports)
- 1 table renamed (products → listings)
- 3 tables converted to UUID (conversations, messages, notification_preferences)
- 6 foreign key constraints established
- V8 migration script for comprehensive schema unification

## Steps

1. **Create unified Spring Boot project structure** merging `backend/`, `listing-api/`, and `communication/` into a single application with modular packages (`auth`, `user`, `listing`, `communication`, `order`, `admin`) and shared infrastructure (`security`, `config`, `exception`, `util`)

2. **Consolidate authentication and security** by keeping Backend's comprehensive JWT implementation (including `JwtUtil.java`, `JwtAuthenticationFilter.java`), removing duplicates from listing-api and communication services, and standardizing on UUID-based user identification across all modules

3. **Merge and reconcile database schemas** through comprehensive unification:
   - 3.1. **Consolidate duplicate tables**: Merge Backend's `products` table (UUID-based, 15+ fields) and Listing API's `listings` table (BIGINT-based, 10 fields) into single `listings` table using Backend's richer schema with UUID IDs
   - 3.2. **Unify ID types across all tables**: Convert Communication service tables (`conversations`, `messages`, `notification_preferences`) from BIGINT to UUID for user_id, listing_id, and all primary keys
   - 3.3. **Add missing foreign key constraints**: Establish proper referential integrity - `listings.seller_id → users.user_id`, `conversations.listing_id → listings.listing_id`, `conversations.buyer_id/seller_id → users.user_id`
   - 3.4. **Merge duplicate enum types**: Consolidate product categories (Backend: 7 types vs Listing API: 5 types) and condition enums (Backend: 5 values vs Listing API: 4 values) into unified definitions
   - 3.5. **Consolidate duplicate migration files**: Remove duplicate V6/V7 migrations from `communication/src/main/resources/db/migration/`, keep single source in root `db/migrations/` folder
   - 3.6. **Create V8 unification migration**: Build comprehensive migration script to execute schema consolidation with data preservation and rollback capability

4. **Integrate service modules** by merging listing controllers/services from `listing-api/src/main/java/com/campusmarketplace/listingapi/` and communication controllers/services from `communication/src/main/java/com/campus/marketplace/communication/` into the unified backend under `/api/listings/*` and `/api/chat/*` paths, applying consistent design patterns across all modules

5. **Apply and standardize design patterns** across the unified codebase:
   - 5.1. **Unified Exception Handling**: Consolidate three GlobalExceptionHandler implementations into single shared framework with consistent ErrorResponse format
   - 5.2. **Security Pattern Standardization**: Apply Backend's comprehensive security model (JWT + RBAC) across all endpoints, replacing permissive configurations in listing-api/communication
   - 5.3. **DTO Mapper Pattern**: Introduce MapStruct for automated entity-to-DTO conversions, eliminating manual mapping code
   - 5.4. **Service Layer Consistency**: Standardize transactional patterns, dependency injection (constructor over field), and logging practices
   - 5.5. **API Gateway Pattern**: Consider Spring Cloud Gateway for unified entry point (optional, post-unification)
   - 5.6. **Circuit Breaker Pattern**: Add Resilience4j for fault tolerance in external calls (email notifications, future integrations)

6. **Consolidate configuration and dependencies** by merging all three `pom.xml` files, standardizing Java 21, resolving dependency conflicts, unifying `application.yml` with environment-specific profiles (dev/prod/test), configuring single Flyway migration path pointing to `db/migrations/`, and running on single port 8080

7. **Update build and deployment infrastructure** by modifying `docker-compose.yml` to run single unified backend container, updating `Dockerfile` and `Makefile`, and revising documentation files (`README.md`, `QUICK_START.md`, `API_INTEGRATION_SUMMARY.md`)

## Further Considerations

1. **Frontend integration strategy** - Should we update frontend API calls immediately after backend merge, or create compatibility layer/proxy endpoints to support gradual migration? Recommend parallel approach with deprecation notices.

2. **Database migration execution** - Need decision on schema migration strategy: (A) Blue-green deployment with schema converter, (B) Maintenance window for direct migration, or (C) Gradual migration with dual-write pattern?

3. **Testing strategy** - Should we migrate existing unit tests from all three services, or rewrite integration tests for unified API? Recommend preserving critical test coverage for auth, listings, and chat functionality.

4. **Data migration validation** - Need strategy to verify UUID conversion maintains referential integrity, test rollback procedures if migration fails, and validate that existing production data (if any) remains accessible after ID type changes. Consider running migration on staging environment first.

## Detailed Analysis

### Current Architecture Issues

**Three Independent Services:**
- **Backend (8080)**: Core user management, authentication, product management, orders
- **Listing API (8100)**: Product listing management and reporting
- **Communication (8200)**: Real-time messaging between buyers and sellers

**Critical Duplications:**
- JWT utilities duplicated 3 times (~180 lines): `JwtUtil.java` in backend, listing-api, communication
- JWT helper duplicated 2 times (~45 lines): `JwtHelper.java` in listing-api, communication
- Security configuration duplicated: Nearly identical `SecurityConfig.java` in listing-api and communication
- Exception handling (~200 lines): Similar `GlobalExceptionHandler.java` in multiple services
- **Total**: ~555 lines of duplicate code

**Schema Conflicts:**
1. **ID Type Inconsistency**:
   - Backend: Uses UUID for all entities
   - Listing API: Uses BIGINT (auto-increment)
   - Communication: Uses BIGINT (auto-increment)
   - JWT contains UUID but is converted to Long in microservices (lossy conversion)

2. **Table Overlap**:
   - Backend has `products` table (UUID-based)
   - Listing API has `listings` table (BIGINT-based)
   - These serve the same purpose but use different schemas

3. **User Reference Issues**:
   - Backend stores users with UUID
   - Listing API and Communication reference users with BIGINT (Long)
   - Cannot directly join tables across services

**Architectural Problems:**
- All services share same PostgreSQL database (violates microservice principles)
- No API Gateway (frontend calls services directly)
- No service discovery
- Inconsistent security (Backend has full Spring Security, others minimal)
- No distributed tracing
- Direct service-to-service HTTP calls (Communication → Listing API)

**Database Duplication Issues:**
1. **Duplicate Product/Listing Tables**:
   - Backend: `products` table (UUID PK, 15+ columns, rich schema with JSONB attributes, search_vector, moderation_status)
   - Listing API: `listings` table (BIGINT PK, 10 columns, simpler schema)
   - Both tables exist in same database but cannot reference each other
   - Different column names: Backend uses `product_id`, Listing API uses `listing_id`

2. **Duplicate Migration Files**:
   - `db/migrations/V6__communication_chat_tables.sql` (root level)
   - `communication/src/main/resources/db/migration/V6__communication_chat_tables.sql` (service level)
   - Same for V7 notification preferences migration
   - Risk of version conflicts and inconsistent schema state

3. **Missing Foreign Key Constraints**:
   - `listings.seller_id` (BIGINT) cannot FK to `users.user_id` (UUID)
   - `conversations.listing_id` (BIGINT) cannot FK to `products.product_id` (UUID) or `listings.listing_id` (BIGINT from different table)
   - `conversations.buyer_id/seller_id` (BIGINT) cannot FK to `users.user_id` (UUID)
   - No database-level referential integrity enforcement

4. **Enum Type Mismatches**:
   - **Categories**: Backend has 7 (`TEXTBOOKS`, `ELECTRONICS`, `FURNITURE`, `CLOTHING`, `SPORTS_EQUIPMENT`, `SERVICES`, `OTHER`) vs Listing API has 5 (`TEXTBOOKS`, `GADGETS`, `ELECTRONICS`, `STATIONARY`, `OTHER`)
   - **Conditions**: Backend has 5 (`NEW`, `LIKE_NEW`, `GOOD`, `FAIR`, `POOR`) vs Listing API has 4 (`NEW`, `LIKE_NEW`, `GOOD`, `USED`)
   - **Status**: Backend uses `moderation_status` enum, Listing API uses `status` VARCHAR with different values

### Technology Stack (All Services)

- **Spring Boot**: 3.5.6
- **Java**: 17 (Backend POM), 21 (config/other services)
- **Database**: PostgreSQL (shared `campus_marketplace` DB)
- **JWT**: jjwt 0.12.6
- **ORM**: JPA/Hibernate
- **Validation**: Jakarta Validation

**Backend Specific:**
- Redis + Caffeine caching
- Swagger/OpenAPI documentation
- Hypersistence Utils
- Spring Session JDBC
- Actuator

**Communication Specific:**
- Flyway migrations (V6, V7)
- Spring Mail (SMTP)
- Email notifications

### Design Patterns Currently Applied

**Well-Implemented Patterns:**
1. **Repository Pattern** (Spring Data JPA) - All services use JpaRepository with custom query methods
2. **Service Layer Pattern** - Business logic separated into @Service classes with @Transactional support
3. **DTO Pattern** - 19+ Request/Response DTOs for data transfer across API boundaries
4. **Global Exception Handler Pattern** - @ControllerAdvice with standardized error responses
5. **AOP Pattern** - RateLimitingAspect, RoleAuthorizationAspect for cross-cutting concerns
6. **Caching Pattern** - @Cacheable annotations on search, discovery, recommendations (Backend only)
7. **Builder Pattern** - PagedResponse.builder() for fluent object construction
8. **Enum Pattern** - Type-safe domain values (UserRole, ListingStatus, Category, ItemCondition)
9. **Validation Pattern** - Jakarta Bean Validation with @Valid, @NotNull, @Size annotations
10. **Domain Model Pattern** - Rich entities with business logic (isParticipant(), getOtherParticipant())

**Patterns Inconsistently Applied:**
- **Security Configuration**: Backend has comprehensive RBAC, listing-api/communication are permissive
- **Exception Handling**: Listing-API has 20 handlers, Communication has 5, Backend varies
- **Dependency Injection**: Mix of field injection (@Autowired) and constructor injection
- **Logging**: Inconsistent log levels and structured logging across services

**Recommended Patterns for Unified Architecture:**
1. **Mapper Pattern** - MapStruct for automated entity-DTO conversions
2. **Circuit Breaker Pattern** - Resilience4j for external service calls (email, future APIs)
3. **Event Bus Pattern** - Domain events for cross-module communication (optional)
4. **Strategy Pattern** - Pluggable search algorithms, notification strategies
5. **Factory Pattern** - Unified creation of complex objects (conversations, orders)
6. **Facade Pattern** - Simplified interface for complex subsystems
7. **Template Method Pattern** - Common CRUD operation flows
8. **Observer Pattern** - Event-driven notifications (new message, listing status change)

### Database Schema Overview

**Shared Database**: `campus_marketplace`

**Backend Service Tables:**
- `universities` - Multi-tenant support
- `users` - Core user management (UUID-based)
- `refresh_tokens` - JWT refresh token storage
- `products` - Product listings (UUID-based)
- `orders` - Order management
- `order_items` - Order line items
- `verification_tokens` - Email/password reset tokens (V4)
- `audit_logs` - Comprehensive audit trail (V4)
- `login_attempts` - Failed login tracking (V4)
- `account_actions` - Account status changes (V4)
- `search_history` - Search tracking
- `product_views` - View tracking

**Listing API Tables:**
- `listings` - Product listings (BIGINT IDs)
- `listing_images` - Image metadata
- `reports` - User reports on listings

**Communication Service Tables:**
- `conversations` - Buyer-seller conversations (V6)
- `messages` - Chat messages (V6)
- `notification_preferences` - Email notification settings (V7)

### Authentication Flow

**Current Flow:**
1. **User Login** (Frontend → Backend:8080):
   - POST `/api/auth/login`
   - Returns access token (1h) + refresh token (7d)
   - JWT contains: `userId` (UUID), `username`, `role`, `email`

2. **Token Validation** (Frontend → Listing API:8100):
   - Includes `Authorization: Bearer {token}` header
   - Listing API extracts `userId` from JWT
   - Converts UUID string to Long (hash-based, lossy)
   - Validates ownership for modify operations

3. **Token Validation** (Frontend → Communication:8200):
   - Same as Listing API
   - Validates conversation participation

**Issues:**
- ID Type Mismatch: Backend stores UUID, other services convert to Long
- Shared JWT Secret: All services must use same secret (security risk)
- No Token Refresh in Microservices: Only Backend can refresh tokens

### Service Integration Points

**Current Integration:**
- Frontend → Backend (8080): User auth, registration, profile management
- Frontend → Listing API (8100): Browse/search listings, create listings
- Frontend → Communication (8200): Send messages, view conversations

**Service-to-Service:**
- Communication → Listing API: Verifies listing existence before creating conversations
- Uses REST API (`GET /api/listings/{listingId}`)
- Configured via `listing.api.url` property

**Problems:**
- No API gateway or service mesh
- Direct HTTP calls (potential SPOF)
- No circuit breaker or retry logic
- No distributed tracing

### Port & Endpoint Summary

| Service | Port | Base Path | Key Endpoints |
|---------|------|-----------|--------------|
| **Backend** | 8080 | `/api` | `/auth/*`, `/users/*`, `/admin/*`, `/student/*` |
| **Listing API** | 8100 | `/api` | `/listings/*`, `/files/*`, `/reports/*` |
| **Communication** | 8200 | `/api` | `/chat/*`, `/notifications/*` |

### Refactoring Benefits

**Code Quality:**
- Eliminate ~555 lines of duplicate code
- Single source of truth for authentication
- Consistent error handling across all APIs
- Unified logging and monitoring

**Architecture:**
- Single deployment unit (simpler operations)
- Consistent security model
- Unified database schema
- Single port (8080)
- Simplified frontend integration

**Development:**
- Easier to add new features
- Reduced maintenance burden
- Consistent coding patterns
- Single test suite
- Faster build times

**Performance:**
- No inter-service HTTP calls
- Shared database connection pool
- Unified caching strategy
- Reduced network latency

### Database Unification Strategy

**Goal**: Create single, consistent database schema with UUID-based IDs, proper foreign keys, and no duplicate tables.

**Impact Summary**:
- **Tables Affected**: 6 tables (products, listings, conversations, messages, notification_preferences, listing_images)
- **Tables Dropped**: 3 (listings, listing_images, reports)
- **Tables Renamed**: 1 (products → listings)
- **Tables Converted**: 3 (conversations, messages, notification_preferences: BIGINT → UUID)
- **New Foreign Keys**: 6 constraints
- **Enum Types Modified**: 2 (product_category, product_condition)
- **Code Impact**: All entity classes in listing-api and communication services (change Long → UUID)

**Risk Level**: HIGH - Involves data type conversions and potential data loss if not executed correctly. Requires thorough testing and rollback plan.

#### Step-by-Step Database Consolidation

**1. Table Consolidation Decision Matrix**

| Current Table | Schema Owner | ID Type | Columns | Action |
|---------------|--------------|---------|---------|--------|
| `products` (Backend) | Backend | UUID | 15+ (rich) | **RENAME to `listings`** |
| `listings` (Listing API) | Listing API | BIGINT | 10 (basic) | **DROP** (migrate data to products first) |
| `conversations` | Communication | BIGINT | 6 | **ALTER** (convert to UUID) |
| `messages` | Communication | BIGINT | 5 | **ALTER** (convert to UUID) |
| `notification_preferences` | Communication | BIGINT | 6 | **ALTER** (convert to UUID) |

**Rationale**: Backend's `products` table has more comprehensive schema including:
- Full-text search support (`search_vector`)
- Flexible attributes (JSONB)
- Moderation workflow (`moderation_status`)
- Multi-university support
- Analytics fields (`view_count`, `favorite_count`)

**2. Enum Unification Strategy**

```sql
-- Merged category enum (union of both schemas)
CREATE TYPE listing_category AS ENUM (
    'TEXTBOOKS',      -- Both
    'ELECTRONICS',    -- Both
    'FURNITURE',      -- Backend only
    'CLOTHING',       -- Backend only
    'SPORTS_EQUIPMENT', -- Backend only (maps from Listing API 'GADGETS')
    'STATIONARY',     -- Listing API only (keep for compatibility)
    'SERVICES',       -- Backend only
    'OTHER'           -- Both
);

-- Merged condition enum
CREATE TYPE listing_condition AS ENUM (
    'NEW',       -- Both
    'LIKE_NEW',  -- Both
    'GOOD',      -- Both
    'FAIR',      -- Backend only
    'POOR',      -- Backend only (maps from Listing API 'USED')
    'USED'       -- Listing API only (alias for POOR)
);

-- Status unification (use Backend's moderation_status)
-- Listing API status → Backend moderation_status mapping:
-- 'ACTIVE' → 'APPROVED'
-- 'PENDING' → 'PENDING'
-- 'SOLD' → custom flag (is_active = false)
-- 'CANCELLED' → 'REJECTED'
```

**3. ID Migration Strategy**

For existing BIGINT records, generate deterministic UUIDs:

```sql
-- Function to create deterministic UUID from BIGINT
CREATE OR REPLACE FUNCTION bigint_to_uuid(id BIGINT) RETURNS UUID AS $$
BEGIN
    RETURN uuid_generate_v5(
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::uuid,  -- Namespace UUID
        id::text
    );
END;
$$ LANGUAGE plpgsql;
```

**4. Migration Order (Critical Dependencies)**

```
Step 1: Backup existing data
Step 2: Migrate listings data → products table
Step 3: Drop listings table and related indexes
Step 4: Rename products → listings
Step 5: Convert conversations table to UUID
Step 6: Convert messages table to UUID
Step 7: Convert notification_preferences to UUID
Step 8: Add foreign key constraints
Step 9: Update enum values for merged types
Step 10: Verify data integrity
```

**5. Foreign Key Relationships (Post-Migration)**

```
listings.seller_id → users.user_id (UUID → UUID)
conversations.listing_id → listings.listing_id (UUID → UUID)
conversations.buyer_id → users.user_id (UUID → UUID)
conversations.seller_id → users.user_id (UUID → UUID)
messages.conversation_id → conversations.conversation_id (UUID → UUID)
notification_preferences.user_id → users.user_id (UUID → UUID)
```

**6. Data Preservation Requirements**

- Preserve all existing listings (migrate from old `listings` to `products`)
- Map BIGINT IDs to UUIDs using deterministic function
- Maintain conversation history (all messages must remain linked)
- Keep user notification preferences
- Update any references in application code from Long → UUID

**7. V8 Migration Script (Complete Implementation)**

The complete V8__unify_schemas.sql migration script:

```sql
-- =============================================================================
-- V8__unify_schemas.sql
-- Database Schema Unification Migration
-- =============================================================================
-- Purpose: Consolidate duplicate tables, convert BIGINT to UUID, add foreign keys
-- Risk Level: HIGH - Creates backups and includes rollback capability
-- Estimated Time: 5-10 minutes on production data
-- =============================================================================

-- =============================================================================
-- PART 1: SAFETY CHECKS AND BACKUPS
-- =============================================================================

DO $$
DECLARE
    products_count INTEGER;
    listings_count INTEGER;
    conversations_count INTEGER;
    messages_count INTEGER;
BEGIN
    -- Record pre-migration counts for validation
    SELECT COUNT(*) INTO products_count FROM products;
    SELECT COUNT(*) INTO listings_count FROM listings;
    SELECT COUNT(*) INTO conversations_count FROM conversations;
    SELECT COUNT(*) INTO messages_count FROM messages;

    RAISE NOTICE 'Pre-migration counts:';
    RAISE NOTICE '  products: %', products_count;
    RAISE NOTICE '  listings: %', listings_count;
    RAISE NOTICE '  conversations: %', conversations_count;
    RAISE NOTICE '  messages: %', messages_count;

    -- Create backup tables (with timestamp)
    EXECUTE format('CREATE TABLE products_backup_%s AS SELECT * FROM products',
                   to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));
    EXECUTE format('CREATE TABLE listings_backup_%s AS SELECT * FROM listings',
                   to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));
    EXECUTE format('CREATE TABLE conversations_backup_%s AS SELECT * FROM conversations',
                   to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));
    EXECUTE format('CREATE TABLE messages_backup_%s AS SELECT * FROM messages',
                   to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));
    EXECUTE format('CREATE TABLE notification_preferences_backup_%s AS SELECT * FROM notification_preferences',
                   to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));

    RAISE NOTICE 'Backup tables created successfully';
END $$;

-- =============================================================================
-- PART 2: CREATE HELPER FUNCTIONS
-- =============================================================================

-- Function to create deterministic UUID from BIGINT
CREATE OR REPLACE FUNCTION bigint_to_uuid(id BIGINT) RETURNS UUID AS $$
BEGIN
    RETURN uuid_generate_v5(
        'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::uuid,  -- Fixed namespace UUID
        id::text
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION bigint_to_uuid(BIGINT) IS 'Converts BIGINT to deterministic UUID using UUIDv5';

-- =============================================================================
-- PART 3: ENUM TYPE UPDATES
-- =============================================================================

-- Add STATIONARY to product_category enum if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_enum e
        JOIN pg_type t ON e.enumtypid = t.oid
        WHERE t.typname = 'product_category' AND e.enumlabel = 'STATIONARY'
    ) THEN
        ALTER TYPE product_category ADD VALUE 'STATIONARY';
        RAISE NOTICE 'Added STATIONARY to product_category enum';
    END IF;
END $$;

-- =============================================================================
-- PART 4: LISTING TABLE CONSOLIDATION
-- =============================================================================

-- Step 4.1: Migrate data from listings table to products table
INSERT INTO products (
    product_id,
    seller_id,
    university_id,
    title,
    description,
    category,
    condition,
    price,
    original_price,
    negotiable,
    quantity,
    sold_quantity,
    is_active,
    moderation_status,
    view_count,
    pickup_location,
    created_at,
    updated_at
)
SELECT
    bigint_to_uuid(listing_id) AS product_id,
    bigint_to_uuid(seller_id) AS seller_id,  -- Assumes seller exists in users table
    (SELECT university_id FROM universities LIMIT 1) AS university_id,  -- Default university
    title,
    description,
    -- Category mapping
    CASE
        WHEN category = 'GADGETS' THEN 'SPORTS_EQUIPMENT'::product_category
        WHEN category = 'STATIONARY' THEN 'STATIONARY'::product_category
        WHEN category = 'TEXTBOOKS' THEN 'TEXTBOOKS'::product_category
        WHEN category = 'ELECTRONICS' THEN 'ELECTRONICS'::product_category
        ELSE 'OTHER'::product_category
    END AS category,
    -- Condition mapping
    CASE
        WHEN condition = 'USED' THEN 'POOR'::product_condition
        WHEN condition = 'NEW' THEN 'NEW'::product_condition
        WHEN condition = 'LIKE_NEW' THEN 'LIKE_NEW'::product_condition
        WHEN condition = 'GOOD' THEN 'GOOD'::product_condition
        ELSE 'FAIR'::product_condition
    END AS condition,
    price,
    NULL AS original_price,
    FALSE AS negotiable,
    1 AS quantity,
    CASE WHEN status = 'SOLD' THEN 1 ELSE 0 END AS sold_quantity,
    -- Status mapping to is_active
    CASE WHEN status IN ('ACTIVE', 'PENDING') THEN TRUE ELSE FALSE END AS is_active,
    -- Status mapping to moderation_status
    CASE
        WHEN status = 'ACTIVE' THEN 'APPROVED'::moderation_status
        WHEN status = 'PENDING' THEN 'PENDING'::moderation_status
        WHEN status = 'CANCELLED' THEN 'REJECTED'::moderation_status
        ELSE 'PENDING'::moderation_status
    END AS moderation_status,
    COALESCE(view_count, 0) AS view_count,
    location AS pickup_location,
    created_at,
    updated_at
FROM listings
WHERE NOT EXISTS (
    SELECT 1 FROM products p WHERE p.product_id = bigint_to_uuid(listings.listing_id)
);

-- Log migration count
DO $$
DECLARE
    migrated_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO migrated_count
    FROM products p
    WHERE EXISTS (
        SELECT 1 FROM listings l WHERE bigint_to_uuid(l.listing_id) = p.product_id
    );
    RAISE NOTICE 'Migrated % listings to products table', migrated_count;
END $$;

-- Step 4.2: Drop listing_images and reports tables (dependent tables)
DROP TABLE IF EXISTS listing_images CASCADE;
DROP TABLE IF EXISTS reports CASCADE;
RAISE NOTICE 'Dropped listing_images and reports tables';

-- Step 4.3: Drop listings table
DROP TABLE IF EXISTS listings CASCADE;
RAISE NOTICE 'Dropped listings table';

-- Step 4.4: Rename products table to listings
ALTER TABLE products RENAME TO listings;
ALTER TABLE listings RENAME COLUMN product_id TO listing_id;
RAISE NOTICE 'Renamed products table to listings';

-- Step 4.5: Rename related objects
ALTER INDEX idx_products_seller RENAME TO idx_listings_seller;
ALTER INDEX idx_products_university RENAME TO idx_listings_university;
ALTER INDEX idx_products_category RENAME TO idx_listings_category;
ALTER INDEX idx_products_status RENAME TO idx_listings_status;
ALTER INDEX idx_products_price RENAME TO idx_listings_price;
ALTER INDEX idx_products_search RENAME TO idx_listings_search;
ALTER INDEX idx_products_seller_active RENAME TO idx_listings_seller_active;
ALTER INDEX idx_products_search_vector RENAME TO idx_listings_search_vector;
ALTER INDEX idx_products_title_trgm RENAME TO idx_listings_title_trgm;

-- Rename triggers
ALTER TRIGGER trigger_products_updated_at ON listings RENAME TO trigger_listings_updated_at;
ALTER TRIGGER trigger_products_search_vector ON listings RENAME TO trigger_listings_search_vector;

-- Update views
DROP VIEW IF EXISTS vw_active_products CASCADE;
CREATE VIEW vw_active_listings AS
SELECT
    l.listing_id,
    l.seller_id,
    u.username AS seller_username,
    u.avatar_url AS seller_avatar,
    l.title,
    l.description,
    l.category,
    l.condition,
    l.price,
    l.original_price,
    l.quantity,
    l.view_count,
    l.favorite_count,
    l.created_at,
    l.university_id
FROM listings l
JOIN users u ON l.seller_id = u.user_id
WHERE l.is_active = true
  AND l.moderation_status = 'APPROVED'
  AND l.quantity > 0;

RAISE NOTICE 'Renamed indexes, triggers, and views for listings table';

-- =============================================================================
-- PART 5: COMMUNICATION TABLES UUID CONVERSION
-- =============================================================================

-- Step 5.1: Create temporary mapping tables
CREATE TEMP TABLE conversation_id_mapping (
    old_id BIGINT PRIMARY KEY,
    new_id UUID NOT NULL
);

CREATE TEMP TABLE message_id_mapping (
    old_id BIGINT PRIMARY KEY,
    new_id UUID NOT NULL
);

CREATE TEMP TABLE notification_pref_id_mapping (
    old_id BIGINT PRIMARY KEY,
    new_id UUID NOT NULL
);

-- Step 5.2: Populate mapping tables
INSERT INTO conversation_id_mapping (old_id, new_id)
SELECT conversation_id, bigint_to_uuid(conversation_id)
FROM conversations;

INSERT INTO message_id_mapping (old_id, new_id)
SELECT message_id, bigint_to_uuid(message_id)
FROM messages;

INSERT INTO notification_pref_id_mapping (old_id, new_id)
SELECT preference_id, bigint_to_uuid(preference_id)
FROM notification_preferences;

RAISE NOTICE 'Created ID mapping tables';

-- Step 5.3: Convert conversations table
-- Add new UUID columns
ALTER TABLE conversations
    ADD COLUMN conversation_id_uuid UUID,
    ADD COLUMN listing_id_uuid UUID,
    ADD COLUMN buyer_id_uuid UUID,
    ADD COLUMN seller_id_uuid UUID;

-- Populate UUID columns
UPDATE conversations SET
    conversation_id_uuid = bigint_to_uuid(conversation_id),
    listing_id_uuid = bigint_to_uuid(listing_id),
    buyer_id_uuid = bigint_to_uuid(buyer_id),
    seller_id_uuid = bigint_to_uuid(seller_id);

-- Drop old columns and constraints
ALTER TABLE conversations
    DROP CONSTRAINT IF EXISTS unique_conversation,
    DROP COLUMN conversation_id,
    DROP COLUMN listing_id,
    DROP COLUMN buyer_id,
    DROP COLUMN seller_id;

-- Rename new columns
ALTER TABLE conversations
    RENAME COLUMN conversation_id_uuid TO conversation_id;
ALTER TABLE conversations
    RENAME COLUMN listing_id_uuid TO listing_id;
ALTER TABLE conversations
    RENAME COLUMN buyer_id_uuid TO buyer_id;
ALTER TABLE conversations
    RENAME COLUMN seller_id_uuid TO seller_id;

-- Add primary key and unique constraint
ALTER TABLE conversations ADD PRIMARY KEY (conversation_id);
ALTER TABLE conversations ADD CONSTRAINT unique_conversation UNIQUE (listing_id, buyer_id, seller_id);

RAISE NOTICE 'Converted conversations table to UUID';

-- Step 5.4: Convert messages table
-- Add new UUID columns
ALTER TABLE messages
    ADD COLUMN message_id_uuid UUID,
    ADD COLUMN conversation_id_uuid UUID,
    ADD COLUMN sender_id_uuid UUID;

-- Populate UUID columns using mapping table
UPDATE messages m SET
    message_id_uuid = bigint_to_uuid(m.message_id),
    conversation_id_uuid = (SELECT new_id FROM conversation_id_mapping WHERE old_id = m.conversation_id),
    sender_id_uuid = bigint_to_uuid(m.sender_id);

-- Drop old foreign key constraint
ALTER TABLE messages DROP CONSTRAINT IF EXISTS messages_conversation_id_fkey;

-- Drop old columns
ALTER TABLE messages
    DROP COLUMN message_id,
    DROP COLUMN conversation_id,
    DROP COLUMN sender_id;

-- Rename new columns
ALTER TABLE messages
    RENAME COLUMN message_id_uuid TO message_id;
ALTER TABLE messages
    RENAME COLUMN conversation_id_uuid TO conversation_id;
ALTER TABLE messages
    RENAME COLUMN sender_id_uuid TO sender_id;

-- Add primary key
ALTER TABLE messages ADD PRIMARY KEY (message_id);

RAISE NOTICE 'Converted messages table to UUID';

-- Step 5.5: Convert notification_preferences table
-- Add new UUID columns
ALTER TABLE notification_preferences
    ADD COLUMN preference_id_uuid UUID,
    ADD COLUMN user_id_uuid UUID;

-- Populate UUID columns
UPDATE notification_preferences SET
    preference_id_uuid = bigint_to_uuid(preference_id),
    user_id_uuid = bigint_to_uuid(user_id);

-- Drop old columns and constraints
ALTER TABLE notification_preferences
    DROP CONSTRAINT IF EXISTS notification_preferences_pkey,
    DROP CONSTRAINT IF EXISTS notification_preferences_user_id_key,
    DROP COLUMN preference_id,
    DROP COLUMN user_id;

-- Rename new columns
ALTER TABLE notification_preferences
    RENAME COLUMN preference_id_uuid TO preference_id;
ALTER TABLE notification_preferences
    RENAME COLUMN user_id_uuid TO user_id;

-- Add constraints
ALTER TABLE notification_preferences ADD PRIMARY KEY (preference_id);
ALTER TABLE notification_preferences ADD CONSTRAINT notification_preferences_user_id_key UNIQUE (user_id);

RAISE NOTICE 'Converted notification_preferences table to UUID';

-- Step 5.6: Recreate indexes on UUID columns
DROP INDEX IF EXISTS idx_conversations_listing;
DROP INDEX IF EXISTS idx_conversations_buyer;
DROP INDEX IF EXISTS idx_conversations_seller;
DROP INDEX IF EXISTS idx_messages_conversation;
DROP INDEX IF EXISTS idx_messages_sender;
DROP INDEX IF EXISTS idx_notification_preferences_user;

CREATE INDEX idx_conversations_listing ON conversations(listing_id);
CREATE INDEX idx_conversations_buyer ON conversations(buyer_id);
CREATE INDEX idx_conversations_seller ON conversations(seller_id);
CREATE INDEX idx_conversations_updated ON conversations(updated_at DESC);

CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_created ON messages(created_at DESC);
CREATE INDEX idx_messages_unread ON messages(conversation_id, is_read) WHERE is_read = false;

CREATE INDEX idx_notification_preferences_user ON notification_preferences(user_id);

RAISE NOTICE 'Recreated indexes on UUID columns';

-- =============================================================================
-- PART 6: ADD FOREIGN KEY CONSTRAINTS
-- =============================================================================

-- Add FK: listings.seller_id → users.user_id
ALTER TABLE listings
    ADD CONSTRAINT fk_listings_seller
    FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Add FK: conversations.listing_id → listings.listing_id
ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_listing
    FOREIGN KEY (listing_id) REFERENCES listings(listing_id) ON DELETE CASCADE;

-- Add FK: conversations.buyer_id → users.user_id
ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_buyer
    FOREIGN KEY (buyer_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Add FK: conversations.seller_id → users.user_id
ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_seller
    FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Add FK: messages.conversation_id → conversations.conversation_id
ALTER TABLE messages
    ADD CONSTRAINT fk_messages_conversation
    FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE;

-- Add FK: notification_preferences.user_id → users.user_id
ALTER TABLE notification_preferences
    ADD CONSTRAINT fk_notification_preferences_user
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

RAISE NOTICE 'Added 6 foreign key constraints';

-- =============================================================================
-- PART 7: DATA VALIDATION
-- =============================================================================

DO $$
DECLARE
    listings_count INTEGER;
    conversations_count INTEGER;
    messages_count INTEGER;
    notification_prefs_count INTEGER;
    orphaned_conversations INTEGER;
    orphaned_messages INTEGER;
BEGIN
    -- Count records after migration
    SELECT COUNT(*) INTO listings_count FROM listings;
    SELECT COUNT(*) INTO conversations_count FROM conversations;
    SELECT COUNT(*) INTO messages_count FROM messages;
    SELECT COUNT(*) INTO notification_prefs_count FROM notification_preferences;

    -- Check for orphaned records
    SELECT COUNT(*) INTO orphaned_conversations
    FROM conversations c
    WHERE NOT EXISTS (SELECT 1 FROM listings l WHERE l.listing_id = c.listing_id)
       OR NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = c.buyer_id)
       OR NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = c.seller_id);

    SELECT COUNT(*) INTO orphaned_messages
    FROM messages m
    WHERE NOT EXISTS (SELECT 1 FROM conversations c WHERE c.conversation_id = m.conversation_id);

    RAISE NOTICE 'Post-migration counts:';
    RAISE NOTICE '  listings: %', listings_count;
    RAISE NOTICE '  conversations: %', conversations_count;
    RAISE NOTICE '  messages: %', messages_count;
    RAISE NOTICE '  notification_preferences: %', notification_prefs_count;
    RAISE NOTICE 'Validation:';
    RAISE NOTICE '  Orphaned conversations: %', orphaned_conversations;
    RAISE NOTICE '  Orphaned messages: %', orphaned_messages;

    IF orphaned_conversations > 0 OR orphaned_messages > 0 THEN
        RAISE WARNING 'Found orphaned records - review foreign key constraints';
    ELSE
        RAISE NOTICE 'All foreign key relationships validated successfully';
    END IF;
END $$;

-- =============================================================================
-- PART 8: UPDATE SCHEMA VERSION
-- =============================================================================

INSERT INTO schema_version (version, description, applied_at)
VALUES ('8.0.0', 'Unified database schema - consolidated tables, UUID migration, foreign keys', CURRENT_TIMESTAMP);

-- =============================================================================
-- MIGRATION COMPLETE
-- =============================================================================

RAISE NOTICE '========================================';
RAISE NOTICE 'V8 Schema Unification Migration Complete';
RAISE NOTICE '========================================';
RAISE NOTICE 'Summary:';
RAISE NOTICE '  - Consolidated products + listings → listings (UUID)';
RAISE NOTICE '  - Converted conversations, messages, notification_preferences to UUID';
RAISE NOTICE '  - Added 6 foreign key constraints';
RAISE NOTICE '  - Created backup tables with timestamp';
RAISE NOTICE 'Next Steps:';
RAISE NOTICE '  1. Verify application code uses UUID types';
RAISE NOTICE '  2. Update entity classes (Long → UUID)';
RAISE NOTICE '  3. Test all API endpoints';
RAISE NOTICE '  4. Drop backup tables after validation (keep for 7 days)';
```

### Migration Strategy Recommendations

**Phase 1: Preparation**
- Create unified project structure
- Merge dependencies (resolve conflicts)
- Create unified configuration
- Set up database migration scripts

**Phase 2: Code Consolidation**
- Keep Backend's authentication (most complete)
- Merge Listing API controllers/services
- Merge Communication controllers/services
- Resolve entity/DTO conflicts (UUID vs Long)

**Phase 3: Database Migration**
- Create V8__unify_schemas.sql with comprehensive data migration
- Backup all affected tables (listings, conversations, messages, notification_preferences)
- Migrate data from Listing API's `listings` table to Backend's `products` table
  - Map categories: GADGETS → SPORTS_EQUIPMENT, STATIONARY → STATIONARY (add to enum)
  - Map conditions: USED → POOR
  - Map status: ACTIVE → APPROVED, SOLD → set is_active=false
- Drop duplicate `listings` table and its indexes/constraints
- Rename `products` table to `listings` (align with domain language)
- Convert all Communication tables to UUID:
  - Generate UUIDs for existing records using deterministic function
  - Create temporary mapping tables for old_id → new_uuid
  - Update all foreign key references
  - Replace BIGINT columns with UUID columns
- Add proper foreign key constraints with referential integrity
- Create validation queries to verify:
  - All conversations reference valid listings and users
  - All messages reference valid conversations
  - No orphaned records exist
- Test rollback script in case of failure

**Phase 4: Testing**
- Migrate unit tests
- Create integration tests for merged APIs
- Test authentication flow
- Test all API endpoints

**Phase 5: Deployment**
- Update Docker configuration
- Update documentation
- Deploy unified backend
- Update frontend API URLs

**Phase 6: Cleanup**
- Archive old service directories (listing-api/, communication/)
- Remove duplicate code and migration files
- Remove duplicate V6/V7 migration files from communication/src/main/resources/db/migration/
- Update CI/CD pipelines to build single artifact
- Final documentation review

### Implementation Checklist

**Database Migration Files to Create:**
- [ ] `db/migrations/V8__unify_schemas.sql` - Main unification migration
- [ ] `db/migrations/V8_rollback.sql` - Emergency rollback script (manual)
- [ ] Update V6 and V7 migrations to remove duplicate files

**Entity Classes to Modify (UUID Migration):**
- [ ] `listing-api/*/Listing.java` - Change listing_id from Long → UUID
- [ ] `listing-api/*/ListingImage.java` - Change IDs from Long → UUID
- [ ] `listing-api/*/Report.java` - Change IDs from Long → UUID
- [ ] `communication/*/Conversation.java` - Change all IDs from Long → UUID
- [ ] `communication/*/Message.java` - Change all IDs from Long → UUID
- [ ] `communication/*/NotificationPreference.java` - Change user_id from Long → UUID

**Service Classes to Modify:**
- [ ] All listing-api services converting JWT userId to Long → Remove conversion, use UUID directly
- [ ] All communication services converting JWT userId to Long → Remove conversion, use UUID directly
- [ ] Update RestTemplate calls in Communication service (listing verification)

**Configuration Files:**
- [ ] Merge `backend/pom.xml` + `listing-api/pom.xml` + `communication/pom.xml`
- [ ] Merge `backend/application.yml` + `listing-api/application.yml` + `communication/application.yml`
- [ ] Configure Flyway: `spring.flyway.locations=classpath:db/migrations`
- [ ] Update `docker-compose.yml` - single backend service

**Testing Requirements:**
- [ ] Create integration test for V8 migration (test with sample data)
- [ ] Test UUID conversion with existing conversations/messages
- [ ] Verify foreign key constraints work correctly
- [ ] Test rollback script
- [ ] End-to-end API tests for merged endpoints
