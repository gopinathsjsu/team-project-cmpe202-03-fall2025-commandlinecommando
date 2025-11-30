# Campus Marketplace Database Troubleshooting Guide

This document provides solutions to common database-related issues encountered during development and deployment.

## Quick Diagnostics

### Health Check Script
Always start with the health check script:

```bash
./db/scripts/monitor.sh --health
```

This will show:
- ✅ Connection status
- ⚠️ Backup status  
- 🔍 Disk usage
- 📊 Overall health

## Connection Issues

### 1. "Connection Refused" Error

**Error Messages:**
```
Connection refused: connect
could not connect to server: Connection refused
```

**Diagnosis:**
```bash
# Check if PostgreSQL is running
docker-compose ps
sudo systemctl status postgresql  # For manual installations

# Check port availability
netstat -tlnp | grep 5432
lsof -i :5432
```

**Solutions:**

#### Docker Setup
```bash
# Start the database
docker-compose up -d postgres

# Check logs for errors
docker-compose logs postgres

# Restart if needed
docker-compose restart postgres
```

#### Manual Installation
```bash
# Start PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql  # Auto-start on boot

# Check PostgreSQL configuration
sudo -u postgres psql -c "SHOW config_file;"
```

### 2. "Authentication Failed" Error

**Error Messages:**
```
FATAL: password authentication failed for user "cm_app_user"
FATAL: Ident authentication failed for user "cm_app_user"
```

**Diagnosis:**
```bash
# Check environment variables
cat .env | grep DB_
echo $DB_APP_PASSWORD

# Test manual connection
psql -h localhost -p 5432 -U cm_app_user -d campus_marketplace
```

**Solutions:**

#### Reset Password
```bash
# Using Docker
docker-compose exec postgres psql -U postgres -c "
ALTER USER cm_app_user PASSWORD 'your_new_password';
"

# Update .env file
sed -i 's/DB_APP_PASSWORD=.*/DB_APP_PASSWORD=your_new_password/' .env
```

#### Check Authentication Configuration
```bash
# View pg_hba.conf (Docker)
docker-compose exec postgres cat /var/lib/postgresql/data/pg_hba.conf

# Should contain lines like:
# host all all all md5
```

### 3. "Database Does Not Exist" Error

**Error Messages:**
```
FATAL: database "campus_marketplace" does not exist
```

**Solutions:**
```bash
# Create database (Docker)
docker-compose exec postgres createdb -U postgres campus_marketplace

# Or recreate entire setup
docker-compose down -v  # WARNING: This deletes all data
docker-compose up -d

# Manual installation
sudo -u postgres createdb campus_marketplace
```

### 4. SSL/TLS Connection Issues

**Error Messages:**
```
FATAL: no pg_hba.conf entry for host
SSL error: certificate verify failed
```

**Solutions:**

#### Disable SSL for Development
```bash
# In .env file
SSL_MODE=disable
```

#### Configure SSL Properly
```bash
# Generate self-signed certificate (development only)
openssl req -new -x509 -days 365 -nodes -text \
  -out server.crt \
  -keyout server.key \
  -subj "/CN=localhost"

# Copy to PostgreSQL data directory (Docker)
docker cp server.crt campus_marketplace_db:/var/lib/postgresql/
docker cp server.key campus_marketplace_db:/var/lib/postgresql/
docker-compose restart postgres
```

## Performance Issues

### 1. Slow Queries

**Symptoms:**
- Application responds slowly
- Timeout errors
- High CPU usage on database

**Diagnosis:**
```bash
# Check for long-running queries
./db/scripts/monitor.sh --queries

# Check slow query log
docker-compose logs postgres | grep "duration:"
```

**Solutions:**

#### Enable Query Logging
```sql
-- Connect to database
docker-compose exec postgres psql -U postgres -d campus_marketplace

-- Enable slow query logging
ALTER SYSTEM SET log_min_duration_statement = 1000;  -- Log queries > 1 second
ALTER SYSTEM SET log_statement = 'all';               -- Log all statements (dev only)
SELECT pg_reload_conf();
```

#### Kill Long-Running Queries
```sql
-- Find long-running queries
SELECT pid, usename, state, query_start, query 
FROM pg_stat_activity 
WHERE state != 'idle' 
AND query_start < now() - interval '5 minutes';

-- Kill specific query
SELECT pg_terminate_backend(PID_HERE);
```

#### Add Missing Indexes
```sql
-- Check table scan statistics
SELECT schemaname, tablename, seq_scan, seq_tup_read, idx_scan, idx_tup_fetch
FROM pg_stat_user_tables 
ORDER BY seq_tup_read DESC;

-- Add indexes for frequently scanned tables
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_listings_status ON listings(status);
CREATE INDEX idx_listings_created_at ON listings(created_at);
```

