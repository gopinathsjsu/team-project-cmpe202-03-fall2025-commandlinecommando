# Campus Marketplace - End-to-End Test Manual

## Overview

This manual provides comprehensive end-to-end testing procedures for the Campus Marketplace application, covering all major features and user flows.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Authentication Tests](#authentication-tests)
4. [User Role Tests](#user-role-tests)
5. [Listing Management Tests](#listing-management-tests)
6. [Search & Discovery Tests](#search--discovery-tests)
7. [Favorites Tests](#favorites-tests)
8. [Chat & Messaging Tests](#chat--messaging-tests)
9. [Reports Tests](#reports-tests)
10. [Admin Dashboard Tests](#admin-dashboard-tests)
11. [Automated Test Execution](#automated-test-execution)

---

## Prerequisites

### Required Software
- Docker Desktop (running)
- Node.js 18+ and npm
- Java 21+
- Maven 3.8+
- Newman (Postman CLI): `npm install -g newman`

### Test Accounts

| Username | Password | Roles | Description |
|----------|----------|-------|-------------|
| `student` | `password123` | BUYER, SELLER | Standard student account |
| `admin` | `admin123` | ADMIN | Administrator account |
| `alice_chen` | `password123` | BUYER, SELLER | Test seller account |
| `bob_martinez` | `password123` | BUYER, SELLER | Test buyer account |

---

## Environment Setup

### 1. Start Infrastructure Services

```bash
# From project root
cd /path/to/project

# Start PostgreSQL and Redis
docker-compose up -d postgres redis

# Verify services are running
docker ps
```

Expected output:
```
CONTAINER ID   IMAGE                  STATUS         PORTS
xxxxx          postgres:16-alpine     Up (healthy)   0.0.0.0:5432->5432/tcp
xxxxx          redis:7-alpine         Up (healthy)   0.0.0.0:6379->6379/tcp
```

### 2. Start Backend Server

**Option A: Using Docker (Recommended)**
```bash
docker-compose up -d backend

# Check logs
docker logs -f campus-marketplace-backend
```

**Option B: Running Locally**
```bash
cd backend
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

### 3. Verify Backend Health

```bash
curl http://localhost:8080/api/actuator/health
```

Expected: `{"status":"UP"}`

### 4. Start Frontend (Optional - for UI testing)

```bash
cd frontend
npm install
npm run dev
```

Frontend available at: `http://localhost:5173`

---

## Authentication Tests

### Test A1: User Registration

**Endpoint:** `POST /api/auth/register`

**Test Steps:**
1. Send registration request with new user data
2. Verify response contains access token and refresh token
3. Verify user is assigned BUYER and SELLER roles automatically

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newstudent_'$(date +%s)'",
    "email": "newstudent_'$(date +%s)'@sjsu.edu",
    "password": "password123",
    "firstName": "New",
    "lastName": "Student"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "<jwt-token>",
  "refreshToken": "<refresh-token>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "roles": ["BUYER", "SELLER"],
  "username": "newstudent_xxx",
  "userId": "<uuid>"
}
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Response contains `accessToken`
- [ ] Response contains `refreshToken`
- [ ] `roles` array contains both `BUYER` and `SELLER`
- [ ] `tokenType` is `Bearer`

---

### Test A2: User Login

**Endpoint:** `POST /api/auth/login`

**Test Steps:**
1. Login with valid credentials
2. Verify JWT tokens are returned
3. Verify roles are included in response

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "<jwt-token>",
  "refreshToken": "<refresh-token>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "roles": ["BUYER", "SELLER"],
  "username": "student",
  "userId": "<uuid>",
  "email": "student@sjsu.edu",
  "firstName": "John",
  "lastName": "Student"
}
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] `roles` is an array (not a string)
- [ ] Student has both BUYER and SELLER roles

---

### Test A3: Admin Login

**Endpoint:** `POST /api/auth/login`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "<jwt-token>",
  "roles": ["ADMIN"],
  "username": "admin"
}
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] `roles` array contains only `ADMIN`
- [ ] Admin does NOT have BUYER or SELLER roles

---

### Test A4: Get Current User

**Endpoint:** `GET /api/auth/me`

**Request:**
```bash
# First login to get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "student", "password": "password123"}' \
  | jq -r '.accessToken')

# Get current user
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Response contains user details
- [ ] `roles` is an array with user's roles

---

### Test A5: Token Refresh

**Endpoint:** `POST /api/auth/refresh`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refresh-token-from-login>"
  }'
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] New access token is returned
- [ ] New refresh token is returned

---

### Test A6: Logout

**Endpoint:** `POST /api/auth/logout`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refresh-token>"
  }'
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Refresh token is invalidated

---

## User Role Tests

### Test R1: Verify Many-to-Many Roles

**Purpose:** Verify students have both BUYER and SELLER roles

**Test Steps:**
1. Login as a student
2. Decode the JWT token
3. Verify `roles` claim contains an array

**JWT Payload Verification:**
```bash
# Decode JWT (middle part, base64)
echo $TOKEN | cut -d. -f2 | base64 -d 2>/dev/null | jq .
```

**Expected JWT Payload:**
```json
{
  "sub": "student",
  "roles": ["BUYER", "SELLER"],
  "userId": "<uuid>",
  "email": "student@sjsu.edu",
  "exp": 1234567890
}
```

---

### Test R2: Role-Based Access Control - Admin Only

**Purpose:** Verify admin endpoints reject non-admin users

**Test Steps:**
1. Login as student
2. Attempt to access admin dashboard
3. Verify 403 Forbidden

**Request:**
```bash
# Login as student
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "student", "password": "password123"}' \
  | jq -r '.accessToken')

# Try admin endpoint
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $TOKEN"
```

**Expected:** 403 Forbidden

---

### Test R3: Role-Based Access Control - Admin Success

**Purpose:** Verify admin can access admin endpoints

**Request:**
```bash
# Login as admin
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}' \
  | jq -r '.accessToken')

# Access admin dashboard
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Expected:** 200 OK with dashboard data

---

## Listing Management Tests

### Test L1: Get All Listings

**Endpoint:** `GET /api/listings`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/listings?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Response has pagination (`totalElements`, `totalPages`, `number`, `size`)
- [ ] `content` array contains listing objects
- [ ] Each listing has `id`, `title`, `price`, `seller` info

---

### Test L2: Get Listing by ID

**Endpoint:** `GET /api/listings/{id}`

**Request:**
```bash
# Get a listing ID first
LISTING_ID=$(curl -s -X GET "http://localhost:8080/api/listings?page=0&size=1" \
  -H "Authorization: Bearer $TOKEN" \
  | jq -r '.content[0].id')

# Get listing details
curl -X GET "http://localhost:8080/api/listings/$LISTING_ID" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Response contains full listing details
- [ ] Seller information is included

---

### Test L3: Create Listing (Seller Role)

**Endpoint:** `POST /api/listings`

**Request:**
```bash
curl -X POST http://localhost:8080/api/listings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Textbook for Sale",
    "description": "Like new condition, barely used",
    "category": "TEXTBOOKS",
    "condition": "LIKE_NEW",
    "price": 45.00,
    "location": "SJSU Library",
    "negotiable": true
  }'
```

**Validation Checklist:**
- [ ] Status code is 201 Created
- [ ] Response contains new listing with generated ID
- [ ] `sellerId` matches current user

---

### Test L4: Update Listing

**Endpoint:** `PUT /api/listings/{id}`

**Request:**
```bash
curl -X PUT "http://localhost:8080/api/listings/$LISTING_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Textbook Title",
    "price": 40.00
  }'
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Listing is updated with new values

