# AWS EC2 Deployment Guide

**Date:** November 26, 2025  
**Status:** ✅ **Deployable with Modifications**

---

## 🎯 Deployment Readiness Assessment

### ✅ What's Ready

1. **Docker Support**
   - ✅ Multi-stage Dockerfile (optimized build)
   - ✅ Docker Compose configuration
   - ✅ Production profile configured
   - ✅ Health checks implemented

2. **Configuration**
   - ✅ Environment variable support
   - ✅ Production profile (`prod`)
   - ✅ SSL configuration support
   - ✅ External database connection support

3. **Application**
   - ✅ Spring Boot application (JAR)
   - ✅ Flyway migrations (automatic)
   - ✅ Health endpoints (`/api/actuator/health`)
   - ✅ Logging configured

### ⚠️ What Needs Modification

1. **Database Configuration**
   - ⚠️ Currently uses Docker service names (`postgres`, `redis`)
   - ✅ Can be changed via environment variables

2. **File Storage**
   - ⚠️ Local file storage (`./uploads`)
   - ⚠️ Should use AWS S3 for production

3. **Infrastructure**
   - ⚠️ No AWS-specific deployment scripts
   - ⚠️ Security groups need configuration
   - ⚠️ Load balancer configuration needed

---

## 🏗️ Deployment Architecture Options

### Option 1: Single EC2 Instance (Simple)

```
┌─────────────────────────────────────┐
│         EC2 Instance                │
│  ┌──────────────────────────────┐  │
│  │  Docker Compose               │  │
│  │  ├── Backend (Spring Boot)    │  │
│  │  ├── PostgreSQL (Container)   │  │
│  │  └── Redis (Container)        │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

**Pros:**
- Simple setup
- Single instance to manage
- Lower cost

**Cons:**
- No high availability
- Database on same instance
- Limited scalability

### Option 2: EC2 + RDS + ElastiCache (Recommended)

```
┌─────────────────────────────────────┐
│         EC2 Instance                │
│  ┌──────────────────────────────┐  │
│  │  Docker Container            │  │
│  │  └── Backend (Spring Boot)   │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
         │              │
         ▼              ▼
┌──────────────┐  ┌──────────────┐
│  RDS         │  │  ElastiCache │
│  PostgreSQL  │  │  Redis       │
└──────────────┘  └──────────────┘
```

**Pros:**
- Managed database (backups, scaling)
- Managed cache (high availability)
- Better performance
- Production-ready

**Cons:**
- Higher cost
- More complex setup

---

## 📋 Step-by-Step Deployment Guide

### Prerequisites

1. **AWS Account** with EC2 access
2. **EC2 Instance** (recommended: t3.medium or larger)
   - Ubuntu 22.04 LTS or Amazon Linux 2023
   - Security group with ports: 22 (SSH), 80 (HTTP), 443 (HTTPS), 8080 (App)
3. **RDS PostgreSQL** (if using managed database)
4. **ElastiCache Redis** (if using managed cache)
5. **S3 Bucket** (for file uploads)
6. **Domain Name** (optional, for SSL)

### Step 1: Prepare EC2 Instance

```bash
# Connect to EC2 instance
ssh -i your-key.pem ubuntu@your-ec2-ip

# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
sudo apt install -y docker.io docker-compose-plugin

# Start Docker
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group
sudo usermod -aG docker ubuntu
newgrp docker

# Verify Docker
docker --version
docker compose version
```

### Step 2: Clone and Prepare Application

```bash
# Clone repository
git clone <your-repo-url>
cd team-project-cmpe202-03-fall2025-commandlinecommando-fork

# Create environment file
cat > .env << EOF
# Database (RDS or local)
DB_HOST=your-rds-endpoint.region.rds.amazonaws.com
DB_PORT=5432
DB_NAME=campus_marketplace
DB_APP_USER=cm_app_user
DB_APP_PASSWORD=your-secure-password

# Redis (ElastiCache or local)
SPRING_REDIS_HOST=your-elasticache-endpoint.cache.amazonaws.com
SPRING_REDIS_PORT=6379

# JWT
JWT_SECRET=your-very-secure-256-bit-secret-key-here
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
EMAIL_FROM=noreply@campusmarketplace.com
EMAIL_NOTIFICATIONS_ENABLED=true

# Application
SPRING_PROFILES_ACTIVE=prod
PORT=8080
SSL_ENABLED=false

# File Upload (S3)
AWS_S3_BUCKET=your-bucket-name
AWS_REGION=us-east-1
EOF
```

### Step 3: Modify Docker Compose for EC2

Create `docker-compose.ec2.yml`:

```yaml
version: '3.8'

