# Campus Marketplace Database Setup Guide

This document provides comprehensive instructions for setting up the PostgreSQL database environment for the Campus Marketplace application.

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Quick Start (Docker)](#quick-start-docker)
4. [Manual Installation](#manual-installation)
5. [Environment Configuration](#environment-configuration)
6. [Database Migration](#database-migration)
7. [Monitoring & Maintenance](#monitoring--maintenance)
8. [Backup & Recovery](#backup--recovery)
9. [Troubleshooting](#troubleshooting)
10. [Security Best Practices](#security-best-practices)

## Overview

The Campus Marketplace uses PostgreSQL 15+ as its primary database with the following features:

- **Development Environment**: H2 in-memory database for quick development
- **Production Environment**: PostgreSQL with connection pooling
- **Connection Pool**: HikariCP with max 20 connections
- **SSL/TLS Support**: Configurable SSL connections
- **Automated Backups**: Daily backups with retention policies
- **Monitoring**: Built-in health checks and performance monitoring
- **Multiple Users**: Application user and read-only analytics user

## Prerequisites

### System Requirements

- **Operating System**: macOS, Linux, or Windows
- **Docker & Docker Compose**: Version 20.10+ (recommended)
- **Java**: JDK 21+ (for application)
- **PostgreSQL Client Tools**: For manual operations (optional)

### Development Tools

- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Database Client**: pgAdmin (included in Docker setup) or DataGrip

## Quick Start (Docker)

### 1. Clone and Setup

```bash
# Navigate to project root
cd team-project-cmpe202-03-fall2025-commandlinecommando

# Copy environment template
cp .env.template .env

# Edit environment variables (important!)
nano .env
```

### 2. Configure Environment

Update the `.env` file with your values:

```bash
# Database Configuration
DB_ROOT_PASSWORD=your_secure_postgres_password
DB_APP_PASSWORD=your_secure_app_password
DB_READONLY_PASSWORD=your_secure_readonly_password

# pgAdmin Configuration
PGADMIN_EMAIL=your_email@example.com
PGADMIN_PASSWORD=your_secure_pgadmin_password
```

### 3. Start Services

```bash
# Start PostgreSQL, pgAdmin, and Redis
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs postgres
```

### 4. Verify Setup

```bash
# Test database connection
./db/scripts/monitor.sh --health

# Access pgAdmin
# Open browser: http://localhost:8080
# Login with credentials from .env file
```

### 5. Run Application

```bash
# Development mode (uses H2)
cd backend
./mvnw spring-boot:run

# Production mode (uses PostgreSQL)
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

## Manual Installation

### 1. Install PostgreSQL

#### macOS (using Homebrew)

```bash
# Install PostgreSQL
brew install postgresql@15

# Start PostgreSQL service
brew services start postgresql@15

# Create database
createdb campus_marketplace
```

#### Ubuntu/Debian

```bash
# Install PostgreSQL
sudo apt update
sudo apt install postgresql-15 postgresql-contrib-15

# Start PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Switch to postgres user and create database
sudo -u postgres createdb campus_marketplace
```

### 2. Configure PostgreSQL

```bash
# Connect as postgres user
sudo -u postgres psql

# Run initialization script
\i /path/to/project/db/scripts/init/01-init-database.sh
\i /path/to/project/db/scripts/init/02-create-schema.sql
```

### 3. Configure SSL (Production)

```bash
# Generate SSL certificates (example)
sudo openssl req -new -x509 -days 365 -nodes -text \
  -out /etc/ssl/certs/server.crt \
  -keyout /etc/ssl/private/server.key \
  -subj "/CN=localhost"

# Set permissions
sudo chmod 600 /etc/ssl/private/server.key
sudo chmod 644 /etc/ssl/certs/server.crt
```

## Environment Configuration

### Application Profiles

The application supports multiple profiles:

- **dev** (default): Uses H2 in-memory database
- **prod**: Uses PostgreSQL with full production settings  
- **test**: Uses H2 for testing with security disabled

### Connection Strings

#### Development (H2)
```
jdbc:h2:mem:campusmarketplace
```

#### Production (PostgreSQL)
```
jdbc:postgresql://localhost:5432/campus_marketplace?sslmode=require&serverTimezone=UTC
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `campus_marketplace` |
| `DB_APP_USER` | Application user | `cm_app_user` |
| `DB_APP_PASSWORD` | Application password | *(required)* |
| `DB_READONLY_USER` | Read-only user | `cm_readonly` |
| `SSL_MODE` | SSL mode | `require` |

### HikariCP Configuration

The application uses HikariCP for connection pooling with these settings:

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 600000
      connection-timeout: 20000
      leak-detection-threshold: 60000
```

## Database Migration

### Schema Management

The application uses Hibernate for schema management:

- **Development**: `ddl-auto: create-drop` (recreates schema on restart)
- **Production**: `ddl-auto: validate` (validates existing schema)

### Manual Schema Updates

For production schema changes:

```bash
# 1. Create migration script
cd db/migrations
nano V1_1__add_new_table.sql

# 2. Test migration in development
# 3. Apply to production during maintenance window
psql -h localhost -U cm_app_user -d campus_marketplace -f V1_1__add_new_table.sql
```

### Entity Relationship Overview

The application includes these main entities:

- **Users**: Base user information
- **Students**: Student-specific data
- **Admins**: Administrator data  
- **Listings**: Product listings
- **ListingImages**: Product images
- **RefreshTokens**: JWT refresh tokens

## Monitoring & Maintenance

### Health Checks

```bash
# Quick health check
./db/scripts/monitor.sh --health

# Full monitoring report
./db/scripts/monitor.sh --full

# Specific checks
./db/scripts/monitor.sh --performance
./db/scripts/monitor.sh --connections
./db/scripts/monitor.sh --size
```

### Application Metrics

The application exposes metrics via Spring Boot Actuator:

- **Health**: http://localhost:8080/api/actuator/health
- **Metrics**: http://localhost:8080/api/actuator/metrics
- **Prometheus**: http://localhost:8080/api/actuator/prometheus

### Database Performance

Monitor these key metrics:

- **Connection Pool Usage**: Should stay below 80%
- **Cache Hit Ratio**: Should be above 95%
- **Index Usage**: Ensure critical queries use indexes
- **Slow Queries**: Monitor queries taking >1 second

## Backup & Recovery

### Automated Backups

Backups are handled by the backup script:

```bash
# Manual backup
./db/scripts/backup.sh

# Test backup script
./db/scripts/backup.sh --test

# Cleanup old backups only
./db/scripts/backup.sh --cleanup-only
```

### Scheduled Backups

Setup automated backups using cron:

```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * /path/to/project/db/scripts/backup.sh >> /var/log/campus-marketplace-backup.log 2>&1
```

### Backup Retention

- **Local Backups**: 7 days (configurable)
- **Remote Backups**: 30 days (configurable)
- **Backup Location**: `db/backups/local/YYYY/MM/DD/`

### Restore Procedures

```bash
# List available backups
./db/scripts/restore.sh --list

# Restore latest backup
./db/scripts/restore.sh --latest

# Restore specific backup
./db/scripts/restore.sh --file campus_marketplace_20231201_120000.sql.gz

# Interactive restore
./db/scripts/restore.sh --interactive
```

### Point-in-Time Recovery

For production systems, enable WAL archiving:

```sql
-- Enable WAL archiving in postgresql.conf
archive_mode = on
archive_command = 'cp %p /path/to/archive/%f'
wal_level = replica
```

## Troubleshooting

### Common Issues

#### 1. Connection Refused

**Symptoms**: `Connection refused` or `could not connect to server`

**Solutions**:
```bash
# Check if PostgreSQL is running
docker-compose ps
# or
sudo systemctl status postgresql

# Check port binding
netstat -tlnp | grep 5432

# Check firewall settings
sudo ufw status
```

#### 2. Authentication Failed

**Symptoms**: `FATAL: password authentication failed`

**Solutions**:
```bash
# Verify credentials in .env file
cat .env | grep DB_

# Reset database user password
docker-compose exec postgres psql -U postgres -c "ALTER USER cm_app_user PASSWORD 'new_password';"
```

#### 3. Database Does Not Exist

**Symptoms**: `FATAL: database "campus_marketplace" does not exist`

**Solutions**:
```bash
# Recreate database
docker-compose exec postgres createdb -U postgres campus_marketplace

# Or reinitialize containers
docker-compose down -v
docker-compose up -d
```

#### 4. Connection Pool Exhausted

**Symptoms**: `Unable to obtain connection from database pool`

**Solutions**:
```bash
# Check active connections
./db/scripts/monitor.sh --connections

# Kill long-running queries
docker-compose exec postgres psql -U postgres -d campus_marketplace -c "
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = 'campus_marketplace' 
AND state = 'idle in transaction' 
AND query_start < now() - interval '5 minutes';
"
```

#### 5. High Memory Usage

**Symptoms**: Out of memory errors, slow performance

**Solutions**:
```bash
# Check memory usage
docker stats campus_marketplace_db

# Adjust PostgreSQL memory settings in docker-compose.yml
# Reduce shared_buffers, work_mem, or effective_cache_size
```

### Performance Tuning

#### 1. Slow Queries

```bash
# Enable slow query logging
docker-compose exec postgres psql -U postgres -c "
ALTER SYSTEM SET log_min_duration_statement = 1000;
SELECT pg_reload_conf();
"

# Check slow queries
./db/scripts/monitor.sh --queries
```

#### 2. Missing Indexes

```bash
# Find unused indexes
./db/scripts/monitor.sh --indexes

# Find missing indexes for common queries
docker-compose exec postgres psql -U postgres -d campus_marketplace -c "
SELECT schemaname, tablename, attname, n_distinct, correlation 
FROM pg_stats 
WHERE schemaname = 'public' 
ORDER BY n_distinct DESC;
"
```

#### 3. Connection Pool Tuning

Adjust HikariCP settings in `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10  # Reduce if CPU usage is high
      minimum-idle: 2        # Reduce for lower memory usage
      idle-timeout: 600000   # Increase to keep connections longer
```

### Log Analysis

#### Application Logs

```bash
# View application logs
cd backend
tail -f logs/campus-marketplace.log

# Or with Docker
docker-compose logs -f your-app-container
```

#### Database Logs

```bash
# View PostgreSQL logs
docker-compose logs postgres

# View slow query log
docker-compose exec postgres tail -f /var/log/postgresql/postgresql-*.log
```

## Security Best Practices

### 1. Password Security

- Use strong passwords (minimum 12 characters)
- Include uppercase, lowercase, numbers, and symbols
- Rotate passwords regularly (quarterly)
- Never commit passwords to version control

### 2. Network Security

```bash
# Use SSL connections
SSL_MODE=require

# Restrict network access
# In production, bind PostgreSQL only to localhost or private network
```

### 3. User Permissions

```sql
-- Follow principle of least privilege
-- Application user: only necessary permissions
-- Read-only user: SELECT only
-- No SUPERUSER permissions for application users
```

### 4. Data Encryption

```sql
-- Enable data encryption at rest
-- Use pgcrypto for sensitive fields
CREATE EXTENSION pgcrypto;

-- Example encrypted column
ALTER TABLE users ADD COLUMN encrypted_ssn TEXT;
UPDATE users SET encrypted_ssn = pgp_sym_encrypt(ssn, 'encryption_key');
```

### 5. Audit Logging

The database includes audit triggers for tracking changes:

```sql
-- View audit logs
SELECT * FROM audit.activity_log 
WHERE table_name = 'users' 
ORDER BY created_at DESC 
LIMIT 10;
```

### 6. Regular Security Updates

```bash
# Update PostgreSQL regularly
docker-compose pull
docker-compose up -d

# Monitor for security advisories
# https://www.postgresql.org/support/security/
```

### 7. Backup Security

```bash
# Encrypt backups
gpg --symmetric --cipher-algo AES256 backup_file.sql.gz

# Secure backup storage
# Use encrypted storage for remote backups
# Restrict access to backup files (600 permissions)
```

## Support and Resources

### Documentation Links

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Boot Data JPA](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.jpa-and-spring-data)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)
- [Docker Compose](https://docs.docker.com/compose/)

### Getting Help

1. Check this documentation first
2. Review application logs and database logs
3. Use monitoring scripts for diagnostics
4. Search PostgreSQL and Spring Boot documentation
5. Contact the development team

### Contributing

When contributing database-related changes:

1. Test changes in development environment first
2. Document schema changes in migration files
3. Update this documentation as needed
4. Follow security best practices
5. Test backup and restore procedures

---

**Version**: 1.0  
**Maintainer**: Campus Marketplace Development Team
