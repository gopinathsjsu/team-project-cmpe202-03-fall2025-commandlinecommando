# Campus Marketplace - Unified Backend Deployment Guide

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 21 (for local development)
- PostgreSQL 16 (if not using Docker)
- Redis 7 (if not using Docker)

## Deployment Options

### Option 1: Docker Compose (Recommended)

**Complete environment with frontend, backend, AI service, database, and cache:**

```bash
# 1. Clone the repository
git clone <repository-url>
cd Project

# 2. Create environment file (optional - defaults are already set)
cp env.example .env
# Edit .env to override any defaults if needed

# 3. Build and start all services
docker-compose -f docker-compose.prod.yml up --build -d

# 4. Wait for services to be healthy
# Frontend will be available at http://localhost
# Backend will be available at http://localhost:8080
# AI Service will be available at http://localhost:3001
# Database migrations run automatically via Flyway
```

**Services Started:**
- Frontend (Nginx): `http://localhost`
- Backend (Spring Boot): `http://localhost:8080`
- AI Integration Service: `http://localhost:3001`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`

**Default Credentials:**
- Database User: `cm_app_user`
- Database Password: `changeme` (default, change in production!)
- Database Name: `campus_marketplace`

### Option 2: Local Development with Docker Database

**Run backend locally while using Docker for PostgreSQL and Redis:**

```bash
# 1. Start only the database and Redis (without backend)
docker-compose -f docker-compose.prod.yml up -d postgres redis

# 2. Wait for database to be ready
sleep 5

# 3. Build the application
cd backend
./mvnw clean install

# 4. Run the application with production profile
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run

# Application will run on http://localhost:8080
# Flyway migrations run automatically on startup
# Press Ctrl+C to stop
```

The `./mvnw flyway:migrate` command isn't needed since Spring Boot runs migrations automatically on startup.

## Environment Variables

All environment variables have defaults set in `env.example`, so you can deploy right away. Create a `.env` file if you want to override any values:

```bash
# Copy template (defaults are already set)
cp env.example .env

# Database Configuration (defaults: cm_app_user / changeme)
DB_APP_USER=cm_app_user
DB_APP_PASSWORD=your_secure_password_here
DB_PORT=5432

# Application Port
APP_PORT=8080

# JWT Configuration (default is set)
JWT_SECRET=your_very_long_secure_secret_key_here_at_least_256_bits
JWT_ACCESS_TOKEN_EXPIRATION=3600000    # 1 hour in milliseconds
JWT_REFRESH_TOKEN_EXPIRATION=604800000 # 7 days in milliseconds

# AWS S3 Configuration (defaults are set)
AWS_S3_BUCKET_NAME=webapp-s3-bucket-2025
AWS_REGION=us-west-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key

# Email Configuration (defaults are set)
EMAIL_NOTIFICATIONS_ENABLED=true
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USERNAME=apikey
SMTP_PASSWORD=your-sendgrid-api-key
EMAIL_FROM=commandline-commandos@seasonsanta.com

# OpenAI Configuration (default is set)
OPENAI_API_KEY=your-openai-api-key

# File Upload Configuration
FILE_UPLOAD_MAX_SIZE=10485760  # 10MB in bytes
FILE_UPLOAD_DIR=./uploads

# Redis Configuration
REDIS_PORT=6379

# Logging
LOG_LEVEL=INFO
SQL_LOG_LEVEL=WARN
```

## Database Migration

### Automatic Migration (Recommended)
Flyway migrations run automatically when the Spring Boot backend starts. This works for both Docker and local development.

### Manual Migration (Docker)
If you need to run migrations manually using Docker:

```bash
# Access the database container
docker-compose -f docker-compose.prod.yml exec postgres psql -U cm_app_user -d campus_marketplace

# Or run SQL files directly
docker-compose -f docker-compose.prod.yml exec -T postgres psql -U cm_app_user -d campus_marketplace < backend/src/main/resources/db/migrations/V1__campus_marketplace_core_schema.sql
```

### Check Migration Status
```bash
# Query Flyway history table
docker-compose -f docker-compose.prod.yml exec postgres psql -U cm_app_user -d campus_marketplace -c "SELECT version, description, success, installed_on FROM flyway_schema_history ORDER BY installed_rank;"
```

### Migration History
The V8 migration (`db/migrations/V8__unify_schemas.sql`) performs:
1. Creates backup tables
2. Converts BIGINT IDs to UUID
3. Renames `products` to `listings`
4. Adds foreign key constraints
5. Validates data integrity

## API Endpoints

All backend endpoints are under `http://localhost:8080/api`
AI service endpoints are under `http://localhost:3001/api`
Frontend is available at `http://localhost`

### Authentication & Users
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Refresh token
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update profile

### Listings
- `GET /api/listings` - Get all listings
- `GET /api/listings/{id}` - Get listing by ID
- `POST /api/listings` - Create new listing
- `PUT /api/listings/{id}` - Update listing
- `DELETE /api/listings/{id}` - Delete listing
- `GET /api/listings/search` - Search listings

### Reports
- `POST /api/reports` - Create report
- `GET /api/reports` - Get all reports (admin)
- `PUT /api/reports/{id}` - Update report status (admin)

