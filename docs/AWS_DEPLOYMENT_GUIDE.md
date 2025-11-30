# AWS EC2 + ALB Deployment Guide

## Overview

Deploy the Campus Marketplace application on AWS EC2 with an Application Load Balancer (ALB).

## Architecture

```
                    ┌─────────────────┐
                    │   Route 53      │
                    │ (DNS - Optional)│
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │       ALB       │
                    │ (Port 80/443)   │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│   Frontend    │   │   Backend     │   │  AI Service   │
│  (nginx:80)   │   │  (Java:8080)  │   │  (Java:3001)  │
└───────┬───────┘   └───────┬───────┘   └───────────────┘
        │                   │
        │           ┌───────┴───────┐
        │           │               │
        │    ┌──────▼──────┐ ┌──────▼──────┐
        │    │  PostgreSQL │ │    Redis    │
        │    │  (Port 5432)│ │  (Port 6379)│
        │    └─────────────┘ └─────────────┘
        │
        └──────────► proxies /api to Backend
```

## Prerequisites

1. **AWS Account** with appropriate permissions
2. **EC2 Instance** (recommended: t3.medium or larger)
3. **Security Groups** configured
4. **S3 Bucket** for image uploads
5. **Docker & Docker Compose** installed on EC2

## Step 1: Launch EC2 Instance

### Recommended Instance Type
- **Development/Testing**: t3.medium (2 vCPU, 4 GB RAM)
- **Production**: t3.large (2 vCPU, 8 GB RAM) or larger

### AMI
- Amazon Linux 2023 or Ubuntu 22.04 LTS

### Security Group Rules

| Type        | Protocol | Port Range | Source      | Description         |
|-------------|----------|------------|-------------|---------------------|
| SSH         | TCP      | 22         | Your IP     | SSH access          |
| HTTP        | TCP      | 80         | 0.0.0.0/0   | Frontend            |
| HTTPS       | TCP      | 443        | 0.0.0.0/0   | Frontend (SSL)      |
| Custom TCP  | TCP      | 8080       | ALB SG      | Backend (from ALB)  |
| Custom TCP  | TCP      | 3001       | ALB SG      | AI Service (from ALB) |

## Step 2: Install Docker on EC2

```bash
# For Amazon Linux 2023
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker --version
docker-compose --version
```

## Step 3: Clone Repository & Configure

```bash
# Clone the repository
git clone <your-repo-url> campus-marketplace
cd campus-marketplace

# Copy environment variables (defaults are already set)
cp env.example .env

# Edit .env if you need to override any defaults
nano .env
```

Default values are already configured in `env.example`, so you can deploy without editing `.env` if the defaults work for you.

## Step 4: Deploy Application

```bash
# Make the deploy script executable
chmod +x aws-deploy-ec2.sh

# Deploy all services (AI service included by default)
./aws-deploy-ec2.sh
```

Or manually:

```bash
# Build and start all services (AI service starts automatically)
docker-compose -f docker-compose.prod.yml up -d --build
```

## Step 5: Configure Application Load Balancer (ALB)

### Create Target Groups

1. **Frontend Target Group**
   - Target type: Instance
   - Protocol: HTTP
   - Port: 80
   - Health check path: `/health`
   - Health check interval: 30 seconds

2. **Backend Target Group** (Optional - if exposing API directly)
   - Target type: Instance
   - Protocol: HTTP
   - Port: 8080
   - Health check path: `/api/actuator/health`
   - Health check interval: 30 seconds

3. **AI Service Target Group**
   - Target type: Instance
   - Protocol: HTTP
   - Port: 3001
   - Health check path: `/api/health`
   - Health check interval: 30 seconds

### Create ALB

1. **Create Application Load Balancer**
   - Scheme: Internet-facing
   - IP address type: IPv4
   - Listeners: HTTP (80), HTTPS (443 - optional)

2. **Configure Routing Rules**
   - Default rule: Forward to Frontend Target Group
   - (Optional) Path `/api/*`: Forward to Backend Target Group
   - (Optional) Path `/ai/*`: Forward to AI Service Target Group

### SSL/TLS (Recommended for Production)

1. Request SSL certificate from AWS Certificate Manager (ACM)
2. Add HTTPS listener (port 443) to ALB
3. Attach certificate to listener
4. Redirect HTTP to HTTPS

## Step 6: Verify Deployment

```bash
# Check container status
docker-compose -f docker-compose.prod.yml ps

# Check logs
docker-compose -f docker-compose.prod.yml logs -f

# Test health endpoints
curl http://localhost/health                    # Frontend
curl http://localhost:8080/api/actuator/health  # Backend
curl http://localhost:3001/api/health           # AI Service
```

## Monitoring & Maintenance

### View Logs

```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f backend
docker-compose -f docker-compose.prod.yml logs -f frontend
```

### Restart Services

```bash
# Restart all
docker-compose -f docker-compose.prod.yml restart

# Restart specific service
docker-compose -f docker-compose.prod.yml restart backend
```

### Update Application

```bash
# Pull latest changes
git pull origin main

# Rebuild and restart
docker-compose -f docker-compose.prod.yml up -d --build
```

### Database Backup

```bash
# Create backup
docker exec campus-marketplace-db pg_dump -U cm_app_user campus_marketplace > backup_$(date +%Y%m%d).sql

# Restore backup
docker exec -i campus-marketplace-db psql -U cm_app_user campus_marketplace < backup_YYYYMMDD.sql
```

## Troubleshooting

### Container won't start
```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs <service-name>

# Check container status
docker ps -a
```

### Database connection issues
```bash
# Check if PostgreSQL is running
docker exec campus-marketplace-db pg_isready

# Connect to database
docker exec -it campus-marketplace-db psql -U cm_app_user campus_marketplace
```

### Health check failing
```bash
# Test health endpoints manually
curl -v http://localhost:8080/api/actuator/health

# Check application logs
docker logs campus-marketplace-backend
```

### Out of memory
```bash
# Check memory usage
docker stats

# Increase swap space on EC2
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

## Cost Optimization Tips

1. **Use Reserved Instances** for long-term deployments
2. **Consider Spot Instances** for development environments
3. **Use AWS RDS** instead of containerized PostgreSQL for production
4. **Use AWS ElastiCache** instead of containerized Redis for production
5. **Enable Auto Scaling** for variable workloads

## Security Best Practices

1. **Use HTTPS** with SSL certificates
2. **Rotate JWT secrets** periodically
3. **Use IAM roles** instead of access keys when possible
4. **Enable VPC** for network isolation
5. **Regular security updates** for Docker images
6. **Enable CloudWatch** for monitoring and alerting