### 2. Connection Pool Exhausted

**Error Messages:**
```
Unable to obtain connection from database pool within 30 seconds
HikariPool-1 - Connection is not available, request timed out after 30000ms
```

**Diagnosis:**
```bash
# Check connection usage
./db/scripts/monitor.sh --connections

# Check HikariCP metrics (if actuator enabled)
curl http://localhost:8080/api/actuator/metrics/hikaricp.connections.active
```

**Solutions:**

#### Increase Pool Size
```yaml
# In application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30  # Increase from 20
      connection-timeout: 30000
```

#### Find Connection Leaks
```sql
-- Check for idle in transaction connections
SELECT pid, usename, state, state_change, query
FROM pg_stat_activity 
WHERE state = 'idle in transaction'
AND state_change < now() - interval '1 minute';
```

#### Application Code Review
Look for:
- Unclosed connections
- Long-running transactions
- Missing `@Transactional` annotations
- Inefficient queries in loops

### 3. High Memory Usage

**Symptoms:**
- Out of memory errors
- System slowdown
- Docker container restarts

**Diagnosis:**
```bash
# Check memory usage
docker stats campus_marketplace_db
free -h  # System memory

# Check PostgreSQL memory settings
docker-compose exec postgres psql -U postgres -c "
SELECT name, setting, unit, short_desc 
FROM pg_settings 
WHERE name IN ('shared_buffers', 'work_mem', 'effective_cache_size', 'maintenance_work_mem');
"
```

**Solutions:**

#### Adjust PostgreSQL Memory Settings
```yaml
# In docker-compose.yml, modify the postgres command:
command: |
  postgres 
  -c shared_buffers=128MB          # Reduce from 256MB
  -c work_mem=2MB                  # Reduce from 4MB
  -c effective_cache_size=512MB    # Reduce from 1GB
  -c maintenance_work_mem=32MB     # Reduce from 64MB
```

#### Check for Memory Leaks
```sql
-- Check query memory usage
SELECT query, calls, mean_exec_time, rows, 
       100.0 * shared_blks_hit / nullif(shared_blks_hit + shared_blks_read, 0) AS hit_percent
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;
```

## Backup and Recovery Issues

### 1. Backup Script Failures

**Error Messages:**
```
pg_dump: error: connection to database "campus_marketplace" failed
Permission denied
```

**Diagnosis:**
```bash
# Test backup script
./db/scripts/backup.sh --test

# Check backup logs
cat db/backups/backup_errors.log
tail -f db/backups/backup.log
```

**Solutions:**

#### Fix Permissions
```bash
# Ensure backup directory is writable
chmod 755 db/backups/
mkdir -p db/backups/local/$(date +%Y/%m/%d)

# Check database permissions
docker-compose exec postgres psql -U postgres -c "
SELECT usename, usecreatedb, usesuper FROM pg_user WHERE usename = 'cm_app_user';
"
```

#### Manual Backup
```bash
# Create manual backup
docker-compose exec postgres pg_dump -U cm_app_user campus_marketplace > manual_backup.sql

# Compress backup
gzip manual_backup.sql
```

### 2. Restore Failures

**Error Messages:**
```
pg_restore: error: input file appears to be a text format dump
ERROR: relation "users" already exists
```

**Solutions:**

#### Text Format Restore
```bash
# For .sql files (text format)
gunzip -c backup_file.sql.gz | docker-compose exec -T postgres psql -U cm_app_user -d campus_marketplace

# For .dump files (binary format)
docker-compose exec postgres pg_restore -U cm_app_user -d campus_marketplace backup_file.dump
```

#### Clean Database Before Restore
```bash
# Drop and recreate database (WARNING: Destroys all data)
docker-compose exec postgres psql -U postgres -c "
DROP DATABASE campus_marketplace;
CREATE DATABASE campus_marketplace OWNER cm_app_user;
"

# Then restore
./db/scripts/restore.sh --latest
```

## Development Issues

### 1. H2 Console Not Accessible

**Symptoms:**
- 404 error when accessing `/h2-console`
- H2 console login fails

**Solutions:**
```yaml
# Ensure H2 console is enabled in application.yml (dev profile)
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: true
```

Access at: `http://localhost:8080/api/h2-console`

**Connection Settings:**
- JDBC URL: `jdbc:h2:mem:campusmarketplace`
- User Name: `sa`
- Password: `password`

### 2. Schema Validation Errors

