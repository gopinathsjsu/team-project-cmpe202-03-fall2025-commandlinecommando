-- =============================================================================
-- Campus Marketplace - API Performance Optimization
-- CMPE 202 Project: Advanced Indexes & Query Optimization
-- Version: 3.0.0
-- Target: <200ms API Response Times
-- =============================================================================

-- =============================================================================
-- ADDITIONAL PERFORMANCE INDEXES FOR RESTFUL API ENDPOINTS
-- =============================================================================

-- Buyer Role API Optimizations
-- ============================================================================

-- Marketplace product search with filters (most common API query)
CREATE INDEX CONCURRENTLY idx_products_marketplace_search 
ON products (university_id, category, is_active, moderation_status, price)
WHERE is_active = true AND moderation_status = 'APPROVED';

-- Product sorting by popularity (views + favorites)
CREATE INDEX CONCURRENTLY idx_products_popularity 
ON products ((view_count + favorite_count) DESC, created_at DESC)
WHERE is_active = true;

-- Product availability check (prevent overselling)
CREATE INDEX CONCURRENTLY idx_products_inventory 
ON products (product_id, quantity, sold_quantity)
WHERE is_active = true AND quantity > 0;

-- Price range queries for buyer filtering
CREATE INDEX CONCURRENTLY idx_products_price_range 
ON products (university_id, category, price ASC)
WHERE is_active = true AND moderation_status = 'APPROVED';

-- Recent listings for buyer homepage
CREATE INDEX CONCURRENTLY idx_products_recent 
ON products (university_id, created_at DESC, is_active, moderation_status)
WHERE is_active = true;

-- Seller Role API Optimizations
-- ============================================================================

-- Seller dashboard - my active listings
CREATE INDEX CONCURRENTLY idx_products_seller_dashboard 
ON products (seller_id, is_active, moderation_status, created_at DESC);

-- Seller sales history (completed orders)
CREATE INDEX CONCURRENTLY idx_order_items_seller_sales 
ON order_items (seller_id, fulfillment_status, created_at DESC);

-- Seller revenue tracking
CREATE INDEX CONCURRENTLY idx_seller_revenue 
ON order_items (seller_id, total_price)
WHERE fulfillment_status IN ('COMPLETED', 'DELIVERED');

-- Pending orders for seller fulfillment
CREATE INDEX CONCURRENTLY idx_order_items_seller_pending 
ON order_items (seller_id, fulfillment_status)
WHERE fulfillment_status IN ('PAID', 'PROCESSING');

-- Admin Role API Optimizations
-- ============================================================================

-- Moderation queue for admin review
CREATE INDEX CONCURRENTLY idx_moderation_queue_pending 
ON moderation_queue (status, created_at ASC)
WHERE status = 'PENDING';

-- User management - verification pending
CREATE INDEX CONCURRENTLY idx_users_pending_verification 
ON users (verification_status, created_at)
WHERE verification_status = 'PENDING';

-- Flagged content for admin review
CREATE INDEX CONCURRENTLY idx_user_reports_pending 
ON user_reports (status, created_at DESC)
WHERE status = 'PENDING';

-- Platform analytics aggregation
CREATE INDEX CONCURRENTLY idx_daily_analytics_admin 
ON daily_analytics (university_id, date DESC, revenue, orders_count);

-- User activity monitoring for admin
CREATE INDEX CONCURRENTLY idx_audit_logs_recent 
ON audit_logs (created_at DESC, table_name, action)
WHERE created_at > CURRENT_TIMESTAMP - INTERVAL '30 days';

-- =============================================================================
-- COMPOSITE INDEXES FOR COMPLEX API QUERIES
-- =============================================================================

-- Shopping cart management
CREATE INDEX CONCURRENTLY idx_orders_cart_management 
ON orders (buyer_id, status, updated_at DESC)
WHERE status = 'CART';

-- Order history for buyer profile
CREATE INDEX CONCURRENTLY idx_orders_buyer_history 
ON orders (buyer_id, status, created_at DESC)
WHERE status != 'CART';

-- Order tracking (buyer checking order status)
CREATE INDEX CONCURRENTLY idx_orders_tracking 
ON orders (order_id, buyer_id, status, tracking_number)
WHERE status IN ('SHIPPED', 'DELIVERED');

-- Payment processing lookup
CREATE INDEX CONCURRENTLY idx_transactions_processing 
ON transactions (order_id, status, processed_at DESC);

-- User payment methods for checkout
CREATE INDEX CONCURRENTLY idx_payment_methods_active 
ON payment_methods (user_id, is_active, is_default)
WHERE is_active = true;

-- Product reviews for product detail page
CREATE INDEX CONCURRENTLY idx_product_reviews_display 
ON product_reviews (product_id, is_visible, created_at DESC)
WHERE is_visible = true;

-- User wishlist/favorites retrieval
CREATE INDEX CONCURRENTLY idx_user_favorites_display 
ON user_favorites (user_id, created_at DESC);

