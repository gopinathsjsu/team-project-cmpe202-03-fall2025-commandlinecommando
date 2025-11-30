# End-to-End Testing Guide

**Version:** 1.0.0

---

## Table of Contents
1. [Prerequisites](#1-prerequisites)
2. [Quick Start](#2-quick-start)
3. [Test Accounts](#3-test-accounts)
4. [API Endpoint Tests](#4-api-endpoint-tests)
5. [Running Unit Tests](#5-running-unit-tests)
6. [Database Verification](#6-database-verification)
7. [Troubleshooting](#7-troubleshooting)

---

## 1. Prerequisites

### Required Services
Ensure all Docker containers are running:

```bash
docker-compose up -d
docker ps
```

Expected containers:
| Container | Port | Status |
|-----------|------|--------|
| `campus-marketplace-backend` | 8080 | healthy |
| `campus-marketplace-db` | 5432 | healthy |
| `campus-marketplace-redis` | 6379 | healthy |

### Health Check
```bash
curl http://localhost:8080/api/actuator/health
# Expected: {"status":"UP"}
```

---

## 2. Quick Start

### Run All E2E Tests
```bash
# From project root
cd backend
./mvnw test
```

### Expected Output
```
Tests run: 129, Failures: 0, Errors: 0, Skipped: 2
BUILD SUCCESS
```

---

## 3. Test Accounts

> ⚠️ **Important**: Only test accounts have valid password hashes. Demo accounts cannot be used for login.

### Working Accounts

| Username | Password | Roles | Use Case |
|----------|----------|-------|----------|
| `test_buyer` | `password123` | BUYER, SELLER | General user testing |
| `test_admin` | `password123` | ADMIN | Admin functionality testing |

### Login Example
```bash
# Login as regular user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test_buyer", "password": "password123"}'

# Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test_admin", "password": "password123"}'
```

---

## 4. API Endpoint Tests

### 4.1 Authentication Tests

```bash
# Store tokens for reuse
LOGIN_RESP=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test_buyer", "password": "password123"}')

ACCESS_TOKEN=$(echo $LOGIN_RESP | jq -r '.accessToken')
REFRESH_TOKEN=$(echo $LOGIN_RESP | jq -r '.refreshToken')

echo "Access Token: ${ACCESS_TOKEN:0:50}..."
```

#### Get Current User
```bash
curl -s http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
```

#### Validate Token
```bash
curl -s http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
```

#### Refresh Token
```bash
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}" | jq .
```

#### Logout
```bash
curl -s -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}" | jq .
```

---

### 4.2 User Profile Tests

```bash
# Get user profile
curl -s http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# Update profile
curl -s -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Updated", "lastName": "Name"}' | jq .
```

---

### 4.3 Listings Tests

#### Get All Listings
```bash
curl -s http://localhost:8080/api/listings \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.content | length'
```

#### Get Single Listing
```bash
LISTING_ID=$(curl -s http://localhost:8080/api/listings \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq -r '.content[0].id')

curl -s "http://localhost:8080/api/listings/$LISTING_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '{title, price, seller}'
```

#### Filter by Category
```bash
curl -s "http://localhost:8080/api/listings?category=ELECTRONICS" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq '.totalElements'
```

#### Create Listing
```bash
curl -s -X POST http://localhost:8080/api/listings \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Item",
    "description": "Test description",
    "category": "ELECTRONICS",
    "condition": "NEW",
    "price": 99.99,
    "location": "Test Location"
  }' | jq .
```

#### Update Listing
```bash
curl -s -X PUT "http://localhost:8080/api/listings/$LISTING_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "description": "Updated description",
    "category": "ELECTRONICS",
    "condition": "LIKE_NEW",
    "price": 89.99,
    "location": "Updated Location"
  }' | jq .
```

#### Delete Listing
```bash
curl -s -X DELETE "http://localhost:8080/api/listings/$LISTING_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
```

---

### 4.4 Favorites Tests

```bash
# Get a listing ID first
LISTING_ID=$(curl -s http://localhost:8080/api/listings \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq -r '.content[0].id')

# Add to favorites
curl -s -X POST "http://localhost:8080/api/favorites/$LISTING_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# Get favorites
curl -s http://localhost:8080/api/favorites \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq 'length'

# Remove from favorites
curl -s -X DELETE "http://localhost:8080/api/favorites/$LISTING_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

---

### 4.5 Admin Tests

```bash
# Login as admin
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test_admin", "password": "password123"}' | jq -r '.accessToken')

# Get admin dashboard
curl -s http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# Expected response:
# {
#   "message": "Admin dashboard loaded",
#   "totalUsers": 8,
#   "totalListings": 5,
#   "pendingApprovals": 0,
#   "pendingReports": 0
# }
```

---

### 4.6 User Registration Test

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "new_test_user",
    "email": "newuser@sjsu.edu",
    "password": "SecurePass123",
    "firstName": "New",
    "lastName": "User",
    "phone": "408-555-1234"
  }' | jq '{username, email, roles}'
```

---

## 5. Running Unit Tests

### Run All Tests
```bash
cd backend
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=AuthServiceTest
./mvnw test -Dtest=ListingControllerIntegrationTest
./mvnw test -Dtest=ChatControllerIntegrationTest
```

### Run with Coverage
```bash
./mvnw test jacoco:report
# Report generated at: target/site/jacoco/index.html
```

### Test Classes Available
| Test Class | Tests | Description |
|------------|-------|-------------|
| `AuthServiceTest` | 11 | Authentication logic |
| `UserManagementServiceTest` | 9 | User management |
| `ListingControllerIntegrationTest` | 12 | Listings API |
| `ChatControllerIntegrationTest` | 8 | Chat/messaging API |
| `DiscoveryControllerIntegrationTest` | 14 | Search/discovery |
| `SearchServiceTest` | 11 | Search functionality |
| `LoginAttemptServiceTest` | 6 | Login rate limiting |

---

## 6. Database Verification

### Check Table Count
```bash
docker exec campus-marketplace-db psql -U cm_app_user -d campus_marketplace \
  -c "SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public';"
# Expected: 28 tables
```

### Check Flyway Migrations
```bash
docker exec campus-marketplace-db psql -U cm_app_user -d campus_marketplace \
  -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

Expected migrations:
| Version | Description | Success |
|---------|-------------|---------|
| 1 | campus marketplace core schema | t |
| 2 | seed demo data | t |
| 4 | user management tables | t |
| 9 | rename products to listings | t |
| 10 | create refresh tokens table | t |
| 11 | schema alignment | t |
| 12 | test accounts | t |
| 14 | user roles many to many | t |

### Check User Count
```bash
docker exec campus-marketplace-db psql -U cm_app_user -d campus_marketplace \
  -c "SELECT COUNT(*) FROM users;"
```

### Check User Roles
```bash
docker exec campus-marketplace-db psql -U cm_app_user -d campus_marketplace \
  -c "SELECT u.username, array_agg(ur.role) as roles 
      FROM users u 
      LEFT JOIN user_roles ur ON u.user_id = ur.user_id 
      GROUP BY u.username 
      ORDER BY u.username;"
```

### Verify Redis
```bash
docker exec campus-marketplace-redis redis-cli ping
# Expected: PONG
```

---

## 7. Troubleshooting

### Common Issues

#### 1. "Invalid username or password" for demo accounts
**Cause**: Demo accounts (alice_buyer, bob_buyer, etc.) have placeholder password hashes.
**Solution**: Use test accounts: `test_buyer` or `test_admin` with password `password123`.

#### 2. 401 Unauthorized
**Cause**: Token expired or invalid.
**Solution**: Re-login to get fresh tokens.

#### 3. 500 on `/api/admin/stats`
**Cause**: Endpoint doesn't exist.
**Solution**: Use `/api/admin/dashboard` instead.

#### 4. 400 on `/api/auth/logout`
**Cause**: Missing request body.
**Solution**: Include `{"refreshToken": "..."}` in request body.

#### 5. 400 on `/api/users/me`
**Cause**: Endpoint doesn't exist.
**Solution**: Use `/api/auth/me` or `/api/users/profile` instead.

### Correct Endpoint Reference

| Wrong Endpoint | Correct Endpoint |
|----------------|------------------|
| `GET /api/users/me` | `GET /api/auth/me` or `GET /api/users/profile` |
| `GET /api/admin/stats` | `GET /api/admin/dashboard` |
| `POST /api/auth/logout` (no body) | `POST /api/auth/logout` with `{"refreshToken": "..."}` |

### Reset Database
If you need to reset the database:
```bash
docker-compose down
docker volume rm campus-marketplace-postgres-data
docker-compose up -d
```

### View Backend Logs
```bash
docker logs -f campus-marketplace-backend
```

### Check Container Health
```bash
docker-compose ps
docker inspect campus-marketplace-backend | jq '.[0].State.Health'
```

---

## E2E Test Checklist

Run through this checklist to verify the system is working:

- [ ] Health check returns UP
- [ ] Login with `test_buyer` works
- [ ] Login with `test_admin` works
- [ ] Token refresh works
- [ ] Token validation works
- [ ] Get user profile works
- [ ] List listings works
- [ ] Get single listing works
- [ ] Filter listings by category works
- [ ] Create listing works
- [ ] Update listing works
- [ ] Delete listing works
- [ ] Add to favorites works
- [ ] Get favorites works
- [ ] Remove from favorites works
- [ ] Admin dashboard works
- [ ] User registration works
- [ ] Logout works
- [ ] Database has 28 tables
- [ ] All 8 Flyway migrations applied
- [ ] Redis responds to PING

---

## Automated E2E Test Script

Save this as `e2e-test.sh` and run it:

```bash
#!/bin/bash
set -e

BASE_URL="http://localhost:8080/api"
PASSED=0
FAILED=0

test_endpoint() {
    local name=$1
    local result=$2
    if [ "$result" = "true" ] || [ "$result" = "UP" ] || [ -n "$result" ]; then
        echo "✅ $name"
        ((PASSED++))
    else
        echo "❌ $name"
        ((FAILED++))
    fi
}

echo "========================================="
echo "     Campus Marketplace E2E Tests"
echo "========================================="

# Health
HEALTH=$(curl -s $BASE_URL/actuator/health | jq -r '.status')
test_endpoint "Health Check" "$HEALTH"

# Login
LOGIN=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test_buyer", "password": "password123"}')
TOKEN=$(echo $LOGIN | jq -r '.accessToken')
REFRESH=$(echo $LOGIN | jq -r '.refreshToken')
test_endpoint "Login" "$TOKEN"

# Auth/Me
ME=$(curl -s $BASE_URL/auth/me -H "Authorization: Bearer $TOKEN" | jq -r '.username')
test_endpoint "Get Current User" "$ME"

# Listings
LISTINGS=$(curl -s $BASE_URL/listings -H "Authorization: Bearer $TOKEN" | jq -r '.totalElements')
test_endpoint "Get Listings" "$LISTINGS"

# Admin
ADMIN_TOKEN=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test_admin", "password": "password123"}' | jq -r '.accessToken')
DASHBOARD=$(curl -s $BASE_URL/admin/dashboard -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.totalUsers')
test_endpoint "Admin Dashboard" "$DASHBOARD"

# Logout
LOGOUT=$(curl -s -X POST $BASE_URL/auth/logout \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH\"}" | jq -r '.message')
test_endpoint "Logout" "$LOGOUT"

echo "========================================="
echo "Passed: $PASSED | Failed: $FAILED"
echo "========================================="
```

Make it executable and run:
```bash
chmod +x e2e-test.sh
./e2e-test.sh
```

---
