# Campus Marketplace Backend

Spring Boot REST API serving the Campus Marketplace application.

## Quick Start

### Prerequisites

- Java 21
- Maven
- PostgreSQL (or use Docker)
- Redis (optional, for caching)

### Setup

1. **Start database services:**

```bash
docker-compose up -d postgres redis
```

2. **Configure environment:**

```bash
cp .env.example .env
# Edit .env with your AWS S3 and SMTP credentials
```

3. **Run the application:**

```bash
./run-with-postgres.sh
```

The API will be available at `http://localhost:8080/api`

### First-time Database Setup

If running PostgreSQL locally (not Docker):

```bash
./setup-database.sh
```

## Environment Variables

### Required (for image uploads)

```bash
AWS_S3_BUCKET_NAME=your-bucket-name
AWS_REGION=us-west-1
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
```

### Optional (for email notifications)

```bash
SMTP_PASSWORD=your-sendgrid-api-key
EMAIL_FROM=no-reply@yourdomain.com
```

## API Overview

| Endpoint | Description |
|----------|-------------|
| `POST /api/auth/login` | User login |
| `POST /api/auth/register` | User registration |
| `GET /api/listings` | Browse listings |
| `POST /api/listings` | Create listing |
| `GET /api/chat/conversations` | Get conversations |
| `POST /api/chat/conversations/{id}/messages` | Send message |
| `GET /api/admin/dashboard` | Admin statistics |

See [API Documentation](../docs/api/README.md) for complete reference.

## Project Structure

```
src/main/java/com/commandlinecommandos/campusmarketplace/
├── controller/          # REST endpoints
├── service/             # Business logic
├── repository/          # Data access (JPA)
├── model/               # Entity classes
├── dto/                 # Request/Response objects
├── security/            # JWT authentication
├── config/              # App configuration
├── exception/           # Error handling
├── listing/             # Listing module
└── communication/       # Chat module
```

## Key Features

### Authentication

- JWT access tokens (1 hour expiry)
- Refresh tokens (7 days expiry)
- Role-based access: BUYER, SELLER, ADMIN

### Image Upload

- AWS S3 integration
- Supports JPEG, PNG, GIF, WebP
- Max 5 images per listing, 5MB each

### Email Notifications

- SendGrid SMTP integration
- Notifications for:
  - New listing created
  - Message received
  - Listing rejected

### Database

- PostgreSQL with Flyway migrations
- Migrations in `src/main/resources/db/migrations/`
- Automatically runs on startup

## Scripts

| Script | Purpose |
|--------|---------|
| `run-with-postgres.sh` | Start with PostgreSQL profile |
| `setup-database.sh` | Create database and user |
| `teardown-database.sh` | Remove database completely |

## Testing

```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=AuthControllerTest

# Skip tests during build
./mvnw package -DskipTests
```

## Docker

```bash
# Build image
docker build -t campus-marketplace-backend .

# Run container
docker run -p 8080:8080 \
  -e AWS_S3_BUCKET_NAME=your-bucket \
  -e AWS_ACCESS_KEY_ID=your-key \
  -e AWS_SECRET_ACCESS_KEY=your-secret \
  campus-marketplace-backend
```

## Health Check

```bash
curl http://localhost:8080/api/actuator/health
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8080 in use | Kill existing process: `lsof -ti:8080 \| xargs kill -9` |
| Database connection refused | Start PostgreSQL: `docker-compose up -d postgres` |
| S3 upload fails | Check AWS credentials in `.env` |
| Flyway migration error | Check `src/main/resources/db/migrations/` for conflicts |

