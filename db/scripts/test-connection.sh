#!/bin/sh
# ==============================================
# Database Connection Test Script
# ==============================================
# Quick test script to verify database connectivity

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Load environment variables
if [ -f "$PROJECT_ROOT/.env" ]; then
    export $(grep -v '^#' "$PROJECT_ROOT/.env" | xargs)
fi

DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-campus_marketplace}
DB_APP_USER=${DB_APP_USER:-cm_app_user}
DB_APP_PASSWORD=${DB_APP_PASSWORD}

echo "Testing database connection..."
echo "Host: $DB_HOST:$DB_PORT"
echo "Database: $DB_NAME"
echo "User: $DB_APP_USER"
echo

# Test with application user
export PGPASSWORD="$DB_APP_PASSWORD"

if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "SELECT 'Connection successful!' as status, version();" 2>/dev/null; then
    echo "✅ Database connection successful!"
    
    # Test basic operations
    echo
    echo "Testing basic operations..."
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_APP_USER" -d "$DB_NAME" -c "
        SELECT 
            'Tables' as metric,
            count(*) as value
        FROM information_schema.tables 
        WHERE table_schema = 'public'
        UNION ALL
        SELECT 
            'Active connections' as metric,
            count(*)::text as value
        FROM pg_stat_activity 
        WHERE datname = current_database();
    " 2>/dev/null
    
else
    echo "❌ Database connection failed!"
    echo
    echo "Troubleshooting steps:"
    echo "1. Check if PostgreSQL is running: docker-compose ps"
    echo "2. Verify credentials in .env file"
    echo "3. Check network connectivity: telnet $DB_HOST $DB_PORT"
    echo "4. Review database logs: docker-compose logs postgres"
    exit 1
fi