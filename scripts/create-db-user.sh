#!/bin/bash
# Quick script to create the cm_app_user role in PostgreSQL
# Run this if you're using a local PostgreSQL instance (not Docker)

set -e

echo "Creating PostgreSQL user 'cm_app_user'..."

# Default values - adjust if needed
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-campus_marketplace}
ADMIN_USER=${ADMIN_USER:-postgres}
APP_USER=${DB_APP_USER:-cm_app_user}
APP_PASSWORD=${DB_APP_PASSWORD:-changeme}

echo "Connecting to PostgreSQL at $DB_HOST:$DB_PORT as $ADMIN_USER..."
echo "Creating user: $APP_USER"

psql -h "$DB_HOST" -p "$DB_PORT" -U "$ADMIN_USER" -d postgres <<EOF
-- Create the application user if it doesn't exist
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '$APP_USER') THEN
        CREATE ROLE $APP_USER WITH
            LOGIN
            PASSWORD '$APP_PASSWORD'
            NOSUPERUSER
            CREATEDB
            NOCREATEROLE
            INHERIT
            NOREPLICATION
            CONNECTION LIMIT -1;
        RAISE NOTICE 'User $APP_USER created successfully';
    ELSE
        RAISE NOTICE 'User $APP_USER already exists';
    END IF;
END
\$\$;

-- Grant privileges on the database
GRANT CONNECT ON DATABASE $DB_NAME TO $APP_USER;
GRANT USAGE ON SCHEMA public TO $APP_USER;
GRANT CREATE ON SCHEMA public TO $APP_USER;

-- Grant privileges on all existing tables
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $APP_USER;

-- Grant privileges on all existing sequences
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $APP_USER;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO $APP_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO $APP_USER;

EOF

echo ""
echo "✅ User '$APP_USER' created successfully!"
echo ""
echo "You can now start your Spring Boot application."
echo "Make sure the database '$DB_NAME' exists. If not, create it with:"
echo "  createdb -h $DB_HOST -p $DB_PORT -U $ADMIN_USER $DB_NAME"

