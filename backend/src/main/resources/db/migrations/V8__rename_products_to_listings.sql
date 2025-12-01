-- =============================================================================
-- V8__rename_products_to_listings.sql
-- Rename products table to listings for domain consistency
-- =============================================================================
-- Purpose: Align database table names with domain language
-- Risk Level: LOW - Simple rename operation
-- =============================================================================

-- Rename the table
ALTER TABLE IF EXISTS products RENAME TO listings;

-- Rename the primary key column
ALTER TABLE listings RENAME COLUMN product_id TO listing_id;

-- Rename indexes
ALTER INDEX IF EXISTS idx_products_seller RENAME TO idx_listings_seller;
ALTER INDEX IF EXISTS idx_products_university RENAME TO idx_listings_university;
ALTER INDEX IF EXISTS idx_products_category RENAME TO idx_listings_category;
ALTER INDEX IF EXISTS idx_products_status RENAME TO idx_listings_status;
ALTER INDEX IF EXISTS idx_products_price RENAME TO idx_listings_price;
ALTER INDEX IF EXISTS idx_products_search RENAME TO idx_listings_search;
ALTER INDEX IF EXISTS idx_products_seller_active RENAME TO idx_listings_seller_active;
ALTER INDEX IF EXISTS idx_products_search_vector RENAME TO idx_listings_search_vector;
ALTER INDEX IF EXISTS idx_products_title_trgm RENAME TO idx_listings_title_trgm;

-- Rename primary key constraint
ALTER TABLE listings RENAME CONSTRAINT products_pkey TO listings_pkey;

-- Rename triggers (if they exist)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_products_updated_at') THEN
        ALTER TRIGGER trigger_products_updated_at ON listings RENAME TO trigger_listings_updated_at;
    END IF;

    IF EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trigger_products_search_vector') THEN
        ALTER TRIGGER trigger_products_search_vector ON listings RENAME TO trigger_listings_search_vector;
    END IF;
END $$;

-- Update view to use new table name (if it exists)
DROP VIEW IF EXISTS vw_active_products CASCADE;
CREATE OR REPLACE VIEW vw_active_listings AS
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

-- Log the migration
DO $$
BEGIN
    RAISE NOTICE 'Successfully renamed products table to listings';
    RAISE NOTICE 'All related indexes, constraints, and triggers updated';
END $$;
