-- =============================================================================
-- Campus Marketplace - Fix Audit Logs record_id to be nullable
-- Version: 10.0.0
-- 
-- This migration fixes the audit_logs table to allow NULL values for record_id
-- because authentication events (login, logout, failed login) don't have an
-- associated database record to reference.
-- =============================================================================

-- Make record_id nullable for auth events that don't have a record to reference
ALTER TABLE audit_logs ALTER COLUMN record_id DROP NOT NULL;

-- Add comment explaining the change
COMMENT ON COLUMN audit_logs.record_id IS 'UUID of the affected record. NULL for auth events (login/logout) that do not reference a specific record.';

-- Update schema version
INSERT INTO schema_version (version, description) VALUES
('10.0.0', 'Fix audit_logs record_id to allow NULL for auth events');

-- =============================================================================
-- END OF MIGRATION
-- =============================================================================
