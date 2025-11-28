-- =============================================================================
-- Campus Marketplace - Search & Discovery Features
-- CMPE 202 Project: Enhanced Search and Discovery Tables
-- Version: 5.0.0
-- Adds search history tracking, product views, and discovery features
-- =============================================================================

-- =============================================================================
-- SEARCH HISTORY TABLE
-- =============================================================================

-- Track user search queries for recent searches and analytics
CREATE TABLE IF NOT EXISTS search_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    search_query VARCHAR(500) NOT NULL,
    results_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Add index for efficient querying
    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Indexes for search history
CREATE INDEX idx_search_history_user ON search_history(user_id, created_at DESC);
CREATE INDEX idx_search_history_query ON search_history(search_query);
CREATE INDEX idx_search_history_created ON search_history(created_at DESC);

-- =============================================================================
-- PRODUCT VIEWS TABLE
-- =============================================================================

-- Track product views for recently viewed and recommendation features
-- Uses composite unique constraint to prevent duplicate views per day
CREATE TABLE IF NOT EXISTS product_views (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    viewed_at_date DATE DEFAULT CURRENT_DATE,
    
    -- Foreign key constraints
    CONSTRAINT fk_product_views_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_product_views_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    
    -- Unique constraint: one view per user per product per day
    CONSTRAINT uniq_user_product_view_per_day UNIQUE (user_id, product_id, viewed_at_date)
);

-- Indexes for product views
CREATE INDEX idx_product_views_user ON product_views(user_id, viewed_at DESC);
CREATE INDEX idx_product_views_product ON product_views(product_id, viewed_at DESC);
CREATE INDEX idx_product_views_user_product ON product_views(user_id, product_id, viewed_at DESC);
CREATE INDEX idx_product_views_date ON product_views(viewed_at_date DESC);

-- =============================================================================
-- OPTIMIZE EXISTING SEARCH INDEXES
-- =============================================================================

-- Ensure search_vector GIN index exists (should exist from V1, but verify)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_products_search_vector_weighted
ON products USING gin(search_vector);

-- Add trigram index for fuzzy search (typo tolerance)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_products_title_trgm
ON products USING gin(title gin_trgm_ops);

-- Add trigram index for description fuzzy search
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_products_description_trgm
ON products USING gin(description gin_trgm_ops);

-- =============================================================================
-- HELPER FUNCTIONS FOR PRODUCT VIEWS
-- =============================================================================

-- Function to upsert product view (update timestamp if exists, insert if not)
CREATE OR REPLACE FUNCTION upsert_product_view(
    p_user_id UUID,
    p_product_id UUID
)
RETURNS VOID AS $$
BEGIN
    INSERT INTO product_views (user_id, product_id, viewed_at, viewed_at_date)
    VALUES (p_user_id, p_product_id, CURRENT_TIMESTAMP, CURRENT_DATE)
    ON CONFLICT (user_id, product_id, viewed_at_date)
    DO UPDATE SET viewed_at = CURRENT_TIMESTAMP;
    
    -- Increment product view count
    UPDATE products
    SET view_count = view_count + 1
    WHERE product_id = p_product_id;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- ANALYTICS VIEWS FOR DISCOVERY FEATURES
-- =============================================================================

-- View for popular searches (for autocomplete suggestions)
CREATE OR REPLACE VIEW v_popular_searches AS
SELECT 
    search_query,
    COUNT(*) as search_count,
    MAX(created_at) as last_searched
FROM search_history
WHERE created_at > CURRENT_TIMESTAMP - INTERVAL '30 days'
GROUP BY search_query
ORDER BY search_count DESC, last_searched DESC
LIMIT 100;

-- View for trending products (based on recent views and favorites)
CREATE OR REPLACE VIEW v_trending_products AS
SELECT 
    p.product_id,
    p.title,
    p.price,
    p.category,
    p.condition,
    p.view_count,
    p.favorite_count,
    p.created_at,
    COUNT(DISTINCT pv.id) as recent_views,
    (p.view_count * 0.4 + p.favorite_count * 0.6 + COUNT(DISTINCT pv.id) * 2) as trending_score
FROM products p
LEFT JOIN product_views pv ON p.product_id = pv.product_id 
    AND pv.viewed_at > CURRENT_TIMESTAMP - INTERVAL '7 days'
WHERE p.is_active = true 
    AND p.moderation_status = 'APPROVED'
GROUP BY p.product_id, p.title, p.price, p.category, p.condition, 
         p.view_count, p.favorite_count, p.created_at
ORDER BY trending_score DESC;

-- =============================================================================
-- CLEANUP FUNCTION FOR OLD DATA
-- =============================================================================

-- Function to clean up old search history (keep last 90 days)
CREATE OR REPLACE FUNCTION cleanup_old_search_history()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM search_history
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '90 days';
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Function to clean up old product views (keep last 90 days)
CREATE OR REPLACE FUNCTION cleanup_old_product_views()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM product_views
    WHERE viewed_at < CURRENT_TIMESTAMP - INTERVAL '90 days';
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- SCHEMA VERSION UPDATE
-- =============================================================================

INSERT INTO schema_version (version, description, applied_at) 
VALUES ('5.0.0', 'Search & Discovery Features - Search History and Product Views', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE search_history IS 
'Tracks user search queries for recent searches feature and search analytics';

COMMENT ON TABLE product_views IS 
'Tracks product views for recently viewed items and recommendation engine. One view per user per product per day.';

COMMENT ON FUNCTION upsert_product_view(UUID, UUID) IS 
'Upserts a product view record and increments the product view count. Idempotent per day.';

COMMENT ON VIEW v_popular_searches IS 
'Top 100 most popular search queries in the last 30 days for autocomplete suggestions';

COMMENT ON VIEW v_trending_products IS 
'Trending products based on recent views, favorites, and overall engagement';

-- =============================================================================
-- END OF MIGRATION
-- =============================================================================

