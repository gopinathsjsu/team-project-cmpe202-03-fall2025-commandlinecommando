# Docker Deployment Guide

This guide explains how to deploy the Campus Marketplace application using Docker and Docker Compose with separate containers for the backend service and PostgreSQL database.

## Architecture

```
┌─────────────────────────────────────────────┐
│         Docker Compose Network              │
│                                             │
│  ┌──────────────┐      ┌───────────────┐    │
│  │   Backend    │─────▶│  PostgreSQL   │    │
│  │  Container   │      │   Container   │    │
│  │ (Spring Boot)│      │   (DB Only)   │    │
│  │  Port: 8080  │      │  Port: 5432   │    │
│  └──────────────┘      └───────────────┘    │
│         ▲                       ▲           │
│         │                       │           │
│    backend_logs          postgres_data      │
│      (volume)              (volume)         │
└─────────────────────────────────────────────┘
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
cd PROJECT_202
```

### 2. Setup Environment Variables

```bash
# Copy the template
cp .env.docker.example .env

# Edit the file and update passwords
nano .env
```

**Required Changes:**
- `DB_APP_PASSWORD`: Set a strong database password
- `JWT_SECRET`: Generate using `openssl rand -base64 64`

### 3. Start Services

```bash
# Start all services in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# View only backend logs
docker-compose logs -f backend
```

### 4. Verify Deployment

```bash
# Check service status
docker-compose ps

# Check backend health
curl http://localhost:8080/api/actuator/health

# Check database connection
docker-compose exec postgres psql -U cm_app_user -d campus_marketplace -c "\dt"
```

### 5. Access the Application

- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/actuator/health
- **Database**: localhost:5432 (use credentials from .env)

## Common Commands

### Service Management

```bash
# Start services
docker-compose up -d

# Stop services (keeps data)
docker-compose stop

# Stop and remove containers (keeps data volumes)
docker-compose down

# Stop and remove everything including volumes (CAUTION: DATA LOSS)
docker-compose down -v

# Restart a specific service
docker-compose restart backend
docker-compose restart postgres
```

### Viewing Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f postgres

# Last 100 lines
docker-compose logs --tail=100 backend
```

### Rebuilding After Code Changes

```bash
# Rebuild and restart backend
docker-compose up -d --build backend

# Force complete rebuild
docker-compose build --no-cache backend
docker-compose up -d backend
```

### Database Operations

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U cm_app_user -d campus_marketplace

# Run SQL file
docker-compose exec -T postgres psql -U cm_app_user -d campus_marketplace < your_script.sql

# Create database backup
docker-compose exec postgres pg_dump -U cm_app_user campus_marketplace > backup_$(date +%Y%m%d).sql

# Restore from backup
docker-compose exec -T postgres psql -U cm_app_user -d campus_marketplace < backup_20241030.sql
```

### Container Shell Access

```bash
# Access backend container shell
docker-compose exec backend sh

# Access postgres container shell
docker-compose exec postgres bash
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
docker-compose logs

# Check if ports are already in use
lsof -i :8080
lsof -i :5432

# Remove old containers and try again
docker-compose down
docker-compose up -d
```

### Backend Can't Connect to Database

```bash
# Check if postgres is healthy
docker-compose ps

# Verify network
docker network inspect campus-marketplace-network

# Check environment variables
docker-compose exec backend env | grep DB_
```

### Database Initialization Issues

If the database schema isn't created:

```bash
# Stop services
docker-compose down

# Remove postgres volume
docker volume rm campus-marketplace-postgres-data

# Start again (will re-initialize)
docker-compose up -d
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
docker-compose logs backend

# Check if port 8080 is accessible inside container
docker-compose exec backend wget -O- http://localhost:8080/api/actuator/health
```

## Production Considerations

### Security

1. **Change Default Passwords**: Update all passwords in `.env`
2. **Secure JWT Secret**: Generate using `openssl rand -base64 64`
3. **Use Secrets Management**: Consider Docker Secrets or external vault
4. **Limit Port Exposure**: Only expose necessary ports
5. **Regular Updates**: Keep base images updated

### Performance

1. **Resource Limits**: Add to `docker-compose.yml`:
```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

2. **Connection Pooling**: Already configured in application.yml
3. **Monitoring**: Use actuator endpoints for health checks

### Backups

1. **Automated Backups**: Set up cron job for regular database backups
2. **Volume Backups**: Backup Docker volumes regularly
3. **Off-site Storage**: Store backups in cloud storage (S3, etc.)

### Scaling

For horizontal scaling:

```bash
# Scale backend to 3 instances
docker-compose up -d --scale backend=3

# Use nginx/traefik for load balancing
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
docker-compose up -d --build backend

# Watch logs
docker-compose logs -f backend
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
1. Check logs: `docker-compose logs`
2. Review this documentation
3. Check Docker and Docker Compose versions
4. Ensure all prerequisites are met

## Clean Up

To completely remove the application:

```bash
# Stop and remove containers, networks (keeps volumes)
docker-compose down

# Remove everything including volumes (WARNING: DATA LOSS)
docker-compose down -v

# Remove images
docker rmi campus-marketplace-backend
docker rmi postgres:16-alpine
```