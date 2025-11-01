# Campus Marketplace Database Migrations

## 📋 Overview

Flyway-compatible database migration scripts for the Campus Marketplace CMPE 202 project. These migrations support multi-cloud deployment (AWS RDS, GCP Cloud SQL, Azure Database) with full backward compatibility.

## 🗂️ Migration Files

| Version | File | Description | Status |
|---------|------|-------------|--------|
| V1 | `V1__campus_marketplace_core_schema.sql` | Core schema with role-based design | ✅ Production Ready |
| V2 | `V2__seed_demo_data.sql` | Demo data for all user roles | ✅ Demo Ready |
| V3 | `V3__api_optimization_indexes.sql` | Performance indexes & materialized views | ✅ API Optimized |

## 🚀 Quick Start

### Local Development

```bash
# Run all migrations
flyway migrate

# Validate migrations
flyway validate

# Check migration status
flyway info
```

### Cloud Deployment

#### AWS RDS

```bash
# Set environment variables
export RDS_ENDPOINT=your-rds-endpoint.amazonaws.com
export RDS_USERNAME=cm_app_user
export RDS_PASSWORD=your_password

# Run migrations
flyway -configFiles=flyway-prod.conf migrate
```

#### GCP Cloud SQL

```bash
# Set environment variables
export CLOUDSQL_CONNECTION_NAME=your-project:region:instance
export CLOUDSQL_USERNAME=cm_app_user
export CLOUDSQL_PASSWORD=your_password

# Run migrations
flyway -configFiles=flyway-gcp.conf migrate
```

#### Azure Database for PostgreSQL

```bash
# Set environment variables
export AZURE_DB_SERVER=your-server.postgres.database.azure.com
export AZURE_USERNAME=cm_app_user
export AZURE_PASSWORD=your_password

# Run migrations
flyway -configFiles=flyway-azure.conf migrate
```

## 📊 Schema Overview

### Core Tables

**User Management:**
- `universities` - Multi-tenant university support
- `users` - Buyer, Seller, Admin roles
- `user_addresses` - Delivery addresses

**Marketplace Catalog:**
- `products` - Product listings with full-text search
- `product_images` - Image management
- `product_reviews` - Ratings and reviews
- `user_favorites` - Wishlist functionality

**Transaction Processing:**
- `orders` - Shopping cart & order lifecycle
- `order_items` - Line items with seller tracking
- `transactions` - Payment processing
- `payment_methods` - Tokenized payment data
- `seller_payouts` - Revenue distribution

**Analytics & Compliance:**
- `search_history` - Search analytics
- `product_views` - Engagement tracking
- `daily_analytics` - Aggregated metrics
- `audit_logs` - Complete audit trail
- `moderation_queue` - Content moderation

### Performance Features

✅ **Full-text search** with `pg_trgm` and `tsvector`
✅ **Materialized views** for complex aggregations
✅ **Partial indexes** for filtered queries
✅ **Composite indexes** for API optimization
✅ **Row-level security** for multi-tenant isolation

## 🔒 Security Features

- **Row-Level Security (RLS)** policies for university isolation
- **Encrypted sensitive data** (payment tokens, personal info)
- **Audit logging** for all data modifications
- **Content moderation** workflow
- **Role-based access control** (RBAC)

## 🎯 API Performance Targets

| Metric | Target | Implementation |
|--------|--------|----------------|
| Product Search | <200ms | Composite indexes + full-text search |
| Order Creation | <300ms | Optimized foreign keys |
| Seller Dashboard | <150ms | Materialized views |
| Admin Analytics | <250ms | Pre-aggregated daily stats |

## 🔄 Migration Workflow

### Development

```bash
# 1. Create new migration
touch V4__your_migration_name.sql

# 2. Run migration
flyway migrate

# 3. Validate
flyway validate

# 4. Check status
flyway info
```

### Production Deployment

```bash
# 1. Backup database
pg_dump -h $DB_HOST -U $DB_USER -d campus_marketplace -F c -f backup.dump

# 2. Run migrations with dry-run
flyway -configFiles=flyway-prod.conf info

# 3. Execute migrations
flyway -configFiles=flyway-prod.conf migrate

# 4. Validate schema
flyway -configFiles=flyway-prod.conf validate
```

## 📈 Monitoring & Maintenance

### Materialized View Refresh

```sql
-- Refresh all analytics views
SELECT refresh_marketplace_analytics();

-- Or refresh individually
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_seller_performance;
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_popular_products;
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_university_stats;
```

**Recommended Schedule:** Every 15 minutes during peak hours (9 AM - 9 PM)

### Index Maintenance

```sql
-- Check index usage
SELECT * FROM pg_stat_user_indexes 
WHERE schemaname = 'public' 
ORDER BY idx_scan DESC;

-- Reindex if needed
REINDEX INDEX CONCURRENTLY idx_products_marketplace_search;
```

### Query Performance Monitoring

```sql
-- Check slow queries
SELECT * FROM query_performance_log 
WHERE execution_time_ms > 200 
ORDER BY created_at DESC 
LIMIT 10;
```

## 🧪 Testing

### Run Demo Data Seed

```bash
# Apply V1 (schema) + V2 (demo data)
flyway migrate -target=2
```

### Rollback (Development Only)

```bash
# Clean database (CAUTION: Deletes all data)
flyway clean

# Reapply migrations
flyway migrate
```

## 📦 Cloud Migration Checklist

- [ ] Set up cloud database instance (RDS/Cloud SQL/Azure)
- [ ] Configure SSL/TLS connection
- [ ] Set environment variables
- [ ] Run initial migration: `flyway baseline`
- [ ] Execute all migrations: `flyway migrate`
- [ ] Verify schema: `flyway validate`
- [ ] Test application connectivity
- [ ] Schedule materialized view refresh (cron/Lambda/Cloud Function)
- [ ] Set up monitoring and alerts
- [ ] Configure automated backups

## 🔗 Related Documentation

- **[Database Setup Guide](../docs/DATABASE_SETUP.md)** - Complete setup instructions
- **[Security Guide](../docs/SECURITY.md)** - Security best practices
- **[Troubleshooting](../docs/TROUBLESHOOTING.md)** - Common issues
- **[API Documentation](../../backend/README.md)** - RESTful API integration

## 📝 Version History

| Version | Date | Changes |
|---------|------|---------|
| 3.0.0 | 2025-01-07 | API optimization indexes & materialized views |
| 2.0.0 | 2025-01-07 | Demo data seed for all user roles |
| 1.0.0 | 2025-01-07 | Core schema with role-based design |

## 🆘 Support

For issues or questions:
1. Check [Troubleshooting Guide](../docs/TROUBLESHOOTING.md)
2. Review [Team Setup Guide](../docs/TEAM_SETUP_GUIDE.md)
3. Contact database admin team

---

**CMPE 202 Team - Commandline Commandos** | San Jose State University

