# API Documentation

Complete REST API reference for the Campus Marketplace backend.

**Base URL:** `http://localhost:8080/api`

## Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <access_token>
```

---

## Auth Endpoints

### Login

```
POST /auth/login
```

Request:
```json
{
  "username": "test_buyer",
  "password": "password123"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "userId": "uuid",
    "username": "test_buyer",
    "email": "test@example.com",
    "roles": ["BUYER", "SELLER"]
  }
}
```

### Register

```
POST /auth/register
```

Request:
```json
{
  "username": "newuser",
  "email": "user@sjsu.edu",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "universityId": "uuid"
}
```

### Refresh Token

```
POST /auth/refresh
```

Request:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

### Logout

```
POST /auth/logout
Authorization: Bearer <token>
```

### Request Password Reset

```
POST /auth/forgot-password
```

Request:
```json
{
  "email": "user@example.com"
}
```

---

## Listings Endpoints

### Get All Listings

```
GET /listings?page=0&size=20&category=ELECTRONICS
```

Query Parameters:
- `page` - Page number (default: 0)
- `size` - Items per page (default: 20)
- `category` - Filter by category

Response:
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "MacBook Pro",
      "price": 1200.00,
      "category": "ELECTRONICS",
      "condition": "LIKE_NEW",
      "primaryImageUrl": "https://...",
      "seller": {
        "id": "uuid",
        "username": "seller1"
      }
    }
  ],
  "totalElements": 50,
  "totalPages": 3
}
```

### Get Single Listing

```
GET /listings/{id}
```

### Create Listing

```
POST /listings
Authorization: Bearer <token>
```

Request:
```json
{
  "title": "iPhone 14 Pro",
  "description": "Excellent condition",
  "price": 800.00,
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "imageUrls": ["https://s3..."]
}
```

### Update Listing

```
PUT /listings/{id}
Authorization: Bearer <token>
```

### Delete Listing

```
DELETE /listings/{id}
Authorization: Bearer <token>
```

---

## Image Upload Endpoints

### Upload Images

```
POST /images/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

Form Data:
- `files` - Image files (max 5, max 5MB each)

Response:
```json
{
  "urls": [
    "https://bucket.s3.amazonaws.com/listings/uuid/image1.jpg"
  ]
}
```

### Upload to Listing

```
POST /images/listing/{listingId}
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

---

## Chat Endpoints

### Get Conversations

```
GET /chat/conversations
Authorization: Bearer <token>
```

Response:
```json
[
  {
    "conversationId": "uuid",
    "listingId": "uuid",
    "listingTitle": "MacBook Pro",
    "otherUser": {
      "id": "uuid",
      "username": "seller1"
    },
    "lastMessage": "Is this still available?",
    "unreadCount": 2,
    "updatedAt": "2024-01-15T10:00:00Z"
  }
]
```

### Get Messages

```
GET /chat/conversations/{id}/messages
Authorization: Bearer <token>
```

### Send Message

```
POST /chat/conversations/{id}/messages
Authorization: Bearer <token>
```

Request:
```json
{
  "content": "Hello, is this still available?"
}
```

### Start Conversation

```
POST /chat/conversations
Authorization: Bearer <token>
```

Request:
```json
{
  "listingId": "uuid",
  "message": "Hi, I'm interested in this item"
}
```

---

## User Endpoints

### Get Current User

```
GET /users/me
Authorization: Bearer <token>
```

### Update Profile

```
PUT /users/me
Authorization: Bearer <token>
```

Request:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "555-1234"
}
```

### Get User Listings

```
GET /users/me/listings
Authorization: Bearer <token>
```

---

## Favorites Endpoints

### Get Favorites

```
GET /favorites
Authorization: Bearer <token>
```

### Add to Favorites

```
POST /favorites/{listingId}
Authorization: Bearer <token>
```

### Remove from Favorites

```
DELETE /favorites/{listingId}
Authorization: Bearer <token>
```

---

## Search Endpoints

### Search Listings

```
GET /search?q=laptop&category=ELECTRONICS&minPrice=100&maxPrice=1000
```

Query Parameters:
- `q` - Search query
- `category` - Category filter
- `minPrice` - Minimum price
- `maxPrice` - Maximum price
- `condition` - Condition filter
- `sortBy` - Sort field (price, createdAt)
- `sortDir` - Sort direction (asc, desc)

### Autocomplete

```
GET /search/autocomplete?q=mac
```

---

## Admin Endpoints

Requires ADMIN role.

### Dashboard Stats

```
GET /admin/dashboard
Authorization: Bearer <token>
```

Response:
```json
{
  "totalUsers": 150,
  "totalListings": 320,
  "pendingApprovals": 5,
  "pendingReports": 3
}
```

### Get All Users

```
GET /admin/users?page=0&size=20
Authorization: Bearer <token>
```

### Suspend User

```
POST /admin/users/{id}/suspend
Authorization: Bearer <token>
```

Request:
```json
{
  "reason": "Violation of terms"
}
```

### Moderate Listing

```
POST /admin/moderate/{listingId}?action=reject&reason=Inappropriate
Authorization: Bearer <token>
```

Actions: `approve`, `reject`, `flag`

---

## Reports Endpoints

### Submit Report

```
POST /reports
Authorization: Bearer <token>
```

Request:
```json
{
  "reportType": "PRODUCT",
  "targetId": "uuid",
  "reason": "SPAM",
  "description": "This listing is spam"
}
```

### Get Reports (Admin)

```
GET /admin/reports
Authorization: Bearer <token>
```

---

## Error Responses

All errors follow this format:

```json
{
  "code": "ERROR_CODE",
  "message": "Human readable message",
  "status": 400,
  "path": "/api/endpoint"
}
```

Common error codes:
- `INVALID_CREDENTIALS` - 401
- `ACCESS_DENIED` - 403
- `NOT_FOUND` - 404
- `VALIDATION_ERROR` - 400

---

## Categories

Available product categories:

- ELECTRONICS
- BOOKS
- CLOTHING
- FURNITURE
- SPORTS
- SERVICES
- OTHER

## Conditions

Available product conditions:

- NEW
- LIKE_NEW
- GOOD
- FAIR
- POOR
