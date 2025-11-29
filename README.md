# Campus Marketplace - Unified Backend

A secure and scalable marketplace platform for university students to buy and sell items within their campus community.

**🎉 Unified Architecture**: Consolidated 3 microservices into a single, modular backend for improved maintainability and performance.

## Team Name
**Commandline Commandos**

## Team Member Names
1. Vineet Kumar
2. Sakshat Patil
3. Wilson Huang
4. Lam Nguyen

---

## Quick Start

### Option 1: Docker Compose (Recommended)
```bash
# Start all services (PostgreSQL, Redis, Backend)
docker-compose up --build

# Backend will be available at http://localhost:8080
# Flyway migrations run automatically
```

### Option 2: Local Development (with Docker databases)
```bash
# 1. Start database services
docker-compose up -d postgres redis

# 2. Start unified backend (connects to Docker PostgreSQL)
cd backend
./mvnw spring-boot:run

# Backend runs on: http://localhost:8080
```

### Option 3: Manual Setup (No Docker)
For complete manual setup without Docker, see **[MANUAL_SETUP.md](MANUAL_SETUP.md)**

This includes:
- Setting up PostgreSQL locally
- Running database migrations manually
- Configuring environment variables
- Starting backend and frontend

### Test Credentials
| Username | Password | Roles |
|----------|----------|-------|
| `test_buyer` | `password123` | BUYER, SELLER |
| `test_admin` | `password123` | ADMIN |

**Note:** Demo accounts (`alice_buyer`, `bob_buyer`, etc.) are for display purposes only and cannot be used for login.

### Quick Test
```bash
# Health check
curl http://localhost:8080/api/actuator/health

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice_buyer", "password": "password123"}'
```

---

## API Documentation

📖 **[Complete API Documentation](docs/api/BACKEND_API_DOCUMENTATION.md)** - Full endpoint reference for frontend integration

### API Endpoints Overview
All endpoints available at `http://localhost:8080/api`:

| Module | Endpoint | Description |
|--------|----------|-------------|
| **Auth** | `/api/auth/*` | Login, register, token refresh, password reset |
| **Users** | `/api/users/*` | Profile management |
| **Listings** | `/api/listings/*` | Product listings CRUD |
| **Search** | `/api/search/*` | Full-text search, autocomplete |
| **Discovery** | `/api/discovery/*` | Trending, recommended, similar items |
| **Favorites** | `/api/favorites/*` | Wishlist management |
| **Chat** | `/api/chat/*` | Buyer-seller messaging |
| **Orders** | `/api/orders/*` | Cart & order processing |
| **Payments** | `/api/payments/*` | Payment methods & transactions |
| **Reports** | `/api/reports/*` | Content flagging |
| **Admin** | `/api/admin/*` | User management, moderation, analytics |

### Services
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/actuator/health
- **PostgreSQL**: localhost:5432 (database: `campus_marketplace`)
- **Redis**: localhost:6379
- **Frontend**: http://localhost:5173 (when running)

---

## Project Structure

```
├── backend/                 # Unified Spring Boot application (Port 8080)
│   ├── src/main/java/com/commandlinecommandos/campusmarketplace/
│   │   ├── controller/     # REST API controllers
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Data access layer
│   │   ├── model/          # Entity classes
│   │   ├── dto/            # Data transfer objects
│   │   ├── security/       # JWT & authentication
│   │   ├── config/         # Application configuration
│   │   ├── exception/      # Exception handling
│   │   ├── listing/        # Listing module
│   │   └── communication/  # Chat & messaging module
│   └── pom.xml             # Maven dependencies
├── frontend/               # React/Vite frontend (TypeScript)
├── ai-integration-server/  # AI service (optional, Java Spring Boot)
├── db/                     # Database infrastructure
│   ├── migrations/         # Flyway SQL migrations (V1-V14)
│   ├── scripts/            # Utility scripts
│   └── docs/               # Database documentation
├── docs/                   # Project documentation
│   ├── api/                # API documentation
│   ├── deployment/         # Deployment guides
│   └── testing/            # Testing documentation
├── docker-compose.yml      # Docker services configuration
└── scripts/                # Development scripts
```

---

## Architecture

