-- Campus Marketplace Database Schema Initialization
-- This script creates the basic schema structure for the application

-- Enable UUID extension (if not already enabled)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'UTC';

-- Create custom types/enums
CREATE TYPE user_role AS ENUM ('STUDENT', 'ADMIN');
CREATE TYPE admin_level AS ENUM ('SUPER_ADMIN', 'MODERATOR');
CREATE TYPE listing_status AS ENUM ('ACTIVE', 'SOLD', 'INACTIVE', 'FLAGGED');
CREATE TYPE category AS ENUM ('ELECTRONICS', 'BOOKS', 'FURNITURE', 'CLOTHING', 'SPORTS', 'OTHER');

-- Audit trail function for tracking changes
CREATE OR REPLACE FUNCTION audit_trigger() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit.activity_log (table_name, operation, new_data, created_at)
        VALUES (TG_TABLE_NAME, TG_OP, to_jsonb(NEW), NOW());
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit.activity_log (table_name, operation, old_data, new_data, created_at)
        VALUES (TG_TABLE_NAME, TG_OP, to_jsonb(OLD), to_jsonb(NEW), NOW());
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit.activity_log (table_name, operation, old_data, created_at)
        VALUES (TG_TABLE_NAME, TG_OP, to_jsonb(OLD), NOW());
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create audit table
CREATE TABLE IF NOT EXISTS audit.activity_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    operation VARCHAR(10) NOT NULL,
    old_data JSONB,
    new_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active) WHERE active = true;

CREATE INDEX IF NOT EXISTS idx_listings_seller_id ON listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_listings_status ON listings(status);
CREATE INDEX IF NOT EXISTS idx_listings_category ON listings(category);
CREATE INDEX IF NOT EXISTS idx_listings_created_at ON listings(created_at);
CREATE INDEX IF NOT EXISTS idx_listings_price ON listings(price);
CREATE INDEX IF NOT EXISTS idx_listings_active_status ON listings(status) WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_listing_images_listing_id ON listing_images(listing_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry ON refresh_tokens(expiry_date);

-- Create composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_listings_seller_status ON listings(seller_id, status);
CREATE INDEX IF NOT EXISTS idx_listings_category_status ON listings(category, status);
CREATE INDEX IF NOT EXISTS idx_listings_status_created ON listings(status, created_at);

-- Full-text search index for listing titles and descriptions
CREATE INDEX IF NOT EXISTS idx_listings_search ON listings 
USING gin(to_tsvector('english', title || ' ' || COALESCE(description, '')));

-- Create some useful views for reporting
CREATE OR REPLACE VIEW reporting.user_statistics AS
SELECT 
    u.role,
    COUNT(*) as total_users,
    COUNT(*) FILTER (WHERE u.active = true) as active_users,
    COUNT(*) FILTER (WHERE u.created_at >= NOW() - INTERVAL '30 days') as new_users_last_30_days,
    COUNT(*) FILTER (WHERE u.last_login >= NOW() - INTERVAL '7 days') as active_last_7_days
FROM users u
GROUP BY u.role;

CREATE OR REPLACE VIEW reporting.listing_statistics AS
SELECT 
    l.category,
    l.status,
    COUNT(*) as total_listings,
    AVG(l.price) as avg_price,
    MIN(l.price) as min_price,
    MAX(l.price) as max_price,
    COUNT(*) FILTER (WHERE l.created_at >= NOW() - INTERVAL '30 days') as new_listings_last_30_days
FROM listings l
GROUP BY l.category, l.status;

CREATE OR REPLACE VIEW reporting.daily_activity AS
SELECT 
    DATE(created_at) as activity_date,
    COUNT(*) FILTER (WHERE table_name = 'users') as new_users,
    COUNT(*) FILTER (WHERE table_name = 'listings') as new_listings,
    COUNT(*) FILTER (WHERE table_name = 'listings' AND operation = 'UPDATE') as listing_updates
FROM audit.activity_log
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY activity_date DESC;

-- Performance monitoring function
CREATE OR REPLACE FUNCTION get_table_sizes() RETURNS TABLE(
    table_name TEXT,
    size_pretty TEXT,
    size_bytes BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        schemaname||'.'||tablename AS table_name,
        pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size_pretty,
        pg_total_relation_size(schemaname||'.'||tablename) AS size_bytes
    FROM pg_tables 
    WHERE schemaname NOT IN ('information_schema', 'pg_catalog')
    ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
END;
$$ LANGUAGE plpgsql;

-- Create connection monitoring view
CREATE OR REPLACE VIEW reporting.connection_stats AS
SELECT 
    datname as database,
    state,
    COUNT(*) as connection_count,
    MAX(now() - query_start) as longest_running_query,
    AVG(now() - query_start) as avg_query_time
FROM pg_stat_activity 
WHERE datname = current_database()
GROUP BY datname, state;

COMMENT ON SCHEMA public IS 'Main application schema for Campus Marketplace';
COMMENT ON SCHEMA audit IS 'Audit trail schema for tracking all data changes';
COMMENT ON SCHEMA reporting IS 'Reporting schema with views and aggregated data';

-- Grant permissions to application user on views
GRANT SELECT ON reporting.user_statistics TO cm_app_user;
GRANT SELECT ON reporting.listing_statistics TO cm_app_user;
GRANT SELECT ON reporting.daily_activity TO cm_app_user;
GRANT SELECT ON reporting.connection_stats TO cm_app_user;

-- Grant permissions to readonly user on views
GRANT SELECT ON reporting.user_statistics TO cm_readonly;
GRANT SELECT ON reporting.listing_statistics TO cm_readonly;
GRANT SELECT ON reporting.daily_activity TO cm_readonly;
GRANT SELECT ON reporting.connection_stats TO cm_readonly;