---

### Test L5: Delete Listing

**Endpoint:** `DELETE /api/listings/{id}`

**Request:**
```bash
curl -X DELETE "http://localhost:8080/api/listings/$LISTING_ID" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200 or 204
- [ ] Listing is no longer accessible

---

### Test L6: Get My Listings

**Endpoint:** `GET /api/listings/my-listings`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/listings/my-listings?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Only listings owned by current user are returned

---

## Search & Discovery Tests

### Test S1: Search Listings

**Endpoint:** `POST /api/search`

**Request:**
```bash
curl -X POST http://localhost:8080/api/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "laptop",
    "category": "ELECTRONICS",
    "minPrice": 100,
    "maxPrice": 1000
  }'
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Results match search criteria
- [ ] Pagination is included

---

### Test S2: Autocomplete

**Endpoint:** `GET /api/search/autocomplete`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/search/autocomplete?query=lap" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Suggestions are returned

---

### Test S3: Get Trending Listings

**Endpoint:** `GET /api/discovery/trending`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/discovery/trending?limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Trending listings are returned

---

### Test S4: Get Recommended Listings

**Endpoint:** `GET /api/discovery/recommended`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/discovery/recommended?limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Personalized recommendations returned

---

### Test S5: Get Similar Listings

**Endpoint:** `GET /api/discovery/similar/{listingId}`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/discovery/similar/$LISTING_ID?limit=5" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Similar items are returned

