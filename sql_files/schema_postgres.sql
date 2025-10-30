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
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

-- Table comments
COMMENT ON TABLE universities IS 'Multi-tenant university/campus support with domain verification';
COMMENT ON TABLE users IS 'User accounts supporting STUDENT and ADMIN roles with verification status';
COMMENT ON TABLE refresh_tokens IS 'JWT refresh token storage with device tracking and revocation support';
COMMENT ON TABLE products IS 'Marketplace product listings with flexible attributes and moderation';
COMMENT ON TABLE orders IS 'Shopping cart and order management with complete lifecycle tracking';
COMMENT ON TABLE order_items IS 'Order line items with snapshot of product data at purchase time';

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
