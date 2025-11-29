# Campus Marketplace – Unified Backend

A campus-exclusive marketplace for SJSU students to buy and sell items such as textbooks, electronics, gadgets, and essentials.  
Our team built a unified backend system by combining multiple services into one clean and modular architecture.

---

## Team Name
**Commandline Commandos**

## Team Members
1. Vineet Kumar  
2. Sakshat Patil  
3. Wilson Huang  
4. Lam Nguyen  

---

# Component Ownership
To divide the work clearly, each team member took responsibility for one major part of the system:

| Team Member | Component Owned |
|-------------|-----------------|
| **Lam** | User Management System (backend), Search Integration, Authentication |
| **Vineet** | AWS Deployment, Infrastructure Setup, ChatGPT Search Feature |
| **Wilson** | Listing API, Database Integration, File Upload Service |
| **Sakshat** | Frontend UI (Login, Listings, Management), E2E UI Integration |

---

# Project Journal & Scrum Artifacts

All project documentation is maintained inside the `/project-journal` folder.

| Artifact | Location |
|----------|----------|
| Weekly Scrum Reports | `project-journal/week1...week12` |
| XP Core Values | `project-journal/xp-values.md` |
| Sprint Backlogs (all 6 sprints) | `project-journal/sprint-artifacts/` |
| Task Board Summary | `project-journal/task-board/task-board-summary.md` |
| Burndown Chart Data | `project-journal/burndown/` |

---

# Quick Start

### Option 1: Docker Compose (Recommended)
```bash
docker-compose up --build
```

Backend will be available at **http://localhost:8080**.  
PostgreSQL, Redis, and migrations start automatically.

### Option 2: Local Development
```bash
docker-compose up -d postgres redis
cd backend
./mvnw spring-boot:run
```

---

## Test Credentials

| Username | Password | Roles | Use Case |
|----------|----------|-------|----------|
| `test_buyer` | `password123` | BUYER, SELLER | General user testing |
| `test_admin` | `password123` | ADMIN | Admin functionality testing |

---

# API Documentation

Full API reference is available in:

📌 **`docs/api/BACKEND_API_DOCUMENTATION.md`**

### Major API Modules:

| Module | Path | Description |
|--------|------|-------------|
| Auth | `/api/auth/*` | Login, register, tokens |
| Users | `/api/users/*` | Profile management |
| Listings | `/api/listings/*` | Marketplace item CRUD |
| Search | `/api/search/*` | Search, autocomplete |
| Chat | `/api/chat/*` | Buyer–seller messaging |
| Reports | `/api/reports/*` | Listing/content reports |
| Admin | `/api/admin/*` | Admin tools |

---

# Project Structure

```
backend/                 # Unified Spring Boot backend (port 8080)
frontend/                # React/Vite frontend
ai-integration-server/   # Optional AI microservice
db/                      # Flyway SQL migrations and DB scripts
docs/                    # API, deployment, testing docs
docker-compose.yml       # Local dev environment
project-journal/         # Scrum reports, XP values, sprint artifacts
```

---

# Architecture (Simplified)

```
┌─────────────────────────────────────────────────────────────┐
│                    Unified Backend (Spring Boot)            │
├──────────┬────────────┬────────────┬────────────────────────┤
│  Auth    │  Listings  │   Chat     │        Admin           │
├──────────┴────────────┴────────────┴────────────────────────┤
│              Shared Services, Security, DTOs                │
├─────────────────────────────────────────────────────────────┤
│                PostgreSQL + Redis + Flyway                  │
└─────────────────────────────────────────────────────────────┘
```

---

# Features (Short Summary)

### 🔐 Authentication
- JWT access + refresh tokens  
- Role-based access (Buyer / Seller / Admin)  
- Password reset and profile management  

### 📦 Marketplace
- Listing creation with photo upload  
- Search with filters, sorting, autocomplete  
- Saved items, reports, admin moderation  

### 💬 Chat
- Buyer–seller chat  
- Unread messages  
- Conversation history  

### 🛠 Admin Tools
- User suspension/reactivation  
- Report moderation  
- Basic analytics  

---

# Development Commands

### Run Backend Tests
```bash
cd backend
./mvnw test
```

### Flyway Migration Commands
```bash
./mvnw flyway:info
./mvnw flyway:migrate
```

### Docker Commands
```bash
docker-compose up -d
docker-compose down -v
docker-compose logs -f backend
```

---

# Troubleshooting

| Issue | Fix |
|-------|------|
| Port 8080 busy | Stop other backend instances |
| DB connection error | `docker-compose up -d postgres redis` |
| Schema mismatch | `docker-compose down -v && docker-compose up --build` |
| Build keeps restarting | Reset Postgres volumes and rebuild backend |

---

# Contributing

1. Create a feature branch  
2. Commit with clear messages  
3. Run tests before PR  
4. Submit PR for review  

---

# Links  
- **GitHub Classroom**: https://classroom.github.com/a/kvgvOCnV  
