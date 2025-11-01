# Listing API Deployment Guide

## Overview
This document provides comprehensive deployment instructions for the Listing API microservice, including development, staging, and production environments.

## Prerequisites

### System Requirements
- **Java**: OpenJDK 21 or Oracle JDK 21
- **Maven**: 3.6.0 or higher
- **Database**: PostgreSQL 12+ (production) or H2 (development)
- **Memory**: Minimum 512MB RAM, Recommended 2GB RAM
- **Storage**: Minimum 1GB free space for application and logs

### Development Tools
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Git**: Version control
- **Docker**: Optional, for containerized deployment
- **Postman/curl**: For API testing

## Environment Setup

### 1. Development Environment

#### Local Development Setup
```bash
# Clone the repository
git clone <repository-url>
cd listing-api

# Verify Java version
java -version  # Should show Java 21

# Verify Maven version
mvn -version   # Should show Maven 3.6+

# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
# or
make run
```

#### Development Configuration
**File**: `src/main/resources/application.yml`

```yaml
server:
  port: 8100

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect

  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    com.commandlinecommandos.listingapi: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# File upload configuration
file:
  upload-dir: ./uploads
  max-file-size: 10MB
  max-request-size: 50MB
```

#### Development Database (H2)
- **URL**: `http://localhost:8100/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`

### 2. Staging Environment

#### Staging Configuration
**File**: `src/main/resources/application-staging.yml`

```yaml
server:
  port: 8100

spring:
  profiles:
    active: staging
  
  datasource:
    url: jdbc:postgresql://staging-db:5432/listingdb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

logging:
  level:
    com.commandlinecommandos.listingapi: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN

# File upload configuration
file:
  upload-dir: /app/uploads
  max-file-size: 10MB
  max-request-size: 50MB

# Management endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

#### Staging Database Setup
```sql
-- Create database
CREATE DATABASE listingdb;

-- Create user
CREATE USER listing_user WITH PASSWORD 'secure_password';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE listingdb TO listing_user;

-- Connect to database and create schema
\c listingdb;

-- Run database migrations
-- (Execute the SQL files from db/migrations/)
```

### 3. Production Environment

#### Production Configuration
**File**: `src/main/resources/application-prod.yml`

```yaml
server:
  port: 8100
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

spring:
  profiles:
    active: prod
  
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 25
        order_inserts: true
        order_updates: true
        connection:
          provider_disables_autocommit: true

logging:
  level:
    com.commandlinecommandos.listingapi: INFO
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/listing-api/application.log
    max-size: 100MB
    max-history: 30

# File upload configuration
file:
  upload-dir: /var/uploads/listing-api
  max-file-size: 10MB
  max-request-size: 50MB

# Management endpoints (restricted)
management:
  endpoints:
    web:
      exposure:
        include: health,info
      base-path: /actuator
  endpoint:
    health:
      show-details: never
  security:
    enabled: true

# Security configuration
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000  # 24 hours
```

#### Production Environment Variables
```bash
# Database configuration
export DB_HOST=prod-database.company.com
export DB_PORT=5432
export DB_NAME=listingdb_prod
export DB_USERNAME=listing_user
export DB_PASSWORD=super_secure_password

# Security
export JWT_SECRET=your_jwt_secret_key_here

# Application configuration
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8100

# File storage
export FILE_UPLOAD_DIR=/var/uploads/listing-api
```

## Deployment Methods

### 1. Traditional Deployment

#### Build and Deploy
```bash
# Build the application
mvn clean package -Pprod

# Create deployment directory
sudo mkdir -p /opt/listing-api
sudo mkdir -p /var/log/listing-api
sudo mkdir -p /var/uploads/listing-api

# Copy JAR file
sudo cp target/listingapi-0.0.1-SNAPSHOT.jar /opt/listing-api/

# Create systemd service file
sudo tee /etc/systemd/system/listing-api.service > /dev/null <<EOF
[Unit]
Description=Listing API Service
After=network.target

[Service]
Type=simple
User=listing-api
Group=listing-api
WorkingDirectory=/opt/listing-api
ExecStart=/usr/bin/java -jar /opt/listing-api/listingapi-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=prod
EnvironmentFile=/opt/listing-api/application-prod.env

[Install]
WantedBy=multi-user.target
EOF

