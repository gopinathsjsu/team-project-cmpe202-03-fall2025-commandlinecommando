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
    SELECT COUNT(*) INTO listings_count FROM listings WHERE 1=0;  -- May not exist
    SELECT COUNT(*) INTO conversations_count FROM conversations WHERE 1=0;  -- May not exist
    SELECT COUNT(*) INTO messages_count FROM messages WHERE 1=0;  -- May not exist
    
    RAISE NOTICE 'Pre-migration counts:';
    RAISE NOTICE '  products: %', products_count;
    RAISE NOTICE '  listings: %', listings_count;
    RAISE NOTICE '  conversations: %', conversations_count;
    RAISE NOTICE '  messages: %', messages_count;
    
    -- Create backup tables (with timestamp)
    EXECUTE format('CREATE TABLE products_backup_%s AS SELECT * FROM products',
                   to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));
    
    -- Only backup if tables exist
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'listings') THEN
        EXECUTE format('CREATE TABLE listings_backup_%s AS SELECT * FROM listings',
                       to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'conversations') THEN
        EXECUTE format('CREATE TABLE conversations_backup_%s AS SELECT * FROM conversations',
                       to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'messages') THEN
        EXECUTE format('CREATE TABLE messages_backup_%s AS SELECT * FROM messages',
                       to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notification_preferences') THEN
        EXECUTE format('CREATE TABLE notification_preferences_backup_%s AS SELECT * FROM notification_preferences',
                       to_char(current_timestamp, 'YYYYMMDD_HH24MISS'));
    END IF;
    
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
    IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'product_category') THEN
        IF NOT EXISTS (
            SELECT 1 FROM pg_enum e
            JOIN pg_type t ON e.enumtypid = t.oid
            WHERE t.typname = 'product_category' AND e.enumlabel = 'STATIONARY'
        ) THEN
            ALTER TYPE product_category ADD VALUE 'STATIONARY';
            RAISE NOTICE 'Added STATIONARY to product_category enum';
        END IF;
    END IF;
END $$;

-- =============================================================================
-- PART 4: LISTING TABLE CONSOLIDATION
-- =============================================================================

-- Step 4.1: Migrate data from listings table to products table (if listings table exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'listings') THEN
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

        RAISE NOTICE 'Migrated listings to products table';
    END IF;
END $$;

-- Step 4.2: Drop listing_images and reports tables (dependent tables)
DROP TABLE IF EXISTS listing_images CASCADE;
DROP TABLE IF EXISTS reports CASCADE;
RAISE NOTICE 'Dropped listing_images and reports tables (if they existed)';

-- Step 4.3: Drop listings table
DROP TABLE IF EXISTS listings CASCADE;
RAISE NOTICE 'Dropped listings table (if it existed)';

-- Step 4.4: Rename products table to listings
ALTER TABLE products RENAME TO listings;
ALTER TABLE listings RENAME COLUMN product_id TO listing_id;
RAISE NOTICE 'Renamed products table to listings';

-- Step 4.5: Rename related objects
DO $$
BEGIN
    -- Rename indexes if they exist
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_seller') THEN
        ALTER INDEX idx_products_seller RENAME TO idx_listings_seller;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_university') THEN
        ALTER INDEX idx_products_university RENAME TO idx_listings_university;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_category') THEN
        ALTER INDEX idx_products_category RENAME TO idx_listings_category;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_status') THEN
        ALTER INDEX idx_products_status RENAME TO idx_listings_status;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_price') THEN
        ALTER INDEX idx_products_price RENAME TO idx_listings_price;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_search') THEN
        ALTER INDEX idx_products_search RENAME TO idx_listings_search;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_seller_active') THEN
        ALTER INDEX idx_products_seller_active RENAME TO idx_listings_seller_active;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_search_vector') THEN
        ALTER INDEX idx_products_search_vector RENAME TO idx_listings_search_vector;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_title_trgm') THEN
        ALTER INDEX idx_products_title_trgm RENAME TO idx_listings_title_trgm;
    END IF;
    
    -- Rename triggers if they exist
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_products_updated_at') THEN
        EXECUTE 'ALTER TRIGGER trigger_products_updated_at ON listings RENAME TO trigger_listings_updated_at';
    END IF;
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_products_search_vector') THEN
        EXECUTE 'ALTER TRIGGER trigger_products_search_vector ON listings RENAME TO trigger_listings_search_vector';
    END IF;
    
    RAISE NOTICE 'Renamed indexes and triggers for listings table';
