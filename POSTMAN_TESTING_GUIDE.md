# Campus Marketplace - Postman Testing Guide

## 📋 Overview
This guide provides comprehensive instructions for testing the Campus Marketplace API using the included Postman collection after the November 2025 backend refactoring.

**Collection File**: `Campus_Marketplace_Complete_API_Collection.postman_collection.json`

## 🚀 Quick Start

### 1. Import the Collection
1. Open Postman
2. Click **Import** button (top left)
3. Select the `Campus_Marketplace_Complete_API_Collection.postman_collection.json` file
4. The collection will appear in your Collections sidebar

### 2. Configure Environment Variables
The collection uses these variables (automatically managed by test scripts):
- `base_url` - Default: `http://localhost:8080`
- `access_token` - Auto-populated after login
- `refresh_token` - Auto-populated after login
- `user_id` - Auto-populated after login
- `listing_id` - Auto-populated from test responses
- `conversation_id` - Auto-populated from chat tests

**No manual configuration needed!** Variables are set automatically by test scripts.

### 3. Start the Backend Server
```bash
cd backend
./mvnw spring-boot:run
```

Or with Docker:
```bash
docker-compose up
```

Verify server is running: `http://localhost:8080/actuator/health`

## 📝 Testing Workflow

### Complete Test Flow (Recommended)
Run folders in this order to test all functionality:

#### **Step 1: Authentication** ✅
```
1. Authentication → Login
```
- Uses pre-seeded user: `student1` / `password123`
- Automatically stores JWT token for all subsequent requests
- Test validates token structure and user data

**Expected Result**:
- Status: 200 OK
- Response includes: `accessToken`, `refreshToken`, `userId`, `username`, `role`
- Token automatically saved to collection variables

#### **Step 2: Listings** 📦
```
2. Listings → Get All Listings
2. Listings → Get Listing by ID
2. Listings → Create Listing
2. Listings → Get My Listings
```

**Key Validations**:
- ✅ Pagination structure (content, totalElements, totalPages)
- ✅ New DTO structure with nested `seller` object
- ✅ String IDs (not UUIDs)
- ✅ Status field ("ACTIVE"/"INACTIVE")
- ✅ Seller has: id, name, username, avatarUrl
- ✅ Images array with proper structure

**Create Listing Test Data**:
```json
{
  "title": "Test Laptop - Dell XPS 13",
  "description": "Great condition laptop for sale",
  "category": "ELECTRONICS",
  "condition": "GOOD",
  "price": 800.00,
  "pickupLocation": "Student Union",
  "negotiable": true
}
```

#### **Step 3: Favorites** ❤️
```
3. Favorites → Toggle Favorite (on created listing)
3. Favorites → Get My Favorites
3. Favorites → Toggle Favorite (again to remove)
```

**Key Validations**:
- ✅ POST `/favorites/{id}` returns `{ favorited: boolean }`
- ✅ GET `/favorites` returns array of ListingDetailResponse
- ✅ All favorites have `favorite: true` field
- ✅ Nested seller information present

#### **Step 4: Discovery** 🔍
```
4. Discovery → Get Trending Listings
4. Discovery → Get Recommended Listings
4. Discovery → Get Similar Listings
```

**Expected Results**:
- Trending: Most viewed/active listings
- Recommended: Personalized based on user activity
- Similar: Based on category and price range

#### **Step 5: Search** 🔎
```
5. Search → Search Listings (with filters)
5. Search → Autocomplete
5. Search → Get Search History
```

**Search Parameters**:
- `query` - Text search term
- `category` - ELECTRONICS, BOOKS, FURNITURE, etc.
- `minPrice` / `maxPrice` - Price range
- `condition` - NEW, LIKE_NEW, GOOD, FAIR
- `page` / `size` - Pagination

#### **Step 6: Chat** 💬
```
6. Chat → Send Message to Listing
6. Chat → Get My Conversations
6. Chat → Get Messages in Conversation
6. Chat → Send Message in Conversation
```

