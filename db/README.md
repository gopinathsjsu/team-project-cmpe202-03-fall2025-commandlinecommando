# Campus Marketplace Database Setup

## Overview

This directory contains all database-related infrastructure for the Campus Marketplace application, including PostgreSQL setup, backup scripts, monitoring tools, and comprehensive documentation.

## Quick Start

1. **Copy environment template:**
   ```bash
   cp .env.template .env
   # Edit .env with your secure passwords and configuration
   ```

2. **Start database services:**
   ```bash
   docker-compose up -d
   ```

3. **Verify setup:**
   ```bash
   ./db/scripts/monitor.sh --health
   ```

4. **Access pgAdmin:**
   - URL: http://localhost:8080
   - Login with credentials from `.env` file

## Directory Structure

```
db/
├── scripts/
│   ├── init/                    # Database initialization scripts
│   │   ├── 01-init-database.sh  # User creation and permissions
│   │   └── 02-create-schema.sql # Schema setup and indexes
│   ├── backup.sh               # Automated backup script
│   ├── restore.sh              # Database restore script
│   └── monitor.sh              # Database monitoring and health checks
├── backups/                    # Backup storage directory
│   ├── local/                  # Local backup files
│   ├── remote/                 # Remote backup tracking
│   └── pre-restore/           # Pre-restore safety backups
├── migrations/                 # Manual database migrations
└── docs/                      # Comprehensive documentation
    ├── DATABASE_SETUP.md      # Complete setup guide
    ├── TROUBLESHOOTING.md     # Common issues and solutions
    └── SECURITY.md            # Security best practices
```

## Features

### 🗄️ Database Configuration
- **PostgreSQL 15+** with optimized settings for development
- **Connection pooling** (HikariCP) with max 20 connections
- **Multiple user roles**: application user and read-only analytics user
- **SSL/TLS support** for secure connections

### 🔧 Development Tools
- **pgAdmin** for database management (localhost:8080)
- **Health monitoring** scripts with colored output
- **Performance metrics** and slow query analysis
- **Connection statistics** and pool monitoring

### 💾 Backup & Recovery
- **Automated daily backups** with compression and integrity checks
- **Point-in-time recovery** capability
- **Retention policies**: 7 days local, 30 days remote
- **Easy restore** with interactive selection

### 📊 Monitoring
- **Real-time health checks** with status indicators
- **Performance monitoring** including query statistics
- **Resource usage tracking** (connections, memory, disk)
- **Audit logging** for security events

### 🔒 Security
- **Strong password policies** and user isolation
- **SSL/TLS encryption** for data in transit
- **Audit trails** for all database changes
- **Secure backup encryption** with GPG

## Environment Profiles

### Development (default)
- Uses H2 in-memory database
- Auto-creates schema on startup
- Verbose SQL logging enabled
- H2 console available at `/h2-console`

### Production
- Uses PostgreSQL with connection pooling
- Schema validation only (no auto-creation)
- Optimized logging and performance settings
- SSL connections required

### Testing
- Uses separate H2 database
- Security disabled for easier testing
- Clean schema for each test run

## Database Users

| User | Purpose | Permissions |
|------|---------|-------------|
| `postgres` | Database administration | Full admin access |
| `cm_app_user` | Application operations | Read/write/admin on app schemas |
| `cm_readonly` | Analytics/reporting | Read-only access to all tables |

## Key Scripts

### Backup Script (`./db/scripts/backup.sh`)
```bash
./db/scripts/backup.sh           # Full backup
./db/scripts/backup.sh --test    # Test connection only
./db/scripts/backup.sh --cleanup-only  # Cleanup old backups
```

### Restore Script (`./db/scripts/restore.sh`)
```bash
./db/scripts/restore.sh --latest      # Restore latest backup
./db/scripts/restore.sh --interactive # Choose backup interactively  
./db/scripts/restore.sh --file backup.sql.gz  # Restore specific file
```

### Monitoring Script (`./db/scripts/monitor.sh`)
```bash
./db/scripts/monitor.sh --health      # Quick health check
./db/scripts/monitor.sh --full        # Complete monitoring report
./db/scripts/monitor.sh --performance # Performance metrics only
```

## Connection Examples

### Spring Boot Application
```yaml
# Development
spring:
  profiles:
    active: dev
  # Uses H2 automatically

# Production  
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/campus_marketplace?sslmode=require
    username: ${DB_APP_USER}
    password: ${DB_APP_PASSWORD}
```

### Direct Connection (psql)
```bash
# Application user
psql -h localhost -p 5432 -U cm_app_user -d campus_marketplace

# Read-only user  
psql -h localhost -p 5432 -U cm_readonly -d campus_marketplace
```

### pgAdmin Connection
- **Host**: `postgres` (container name)
- **Port**: `5432`
- **Database**: `campus_marketplace`
- **Username**: `cm_app_user` or `cm_readonly`
- **Password**: From `.env` file

## Performance Optimization

### Connection Pool Settings
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Max connections
      minimum-idle: 5            # Min idle connections
      idle-timeout: 300000       # 5 minutes
      max-lifetime: 600000       # 10 minutes
      connection-timeout: 20000  # 20 seconds
```

### Database Tuning
The PostgreSQL instance is optimized with:
- `shared_buffers=256MB` - Memory for caching
- `effective_cache_size=1GB` - Available system cache
- `work_mem=4MB` - Memory per query operation
- `max_connections=20` - Connection limit
- Query performance monitoring enabled

## Troubleshooting

### Common Issues

1. **Connection Refused**
   ```bash
   docker-compose ps  # Check if containers are running
   docker-compose up -d postgres  # Start database
   ```

2. **Authentication Failed**
   ```bash
   # Check environment variables
   cat .env | grep DB_
   # Reset password if needed
   docker-compose exec postgres psql -U postgres -c "ALTER USER cm_app_user PASSWORD 'new_pass';"
   ```

3. **Database Not Found**
   ```bash
   # Recreate database
   docker-compose down -v  # WARNING: Deletes data
   docker-compose up -d
   ```

### Health Check
Always start troubleshooting with:
```bash
./db/scripts/monitor.sh --health
```

This provides:
- ✅ Connection status
- ⚠️ Backup status  
- 🔍 Disk usage
- 📊 Overall health

## Security Checklist

- [ ] Changed default passwords in `.env`
- [ ] Enabled SSL for production connections
- [ ] Configured firewall rules for database port
- [ ] Set up automated backup encryption
- [ ] Reviewed user permissions and roles
- [ ] Enabled audit logging for sensitive operations
- [ ] Configured backup retention policies
- [ ] Tested disaster recovery procedures

## Monitoring Checklist

- [ ] Database health checks are green
- [ ] Connection pool usage is below 80%
- [ ] No slow queries (>1 second)
- [ ] Cache hit ratio is above 95%
- [ ] Recent backups are available
- [ ] Disk usage is below 80%
- [ ] No failed authentication attempts

## Documentation

For detailed information, see:

- **[DATABASE_SETUP.md](docs/DATABASE_SETUP.md)** - Complete setup guide with step-by-step instructions
- **[TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)** - Solutions to common issues and problems
- **[SECURITY.md](docs/SECURITY.md)** - Security best practices and compliance guidelines

## Support

For issues or questions:

1. Check the troubleshooting guide
2. Run health monitoring scripts
3. Review application and database logs
4. Contact the development team with diagnostic information

---

**Database Version**: PostgreSQL 15+  
**Last Updated**: December 2024  
**Maintainer**: Campus Marketplace Team