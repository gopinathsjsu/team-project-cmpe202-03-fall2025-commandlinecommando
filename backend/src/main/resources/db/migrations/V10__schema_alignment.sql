-- Schema alignment patch to unblock API features required for automated tests

-- Create notification preferences table used by chat/email workflows
CREATE TABLE IF NOT EXISTS notification_preferences (
    preference_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    email VARCHAR(255),
    first_name VARCHAR(255),
    email_notifications_enabled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Ensure trigger-friendly timestamps stay in sync
CREATE OR REPLACE FUNCTION notification_preferences_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_notification_preferences_updated ON notification_preferences;
CREATE TRIGGER tr_notification_preferences_updated
BEFORE UPDATE ON notification_preferences
FOR EACH ROW EXECUTE FUNCTION notification_preferences_set_updated_at();

-- Align user_reports table with latest entity fields
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_reports' AND column_name = 'reported_type'
    ) THEN
        EXECUTE 'ALTER TABLE user_reports RENAME COLUMN reported_type TO report_type';
    END IF;
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'user_reports' AND column_name = 'reported_id'
    ) THEN
        EXECUTE 'ALTER TABLE user_reports RENAME COLUMN reported_id TO reported_entity_id';
    END IF;
END $$;

ALTER TABLE user_reports
    ADD COLUMN IF NOT EXISTS reported_product_id UUID,
    ADD COLUMN IF NOT EXISTS reported_user_id UUID,
    ADD COLUMN IF NOT EXISTS reported_review_id UUID,
    ADD COLUMN IF NOT EXISTS reviewed_by_id UUID,
    ADD COLUMN IF NOT EXISTS resolution_notes TEXT,
    ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'MEDIUM';

ALTER TABLE user_reports
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- Set updated_at on existing rows for consistency
UPDATE user_reports SET updated_at = COALESCE(updated_at, created_at);

-- Add foreign keys where possible
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_reports_reported_product'
    ) THEN
        ALTER TABLE user_reports
            ADD CONSTRAINT fk_user_reports_reported_product
            FOREIGN KEY (reported_product_id) REFERENCES listings(listing_id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_reports_reported_user'
    ) THEN
        ALTER TABLE user_reports
            ADD CONSTRAINT fk_user_reports_reported_user
            FOREIGN KEY (reported_user_id) REFERENCES users(user_id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_reports_reported_review'
    ) THEN
        ALTER TABLE user_reports
            ADD CONSTRAINT fk_user_reports_reported_review
            FOREIGN KEY (reported_review_id) REFERENCES product_reviews(review_id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_user_reports_reviewed_by'
    ) THEN
        ALTER TABLE user_reports
            ADD CONSTRAINT fk_user_reports_reviewed_by
            FOREIGN KEY (reviewed_by_id) REFERENCES users(user_id) ON DELETE SET NULL;
    END IF;
END $$;

-- Helpful indexes for new columns
CREATE INDEX IF NOT EXISTS idx_user_reports_reported_product ON user_reports (reported_product_id);
CREATE INDEX IF NOT EXISTS idx_user_reports_reported_user ON user_reports (reported_user_id);
CREATE INDEX IF NOT EXISTS idx_user_reports_priority ON user_reports (priority);
