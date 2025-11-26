# Campus Marketplace - Unified Backend

A secure and scalable marketplace platform for university students to buy and sell items within their campus community.

**🎉 Recently Refactored**: Consolidated 3 microservices into a unified backend architecture for improved maintainability and performance.

## Team Name
**Commandline Commandos**

## Team Member Names
1. Vineet Kumar
2. Sakshat Patil
3. Wilson Huang
4. Lam Nguyen

## Quick Start

### Option 1: Docker Compose (Recommended)
```bash
# Start all services (PostgreSQL, Redis, Backend)
docker-compose up --build

# Backend will be available at http://localhost:8080
# Flyway migrations run automatically
```

### Option 2: Local Development
```bash
# 1. Start database services
docker-compose up -d postgres redis

# 2. Run database migrations (first time only)
cd backend
./mvnw flyway:migrate

# 3. Start unified backend
./mvnw spring-boot:run

# Backend runs on: http://localhost:8080
```

### Access Services
- **Unified Backend API**: http://localhost:8080/api
  - Authentication: `/api/auth/*`
  - Users: `/api/users/*`
  - Listings: `/api/listings/*`
  - Reports: `/api/reports/*`
  - Communication: `/api/conversations/*`, `/api/messages/*`
  - Orders: `/api/orders/*`
  - Admin: `/api/admin/*`
- **Health Check**: http://localhost:8080/api/actuator/health
- **PostgreSQL**: localhost:5432 (database: `campus_marketplace`)
- **Redis**: localhost:6379

## Project Structure

```
├── backend/                 # Unified Spring Boot application (Port 8080)
│   ├── src/main/java/com/commandlinecommandos/campusmarketplace/
│   │   ├── auth/           # Authentication & JWT
│   │   ├── user/           # User management
│   │   ├── listing/        # Listing management (merged from listing-api)
│   │   ├── communication/  # Chat & messaging (merged from communication)
│   │   ├── order/          # Order processing
│   │   ├── admin/          # Admin operations
│   │   ├── security/       # Security configuration
│   │   ├── config/         # Application configuration
│   │   ├── exception/      # Unified exception handling
│   │   └── dto/            # Data transfer objects
│   ├── src/main/resources/
│   │   └── application.yml # Unified configuration
│   └── pom.xml            # Consolidated dependencies (Java 21)
├── frontend/               # React/Vite frontend
├── db/                    # Database infrastructure
│   ├── migrations/        # Flyway migrations (V1-V8)
│   ├── scripts/           # Backup & monitoring utilities
│   └── docs/             # Database documentation
├── docker-compose.yml    # Unified deployment (3 services: postgres, redis, backend)
├── .archive/             # Archived pre-refactoring code
│   └── pre-refactoring-YYYYMMDD/
│       ├── listing-api/   # Old listing microservice (archived)
│       ├── communication/ # Old communication microservice (archived)
│       └── sql_files/     # Old manual SQL schemas (archived)
├── DEPLOYMENT_GUIDE.md   # Deployment instructions
├── REFACTORING_SUMMARY.md # Refactoring documentation
└── refactor_plan.md      # Original refactoring plan
```

## Architecture

### Unified Backend (Single Service)
The application has been refactored from 3 separate microservices into a single, modular monolith:

**Before (3 Services)**:
- Backend (8080): User management, authentication, orders
- Listing API (8100): Listings, reports, search
- Communication (8200): Chat, messages, notifications

**After (1 Service)**:
- Unified Backend (8080): All functionality in modular packages

**Benefits**:
- ✅ Eliminated ~734 lines of duplicate code
- ✅ Single database with proper foreign key constraints
- ✅ UUID-based IDs throughout (no lossy conversions)
- ✅ Unified exception handling and security
- ✅ Simplified deployment (1 service instead of 3)
- ✅ No inter-service HTTP calls
- ✅ Single configuration file

## Features

### 🏗️ Unified Architecture
- **Single Codebase**: All functionality in one application
- **Modular Design**: Organized into domain packages (auth, user, listing, communication, order, admin)
- **Flyway Migrations**: Automated database schema management (V1-V8)
- **UUID-Based IDs**: Consistent UUID usage across all tables
- **Foreign Key Constraints**: Proper referential integrity

### 🗄️ Database Infrastructure
- **PostgreSQL 16** with connection pooling (HikariCP)
- **Flyway Migrations**: V8 migration consolidates schemas with UUID conversion
- **Foreign Keys**: 6 constraints ensure data integrity
- **Automated backups** with retention policies
- **Real-time monitoring** and health checks

