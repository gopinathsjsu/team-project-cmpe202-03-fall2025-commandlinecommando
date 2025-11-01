# Campus Marketplace Database - Team Setup Guide

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Initial Setup](#initial-setup)
3. [Database Services](#database-services)
4. [Spring Boot Configuration](#spring-boot-configuration)
5. [Development Workflow](#development-workflow)
6. [Testing & Validation](#testing--validation)
7. [Troubleshooting](#troubleshooting)
8. [Best Practices](#best-practices)
9. [Advanced Usage](#advanced-usage)

---

## Prerequisites

Before starting, ensure you have the following installed on your development machine:

### Required Software
- **Docker Desktop** (v4.0+) - [Download](https://www.docker.com/products/docker-desktop/)
- **Docker Compose** (v2.0+) - Usually included with Docker Desktop
- **Git** - For version control
- **Java 17+** - For Spring Boot development
- **Maven** (optional) - Build tool for Java projects

### Verify Prerequisites
```bash
# Check Docker installation
docker --version
docker-compose --version

# Check Java installation
java --version

# Verify Docker is running
docker ps
```

---

## Initial Setup

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd team-project-cmpe202-03-fall2025-commandlinecommando
```

### Step 2: Environment Configuration
```bash
# Copy the environment template
cp .env.template .env

# Edit the .env file with your preferred settings (optional)
# The default values are suitable for development
nano .env
```

### Step 3: Start the Database Environment
```bash
# Start all database services
docker-compose up -d

# Verify services are running
docker-compose ps
```

Expected output:
```
NAME                         IMAGE                   STATUS
campus_marketplace_db        postgres:15-alpine      Up (healthy)
campus_marketplace_pgadmin   dpage/pgadmin4:latest   Up
campus_marketplace_redis     redis:7-alpine          Up
```

### Step 4: Validate Setup
```bash
# Run the validation script
cd db/scripts
./validate-connection.sh
```

You should see all green checkmarks ✅ indicating successful setup.

---

## Database Services

### PostgreSQL Database
- **Host**: localhost
- **Port**: 5432
- **Database**: `campusmarketplace_db`
- **Users**:
  - `postgres` (admin): `postgres_admin_2024!`
  - `cm_app_user` (application): `campusapp2024`
  - `cm_readonly` (read-only): `readonly2024`

### pgAdmin Web Interface
- **URL**: http://localhost:8080
- **Email**: `admin@campusmarketplace.com`
- **Password**: `pgadmin2024`

### Redis Cache
- **Host**: localhost
- **Port**: 6379
- **No authentication required**

---

## Spring Boot Configuration

### Environment Profiles

The application supports multiple environments:

#### Development Profile (Default)
- Uses **H2 in-memory database**
- Automatic table creation
- SQL logging enabled
- H2 console: http://localhost:8080/api/h2-console

```bash
# Run with development profile
SPRING_PROFILES_ACTIVE=dev java -jar target/campusmarketplace.jar
```

#### Production Profile
- Uses **PostgreSQL database**
- Connects to Docker container
- Production-optimized settings

```bash
# Run with production profile
SPRING_PROFILES_ACTIVE=prod java -jar target/campusmarketplace.jar
```

#### Test Profile
- Uses **isolated H2 database**
- Clean database for each test run
- Optimized for testing

```bash
# Run tests
SPRING_PROFILES_ACTIVE=test mvn test
```

### Connection Configuration

The application automatically configures database connections based on environment variables:

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_HOST` | localhost | Database host |
| `DB_PORT` | 5432 | Database port |
| `DB_APP_USER` | cm_app_user | Application user |
| `DB_APP_PASSWORD` | campusapp2024 | Application password |

---

## Development Workflow

### Daily Development Routine

#### 1. Start Your Day
```bash
# Navigate to project directory
cd team-project-cmpe202-03-fall2025-commandlinecommando

# Start database services
docker-compose up -d

# Verify everything is running
./db/scripts/validate-connection.sh
```

#### 2. Development with H2 (Recommended for Development)
```bash
cd backend

# Start application with H2 database
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# Access H2 console at: http://localhost:8080/api/h2-console
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: (leave blank)
```

#### 3. Testing with PostgreSQL
```bash
cd backend

# Run with PostgreSQL for integration testing
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

#### 4. End of Day Cleanup
```bash
# Stop services to free up resources
docker-compose stop

# Or completely remove containers and volumes
docker-compose down -v
```

### Database Management

#### View Database in pgAdmin
1. Open http://localhost:8080
2. Login with admin credentials
3. Add new server:
   - **Name**: Campus Marketplace
   - **Host**: postgres (Docker service name)
   - **Port**: 5432
   - **Username**: cm_app_user
   - **Password**: campusapp2024

#### Direct Database Access
```bash
# Connect as application user
docker exec -it campus_marketplace_db psql -U cm_app_user -d campusmarketplace_db

# Connect as admin
docker exec -it campus_marketplace_db psql -U postgres -d campusmarketplace_db

# Connect as read-only user
docker exec -it campus_marketplace_db psql -U cm_readonly -d campusmarketplace_db
```

#### Common Database Operations
```sql
-- View all tables
\dt

-- Describe table structure
\d table_name

-- View database users
SELECT rolname, rolcanlogin, rolsuper FROM pg_roles;

-- Check database size
SELECT pg_size_pretty(pg_database_size('campusmarketplace_db'));
```

---

## Testing & Validation

### Automated Validation
```bash
# Full validation suite
cd db/scripts
./validate-connection.sh

# Check specific services
docker-compose ps
docker-compose logs postgres
```

### Manual Testing

#### Test Database Connections
```bash
# Test application user permissions
docker exec -it campus_marketplace_db psql -U cm_app_user -d campusmarketplace_db -c "
CREATE TABLE test_permissions (id SERIAL, data VARCHAR(50));
INSERT INTO test_permissions (data) VALUES ('Test data');
SELECT * FROM test_permissions;
DROP TABLE test_permissions;
"
```

#### Test Spring Boot Connection
```bash
cd backend

# Test with production profile
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run

# Check application logs for successful database connection
```

### Performance Testing

#### Connection Pool Monitoring
```bash
# Monitor active connections
docker exec -it campus_marketplace_db psql -U postgres -c "
SELECT 
    pid,
    usename,
    application_name,
    client_addr,
    backend_start,
    state
FROM pg_stat_activity 
WHERE datname = 'campusmarketplace_db';
"
```

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Services Won't Start
```bash
# Check if ports are in use
lsof -i :5432  # PostgreSQL
lsof -i :8080  # pgAdmin
lsof -i :6379  # Redis

# Stop conflicting services
sudo kill -9 <PID>

# Restart Docker Desktop
# Use Docker Desktop GUI or restart service
```

#### 2. Database Connection Refused
```bash
# Check container status
docker-compose ps

# Check container logs
docker-compose logs postgres

# Restart database service
docker-compose restart postgres

# Reset everything
docker-compose down -v
docker-compose up -d
```

#### 3. Spring Boot Can't Connect
```bash
# Verify environment variables
source .env
echo $DB_APP_PASSWORD

# Check application.yml configuration
cat backend/src/main/resources/application.yml

# Test direct connection
docker exec -it campus_marketplace_db psql -U cm_app_user -d campusmarketplace_db
```

#### 4. pgAdmin Login Issues
```bash
# Reset pgAdmin container
docker-compose stop pgadmin
docker-compose rm pgadmin
docker-compose up -d pgadmin

# Check pgAdmin logs
docker-compose logs pgadmin
```

#### 5. Performance Issues
```bash
# Monitor resource usage
docker stats

# Check PostgreSQL performance
docker exec -it campus_marketplace_db psql -U postgres -c "
SELECT 
    query,
    calls,
    total_time,
    mean_time
FROM pg_stat_statements 
ORDER BY total_time DESC LIMIT 10;
"
```

### Debug Commands

```bash
# View all containers
docker ps -a

# Check container resource usage
docker stats

# View container logs
docker-compose logs <service-name>

# Enter container shell
docker exec -it campus_marketplace_db sh

# Check network connectivity
docker network ls
docker network inspect team-project-cmpe202-03-fall2025-commandlinecommando_campus_marketplace_network
```

---

## Best Practices

### Development Guidelines

#### 1. Environment Management
- Always use **development profile (H2)** for daily development
- Use **production profile (PostgreSQL)** for integration testing
- Never commit sensitive credentials to git

#### 2. Database Schema Changes
- Use **JPA/Hibernate migrations** for schema changes
- Test schema changes with both H2 and PostgreSQL
- Always backup before major schema changes

#### 3. Connection Management
- Let HikariCP handle connection pooling
- Don't create manual database connections in code
- Monitor connection usage in production

#### 4. Security Practices
- Use environment variables for credentials
- Rotate passwords regularly
- Use read-only user for reporting queries
- Enable SSL in production environments

### Performance Optimization

#### 1. Query Optimization
- Use proper database indexes
- Monitor slow queries with pgAdmin
- Use connection pooling effectively

#### 2. Resource Management
- Monitor container resource usage
- Adjust PostgreSQL memory settings as needed
- Clean up test data regularly

#### 3. Monitoring
- Use the validation script regularly
- Monitor database size growth
- Set up alerts for connection pool exhaustion

---

## Advanced Usage

### Custom Configuration

#### Modify PostgreSQL Settings
Edit `docker-compose.yml` to adjust PostgreSQL parameters:
```yaml
postgres:
  command: >
    postgres 
    -c max_connections=50          # Increase connections
    -c shared_buffers=512MB        # Increase memory
    -c effective_cache_size=2GB    # Adjust cache size
```

#### Add Database Extensions
```sql
-- Connect as admin and add extensions
docker exec -it campus_marketplace_db psql -U postgres -d campusmarketplace_db

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable full-text search
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
```

### Backup and Recovery

#### Automated Backups
```bash
# Run backup script
cd db/scripts
./backup-database.sh

# Schedule with cron (Linux/Mac)
crontab -e
# Add: 0 2 * * * /path/to/backup-database.sh
```

#### Manual Backup/Restore
```bash
# Create backup
docker exec -t campus_marketplace_db pg_dump -U postgres campusmarketplace_db > backup.sql

# Restore from backup
docker exec -i campus_marketplace_db psql -U postgres campusmarketplace_db < backup.sql
```

### Multi-Environment Setup

#### Staging Environment
Create `.env.staging`:
```bash
DB_HOST=staging-db.company.com
DB_PORT=5432
DB_APP_PASSWORD=staging-password-here
```

#### Production Deployment
For production deployment:
1. Use external PostgreSQL service (AWS RDS, Google Cloud SQL)
2. Enable SSL certificates
3. Configure proper security groups
4. Set up monitoring and alerting

---

## Team Collaboration

### Git Workflow
- Database scripts are version controlled
- Environment templates are included
- Database migrations are tracked

### Code Reviews
- Review database schema changes
- Validate connection configurations
- Test with both development and production profiles

### Documentation Updates
- Update this guide when making database changes
- Document new environment variables
- Share troubleshooting solutions

---

## Support and Resources

### Getting Help
1. **First**: Run validation script and check logs
2. **Documentation**: Refer to files in `db/docs/`
3. **Team**: Ask team members for assistance
4. **Stack Overflow**: Search for specific error messages

### Useful Resources
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Boot Data JPA](https://spring.io/projects/spring-data-jpa)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)
- [Docker Compose Reference](https://docs.docker.com/compose/)

### Quick Reference Commands
```bash
# Start everything
docker-compose up -d

# Stop everything
docker-compose stop

# Reset everything
docker-compose down -v && docker-compose up -d

# Validate setup
./db/scripts/validate-connection.sh

# View logs
docker-compose logs postgres

# Connect to database
docker exec -it campus_marketplace_db psql -U cm_app_user -d campusmarketplace_db
```

---

**Happy Development! 🚀**

*This guide is maintained by the Campus Marketplace development team. Please update it when making database-related changes.*