---

## Favorites Tests

### Test F1: Get My Favorites

**Endpoint:** `GET /api/favorites`

**Request:**
```bash
curl -X GET http://localhost:8080/api/favorites \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Returns array of favorited listings
- [ ] Each item has `favorite: true`

---

### Test F2: Toggle Favorite (Add)

**Endpoint:** `POST /api/favorites/{listingId}`

**Request:**
```bash
curl -X POST "http://localhost:8080/api/favorites/$LISTING_ID" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Response shows `favorited: true`

---

### Test F3: Remove from Favorites

**Endpoint:** `DELETE /api/favorites/{listingId}`

**Request:**
```bash
curl -X DELETE "http://localhost:8080/api/favorites/$LISTING_ID" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 204 No Content

---

### Test F4: Get Favorite Count

**Endpoint:** `GET /api/favorites/count`

**Request:**
```bash
curl -X GET http://localhost:8080/api/favorites/count \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Returns count of favorites

---

## Chat & Messaging Tests

### Test C1: Send Message to Listing

**Endpoint:** `POST /api/chat/messages`

**Request:**
```bash
curl -X POST http://localhost:8080/api/chat/messages \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "listingId": "'$LISTING_ID'",
    "content": "Hi, is this still available?"
  }'
```

**Validation Checklist:**
- [ ] Status code is 201 Created
- [ ] Message ID is returned (as string)
- [ ] Conversation is created if new

---

### Test C2: Get My Conversations

**Endpoint:** `GET /api/chat/conversations`

**Request:**
```bash
curl -X GET http://localhost:8080/api/chat/conversations \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Conversations have string IDs
- [ ] Each conversation shows last message
- [ ] Unread count is included

---

### Test C3: Get Messages in Conversation

**Endpoint:** `GET /api/chat/conversations/{conversationId}/messages`

**Request:**
```bash
CONV_ID=$(curl -s -X GET http://localhost:8080/api/chat/conversations \
  -H "Authorization: Bearer $TOKEN" \
  | jq -r '.[0].conversationId')

curl -X GET "http://localhost:8080/api/chat/conversations/$CONV_ID/messages" \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Messages are returned in order
- [ ] Each message has sender info

---

### Test C4: Get Unread Count

**Endpoint:** `GET /api/chat/unread-count`

**Request:**
```bash
curl -X GET http://localhost:8080/api/chat/unread-count \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Returns unread message count

---

## Reports Tests

### Test RP1: Create Report

**Endpoint:** `POST /api/reports`

**Request:**
```bash
curl -X POST http://localhost:8080/api/reports \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "listingId": "'$LISTING_ID'",
    "reportType": "SPAM",
    "description": "This listing appears to be spam"
  }'
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Report ID is returned
- [ ] Status is PENDING