-- =============================================================================
-- FULL-TEXT SEARCH OPTIMIZATION
-- =============================================================================

-- Additional GIN indexes for advanced search
CREATE INDEX CONCURRENTLY idx_products_description_search 
ON products USING gin(to_tsvector('english', description))
WHERE is_active = true;

-- Category-specific search optimization
CREATE INDEX CONCURRENTLY idx_products_textbook_search 
ON products USING gin((attributes->>'isbn') gin_trgm_ops)
WHERE category = 'TEXTBOOKS' AND is_active = true;

-- Electronics model search
CREATE INDEX CONCURRENTLY idx_products_electronics_search 
ON products USING gin(
    (COALESCE(attributes->>'brand', '') || ' ' || 
     COALESCE(attributes->>'model', '')) gin_trgm_ops
)
WHERE category = 'ELECTRONICS' AND is_active = true;

-- =============================================================================
-- ANALYTICS & REPORTING INDEXES
-- =============================================================================

-- Search analytics for recommendation engine
CREATE INDEX CONCURRENTLY idx_search_history_analytics 
ON search_history (user_id, search_query, created_at DESC);

-- Popular searches aggregation
CREATE INDEX CONCURRENTLY idx_search_history_trends 
ON search_history (search_query, created_at DESC)
WHERE created_at > CURRENT_TIMESTAMP - INTERVAL '7 days';

-- Product view patterns for recommendations
CREATE INDEX CONCURRENTLY idx_product_views_patterns 
ON product_views (user_id, product_id, viewed_at DESC);

-- Revenue reporting by date range
CREATE INDEX CONCURRENTLY idx_transactions_revenue 
ON transactions (processed_at DESC, amount, status)
WHERE status = 'COMPLETED';

-- =============================================================================
-- PARTIAL INDEXES FOR SPECIFIC USE CASES
-- =============================================================================

-- Featured products for homepage
CREATE INDEX CONCURRENTLY idx_products_featured 
ON products (university_id, is_featured, created_at DESC)
WHERE is_active = true AND is_featured = true;

-- Negotiable products for bargain hunters
CREATE INDEX CONCURRENTLY idx_products_negotiable 
ON products (university_id, category, negotiable, price ASC)
WHERE is_active = true AND negotiable = true;

-- Low stock alerts for sellers
CREATE INDEX CONCURRENTLY idx_products_low_stock 
ON products (seller_id, quantity, title)
WHERE is_active = true AND quantity <= 3;

-- Expired listings for cleanup
CREATE INDEX CONCURRENTLY idx_products_expired 
ON products (expires_at, seller_id)
WHERE expires_at < CURRENT_TIMESTAMP AND is_active = true;

-- =============================================================================
-- MATERIALIZED VIEWS FOR COMPLEX AGGREGATIONS
-- =============================================================================

-- Seller performance summary (cached for dashboard)
CREATE MATERIALIZED VIEW mv_seller_performance AS
SELECT 
    u.user_id AS seller_id,
    u.username,
    u.first_name || ' ' || u.last_name AS seller_name,
    COUNT(DISTINCT p.product_id) AS total_products,
    COUNT(DISTINCT CASE WHEN p.is_active THEN p.product_id END) AS active_products,
    COALESCE(SUM(p.view_count), 0) AS total_views,
    COUNT(DISTINCT oi.order_item_id) AS total_sales,
    COALESCE(SUM(oi.total_price), 0) AS total_revenue,
    COALESCE(AVG(pr.rating), 0) AS avg_rating,
    COUNT(DISTINCT pr.review_id) AS total_reviews,
    (
        SELECT COUNT(*) FROM order_items oi2 
        WHERE oi2.seller_id = u.user_id 
        AND oi2.fulfillment_status IN ('PAID', 'PROCESSING')
    ) AS pending_orders
FROM users u
LEFT JOIN products p ON u.user_id = p.seller_id
LEFT JOIN order_items oi ON p.product_id = oi.product_id 
    AND oi.fulfillment_status IN ('COMPLETED', 'DELIVERED')
LEFT JOIN product_reviews pr ON p.product_id = pr.product_id
WHERE u.role = 'SELLER'
GROUP BY u.user_id, u.username, u.first_name, u.last_name;

CREATE UNIQUE INDEX ON mv_seller_performance (seller_id);
CREATE INDEX ON mv_seller_performance (total_revenue DESC);

-- Popular products view (for recommendations)
CREATE MATERIALIZED VIEW mv_popular_products AS
SELECT 
    p.product_id,
    p.title,
    p.category,
    p.price,
    p.seller_id,
    p.view_count,
    p.favorite_count,
    (p.view_count * 0.4 + p.favorite_count * 0.6) AS popularity_score,
    COUNT(DISTINCT oi.order_item_id) AS purchase_count,
    COALESCE(AVG(pr.rating), 0) AS avg_rating,
    COUNT(DISTINCT pr.review_id) AS review_count