### Communication
- `GET /api/conversations` - Get user's conversations
- `POST /api/conversations` - Start new conversation
- `GET /api/conversations/{id}/messages` - Get messages
- `POST /api/messages` - Send message
- `GET /api/notifications/preferences` - Get notification preferences
- `PUT /api/notifications/preferences` - Update preferences

### Orders (Existing)
- `POST /api/orders` - Create order
- `GET /api/orders` - Get user's orders
- `GET /api/orders/{id}` - Get order details

### Admin (Existing)
- `GET /api/admin/users` - List all users
- `GET /api/admin/statistics` - Platform statistics

## Health Check

```bash
# Check if backend is running
curl http://localhost:8080/api/actuator/health

# Expected response:
# {"status":"UP"}
```

## Troubleshooting

### Backend won't start
```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs backend

# Common issues:
# 1. Database not ready - wait for health check
# 2. Port 8080 already in use - stop other services
# 3. Invalid JWT_SECRET - must be at least 256 bits (default is set)
```

### Database connection failed
```bash
# Verify PostgreSQL is running
docker-compose -f docker-compose.prod.yml ps postgres

# Test connection
docker-compose -f docker-compose.prod.yml exec postgres psql -U cm_app_user -d campus_marketplace

# Check database logs
docker-compose -f docker-compose.prod.yml logs postgres
```

### Migration failed
```bash
# Check migration status
cd backend
./mvnw flyway:info

# View migration details
docker-compose -f docker-compose.prod.yml exec postgres psql -U cm_app_user -d campus_marketplace -c "SELECT * FROM flyway_schema_history;"

# If V8 migration failed, check for:
# 1. Existing data conflicts
# 2. Missing tables
# 3. Database permissions
```

### Redis connection issues
```bash
# Check Redis status
docker-compose -f docker-compose.prod.yml exec redis redis-cli ping
# Should return: PONG

# View Redis logs
docker-compose -f docker-compose.prod.yml logs redis
```

### File upload errors
```bash
# Ensure upload directory exists and has write permissions
mkdir -p uploads
chmod 755 uploads

# In Docker, check volume mount
docker volume inspect project-file-uploads
```

## Testing the Deployment

### 1. Health Check
```bash
curl http://localhost:8080/api/actuator/health
```

### 2. Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

### 3. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 4. Create a Listing
```bash
# Save the JWT token from login response
TOKEN="your-jwt-token-here"

curl -X POST http://localhost:8080/api/listings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Listing",
    "description": "A test listing",
    "price": 99.99,
    "category": "ELECTRONICS"
  }'
```

## Production Deployment

### Security Checklist
- [ ] Change all default passwords
- [ ] Use strong JWT_SECRET (minimum 256 bits)
- [ ] Enable HTTPS/TLS
- [ ] Configure firewall rules
- [ ] Set up proper logging and monitoring
- [ ] Enable database backups
- [ ] Use environment-specific configuration
- [ ] Implement rate limiting
- [ ] Set up CDN for static files
- [ ] Configure proper CORS settings

### Performance Tuning
```yaml
# In application.yml or via environment variables
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

  cache:
    redis:
      time-to-live: 600000  # 10 minutes
```

### Monitoring
- Frontend logs: `docker-compose -f docker-compose.prod.yml logs -f frontend`
- Backend logs: `docker-compose -f docker-compose.prod.yml logs -f backend`
- AI Service logs: `docker-compose -f docker-compose.prod.yml logs -f ai-integration-server`
- Database logs: `docker-compose -f docker-compose.prod.yml logs -f postgres`
- Redis logs: `docker-compose -f docker-compose.prod.yml logs -f redis`

### Backup Strategy
```bash
# Database backup
docker-compose -f docker-compose.prod.yml exec postgres pg_dump -U cm_app_user campus_marketplace > backup_$(date +%Y%m%d).sql

# Restore from backup
docker-compose -f docker-compose.prod.yml exec -T postgres psql -U cm_app_user campus_marketplace < backup_20250110.sql

# Redis backup (automatic via AOF persistence)
docker-compose -f docker-compose.prod.yml exec redis redis-cli BGSAVE
```

## Scaling Considerations

### Horizontal Scaling
The unified backend can be scaled horizontally:

```bash
# Scale backend to 3 instances
docker-compose -f docker-compose.prod.yml up -d --scale backend=3

# For AWS deployment with ALB, see AWS_DEPLOYMENT_GUIDE.md
```

### Database Scaling
- Use connection pooling (HikariCP configured)
- Consider read replicas for heavy read workloads
- Implement database partitioning for large datasets

### Cache Optimization
- Redis already configured for session storage
- Add application-level caching with Caffeine
- Implement cache warming strategies

## Support

For issues or questions:
1. Check logs: `docker-compose -f docker-compose.prod.yml logs`
2. Review documentation: `/docs` directory
3. Check migration status: `./mvnw flyway:info`
4. See [AWS_DEPLOYMENT_GUIDE.md](AWS_DEPLOYMENT_GUIDE.md) for EC2 deployment
5. Contact development team

---

**Version**: 1.0.0 (Unified Architecture)
