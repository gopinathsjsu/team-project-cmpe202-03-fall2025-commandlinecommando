# Campus Marketplace - Documentation Index

**Last Updated:** January 2025  
**Project Status:** ✅ Production Ready - All 129 Tests Passing

---

## 📚 Documentation Overview

This project has a clean, focused documentation structure. All essential docs are organized below.

---

## 🚀 Core Documentation

| Document | Location | Description |
|----------|----------|-------------|
| **README.md** | `/README.md` | Project overview, quick start, architecture |
| **API Documentation** | `docs/api/BACKEND_API_DOCUMENTATION.md` | Complete API reference (70+ endpoints) |
| **Deployment Guide** | `docs/DEPLOYMENT_GUIDE.md` | Docker deployment instructions |
| **Docker Deployment** | `docs/DOCKER_DEPLOYMENT.md` | Advanced Docker configuration |
| **Auth & Security** | `docs/Authentication_Authorization_ReadMe.md` | JWT authentication details |

---

## 🗄️ Database Documentation

| Document | Location | Description |
|----------|----------|-------------|
| **Database Setup** | `db/docs/DATABASE_SETUP.md` | Complete PostgreSQL setup guide |
| **Schema Design** | `db/docs/SCHEMA_DESIGN.md` | ERD, tables, relationships |
| **Security** | `db/docs/SECURITY.md` | Database security practices |
| **Troubleshooting** | `db/docs/TROUBLESHOOTING.md` | Common issues and solutions |
| **DB README** | `db/docs/README.md` | Database overview |

---

## 🧪 Testing Resources

| Resource | Location | Description |
|----------|----------|-------------|
| **Postman Collection** | `docs/postman/Campus_Marketplace_API.postman_collection.json` | 40+ endpoint tests |
| **Unit Tests** | `backend/src/test/` | 129 JUnit tests |

---

## 📁 Directory Structure

```
.
├── README.md                           # ⭐ Start here
│
├── docs/
│   ├── DOCUMENTATION_INDEX.md          # This file
│   ├── DEPLOYMENT_GUIDE.md             # Docker deployment
│   ├── DOCKER_DEPLOYMENT.md            # Advanced Docker config
│   ├── Authentication_Authorization_ReadMe.md
│   ├── api/
│   │   └── BACKEND_API_DOCUMENTATION.md  # ⭐ Complete API reference
│   └── postman/
│       └── Campus_Marketplace_API.postman_collection.json
│
├── db/
│   ├── docs/
│   │   ├── README.md
│   │   ├── DATABASE_SETUP.md           # ⭐ DB setup guide
│   │   ├── SCHEMA_DESIGN.md
│   │   ├── SECURITY.md
│   │   └── TROUBLESHOOTING.md
│   └── migrations/                     # Flyway V1-V14 migrations
│
├── backend/
│   ├── QUICK_START_GUIDE.md
│   └── src/
│
├── frontend/
│   └── src/
│
└── scripts/
    └── *.sh                            # Database setup scripts
```

---

## 🎯 Quick Links by Task

| I want to... | Go to |
|--------------|-------|
| Get started quickly | [README.md](../README.md) |
| Deploy with Docker | [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) |
| Integrate with API | [BACKEND_API_DOCUMENTATION.md](api/BACKEND_API_DOCUMENTATION.md) |
| Set up the database | [db/docs/DATABASE_SETUP.md](../db/docs/DATABASE_SETUP.md) |
| Test with Postman | [postman/Campus_Marketplace_API.postman_collection.json](postman/Campus_Marketplace_API.postman_collection.json) |
| Troubleshoot DB issues | [db/docs/TROUBLESHOOTING.md](../db/docs/TROUBLESHOOTING.md) |
| Understand auth flow | [Authentication_Authorization_ReadMe.md](Authentication_Authorization_ReadMe.md) |

---

## 🔑 Test Credentials

| Username | Password | Role |
|----------|----------|------|
| `sjsu_admin` | `password123` | ADMIN |
| `alice_buyer` | `password123` | BUYER |
| `bob_buyer` | `password123` | BUYER |
| `carol_seller` | `password123` | SELLER |
| `david_techseller` | `password123` | SELLER |

---

## 📊 Project Status

| Component | Status |
|-----------|--------|
| **Architecture** | ✅ Unified Spring Boot backend |
| **Database** | ✅ PostgreSQL 16 + Redis 7 |
| **Tests** | ✅ 129/129 passing |
| **API Endpoints** | ✅ 70+ documented |
| **Docker** | ✅ Compose ready |
| **Documentation** | ✅ Clean & consolidated |

---

**Last Cleanup:** January 2025

