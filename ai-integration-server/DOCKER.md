# Docker Deployment Guide for AI Integration Server

This document explains how to run the AI Integration Server using Docker.

## Quick Start with Docker Compose

The AI Integration Server is included in the main `docker-compose.yml` at the project root.

### 1. Prerequisites
- Docker and Docker Compose installed
- OpenAI API key

### 2. Set Up Environment Variables

```bash
# From project root
cd '/Users/vineetkia/SJSU/Projects/CMPE-202/Group Project/Project'

# Copy environment template
cp .env.docker.example .env

# Edit .env and add your OpenAI API key
nano .env  # or vim, or any editor
```

In the `.env` file, set:
```bash
OPENAI_API_KEY=your-actual-openai-api-key-here
```

### 3. Start All Services

```bash
# Build and start all services including AI integration server
docker-compose up -d

# Or start only the AI service
docker-compose up -d ai-integration-server
```

### 4. Check Status

```bash
# View all running services
docker-compose ps

# View AI service logs
docker-compose logs -f ai-integration-server

# Check health
curl http://localhost:3001/api/health
```

### 5. Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

---

## Standalone Docker Build

If you want to run the AI service independently:

### Build the Image

```bash
cd ai-integration-server

# Build the Docker image
docker build -t ai-integration-server:latest .
```

### Run the Container

```bash
# Run with environment variable
docker run -d \
  --name ai-integration-server \
  -p 3001:3001 \
  -e OPENAI_API_KEY=your-api-key-here \
  ai-integration-server:latest

# View logs
docker logs -f ai-integration-server

# Stop container
docker stop ai-integration-server
docker rm ai-integration-server
```

---

## Docker Compose Configuration

The AI service is configured in the main `docker-compose.yml`:

```yaml
ai-integration-server:
  build:
    context: ./ai-integration-server
    dockerfile: Dockerfile
  container_name: campus-marketplace-ai-service
  restart: unless-stopped
  environment:
    OPENAI_API_KEY: ${OPENAI_API_KEY}
    LOG_LEVEL: ${LOG_LEVEL:-INFO}
    PORT: 3001
  ports:
    - "${AI_SERVICE_PORT:-3001}:3001"
  healthcheck:
    test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:3001/api/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 40s
  networks:
    - campus-marketplace-network
```

---

## Dockerfile Explanation

The Dockerfile uses a **multi-stage build** for optimal image size:

### Stage 1: Build
```dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS build
```
- Uses Maven to compile the application
- Downloads dependencies
- Creates the JAR file

### Stage 2: Runtime
```dockerfile
FROM eclipse-temurin:17-jre-alpine
```
- Uses a minimal JRE image (no Maven, no build tools)
- Runs as non-root user for security
- Final image is ~200MB smaller than using full JDK

---

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `OPENAI_API_KEY` | **Yes** | - | Your OpenAI API key |
| `LOG_LEVEL` | No | `INFO` | Logging level (TRACE, DEBUG, INFO, WARN, ERROR) |
| `PORT` | No | `3001` | Server port |

---

## Networking

### Within Docker Compose
Other services can access the AI service at:
```
http://ai-integration-server:3001
```

### From Host Machine
Access the AI service at:
```
http://localhost:3001
```

### Frontend Configuration
Update your frontend `.env` to point to the Docker service:
```bash
VITE_AI_API_SERVICE_URL=http://localhost:3001
```

---

## Health Checks

The service includes a health check endpoint:

```bash
# From host
curl http://localhost:3001/api/health

# From within Docker network
docker exec campus-marketplace-ai-service wget -qO- http://localhost:3001/api/health
```

Expected response:
```json
{
  "status": "ok",
  "hasApiKey": true
}
```

---

## Troubleshooting

### Service Won't Start

**Check logs:**
```bash
docker-compose logs ai-integration-server
```

**Common issues:**
1. Missing `OPENAI_API_KEY`
   ```bash
   # Make sure it's set in .env
   grep OPENAI_API_KEY .env
   ```

2. Port 3001 already in use
   ```bash
   # Change port in .env
   AI_SERVICE_PORT=3002
   ```

### Build Failures

**Clear Docker cache:**
```bash
docker-compose build --no-cache ai-integration-server
```

**Check Docker resources:**
```bash
docker system df
docker system prune -a  # Clean up
```

### Connection Issues

**Verify service is running:**
```bash
docker-compose ps ai-integration-server
```

**Check network connectivity:**
```bash
docker network inspect campus-marketplace-network
```

**Test from another container:**
```bash
docker-compose exec backend curl http://ai-integration-server:3001/api/health
```

---

## Production Considerations

### 1. Security
- Store `OPENAI_API_KEY` in secrets manager (AWS Secrets Manager, HashiCorp Vault)
- Use Docker secrets instead of environment variables
- Enable HTTPS/TLS

### 2. Resource Limits
Add to docker-compose.yml:
```yaml
ai-integration-server:
  deploy:
    resources:
      limits:
        cpus: '1.0'
        memory: 512M
      reservations:
        cpus: '0.5'
        memory: 256M
```

### 3. Monitoring
- Add logging aggregation (ELK stack, Splunk)
- Monitor health check endpoint
- Set up alerts for service failures

### 4. Scaling
For high traffic, run multiple instances:
```bash
docker-compose up -d --scale ai-integration-server=3
```

---

## Integration with CI/CD

### GitHub Actions Example

```yaml
name: Build and Push AI Service

on:
  push:
    branches: [ main ]
    paths:
      - 'ai-integration-server/**'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Build Docker image
        run: |
          cd ai-integration-server
          docker build -t myregistry/ai-integration-server:${{ github.sha }} .

      - name: Push to registry
        run: |
          docker push myregistry/ai-integration-server:${{ github.sha }}
```

---

## Commands Reference

```bash
# Build
docker-compose build ai-integration-server

# Start
docker-compose up -d ai-integration-server

# Stop
docker-compose stop ai-integration-server

# Restart
docker-compose restart ai-integration-server

# View logs
docker-compose logs -f ai-integration-server

# Execute command in container
docker-compose exec ai-integration-server sh

# Remove container
docker-compose rm -f ai-integration-server

# Rebuild from scratch
docker-compose build --no-cache ai-integration-server
docker-compose up -d ai-integration-server
```

---

## Support

For issues or questions:
1. Check logs: `docker-compose logs ai-integration-server`
2. Verify configuration: `docker-compose config`
3. Review [README.md](README.md) for service details
4. Check OpenAI API status: https://status.openai.com/
