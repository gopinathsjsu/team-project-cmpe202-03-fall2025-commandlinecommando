-- schema_postgres.sql
-- Campus Marketplace PostgreSQL Database Schema
-- Aligned with Spring Boot 3.5.6 codebase and Hibernate 6.6.29
-- Compatible with PostgreSQL 14+
-- Primary Keys: UUID (not BIGINT)
-- Generated: 2024-10-29

-- =====================================================
-- SCHEMA SETUP AND EXTENSIONS
-- =====================================================

-- Enable UUID extension for primary keys
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Drop existing tables in correct order (foreign key dependencies)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS product_views CASCADE;
DROP TABLE IF EXISTS search_history CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS universities CASCADE;

-- =====================================================
-- UNIVERSITY TABLE (Multi-tenant support)
-- =====================================================

CREATE TABLE universities (
    university_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- University Information
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) NOT NULL UNIQUE, -- e.g., "sjsu.edu"
    city VARCHAR(100),
    state VARCHAR(50),
    country VARCHAR(50) DEFAULT 'USA',

    -- Status
    is_active BOOLEAN NOT NULL DEFAULT true,

    -- Flexible settings (JSON for university-specific config)
    settings TEXT, -- JSONB stored as TEXT for H2 compatibility

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- USER TABLE (STUDENT and ADMIN roles)
-- =====================================================

CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- University relationship (nullable for tests)
    university_id UUID REFERENCES universities(university_id) ON DELETE SET NULL,

    -- Authentication
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- BCrypt hash

    -- Profile Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),

    -- Role & Status
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'ADMIN')),
    verification_status VARCHAR(20) DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'VERIFIED', 'SUSPENDED')),

    -- Student-specific fields
    student_id VARCHAR(50),
    university_email VARCHAR(100),
    graduation_year INTEGER CHECK (graduation_year >= 2020 AND graduation_year <= 2035),
    major VARCHAR(100),

    -- Preferences (JSON storage for flexible user settings)
    preferences TEXT, -- JSONB stored as TEXT for H2 compatibility

    -- Security & Tracking
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login_at TIMESTAMP,
    email_verified_at TIMESTAMP,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- =====================================================
-- REFRESH TOKENS TABLE (JWT Authentication)
-- =====================================================

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Token and user relationship
    token VARCHAR(1000) NOT NULL UNIQUE, -- JWT refresh token (increased from 500)
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    -- Expiry and status
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT false,

    -- Optional device tracking
    device_info VARCHAR(255),

    CONSTRAINT chk_expires_future CHECK (expires_at > created_at)
);

-- =====================================================
-- PRODUCTS TABLE (Marketplace listings)
-- =====================================================

CREATE TABLE products (
    product_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Relationships
    seller_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    university_id UUID REFERENCES universities(university_id) ON DELETE SET NULL,

    -- Product Information
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(20) NOT NULL CHECK (category IN ('TEXTBOOKS', 'GADGETS', 'ELECTRONICS', 'STATIONARY', 'OTHER')),
    condition VARCHAR(20) NOT NULL CHECK (condition IN ('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR')),

    -- Pricing
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    original_price DECIMAL(10,2) CHECK (original_price >= 0),
    negotiable BOOLEAN NOT NULL DEFAULT false,

    -- Inventory
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity >= 0),
    sold_quantity INTEGER NOT NULL DEFAULT 0 CHECK (sold_quantity >= 0),

    -- Additional Attributes (flexible JSON for category-specific data)
    -- e.g., {"isbn": "123", "edition": "5th", "author": "Smith"} for textbooks
    attributes TEXT, -- JSONB stored as TEXT for H2 compatibility

    -- Visibility & Status
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_featured BOOLEAN NOT NULL DEFAULT false,
    moderation_status VARCHAR(20) DEFAULT 'PENDING' CHECK (moderation_status IN ('PENDING', 'APPROVED', 'REJECTED')),

    -- Analytics
    view_count INTEGER NOT NULL DEFAULT 0,
    favorite_count INTEGER NOT NULL DEFAULT 0,

    -- Delivery Options (stored as JSON array)
    delivery_methods TEXT, -- JSON array: ["CAMPUS_PICKUP", "SHIPPING", "MEET_UP"]
    pickup_location VARCHAR(255),

    -- Lifecycle timestamps
    published_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- SEARCH HISTORY TABLE (Tracks user search queries)
-- =====================================================

CREATE TABLE search_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    search_query VARCHAR(500) NOT NULL,
    results_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =====================================================
