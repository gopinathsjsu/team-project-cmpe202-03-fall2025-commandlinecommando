# New Team Member Database Onboarding Checklist

Welcome to the Campus Marketplace project! This checklist will help you get the database environment set up quickly and correctly.

## ✅ Prerequisites Check

### Required Software Installation
- [ ] **Docker Desktop** installed and running
  - Download from: https://www.docker.com/products/docker-desktop/
  - Verify: `docker --version` and `docker-compose --version`
  
- [ ] **Java 17+** installed
  - Verify: `java --version`
  
- [ ] **Git** installed
  - Verify: `git --version`

- [ ] **Code Editor** (VS Code, IntelliJ, etc.)

### System Check
- [ ] Docker Desktop is running (check system tray/menu bar)
- [ ] No services using ports 5432, 8080, 6379
  - Check: `lsof -i :5432 :8080 :6379` (should return nothing)
- [ ] At least 2GB free RAM and 5GB free disk space

## 📂 Project Setup

### Repository Setup
- [ ] Clone the repository
  ```bash
  git clone <repository-url>
  cd team-project-cmpe202-03-fall2025-commandlinecommando
  ```

- [ ] Switch to correct branch (if applicable)
  ```bash
  git checkout backend/database-environment-setup
  ```

### Environment Configuration
- [ ] Copy environment template
  ```bash
  cp .env.template .env
  ```

- [ ] Review `.env` file (default values are fine for development)
  ```bash
  cat .env
  ```

## 🚀 Database Environment Setup

### Start Services
- [ ] Start all database services
  ```bash
  docker-compose up -d
  ```

- [ ] Verify services are running
  ```bash
  docker-compose ps
  ```
  Expected: All services show "Up" status

### Initial Validation
- [ ] Run the validation script
  ```bash
  cd db/scripts
  ./validate-connection.sh
  ```
  Expected: All green checkmarks ✅

### Service Access Verification
- [ ] **PostgreSQL**: Test direct connection
  ```bash
  docker exec -it campus_marketplace_db psql -U cm_app_user -d campusmarketplace_db -c "SELECT 'Connection successful!' as status;"
  ```

- [ ] **pgAdmin**: Access web interface
  - Open: http://localhost:8080
  - Login: `admin@campusmarketplace.com` / `pgadmin2024`
  - Add server connection (see guide below)

- [ ] **Redis**: Test connection
  ```bash
  docker exec campus_marketplace_redis redis-cli ping
  ```
  Expected: "PONG"

## 🔗 pgAdmin Server Configuration

Add the PostgreSQL server to pgAdmin:

- [ ] Click "Add New Server"
- [ ] **General Tab**:
  - Name: `Campus Marketplace DB`
- [ ] **Connection Tab**:
  - Host: `postgres`
  - Port: `5432`
  - Database: `campusmarketplace_db`
  - Username: `cm_app_user`
  - Password: `campusapp2024`
- [ ] Click "Save"
- [ ] Verify you can browse database structure

## 🏗️ Spring Boot Application Setup

### Backend Configuration Verification
- [ ] Navigate to backend directory
  ```bash
  cd backend
  ```

- [ ] Check application configuration
  ```bash
  cat src/main/resources/application.yml | grep -A 10 "datasource:"
  ```

### Test Different Profiles

#### Development Profile (H2)
- [ ] Test H2 development setup
  ```bash
  # Note: This requires Maven to be installed or Maven wrapper to be properly configured
  SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
  ```
- [ ] Access H2 Console: http://localhost:8080/api/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (leave blank)

#### Production Profile (PostgreSQL)
- [ ] Test PostgreSQL production setup
  ```bash
  SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
  ```
- [ ] Check application logs for successful database connection

*Note: If Maven wrapper issues occur, skip Spring Boot testing for now - the database setup is complete.*

## 📚 Familiarization

### Documentation Review
- [ ] Read the complete team setup guide
  ```bash
  cat db/docs/TEAM_SETUP_GUIDE.md
  ```

- [ ] Review quick reference card
  ```bash
  cat db/docs/QUICK_REFERENCE.md
  ```

- [ ] Understand project structure
  ```bash
  tree -L 3 db/
  ```

### Database Exploration
- [ ] Connect to database and explore
  ```bash
  docker exec -it campus_marketplace_db psql -U cm_app_user -d campusmarketplace_db
  ```

- [ ] Run basic SQL commands:
  ```sql
  -- List tables
  \dt
  
  -- List users
  \du
  
  -- Check database size
  SELECT pg_size_pretty(pg_database_size('campusmarketplace_db'));
  
  -- Exit
  \q
  ```

## 🎯 Team Integration

### Share Your Setup
- [ ] Confirm setup completion to team lead
- [ ] Share any issues encountered and solutions found
- [ ] Add your name and setup date to team setup log (if exists)

### Development Workflow Understanding
- [ ] Understand daily workflow:
  1. Start services: `docker-compose up -d`
  2. Develop with H2: `SPRING_PROFILES_ACTIVE=dev`
  3. Test with PostgreSQL: `SPRING_PROFILES_ACTIVE=prod`
  4. Stop services: `docker-compose stop`

- [ ] Know emergency commands:
  ```bash
  # Reset everything
  docker-compose down -v && docker-compose up -d
  
  # Validate setup
  ./db/scripts/validate-connection.sh
  ```

## 🐛 Troubleshooting Verification

Test your troubleshooting knowledge:

- [ ] **Port Conflict**: Know how to check what's using ports
  ```bash
  lsof -i :5432 :8080 :6379
  ```

- [ ] **Service Issues**: Know how to check logs
  ```bash
  docker-compose logs postgres
  docker-compose logs pgadmin
  ```

- [ ] **Connection Problems**: Know how to reset
  ```bash
  docker-compose restart postgres
  ```

- [ ] **Complete Reset**: Know the nuclear option
  ```bash
  docker-compose down -v
  docker-compose up -d
  ./db/scripts/validate-connection.sh
  ```

## ✅ Final Validation

### Comprehensive Test
- [ ] Stop all services
  ```bash
  docker-compose down
  ```

- [ ] Start fresh
  ```bash
  docker-compose up -d
  ```

- [ ] Wait 30 seconds for services to initialize

- [ ] Run full validation
  ```bash
  cd db/scripts
  ./validate-connection.sh
  ```

- [ ] All checks pass ✅

### Success Criteria
You should be able to:
- [ ] Start and stop the database environment
- [ ] Access PostgreSQL via command line
- [ ] Access pgAdmin web interface
- [ ] Understand the difference between dev/prod profiles
- [ ] Know how to troubleshoot common issues
- [ ] Find help in the documentation

## 🎉 Onboarding Complete!

### What You've Accomplished
✅ Set up a complete PostgreSQL development environment  
✅ Configured multi-service Docker stack  
✅ Tested all database connections and services  
✅ Learned the development workflow  
✅ Prepared for team development  

### Next Steps
1. **Start developing**: Your database environment is ready
2. **Daily routine**: Use the quick reference card
3. **Get help**: Refer to the team setup guide
4. **Contribute**: Help improve documentation based on your experience

### Team Contact
- **Database issues**: Refer to troubleshooting guide first
- **Documentation updates**: Submit pull requests for improvements
- **Team questions**: Ask in team channels

---

**Welcome to the team! You're all set to start developing with the Campus Marketplace database! 🚀**

*Onboarding completed on: ________________*  
*Team member: ________________*  
*Setup time: ________________*