# Create application user
sudo useradd -r -s /bin/false listing-api
sudo chown -R listing-api:listing-api /opt/listing-api
sudo chown -R listing-api:listing-api /var/log/listing-api
sudo chown -R listing-api:listing-api /var/uploads/listing-api

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable listing-api
sudo systemctl start listing-api

# Check status
sudo systemctl status listing-api
```

#### Process Management
```bash
# Start service
sudo systemctl start listing-api

# Stop service
sudo systemctl stop listing-api

# Restart service
sudo systemctl restart listing-api

# View logs
sudo journalctl -u listing-api -f

# View application logs
tail -f /var/log/listing-api/application.log
```

### 2. Docker Deployment

#### Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create application user
RUN useradd -r -s /bin/false listing-api

# Create directories
RUN mkdir -p /app/uploads /app/logs
RUN chown -R listing-api:listing-api /app

# Set working directory
WORKDIR /app

# Copy application JAR
COPY target/listingapi-0.0.1-SNAPSHOT.jar app.jar

# Change ownership
RUN chown listing-api:listing-api app.jar

# Switch to non-root user
USER listing-api

# Expose port
EXPOSE 8100

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8100/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Docker Compose
```yaml
version: '3.8'

services:
  listing-api:
    build: .
    ports:
      - "8100:8100"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=listingdb
      - DB_USERNAME=listing_user
      - DB_PASSWORD=secure_password
      - JWT_SECRET=your_jwt_secret
    volumes:
      - ./uploads:/app/uploads
      - ./logs:/app/logs
    depends_on:
      - postgres
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8100/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=listingdb
      - POSTGRES_USER=listing_user
      - POSTGRES_PASSWORD=secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./db/init:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    restart: unless-stopped

volumes:
  postgres_data:
```

#### Docker Deployment Commands
```bash
# Build and run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f listing-api

# Scale the service
docker-compose up -d --scale listing-api=3

# Update and restart
docker-compose down
docker-compose build
docker-compose up -d
```

### 3. Kubernetes Deployment

#### Deployment Manifest
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: listing-api
  labels:
    app: listing-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: listing-api
  template:
    metadata:
      labels:
        app: listing-api
    spec:
      containers:
      - name: listing-api
        image: listing-api:latest
        ports:
        - containerPort: 8100
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: listing-api-secrets
              key: db-host
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: listing-api-secrets
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: listing-api-secrets
              key: db-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: listing-api-secrets
              key: jwt-secret
        volumeMounts:
        - name: uploads
          mountPath: /app/uploads
        - name: logs
          mountPath: /app/logs
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8100
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8100
          initialDelaySeconds: 30
          periodSeconds: 10
      volumes:
      - name: uploads
        persistentVolumeClaim:
          claimName: listing-api-uploads
      - name: logs
        persistentVolumeClaim:
          claimName: listing-api-logs
---
apiVersion: v1
kind: Service
metadata:
  name: listing-api-service
spec:
  selector:
    app: listing-api
  ports:
  - port: 80
    targetPort: 8100
  type: LoadBalancer
```

#### Secrets Configuration
```bash
# Create secrets
kubectl create secret generic listing-api-secrets \
  --from-literal=db-host=postgres-service \
  --from-literal=db-username=listing_user \
  --from-literal=db-password=secure_password \
  --from-literal=jwt-secret=your_jwt_secret

# Apply deployment
kubectl apply -f listing-api-deployment.yaml

# Check deployment status
kubectl get deployments
kubectl get pods
kubectl get services
```

## Configuration Management

### 1. Environment-Specific Configuration

#### Development Properties
```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.show-sql=true
logging.level.com.commandlinecommandos.listingapi=DEBUG
```

#### Production Properties
```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.jpa.show-sql=false
logging.level.com.commandlinecommandos.listingapi=INFO
```

### 2. External Configuration
```bash
# Using environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/listingdb
export SPRING_DATASOURCE_USERNAME=listing_user
export SPRING_DATASOURCE_PASSWORD=secure_password

# Using external configuration file
java -jar app.jar --spring.config.location=file:/opt/config/application-prod.yml
```

### 3. Configuration Validation
```yaml
# application.yml
management:
  endpoint:
    configprops:
      enabled: true
    env:
      enabled: true
```

## Database Setup