---

### Test RP2: Get My Reports

**Endpoint:** `GET /api/reports/my-reports`

**Request:**
```bash
curl -X GET http://localhost:8080/api/reports/my-reports \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Returns reports created by current user

---

## Admin Dashboard Tests

### Test AD1: Get Dashboard Overview

**Endpoint:** `GET /api/admin/dashboard`

**Request:**
```bash
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Contains `totalUsers`, `activeListings`, `pendingReports`

---

### Test AD2: Get All Users (Admin)

**Endpoint:** `GET /api/admin/user-management/search`

**Request:**
```bash
curl -X GET "http://localhost:8080/api/admin/user-management/search?page=0&size=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Returns paginated list of users
- [ ] Each user has `roles` array

---

### Test AD3: Get Analytics

**Endpoint:** `GET /api/admin/analytics/overview`

**Request:**
```bash
curl -X GET http://localhost:8080/api/admin/analytics/overview \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Contains analytics data

---

## User Profile Tests

### Test UP1: Get My Profile

**Endpoint:** `GET /api/users/profile`

**Request:**
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $TOKEN"
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Returns user profile with `roles` array

---

### Test UP2: Update Profile

**Endpoint:** `PUT /api/users/profile`

**Request:**
```bash
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Updated",
    "lastName": "Name",
    "phone": "555-1234"
  }'
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Profile is updated

---

### Test UP3: Change Password

**Endpoint:** `POST /api/users/change-password`

**Request:**
```bash
curl -X POST http://localhost:8080/api/users/change-password \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "password123",
    "newPassword": "newpassword123"
  }'
```

**Validation Checklist:**
- [ ] Status code is 200
- [ ] Can login with new password

---

## Automated Test Execution

### Run All API Tests with Newman

```bash
cd /path/to/project

# Run complete API collection
npx newman run docs/postman/Campus_Marketplace_Complete_API_Collection.postman_collection.json \
  --reporters cli,json \
  --reporter-json-export test-results/newman-results.json
```

### Run Backend Unit Tests

```bash
cd backend
./mvnw test
```

**Expected Output:**
```
Tests run: 129, Failures: 0, Errors: 0, Skipped: 2
```

### Run Frontend Build Verification

```bash
cd frontend
npm run build
```

**Expected Output:**
```
✓ 109 modules transformed
✓ built in xxxms
```

---

## Test Result Summary Template

| Test Category | Total Tests | Passed | Failed | Notes |
|---------------|-------------|--------|--------|-------|
| Authentication | 6 | | | |
| User Roles | 3 | | | |
| Listings | 6 | | | |
| Search & Discovery | 5 | | | |
| Favorites | 4 | | | |
| Chat & Messaging | 4 | | | |
| Reports | 2 | | | |
| Admin Dashboard | 3 | | | |
| User Profile | 3 | | | |
| **TOTAL** | **36** | | | |

---

## Troubleshooting

### Common Issues

#### 1. Database Connection Error
```
FATAL: database "campus_marketplace" does not exist
```
**Solution:** Ensure PostgreSQL container is running and database is created:
```bash
docker exec -it campus-marketplace-db psql -U cm_app_user -d postgres -c "CREATE DATABASE campus_marketplace;"
```

#### 2. 401 Unauthorized
**Solution:** Check that:
- Token is not expired
- Token is correctly formatted: `Authorization: Bearer <token>`
- User account is active

#### 3. 403 Forbidden
**Solution:** Verify user has required role for the endpoint:
- Admin endpoints require ADMIN role
- Listing creation requires SELLER role

#### 4. Redis Connection Error
**Solution:** Ensure Redis container is running:
```bash
docker-compose up -d redis
```

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-11-27 | Initial version with many-to-many roles support |

---

## Contact

For test-related issues, contact the development team or create an issue in the repository.