services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: campus-marketplace-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      
      # Database (from .env or RDS)
      DB_HOST: ${DB_HOST}
      DB_PORT: ${DB_PORT:-5432}
      DB_NAME: ${DB_NAME}
      DB_APP_USER: ${DB_APP_USER}
      DB_APP_PASSWORD: ${DB_APP_PASSWORD}
      
      # Redis (from .env or ElastiCache)
      SPRING_REDIS_HOST: ${SPRING_REDIS_HOST}
      SPRING_REDIS_PORT: ${SPRING_REDIS_PORT:-6379}
      
      # JWT
      JWT_SECRET: ${JWT_SECRET}
      JWT_ACCESS_TOKEN_EXPIRATION: ${JWT_ACCESS_TOKEN_EXPIRATION:-3600000}
      JWT_REFRESH_TOKEN_EXPIRATION: ${JWT_REFRESH_TOKEN_EXPIRATION:-604800000}
      
      # Email
      SMTP_HOST: ${SMTP_HOST}
      SMTP_PORT: ${SMTP_PORT:-587}
      SMTP_USERNAME: ${SMTP_USERNAME}
      SMTP_PASSWORD: ${SMTP_PASSWORD}
      EMAIL_FROM: ${EMAIL_FROM}
      EMAIL_NOTIFICATIONS_ENABLED: ${EMAIL_NOTIFICATIONS_ENABLED:-true}
      
      # SSL
      SSL_ENABLED: ${SSL_ENABLED:-false}
      SSL_KEYSTORE_PATH: ${SSL_KEYSTORE_PATH:-}
      SSL_KEYSTORE_PASSWORD: ${SSL_KEYSTORE_PASSWORD:-}
    ports:
      - "8080:8080"
    volumes:
      - backend_logs:/app/logs
      - file_uploads:/app/uploads
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

volumes:
  backend_logs:
  file_uploads:
```

**Note:** If using RDS and ElastiCache, remove `postgres` and `redis` services.

### Step 4: Set Up RDS PostgreSQL (Recommended)

```bash
# Via AWS Console or CLI
aws rds create-db-instance \
  --db-instance-identifier campus-marketplace-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 16.1 \
  --master-username cm_app_user \
  --master-user-password your-secure-password \
  --allocated-storage 20 \
  --vpc-security-group-ids sg-xxxxx \
  --db-name campus_marketplace \
  --backup-retention-period 7 \
  --storage-encrypted
```

**Important:**
- Configure security group to allow EC2 instance access
- Enable automated backups
- Set up parameter group for optimal settings

### Step 5: Set Up ElastiCache Redis (Recommended)

```bash
# Via AWS Console or CLI
aws elasticache create-cache-cluster \
  --cache-cluster-id campus-marketplace-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --security-group-ids sg-xxxxx
```

**Important:**
- Configure security group to allow EC2 instance access
- Use in-transit encryption for production

### Step 6: Set Up S3 for File Storage

```bash
# Create S3 bucket
aws s3 mb s3://campus-marketplace-uploads

# Create IAM policy for S3 access
# Attach to EC2 instance role or create IAM user
```

**Modify application to use S3:**
- Update `FileStorageService` to use AWS S3 SDK
- Replace local file storage with S3 uploads

### Step 7: Deploy Application

```bash
# Build and start
docker compose -f docker-compose.ec2.yml up -d --build

# Check logs
docker compose -f docker-compose.ec2.yml logs -f backend

# Verify health
curl http://localhost:8080/api/actuator/health
```

### Step 8: Set Up Reverse Proxy (Nginx)

```bash
# Install Nginx
sudo apt install -y nginx

# Configure Nginx
sudo nano /etc/nginx/sites-available/campus-marketplace

