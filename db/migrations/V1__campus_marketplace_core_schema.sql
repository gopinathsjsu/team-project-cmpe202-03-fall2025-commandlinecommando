-- =============================================================================
-- Campus Marketplace Database Schema - Core Foundation
-- CMPE 202 Project: Role-Based Marketplace Platform
-- Version: 1.0.0
-- Cloud-Ready | RESTful API Optimized | Multi-Tenant Support
-- =============================================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";        -- For full-text search
CREATE EXTENSION IF NOT EXISTS "pgcrypto";       -- For encryption
CREATE EXTENSION IF NOT EXISTS "btree_gin";      -- For composite indexes

-- Set timezone for consistent timestamps
SET timezone = 'UTC';

-- =============================================================================
-- ENUM TYPES - Marketplace Domain Models
-- =============================================================================

-- User role types supporting three core personas
CREATE TYPE user_role AS ENUM ('BUYER', 'SELLER', 'ADMIN');

-- User verification status for university affiliation
CREATE TYPE verification_status AS ENUM ('PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED');

-- Product categories specific to campus marketplace
CREATE TYPE product_category AS ENUM (
    'TEXTBOOKS',
    'ELECTRONICS',
    'FURNITURE',
    'CLOTHING',
    'SPORTS_EQUIPMENT',
    'SERVICES',
    'OTHER'
);

-- Product condition for marketplace listings
CREATE TYPE product_condition AS ENUM ('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR');

-- Order lifecycle states
CREATE TYPE order_status AS ENUM (
    'CART',              -- Shopping cart (not yet ordered)
    'PENDING_PAYMENT',   -- Order placed, awaiting payment
    'PAID',              -- Payment confirmed
    'PROCESSING',        -- Seller processing order
    'SHIPPED',           -- Order shipped/in transit
    'DELIVERED',         -- Order delivered
    'COMPLETED',         -- Transaction completed
    'CANCELLED',         -- Order cancelled
    'REFUNDED'           -- Payment refunded
);

-- Payment method types
CREATE TYPE payment_method_type AS ENUM ('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'VENMO', 'CAMPUS_CARD');

-- Transaction status for payment processing
CREATE TYPE transaction_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

-- Delivery method options
CREATE TYPE delivery_method AS ENUM ('CAMPUS_PICKUP', 'DORM_DELIVERY', 'SHIPPING', 'DIGITAL');

-- Content moderation status
CREATE TYPE moderation_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'FLAGGED');

-- =============================================================================
-- CORE TABLES - Users & Authentication
-- =============================================================================

-- Universities table for multi-tenant support
CREATE TABLE universities (
    university_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(100) NOT NULL UNIQUE,  -- e.g., "sjsu.edu"
    city VARCHAR(100),
    state VARCHAR(50),
    country VARCHAR(50) DEFAULT 'USA',
    is_active BOOLEAN DEFAULT true,
    settings JSONB DEFAULT '{}',           -- University-specific settings
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table - Core user management for all roles
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    university_id UUID NOT NULL REFERENCES universities(university_id),
    
    -- Authentication
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    
    -- Profile Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url TEXT,
    
    -- Role & Status
    role user_role NOT NULL DEFAULT 'BUYER',
    verification_status verification_status DEFAULT 'PENDING',
    
    -- University Verification
    student_id VARCHAR(50),
    university_email VARCHAR(255),
    graduation_year INTEGER,
    major VARCHAR(100),
    
    -- Preferences (API-friendly JSON storage)
    preferences JSONB DEFAULT '{
        "notifications": true,
        "email_updates": true,
        "search_radius_miles": 10,
        "preferred_categories": [],
        "blocked_users": []
    }',
    
    -- Security & Tracking
    is_active BOOLEAN DEFAULT true,
    last_login_at TIMESTAMP,
    email_verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_student_id CHECK (
        (role = 'BUYER' OR role = 'SELLER') AND student_id IS NOT NULL 
        OR role = 'ADMIN'
    )
);

