# Campus Marketplace - API Quick Reference

**Base URL:** `http://localhost:8080/api`

---

## 🔐 Authentication

### Register
```bash
POST /auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@sjsu.edu",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT",
  "universityDomain": "sjsu.edu"
}
```

### Login
```bash
POST /auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "SecurePass123!"
}

Response:
{
  "accessToken": "eyJhbG...",
  "refreshToken": "eyJhbG...",
  "userId": "uuid-here",
  "username": "johndoe",
  "role": "STUDENT"
}
```

---

## 📦 Listings

### Get All Listings (Public)
```bash
GET /listings?page=0&size=20&category=ELECTRONICS
```

### Get Listing by ID (Public)
```bash
GET /listings/{listingId}
```

### Create Listing (Authenticated)
```bash
POST /listings
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "MacBook Pro 2021",
  "description": "M1 Pro, 16GB RAM, 512GB SSD",
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "price": 1499.99,
  "location": "Engineering Building",
  "negotiable": true,
  "quantity": 1
}
```

**Categories:** `TEXTBOOKS`, `ELECTRONICS`, `FURNITURE`, `CLOTHING`, `SPORTS_EQUIPMENT`, `SERVICES`, `OTHER`

**Conditions:** `NEW`, `LIKE_NEW`, `GOOD`, `FAIR`, `POOR`

### Update Listing (Owner Only)
```bash
PUT /listings/{listingId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "MacBook Pro 2021 - Price Reduced!",
  "price": 1399.99
}
```

### Delete Listing (Owner Only)
```bash
DELETE /listings/{listingId}
Authorization: Bearer {token}
```

### Get My Listings
```bash
GET /listings/my-listings?page=0&size=20
Authorization: Bearer {token}
```

### Get Listings by Seller
```bash
GET /listings/seller/{sellerId}?page=0&size=20
```

---

## 💬 Communication (Chat)

### Create Conversation
```bash
POST /chat/conversations
Authorization: Bearer {token}
Content-Type: application/json

{
  "listingId": "uuid-of-listing",
  "sellerId": "uuid-of-seller"
}
```

### Get My Conversations
```bash
GET /chat/conversations
Authorization: Bearer {token}
```

### Send Message
```bash
POST /chat/messages
Authorization: Bearer {token}
Content-Type: application/json

{
  "conversationId": "uuid-of-conversation",
  "content": "Hi! Is this still available?"
}
```

### Get Conversation Messages
```bash
GET /chat/conversations/{conversationId}/messages
Authorization: Bearer {token}
```

### Get Unread Count
```bash
GET /chat/unread-count
Authorization: Bearer {token}

Response:
{
  "unreadCount": 5
}
```

### Mark Message as Read
```bash
PUT /chat/messages/{messageId}/read
Authorization: Bearer {token}
```

---

## 👤 User Profile

### Get My Profile
```bash
GET /users/profile
Authorization: Bearer {token}
```

### Update Profile
```bash
PUT /users/profile
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+14155551234",
  "bio": "SJSU Computer Science student"
}
```

---

## 🏫 Student Dashboard

### Get Dashboard
```bash
GET /student/dashboard
Authorization: Bearer {token}

Response:
{
  "message": "Welcome to Student Dashboard",
  "userId": "uuid",
  "username": "johndoe",
  "myListings": 5,
  "watchlist": 12,
  "messages": 3
}
```

---

## 🔍 Search & Discovery

### Search Products
```bash
GET /search?q=macbook&page=0&size=20
Authorization: Bearer {token}
```

### Trending Products
```bash
GET /discovery/trending?limit=10
```

### Recommended Products
```bash
GET /discovery/recommended?limit=10
Authorization: Bearer {token}
```

---

## ❤️ Health Check

### Actuator Health
```bash
GET /actuator/health

Response:
{
  "status": "UP"
}
```

---

## 🔑 Authentication Headers

All authenticated endpoints require:
```
Authorization: Bearer {access_token}
```

---

## 📊 Common Response Formats

### Success Response
```json
{
  "message": "Operation successful",
  "data": { ... }
}
```

### Error Response
```json
{
  "error": "ERROR_CODE",
  "message": "Human readable message",
  "status": 400,
  "timestamp": "2025-11-26T12:00:00Z",
  "path": "/api/listings/123"
}
```

### Paginated Response
```json
{
  "content": [ ... ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

---

## 🚀 Quick Start Examples

### Complete Flow: Create Account → Create Listing → Start Chat

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "seller1",
    "email": "seller1@sjsu.edu",
    "password": "Pass123!",
    "firstName": "Seller",
    "lastName": "One",
    "role": "STUDENT",
    "universityDomain": "sjsu.edu"
  }'

# Save the token from response

# 2. Create Listing
curl -X POST http://localhost:8080/api/listings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Calculus Textbook",
    "description": "Stewart Calculus 8th Edition",
    "category": "TEXTBOOKS",
    "condition": "GOOD",
    "price": 45.00,
    "location": "Library",
    "negotiable": true
  }'

# Save the listing ID from response

# 3. Another user starts a conversation
curl -X POST http://localhost:8080/api/chat/conversations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer BUYER_TOKEN" \
  -d '{
    "listingId": "LISTING_UUID",
    "sellerId": "SELLER_UUID"
  }'

# 4. Send a message
curl -X POST http://localhost:8080/api/chat/messages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer BUYER_TOKEN" \
  -d '{
    "conversationId": "CONVERSATION_UUID",
    "content": "Is this book still available?"
  }'
```

---

## 📱 Postman Collection

Import the ready-to-use Postman collection:
```
Campus_Marketplace_API_Collection.postman_collection.json
```

The collection includes:
- Pre-configured requests
- Auto-token management
- Test assertions
- Environment variables

---

## 🐛 Common Issues & Solutions

### 401 Unauthorized
- Check if token is expired (1 hour default)
- Ensure `Authorization: Bearer {token}` header is included
- Verify token format (no extra spaces)

### 403 Forbidden
- Check user role (STUDENT vs ADMIN)
- Verify ownership (can only edit/delete own listings)

### 404 Not Found
- Verify UUID format is correct
- Check if resource exists in database
- Ensure using correct endpoint path

### 500 Internal Server Error
- Check application logs: `docker logs campus-marketplace-backend`
- Verify database connection
- Check required fields in request body

---

## 📚 Additional Resources

- [ENHANCEMENT_SUMMARY.md](ENHANCEMENT_SUMMARY.md) - Detailed implementation guide
- [REFACTORING_STATUS.md](REFACTORING_STATUS.md) - Refactoring completion status
- [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) - Deployment instructions
- [Swagger UI](http://localhost:8080/api/swagger-ui.html) - Interactive API docs (when running)

---

**Last Updated:** November 26, 2025
