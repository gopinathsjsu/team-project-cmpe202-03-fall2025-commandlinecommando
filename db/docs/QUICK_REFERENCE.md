# Campus Marketplace Database - Quick Reference Card

## 🚀 Daily Commands

### Start/Stop Database
```bash
# Start all services
docker-compose up -d

# Stop all services  
docker-compose stop

# Reset everything (removes data)
docker-compose down -v
```

### Validate Setup
```bash
# Check everything is working
cd db/scripts && ./validate-connection.sh

# Check service status
docker-compose ps
```

## 📊 Database Access

### Direct Database Connections
```bash
# Application user (full access)
docker exec -it campus_marketplace_db psql -U cm_app_user -d campusmarketplace_db

# Read-only user
docker exec -it campus_marketplace_db psql -U cm_readonly -d campusmarketplace_db

# Admin user
docker exec -it campus_marketplace_db psql -U postgres -d campusmarketplace_db
```

### Web Interface
- **pgAdmin**: http://localhost:8080
  - Email: `admin@campusmarketplace.com`
  - Password: `pgadmin2024`

## 🔧 Spring Boot Profiles

### Development (H2 Database)
```bash
cd backend
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# H2 Console: http://localhost:8080/api/h2-console
# JDBC URL: jdbc:h2:mem:testdb
```

### Production (PostgreSQL)
```bash
cd backend  
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

### Testing
```bash
cd backend
SPRING_PROFILES_ACTIVE=test mvn test
```

## 🔐 Database Credentials

| User | Password | Access Level |
|------|----------|--------------|
| postgres | postgres_admin_2024! | Full admin |
| cm_app_user | campusapp2024 | Application user |
| cm_readonly | readonly2024 | Read-only |

## 🌐 Service Endpoints

| Service | URL | Purpose |
|---------|-----|---------|
| PostgreSQL | localhost:5432 | Main database |
| pgAdmin | http://localhost:8080 | DB management |
| Redis | localhost:6379 | Cache/sessions |
| H2 Console | http://localhost:8080/api/h2-console | Dev database |

## 🐛 Troubleshooting

### Common Issues
```bash
# Port conflicts
lsof -i :5432 :8080 :6379

# Container problems
docker-compose logs postgres

# Connection issues
./db/scripts/validate-connection.sh

# Reset everything
docker-compose down -v && docker-compose up -d
```

### Useful SQL Commands
```sql
-- View tables
\dt

-- Describe table
\d table_name

-- Current connections
SELECT * FROM pg_stat_activity WHERE datname = 'campusmarketplace_db';

-- Database size
SELECT pg_size_pretty(pg_database_size('campusmarketplace_db'));
```

## 📁 Important Files

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Service configuration |
| `.env` | Environment variables |
| `db/scripts/validate-connection.sh` | Health check |
| `backend/src/main/resources/application.yml` | Spring config |
| `db/docs/TEAM_SETUP_GUIDE.md` | Full documentation |

## ⚡ Emergency Commands

```bash
# Nuclear option - reset everything
docker-compose down -v
docker system prune -f
docker-compose up -d
./db/scripts/validate-connection.sh

# Check what's using ports
sudo lsof -i :5432 -i :8080 -i :6379

# Restart Docker Desktop
# Use Docker Desktop GUI
```

## 💡 Best Practices

- ✅ Use **dev profile** for daily development
- ✅ Use **prod profile** for integration testing  
- ✅ Run validation script when troubleshooting
- ✅ Stop services when not developing
- ❌ Don't commit credentials to git
- ❌ Don't modify container data directly

---

**Need more help?** See `db/docs/TEAM_SETUP_GUIDE.md` for complete documentation.

**Quick validation:** `cd db/scripts && ./validate-connection.sh` 🔍