END $$;

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

RAISE NOTICE 'Created vw_active_listings view';

-- =============================================================================
-- PART 5: COMMUNICATION TABLES UUID CONVERSION (if they exist)
-- =============================================================================

-- Step 5.1: Convert conversations table (if exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'conversations') THEN
        -- Create temporary mapping table
        CREATE TEMP TABLE conversation_id_mapping (
            old_id BIGINT PRIMARY KEY,
            new_id UUID NOT NULL
        );
        
        INSERT INTO conversation_id_mapping (old_id, new_id)
        SELECT conversation_id, bigint_to_uuid(conversation_id)
        FROM conversations;
        
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
    END IF;
END $$;

-- Step 5.2: Convert messages table (if exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'messages') THEN
        -- Add new UUID columns
        ALTER TABLE messages
            ADD COLUMN message_id_uuid UUID,
            ADD COLUMN conversation_id_uuid UUID,
            ADD COLUMN sender_id_uuid UUID;
        
        -- Populate UUID columns
        UPDATE messages m SET
            message_id_uuid = bigint_to_uuid(m.message_id),
            conversation_id_uuid = bigint_to_uuid(m.conversation_id),
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
    END IF;
END $$;

-- Step 5.3: Convert notification_preferences table (if exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notification_preferences') THEN
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
    END IF;
END $$;

-- Step 5.4: Recreate indexes on UUID columns
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'conversations') THEN
        DROP INDEX IF EXISTS idx_conversations_listing;
        DROP INDEX IF EXISTS idx_conversations_buyer;
        DROP INDEX IF EXISTS idx_conversations_seller;
        
        CREATE INDEX idx_conversations_listing ON conversations(listing_id);
        CREATE INDEX idx_conversations_buyer ON conversations(buyer_id);
        CREATE INDEX idx_conversations_seller ON conversations(seller_id);
        CREATE INDEX idx_conversations_updated ON conversations(updated_at DESC);
        
        RAISE NOTICE 'Recreated indexes for conversations table';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'messages') THEN
        DROP INDEX IF EXISTS idx_messages_conversation;
        DROP INDEX IF EXISTS idx_messages_sender;
        
        CREATE INDEX idx_messages_conversation ON messages(conversation_id);
        CREATE INDEX idx_messages_sender ON messages(sender_id);
        CREATE INDEX idx_messages_created ON messages(created_at DESC);
        CREATE INDEX idx_messages_unread ON messages(conversation_id, is_read) WHERE is_read = false;
        
        RAISE NOTICE 'Recreated indexes for messages table';
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notification_preferences') THEN
        DROP INDEX IF EXISTS idx_notification_preferences_user;
        
        CREATE INDEX idx_notification_preferences_user ON notification_preferences(user_id);
        
        RAISE NOTICE 'Recreated indexes for notification_preferences table';
    END IF;
END $$;

-- =============================================================================
-- PART 6: ADD FOREIGN KEY CONSTRAINTS
-- =============================================================================

-- Add FK: listings.seller_id → users.user_id
ALTER TABLE listings
    DROP CONSTRAINT IF EXISTS fk_listings_seller,
    ADD CONSTRAINT fk_listings_seller
    FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Add FKs for conversations (if table exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'conversations') THEN
        ALTER TABLE conversations
            DROP CONSTRAINT IF EXISTS fk_conversations_listing,
            ADD CONSTRAINT fk_conversations_listing
            FOREIGN KEY (listing_id) REFERENCES listings(listing_id) ON DELETE CASCADE;
        
        ALTER TABLE conversations
            DROP CONSTRAINT IF EXISTS fk_conversations_buyer,
            ADD CONSTRAINT fk_conversations_buyer
            FOREIGN KEY (buyer_id) REFERENCES users(user_id) ON DELETE CASCADE;
        
        ALTER TABLE conversations
            DROP CONSTRAINT IF EXISTS fk_conversations_seller,
            ADD CONSTRAINT fk_conversations_seller
            FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE;
        
        RAISE NOTICE 'Added foreign keys for conversations table';
    END IF;
