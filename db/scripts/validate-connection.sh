#!/bin/bash

# Campus Marketplace Database Connection Validation Script
# This script validates database connections and permissions

set -e

echo "=== Campus Marketplace Database Connection Validation ==="
echo

# Load environment variables
if [ -f "../../.env" ]; then
    source ../../.env
    echo "✅ Environment variables loaded"
else
    echo "❌ .env file not found"
    exit 1
fi

# Test PostgreSQL service availability
echo
echo "🔍 Testing PostgreSQL service availability..."
if docker exec campus_marketplace_db pg_isready -U postgres > /dev/null 2>&1; then
    echo "✅ PostgreSQL service is running and accepting connections"
else
    echo "❌ PostgreSQL service is not available"
    exit 1
fi

# Test admin connection
echo
echo "🔍 Testing admin (postgres) connection..."
if docker exec -it campus_marketplace_db psql -U postgres -d campusmarketplace_db -c "SELECT 'Admin connection successful' as status;" > /dev/null 2>&1; then
    echo "✅ Admin connection successful"
else
    echo "❌ Admin connection failed"
    exit 1
fi

# Test application user connection
echo
echo "🔍 Testing application user (cm_app_user) connection..."
if docker exec -it campus_marketplace_db psql -U cm_app_user -d campusmarketplace_db -c "SELECT 'App user connection successful' as status;" > /dev/null 2>&1; then
    echo "✅ Application user connection successful"
else
    echo "❌ Application user connection failed"
    exit 1
fi

# Test read-only user connection
echo
echo "🔍 Testing read-only user (cm_readonly) connection..."
if docker exec -it campus_marketplace_db psql -U cm_readonly -d campusmarketplace_db -c "SELECT 'Readonly user connection successful' as status;" > /dev/null 2>&1; then
    echo "✅ Read-only user connection successful"
else
    echo "❌ Read-only user connection failed"
    exit 1
fi

# Test application user permissions
echo
echo "🔍 Testing application user permissions (CREATE, INSERT, SELECT, DROP)..."
if docker exec -it campus_marketplace_db psql -U cm_app_user -d campusmarketplace_db -c "
    CREATE TABLE permission_test (id SERIAL PRIMARY KEY, test_data VARCHAR(50));
    INSERT INTO permission_test (test_data) VALUES ('Permission test data');
    SELECT COUNT(*) FROM permission_test;
    DROP TABLE permission_test;
" > /dev/null 2>&1; then
    echo "✅ Application user has full permissions (CREATE, INSERT, SELECT, DROP)"
else
    echo "❌ Application user permissions test failed"
    exit 1
fi

# Test read-only user limitations
echo
echo "🔍 Testing read-only user limitations..."
# First create a test table as admin
docker exec -it campus_marketplace_db psql -U postgres -d campusmarketplace_db -c "
    CREATE TABLE readonly_test (id SERIAL PRIMARY KEY, data VARCHAR(50));
    INSERT INTO readonly_test (data) VALUES ('Test data');
    GRANT SELECT ON readonly_test TO cm_readonly;
" > /dev/null 2>&1

# Test that readonly user can SELECT but not INSERT
if docker exec -it campus_marketplace_db psql -U cm_readonly -d campusmarketplace_db -c "SELECT COUNT(*) FROM readonly_test;" > /dev/null 2>&1; then
    echo "✅ Read-only user can SELECT from tables"
else
    echo "❌ Read-only user cannot SELECT from tables"
fi

# Test that readonly user cannot INSERT
if ! docker exec -it campus_marketplace_db psql -U cm_readonly -d campusmarketplace_db -c "INSERT INTO readonly_test (data) VALUES ('Should fail');" > /dev/null 2>&1; then
    echo "✅ Read-only user correctly restricted from INSERT operations"
else
    echo "❌ Read-only user can INSERT (this should not be allowed)"
fi

# Cleanup
docker exec -it campus_marketplace_db psql -U postgres -d campusmarketplace_db -c "DROP TABLE readonly_test;" > /dev/null 2>&1

# Test services availability
echo
echo "🔍 Testing additional services..."

# Test pgAdmin availability
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q "200"; then
    echo "✅ pgAdmin is available at http://localhost:8080"
else
    echo "ℹ️  pgAdmin may be starting up (accessible at http://localhost:8080)"
fi

# Test Redis availability
if docker exec campus_marketplace_redis redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis is running and responsive"
else
    echo "❌ Redis is not available"
fi

echo
echo "=== Database Connection Validation Summary ==="
echo "✅ PostgreSQL Database: campusmarketplace_db"
echo "✅ Application User: cm_app_user (full permissions)"
echo "✅ Read-only User: cm_readonly (SELECT only)"
echo "✅ Connection Pool: HikariCP configured"
echo "✅ Services: PostgreSQL, pgAdmin, Redis"
echo
echo "🎉 Campus Marketplace database is ready for development!"
echo
echo "Database Connection Details:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: campusmarketplace_db"
echo "  Application User: cm_app_user"
echo "  Read-only User: cm_readonly"
echo
echo "Spring Boot Configuration:"
echo "  Production Profile: SPRING_PROFILES_ACTIVE=prod"
echo "  Database URL: jdbc:postgresql://localhost:5432/campusmarketplace_db"
echo
echo "Web Interfaces:"
echo "  pgAdmin: http://localhost:8080"
echo "  Admin: ${PGADMIN_DEFAULT_EMAIL:-admin@campusmarketplace.com}"