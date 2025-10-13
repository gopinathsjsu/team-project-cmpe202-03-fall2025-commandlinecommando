# Campus Marketplace

A secure and scalable marketplace platform for university students to buy and sell items within their campus community.

## Team Name
**Commandline Commandos**

## Team Member Names
1. Vineet Kumar
2. Sakshat Patil
3. Wilson Huang
4. Lam Nguyen

## Quick Start

### Database Setup
```bash
# Quick database setup (PostgreSQL + pgAdmin)
./setup-database.sh

# Or manual setup
cp .env.template .env  # Edit with your passwords
docker-compose up -d
```

### Run Application
```bash
# Development mode (H2 database)
cd backend
./mvnw spring-boot:run

# Production mode (PostgreSQL)
./mvnw spring-boot:run -Dspring.profiles.active=prod

# Listing API microservice
cd listing-api
./mvnw spring-boot:run
```

### Access Services
- **Main Application**: http://localhost:8080/api
- **Listing API**: http://localhost:8100/api
- **pgAdmin**: http://localhost:8080 (database management)
- **H2 Console**: http://localhost:8080/api/h2-console (development only)

## Project Structure

```
├── backend/                 # Main Spring Boot application
│   ├── src/main/java/      # Application source code
│   ├── src/main/resources/ # Configuration files
│   └── pom.xml            # Maven dependencies
├── listing-api/            # Listing Management microservice
│   ├── src/main/java/      # Listing API source code
│   ├── src/main/resources/ # Configuration files
│   └── pom.xml            # Maven dependencies
├── frontend/               # React/Vite frontend
├── db/                    # Database infrastructure
│   ├── scripts/           # Backup, monitoring, and utility scripts
│   ├── docs/             # Database documentation
│   └── migrations/       # Database schema changes
├── documentation/         # Project documentation
├── docker-compose.yml    # PostgreSQL, pgAdmin, Redis services
├── setup-database.sh     # Database quick start script
└── .env.template        # Environment variables template
```

## Features

### 🗄️ Database Infrastructure
- **PostgreSQL 15+** with connection pooling (HikariCP)
- **Multi-environment support** (dev: H2, prod: PostgreSQL)
- **Automated backups** with retention policies
- **Real-time monitoring** and health checks
- **Security hardened** with SSL/TLS and audit logging

### 🔧 Development Tools
- **pgAdmin** for database management
- **Health monitoring** scripts with status indicators
- **Automated backup/restore** capabilities
- **Performance monitoring** and optimization

### 🚀 Application Features
- **JWT-based authentication and authorization**
- **Role-based access control** (Student, Admin)
- **File upload support** for product images
- **RESTful API** with comprehensive error handling
- **Session management** with Redis support
- **Advanced Listing Management** with search and filtering
- **Report Management System** for content moderation
- **Image Management** with multiple upload support
- **Admin Dashboard** with report analytics

## Database Management

### Quick Commands
```bash
# Health check
./db/scripts/monitor.sh --health

# Create backup
./db/scripts/backup.sh

# Restore from backup
./db/scripts/restore.sh --latest

# Performance monitoring
./db/scripts/monitor.sh --performance
```

### Database Users
| User | Purpose | Permissions |
|------|---------|-------------|
| `cm_app_user` | Application operations | Full read/write access |
| `cm_readonly` | Analytics/reporting | Read-only access |

### Environment Profiles
- **Development**: H2 in-memory database with auto-schema creation
- **Production**: PostgreSQL with connection pooling and SSL
- **Testing**: Isolated H2 database with security disabled

## Documentation

### Database Documentation
- **[📚 Complete Team Setup Guide](db/docs/TEAM_SETUP_GUIDE.md)** - Comprehensive guide for teams to set up and use the database
- **[⚡ Quick Reference Card](db/docs/QUICK_REFERENCE.md)** - Daily commands and quick troubleshooting
- **[✅ New Team Member Onboarding](db/docs/ONBOARDING_CHECKLIST.md)** - Step-by-step checklist for new developers
- **[🔧 Database Setup Guide](db/docs/DATABASE_SETUP.md)** - Complete setup instructions
- **[🚨 Troubleshooting Guide](db/docs/TROUBLESHOOTING.md)** - Common issues and solutions
- **[🔐 Security Guide](db/docs/SECURITY.md)** - Security best practices

### API Documentation
- **Authentication**: JWT-based with refresh tokens
- **Authorization**: Role-based access control
- **File Upload**: Multi-part file upload support
- **Error Handling**: Comprehensive error responses
- **Listing Management**: Full CRUD operations with advanced search
- **Report Management**: Complete moderation system with admin tools
- **Image Management**: Multiple image upload and organization

## Security Features

### Database Security
- 🔐 **Strong password policies** and user isolation
- 🔒 **SSL/TLS encryption** for all connections
- 📊 **Audit logging** for all database changes
- 💾 **Encrypted backups** with integrity verification
- 🚨 **Real-time monitoring** and alerting

### Application Security
- JWT token-based authentication
- Role-based authorization with method-level security
- Input validation and sanitization
- CORS configuration for frontend integration
- Session management with Redis

## Monitoring & Maintenance

### Health Monitoring
```bash
# Quick health check
./setup-database.sh status

# Detailed monitoring
./db/scripts/monitor.sh --full

# Connection monitoring
./db/scripts/monitor.sh --connections
```

### Automated Backups
- **Daily backups** at 2:00 AM
- **7 days** local retention
- **30 days** remote retention
- **Integrity verification** with checksums
- **Point-in-time recovery** capability

## Development Workflow

1. **Setup Environment**
   ```bash
   ./setup-database.sh
   cp .env.template .env  # Update with your values
   ```

2. **Start Development**
   ```bash
   cd backend
   ./mvnw spring-boot:run  # Uses H2 database
   ```

3. **Test with Production Database**
   ```bash
   ./mvnw spring-boot:run -Dspring.profiles.active=prod
   ```

4. **Monitor and Maintain**
   ```bash
   ./db/scripts/monitor.sh --health
   ./db/scripts/backup.sh
   ```

## Troubleshooting

### Common Issues
1. **Database Connection Failed**: Check if Docker containers are running
2. **Authentication Error**: Verify credentials in `.env` file
3. **Port Conflicts**: Ensure ports 5432 and 8080 are available

### Quick Fixes
```bash
# Restart database services
./setup-database.sh restart

# Check service logs
./setup-database.sh logs

# Clean and restart (WARNING: deletes data)
./setup-database.sh cleanup
./setup-database.sh setup
```

## Summary of areas of contributions : (Per Member)

## Link to Project Journal

## Team Google Sheet or Project Board : (Product Backlog and Sprint Backlog for each Sprint)

[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/kvgvOCnV)