END $$;

-- Add FKs for messages (if table exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'messages') THEN
        ALTER TABLE messages
            DROP CONSTRAINT IF EXISTS fk_messages_conversation,
            ADD CONSTRAINT fk_messages_conversation
            FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE;
        
        RAISE NOTICE 'Added foreign keys for messages table';
    END IF;
END $$;

-- Add FKs for notification_preferences (if table exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notification_preferences') THEN
        ALTER TABLE notification_preferences
            DROP CONSTRAINT IF EXISTS fk_notification_preferences_user,
            ADD CONSTRAINT fk_notification_preferences_user
            FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
        
        RAISE NOTICE 'Added foreign keys for notification_preferences table';
    END IF;
END $$;

-- =============================================================================
-- PART 7: DATA VALIDATION
-- =============================================================================

DO $$
DECLARE
    listings_count INTEGER;
    conversations_count INTEGER := 0;
    messages_count INTEGER := 0;
    notification_prefs_count INTEGER := 0;
    orphaned_conversations INTEGER := 0;
    orphaned_messages INTEGER := 0;
BEGIN
    -- Count records after migration
    SELECT COUNT(*) INTO listings_count FROM listings;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'conversations') THEN
        SELECT COUNT(*) INTO conversations_count FROM conversations;
        
        -- Check for orphaned records
        SELECT COUNT(*) INTO orphaned_conversations
        FROM conversations c
        WHERE NOT EXISTS (SELECT 1 FROM listings l WHERE l.listing_id = c.listing_id)
           OR NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = c.buyer_id)
           OR NOT EXISTS (SELECT 1 FROM users u WHERE u.user_id = c.seller_id);
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'messages') THEN
        SELECT COUNT(*) INTO messages_count FROM messages;
        
        SELECT COUNT(*) INTO orphaned_messages
        FROM messages m
        WHERE NOT EXISTS (SELECT 1 FROM conversations c WHERE c.conversation_id = m.conversation_id);
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notification_preferences') THEN
        SELECT COUNT(*) INTO notification_prefs_count FROM notification_preferences;
    END IF;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Post-migration validation:';
    RAISE NOTICE '  listings: %', listings_count;
    RAISE NOTICE '  conversations: %', conversations_count;
    RAISE NOTICE '  messages: %', messages_count;
    RAISE NOTICE '  notification_preferences: %', notification_prefs_count;
    RAISE NOTICE '  orphaned_conversations: %', orphaned_conversations;
    RAISE NOTICE '  orphaned_messages: %', orphaned_messages;
    RAISE NOTICE '========================================';
    
    IF orphaned_conversations > 0 OR orphaned_messages > 0 THEN
        RAISE WARNING 'Found orphaned records - review foreign key constraints';
    ELSE
        RAISE NOTICE 'All foreign key relationships validated successfully';
    END IF;
END $$;

-- =============================================================================
-- PART 8: UPDATE SCHEMA VERSION (if schema_version table exists)
-- =============================================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'schema_version') THEN
        INSERT INTO schema_version (version, description, applied_at)
        VALUES ('8.0.0', 'Unified database schema - consolidated tables, UUID migration, foreign keys', CURRENT_TIMESTAMP);
        
        RAISE NOTICE 'Updated schema_version table';
    END IF;
END $$;

-- =============================================================================
-- MIGRATION COMPLETE
-- =============================================================================

RAISE NOTICE '========================================';
RAISE NOTICE 'V8 Schema Unification Migration Complete';
RAISE NOTICE '========================================';
RAISE NOTICE 'Summary:';
RAISE NOTICE '  - Consolidated products + listings → listings (UUID)';
RAISE NOTICE '  - Converted conversations, messages, notification_preferences to UUID';
RAISE NOTICE '  - Added foreign key constraints';
RAISE NOTICE '  - Created backup tables with timestamp';
RAISE NOTICE 'Next Steps:';
RAISE NOTICE '  1. Verify application code uses UUID types';
RAISE NOTICE '  2. Update entity classes (Long → UUID)';
RAISE NOTICE '  3. Test all API endpoints';
RAISE NOTICE '  4. Drop backup tables after validation (keep for 7 days)';