FROM products p
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN product_reviews pr ON p.product_id = pr.product_id
WHERE p.is_active = true AND p.moderation_status = 'APPROVED'
GROUP BY p.product_id, p.title, p.category, p.price, p.seller_id, p.view_count, p.favorite_count
ORDER BY popularity_score DESC;

CREATE UNIQUE INDEX ON mv_popular_products (product_id);
CREATE INDEX ON mv_popular_products (category, popularity_score DESC);

-- University marketplace statistics
CREATE MATERIALIZED VIEW mv_university_stats AS
SELECT 
    uni.university_id,
    uni.name AS university_name,
    COUNT(DISTINCT u.user_id) AS total_users,
    COUNT(DISTINCT CASE WHEN u.role = 'BUYER' THEN u.user_id END) AS total_buyers,
    COUNT(DISTINCT CASE WHEN u.role = 'SELLER' THEN u.user_id END) AS total_sellers,
    COUNT(DISTINCT p.product_id) AS total_products,
    COUNT(DISTINCT CASE WHEN p.is_active THEN p.product_id END) AS active_products,
    COUNT(DISTINCT o.order_id) AS total_orders,
    COALESCE(SUM(o.total_amount), 0) AS total_revenue,
    COALESCE(AVG(o.total_amount), 0) AS avg_order_value,
    (
        SELECT COUNT(*) FROM daily_analytics da 
        WHERE da.university_id = uni.university_id 
        AND da.date = CURRENT_DATE
    ) AS today_metrics_count
FROM universities uni
LEFT JOIN users u ON uni.university_id = u.university_id
LEFT JOIN products p ON uni.university_id = p.university_id
LEFT JOIN orders o ON uni.university_id = o.university_id AND o.status != 'CART'
GROUP BY uni.university_id, uni.name;

CREATE UNIQUE INDEX ON mv_university_stats (university_id);

-- =============================================================================
-- REFRESH FUNCTIONS FOR MATERIALIZED VIEWS
-- =============================================================================

-- Function to refresh all materialized views
CREATE OR REPLACE FUNCTION refresh_marketplace_analytics()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_seller_performance;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_popular_products;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_university_stats;
END;
$$ LANGUAGE plpgsql;

-- Schedule refresh (can be called by cron or application)
COMMENT ON FUNCTION refresh_marketplace_analytics() IS 
'Refreshes all marketplace materialized views. Should be run every 15 minutes during peak hours.';

-- =============================================================================
-- QUERY PERFORMANCE MONITORING
-- =============================================================================

-- Create table to track slow queries
CREATE TABLE IF NOT EXISTS query_performance_log (
    log_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    query_text TEXT NOT NULL,
    execution_time_ms NUMERIC,
    rows_returned BIGINT,
    user_id UUID REFERENCES users(user_id),
    endpoint VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_query_performance_slow 
ON query_performance_log (execution_time_ms DESC)
WHERE execution_time_ms > 200;  -- Log queries slower than 200ms

-- =============================================================================
-- STATISTICS COLLECTION FOR QUERY PLANNER
-- =============================================================================

-- Ensure statistics are up to date for optimal query planning
ANALYZE users;
ANALYZE products;
ANALYZE orders;
ANALYZE order_items;
ANALYZE transactions;
ANALYZE product_reviews;
ANALYZE product_views;
ANALYZE search_history;

-- =============================================================================
-- CONSTRAINT IMPROVEMENTS FOR DATA INTEGRITY
-- =============================================================================

-- Prevent overselling (ensure quantity doesn't go negative)
ALTER TABLE products 
ADD CONSTRAINT chk_quantity_non_negative 
CHECK (quantity >= 0);

-- Ensure order totals are calculated correctly
ALTER TABLE orders 
ADD CONSTRAINT chk_order_total_valid 
CHECK (total_amount = subtotal + tax_amount + delivery_fee);

-- Ensure order items have valid quantities
ALTER TABLE order_items 
ADD CONSTRAINT chk_order_item_quantity 
CHECK (quantity > 0 AND total_price = unit_price * quantity);

-- =============================================================================
-- SCHEMA VERSION UPDATE
-- =============================================================================

INSERT INTO schema_version (version, description) VALUES
('3.0.0', 'API Performance Optimization - Advanced Indexes & Materialized Views');

-- =============================================================================
-- MAINTENANCE RECOMMENDATIONS
-- =============================================================================

COMMENT ON MATERIALIZED VIEW mv_seller_performance IS 
'Refresh every 15 minutes: SELECT refresh_marketplace_analytics();';

COMMENT ON MATERIALIZED VIEW mv_popular_products IS 
'Refresh every 15 minutes for real-time recommendations';

COMMENT ON INDEX idx_products_marketplace_search IS 
'Critical for marketplace search API - monitor query performance';

-- =============================================================================
-- END OF API OPTIMIZATION
-- =============================================================================