# Add configuration:
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Enable site
sudo ln -s /etc/nginx/sites-available/campus-marketplace /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Step 9: Set Up SSL (Let's Encrypt)

```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal (already configured)
sudo certbot renew --dry-run
```

---

## 🔧 Required Code Modifications

### 1. Update File Storage to S3

**File:** `backend/src/main/java/.../service/FileStorageService.java`

```java
// Add AWS S3 dependency to pom.xml
// <dependency>
//     <groupId>com.amazonaws</groupId>
//     <artifactId>aws-java-sdk-s3</artifactId>
// </dependency>

// Modify to use S3 instead of local storage
@Value("${aws.s3.bucket}")
private String bucketName;

@Autowired
private AmazonS3 s3Client;

public String uploadFile(MultipartFile file) {
    String fileName = generateFileName(file);
    s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
    return s3Client.getUrl(bucketName, fileName).toString();
}
```

### 2. Update Database Connection for RDS

**Already supported via environment variables:**
```yaml
# application.yml (prod profile)
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require
```

**Just set environment variables:**
```bash
export DB_HOST=your-rds-endpoint.rds.amazonaws.com
export DB_PORT=5432
export DB_NAME=campus_marketplace
export DB_APP_PASSWORD=your-password
```

### 3. Update Redis Connection for ElastiCache

**Already supported via environment variables:**
```yaml
# application.yml (prod profile)
spring:
  redis:
    host: ${SPRING_REDIS_HOST}
    port: ${SPRING_REDIS_PORT}
```

**Just set environment variables:**
```bash
export SPRING_REDIS_HOST=your-elasticache-endpoint.cache.amazonaws.com
export SPRING_REDIS_PORT=6379
```

---

## 🔒 Security Considerations

### 1. Security Groups

**EC2 Security Group:**
- Inbound: 22 (SSH), 80 (HTTP), 443 (HTTPS)
- Outbound: All (for RDS, ElastiCache, S3)

**RDS Security Group:**
- Inbound: 5432 from EC2 security group only
- Outbound: None

**ElastiCache Security Group:**
- Inbound: 6379 from EC2 security group only
- Outbound: None

### 2. Environment Variables

**Use AWS Systems Manager Parameter Store or Secrets Manager:**
```bash
# Store secrets in Parameter Store
aws ssm put-parameter \
  --name /campus-marketplace/db-password \
  --value "your-password" \
  --type SecureString

# Retrieve in application
aws ssm get-parameter \
  --name /campus-marketplace/db-password \
  --with-decryption
```

### 3. IAM Roles

**EC2 Instance Role should have:**
- S3 read/write access (for file uploads)
- CloudWatch Logs write access
- Systems Manager Parameter Store read access

### 4. SSL/TLS

- Use Let's Encrypt for free SSL certificates
- Or use AWS Certificate Manager (ACM) with Application Load Balancer
- Enable HTTPS in application.yml:
```yaml
server:
  ssl:
    enabled: true
    key-store: /path/to/keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
```

---

## 📊 Monitoring & Logging

### CloudWatch Logs

```bash
# Install CloudWatch agent
wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i amazon-cloudwatch-agent.deb

# Configure to send application logs
sudo nano /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json
```

### Application Metrics

- Health endpoint: `/api/actuator/health`
- Metrics endpoint: `/api/actuator/metrics`
- Prometheus endpoint: `/api/actuator/prometheus`

---

## 💰 Cost Estimation

### Option 1: Single EC2 (Simple)
- EC2 t3.medium: ~$30/month
- EBS storage: ~$10/month
- **Total: ~$40/month**

### Option 2: EC2 + RDS + ElastiCache (Recommended)
- EC2 t3.medium: ~$30/month
- RDS db.t3.micro: ~$15/month
- ElastiCache cache.t3.micro: ~$12/month
- S3 storage: ~$1/month
- **Total: ~$58/month**

---

## ✅ Deployment Checklist

- [ ] EC2 instance created and configured
- [ ] Docker installed on EC2
- [ ] Security groups configured
- [ ] RDS PostgreSQL created (or use container)
- [ ] ElastiCache Redis created (or use container)
- [ ] S3 bucket created for file uploads
- [ ] Environment variables configured
- [ ] Application code updated for S3
- [ ] Docker Compose file modified for EC2
- [ ] Application deployed and running
- [ ] Health check passing
- [ ] Nginx reverse proxy configured
- [ ] SSL certificate installed
- [ ] Domain name configured
- [ ] Monitoring set up
- [ ] Backups configured

---

## 🚀 Quick Start Script

Create `scripts/deploy-ec2.sh`:

```bash
#!/bin/bash
set -e

echo "🚀 Deploying Campus Marketplace to EC2..."

# Build application
cd backend
./mvnw clean package -DskipTests
cd ..

# Build Docker image
docker compose -f docker-compose.ec2.yml build

# Start services
docker compose -f docker-compose.ec2.yml up -d

# Wait for health check
echo "⏳ Waiting for application to start..."
sleep 30

# Check health
if curl -f http://localhost:8080/api/actuator/health; then
    echo "✅ Application is healthy!"
else
    echo "❌ Application health check failed"
    docker compose -f docker-compose.ec2.yml logs backend
    exit 1
fi

echo "🎉 Deployment complete!"
```

---

## 📝 Summary

**Is it deployable to AWS EC2?** ✅ **YES, with modifications:**

1. ✅ **Docker support** - Ready
2. ✅ **Configuration** - Environment variables supported
3. ⚠️ **Database** - Need to use RDS or update connection strings
4. ⚠️ **Redis** - Need to use ElastiCache or update connection strings
5. ⚠️ **File Storage** - Need to implement S3 integration
6. ⚠️ **SSL/HTTPS** - Need to configure
7. ⚠️ **Security Groups** - Need to configure
8. ⚠️ **Monitoring** - Need to set up CloudWatch

**Estimated effort:** 4-8 hours for full production deployment

**Recommended approach:** Start with Option 1 (single EC2), then migrate to Option 2 (managed services) for production.

---

**Last Updated:** November 26, 2025

