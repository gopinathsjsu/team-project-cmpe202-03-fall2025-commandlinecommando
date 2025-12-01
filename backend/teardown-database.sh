#!/bin/bash
# =============================================================================
# Campus Marketplace - Database Teardown Script
# =============================================================================
# This script removes the PostgreSQL database completely:
#   1. Terminates all active connections
#   2. Drops the database (campus_marketplace)
#   3. Drops the database user (cm_app_user)
#
# Usage:
#   ./teardown-database.sh           # Interactive mode (asks for confirmation)
#   ./teardown-database.sh --force   # Skip confirmation
#
# Prerequisites:
#   - PostgreSQL installed and running
#   - Current user has superuser access to PostgreSQL
# =============================================================================

set -e  # Exit on any error

# Configuration
DB_NAME="campus_marketplace"
DB_USER="cm_app_user"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${RED}=========================================${NC}"
echo -e "${RED}  Campus Marketplace Database Teardown${NC}"
echo -e "${RED}=========================================${NC}"
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

# Check if database exists
DB_EXISTS=$(psql -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '${DB_NAME}'")
USER_EXISTS=$(psql -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname = '${DB_USER}'")

if [ -z "$DB_EXISTS" ] && [ -z "$USER_EXISTS" ]; then
    echo -e "${YELLOW}Nothing to remove - database and user don't exist.${NC}"
    exit 0
fi

# Show what will be removed
echo -e "${YELLOW}The following will be PERMANENTLY DELETED:${NC}"
echo ""
if [ -n "$DB_EXISTS" ]; then
    echo -e "  ${RED}✗${NC} Database: ${BLUE}${DB_NAME}${NC}"
    # Show table count
    TABLE_COUNT=$(psql -d ${DB_NAME} -tAc "SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public'" 2>/dev/null || echo "0")
    echo -e "    └── Tables: ${TABLE_COUNT}"
fi
if [ -n "$USER_EXISTS" ]; then
    echo -e "  ${RED}✗${NC} User: ${BLUE}${DB_USER}${NC}"
fi
echo ""

# Confirmation (unless --force flag is used)
if [ "$1" != "--force" ]; then
    echo -e "${RED}⚠️  WARNING: This action cannot be undone!${NC}"
    echo ""
    read -p "Are you sure you want to continue? (yes/no): " CONFIRM
    if [ "$CONFIRM" != "yes" ]; then
        echo -e "${YELLOW}Cancelled. No changes made.${NC}"
        exit 0
    fi
    echo ""
fi

# Terminate all connections
echo -e "${YELLOW}Terminating active connections...${NC}"
psql -d postgres << EOF
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = '${DB_NAME}' AND pid <> pg_backend_pid();
EOF
echo -e "${GREEN}✓ Connections terminated${NC}"

# Drop database
if [ -n "$DB_EXISTS" ]; then
    echo -e "${YELLOW}Dropping database...${NC}"
    psql -d postgres -c "DROP DATABASE IF EXISTS ${DB_NAME};"
    echo -e "${GREEN}✓ Database '${DB_NAME}' dropped${NC}"
fi

# Drop user
if [ -n "$USER_EXISTS" ]; then
    echo -e "${YELLOW}Dropping user...${NC}"
    psql -d postgres -c "DROP USER IF EXISTS ${DB_USER};"
    echo -e "${GREEN}✓ User '${DB_USER}' dropped${NC}"
fi

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  Database Teardown Complete!${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "${YELLOW}To set up a fresh database, run:${NC}"
echo "  ./setup-database.sh"
echo ""

