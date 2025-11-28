-- =============================================================================
-- Listing API - Listings, Images, and Reports Tables
-- =============================================================================
-- This migration creates tables for marketplace listings and moderation
-- Version: 8.0.0
-- =============================================================================

-- Create listing status enum type
DO $$ BEGIN
    CREATE TYPE listing_status AS ENUM ('PENDING', 'ACTIVE', 'SOLD', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create item condition enum type
DO $$ BEGIN
    CREATE TYPE item_condition AS ENUM ('NEW', 'LIKE_NEW', 'GOOD', 'USED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create category enum type
DO $$ BEGIN
    CREATE TYPE category AS ENUM ('TEXTBOOKS', 'GADGETS', 'ELECTRONICS', 'STATIONARY', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create report type enum type
DO $$ BEGIN
    CREATE TYPE report_type AS ENUM (
        'SPAM',
        'INAPPROPRIATE_CONTENT',
        'FAKE_LISTING',
        'HARASSMENT',
        'COPYRIGHT_VIOLATION',
        'PRICE_MANIPULATION',
        'OTHER'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create report status enum type
DO $$ BEGIN
    CREATE TYPE report_status AS ENUM ('PENDING', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Listings table - Marketplace items posted by sellers
CREATE TABLE IF NOT EXISTS listings (
    listing_id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category category NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    condition item_condition NOT NULL,
    status listing_status NOT NULL DEFAULT 'PENDING',
    location VARCHAR(255),
    view_count INTEGER NOT NULL DEFAULT 0 CHECK (view_count >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Listing images table - Multiple images per listing
CREATE TABLE IF NOT EXISTS listing_images (
    image_id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Reports table - Moderation and flagging system
CREATE TABLE IF NOT EXISTS reports (
    report_id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    report_type report_type NOT NULL,
    description TEXT,
    status report_status NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_listings_seller ON listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_listings_status ON listings(status);
CREATE INDEX IF NOT EXISTS idx_listings_category ON listings(category);
CREATE INDEX IF NOT EXISTS idx_listings_created ON listings(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_listings_price ON listings(price);
CREATE INDEX IF NOT EXISTS idx_listings_location ON listings(location);

CREATE INDEX IF NOT EXISTS idx_listing_images_listing ON listing_images(listing_id);
CREATE INDEX IF NOT EXISTS idx_listing_images_display_order ON listing_images(listing_id, display_order);

CREATE INDEX IF NOT EXISTS idx_reports_listing ON reports(listing_id);
CREATE INDEX IF NOT EXISTS idx_reports_reporter ON reports(reporter_id);
CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);
CREATE INDEX IF NOT EXISTS idx_reports_created ON reports(created_at DESC);

-- Trigger to auto-update updated_at timestamp for listings
DROP TRIGGER IF EXISTS update_listings_updated_at ON listings;

CREATE TRIGGER update_listings_updated_at
    BEFORE UPDATE ON listings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger to auto-update updated_at timestamp for reports
DROP TRIGGER IF EXISTS update_reports_updated_at ON reports;

CREATE TRIGGER update_reports_updated_at
    BEFORE UPDATE ON reports
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE listings IS 'Marketplace listings posted by sellers';
COMMENT ON COLUMN listings.seller_id IS 'Reference to user ID of the seller';
COMMENT ON COLUMN listings.view_count IS 'Number of times this listing has been viewed';

COMMENT ON TABLE listing_images IS 'Images associated with marketplace listings';
COMMENT ON COLUMN listing_images.display_order IS 'Order in which images should be displayed';

COMMENT ON TABLE reports IS 'User reports for inappropriate or problematic listings';
COMMENT ON COLUMN reports.reporter_id IS 'Reference to user ID who filed the report';
COMMENT ON COLUMN reports.reviewed_by IS 'Reference to admin user ID who reviewed the report';
