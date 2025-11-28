-- =============================================================================
-- Campus Marketplace Database Schema - User Management System
-- Version: 4.0.0
-- Date: 2025-10-11
-- Description: Adds comprehensive user management tables for authentication,
--              audit logging, security tracking, and account management
-- =============================================================================

-- =============================================================================
-- VERIFICATION TOKENS TABLE
-- =============================================================================
-- Stores email verification tokens, password reset tokens, and email change tokens

CREATE TABLE IF NOT EXISTS verification_tokens (
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
CREATE INDEX IF NOT EXISTS idx_verification_token ON verification_tokens(token);
CREATE INDEX IF NOT EXISTS idx_verification_user ON verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_verification_expires ON verification_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_verification_type ON verification_tokens(token_type);

-- =============================================================================
-- AUDIT LOGS TABLE
-- =============================================================================
-- Comprehensive audit logging for all user actions and admin operations

CREATE TABLE IF NOT EXISTS audit_logs (
    audit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
    username VARCHAR(50),  -- Store username for reference even if user is deleted
    table_name VARCHAR(100) NOT NULL,
    record_id UUID,
    action VARCHAR(50) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    description VARCHAR(500),
    ip_address VARCHAR(45),  -- Support IPv6
    user_agent VARCHAR(500),
    severity VARCHAR(20) DEFAULT 'INFO' CHECK (severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for audit_logs
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_username ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_table ON audit_logs(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_record ON audit_logs(record_id);
CREATE INDEX IF NOT EXISTS idx_audit_severity ON audit_logs(severity);

-- =============================================================================
-- LOGIN ATTEMPTS TABLE
-- =============================================================================
-- Track login attempts for security monitoring and account lockout

CREATE TABLE IF NOT EXISTS login_attempts (
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
CREATE INDEX IF NOT EXISTS idx_login_username ON login_attempts(username);
CREATE INDEX IF NOT EXISTS idx_login_ip ON login_attempts(ip_address);
CREATE INDEX IF NOT EXISTS idx_login_created ON login_attempts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_login_success ON login_attempts(success);

-- =============================================================================
-- ACCOUNT ACTIONS TABLE
-- =============================================================================
-- Track account status changes for admin accountability and account recovery

CREATE TABLE IF NOT EXISTS account_actions (
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
CREATE INDEX IF NOT EXISTS idx_account_user ON account_actions(user_id);
CREATE INDEX IF NOT EXISTS idx_account_admin ON account_actions(performed_by);
CREATE INDEX IF NOT EXISTS idx_account_type ON account_actions(action_type);
CREATE INDEX IF NOT EXISTS idx_account_created ON account_actions(created_at DESC);

-- =============================================================================
-- UPDATE EXISTING TABLES (IF NEEDED)
-- =============================================================================

-- Add any missing columns to users table (if not already present)
DO $$ 
BEGIN
    -- Check and add last_login_at if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'last_login_at'
    ) THEN
        ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;
        CREATE INDEX IF NOT EXISTS idx_users_last_login ON users(last_login_at);
    END IF;
    
    -- Check and add email_verified_at if not exists
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'email_verified_at'
    ) THEN
        ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMP;
    END IF;
END $$;

-- =============================================================================
-- FUNCTIONS FOR CLEANUP (SCHEDULED TASKS)
-- =============================================================================

-- Function to clean up expired verification tokens
CREATE OR REPLACE FUNCTION cleanup_expired_tokens()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM verification_tokens 
    WHERE expires_at < CURRENT_TIMESTAMP;
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Function to clean up old login attempts (older than 30 days)
CREATE OR REPLACE FUNCTION cleanup_old_login_attempts()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM login_attempts 
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '30 days';
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Function to clean up old audit logs (older than 1 year)
CREATE OR REPLACE FUNCTION cleanup_old_audit_logs()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM audit_logs 
    WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '1 year'
    AND severity = 'INFO';  -- Keep WARNING, ERROR, CRITICAL longer
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- COMMENTS FOR DOCUMENTATION
-- =============================================================================

COMMENT ON TABLE verification_tokens IS 'Stores email verification, password reset, and email change tokens';
COMMENT ON TABLE audit_logs IS 'Comprehensive audit logging for all user actions and admin operations';
COMMENT ON TABLE login_attempts IS 'Tracks login attempts for security monitoring and account lockout';
COMMENT ON TABLE account_actions IS 'Tracks account status changes for admin accountability';

COMMENT ON COLUMN verification_tokens.token_type IS 'Type: EMAIL_VERIFICATION, PASSWORD_RESET, EMAIL_CHANGE';
COMMENT ON COLUMN audit_logs.severity IS 'Severity: INFO, WARNING, ERROR, CRITICAL';
COMMENT ON COLUMN account_actions.action_type IS 'Type: SUSPEND, REACTIVATE, DELETE, ROLE_CHANGE, etc.';

-- =============================================================================
-- GRANT PERMISSIONS (IF APPLICATION USER EXISTS)
-- =============================================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'cm_app_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON verification_tokens TO cm_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON audit_logs TO cm_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON login_attempts TO cm_app_user;
        GRANT SELECT, INSERT, UPDATE, DELETE ON account_actions TO cm_app_user;
        GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO cm_app_user;
    END IF;
END $$;

-- =============================================================================
-- VERSION TRACKING
-- =============================================================================

INSERT INTO schema_version (version, description) VALUES
('4.0.0', 'User Management System - Audit logging, security tracking, account management');

-- =============================================================================
-- END OF MIGRATION
-- =============================================================================

