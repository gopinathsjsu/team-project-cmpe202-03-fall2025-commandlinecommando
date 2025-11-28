-- =============================================================================
-- Campus Marketplace - User Roles Many-to-Many Migration
-- CMPE 202 Project: Flexible Role-Based Access Control
-- Version: 9.0.0
-- 
-- This migration converts the single-role-per-user design to a many-to-many
-- relationship, allowing users (students) to have multiple roles (BUYER, SELLER)
-- while keeping ADMIN role exclusive.
-- =============================================================================

-- =============================================================================
-- STEP 1: Create the user_roles junction table
-- =============================================================================

-- Create the junction table for many-to-many user-role relationship
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role user_role NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role)
);

-- Create index for efficient role-based queries
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);

-- =============================================================================
-- STEP 2: Migrate existing role data to the junction table
-- =============================================================================

-- Migrate existing users with their current roles
-- BUYER users get BUYER + SELLER roles (since they're students who can do both)
-- SELLER users get BUYER + SELLER roles (since they're students who can do both)
-- ADMIN users get only ADMIN role (exclusive)

-- Insert existing BUYER users with both BUYER and SELLER roles
INSERT INTO user_roles (user_id, role)
SELECT user_id, 'BUYER'::user_role
FROM users
WHERE role = 'BUYER';

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'SELLER'::user_role
FROM users
WHERE role = 'BUYER';

-- Insert existing SELLER users with both BUYER and SELLER roles
INSERT INTO user_roles (user_id, role)
SELECT user_id, 'BUYER'::user_role
FROM users
WHERE role = 'SELLER'
ON CONFLICT (user_id, role) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'SELLER'::user_role
FROM users
WHERE role = 'SELLER'
ON CONFLICT (user_id, role) DO NOTHING;

-- Insert ADMIN users with only ADMIN role
INSERT INTO user_roles (user_id, role)
SELECT user_id, 'ADMIN'::user_role
FROM users
WHERE role = 'ADMIN';

-- =============================================================================
-- STEP 3: Drop the old role column from users table
-- =============================================================================

-- First, drop the check constraint that references the role column
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_student_id;

-- Drop the index on the role column
DROP INDEX IF EXISTS idx_users_role;

-- Drop the role column from users table
ALTER TABLE users DROP COLUMN role;

-- =============================================================================
-- STEP 4: Add new constraint for student_id validation
-- =============================================================================

-- Create a function to check if user is a student (has BUYER or SELLER role)
CREATE OR REPLACE FUNCTION is_student_user(p_user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM user_roles 
        WHERE user_id = p_user_id 
        AND role IN ('BUYER', 'SELLER')
    );
END;
$$ LANGUAGE plpgsql;

-- Create a function to check if user is admin only
CREATE OR REPLACE FUNCTION is_admin_only_user(p_user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM user_roles 
        WHERE user_id = p_user_id 
        AND role = 'ADMIN'
    ) AND NOT EXISTS (
        SELECT 1 FROM user_roles 
        WHERE user_id = p_user_id 
        AND role IN ('BUYER', 'SELLER')
    );
END;
$$ LANGUAGE plpgsql;

-- Add trigger to validate student_id requirement for non-admin users
CREATE OR REPLACE FUNCTION validate_student_id()
RETURNS TRIGGER AS $$
BEGIN
    -- If user has BUYER or SELLER role, they must have a student_id
    IF EXISTS (
        SELECT 1 FROM user_roles 
        WHERE user_id = NEW.user_id 
        AND role IN ('BUYER', 'SELLER')
    ) AND NEW.student_id IS NULL THEN
        RAISE EXCEPTION 'Student users (BUYER/SELLER) must have a student_id';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Note: We'll validate via application logic instead of trigger to avoid
-- complexity with the order of operations (user insert before roles)

-- =============================================================================
-- STEP 5: Update views that reference the old role column
-- =============================================================================

-- Drop existing views that use the role column
DROP VIEW IF EXISTS vw_seller_dashboard;
DROP VIEW IF EXISTS vw_admin_analytics;

-- Recreate seller dashboard view with new role structure
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
INNER JOIN user_roles ur ON u.user_id = ur.user_id AND ur.role = 'SELLER'
LEFT JOIN products p ON u.user_id = p.seller_id
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN product_reviews pr ON p.product_id = pr.product_id
GROUP BY u.user_id;

-- Recreate admin analytics view with new role structure
CREATE VIEW vw_admin_analytics AS
SELECT 
    u.university_id,
    COUNT(DISTINCT u.user_id) AS total_users,
    COUNT(DISTINCT CASE WHEN ur_buyer.user_id IS NOT NULL THEN u.user_id END) AS total_buyers,
    COUNT(DISTINCT CASE WHEN ur_seller.user_id IS NOT NULL THEN u.user_id END) AS total_sellers,
    COUNT(DISTINCT p.product_id) AS total_products,
    COUNT(DISTINCT CASE WHEN p.is_active THEN p.product_id END) AS active_products,
    COUNT(DISTINCT o.order_id) AS total_orders,
    COALESCE(SUM(o.total_amount), 0) AS total_revenue
FROM universities uni
LEFT JOIN users u ON uni.university_id = u.university_id
LEFT JOIN user_roles ur_buyer ON u.user_id = ur_buyer.user_id AND ur_buyer.role = 'BUYER'
LEFT JOIN user_roles ur_seller ON u.user_id = ur_seller.user_id AND ur_seller.role = 'SELLER'
LEFT JOIN products p ON uni.university_id = p.university_id
LEFT JOIN orders o ON uni.university_id = o.university_id AND o.status != 'CART'
GROUP BY u.university_id;

-- =============================================================================
-- STEP 6: Create helper views for role queries
-- =============================================================================

-- View to easily get all users with their roles as an array
CREATE VIEW vw_users_with_roles AS
SELECT 
    u.*,
    ARRAY_AGG(ur.role ORDER BY ur.role) AS roles
FROM users u
LEFT JOIN user_roles ur ON u.user_id = ur.user_id
GROUP BY u.user_id;

-- =============================================================================
-- STEP 7: Grant permissions
-- =============================================================================

-- Grant permissions to application user (assuming cm_app_user exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'cm_app_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON user_roles TO cm_app_user;
    END IF;
END $$;

-- =============================================================================
-- STEP 8: Schema version update
-- =============================================================================

INSERT INTO schema_version (version, description) VALUES
('9.0.0', 'User Roles Many-to-Many Migration - Flexible role assignment for students');

-- =============================================================================
-- END OF MIGRATION
-- =============================================================================
