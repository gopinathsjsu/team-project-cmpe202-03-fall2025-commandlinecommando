-- schema_postgres.sql
-- Campus Marketplace PostgreSQL Database Schema
-- Production-ready schema with comprehensive constraints, indexes, and seed data
-- Compatible with Spring Boot 3.5.6 and Hibernate 6.6.29
-- Assumes PostgreSQL 14+

-- =====================================================
-- SCHEMA SETUP AND SEQUENCES
-- =====================================================

-- Drop existing tables in correct order (foreign key dependencies)
DROP TABLE IF EXISTS admin_permissions CASCADE;
DROP TABLE IF EXISTS listing_images CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS conversations CASCADE;
DROP TABLE IF EXISTS reports CASCADE;
DROP TABLE IF EXISTS chatbot_queries CASCADE;
DROP TABLE IF EXISTS listings CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop sequences if they exist
DROP SEQUENCE IF EXISTS user_sequence CASCADE;

-- Create sequences for ID generation
CREATE SEQUENCE user_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- =====================================================
-- CORE USER MANAGEMENT TABLES
-- =====================================================

-- Users table (Single Table Inheritance for User/Student/Admin)
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY DEFAULT nextval('user_sequence'),
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('STUDENT', 'ADMIN')),
    
    -- Core user fields
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- BCrypt hash
    
    -- Profile information
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    
    -- Status and timestamps
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- User role (enum stored as string)
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'ADMIN')),
    
    -- Student-specific fields (only populated when user_type = 'STUDENT')
    student_id VARCHAR(20) UNIQUE, -- University student ID
    major VARCHAR(100),
    graduation_year INTEGER CHECK (graduation_year >= 2020 AND graduation_year <= 2030),
    campus_location VARCHAR(100),
    
    -- Admin-specific fields (only populated when user_type = 'ADMIN')
    admin_level VARCHAR(20) CHECK (admin_level IN ('SUPER_ADMIN', 'ADMIN', 'MODERATOR')),
    
    -- Constraints
    CONSTRAINT chk_student_fields CHECK (
        (user_type = 'STUDENT' AND student_id IS NOT NULL) OR 
        (user_type = 'ADMIN' AND student_id IS NULL)
    ),
    CONSTRAINT chk_admin_fields CHECK (
        (user_type = 'ADMIN' AND admin_level IS NOT NULL) OR 
        (user_type = 'STUDENT' AND admin_level IS NULL)
    ),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_password_length CHECK (length(password_hash) >= 8)
);

-- Admin permissions table (ElementCollection mapping)
CREATE TABLE admin_permissions (
    admin_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    permission VARCHAR(50) NOT NULL CHECK (permission IN (
        'CREATE_USER', 'READ_USER', 'UPDATE_USER', 'DELETE_USER',
        'CREATE_LISTING', 'READ_LISTING', 'UPDATE_LISTING', 'DELETE_LISTING', 'MODERATE_LISTING',
        'VIEW_REPORTS', 'RESOLVE_REPORTS',
        'SYSTEM_CONFIG', 'VIEW_ANALYTICS', 'MANAGE_CATEGORIES'
    )),
    PRIMARY KEY (admin_id, permission)
);

-- Refresh tokens for JWT authentication
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE, -- JWT refresh token
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT false,
    device_info VARCHAR(255), -- Optional device information
    
    CONSTRAINT chk_expires_future CHECK (expires_at > created_at)
);

-- =====================================================
-- MARKETPLACE CORE TABLES
-- =====================================================

-- Listings table for marketplace items
CREATE TABLE listings (
    listing_id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Listing details
    title VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    
    -- Categorization and status
    category VARCHAR(20) NOT NULL CHECK (category IN ('TEXTBOOKS', 'GADGETS', 'ELECTRONICS', 'STATIONARY', 'OTHER')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'SOLD', 'CANCELLED')),
    condition VARCHAR(20) CHECK (condition IN ('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR')),
    
    -- Additional metadata
    location VARCHAR(100),
    view_count INTEGER NOT NULL DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Status tracking
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Listing images table
CREATE TABLE listing_images (
    image_id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    
    -- Image details
    image_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER NOT NULL DEFAULT 1,
    
    -- Metadata
    file_size BIGINT, -- Size in bytes
    mime_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_display_order CHECK (display_order >= 1),
    CONSTRAINT chk_file_size CHECK (file_size IS NULL OR file_size > 0)
);

-- =====================================================
-- COMMUNICATION SYSTEM TABLES
-- =====================================================

-- Conversations between buyers and sellers
CREATE TABLE conversations (
    conversation_id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    buyer_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    seller_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Conversation status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED', 'BLOCKED')),
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_different_users CHECK (buyer_id != seller_id),
    UNIQUE(listing_id, buyer_id) -- One conversation per buyer per listing
);

-- Messages within conversations
CREATE TABLE messages (
    message_id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    
    -- Message content
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT' CHECK (message_type IN ('TEXT', 'IMAGE', 'SYSTEM')),
    
    -- Message status
    is_read BOOLEAN NOT NULL DEFAULT false,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_content_not_empty CHECK (length(trim(content)) > 0)
);

-- =====================================================
-- MODERATION AND REPORTING SYSTEM
-- =====================================================