-- User addresses for delivery
CREATE TABLE user_addresses (
    address_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    address_type VARCHAR(50) NOT NULL,  -- 'DORM', 'APARTMENT', 'HOME', 'CAMPUS_BUILDING'
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) DEFAULT 'USA',
    
    -- Campus-specific location
    building_name VARCHAR(100),
    room_number VARCHAR(20),
    
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- PRODUCTS SCHEMA - Marketplace Catalog
-- =============================================================================

-- Product listings table
CREATE TABLE products (
    product_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    seller_id UUID NOT NULL REFERENCES users(user_id),
    university_id UUID NOT NULL REFERENCES universities(university_id),
    
    -- Product Information
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category product_category NOT NULL,
    condition product_condition NOT NULL,
    
    -- Pricing
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    original_price DECIMAL(10, 2),  -- For showing discount
    negotiable BOOLEAN DEFAULT false,
    
    -- Inventory
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity >= 0),
    sold_quantity INTEGER DEFAULT 0,
    
    -- Additional Attributes (flexible JSON for category-specific data)
    attributes JSONB DEFAULT '{}',  -- e.g., {"isbn": "123", "edition": "5th", "author": "Smith"}
    
    -- Visibility & Status
    is_active BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false,
    moderation_status moderation_status DEFAULT 'PENDING',
    
    -- Analytics
    view_count INTEGER DEFAULT 0,
    favorite_count INTEGER DEFAULT 0,
    
    -- Delivery Options
    delivery_methods JSONB DEFAULT '["CAMPUS_PICKUP"]',  -- Array of delivery_method values
    pickup_location VARCHAR(255),
    
    -- SEO & Search
    search_vector tsvector,  -- Full-text search optimization
    
    -- Timestamps
    published_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure university isolation
    CONSTRAINT fk_seller_university CHECK (
        (SELECT university_id FROM users WHERE user_id = seller_id) = university_id
    )
);

