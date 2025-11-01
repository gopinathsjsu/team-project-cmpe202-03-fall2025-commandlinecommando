# Listing API Quick Reference

## Quick Start Commands

### Development Setup
```bash
# Clone and build
git clone <repository-url>
cd listing-api
mvn clean install

# Run locally
mvn spring-boot:run
# or
make run

# Access application
curl http://localhost:8100/actuator/health
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ListingControllerTest

# Test API endpoints
curl -X GET "http://localhost:8100/api/listings"
```

## Common API Calls

### Create Listing
```bash
curl -X POST "http://localhost:8100/api/listings/" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "MacBook Pro 13-inch",
    "description": "Excellent condition MacBook Pro",
    "price": 1200.00,
    "category": "ELECTRONICS",
    "condition": "LIKE_NEW",
    "location": "San Jose, CA"
  }'
```

### Search Listings
```bash
curl -X GET "http://localhost:8100/api/listings/search?category=ELECTRONICS&keyword=MacBook"
```

### Upload Image
```bash
curl -X POST "http://localhost:8100/api/files/upload/1" \
  -F "file=@image.jpg" \
  -F "displayOrder=1"
```

### Get Listing by ID
```bash
curl -X GET "http://localhost:8100/api/listings/1"
```

## Configuration Quick Reference

### Application Properties
```yaml
# Server
server.port: 8100

# Database (H2 Development)
spring.datasource.url: jdbc:h2:mem:testdb
spring.datasource.username: sa
spring.datasource.password: password

# Database (PostgreSQL Production)
spring.datasource.url: jdbc:postgresql://localhost:5432/listingdb
spring.datasource.username: ${DB_USERNAME}
spring.datasource.password: ${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto: create-drop  # dev
spring.jpa.hibernate.ddl-auto: validate     # prod
spring.jpa.show-sql: true                   # dev

# File Upload
file.upload-dir: ./uploads
file.max-file-size: 10MB
file.max-request-size: 50MB

# Logging
logging.level.com.commandlinecommandos.listingapi: DEBUG
```

### Environment Variables
```bash
# Database
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=listingdb
export DB_USERNAME=listing_user
export DB_PASSWORD=secure_password

# Application
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8100

# Security
export JWT_SECRET=your_jwt_secret
```

## Data Models Quick Reference

### Listing Entity
```java
{
  "listingId": Long,
  "sellerId": Long,
  "title": String (2-255 chars),
  "description": String (10-1000 chars),
  "category": Category enum,
  "price": BigDecimal,
  "condition": ItemCondition enum,
  "status": ListingStatus enum,
  "location": String,
  "createdAt": LocalDateTime,
  "updatedAt": LocalDateTime,
  "viewCount": int,
  "images": List<ListingImage>
}
```

### Enums
```java
// Category
TEXTBOOKS, GADGETS, ELECTRONICS, STATIONARY, OTHER

// ItemCondition  
NEW, LIKE_NEW, GOOD, USED

// ListingStatus
PENDING, ACTIVE, SOLD, CANCELLED
```

## Database Schema Quick Reference

### Tables
```sql
-- Listings
CREATE TABLE listings (
    listing_id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    condition VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    view_count INTEGER DEFAULT 0
);

-- Listing Images
CREATE TABLE listing_images (
    image_id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(listing_id),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Key Indexes
```sql
CREATE INDEX idx_listings_status_created ON listings(status, created_at DESC);
CREATE INDEX idx_listings_seller_id ON listings(seller_id);
CREATE INDEX idx_listings_category ON listings(category);
CREATE INDEX idx_listings_price ON listings(price);
```

## Error Codes Quick Reference

| Status | Description | Common Causes |
|--------|-------------|---------------|
| 200 | OK | Success |
| 400 | Bad Request | Validation errors, invalid JSON |
| 401 | Unauthorized | Missing/invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 500 | Internal Server Error | Server/database issues |

## Development Workflow

### 1. Feature Development
```bash
# Create feature branch
git checkout -b feature/new-feature

# Make changes and test
mvn test

# Commit changes
git add .
git commit -m "Add new feature"

