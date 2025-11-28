# Frontend-Backend Integration Testing

**Date:** November 26, 2025  
**Status:** 🔄 **Testing in Progress**

---

## 🔍 API Endpoint Mismatch Analysis

### Issues Found

#### 1. Listings Endpoints ❌

**Frontend expects:**
- `GET /student/listings` - Get all listings
- `GET /student/listings/search` - Search listings
- `GET /student/listings/{id}` - Get listing by ID
- `POST /student/listings` - Create listing
- `PUT /student/listings/{id}` - Update listing
- `DELETE /student/listings/{id}` - Delete listing
- `POST /student/listings/{id}/favorite` - Toggle favorite
- `GET /student/favorites` - Get favorites
- `POST /student/listings/{id}/report` - Report listing

**Backend provides:**
- `GET /listings` - Get all listings ✅
- `GET /search` or `GET /listings?category=X` - Search ✅
- `GET /listings/{id}` - Get listing by ID ✅
- `POST /listings` - Create listing ✅
- `PUT /listings/{id}` - Update listing ✅
- `DELETE /listings/{id}` - Delete listing ✅
- `POST /favorites/{id}` - Toggle favorite ✅
- `GET /favorites` - Get favorites ✅
- `POST /reports` - Create report ✅

**Action Required:** Update frontend to use `/listings` instead of `/student/listings`

#### 2. Chat Endpoints ⚠️

**Frontend expects:**
- `GET /chat/conversations` ✅
- `POST /chat/conversations` ✅
- `GET /chat/conversations/{conversationId}/messages` ✅
- `POST /chat/conversations/{conversationId}/messages` ✅
- `PUT /chat/conversations/{conversationId}/read` ⚠️ (needs verification)

**Backend provides:**
- `GET /chat/conversations` ✅
- `POST /chat/messages` (creates conversation) ⚠️ Different pattern
- `GET /chat/conversations/{conversationId}/messages` ✅
- `POST /chat/conversations/{conversationId}/messages` ✅
- `PUT /chat/messages/{messageId}/read` ⚠️ Different endpoint

**Action Required:** Align conversation creation and read endpoints

#### 3. Data Type Mismatches ⚠️

**Frontend expects:**
- `conversationId: number`
- `messageId: number`

**Backend provides:**
- `conversationId: UUID (string)`
- `messageId: UUID (string)`

**Action Required:** Update frontend types to use UUID strings

#### 4. Mock API Configuration ⚠️

**Current:** `USE_MOCK_API: true` (default)

**Action Required:** Disable mock API for production testing

---

## 🔧 Required Frontend Updates

### 1. Update API Configuration

**File:** `frontend/src/api/config.ts`

```typescript
export const API_CONFIG = {
  USE_MOCK_API: false,  // Change to false
  BACKEND_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  // Remove LISTING_API_URL and COMMUNICATION_URL (unified backend)
  TIMEOUT: 5000,
};
```

### 2. Update Listings API

**File:** `frontend/src/api/listings.ts`

Change all `/student/listings` to `/listings`:
- `GET /student/listings` → `GET /listings`
- `GET /student/listings/search` → `GET /search` or `GET /listings?category=X`
- `POST /student/listings/{id}/favorite` → `POST /favorites/{id}`
- `GET /student/favorites` → `GET /favorites`
- `POST /student/listings/{id}/report` → `POST /reports`

### 3. Update Chat Types

**File:** `frontend/src/api/chat.ts`

Change:
- `conversationId: number` → `conversationId: string`
- `messageId: number` → `messageId: string`

### 4. Update Vite Proxy

**File:** `frontend/vite.config.ts`

```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',  // Change from 3001 to 8080
    changeOrigin: true,
  },
}
```

---

## 🧪 Testing Plan

### Phase 1: Backend Verification ✅
- [x] Run Postman collection tests
- [x] Verify all endpoints respond correctly
- [x] Check health endpoint

### Phase 2: Frontend Configuration
- [ ] Update API config to disable mock API
- [ ] Update endpoint paths to match backend
- [ ] Fix type mismatches (UUID vs number)
- [ ] Update Vite proxy configuration

### Phase 3: Integration Testing
- [ ] Test authentication flow (login/register)
- [ ] Test listings CRUD operations
- [ ] Test favorites functionality
- [ ] Test chat/messaging
- [ ] Test search and discovery
- [ ] Test reporting

### Phase 4: End-to-End Testing
- [ ] Full user workflow: Register → Login → Browse → Create Listing → Chat → Purchase
- [ ] Error handling and edge cases
- [ ] Token refresh flow
- [ ] Logout functionality

---

## 📋 Test Results

### Backend Tests
**Status:** ✅ Running
**Results:** Pending...

### Frontend-Backend Integration
**Status:** 🔄 In Progress
**Issues Found:** See above

---

## 🚀 Quick Fixes

### Immediate Actions

1. **Disable Mock API:**
```typescript
// frontend/src/api/config.ts
USE_MOCK_API: false
```

2. **Update Base URL:**
```typescript
// frontend/src/api/client.ts
// Already correct: 'http://localhost:8080/api'
```

3. **Fix Listings Endpoints:**
```typescript
// frontend/src/api/listings.ts
// Change /student/listings to /listings
```

4. **Fix Favorites Endpoints:**
```typescript
// frontend/src/api/listings.ts
// Change /student/listings/{id}/favorite to /favorites/{id}
// Change /student/favorites to /favorites
```

---

**Last Updated:** November 26, 2025

