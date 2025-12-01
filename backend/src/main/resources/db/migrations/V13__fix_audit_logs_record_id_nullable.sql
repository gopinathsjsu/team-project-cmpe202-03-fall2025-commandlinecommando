-- V13: Fix audit_logs record_id constraint
-- The record_id column was defined as NOT NULL in V1, but authentication events
-- (LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, ACCOUNT_LOCKED) don't have a record_id.
-- The JPA entity already allows null, so we align the database schema.

ALTER TABLE audit_logs ALTER COLUMN record_id DROP NOT NULL;