-- Product images table
CREATE TABLE product_images (
    image_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    
    image_url TEXT NOT NULL,
    thumbnail_url TEXT,
    display_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT false,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product reviews and ratings
CREATE TABLE product_reviews (
    review_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    buyer_id UUID NOT NULL REFERENCES users(user_id),
    order_id UUID,  -- Will reference orders table
    
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(200),
    comment TEXT,
    
    -- Review helpfulness
    helpful_count INTEGER DEFAULT 0,
    
    is_verified_purchase BOOLEAN DEFAULT false,
    is_visible BOOLEAN DEFAULT true,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(product_id, buyer_id, order_id)
);

-- Wishlist/Favorites
CREATE TABLE user_favorites (
    favorite_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(user_id, product_id)
);

-- =============================================================================
-- ORDERS SCHEMA - Transaction Processing
-- =============================================================================

-- Shopping carts and orders (unified table for lifecycle)
CREATE TABLE orders (
    order_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    buyer_id UUID NOT NULL REFERENCES users(user_id),
    university_id UUID NOT NULL REFERENCES universities(university_id),
    
    -- Order Details
    order_number VARCHAR(50) UNIQUE,  -- Human-readable order number
    status order_status NOT NULL DEFAULT 'CART',
    
    -- Pricing
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(10, 2) DEFAULT 0,
    delivery_fee DECIMAL(10, 2) DEFAULT 0,
    platform_fee DECIMAL(10, 2) DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    
    -- Delivery Information
    delivery_method delivery_method,
    delivery_address_id UUID REFERENCES user_addresses(address_id),
    delivery_instructions TEXT,
    
    -- Tracking
    tracking_number VARCHAR(100),
    estimated_delivery_date DATE,
    actual_delivery_date TIMESTAMP,
    
    -- Notes
    buyer_notes TEXT,
    seller_notes TEXT,
    
    -- Lifecycle Timestamps
    cart_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ordered_at TIMESTAMP,
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Order items (line items)
CREATE TABLE order_items (
    order_item_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(product_id),
    seller_id UUID NOT NULL REFERENCES users(user_id),
    
    -- Item Details (snapshot at time of order)
    product_title VARCHAR(255) NOT NULL,
    product_condition product_condition NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    total_price DECIMAL(10, 2) NOT NULL,
    
    -- Fulfillment
    fulfillment_status order_status DEFAULT 'PENDING_PAYMENT',
    shipped_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- PAYMENTS SCHEMA - Financial Processing
-- =============================================================================

-- Payment methods (tokenized for security)
CREATE TABLE payment_methods (
    payment_method_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    method_type payment_method_type NOT NULL,
    
    -- Tokenized payment data (never store raw card numbers)
    payment_token VARCHAR(255) NOT NULL,  -- Token from payment gateway
    
    -- Display information
    last_four VARCHAR(4),
    card_brand VARCHAR(50),  -- 'VISA', 'MASTERCARD', etc.
    expiry_month INTEGER,
    expiry_year INTEGER,
    
    billing_address_id UUID REFERENCES user_addresses(address_id),
    
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment transactions
CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(order_id),
    buyer_id UUID NOT NULL REFERENCES users(user_id),
    payment_method_id UUID REFERENCES payment_methods(payment_method_id),
    
    -- Transaction Details
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status transaction_status NOT NULL DEFAULT 'PENDING',
    
    -- Payment Gateway Integration
    gateway_transaction_id VARCHAR(255),  -- From Stripe, PayPal, etc.
    gateway_response JSONB,               -- Full gateway response
    
    -- Transaction Type
    transaction_type VARCHAR(50) NOT NULL,  -- 'PURCHASE', 'REFUND', 'PAYOUT'
    
    -- Timestamps
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seller payouts
CREATE TABLE seller_payouts (
    payout_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    seller_id UUID NOT NULL REFERENCES users(user_id),
    
    -- Payout Details
    amount DECIMAL(10, 2) NOT NULL,
    platform_fee DECIMAL(10, 2) DEFAULT 0,
    net_amount DECIMAL(10, 2) NOT NULL,
    
    -- Banking Information (encrypted)
    bank_account_token VARCHAR(255),
    
    status transaction_status DEFAULT 'PENDING',
    
    -- Processing
    processed_at TIMESTAMP,
    expected_deposit_date DATE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- ANALYTICS SCHEMA - Business Intelligence
-- =============================================================================

-- User search history for recommendations
CREATE TABLE search_history (
    search_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
    
    search_query TEXT NOT NULL,
    filters_applied JSONB,
    results_count INTEGER,
    clicked_product_id UUID REFERENCES products(product_id),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product view tracking
CREATE TABLE product_views (
    view_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
    
    -- Analytics data
    session_id VARCHAR(255),
    ip_address INET,
    user_agent TEXT,
    referrer TEXT,
    
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Platform analytics aggregations
CREATE TABLE daily_analytics (
    analytics_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    university_id UUID REFERENCES universities(university_id),
    date DATE NOT NULL,
    
    -- User metrics
    new_users_count INTEGER DEFAULT 0,
    active_users_count INTEGER DEFAULT 0,
    
    -- Product metrics
    new_products_count INTEGER DEFAULT 0,
    active_products_count INTEGER DEFAULT 0,
    
    -- Transaction metrics
    orders_count INTEGER DEFAULT 0,
    revenue DECIMAL(12, 2) DEFAULT 0,
    platform_fees DECIMAL(12, 2) DEFAULT 0,
    
    -- Engagement metrics
    searches_count INTEGER DEFAULT 0,
    product_views_count INTEGER DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(university_id, date)
);

-- =============================================================================
-- AUDIT & COMPLIANCE SCHEMA
-- =============================================================================

-- Comprehensive audit log
CREATE TABLE audit_logs (
    audit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- Context
    user_id UUID REFERENCES users(user_id),
    university_id UUID REFERENCES universities(university_id),
    
    -- Action Details
    table_name VARCHAR(100) NOT NULL,
    record_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,  -- 'INSERT', 'UPDATE', 'DELETE'
    
    -- Change Tracking
    old_values JSONB,
    new_values JSONB,
    changed_fields TEXT[],
    
    -- Request Context
    ip_address INET,
    user_agent TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Content moderation queue
CREATE TABLE moderation_queue (
    moderation_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    content_type VARCHAR(50) NOT NULL,  -- 'PRODUCT', 'REVIEW', 'USER'
    content_id UUID NOT NULL,
    
    status moderation_status DEFAULT 'PENDING',
    flagged_reason TEXT,
    
    moderator_id UUID REFERENCES users(user_id),
    moderator_notes TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User reports (for flagging inappropriate content)
CREATE TABLE user_reports (
    report_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reporter_id UUID NOT NULL REFERENCES users(user_id),
    
    reported_type VARCHAR(50) NOT NULL,  -- 'PRODUCT', 'USER', 'REVIEW'
    reported_id UUID NOT NULL,
    
    reason VARCHAR(100) NOT NULL,
    description TEXT,
    
    status moderation_status DEFAULT 'PENDING',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- =============================================================================
-- INDEXES - RESTful API Performance Optimization
-- =============================================================================

-- Users indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_university ON users(university_id) WHERE is_active = true;
CREATE INDEX idx_users_role ON users(role) WHERE is_active = true;
CREATE INDEX idx_users_verification ON users(verification_status);

-- Products indexes (critical for marketplace search)
CREATE INDEX idx_products_seller ON products(seller_id) WHERE is_active = true;
CREATE INDEX idx_products_university ON products(university_id) WHERE is_active = true;
CREATE INDEX idx_products_category ON products(category) WHERE is_active = true;
CREATE INDEX idx_products_status ON products(moderation_status) WHERE is_active = true;
CREATE INDEX idx_products_price ON products(price) WHERE is_active = true;

-- Composite indexes for common API queries
CREATE INDEX idx_products_search ON products(university_id, category, is_active, price);
CREATE INDEX idx_products_seller_active ON products(seller_id, is_active, created_at DESC);

-- Full-text search index
CREATE INDEX idx_products_search_vector ON products USING gin(search_vector);
CREATE INDEX idx_products_title_trgm ON products USING gin(title gin_trgm_ops);

-- Orders indexes
CREATE INDEX idx_orders_buyer ON orders(buyer_id, status);
CREATE INDEX idx_orders_status ON orders(status) WHERE status != 'CART';
CREATE INDEX idx_orders_date ON orders(created_at DESC);

-- Order items indexes
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_seller ON order_items(seller_id, fulfillment_status);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- Analytics indexes
CREATE INDEX idx_product_views_product ON product_views(product_id, viewed_at DESC);
CREATE INDEX idx_search_history_user ON search_history(user_id, created_at DESC);
CREATE INDEX idx_daily_analytics_date ON daily_analytics(university_id, date DESC);

-- Audit indexes
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id, created_at DESC);
CREATE INDEX idx_audit_logs_table ON audit_logs(table_name, record_id);

-- =============================================================================
-- FUNCTIONS & TRIGGERS - Automation
-- =============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to update product search vector
CREATE OR REPLACE FUNCTION update_product_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.category::text, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to generate order number
CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status != 'CART' AND NEW.order_number IS NULL THEN
        NEW.order_number := 'ORD-' || 
            TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD') || '-' || 
            LPAD(NEXTVAL('order_number_seq')::TEXT, 6, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Sequence for order numbers
CREATE SEQUENCE IF NOT EXISTS order_number_seq START 1;

-- Apply updated_at triggers to all tables
CREATE TRIGGER trigger_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Apply search vector trigger
CREATE TRIGGER trigger_products_search_vector 
    BEFORE INSERT OR UPDATE OF title, description, category ON products
    FOR EACH ROW EXECUTE FUNCTION update_product_search_vector();

-- Apply order number generation trigger
CREATE TRIGGER trigger_generate_order_number
    BEFORE INSERT OR UPDATE OF status ON orders
    FOR EACH ROW EXECUTE FUNCTION generate_order_number();

-- =============================================================================
-- VIEWS - API Response Optimization
-- =============================================================================

-- Active products view for marketplace listing
CREATE VIEW vw_active_products AS
SELECT 
    p.product_id,
    p.seller_id,
    u.username AS seller_username,
    u.avatar_url AS seller_avatar,
    p.title,
    p.description,
    p.category,
    p.condition,
    p.price,
    p.original_price,
    p.quantity,
    p.view_count,
    p.favorite_count,
    (SELECT image_url FROM product_images WHERE product_id = p.product_id AND is_primary = true LIMIT 1) AS primary_image,
    (SELECT COALESCE(AVG(rating), 0) FROM product_reviews WHERE product_id = p.product_id) AS avg_rating,
    (SELECT COUNT(*) FROM product_reviews WHERE product_id = p.product_id) AS review_count,
    p.created_at,
    p.university_id
FROM products p
JOIN users u ON p.seller_id = u.user_id
WHERE p.is_active = true 
  AND p.moderation_status = 'APPROVED'
  AND p.quantity > 0;

-- Seller dashboard view
CREATE VIEW vw_seller_dashboard AS
SELECT 
    u.user_id AS seller_id,
    COUNT(DISTINCT p.product_id) AS total_products,
    COUNT(DISTINCT CASE WHEN p.is_active THEN p.product_id END) AS active_products,
    COALESCE(SUM(p.view_count), 0) AS total_views,
    COUNT(DISTINCT oi.order_item_id) AS total_sales,
    COALESCE(SUM(oi.total_price), 0) AS total_revenue,
    COALESCE(AVG(pr.rating), 0) AS avg_rating,
    COUNT(DISTINCT pr.review_id) AS total_reviews
FROM users u
LEFT JOIN products p ON u.user_id = p.seller_id
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN product_reviews pr ON p.product_id = pr.product_id
WHERE u.role = 'SELLER'
GROUP BY u.user_id;

-- Admin analytics view
CREATE VIEW vw_admin_analytics AS
SELECT 
    u.university_id,
    COUNT(DISTINCT u.user_id) AS total_users,
    COUNT(DISTINCT CASE WHEN u.role = 'BUYER' THEN u.user_id END) AS total_buyers,
    COUNT(DISTINCT CASE WHEN u.role = 'SELLER' THEN u.user_id END) AS total_sellers,
    COUNT(DISTINCT p.product_id) AS total_products,
    COUNT(DISTINCT CASE WHEN p.is_active THEN p.product_id END) AS active_products,
    COUNT(DISTINCT o.order_id) AS total_orders,
    COALESCE(SUM(o.total_amount), 0) AS total_revenue
FROM universities uni
LEFT JOIN users u ON uni.university_id = u.university_id
LEFT JOIN products p ON uni.university_id = p.university_id
LEFT JOIN orders o ON uni.university_id = o.university_id AND o.status != 'CART'
GROUP BY u.university_id;

-- =============================================================================
-- ROW LEVEL SECURITY (RLS) - Multi-Tenant Data Isolation
-- =============================================================================

-- Enable RLS on core tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Users can only see users from their university
CREATE POLICY university_isolation_users ON users
    FOR ALL
    USING (university_id = current_setting('app.university_id')::UUID);

-- RLS Policy: Products are isolated by university
CREATE POLICY university_isolation_products ON products
    FOR ALL
    USING (university_id = current_setting('app.university_id')::UUID);

-- RLS Policy: Orders are isolated by university
CREATE POLICY university_isolation_orders ON orders
    FOR ALL
    USING (university_id = current_setting('app.university_id')::UUID);

-- =============================================================================
-- INITIAL DATA - Demo & Testing
-- =============================================================================

-- Insert default university
INSERT INTO universities (name, domain, city, state) VALUES
('San Jose State University', 'sjsu.edu', 'San Jose', 'California');

-- =============================================================================
-- GRANTS - Application User Permissions
-- =============================================================================

-- Grant permissions to application user (assuming cm_app_user exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'cm_app_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO cm_app_user;
        GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO cm_app_user;
    END IF;
END $$;

-- =============================================================================
-- SCHEMA VERSION TRACKING
-- =============================================================================

CREATE TABLE IF NOT EXISTS schema_version (
    version VARCHAR(20) PRIMARY KEY,
    description TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO schema_version (version, description) VALUES
('1.0.0', 'Campus Marketplace Core Schema - Role-Based Design with API Optimization');

-- =============================================================================
-- END OF SCHEMA
-- =============================================================================