-- PRODUCT VIEWS TABLE (Tracks product views for recommendations)
-- =====================================================

CREATE TABLE product_views (
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

-- =====================================================
-- ORDERS TABLE (Shopping cart and completed orders)
-- =====================================================

CREATE TABLE orders (
    order_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Relationships
    buyer_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    university_id UUID NOT NULL REFERENCES universities(university_id) ON DELETE RESTRICT,

    -- Order Details
    order_number VARCHAR(50) UNIQUE, -- Human-readable order number (generated on PAID status)
    status VARCHAR(20) NOT NULL DEFAULT 'CART' CHECK (status IN (
        'CART', 'PENDING_PAYMENT', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED', 'REFUNDED'
    )),

    -- Pricing
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (delivery_fee >= 0),
    platform_fee DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (platform_fee >= 0),
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),

    -- Delivery Information
    delivery_method VARCHAR(20) CHECK (delivery_method IN ('CAMPUS_PICKUP', 'SHIPPING', 'MEET_UP')),
    delivery_address_id UUID, -- Reference to user_addresses (future table)
    delivery_instructions TEXT,

    -- Tracking
    tracking_number VARCHAR(100),
    estimated_delivery_date DATE,
    actual_delivery_date TIMESTAMP,

    -- Notes
    buyer_notes TEXT,
    seller_notes TEXT,

    -- Lifecycle Timestamps
    cart_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ordered_at TIMESTAMP,
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    -- Standard Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- ORDER ITEMS TABLE (Line items in orders)
-- =====================================================

CREATE TABLE order_items (
    order_item_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Relationships
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
    seller_id UUID NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,

    -- Snapshot product details at time of order (for historical accuracy)
    product_title VARCHAR(255) NOT NULL,
    product_condition VARCHAR(20) NOT NULL CHECK (product_condition IN ('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR')),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price >= 0),

    -- Fulfillment tracking
    fulfillment_status VARCHAR(20) DEFAULT 'PENDING_PAYMENT' CHECK (fulfillment_status IN (
        'PENDING_PAYMENT', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED', 'REFUNDED'
    )),
    shipped_at TIMESTAMP,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- =====================================================

-- University indexes
CREATE INDEX idx_university_domain ON universities(domain);
CREATE INDEX idx_university_active ON universities(is_active);

-- User indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_university ON users(university_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_verification ON users(verification_status);
CREATE INDEX idx_users_student_id ON users(student_id) WHERE student_id IS NOT NULL;

-- Refresh token indexes
CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_is_revoked ON refresh_tokens(is_revoked);

-- Product indexes
CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_products_university ON products(university_id);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_status ON products(moderation_status);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_search ON products(university_id, category, is_active, price);

-- Search history indexes
CREATE INDEX idx_search_history_user ON search_history(user_id, created_at DESC);
CREATE INDEX idx_search_history_query ON search_history(search_query);
CREATE INDEX idx_search_history_created ON search_history(created_at DESC);

-- Product views indexes
CREATE INDEX idx_product_views_user ON product_views(user_id, viewed_at DESC);
CREATE INDEX idx_product_views_product ON product_views(product_id, viewed_at DESC);
CREATE INDEX idx_product_views_user_product ON product_views(user_id, product_id, viewed_at DESC);
CREATE INDEX idx_product_views_date ON product_views(viewed_at_date DESC);

-- Order indexes
CREATE INDEX idx_orders_buyer ON orders(buyer_id, status);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_date ON orders(created_at);
CREATE INDEX idx_orders_university ON orders(university_id);

-- Order item indexes
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_seller ON order_items(seller_id, fulfillment_status);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- =====================================================
-- TRIGGERS FOR AUTOMATIC TIMESTAMP UPDATES
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to tables with updated_at column
CREATE TRIGGER update_universities_updated_at
    BEFORE UPDATE ON universities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_order_items_updated_at
    BEFORE UPDATE ON order_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- LISTING API TABLES (Listings, Images, Reports)
-- =====================================================

-- Drop existing tables (Listing API) in dependency order
DROP TABLE IF EXISTS listing_images CASCADE;
DROP TABLE IF EXISTS reports CASCADE;
DROP TABLE IF EXISTS listings CASCADE;

-- LISTINGS TABLE
CREATE TABLE listings (
    listing_id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(20) NOT NULL CHECK (category IN ('TEXTBOOKS', 'GADGETS', 'ELECTRONICS', 'STATIONARY', 'OTHER')),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    condition VARCHAR(20) NOT NULL CHECK (condition IN ('NEW', 'LIKE_NEW', 'GOOD', 'USED')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('PENDING', 'ACTIVE', 'SOLD', 'CANCELLED')),
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    view_count INTEGER NOT NULL DEFAULT 0
);

-- LISTING IMAGES TABLE
CREATE TABLE listing_images (
    image_id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0
);

-- REPORTS TABLE (for listings)
CREATE TABLE reports (
    report_id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    report_type VARCHAR(50) NOT NULL CHECK (report_type IN ('SPAM','INAPPROPRIATE_CONTENT','FAKE_LISTING','HARASSMENT','COPYRIGHT_VIOLATION','PRICE_MANIPULATION','OTHER')),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','UNDER_REVIEW','RESOLVED','DISMISSED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT
);

-- Indexes for Listing API tables
CREATE INDEX idx_listings_category ON listings(category);
CREATE INDEX idx_listings_status ON listings(status);
CREATE INDEX idx_listings_price ON listings(price);
CREATE INDEX idx_listings_seller ON listings(seller_id);
CREATE INDEX idx_listing_images_listing ON listing_images(listing_id, display_order);
CREATE INDEX idx_reports_listing ON reports(listing_id);
CREATE INDEX idx_reports_reporter ON reports(reporter_id);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_created_at ON reports(created_at);

-- Trigger to auto-update updated_at on listings
CREATE TRIGGER update_listings_updated_at
    BEFORE UPDATE ON listings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- COMMUNICATION SERVICE TABLES (Chat/Messaging)
-- =====================================================

-- Drop existing tables (Communication Service) in dependency order
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS conversations CASCADE;

-- CONVERSATIONS TABLE
-- Links a buyer and seller for a specific listing
CREATE TABLE conversations (
    conversation_id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure one conversation per buyer-seller-listing combination
    CONSTRAINT unique_conversation UNIQUE (listing_id, buyer_id, seller_id)
);

-- MESSAGES TABLE
-- Individual messages within a conversation
CREATE TABLE messages (
    message_id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Communication Service tables
CREATE INDEX idx_conversations_listing ON conversations(listing_id);
CREATE INDEX idx_conversations_buyer ON conversations(buyer_id);
CREATE INDEX idx_conversations_seller ON conversations(seller_id);
CREATE INDEX idx_conversations_updated ON conversations(updated_at DESC);

CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_created ON messages(created_at DESC);
CREATE INDEX idx_messages_unread ON messages(conversation_id, is_read) WHERE is_read = false;

-- Function to update conversation updated_at timestamp when a message is added
CREATE OR REPLACE FUNCTION update_conversation_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE conversations
    SET updated_at = CURRENT_TIMESTAMP
    WHERE conversation_id = NEW.conversation_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update conversation timestamp when a message is added
CREATE TRIGGER update_conversation_on_message
    AFTER INSERT ON messages
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_timestamp();

-- Trigger to auto-update updated_at on conversations
CREATE TRIGGER update_conversations_updated_at
    BEFORE UPDATE ON conversations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- USER MANAGEMENT TABLES (Authentication, Audit, Security)
-- =====================================================

-- Drop existing tables (User Management) in dependency order
DROP TABLE IF EXISTS account_actions CASCADE;
DROP TABLE IF EXISTS login_attempts CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS verification_tokens CASCADE;

-- VERIFICATION TOKENS TABLE
-- Stores email verification tokens, password reset tokens, and email change tokens
CREATE TABLE verification_tokens (
    token_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token VARCHAR(100) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token_type VARCHAR(50) NOT NULL CHECK (token_type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'EMAIL_CHANGE')),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for verification_tokens
CREATE INDEX idx_verification_token ON verification_tokens(token);
CREATE INDEX idx_verification_user ON verification_tokens(user_id);
CREATE INDEX idx_verification_expires ON verification_tokens(expires_at);
CREATE INDEX idx_verification_type ON verification_tokens(token_type);

-- AUDIT LOGS TABLE
-- Comprehensive audit logging for all user actions and admin operations
CREATE TABLE audit_logs (
    audit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
    username VARCHAR(50),  -- Store username for reference even if user is deleted
    table_name VARCHAR(100) NOT NULL,
    record_id UUID,
    action VARCHAR(50) NOT NULL,
    old_values TEXT,  -- JSONB stored as TEXT for compatibility
    new_values TEXT,  -- JSONB stored as TEXT for compatibility
    description VARCHAR(500),
    ip_address VARCHAR(45),  -- Support IPv6
    user_agent VARCHAR(500),
    severity VARCHAR(20) DEFAULT 'INFO' CHECK (severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for audit_logs
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_username ON audit_logs(username);
CREATE INDEX idx_audit_table ON audit_logs(table_name);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_created ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_record ON audit_logs(record_id);
CREATE INDEX idx_audit_severity ON audit_logs(severity);

-- LOGIN ATTEMPTS TABLE
-- Track login attempts for security monitoring and account lockout
CREATE TABLE login_attempts (
    attempt_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL DEFAULT FALSE,
    failure_reason VARCHAR(200),
    device_info VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for login_attempts
CREATE INDEX idx_login_username ON login_attempts(username);
CREATE INDEX idx_login_ip ON login_attempts(ip_address);
CREATE INDEX idx_login_created ON login_attempts(created_at DESC);
CREATE INDEX idx_login_success ON login_attempts(success);

-- ACCOUNT ACTIONS TABLE
-- Track account status changes for admin accountability and account recovery
CREATE TABLE account_actions (
    action_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    performed_by UUID NOT NULL REFERENCES users(user_id) ON DELETE SET NULL,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN (
        'SUSPEND', 'REACTIVATE', 'DELETE', 'ROLE_CHANGE', 
        'PASSWORD_RESET', 'EMAIL_CHANGE', 'VERIFICATION_STATUS_CHANGE'
    )),
    reason VARCHAR(500),
    notes TEXT,
    scheduled_revert_at TIMESTAMP,  -- For temporary suspensions
    reverted_at TIMESTAMP,
    is_reverted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for account_actions
CREATE INDEX idx_account_user ON account_actions(user_id);
CREATE INDEX idx_account_admin ON account_actions(performed_by);
CREATE INDEX idx_account_type ON account_actions(action_type);
CREATE INDEX idx_account_created ON account_actions(created_at DESC);

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

-- Table comments
COMMENT ON TABLE universities IS 'Multi-tenant university/campus support with domain verification';
COMMENT ON TABLE users IS 'User accounts supporting STUDENT and ADMIN roles with verification status';
COMMENT ON TABLE refresh_tokens IS 'JWT refresh token storage with device tracking and revocation support';
COMMENT ON TABLE products IS 'Marketplace product listings with flexible attributes and moderation';
COMMENT ON TABLE orders IS 'Shopping cart and order management with complete lifecycle tracking';
COMMENT ON TABLE order_items IS 'Order line items with snapshot of product data at purchase time';
COMMENT ON TABLE listings IS 'Listing API product listings with images and reports';
COMMENT ON TABLE listing_images IS 'Images associated with listings';
COMMENT ON TABLE reports IS 'User reports against listings for moderation';
COMMENT ON TABLE conversations IS 'Chat conversations between buyers and sellers for specific listings';
COMMENT ON TABLE messages IS 'Individual messages within conversations';
COMMENT ON TABLE verification_tokens IS 'Stores email verification, password reset, and email change tokens';
COMMENT ON TABLE audit_logs IS 'Comprehensive audit logging for all user actions and admin operations';
COMMENT ON TABLE login_attempts IS 'Tracks login attempts for security monitoring and account lockout';
COMMENT ON TABLE account_actions IS 'Tracks account status changes for admin accountability';

-- Key column comments
COMMENT ON COLUMN users.verification_status IS 'Account verification state: PENDING, VERIFIED, or SUSPENDED';
COMMENT ON COLUMN users.preferences IS 'JSON object for user preferences (notifications, search settings, etc.)';
COMMENT ON COLUMN products.attributes IS 'JSON object for category-specific attributes (ISBN, model, etc.)';
COMMENT ON COLUMN products.delivery_methods IS 'JSON array of available delivery methods';
COMMENT ON COLUMN products.moderation_status IS 'Content moderation status: PENDING, APPROVED, or REJECTED';
COMMENT ON COLUMN orders.status IS 'Order lifecycle status from CART to COMPLETED/CANCELLED';
COMMENT ON COLUMN order_items.product_title IS 'Snapshot of product title at time of order for historical accuracy';

-- Schema metadata
COMMENT ON SCHEMA public IS 'Campus Marketplace Schema v2.0 - Aligned with Spring Boot codebase, UUID primary keys, Multi-tenant support';
