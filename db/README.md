# Database

PostgreSQL database setup and Flyway migrations for Campus Marketplace.

## Quick Start

### Using Docker (Recommended)

```bash
# Start PostgreSQL and Redis
docker-compose up -d postgres redis

# Database is automatically initialized with:
# - User: cm_app_user
# - Database: campus_marketplace
# - Port: 5432
```

### Local PostgreSQL Setup

```bash
cd backend
./setup-database.sh
```

This creates the database user and schema.

## Migrations

Database schema is managed by Flyway. Migrations run automatically when the backend starts.

### Migration Files

Located in `backend/src/main/resources/db/migrations/`:

| Migration | Description |
|-----------|-------------|
| V1 | Core schema (users, listings, universities) |
| V2 | Seed demo data |
| V3 | API performance indexes (disabled) |
| V4 | User management tables |
| V5 | Search features (disabled) |
| V6 | Chat tables (disabled) |
| V7 | Schema unification (disabled) |
| V8 | Rename products to listings |
| V9 | Refresh tokens table |
| V10 | Schema alignment, notification preferences |
| V11 | Test accounts |
| V12 | User roles many-to-many |
| V13 | Audit logs fix |
| V14 | Image columns for listings |

### Run Migrations Manually

```bash
cd backend
./mvnw flyway:migrate
```

### Check Migration Status

```bash
cd backend
./mvnw flyway:info
```

## Database Schema

### Core Tables

| Table | Description |
|-------|-------------|
| users | User accounts and profiles |
| user_roles | User role assignments (BUYER, SELLER, ADMIN) |
| universities | Supported universities |
| listings | Product listings |
| messages | Chat messages |
| conversations | Chat conversations |
| user_favorites | Wishlist items |
| refresh_tokens | JWT refresh tokens |
| notification_preferences | Email notification settings |

### Key Relationships

- Users belong to a University
- Users can have multiple Roles
- Listings belong to a Seller (User)
- Messages belong to Conversations
- Conversations link Buyers and Sellers

## Connection Details

| Setting | Value |
|---------|-------|
| Host | localhost (or `postgres` in Docker) |
| Port | 5432 |
| Database | campus_marketplace |
| Username | cm_app_user |
| Password | Set in `.env` or `changeme` (default) |

### Connect via psql

```bash
# Docker
docker exec -it campus-marketplace-db psql -U cm_app_user -d campus_marketplace

# Local
psql -h localhost -U cm_app_user -d campus_marketplace
```

## Scripts

Located in `backend/`:

| Script | Purpose |
|--------|---------|
| setup-database.sh | Create database and user |
| teardown-database.sh | Remove database completely |

Located in `db/scripts/`:

| Script | Purpose |
|--------|---------|
| backup.sh | Create database backup |
| restore.sh | Restore from backup |
| monitor.sh | Health checks |

## Troubleshooting

### Connection Refused

```bash
# Make sure PostgreSQL is running
docker-compose up -d postgres
```

### Authentication Failed

```bash
# Reset with fresh database
docker-compose down -v
docker-compose up -d postgres
```

### Migration Failed

1. Check the error message for the specific migration
2. Fix the SQL in `backend/src/main/resources/db/migrations/`
3. If stuck, reset with `flyway:clean` (deletes all data)

## Documentation

- [Database Setup Guide](docs/DATABASE_SETUP.md)
- [Troubleshooting Guide](docs/TROUBLESHOOTING.md)
- [Security Best Practices](docs/SECURITY.md)
