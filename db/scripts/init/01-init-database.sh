#!/bin/sh
# Database Initialization Script for Campus Marketplace
# This script runs automatically when PostgreSQL container starts for the first time

set -e

# Function to log messages
log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

log "Starting Campus Marketplace database initialization..."

# Set environment variables with defaults
DB_NAME=${POSTGRES_DB:-campus_marketplace}
DB_ROOT_USER=${POSTGRES_USER:-postgres}
DB_APP_USER=${DB_APP_USER:-cm_app_user}
DB_APP_PASSWORD=${DB_APP_PASSWORD:-CampusApp2024!SecurePass}
DB_READONLY_USER=${DB_READONLY_USER:-cm_readonly}
DB_READONLY_PASSWORD=${DB_READONLY_PASSWORD:-CampusRead2024!SecurePass}

# Create application user with full permissions
log "Creating application user: $DB_APP_USER"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create application user
    CREATE USER $DB_APP_USER WITH PASSWORD '$DB_APP_PASSWORD';
    
    -- Grant database creation privileges (needed for Spring Boot)
    ALTER USER $DB_APP_USER CREATEDB;
    
    -- Grant all privileges on the database
    GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_APP_USER;
    
    -- Grant all privileges on the public schema
    GRANT ALL ON SCHEMA public TO $DB_APP_USER;
    
    -- Grant privileges on all existing tables in public schema
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DB_APP_USER;
    
    -- Grant privileges on all existing sequences in public schema
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $DB_APP_USER;
    
    -- Grant privileges on all future tables and sequences in public schema
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO $DB_APP_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO $DB_APP_USER;
EOSQL

# Create read-only user for analytics/reporting
log "Creating read-only user: $DB_READONLY_USER"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create read-only user
    CREATE USER $DB_READONLY_USER WITH PASSWORD '$DB_READONLY_PASSWORD';
    
    -- Grant connect privilege
    GRANT CONNECT ON DATABASE $DB_NAME TO $DB_READONLY_USER;
    
    -- Grant usage on public schema
    GRANT USAGE ON SCHEMA public TO $DB_READONLY_USER;
    
    -- Grant select on all existing tables in public schema
    GRANT SELECT ON ALL TABLES IN SCHEMA public TO $DB_READONLY_USER;
    
    -- Grant select on all future tables in public schema
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO $DB_READONLY_USER;
    
    -- Grant usage on all existing sequences in public schema
    GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO $DB_READONLY_USER;
    
    -- Grant usage on all future sequences in public schema
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE ON SEQUENCES TO $DB_READONLY_USER;
EOSQL

# Create additional schemas if needed
log "Creating additional schemas..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create audit schema for tracking changes
    CREATE SCHEMA IF NOT EXISTS audit;
    GRANT USAGE ON SCHEMA audit TO $DB_APP_USER;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA audit TO $DB_APP_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA audit GRANT ALL PRIVILEGES ON TABLES TO $DB_APP_USER;
    
    -- Create reporting schema for views and aggregated data
    CREATE SCHEMA IF NOT EXISTS reporting;
    GRANT USAGE ON SCHEMA reporting TO $DB_APP_USER;
    GRANT USAGE ON SCHEMA reporting TO $DB_READONLY_USER;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA reporting TO $DB_APP_USER;
    GRANT SELECT ON ALL TABLES IN SCHEMA reporting TO $DB_READONLY_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA reporting GRANT ALL PRIVILEGES ON TABLES TO $DB_APP_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA reporting GRANT SELECT ON TABLES TO $DB_READONLY_USER;
EOSQL

# Install commonly used extensions
log "Installing PostgreSQL extensions..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- UUID extension for generating UUIDs
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    
    -- pg_stat_statements for query performance monitoring
    CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
    
    -- Additional useful extensions
    CREATE EXTENSION IF NOT EXISTS btree_gin;
    CREATE EXTENSION IF NOT EXISTS btree_gist;
EOSQL

log "Database initialization completed successfully!"