### Unified Backend
Single Spring Boot application with modular packages:

```
┌─────────────────────────────────────────────────────────────┐
│                    Unified Backend (8080)                    │
├─────────────┬─────────────┬─────────────┬──────────────────┤
│    Auth     │  Listings   │    Chat     │      Admin       │
│   Module    │   Module    │   Module    │     Module       │
├─────────────┴─────────────┴─────────────┴──────────────────┤
│              Shared Services & Security                      │
├─────────────────────────────────────────────────────────────┤
│              PostgreSQL + Redis + Flyway                     │
└─────────────────────────────────────────────────────────────┘
```

### Key Features
- ✅ **Many-to-Many User Roles**: Users can have multiple roles (BUYER, SELLER, ADMIN)
- ✅ **JWT Authentication**: Access tokens (1hr) + Refresh tokens (7 days)
- ✅ **UUID-based IDs**: Consistent UUID usage across all entities
- ✅ **Flyway Migrations**: Automated database schema management
- ✅ **Redis Caching**: Session management and caching
- ✅ **Unified Exception Handling**: Consistent error responses

---

## Features

### 🔐 Authentication & Authorization
- JWT-based authentication with refresh tokens
- Role-based access control (BUYER, SELLER, ADMIN)
- Password reset via email
- Account deactivation with recovery period

### 📦 Marketplace
- Product listings with categories (Electronics, Books, Clothing, etc.)
- Full-text search with filters and sorting
- Favorites/wishlist functionality
- Image upload support

### 💬 Communication
- Real-time chat between buyers and sellers
- Unread message count
- Conversation history

### 🛒 Orders
- Shopping cart functionality
- Order lifecycle management
- Seller order dashboard

### 👮 Admin
- User management (suspend, reactivate, delete)
- Content moderation (reports)
- Analytics dashboard

---

## Development

### Running Tests
```bash
cd backend

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=RoleBasedAccessTest
```

### Database Commands
```bash
# Check migration status
cd backend && ./mvnw flyway:info

# Run migrations manually
./mvnw flyway:migrate

# Connect to database
docker exec -it campus-marketplace-db psql -U cm_app_user -d campus_marketplace
```

### Docker Commands
```bash
# Start all services
docker-compose up -d

# Rebuild backend after code changes
docker-compose build --no-cache backend
docker-compose up -d backend

# View logs
docker-compose logs -f backend

# Stop all services
docker-compose down
```

---

## Documentation

### Quick Links
| Document | Description |
|----------|-------------|
| **[📖 API Documentation](docs/api/BACKEND_API_DOCUMENTATION.md)** | Complete API reference for frontend |
| **[🚀 Deployment Guide](docs/DEPLOYMENT_GUIDE.md)** | Production deployment instructions |
| **[🧪 Testing Guide](docs/testing/E2E_TEST_MANUAL.md)** | E2E testing procedures |
| **[📚 Documentation Index](docs/DOCUMENTATION_INDEX.md)** | All documentation links |

### Database Documentation
- **[Database Setup](db/docs/DATABASE_SETUP.md)** - Initial setup
- **[Schema Design](db/docs/SCHEMA_DESIGN.md)** - Database schema
- **[Troubleshooting](db/docs/TROUBLESHOOTING.md)** - Common issues

---

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Port 8080 in use | `docker stop campus-marketplace-backend` or kill process |
| Database connection failed | `docker-compose up -d postgres redis` |
| "database does not exist" | Database auto-created by Docker; restart with `docker-compose down -v && docker-compose up -d` |
| Backend keeps restarting with "password authentication failed" | PostgreSQL volume has stale credentials. Run `docker-compose down -v && docker-compose up --build` to reset volumes |
| Tests failing | Run `./mvnw test` in backend directory |

### Quick Fixes
```bash
# Restart all services
docker-compose restart

# Full reset (deletes data)
docker-compose down -v
docker-compose up -d

# Check service health
docker-compose ps
curl http://localhost:8080/api/actuator/health
```

---

## Contributing

1. Create feature branch from `main`
2. Make changes and add tests
3. Run `./mvnw test` to ensure all tests pass
4. Submit pull request

---

## Links

- **GitHub Classroom**: [![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/kvgvOCnV)
