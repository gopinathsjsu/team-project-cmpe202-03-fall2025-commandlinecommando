# Campus Marketplace API - Postman Quick Start

## 📦 What You Get

1. **Campus_Marketplace_Complete_API_Collection.postman_collection.json** (1047 lines)
   - Complete Postman collection with 40+ API endpoints
   - Automated test scripts for response validation
   - Auto-managed variables (tokens, IDs)
   
2. **POSTMAN_TESTING_GUIDE.md**
   - Comprehensive 500+ line testing guide
   - Step-by-step testing workflows
   - Troubleshooting and best practices

## ⚡ Quick Start (3 Steps)

### Step 1: Import Collection
```
Postman → Import → Campus_Marketplace_Complete_API_Collection.postman_collection.json
```

### Step 2: Start Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Step 3: Test!
```
1. Open "1. Authentication" folder
2. Run "Login" request
3. Run other requests in any order
```

**Default credentials**: `student1` / `password123`

## 📊 Collection Contents

### 9 Organized Folders with 40+ Endpoints

| Folder | Endpoints | Key Features |
|--------|-----------|--------------|
| **1. Authentication** | 5 | Register, Login, Token refresh, Logout |
| **2. Listings** | 6 | CRUD operations with new DTO structure |
| **3. Favorites** | 4 | Toggle favorites, get user favorites |
| **4. Discovery** | 4 | Trending, Recommended, Similar listings |
| **5. Search** | 3 | Advanced search, Autocomplete, History |
| **6. Chat** | 5 | Messaging with string IDs, senderName |
| **7. User Profile** | 4 | Profile management, password change |
| **8. Admin** | 3 | Dashboard, analytics (requires ADMIN role) |
| **9. Reports** | 2 | Create and view reports |

## 🎯 Key Testing Points

### ✅ Validates Refactoring Changes

**1. DTO Structure**
- Nested `seller` object in listings
- String IDs (not UUID objects)
- `status` field: "ACTIVE"/"INACTIVE"
- `favorite` boolean flag

**2. API Paths**
- No `/api` prefix (removed from 4 controllers)
- Paths: `/listings`, `/favorites`, `/chat`, `/reports`

**3. Chat Updates**
- All IDs are strings (messageId, conversationId, senderId)
- Added `senderName` field to MessageResponse

**4. Favorites API**
- POST `/favorites/{id}` returns `{ favorited: boolean }`
- GET `/favorites` returns ListingDetailResponse[]

**5. Admin Dashboard**
- Added `pendingReports` field
- Message: "Admin dashboard loaded"

## 🧪 Automated Tests

Each request includes test scripts that automatically:
- ✅ Verify status codes (200, 201, 204)
- ✅ Validate response structure
- ✅ Check data types (strings, booleans, objects)
- ✅ Save variables (tokens, IDs) for next requests
- ✅ Validate nested objects

**No manual work needed!** Tests run automatically with each request.

## 📝 Example Test Flow

```
1. Login → Saves access_token
2. Get All Listings → Saves first listing_id
3. Get Listing by ID → Uses saved listing_id
4. Toggle Favorite → Adds to favorites
5. Get My Favorites → Verifies favorite appears
6. Send Message → Creates conversation, saves conversation_id
7. Get Conversations → Shows all chats
```

## 🔍 Sample Requests

### Login
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "student1",
  "password": "password123"
}
```

**Response**:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "userId": "uuid-string",
  "username": "student1",
  "role": "STUDENT",
  "tokenType": "Bearer"
}
```

### Get Listings (New DTO)
```http
GET http://localhost:8080/listings?page=0&size=10
Authorization: Bearer {{access_token}}
```

**Response**:
```json
{
  "content": [
    {
      "id": "uuid-string",
      "title": "MacBook Pro 2021",
      "seller": {
        "id": "uuid-string",
        "username": "student1",
        "name": "John Doe",
        "avatarUrl": "https://...",
        "rating": 4.5,
        "verificationStatus": "VERIFIED"
      },
      "status": "ACTIVE",
      "favorite": false,
      "images": [...]
    }
  ],
  "totalElements": 50,
  "totalPages": 5
}
```

### Toggle Favorite (New Response)
```http
POST http://localhost:8080/favorites/{{listing_id}}
Authorization: Bearer {{access_token}}
```

**Response**:
```json
{
  "favorited": true
}
```

## 🎓 Test Users

### Pre-seeded Accounts
```
Student: student1 / password123
Admin:   admin    / admin123
```

### Or Register New User
```http
POST http://localhost:8080/auth/register
{
  "username": "newuser",
  "email": "newuser@sjsu.edu",
  "password": "Test123!@#",
  "firstName": "New",
  "lastName": "User",
  "role": "STUDENT"
}
```

## 🔧 Variables (Auto-Managed)

These are automatically set by test scripts:
- `base_url` → http://localhost:8080
- `access_token` → JWT token from login
- `refresh_token` → Refresh token
- `user_id` → Current user ID
- `listing_id` → Most recent listing ID
- `conversation_id` → Most recent conversation ID

**You don't need to set these manually!**

## 🐛 Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| 401 Unauthorized | Run "Login" request again |
| 404 Not Found | Check path has no `/api` prefix |
| Connection refused | Start backend: `./mvnw spring-boot:run` |
| Listing not found | Run "Get All Listings" first |
| Admin tests fail | Login as "admin" user |

## 📊 Running All Tests

1. Right-click collection name
2. Select "Run Collection"
3. Set delay: 100ms
4. Click "Run"

**Expected**: All tests pass ✅

## 📚 Full Documentation

For detailed instructions, see:
- **POSTMAN_TESTING_GUIDE.md** - Complete testing guide
- **API_ENDPOINT_ALIGNMENT.md** - API path mapping
- **API_INTEGRATION_SUMMARY.md** - Integration details

## 🎉 Success Criteria

After running tests, you should see:
- ✅ All status codes correct (200, 201, 204)
- ✅ All IDs are strings (not UUID objects)
- ✅ Nested seller objects in listings
- ✅ Favorite toggle returns `{ favorited: boolean }`
- ✅ Chat messages have `senderName` field
- ✅ Admin dashboard has `pendingReports`
- ✅ No `/api` prefix in any paths
- ✅ All automated tests passing

## 🚀 Next Steps

1. ✅ Import collection into Postman
2. ✅ Start backend server
3. ✅ Run "Login" request
4. ✅ Test any endpoint
5. ✅ Run full collection for complete validation
6. ✅ Use for frontend integration testing

---

**Created**: November 2025  
**Backend Version**: Post-Refactoring  
**Total Endpoints**: 40+  
**Automated Tests**: ✅ Included  
**Status**: Ready to Use
