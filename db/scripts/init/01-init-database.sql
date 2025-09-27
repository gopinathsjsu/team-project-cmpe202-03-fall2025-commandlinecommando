-- Database initialization script for Campus Marketplace
-- This script creates the necessary users and database with proper permissions

-- Create the application database
CREATE DATABASE campusmarketplace_db;

-- Create application user with limited permissions
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'cm_app_user') THEN
        CREATE ROLE cm_app_user WITH
            LOGIN
            PASSWORD 'campusapp2024'
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            INHERIT
            NOREPLICATION
            CONNECTION LIMIT -1;
    END IF;
END
$$;

-- Create read-only user for reporting and analytics
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'cm_readonly') THEN
        CREATE ROLE cm_readonly WITH
            LOGIN
            PASSWORD 'readonly2024'
            NOSUPERUSER
            NOCREATEDB
            NOCREATEROLE
            INHERIT
            NOREPLICATION
            CONNECTION LIMIT -1;
    END IF;
END
$$;

-- Connect to the campusmarketplace_db database to set permissions
\c campusmarketplace_db

-- Grant privileges to application user
GRANT CONNECT ON DATABASE campusmarketplace_db TO cm_app_user;
GRANT USAGE ON SCHEMA public TO cm_app_user;
GRANT CREATE ON SCHEMA public TO cm_app_user;

-- Grant all privileges on existing tables to cm_app_user
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
        EXECUTE 'GRANT ALL PRIVILEGES ON TABLE public.' || quote_ident(r.tablename) || ' TO cm_app_user';
    END LOOP;
END
$$;

-- Grant all privileges on existing sequences to cm_app_user
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'public') LOOP
        EXECUTE 'GRANT ALL PRIVILEGES ON SEQUENCE public.' || quote_ident(r.sequence_name) || ' TO cm_app_user';
    END LOOP;
END
$$;

-- Set default privileges for future tables and sequences
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO cm_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO cm_app_user;

-- Grant read-only privileges to readonly user
GRANT CONNECT ON DATABASE campusmarketplace_db TO cm_readonly;
GRANT USAGE ON SCHEMA public TO cm_readonly;

-- Grant select on existing tables to readonly user
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
        EXECUTE 'GRANT SELECT ON TABLE public.' || quote_ident(r.tablename) || ' TO cm_readonly';
    END LOOP;
END
$$;

-- Set default privileges for future tables for readonly user
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cm_readonly;

-- Create a function to display user information (for verification)
CREATE OR REPLACE FUNCTION get_database_users()
RETURNS TABLE(
    username TEXT,
    can_login BOOLEAN,
    is_superuser BOOLEAN,
    connection_limit INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        rolname::TEXT,
        rolcanlogin,
        rolsuper,
        rolconnlimit
    FROM pg_roles 
    WHERE rolname IN ('cm_app_user', 'cm_readonly', 'postgres');
END;
$$ LANGUAGE plpgsql;

-- Output user information
SELECT 'Database users created successfully:' as message;
SELECT * FROM get_database_users();

-- Output completion message
SELECT 'Campus Marketplace database initialization completed successfully!' as message;