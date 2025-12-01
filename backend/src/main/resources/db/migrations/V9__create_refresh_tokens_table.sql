-- =============================================================================
-- Campus Marketplace Database Schema - Refresh Tokens Table
-- Version: 9.0.0
-- Date: 2025-11-26
-- Description: Creates refresh_tokens table for JWT refresh token management
-- =============================================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    device_info VARCHAR(500),
    is_revoked BOOLEAN DEFAULT FALSE NOT NULL
);

-- Indexes for refresh_tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires ON refresh_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked ON refresh_tokens(is_revoked);

-- Comment
COMMENT ON TABLE refresh_tokens IS 'Stores JWT refresh tokens for secure token rotation';

-- =============================================================================
-- END OF MIGRATION
-- =============================================================================
