-- =============================================================================
-- Communication Service - Notification Preferences Table
-- =============================================================================
-- This migration creates a table for storing user email notification preferences
-- Version: 7.0.0
-- =============================================================================

-- Notification preferences table - Stores user preferences for email notifications
CREATE TABLE IF NOT EXISTS notification_preferences (
    preference_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email VARCHAR(255),
    first_name VARCHAR(255),
    email_notifications_enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for quick lookup by user ID
CREATE INDEX IF NOT EXISTS idx_notification_preferences_user ON notification_preferences(user_id);

-- Trigger to auto-update updated_at timestamp
DROP TRIGGER IF EXISTS update_notification_preferences_updated_at ON notification_preferences;

CREATE TRIGGER update_notification_preferences_updated_at
    BEFORE UPDATE ON notification_preferences
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Comments
COMMENT ON TABLE notification_preferences IS 'Stores user preferences for email notifications';
COMMENT ON COLUMN notification_preferences.user_id IS 'Reference to user ID (from backend service)';
COMMENT ON COLUMN notification_preferences.email_notifications_enabled IS 'Whether the user wants to receive email notifications for new messages';