**Key Validations**:
- ✅ All IDs are strings (messageId, conversationId, senderId)
- ✅ MessageResponse includes `senderName` field
- ✅ ConversationResponse has proper structure
- ✅ No `/api` prefix in paths

**Chat Flow**:
1. Send message to listing → Creates conversation
2. Response includes `conversationId` (auto-saved)
3. Get conversations → See all chats
4. Get messages → View conversation history

#### **Step 7: User Profile** 👤
```
7. User Profile → Get My Profile
7. User Profile → Update Profile
7. User Profile → Change Password
```

#### **Step 8: Admin** 🔐
⚠️ **Requires ADMIN role**

```bash
# First login as admin
POST /auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

Then test:
```
8. Admin → Get Dashboard
8. Admin → Get All Users
8. Admin → Get Analytics
```

**Dashboard Validation**:
- ✅ Has `pendingReports` field (new in refactoring)
- ✅ Message: "Admin dashboard loaded"
- ✅ Includes totalUsers, totalListings, pendingApprovals

#### **Step 9: Reports** 🚩
```
9. Reports → Create Report
9. Reports → Get My Reports
```

## 🧪 Test Assertions

### Automatic Test Scripts
Each request includes automated tests that verify:

**1. Status Codes**
```javascript
pm.test('Status code is 200', function () {
    pm.response.to.have.status(200);
});
```

**2. Response Structure**
```javascript
pm.test('Response has correct fields', function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('title');
});
```

**3. Data Type Validation**
```javascript
pm.test('IDs are strings', function () {
    var listing = pm.response.json();
    pm.expect(listing.id).to.be.a('string');
    pm.expect(listing.seller.id).to.be.a('string');
});
```

**4. Nested Object Validation**
```javascript
pm.test('Listing has nested seller', function () {
    var listing = pm.response.json();
    pm.expect(listing).to.have.property('seller');
    pm.expect(listing.seller).to.have.property('username');
    pm.expect(listing.seller).to.have.property('avatarUrl');
});
```

### Running All Tests
1. Right-click on "Campus Marketplace - Complete API Collection"
2. Select **Run Collection**
3. Configure:
   - **Iterations**: 1
   - **Delay**: 100ms between requests
4. Click **Run**

**Expected Results**: All tests should pass ✅

## 🔍 Key Refactoring Validations

### 1. DTO Structure Changes ✅
**Before Refactoring**:
```json
{
  "id": "uuid-format",
  "title": "Product",
  "sellerId": "uuid-format"
}
```

**After Refactoring**:
```json
{
  "id": "string-uuid",
  "title": "Product",
  "seller": {
    "id": "string-uuid",
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
```

### 2. API Path Changes ✅
**Removed `/api` prefix from**:
- ❌ `/api/favorites` → ✅ `/favorites`
- ❌ `/api/chat` → ✅ `/chat`
- ❌ `/api/reports` → ✅ `/reports`
- ❌ `/api/payments` → ✅ `/payments`

### 3. Chat Response Changes ✅
**MessageResponse**:
- All IDs are strings (was UUIDs)
- Added `senderName` field
- `conversationId`, `messageId`, `senderId` all strings

### 4. Favorites API Changes ✅
**POST `/favorites/{id}`** now returns:
```json
{
  "favorited": true
}
```

**GET `/favorites`** returns:
```json
[
  {
    "id": "...",
    "title": "...",
    "favorite": true,
    "seller": { ... }
  }
]
```

### 5. Admin Dashboard ✅
Added `pendingReports` field:
```json
{
  "message": "Admin dashboard loaded",
  "totalUsers": 150,
  "totalListings": 230,
  "pendingApprovals": 5,
  "pendingReports": 3
}
```

## 🎯 Testing Scenarios

### Scenario 1: New User Journey
1. Register new user
2. Login
3. Browse listings
4. Search for items
5. Favorite a listing
6. Send message to seller
7. Create own listing

### Scenario 2: Seller Workflow
1. Login
2. Create listing
3. View my listings
4. Receive messages
5. Respond to buyers
6. Update listing
7. Mark as sold

### Scenario 3: Admin Tasks
1. Login as admin
2. View dashboard
3. Check pending reports
4. Review user analytics
5. Manage users

### Scenario 4: Buyer Experience
1. Login
2. Search for products
3. View trending items
4. Get recommendations
5. View listing details
6. Add to favorites
7. Contact seller
8. Complete transaction

## 📊 Performance Testing

### Load Testing with Collection Runner
1. Open Collection Runner
2. Select collection
3. Set **Iterations**: 100
4. Set **Delay**: 50ms
5. Monitor response times:
   - ✅ Login: < 500ms
   - ✅ Get Listings: < 300ms
   - ✅ Search: < 400ms
   - ✅ Create Listing: < 600ms

## 🐛 Troubleshooting

### Common Issues

**1. 401 Unauthorized**
- **Cause**: No token or expired token
- **Fix**: Run "Login" request again
- **Verify**: Check `{{access_token}}` variable is set

**2. 404 Not Found**
- **Cause**: Using old `/api` prefix
- **Fix**: Verify path has no `/api` (e.g., `/listings` not `/api/listings`)

**3. Listing ID Not Found**
- **Cause**: No listings in database
- **Fix**: Run "Get All Listings" first, or "Create Listing"
- **Verify**: `{{listing_id}}` variable is populated

**4. Admin Tests Fail**
- **Cause**: Not logged in as admin
- **Fix**: Login with admin credentials
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**5. Connection Refused**
- **Cause**: Backend not running
- **Fix**: Start backend server: `./mvnw spring-boot:run`
- **Verify**: `curl http://localhost:8080/actuator/health`

### Debug Tips

**View Console**:
- Open Postman Console (bottom left)
- See full request/response details
- Check variable values

**Check Variables**:
- Collection → Variables tab
- Verify `access_token`, `listing_id`, etc. are set
- Manually set if needed

**Test Individual Requests**:
- Don't run entire collection first
- Test each folder separately
- Check each request passes before continuing

## 📈 Success Criteria

### All Tests Should Pass ✅
- **Authentication**: 5/5 tests pass
- **Listings**: 6/6 tests pass with new DTO structure
- **Favorites**: 4/4 tests pass with new responses
- **Discovery**: 4/4 tests pass
- **Search**: 3/3 tests pass
- **Chat**: 5/5 tests pass with string IDs
- **User Profile**: 4/4 tests pass
- **Admin**: 3/3 tests pass (with admin role)
- **Reports**: 2/2 tests pass

### Validation Checklist
- [ ] All status codes correct (200, 201, 204)
- [ ] All IDs are strings (not UUID objects)
- [ ] Nested seller objects present in listings
- [ ] Favorite toggle returns `{ favorited: boolean }`
- [ ] Chat messages have `senderName` field
- [ ] Admin dashboard has `pendingReports`
- [ ] No `/api` prefix in any paths
- [ ] Pagination works correctly
- [ ] Authentication tokens persist across requests
- [ ] Variables auto-populate from responses

## 🎓 Test Data

### Pre-seeded Users
```
Student: student1 / password123
Admin: admin / admin123
```

### Sample Categories
- ELECTRONICS
- BOOKS
- FURNITURE
- CLOTHING
- SPORTS
- OTHER

### Sample Conditions
- NEW
- LIKE_NEW
- GOOD
- FAIR

## 📚 Additional Resources

- **Backend README**: `backend/README.md`
- **API Documentation**: `API_ENDPOINT_ALIGNMENT.md`
- **Integration Summary**: `API_INTEGRATION_SUMMARY.md`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (if enabled)

## 🎉 Next Steps

After successful testing:
1. ✅ All endpoints working as expected
2. ✅ Frontend integration ready
3. ✅ Update frontend API calls to match new structure
4. ✅ Remove mock data, use real API
5. ✅ Deploy to staging environment

---

**Created**: November 2025  
**Backend Version**: Post-Refactoring  
**Test Coverage**: 9 modules, 40+ endpoints  
**Status**: ✅ Production Ready