**Error Messages:**
```
Schema-validation: missing table [users]
Schema-validation: wrong column type encountered in column [id] in table [users]
```

**Solutions:**

#### Development Mode
```yaml
# Use create-drop for development
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recreates schema on startup
```

#### Production Mode
```bash
# Run schema migration manually
docker-compose exec postgres psql -U cm_app_user -d campus_marketplace -f db/migrations/V1_0__initial_schema.sql

# Or update application to use 'update' mode temporarily
spring.jpa.hibernate.ddl-auto=update
```

### 3. Entity Mapping Issues

**Error Messages:**
```
org.hibernate.MappingException: Could not determine type
@JoinColumn references an unmapped column
```

**Solutions:**

#### Check Entity Annotations
```java
@Entity
@Table(name = "users")  // Ensure table name matches
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Use IDENTITY for PostgreSQL
    private Long id;
    
    @Column(name = "email", unique = true, nullable = false)  // Explicit column mapping
    private String email;
}
```

#### Verify Database Schema
```sql
-- Check actual table structure
\d users;
\d listings;

-- Verify foreign key relationships
SELECT 
    tc.constraint_name, 
    tc.table_name, 
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE constraint_type = 'FOREIGN KEY';
```

## Docker-Specific Issues

### 1. Container Won't Start

**Error Messages:**
```
database system is shut down
initdb: error: directory "/var/lib/postgresql/data" exists but is not empty
```

**Solutions:**

#### Clean Docker Volumes
```bash
# Stop containers
docker-compose down

# Remove volumes (WARNING: Deletes all data)
docker-compose down -v

# Rebuild and start
docker-compose up -d

# Check status
docker-compose ps
```

#### Check Docker Resources
```bash
# Check available disk space
docker system df

# Clean up unused resources
docker system prune -f

# Check memory/CPU limits
docker stats
```

### 2. Port Conflicts

**Error Messages:**
```
bind: address already in use
port is already allocated
```

**Solutions:**
```bash
# Find process using port 5432
lsof -i :5432
netstat -tulpn | grep 5432

# Kill conflicting process or change port in docker-compose.yml
ports:
  - "5433:5432"  # Use different host port

# Update connection string
DB_PORT=5433
```

### 3. pgAdmin Connection Issues

**Symptoms:**
- Can't connect to PostgreSQL from pgAdmin
- "Server not found" error

**Solutions:**

#### Use Correct Connection Settings
In pgAdmin:
- **Host**: `postgres` (container name, not localhost)
- **Port**: `5432`
- **Username**: `cm_app_user`
- **Password**: From `.env` file

#### Network Issues
```bash
# Check if containers are on same network
docker network ls
docker network inspect team-project-cmpe202-03-fall2025-commandlinecommando_campus_marketplace_network

# Restart pgAdmin container
docker-compose restart pgadmin
```

## Environment-Specific Issues

### 1. Production Deployment

**Common Issues:**
- SSL certificate problems
- Firewall blocking connections
- Resource limits

**Pre-deployment Checklist:**
```bash
# Verify environment variables
env | grep DB_

# Test connection from application server
telnet db_host 5432

# Check firewall rules
sudo ufw status
sudo iptables -L

# Verify SSL certificates
openssl s_client -connect db_host:5432 -starttls postgres
```

### 2. Load Balancer/Proxy Issues

**Error Messages:**
```
Connection reset by peer
Unexpected packet type during COPY
```

**Solutions:**
- Configure connection pooling properly
- Set appropriate timeouts
- Use connection validation queries
- Enable keep-alive settings

```yaml
spring:
  datasource:
    hikari:
      connection-test-query: SELECT 1
      validation-timeout: 3000
      keepalive-time: 300000
```

## Getting More Help

### Enable Debug Logging

```yaml
# In application.yml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    com.zaxxer.hikari: DEBUG
    org.springframework.jdbc: DEBUG
```

### Collect Diagnostic Information

```bash
# System information
uname -a
docker --version
docker-compose --version

# Database status
./db/scripts/monitor.sh --full > diagnostic_report.txt

# Application logs
tail -100 backend/logs/campus-marketplace.log

# Docker logs
docker-compose logs --tail=100 postgres > postgres_logs.txt
```

### Contact Support

When reporting issues, include:

1. **Error message** (full stack trace)
2. **Steps to reproduce**
3. **Environment details** (OS, Docker version, etc.)
4. **Configuration files** (sanitized, no passwords)
5. **Diagnostic report** from monitor script
6. **Recent changes** to code or configuration

---

**Remember**: Always backup your data before attempting major fixes or changes!
