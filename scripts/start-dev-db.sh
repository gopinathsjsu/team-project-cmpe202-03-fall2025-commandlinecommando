#!/bin/bash

# ==============================================
# Start PostgreSQL Database for Development
# ==============================================

set -e

echo "🚀 Starting Campus Marketplace Development Database..."
echo ""

# Start only PostgreSQL and Redis (no backend)
docker-compose up -d postgres redis

echo ""
echo "⏳ Waiting for PostgreSQL to be ready..."
sleep 5

# Check PostgreSQL health
if docker exec campus-marketplace-db pg_isready -U cm_app_user -d campus_marketplace > /dev/null 2>&1; then
    echo "✅ PostgreSQL is ready!"
else
    echo "⚠️  PostgreSQL is starting... (may take a few more seconds)"
    sleep 5
fi

# Check Redis health
if docker exec campus-marketplace-redis redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis is ready!"
else
    echo "⚠️  Redis is starting..."
fi

echo ""
echo "📊 Database Information:"
echo "   Host: localhost"
echo "   Port: 5432"
echo "   Database: campus_marketplace"
echo "   Username: cm_app_user"
echo "   Password: changeme"
echo ""
echo "🔧 Redis Information:"
echo "   Host: localhost"
echo "   Port: 6379"
echo ""
echo "🎯 Next Steps:"
echo "   1. Run backend: cd backend && ./mvnw spring-boot:run"
echo "   2. Backend will connect to PostgreSQL automatically"
echo "   3. Flyway migrations will run on startup"
echo ""
echo "🛑 To stop: docker-compose down"
echo "🗑️  To reset data: docker-compose down -v"
echo ""
