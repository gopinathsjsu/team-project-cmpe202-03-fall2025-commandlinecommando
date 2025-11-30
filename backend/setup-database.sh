#!/bin/bash
# =============================================================================
# Campus Marketplace - Database Setup Script
# =============================================================================
# This script sets up the PostgreSQL database from scratch:
#   1. Creates the database user (cm_app_user)
#   2. Creates the database (campus_marketplace)
#   3. Grants necessary permissions
#   4. Installs required extensions
#
# Usage:
#   ./setup-database.sh           # Setup with default password
#   ./setup-database.sh mypass    # Setup with custom password
#
# Prerequisites:
#   - PostgreSQL installed and running
#   - Current user has superuser access to PostgreSQL
# =============================================================================

set -e  # Exit on any error

# Configuration
DB_NAME="campus_marketplace"
DB_USER="cm_app_user"
DB_PASSWORD="${1:-changeme}"  # Use argument or default

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  Campus Marketplace Database Setup${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# Check if psql is available
if ! command -v psql &> /dev/null; then
    echo -e "${RED}ERROR: psql command not found. Please install PostgreSQL.${NC}"
    exit 1
fi

# Check PostgreSQL connection
echo -e "${YELLOW}Checking PostgreSQL connection...${NC}"
if ! psql -d postgres -c "SELECT 1" &> /dev/null; then
    echo -e "${RED}ERROR: Cannot connect to PostgreSQL.${NC}"
    echo "Make sure PostgreSQL is running and you have access."
    exit 1
fi
echo -e "${GREEN}✓ PostgreSQL connection successful${NC}"
echo ""

# Drop existing database and user (if they exist)
echo -e "${YELLOW}Cleaning up existing database (if any)...${NC}"
psql -d postgres << EOF
-- Terminate existing connections
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = '${DB_NAME}' AND pid <> pg_backend_pid();

-- Drop database if exists
DROP DATABASE IF EXISTS ${DB_NAME};

-- Drop user if exists
DROP USER IF EXISTS ${DB_USER};
EOF
echo -e "${GREEN}✓ Cleanup complete${NC}"
echo ""

# Create user and database
echo -e "${YELLOW}Creating database user and database...${NC}"
psql -d postgres << EOF
-- Create application user
CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASSWORD}';

-- Create database owned by app user
CREATE DATABASE ${DB_NAME} OWNER ${DB_USER};

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};
EOF
echo -e "${GREEN}✓ User '${DB_USER}' created${NC}"
echo -e "${GREEN}✓ Database '${DB_NAME}' created${NC}"
echo ""

# Setup extensions and schema permissions
echo -e "${YELLOW}Setting up extensions and permissions...${NC}"
psql -d ${DB_NAME} << EOF
-- Install UUID extension (required for UUID generation)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO ${DB_USER};
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${DB_USER};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${DB_USER};

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${DB_USER};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ${DB_USER};
EOF
echo -e "${GREEN}✓ Extensions installed${NC}"
echo -e "${GREEN}✓ Permissions configured${NC}"
echo ""

# Verify setup
echo -e "${YELLOW}Verifying setup...${NC}"
psql -d postgres << EOF
\echo '--- Database ---'
SELECT datname, datdba::regrole as owner FROM pg_database WHERE datname = '${DB_NAME}';
\echo ''
\echo '--- User ---'
SELECT rolname, rolsuper, rolcreatedb FROM pg_roles WHERE rolname = '${DB_USER}';
EOF
echo ""

echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  Database Setup Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "Database: ${BLUE}${DB_NAME}${NC}"
echo -e "User:     ${BLUE}${DB_USER}${NC}"
echo -e "Password: ${BLUE}${DB_PASSWORD}${NC}"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "  1. Create .env file with AWS credentials:"
echo "     cp .env.example .env"
echo "     # Edit .env with your AWS S3 credentials"
echo ""
echo "  2. Run the backend (Flyway will create tables & seed data):"
echo "     ./run-with-postgres.sh"
echo ""
echo -e "${GREEN}=========================================${NC}"

