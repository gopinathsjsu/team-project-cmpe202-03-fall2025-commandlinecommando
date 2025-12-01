# Docker Deployment Guide

Deploy the Campus Marketplace application using Docker and Docker Compose. The stack includes frontend (React + Nginx), backend (Spring Boot), AI service, PostgreSQL database, and Redis cache.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│              Docker Compose Network                          │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │  Frontend    │  │   Backend    │  │ AI Service  │        │
│  │  (Nginx:80)  │  │ (Spring:8080)│  │ (Java:3001) │        │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘        │
│         │                  │                                  │
│         └──────────┬────────┘                                  │
│                    │                                           │
│         ┌──────────┴──────────┐                               │
│         │                      │                               │
│  ┌──────▼──────┐      ┌───────▼──────┐                        │
│  │  PostgreSQL │      │    Redis     │                        │
│  │  (Port 5432)│      │  (Port 6379) │                        │
│  └─────────────┘      └──────────────┘                        │
│                                                               │
│  Volumes: postgres_data, redis_data, backend_logs,           │
│           file_uploads                                       │
└─────────────────────────────────────────────────────────────┘
```

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- Git

### Verify Installation

```bash
docker --version
docker-compose --version
```

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd CLONED_GIT_REPOSITORY
```

### 2. Setup Environment Variables

```bash
# Copy the template (defaults are already set)
cp env.example .env

# Edit the file if you need to override any defaults
nano .env
```

Default values are already configured, so you can skip editing `.env` if the defaults work for you.

### 3. Start Services

```bash
# Start all services in detached mode (includes frontend, backend, AI service, database, Redis)
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# View specific service logs
docker-compose -f docker-compose.prod.yml logs -f backend
docker-compose -f docker-compose.prod.yml logs -f frontend
```

### 4. Verify Deployment

```bash
# Check service status
docker-compose -f docker-compose.prod.yml ps

# Check frontend health
curl http://localhost/health

# Check backend health
curl http://localhost:8080/api/actuator/health

# Check AI service health
curl http://localhost:3001/api/health

# Check database connection
docker-compose -f docker-compose.prod.yml exec postgres psql -U cm_app_user -d campus_marketplace -c "\dt"
```

### 5. Access the Application

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080/api
- **AI Service**: http://localhost:3001/api
- **Health Check**: http://localhost:8080/api/actuator/health
- **Database**: localhost:5432 (use credentials from .env)
- **Redis**: localhost:6379

## Common Commands

### Service Management

```bash
# Start services
docker-compose -f docker-compose.prod.yml up -d

# Stop services (keeps data)
docker-compose -f docker-compose.prod.yml stop

# Stop and remove containers (keeps data volumes)
docker-compose -f docker-compose.prod.yml down

# Stop and remove everything including volumes (CAUTION: DATA LOSS)
docker-compose -f docker-compose.prod.yml down -v

# Restart a specific service
docker-compose -f docker-compose.prod.yml restart backend
docker-compose -f docker-compose.prod.yml restart frontend
docker-compose -f docker-compose.prod.yml restart postgres
```

### Viewing Logs

```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f backend
docker-compose -f docker-compose.prod.yml logs -f frontend
docker-compose -f docker-compose.prod.yml logs -f postgres

# Last 100 lines
docker-compose -f docker-compose.prod.yml logs --tail=100 backend
```

### Rebuilding After Code Changes

```bash
# Rebuild and restart all services
docker-compose -f docker-compose.prod.yml up -d --build

# Rebuild specific service
docker-compose -f docker-compose.prod.yml up -d --build backend

# Force complete rebuild
docker-compose -f docker-compose.prod.yml build --no-cache backend
docker-compose -f docker-compose.prod.yml up -d backend
```

### Database Operations

```bash
# Connect to PostgreSQL
docker-compose -f docker-compose.prod.yml exec postgres psql -U cm_app_user -d campus_marketplace

# Run SQL file
docker-compose -f docker-compose.prod.yml exec -T postgres psql -U cm_app_user -d campus_marketplace < your_script.sql

# Create database backup
docker-compose -f docker-compose.prod.yml exec postgres pg_dump -U cm_app_user campus_marketplace > backup_$(date +%Y%m%d).sql

# Restore from backup
docker-compose -f docker-compose.prod.yml exec -T postgres psql -U cm_app_user -d campus_marketplace < backup_20241030.sql
```

### Container Shell Access

```bash
# Access backend container shell
docker-compose -f docker-compose.prod.yml exec backend sh

# Access frontend container shell
docker-compose -f docker-compose.prod.yml exec frontend sh

# Access postgres container shell
docker-compose -f docker-compose.prod.yml exec postgres bash
```

## Volume Management

### View Volumes

```bash
docker volume ls | grep campus-marketplace
```

### Backup Volume Data

