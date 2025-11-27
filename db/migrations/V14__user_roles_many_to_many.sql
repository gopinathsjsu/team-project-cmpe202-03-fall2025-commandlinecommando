-- =============================================================================
-- Campus Marketplace - User Roles Many-to-Many Migration
-- CMPE 202 Project: Flexible Role-Based Access Control
-- Version: 14
-- 
-- This migration converts the single-role-per-user design to a many-to-many
-- relationship, allowing users (students) to have multiple roles (BUYER, SELLER)
-- while keeping ADMIN role exclusive.
-- =============================================================================

-- =============================================================================
-- STEP 1: Create the user_roles junction table
-- =============================================================================

-- Create the junction table for many-to-many user-role relationship
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role user_role NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role)
);

-- Create index for efficient role-based queries
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role);

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
WHERE role = 'BUYER'
ON CONFLICT (user_id, role) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT user_id, 'SELLER'::user_role
FROM users
WHERE role = 'BUYER'
ON CONFLICT (user_id, role) DO NOTHING;

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
WHERE role = 'ADMIN'
ON CONFLICT (user_id, role) DO NOTHING;

-- =============================================================================
-- STEP 3: Drop the old role column from users table
-- =============================================================================

-- First, drop the check constraint that references the role column
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_student_id;

-- Drop the index on the role column
DROP INDEX IF EXISTS idx_users_role;

-- Drop the role column from users table
ALTER TABLE users DROP COLUMN IF EXISTS role;

-- =============================================================================
-- STEP 4: Update views that reference the old role column
-- =============================================================================

-- Drop existing views that use the role column (if they exist)
DROP VIEW IF EXISTS vw_seller_dashboard;
DROP VIEW IF EXISTS vw_admin_analytics;
DROP VIEW IF EXISTS vw_users_with_roles;

-- Create view to easily get all users with their roles as an array
CREATE VIEW vw_users_with_roles AS
SELECT 
    u.*,
    ARRAY_AGG(ur.role ORDER BY ur.role) AS roles
FROM users u
LEFT JOIN user_roles ur ON u.user_id = ur.user_id
GROUP BY u.user_id;

-- =============================================================================
-- END OF MIGRATION
-- =============================================================================
