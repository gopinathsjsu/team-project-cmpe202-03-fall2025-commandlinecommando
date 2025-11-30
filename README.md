# Campus Marketplace

A marketplace platform for university students to buy and sell items within their campus community.

---

## Team Name

**Commandline Commandos**

## Team Members

| Name | Areas of Contribution |
|------|----------------------|
| **Vineet Kumar** | Backend API development, Authentication & JWT, Database design, AWS S3 integration, Email notifications (SendGrid), DevOps & Docker |
| **Sakshat Patil** | Frontend development, React components, UI/UX design, Marketplace page, Listing details |
| **Wilson Huang** | Backend services, Chat/Messaging system, Search functionality, Admin dashboard API |
| **Lam Nguyen** | Frontend components, User authentication flow, Profile management, Testing |

## Project Links

- **Project Journal:** [GitHub Wiki](https://github.com/your-repo/wiki) *(Update with actual link)*
- **Sprint Board:** [Google Sheet](https://docs.google.com/spreadsheets/d/your-sheet-id) *(Update with actual link)*

---

## Quick Start

### Using Docker (Recommended)

```bash
# Start all services
docker-compose up -d

# Backend API: http://localhost:8080/api
# Frontend: http://localhost:5173
```

### Local Development

```bash
# 1. Start database
docker-compose up -d postgres redis

# 2. Start backend
cd backend
cp .env.example .env  # Add your AWS S3 and SMTP credentials
./run-with-postgres.sh

# 3. Start frontend
cd frontend
npm install
npm run dev
```

### Test Accounts

| Username | Password | Role |
|----------|----------|------|
| test_buyer | password123 | Buyer/Seller |
| test_admin | password123 | Admin |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend                                 │
│                  React + Vite + TypeScript                       │
│                      (Port 5173)                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Unified Backend API                           │
│                   Spring Boot (Port 8080)                        │
├─────────────┬─────────────┬─────────────┬──────────────────────┤
│    Auth     │  Listings   │    Chat     │       Admin          │
│   Module    │   Module    │   Module    │      Module          │
├─────────────┴─────────────┴─────────────┴──────────────────────┤
│                    Shared Services                               │
│         JWT Security │ Email Service │ S3 Storage               │
└─────────────────────────────────────────────────────────────────┘
        │                       │                    │
        ▼                       ▼                    ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  PostgreSQL  │      │    Redis     │      │   AWS S3     │
│   Database   │      │    Cache     │      │   Storage    │
│  (Port 5432) │      │  (Port 6379) │      │              │
└──────────────┘      └──────────────┘      └──────────────┘
```

### Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 19, TypeScript, Vite, Tailwind CSS |
| Backend | Spring Boot 3.5, Java 21, Spring Security |
| Database | PostgreSQL 16, Flyway migrations |
| Cache | Redis 7 |
| Storage | AWS S3 |
| Email | SendGrid SMTP |
| Auth | JWT (Access + Refresh tokens) |

---

## Features

### User Authentication
- JWT-based login with access and refresh tokens
- User registration with email verification
- Password reset via email
- Role-based access control (Buyer, Seller, Admin)

### Marketplace
- Create, edit, and delete product listings
- Browse listings with category filters
- Full-text search with autocomplete
- Multiple images per listing (AWS S3)
- Favorites/wishlist functionality

### Messaging
- Real-time chat between buyers and sellers
- Conversation threads per listing
- Unread message indicators
- Email notifications for new messages

### Email Notifications
- Listing created confirmation
- New message alerts
- Listing rejection notices

### Admin Dashboard
- User management (suspend, reactivate, delete)
- Content moderation and reports
- Platform statistics

---

## API Endpoints

Base URL: `http://localhost:8080/api`

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | User login |
| POST | `/auth/register` | User registration |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Logout user |
| POST | `/auth/forgot-password` | Request password reset |

### Listings
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/listings` | Get all listings (paginated) |
| GET | `/listings/{id}` | Get single listing |
| POST | `/listings` | Create new listing |
| PUT | `/listings/{id}` | Update listing |
| DELETE | `/listings/{id}` | Delete listing |

### Images
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/images/upload` | Upload images |
| POST | `/images/listing/{id}` | Upload to specific listing |
| DELETE | `/images/{id}` | Delete image |

### Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/chat/conversations` | Get user conversations |
| POST | `/chat/conversations` | Start new conversation |
| GET | `/chat/conversations/{id}/messages` | Get messages |
| POST | `/chat/conversations/{id}/messages` | Send message |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/me` | Get current user profile |
| PUT | `/users/me` | Update profile |
| GET | `/users/me/listings` | Get user's listings |

### Favorites
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/favorites` | Get user favorites |
| POST | `/favorites/{listingId}` | Add to favorites |
| DELETE | `/favorites/{listingId}` | Remove from favorites |

### Search
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/search` | Search listings |
| GET | `/search/autocomplete` | Search suggestions |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/dashboard` | Get statistics |
| GET | `/admin/users` | Get all users |
| POST | `/admin/users/{id}/suspend` | Suspend user |
| POST | `/admin/moderate/{listingId}` | Moderate listing |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/reports` | Submit report |
| GET | `/admin/reports` | Get all reports (admin) |

See [API Documentation](docs/api/README.md) for complete reference with request/response examples.

---

## Project Structure

```
├── backend/                 # Spring Boot API (Port 8080)
├── frontend/                # React + Vite + TypeScript
├── ai-integration-server/   # AI chat service (Port 3001)
├── db/                      # Database migrations and scripts
├── docs/                    # Documentation
└── mockdata/                # Mock data for frontend development
```

---

## Environment Variables

### Backend (.env)

```bash
# Required - AWS S3 for image uploads
AWS_S3_BUCKET_NAME=your-bucket
AWS_REGION=us-west-1
AWS_ACCESS_KEY_ID=your-key
AWS_SECRET_ACCESS_KEY=your-secret

# Optional - Email notifications
SMTP_PASSWORD=your-sendgrid-api-key
EMAIL_FROM=no-reply@yourdomain.com
```

### Frontend (.env)

```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_AI_API_SERVICE_URL=http://localhost:3001
```

---

## Development

### Run Tests

```bash
cd backend
./mvnw test
```

### Database Commands

```bash
cd backend
./setup-database.sh      # Create database
./teardown-database.sh   # Remove database
./mvnw flyway:migrate    # Run migrations
```

### Docker Commands

```bash
docker-compose up -d          # Start services
docker-compose logs -f        # View logs
docker-compose down           # Stop services
docker-compose down -v        # Stop and remove data
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [Backend README](backend/README.md) | Backend setup and API details |
| [Frontend README](frontend/README.md) | Frontend development guide |
| [Database README](db/README.md) | Database setup and migrations |
| [API Documentation](docs/api/README.md) | Complete API reference |
| [Deployment Guide](docs/DEPLOYMENT_GUIDE.md) | Production deployment |

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8080 in use | `lsof -ti:8080 \| xargs kill -9` |
| Database connection failed | `docker-compose up -d postgres redis` |
| Tests failing | Check `backend/.env` has valid credentials |

---