```bash
# Backup PostgreSQL data
docker run --rm \
  -v campus-marketplace-postgres-data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/postgres-data-backup.tar.gz /data
```

### Restore Volume Data

```bash
# Restore PostgreSQL data
docker run --rm \
  -v campus-marketplace-postgres-data:/data \
  -v $(pwd):/backup \
  alpine tar xzf /backup/postgres-data-backup.tar.gz -C /
```

## Configuration

### Environment Variables

All configuration is managed through the `.env` file:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_APP_USER` | Database username | cm_app_user |
| `DB_APP_PASSWORD` | Database password | changeme |
| `DB_PORT` | PostgreSQL port | 5432 |
| `APP_PORT` | Backend API port | 8080 |
| `JWT_SECRET` | JWT signing secret | (see .env file) |
| `LOG_LEVEL` | Application log level | INFO |

### Customizing Ports

Edit `.env` file:
```bash
APP_PORT=9090  # Backend will be available at localhost:9090
DB_PORT=5433   # PostgreSQL will be available at localhost:5433
```

Then restart:
```bash
docker-compose down
docker-compose up -d
```

## Troubleshooting

### Services Won't Start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs

# Check if ports are already in use
lsof -i :80      # Frontend
lsof -i :8080    # Backend
lsof -i :3001    # AI Service
lsof -i :5432    # Database
lsof -i :6379    # Redis

# Remove old containers and try again
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

### Backend Can't Connect to Database

```bash
# Check if postgres is healthy
docker-compose -f docker-compose.prod.yml ps

# Verify network
docker network inspect campus-marketplace-network

# Check environment variables
docker-compose -f docker-compose.prod.yml exec backend env | grep DB_
```

### Database Initialization Issues

If the database schema isn't created:

```bash
# Stop services
docker-compose -f docker-compose.prod.yml down

# Remove postgres volume
docker volume rm campus-marketplace-postgres-data

# Start again (will re-initialize)
docker-compose -f docker-compose.prod.yml up -d
```

### Out of Disk Space

```bash
# Clean up unused images
docker system prune -a

# Remove old volumes
docker volume prune
```

### Backend Health Check Failing

```bash
# Check if backend started successfully
docker-compose -f docker-compose.prod.yml logs backend

# Check if port 8080 is accessible inside container
docker-compose -f docker-compose.prod.yml exec backend wget -O- http://localhost:8080/api/actuator/health
```

## Production Considerations

### Security

1. **Change Default Passwords**: Update all passwords in `.env`
2. **Secure JWT Secret**: Generate using `openssl rand -base64 64`
3. **Use Secrets Management**: Consider Docker Secrets or external vault
4. **Limit Port Exposure**: Only expose necessary ports
5. **Regular Updates**: Keep base images updated

### Performance

1. **Resource Limits**: Already configured in `docker-compose.prod.yml` with CPU and memory limits for all services
2. **Connection Pooling**: Already configured in application.yml
3. **Monitoring**: Use actuator endpoints for health checks
4. **Log Rotation**: Configured to prevent disk space issues

### Backups

1. **Automated Backups**: Set up cron job for regular database backups
2. **Volume Backups**: Backup Docker volumes regularly
3. **Off-site Storage**: Store backups in cloud storage (S3, etc.)

### Scaling

For horizontal scaling:

```bash
# Scale backend to 3 instances
docker-compose -f docker-compose.prod.yml up -d --scale backend=3

# Use ALB (Application Load Balancer) for AWS deployments
# See AWS_DEPLOYMENT_GUIDE.md for details
```

## Development Workflow

### Local Development

For local development without Docker:
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Testing Changes

```bash
# Make code changes
# Rebuild and restart
docker-compose -f docker-compose.prod.yml up -d --build backend

# Watch logs
docker-compose -f docker-compose.prod.yml logs -f backend
```

## CI/CD Integration

### Build Image

```bash
docker build -t campus-marketplace-backend:latest ./backend
```

### Push to Registry

```bash
docker tag campus-marketplace-backend:latest your-registry/campus-marketplace:v1.0
docker push your-registry/campus-marketplace:v1.0
```

## Support

For issues or questions:
1. Check logs: `docker-compose -f docker-compose.prod.yml logs`
2. Review this documentation
3. Check Docker and Docker Compose versions
4. Ensure all prerequisites are met
5. See [AWS_DEPLOYMENT_GUIDE.md](AWS_DEPLOYMENT_GUIDE.md) for EC2 deployment

## Clean Up

To completely remove the application:

```bash
# Stop and remove containers, networks (keeps volumes)
docker-compose -f docker-compose.prod.yml down

# Remove everything including volumes (WARNING: DATA LOSS)
docker-compose -f docker-compose.prod.yml down -v

# Remove images
docker rmi project-frontend project-backend project-ai-integration-server
docker rmi postgres:16-alpine redis:7-alpine
```
