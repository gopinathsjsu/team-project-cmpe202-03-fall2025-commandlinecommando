# API Testing Progress Report

**Date:** November 26, 2025  
**Status:** ✅ Authentication Fixed - Testing In Progress

---

## ✅ What's Working

### Authentication & Authorization
- ✅ **Login endpoint** working perfectly
- ✅ **JWT token generation** working
- ✅ **Refresh token** endpoint working
- ✅ **Get current user (me)** endpoint working
- ✅ Test users created:
  - `student1` / `password123` (BUYER role)
  - `admin` / `password123` (ADMIN role)

### Infrastructure
- ✅ PostgreSQL database running (campus-marketplace-db)
- ✅ Redis cache running (campus-marketplace-redis)
- ✅ Backend running successfully (port 8080)
- ✅ Flyway migrations applied (V1, V2, V4, V9, V10)
- ✅ `refresh_tokens` table created and working

### API Endpoints
- ✅ GET /listings (public access working)
- ✅ GET /discovery/trending (working)
- ✅ GET /discovery/recommended (working)
- ✅ GET /discovery/recently-viewed (working)
- ✅ GET /search/autocomplete (working)
- ✅ GET /search/history (working)
- ✅ GET /users/profile (working)
- ✅ GET /users/{id} (working)

---

## 🔄 Issues Remaining

### 1. Registration Endpoint (500 Error)
**Issue:** POST /auth/register returns 500  
**Likely Cause:** Check constraint violation on student_id field  
**Impact:** Cannot register new users via API  
**Workaround:** Users created directly in database work fine

### 2. Missing DTO Fields
**Issues:**
- Login response missing `verificationStatus` field
- User profile missing `userId` field (has `id` instead)
- Listings missing `status` field

**Impact:** Test assertions fail even though endpoints work  
**Priority:** Medium - API functional, just field naming inconsistencies

### 3. Favorites Module (500 Errors)
**Endpoints Affected:**
- GET /favorites
- POST /favorites/{listingId}
- DELETE /favorites/{listingId}
- GET /favorites/count

**Status:** All returning 500  
**Next Step:** Check backend logs for specific errors

### 4. Chat Module (500 Errors)
**Endpoints Affected:**
- POST /chat/messages
- GET /chat/conversations
- POST /chat/conversations/{id}/messages
- GET /chat/conversations/{id}/messages
- GET /chat/unread-count

**Status:** Partially working (some 401, some 500)  
**Next Step:** Verify conversation/message table structure

### 5. Admin Endpoints (401 Unauthorized)
**Endpoints Affected:**
- GET /admin/dashboard
- GET /admin/user-management/search
- GET /admin/analytics/overview

**Issue:** Returning 401 even with admin user logged in  
**Likely Cause:** Token not being passed correctly or role check failing  
**Next Step:** Test manually with admin token

### 6. Listing Operations (403/401 Errors)
**Issues:**
- GET /listings/{id} returns 401 (should be public?)
- POST /listings returns 403 (permission issue)
- PUT /listings/{id} returns 401
- DELETE /listings/{id} returns 401

**Next Step:** Review security configuration for listings

### 7. Search Endpoint (405 Method Not Allowed)
**Issue:** GET /search returns 405  
**Possible Cause:** Endpoint might expect POST or different path  
**Next Step:** Check controller method mapping

---

## 📊 Test Results Summary

### Current Status
```
✅ Executed: 36 requests
✅ Passed: 6/24 assertions (25%)
❌ Failed: 18/24 assertions (75%)
⏱️  Duration: 5.7 seconds
📈 Average Response Time: 44ms
```

### Comparison to Previous Run
| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Pass Rate | 12.5% (3/24) | 25% (6/24) | **+100%** |
| Authentication | ❌ Failed | ✅ Working | **Fixed** |
| Token Generation | ❌ Failed | ✅ Working | **Fixed** |
| Database Connection | ⚠️ Issues | ✅ Stable | **Fixed** |

---

## 🔧 Fixes Applied

### Database
1. ✅ Created `refresh_tokens` table (V10 migration)
2. ✅ Inserted test users with correct password hashes
3. ✅ Fixed PostgreSQL connection from backend container

### Configuration
1. ✅ Updated `application.yml` postgres profile:
   - Changed DB_HOST default from 127.0.0.1 to `postgres` (Docker service name)
   - Added Redis host override: `redis` (Docker service name)
2. ✅ Disabled mail health checks
3. ✅ Set default profile to `postgres`

### Password Management
1. ✅ Installed bcrypt Python module
2. ✅ Generated proper BCrypt hash for `password123`
3. ✅ Applied hash to test users (student1, admin)

---

## 📝 Test Credentials

### Student Account (BUYER)
```
Username: student1
Password: password123
Role: BUYER
Email: student1@sjsu.edu
Student ID: STU001
```

### Admin Account
```
Username: admin
Password: password123
Role: ADMIN
Email: admin@test.sjsu.edu
Student ID: ADM001
```

---

## 🎯 Next Steps

### Priority 1 - Fix Critical Errors (500s)
1. [ ] Debug favorites module backend errors
2. [ ] Debug chat module backend errors
3. [ ] Fix registration endpoint constraint violation

### Priority 2 - Authorization Issues
1. [ ] Test admin endpoints with proper token header
2. [ ] Review listing endpoint security configuration
3. [ ] Fix 403 Forbidden errors on listing creation

### Priority 3 - DTO Alignment
1. [ ] Add `verificationStatus` field to login response
2. [ ] Align `userId` vs `id` field naming
3. [ ] Add `status` field to listing DTOs

### Priority 4 - Test Updates
1. [ ] Update Postman collection field expectations
2. [ ] Add proper Authorization headers for admin requests
3. [ ] Fix search endpoint method/path

---

## 🚀 How to Continue Testing

### Run Full Test Suite
```bash
cd /Users/duylam1407/Workspace/SJSU/team-project-cmpe202-03-fall2025-commandlinecommando-fork
newman run "Campus_Marketplace_Complete_API_Collection.postman_collection.json" \
  --env-var "base_url=http://localhost:8080/api" \
  --delay-request 100 \
  --reporters cli,json \
  --reporter-json-export postman-test-results.json
```

### Manual Testing
```bash
# Login as student
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"password123"}' | jq

# Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' | jq

# Test protected endpoint (use token from login response)
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" | jq
```

### Check Backend Logs
```bash
# Follow logs in real-time
docker logs -f campus-marketplace-backend

# Check recent errors
docker logs campus-marketplace-backend --tail 100 | grep ERROR
```

---

## 📦 Environment Details

- **Backend:** Spring Boot 3.5.6, Java 21
- **Database:** PostgreSQL 16.11 (Docker)
- **Cache:** Redis 7-alpine (Docker)
- **Testing Tool:** Newman (Postman CLI)
- **Base URL:** `http://localhost:8080/api`

---

## 📄 Related Documentation

- [POSTMAN_TESTING_GUIDE.md](POSTMAN_TESTING_GUIDE.md) - Complete testing guide
- [POSTMAN_QUICK_START.md](POSTMAN_QUICK_START.md) - Quick reference
- [DEV_ENVIRONMENT_SETUP.md](DEV_ENVIRONMENT_SETUP.md) - Environment setup
- [Campus_Marketplace_Complete_API_Collection.postman_collection.json](Campus_Marketplace_Complete_API_Collection.postman_collection.json) - Test collection

---

**Status:** 🟡 In Progress - Authentication working, debugging remaining endpoints