### 1. PostgreSQL Production Setup
```sql
-- Create database
CREATE DATABASE listingdb;

-- Create user with limited privileges
CREATE USER listing_user WITH PASSWORD 'secure_password';

-- Grant necessary privileges
GRANT CONNECT ON DATABASE listingdb TO listing_user;
GRANT USAGE ON SCHEMA public TO listing_user;
GRANT CREATE ON SCHEMA public TO listing_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO listing_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO listing_user;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO listing_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO listing_user;
```

### 2. Database Migrations
```bash
# Run migrations using Flyway (if integrated)
mvn flyway:migrate

# Or manually execute SQL files
psql -h localhost -U listing_user -d listingdb -f db/migrations/V1__campus_marketplace_core_schema.sql
psql -h localhost -U listing_user -d listingdb -f db/migrations/V2__seed_demo_data.sql
```

## Monitoring and Health Checks

### 1. Health Check Endpoints
```bash
# Basic health check
curl http://localhost:8100/actuator/health

# Detailed health information
curl http://localhost:8100/actuator/health/readiness
curl http://localhost:8100/actuator/health/liveness
```

### 2. Application Metrics
```bash
# Application info
curl http://localhost:8100/actuator/info

# Metrics
curl http://localhost:8100/actuator/metrics

# Specific metrics
curl http://localhost:8100/actuator/metrics/jvm.memory.used
curl http://localhost:8100/actuator/metrics/http.server.requests
```

### 3. Log Monitoring
```bash
# Real-time log monitoring
tail -f /var/log/listing-api/application.log

# Log rotation configuration
sudo tee /etc/logrotate.d/listing-api > /dev/null <<EOF
/var/log/listing-api/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 listing-api listing-api
    postrotate
        systemctl reload listing-api
    endscript
}
EOF
```

## Backup and Recovery

### 1. Database Backup
```bash
# Full database backup
pg_dump -h localhost -U listing_user -d listingdb > listingdb_backup_$(date +%Y%m%d).sql

# Automated backup script
#!/bin/bash
BACKUP_DIR="/opt/backups/listing-api"
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -h localhost -U listing_user -d listingdb > "$BACKUP_DIR/listingdb_$DATE.sql"
gzip "$BACKUP_DIR/listingdb_$DATE.sql"
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
```

### 2. File Storage Backup
```bash
# Backup uploaded files
tar -czf uploads_backup_$(date +%Y%m%d).tar.gz /var/uploads/listing-api/

# Sync to remote storage (AWS S3 example)
aws s3 sync /var/uploads/listing-api/ s3://your-bucket/listing-api/uploads/
```

### 3. Application Backup
```bash
# Backup application configuration
tar -czf listing-api-config-backup.tar.gz /opt/listing-api/ /etc/systemd/system/listing-api.service
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check database connectivity
telnet $DB_HOST $DB_PORT

# Test database connection
psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME -c "SELECT 1;"

# Check application logs for connection errors
grep -i "connection" /var/log/listing-api/application.log
```

#### 2. File Upload Issues
```bash
# Check directory permissions
ls -la /var/uploads/listing-api/

# Check disk space
df -h /var/uploads/listing-api/

# Fix permissions
sudo chown -R listing-api:listing-api /var/uploads/listing-api/
sudo chmod -R 755 /var/uploads/listing-api/
```

#### 3. Memory Issues
```bash
# Check JVM memory usage
curl http://localhost:8100/actuator/metrics/jvm.memory.used

# Increase heap size
java -Xms512m -Xmx2g -jar app.jar
```

#### 4. Port Conflicts
```bash
# Check if port is in use
sudo netstat -tulpn | grep :8100

# Kill process using port
sudo lsof -ti:8100 | xargs sudo kill -9
```

### Performance Tuning

#### 1. JVM Tuning
```bash
# Production JVM settings
java -server \
     -Xms1g -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseStringDeduplication \
     -jar app.jar
```

#### 2. Database Tuning
```sql
-- PostgreSQL tuning parameters
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
SELECT pg_reload_conf();
```

## Security Considerations

### 1. Network Security
- Use HTTPS in production
- Implement firewall rules
- Use VPN for database access
- Enable CORS for specific origins only

### 2. Application Security
- Implement proper authentication
- Use environment variables for secrets
- Enable request logging
- Implement rate limiting

### 3. Database Security
- Use strong passwords
- Limit database user privileges
- Enable SSL connections
- Regular security updates

This deployment guide provides comprehensive instructions for deploying the Listing API in various environments while maintaining security, performance, and reliability.