# Push and create PR
git push origin feature/new-feature
```

### 2. Testing Workflow
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Manual API testing
curl -X GET "http://localhost:8100/api/listings"
```

### 3. Deployment Workflow
```bash
# Build for production
mvn clean package -Pprod

# Deploy with Docker
docker build -t listing-api .
docker run -p 8100:8100 listing-api

# Deploy with systemd
sudo systemctl restart listing-api
```

## Monitoring Quick Reference

### Health Checks
```bash
# Basic health
curl http://localhost:8100/actuator/health

# Readiness probe
curl http://localhost:8100/actuator/health/readiness

# Liveness probe
curl http://localhost:8100/actuator/health/liveness
```

### Metrics
```bash
# All metrics
curl http://localhost:8100/actuator/metrics

# Memory usage
curl http://localhost:8100/actuator/metrics/jvm.memory.used

# HTTP requests
curl http://localhost:8100/actuator/metrics/http.server.requests
```

### Logs
```bash
# Application logs
tail -f /var/log/listing-api/application.log

# System logs (systemd)
sudo journalctl -u listing-api -f

# Docker logs
docker logs -f listing-api-container
```

## Troubleshooting Quick Reference

### Common Issues

**Port 8100 already in use**
```bash
sudo lsof -ti:8100 | xargs sudo kill -9
```

**Database connection failed**
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Test connection
psql -h localhost -U listing_user -d listingdb
```

**File upload failed**
```bash
# Check permissions
ls -la /var/uploads/listing-api/

# Fix permissions
sudo chown -R listing-api:listing-api /var/uploads/listing-api/
```

**Out of memory**
```bash
# Check memory usage
curl http://localhost:8100/actuator/metrics/jvm.memory.used

# Increase heap size
java -Xms512m -Xmx2g -jar app.jar
```

## Performance Quick Reference

### JVM Settings
```bash
# Development
java -Xms256m -Xmx512m -jar app.jar

# Production
java -Xms1g -Xmx2g -XX:+UseG1GC -jar app.jar
```

### Database Settings
```sql
-- PostgreSQL tuning
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
SELECT pg_reload_conf();
```

### Application Settings
```yaml
# Connection pooling
spring.datasource.hikari.maximum-pool-size: 20
spring.datasource.hikari.minimum-idle: 5

# JPA optimization
spring.jpa.properties.hibernate.jdbc.batch_size: 25
spring.jpa.properties.hibernate.order_inserts: true
```

## Security Quick Reference

### Environment Variables
```bash
# Never commit these to version control
export DB_PASSWORD=secure_password
export JWT_SECRET=your_jwt_secret
```

### File Permissions
```bash
# Application files
chmod 755 /opt/listing-api/
chown -R listing-api:listing-api /opt/listing-api/

# Upload directory
chmod 755 /var/uploads/listing-api/
chown -R listing-api:listing-api /var/uploads/listing-api/

# Logs
chmod 755 /var/log/listing-api/
chown -R listing-api:listing-api /var/log/listing-api/
```

### Network Security
```bash
# Firewall rules (iptables)
sudo iptables -A INPUT -p tcp --dport 8100 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 5432 -j DROP
```

## Integration Quick Reference

### Frontend Integration
```javascript
// Base URL
const API_BASE = 'http://localhost:8100/api';

// Get all listings
fetch(`${API_BASE}/listings`)
  .then(response => response.json())
  .then(data => console.log(data));

// Create listing
fetch(`${API_BASE}/listings/`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    title: 'MacBook Pro',
    description: 'Great condition',
    price: 1200.00,
    category: 'ELECTRONICS',
    condition: 'LIKE_NEW',
    location: 'San Jose, CA'
  })
});
```

### Backend Integration
```java
// Spring Boot RestTemplate
@Autowired
private RestTemplate restTemplate;

public List<Listing> getAllListings() {
    String url = "http://localhost:8100/api/listings";
    ResponseEntity<List<Listing>> response = restTemplate.exchange(
        url, HttpMethod.GET, null, 
        new ParameterizedTypeReference<List<Listing>>() {}
    );
    return response.getBody();
}
```

This quick reference provides essential information for developers working with the Listing API, covering common tasks, configurations, and troubleshooting scenarios.