-- Reports for content moderation
CREATE TABLE reports (
    report_id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    listing_id BIGINT REFERENCES listings(listing_id) ON DELETE CASCADE,
    
    -- Report details
    report_type VARCHAR(30) NOT NULL CHECK (report_type IN ('SPAM', 'INAPPROPRIATE', 'FRAUD', 'DUPLICATE', 'OTHER')),
    description TEXT NOT NULL,
    
    -- Report status and resolution
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT REFERENCES users(user_id), -- Admin who reviewed
    resolution_notes TEXT,
    
    CONSTRAINT chk_description_not_empty CHECK (length(trim(description)) > 0),
    CONSTRAINT chk_reviewed_fields CHECK (
        (status IN ('RESOLVED', 'DISMISSED') AND reviewed_at IS NOT NULL AND reviewed_by IS NOT NULL) OR
        (status IN ('PENDING', 'UNDER_REVIEW') AND reviewed_at IS NULL)
    )
);

-- =====================================================
-- CHATBOT AND AI FEATURES
-- =====================================================

-- Chatbot queries for AI assistance
CREATE TABLE chatbot_queries (
    query_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL, -- Allow anonymous queries
    
    -- Query details
    query TEXT NOT NULL,
    response TEXT,
    
    -- Processing metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    processing_time_ms INTEGER, -- Response time in milliseconds
    
    CONSTRAINT chk_query_not_empty CHECK (length(trim(query)) > 0),
    CONSTRAINT chk_processing_time CHECK (processing_time_ms IS NULL OR processing_time_ms >= 0)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- =====================================================

-- User table indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_student_id ON users(student_id) WHERE student_id IS NOT NULL;
CREATE INDEX idx_users_created_at ON users(created_at);

-- Refresh token indexes
CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_is_revoked ON refresh_tokens(is_revoked);

-- Listing indexes
CREATE INDEX idx_listings_seller ON listings(seller_id);
CREATE INDEX idx_listings_category ON listings(category);
CREATE INDEX idx_listings_status ON listings(status);
CREATE INDEX idx_listings_active ON listings(is_active);
CREATE INDEX idx_listings_created_at ON listings(created_at);
CREATE INDEX idx_listings_price ON listings(price);
CREATE INDEX idx_listings_location ON listings(location);

-- Listing image indexes
CREATE INDEX idx_listing_images_listing ON listing_images(listing_id);
CREATE INDEX idx_listing_images_order ON listing_images(listing_id, display_order);

-- Conversation indexes
CREATE INDEX idx_conversations_listing ON conversations(listing_id);
CREATE INDEX idx_conversations_buyer ON conversations(buyer_id);
CREATE INDEX idx_conversations_seller ON conversations(seller_id);
CREATE INDEX idx_conversations_last_message ON conversations(last_message_at);

-- Message indexes
CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_sent_at ON messages(sent_at);
CREATE INDEX idx_messages_unread ON messages(is_read) WHERE is_read = false;

-- Report indexes
CREATE INDEX idx_reports_reporter ON reports(reporter_id);
CREATE INDEX idx_reports_listing ON reports(listing_id);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_created_at ON reports(created_at);
CREATE INDEX idx_reports_reviewer ON reports(reviewed_by);

-- Chatbot indexes
CREATE INDEX idx_chatbot_user ON chatbot_queries(user_id);
CREATE INDEX idx_chatbot_created_at ON chatbot_queries(created_at);

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
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_listings_updated_at 
    BEFORE UPDATE ON listings 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

-- Table comments
COMMENT ON TABLE users IS 'Single table inheritance for User/Student/Admin with comprehensive profile management';
COMMENT ON TABLE admin_permissions IS 'ElementCollection mapping for admin-specific permissions';
COMMENT ON TABLE refresh_tokens IS 'JWT refresh token storage with device tracking and revocation support';
COMMENT ON TABLE listings IS 'Marketplace listings with full metadata and status tracking';
COMMENT ON TABLE listing_images IS 'Image attachments for listings with ordering and metadata';
COMMENT ON TABLE conversations IS 'Private conversations between buyers and sellers';
COMMENT ON TABLE messages IS 'Individual messages within conversations with read status';
COMMENT ON TABLE reports IS 'Content moderation reports with admin workflow support';
COMMENT ON TABLE chatbot_queries IS 'AI chatbot interaction history with performance metrics';

-- Column comments for complex fields
COMMENT ON COLUMN users.user_type IS 'Discriminator column for single table inheritance (STUDENT/ADMIN)';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password with salt';
COMMENT ON COLUMN users.student_id IS 'University-issued student identifier (required for STUDENT type)';
COMMENT ON COLUMN refresh_tokens.token IS 'JWT refresh token string (up to 500 chars for future expansion)';
COMMENT ON COLUMN listings.condition IS 'Physical condition of the item being sold';
COMMENT ON COLUMN listing_images.display_order IS 'Order for displaying images (1 = primary image)';
COMMENT ON COLUMN reports.report_type IS 'Category of report for content moderation workflow';
COMMENT ON COLUMN chatbot_queries.processing_time_ms IS 'Response time in milliseconds for performance monitoring';

-- Schema assumptions and trade-offs
COMMENT ON SCHEMA public IS 'Campus Marketplace Schema v1.0 - Assumptions: PostgreSQL 14+, Single table inheritance for users, JWT-based auth, Comprehensive indexing for performance';
