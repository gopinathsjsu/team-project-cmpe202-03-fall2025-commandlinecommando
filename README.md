<<<<<<< HEAD
## 👥 Team - Commandline Commandos
# Campus Marketplace - CMPE-202 Project
=======
# Campus Marketplace

A secure and scalable marketplace platform for university students to buy and sell items within their campus community.

## Team Name
**Commandline Commandos**
>>>>>>> eb856e2 (POSTGRES database infrastructure, docs, and monitoring setup)

A full-stack campus marketplace application built with Spring Boot backend and React frontend, featuring JWT authentication, role-based access control, and PostgreSQL database.

<<<<<<< HEAD
---
=======
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
```

### Access Services
- **Application**: http://localhost:8080/api
- **pgAdmin**: http://localhost:8080 (database management)
- **H2 Console**: http://localhost:8080/api/h2-console (development only)

## Project Structure

```
├── backend/                 # Spring Boot application
│   ├── src/main/java/      # Application source code
│   ├── src/main/resources/ # Configuration files
│   └── pom.xml            # Maven dependencies
├── frontend/               # React/Vite frontend
├── db/                    # Database infrastructure
│   ├── scripts/           # Backup, monitoring, and utility scripts
│   ├── docs/             # Database documentation
│   └── migrations/       # Database schema changes
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
- JWT-based authentication and authorization
- Role-based access control (Student, Admin)
- File upload support for product images
- RESTful API with comprehensive error handling
- Session management with Redis support

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
>>>>>>> eb856e2 (POSTGRES database infrastructure, docs, and monitoring setup)

## 🔗 Links

- **Project Journal:**
- **Team Jira Board:**

### Key Contribution Area Per Member
- **Vineet Kumar** - Authentication & Authorization Backend System
    1. Authentication and Authorization Backend System
    2. UML Diagram Design for Authentication and Authorization System
    3. API Development for Registration, Login, Validate Token, Refresh Token, User Info and Logout functionality
    4. Unit tests for Authentication and Authorization module
    5. Initial Postgres database SQL schema model files to setup database across different systems and seed sample data
- **Sakshat Patil** - 
- **Wilson Huang** - 
- **Lam Nguyen** - 

---

## 🚀 Project Quick Start

### **Prerequisites**
- Java 21+
- Maven 3.6+
- PostgreSQL 12+
- Node.js 18+

### **Start Backend**
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
**Backend runs on:** `http://localhost:8080`

### **Start Frontend**
```bash
cd frontend
npm install
npm run dev
```
**Frontend runs on:** `http://localhost:5173`

---

## 📁 Project Structure

```
campus-marketplace/
├── backend/                 # Spring Boot API
│   ├── src/main/java/      # Java source code
│   ├── src/test/java/      # Unit tests
│   └── pom.xml            # Maven configuration
├── frontend/               # React application
│   ├── src/               # React source code
│   └── package.json       # Node.js dependencies
├── documentation/          # Project documentation
├── sql_files/             # Database schema and seed data
└── README.md              # This file
```

---

## 🔧 Database Setup

1. **Install PostgreSQL**
2. **Create Database:**
   ```sql
   CREATE DATABASE campus_marketplace;
   ```
3. **Run Schema:**
   ```bash
   psql -U your_username -d campus_marketplace -f sql_files/schema_postgres.sql
   ```
4. **Load Test Data:**
   ```bash
   psql -U your_username -d campus_marketplace -f sql_files/seed_data.sql
   ```

---

## 🧪 Testing

### **Test Users**
| Role | Username | Password | Email |
|------|----------|----------|--------|
| **Student** | `student` | `password123` | `student@sjsu.edu` |
| **Admin** | `admin` | `password123` | `admin@sjsu.edu` |

### **Quick Health Check**
```bash
# Test backend
curl http://localhost:8080/
curl http://localhost:8080/api/auth/validate

# Test frontend
curl http://localhost:5173/
```

---

## 📚 Documentation

- **Authentication & Authorization:** [`documentation/Authentication_Authorization_ReadMe.md`](documentation/Authentication_Authorization_ReadMe.md)
- **Backend API:** [`backend/README.md`](backend/README.md)
- **Database Schema:** [`sql_files/schema_postgres.sql`](sql_files/schema_postgres.sql)
- **Test Data:** [`sql_files/seed_data.sql`](sql_files/seed_data.sql)

---

## 🏗️ Architecture

### **Backend (Spring Boot)**
- **Framework:** Spring Boot 3.5.6
- **Security:** JWT Authentication with Spring Security
- **Database:** PostgreSQL with Hibernate ORM
- **API:** RESTful endpoints with role-based access control

### **Frontend (React)**
- **Framework:** React 18 with TypeScript
- **Build Tool:** Vite
- **Styling:** CSS modules
- **State Management:** React hooks

### **Database**
- **Engine:** PostgreSQL 17.5
- **ORM:** Hibernate 6.6.29
- **Schema:** Single table inheritance for User/Student/Admin

---

## 🔒 Security Features

- **JWT Authentication** with HS512 signing
- **Role-Based Access Control** (Student/Admin roles)
- **Refresh Token Management** (7-day expiration)
- **Password Hashing** with BCrypt
- **CORS Configuration** for frontend integration
- **Input Validation** with Bean Validation

---

## 📊 Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Backend** | Spring Boot | 3.5.6 |
| **Security** | Spring Security | 6.2.11 |
| **Database** | PostgreSQL | 17.5 |
| **ORM** | Hibernate | 6.6.29 |
| **Frontend** | React | 18 |
| **Build Tool** | Maven/Vite | 3.9.10/5.0 |
| **Java** | OpenJDK | 21 |

---

## 🚀 Deployment

### **Development**
```bash
# Backend
cd backend && mvn spring-boot:run

# Frontend
cd frontend && npm run dev
```

### **Production**
```bash
# Build backend JAR
cd backend && mvn clean package
java -jar target/campusmarketplace-0.0.1-SNAPSHOT.jar

# Build frontend
cd frontend && npm run build
```

---

## 🐛 Troubleshooting

### **Common Issues**

1. **Port 8080 already in use:**
   ```bash
   lsof -i :8080 && kill -9 <PID>
   ```

2. **Database connection failed:**
   - Check PostgreSQL is running
   - Verify database credentials
   - Ensure database exists

3. **ClassNotFoundException:**
   ```bash
   mvn clean compile
   ```

---

## 📝 License

This project is part of CMPE-202 coursework at San Jose State University.

---