### 🔐 Security Features
- **JWT Authentication**: Token-based with refresh tokens
- **Role-based Access Control**: Student and Admin roles
- **UUID-based Authorization**: Proper owner verification
- **Session Management**: Redis-backed sessions
- **Unified Exception Handling**: 20+ exception handlers

### 📦 Core Features
- **User Management**: Registration, login, profile management
- **Listing Management**: CRUD operations with search and filtering
- **Communication**: Real-time chat between buyers and sellers
- **Report System**: Content moderation with admin dashboard
- **Order Processing**: Order creation and tracking
- **File Upload**: Image upload for listings
- **Email Notifications**: SMTP-based notifications

## Database Management

### Quick Commands
```bash
# Run Flyway migrations
cd backend
./mvnw flyway:migrate

# Check migration status
./mvnw flyway:info

# Health check (if using provided scripts)
./db/scripts/monitor.sh --health

# Create backup (if using provided scripts)
./db/scripts/backup.sh
```

### Database Schema
- **Managed by**: Flyway migrations in `db/migrations/`
- **Latest Version**: V8 (Schema unification with UUID conversion)
- **Tables**: users, listings, conversations, messages, orders, reports, etc.
- **All IDs**: UUID (no BIGINT)

## Documentation

### Getting Started
- **[🚀 Deployment Guide](DEPLOYMENT_GUIDE.md)** - Complete deployment instructions
- **[📋 Refactoring Summary](REFACTORING_SUMMARY.md)** - Details of the consolidation effort
- **[📝 Refactor Plan](refactor_plan.md)** - Original refactoring plan

### Database Documentation
- **[📚 Team Setup Guide](db/docs/TEAM_SETUP_GUIDE.md)** - Comprehensive setup guide
- **[⚡ Quick Reference](db/docs/QUICK_REFERENCE.md)** - Daily commands
- **[✅ Onboarding Checklist](db/docs/ONBOARDING_CHECKLIST.md)** - New developer checklist
- **[🔧 Database Setup](db/docs/DATABASE_SETUP.md)** - Setup instructions
- **[🚨 Troubleshooting](db/docs/TROUBLESHOOTING.md)** - Common issues

### API Documentation
All endpoints available at `http://localhost:8080/api`:
- **Authentication**: `/auth/*` - Login, register, refresh tokens
- **Users**: `/users/*` - Profile management
- **Listings**: `/listings/*` - Product listings with search
- **Reports**: `/reports/*` - Content moderation
- **Communication**: `/conversations/*`, `/messages/*` - Buyer-seller chat
- **Orders**: `/orders/*` - Order processing
- **Admin**: `/admin/*` - Admin operations

## Archived Code

The previous microservices architecture has been archived in `.archive/pre-refactoring-YYYYMMDD/`:
- `listing-api/` - Old listing microservice (port 8100)
- `communication/` - Old communication microservice (port 8200)  
- `sql_files/` - Old manual SQL schemas (replaced by Flyway)

These are kept for reference but are no longer used in development.

## Summary of areas of contributions : (Per Member)

1. **Setup Environment**
   ```bash
   # Start database services
   docker-compose up -d postgres redis
   
   # Wait for services to be ready
   docker-compose ps
   ```

2. **Run Migrations** (First time only)
   ```bash
   cd backend
   ./mvnw flyway:migrate
   ```

3. **Start Development**
   ```bash
   # Start unified backend
   ./mvnw spring-boot:run
   
   # Backend available at http://localhost:8080
   ```

4. **Test API**
   ```bash
   # Health check
   curl http://localhost:8080/api/actuator/health
   
   # Register a user
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"password123","fullName":"Test User"}'
   ```

## Troubleshooting

### Common Issues
1. **Port 8080 already in use**: Stop other services or change port in `application.yml`
2. **Database connection failed**: Ensure PostgreSQL is running: `docker-compose ps`
3. **Migration failed**: Check Flyway status: `./mvnw flyway:info`

### Quick Fixes
```bash
# Restart all services
docker-compose restart

# View logs
docker-compose logs -f backend

# Reset database (WARNING: deletes all data)
docker-compose down -v
docker-compose up -d postgres redis
cd backend && ./mvnw flyway:migrate
```

## Archived Code

The previous microservices architecture has been archived in `.archive/pre-refactoring-YYYYMMDD/`:
- `listing-api/` - Old listing microservice (port 8100)
- `communication/` - Old communication microservice (port 8200)
- `sql_files/` - Old manual SQL schemas (replaced by Flyway)

These are kept for reference but are no longer used in development.

## Summary of areas of contributions : (Per Member)

## Link to Project Journal

## Team Google Sheet or Project Board : (Product Backlog and Sprint Backlog for each Sprint)

[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/kvgvOCnV)
