# Campus Marketplace Backend API Documentation

**Version:** 1.1.0  
**Base URL:** `http://localhost:8080/api`  
**Last Updated:** December 2025

> ⚠️ **Important Changes in v1.1.0:**
> - Discovery endpoints return specific field names (`trending`, `recommended`, `recentlyViewed`, `similar`)
> - Chat endpoints return arrays directly, not wrapped objects
> - Search endpoint uses `results` field in response
> - All IDs are UUID strings

---

## Table of Contents
1. [Authentication](#1-authentication)
2. [User Profile](#2-user-profile)
3. [Listings](#3-listings)
4. [Search & Discovery](#4-search--discovery)
5. [Favorites](#5-favorites)
6. [Chat & Messaging](#6-chat--messaging)
7. [Orders & Cart](#7-orders--cart)
8. [Payments](#8-payments)
9. [Reports](#9-reports)
10. [Admin](#10-admin)
11. [Common Response Formats](#11-common-response-formats)
12. [Error Handling](#12-error-handling)

---

## Authentication Header

All authenticated endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <access_token>
```

---

## 1. Authentication

### 1.1 Login
**POST** `/api/auth/login`

Authenticate a user and receive access tokens.

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Success Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "roles": ["BUYER", "SELLER"],
  "active": true,
  "verificationStatus": "VERIFIED",
  "studentId": "string",
  "major": "string",
  "graduationYear": 2025,
  "avatarUrl": "string",
  "createdAt": "2025-01-01T00:00:00Z",
  "lastLoginAt": "2025-11-27T00:00:00Z"
}
```

**Error Response (401):**
```json
{
  "error": "Authentication failed",
  "message": "Invalid username or password"
}
```

---

### 1.2 Register
**POST** `/api/auth/register`

Register a new student user. New users automatically receive BUYER and SELLER roles.

**Request Body:**
```json
{
  "username": "string (3-50 chars, required)",
  "email": "string (valid email, required)",
  "password": "string (8+ chars, at least 1 letter and 1 number, required)",
  "firstName": "string (required)",
  "lastName": "string (required)",
  "phone": "string (optional)",
  "studentId": "string (optional)",
  "major": "string (optional)",
  "graduationYear": 2025,
  "campusLocation": "string (optional)"
}
```

**Success Response (200):**
```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "roles": ["BUYER", "SELLER"],
  "active": true
}
```

---

### 1.3 Refresh Token
**POST** `/api/auth/refresh`

Refresh an expired access token using a valid refresh token.

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Success Response (200):**
```json
{
  "accessToken": "new_access_token",
  "refreshToken": "new_refresh_token",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

### 1.4 Logout
**POST** `/api/auth/logout`

Invalidate the current refresh token.

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Success Response (200):**
```json
{
  "message": "Logged out successfully"
}
```

---

### 1.5 Logout All Devices
**POST** `/api/auth/logout-all`

🔒 **Requires Authentication**

Invalidate all refresh tokens for the current user.

**Success Response (200):**
```json
{
  "message": "Logged out from all devices successfully"
}
```

---

### 1.6 Get Current User
**GET** `/api/auth/me`

🔒 **Requires Authentication**

Get the currently authenticated user's profile.

**Success Response (200):**
```json
{
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "roles": ["BUYER", "SELLER"],
  "active": true,
  "verificationStatus": "VERIFIED",
  "studentId": "string",
  "major": "string",
  "graduationYear": 2025,
  "avatarUrl": "string"
}
```

---

### 1.7 Validate Token
**GET** `/api/auth/validate`

🔒 **Requires Authentication**

Check if the current access token is valid.

**Success Response (200):**
```json
{
  "valid": true,
  "username": "string",
  "authorities": [{"authority": "ROLE_BUYER"}, {"authority": "ROLE_SELLER"}]
}
```

---

### 1.8 Forgot Password
**POST** `/api/auth/forgot-password`

Request a password reset email.

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Success Response (200):**
```json
{
  "message": "Password reset email sent"
}
```

---

### 1.9 Reset Password
**POST** `/api/auth/reset-password`

Reset password using the token from email.

**Request Body:**
```json
{
  "token": "reset_token_from_email",
  "newPassword": "string"
}
```

**Success Response (200):**
```json
{
  "message": "Password reset successfully"
}
```

---

## 2. User Profile

### 2.1 Get My Profile
**GET** `/api/users/profile`

🔒 **Requires Authentication**

**Success Response (200):**
```json
{
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "phone": "string",
  "roles": ["BUYER", "SELLER"],
  "active": true,
  "verificationStatus": "VERIFIED",
  "studentId": "string",
  "major": "string",
  "graduationYear": 2025,
  "avatarUrl": "string",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

---

### 2.2 Get User by ID
**GET** `/api/users/{userId}`

🔒 **Requires Authentication**

**Path Parameters:**
- `userId` (UUID): User ID

---

### 2.3 Update Profile
**PUT** `/api/users/profile`

🔒 **Requires Authentication**

**Request Body:**
```json
{
  "firstName": "string",
  "lastName": "string",
  "phone": "string",
  "studentId": "string",
  "major": "string",
  "graduationYear": 2025,
  "avatarUrl": "string"
}
```

---

### 2.4 Change Password
**POST** `/api/users/change-password`

🔒 **Requires Authentication**

**Request Body:**
```json
{
  "currentPassword": "string",
  "newPassword": "string (8+ chars, at least 1 letter and 1 number)",
  "confirmPassword": "string (must match newPassword)"
}
```

**Success Response (200):**
```json
{
  "message": "Password changed successfully"
}
```

---

### 2.5 Deactivate Account
**POST** `/api/users/deactivate`

🔒 **Requires Authentication**

Soft delete account with 30-day recovery period.

---

## 3. Listings

### 3.1 Get All Listings
**GET** `/api/listings`

Get paginated list of all active listings.

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Items per page
- `category` (string, optional): Filter by category

**Category Values:**
- `ELECTRONICS`
- `BOOKS`
- `CLOTHING`
- `FURNITURE`
- `SPORTS`
- `MUSIC`
- `GAMES`
- `OTHER`

**Success Response (200):**
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "MacBook Pro 2023",
      "description": "Like new condition",
      "price": 1200.00,
      "category": "ELECTRONICS",
      "condition": "LIKE_NEW",
      "status": "ACTIVE",
      "images": ["https://example.com/image1.jpg"],
      "seller": {
        "id": "uuid",
        "username": "seller_user",
        "firstName": "John",
        "lastName": "Doe",
        "avatarUrl": "string",
        "rating": 4.5,
        "reviewCount": 10
      },
      "location": "San Jose, CA",
      "createdAt": "2025-11-27T00:00:00Z",
      "updatedAt": "2025-11-27T00:00:00Z",
      "viewCount": 150,
      "favoriteCount": 25,
      "favorite": false
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

---

### 3.2 Get Listing by ID
**GET** `/api/listings/{id}`

**Path Parameters:**
- `id` (UUID): Listing ID

**Success Response (200):**
```json
{
  "id": "uuid",
  "title": "MacBook Pro 2023",
  "description": "Like new condition, includes charger",
  "price": 1200.00,
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "status": "ACTIVE",
  "images": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"],
  "seller": {
    "id": "uuid",
    "username": "seller_user",
    "firstName": "John",
    "lastName": "Doe",
    "avatarUrl": "string",
    "rating": 4.5,
    "reviewCount": 10
  },
  "location": "San Jose, CA",
  "createdAt": "2025-11-27T00:00:00Z",
  "updatedAt": "2025-11-27T00:00:00Z",
  "viewCount": 150,
  "favoriteCount": 25,
  "favorite": false
}
```

---

### 3.3 Create Listing
**POST** `/api/listings`

🔒 **Requires Authentication** (SELLER or ADMIN role)

**Request Body:**
```json
{
  "title": "string (required)",
  "description": "string (required)",
  "price": 100.00,
  "category": "ELECTRONICS",
  "condition": "NEW",
  "images": ["https://example.com/image1.jpg"],
  "location": "San Jose, CA"
}
```

**Condition Values:**
- `NEW`
- `LIKE_NEW`
- `GOOD`
- `FAIR`
- `POOR`

**Success Response (201):**
```json
{
  "message": "Listing created successfully",
  "listing": { ... }
}
```

---

### 3.4 Update Listing
**PUT** `/api/listings/{id}`

🔒 **Requires Authentication** (Owner or ADMIN)

**Path Parameters:**
- `id` (UUID): Listing ID

**Request Body:** (partial updates allowed)
```json
{
  "title": "string",
  "description": "string",
  "price": 100.00,
  "category": "ELECTRONICS",
  "condition": "GOOD",
  "status": "ACTIVE"
}
```

---

### 3.5 Delete Listing
**DELETE** `/api/listings/{id}`

🔒 **Requires Authentication** (Owner or ADMIN)

Soft delete a listing.

**Success Response (200):**
```json
{
  "message": "Listing deleted successfully",
  "listingId": "uuid"
}
```

---

### 3.6 Get My Listings
**GET** `/api/listings/my-listings`

🔒 **Requires Authentication**

Get listings created by the current user.

**Query Parameters:**
- `page` (int, default: 0)
- `size` (int, default: 20)

---

### 3.7 Get Seller's Listings
**GET** `/api/listings/seller/{sellerId}`

Get listings by a specific seller.

**Path Parameters:**
- `sellerId` (UUID): Seller's user ID

---

## 4. Search & Discovery

### 4.1 Search Products
**POST** `/api/search`

🔒 **Requires Authentication**

Full-text search with filters.

**Request Body:**
```json
{
  "query": "laptop",
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "minPrice": 100.00,
  "maxPrice": 2000.00,
  "sortBy": "relevance",
  "page": 0,
  "size": 20
}
```

**Sort Options:**
- `relevance` (default)
- `price_asc`
- `price_desc`
- `date_asc`
- `date_desc`

**Success Response (200):**
```json
{
  "results": [
    {
      "id": "uuid",
      "title": "MacBook Pro",
      "price": 1200.00,
      "category": "ELECTRONICS",
      "condition": "LIKE_NEW",
      "images": ["https://..."],
      "seller": { ... },
      "score": 0.95
    }
  ],
  "totalResults": 50,
  "totalPages": 3,
  "page": 0,
  "size": 20,
  "query": "laptop",
  "filters": {
    "category": "ELECTRONICS"
  }
}
```

> ⚠️ **Note:** Search results are in `results` array, not `products`
```

---

### 4.2 Autocomplete
**GET** `/api/search/autocomplete`

🔒 **Requires Authentication**

Get search suggestions.

**Query Parameters:**
- `q` or `query` (string, min 2 chars): Search query

**Success Response (200):**
```json
{
  "suggestions": ["laptop", "laptop stand", "laptop bag"]
}
```

---

### 4.3 Search History
**GET** `/api/search/history`

🔒 **Requires Authentication**

Get user's recent search queries.

---

### 4.4 Trending Products
**GET** `/api/discovery/trending`

🔒 **Requires Authentication**

Get trending products based on views and engagement.

**Success Response (200):**
```json
{
  "trending": [
    {
      "id": "uuid",
      "title": "MacBook Pro 2023",
      "price": 1200.00,
      "category": "ELECTRONICS",
      "condition": "LIKE_NEW",
      "images": ["https://..."],
      "seller": { ... },
      "viewCount": 150
    }
  ]
}
```

> ⚠️ **Note:** Response uses `trending` field, not `products`

---

### 4.5 Recommended Products
**GET** `/api/discovery/recommended`

🔒 **Requires Authentication**

Get personalized product recommendations.

**Success Response (200):**
```json
{
  "recommended": [
    {
      "id": "uuid",
      "title": "iPhone 15",
      "price": 899.00,
      ...
    }
  ]
}
```

> ⚠️ **Note:** Response uses `recommended` field, not `recommendations`

---

### 4.6 Similar Products
**GET** `/api/discovery/similar/{productId}`

🔒 **Requires Authentication**

Get products similar to the specified product.

**Path Parameters:**
- `productId` (UUID): The product ID to find similar products for

**Success Response (200):**
```json
{
  "similar": [
    {
      "id": "uuid",
      "title": "MacBook Air",
      ...
    }
  ]
}
```

---

### 4.7 Recently Viewed
**GET** `/api/discovery/recently-viewed`

🔒 **Requires Authentication**

Get user's recently viewed products.

**Success Response (200):**
```json
{
  "recentlyViewed": [
    {
      "id": "uuid",
      "title": "Desk Lamp",
      ...
    }
  ]
}
```

> ⚠️ **Note:** Response uses `recentlyViewed` field, not `products`

---

## 5. Favorites

### 5.1 Get Favorites
**GET** `/api/favorites`

🔒 **Requires Authentication**

Get user's favorited listings.

**Success Response (200):**
```json
[
  {
    "id": "uuid",
    "title": "MacBook Pro",
    "price": 1200.00,
    "favorite": true,
    ...
  }
]
```

---

### 5.2 Toggle Favorite
**POST** `/api/favorites/{productId}`

🔒 **Requires Authentication**

Add or remove a product from favorites.

**Success Response (200):**
```json
{
  "favorited": true
}
```

---

### 5.3 Remove Favorite
**DELETE** `/api/favorites/{productId}`

🔒 **Requires Authentication**

Remove a product from favorites.

**Success Response (204):** No content

---

### 5.4 Check Favorite Status
**GET** `/api/favorites/{productId}/check`

🔒 **Requires Authentication**

**Success Response (200):**
```json
{
  "isFavorited": true
}
```

---

### 5.5 Get Favorite Count
**GET** `/api/favorites/count`

🔒 **Requires Authentication**

**Success Response (200):**
```json
{
  "count": 15
}
```

---

### 5.6 Clear All Favorites
**DELETE** `/api/favorites`

🔒 **Requires Authentication**

**Success Response (204):** No content

---

## 6. Chat & Messaging

> ⚠️ **Important:** Chat endpoints return arrays directly, NOT wrapped in objects.

### 6.1 Send Message to Listing
**POST** `/api/chat/messages`

🔒 **Requires Authentication**

Start a conversation or send a message about a listing.

**Request Body:**
```json
{
  "listingId": "uuid",
  "content": "Hi, is this still available?"
}
```

**Success Response (201):**
```json
{
  "id": "uuid",
  "content": "Hi, is this still available?",
  "senderId": "uuid",
  "senderName": "John Doe",
  "conversationId": "uuid",
  "createdAt": "2025-11-27T10:00:00Z",
  "read": false
}
```

---

### 6.2 Send Message in Conversation
**POST** `/api/chat/conversations/{conversationId}/messages`

🔒 **Requires Authentication**

**Request Body:**
```json
{
  "content": "string"
}
```

---

### 6.3 Get All Conversations
**GET** `/api/chat/conversations`

🔒 **Requires Authentication**

**Success Response (200):** Returns array directly
```json
[
  {
    "id": "uuid",
    "listingId": "uuid",
    "listingTitle": "MacBook Pro",
    "listingImage": "https://...",
    "participants": [
      { "id": "uuid", "username": "buyer" },
      { "id": "uuid", "username": "seller" }
    ],
    "messages": [ ... ],
    "unreadCount": 3,
    "updatedAt": "2025-11-27T10:00:00Z"
  }
]
```

> ⚠️ **Note:** Returns `List<ConversationResponse>` directly, NOT `{ conversations: [...] }`

---

### 6.4 Get Conversation
**GET** `/api/chat/conversations/{conversationId}`

🔒 **Requires Authentication**

---

### 6.5 Get Messages in Conversation
**GET** `/api/chat/conversations/{conversationId}/messages`

🔒 **Requires Authentication**

**Success Response (200):** Returns array directly
```json
[
  {
    "id": "uuid",
    "content": "Message text",
    "senderId": "uuid",
    "senderName": "John",
    "createdAt": "2025-11-27T10:00:00Z",
    "read": true
  }
]
```

> ⚠️ **Note:** Returns `List<MessageResponse>` directly, NOT `{ messages: [...] }`

---

### 6.6 Mark Conversation as Read
**PUT** `/api/chat/conversations/{conversationId}/read`

🔒 **Requires Authentication**

**Success Response (200):**
```json
{
  "count": 5
}
```

---

### 6.7 Get Unread Count
**GET** `/api/chat/unread-count`

🔒 **Requires Authentication**

**Success Response (200):**
```json
{
  "count": 10
}
```

---

### 6.8 Get or Create Conversation for Listing
**GET** `/api/chat/conversations/listing/{listingId}`

🔒 **Requires Authentication**

Get existing conversation or create a new one for the listing.

---

### 6.9 Mark Single Message as Read
**PUT** `/api/chat/messages/{messageId}/read`

🔒 **Requires Authentication**

Mark a specific message as read.

**Success Response (200):** Empty body

---

## 7. Orders & Cart

### 7.1 Get Cart
**GET** `/api/orders/cart`

🔒 **Requires Authentication**

---

### 7.2 Add to Cart
**POST** `/api/orders/cart/items`

🔒 **Requires Authentication**

**Request Body:**
```json
{
  "productId": "uuid",
  "quantity": 1
}
```

---

### 7.3 Update Cart Item
**PUT** `/api/orders/cart/items/{orderItemId}`

🔒 **Requires Authentication**

**Query Parameters:**
- `quantity` (int): New quantity

---

### 7.4 Remove from Cart
**DELETE** `/api/orders/cart/items/{orderItemId}`

🔒 **Requires Authentication**

---

### 7.5 Clear Cart
**DELETE** `/api/orders/cart`

🔒 **Requires Authentication**

---

### 7.6 Checkout
**POST** `/api/orders/checkout`

🔒 **Requires Authentication**

**Request Body:**
```json
{
  "deliveryMethod": "PICKUP",
  "deliveryAddressId": "uuid (optional)",
  "buyerNotes": "string (optional)"
}
```

**Delivery Methods:**
- `PICKUP`
- `DELIVERY`
- `SHIPPING`

---

### 7.7 Get Order by ID
**GET** `/api/orders/{orderId}`

🔒 **Requires Authentication**

---

### 7.8 Get Order History
**GET** `/api/orders`

🔒 **Requires Authentication**

**Query Parameters:**
- `page` (int)
- `size` (int)
- `sort` (string)

---

### 7.9 Get Seller Orders
**GET** `/api/orders/seller`

🔒 **Requires Authentication**

Get orders where the current user is the seller.

---

### 7.10 Update Order Status
**PUT** `/api/orders/{orderId}/status`

🔒 **Requires Authentication** (Seller only)

**Request Body:**
```json
{
  "status": "SHIPPED",
  "trackingNumber": "string (optional)"
}
```

**Order Status Values:**
- `PENDING`
- `PROCESSING`
- `SHIPPED`
- `DELIVERED`
- `COMPLETED`
- `CANCELLED`

---

### 7.11 Cancel Order
**POST** `/api/orders/{orderId}/cancel`

🔒 **Requires Authentication**

---

## 8. Payments

### 8.1 Get Payment Methods
**GET** `/api/payments/methods`

🔒 **Requires Authentication**

---

### 8.2 Add Payment Method
**POST** `/api/payments/methods`

🔒 **Requires Authentication**

**Request Body:**
```json
{
  "type": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "expiryMonth": 12,
  "expiryYear": 2026,
  "cvv": "123",
  "cardholderName": "John Doe",
  "isDefault": true
}
```

---

### 8.3 Set Default Payment Method
**PUT** `/api/payments/methods/{paymentMethodId}/default`

🔒 **Requires Authentication**

---

### 8.4 Delete Payment Method
**DELETE** `/api/payments/methods/{paymentMethodId}`

🔒 **Requires Authentication**

---

### 8.5 Process Payment
**POST** `/api/payments/process`

🔒 **Requires Authentication**

---

### 8.6 Get Transactions
**GET** `/api/payments/transactions`

🔒 **Requires Authentication**

---

### 8.7 Request Refund
**POST** `/api/payments/refund`

🔒 **Requires Authentication**

---

## 9. Reports

### 9.1 Submit Report
**POST** `/api/reports`

🔒 **Requires Authentication**

**Request Body:**
```json
{
  "reportType": "LISTING",
  "targetId": "uuid",
  "reason": "INAPPROPRIATE",
  "description": "This listing contains inappropriate content"
}
```

**Report Types:**
- `LISTING`
- `USER`
- `MESSAGE`

**Report Reasons:**
- `INAPPROPRIATE`
- `SPAM`
- `SCAM`
- `PROHIBITED_ITEM`
- `WRONG_CATEGORY`
- `OTHER`

**Success Response (201):**
```json
{
  "reportId": "uuid-string",
  "reportType": "LISTING",
  "targetId": "uuid",
  "reason": "INAPPROPRIATE",
  "description": "...",
  "status": "PENDING",
  "createdAt": "2025-11-27T10:00:00Z"
}
```

> ⚠️ **Note:** `reportId` is a UUID string, not a number

---

### 9.2 Get Report by ID
**GET** `/api/reports/{reportId}`

🔒 **Requires Authentication**

---

### 9.3 Get My Reports
**GET** `/api/reports/my-reports`

🔒 **Requires Authentication**

Get reports submitted by the current user. Paginated response.

**Query Parameters:**
- `page` (int, default: 0)
- `size` (int, default: 10)

**Success Response (200):**
```json
{
  "content": [
    {
      "reportId": "uuid-string",
      "reportType": "LISTING",
      "targetId": "uuid",
      "reason": "SPAM",
      "description": "This is spam",
      "status": "PENDING",
      "createdAt": "2025-11-27T10:00:00Z",
      "updatedAt": "2025-11-27T10:00:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

> ⚠️ **Note:** Uses paginated format with `content` array

---

## 10. Admin

All admin endpoints require ADMIN role.

### 10.1 Admin Dashboard
**GET** `/api/admin/dashboard`

🔒 **Requires ADMIN role**

**Success Response (200):**
```json
{
  "message": "Admin dashboard loaded",
  "totalUsers": 8,
  "totalListings": 5,
  "pendingApprovals": 0,
  "pendingReports": 0
}
```

---

### 10.2 Get All Users
**GET** `/api/admin/users`

🔒 **Requires ADMIN role**

**Query Parameters:**
- `page` (int)
- `size` (int)
- `search` (string, optional)

---

### 10.3 Search Users
**GET** `/api/admin/user-management/search`

🔒 **Requires ADMIN role**

**Query Parameters:**
- `query` (string)

---

### 10.4 Get User Details
**GET** `/api/admin/users/{userId}`

🔒 **Requires ADMIN role**

---

### 10.5 Create User (Admin)
**POST** `/api/admin/users`

🔒 **Requires ADMIN role**

---

### 10.6 Update User
**PUT** `/api/admin/users/{userId}`

🔒 **Requires ADMIN role**

---

### 10.7 Suspend User
**POST** `/api/admin/users/{userId}/suspend`

🔒 **Requires ADMIN role**

---

### 10.8 Reactivate User
**POST** `/api/admin/users/{userId}/reactivate`

🔒 **Requires ADMIN role**

---

### 10.9 Delete User
**DELETE** `/api/admin/users/{userId}`

🔒 **Requires ADMIN role**

---

### 10.10 Moderate Listing
**POST** `/api/admin/moderate/{listingId}`

🔒 **Requires ADMIN role**

**Query Parameters:**
- `action` (string): `approve` or `reject`

---

### 10.11 Create Admin User
**POST** `/api/admin/users/admin`

🔒 **Requires ADMIN role**

Create a new admin user.

---

### 10.12 Get All Reports (Admin)
**GET** `/api/reports/admin`

🔒 **Requires ADMIN role**

**Query Parameters:**
- `status` (string, optional): `PENDING`, `APPROVED`, `REJECTED`, `FLAGGED`
- `page` (int)
- `size` (int)

---

### 10.13 Approve Report
**POST** `/api/reports/{reportId}/approve`

🔒 **Requires ADMIN role**

**Request Body:**
```json
{
  "resolutionNotes": "Report approved, listing removed"
}
```

---

### 10.14 Reject Report
**POST** `/api/reports/{reportId}/reject`

🔒 **Requires ADMIN role**

---

### 10.15 Get Report Statistics
**GET** `/api/reports/admin/stats`

🔒 **Requires ADMIN role**

**Success Response (200):**
```json
{
  "pendingCount": 15,
  "highPriorityCount": 3
}
```

---

### 10.16 Analytics Overview
**GET** `/api/admin/analytics/overview`

🔒 **Requires ADMIN role**

---

### 10.17 User Analytics
**GET** `/api/admin/analytics/users`

🔒 **Requires ADMIN role**

---

### 10.18 Security Analytics
**GET** `/api/admin/analytics/security`

🔒 **Requires ADMIN role**

---

## 11. Common Response Formats

### Paginated Response
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

### Success Response with Message
```json
{
  "message": "Operation completed successfully",
  "data": { ... }
}
```

---

## 12. Error Handling

### Error Response Format
```json
{
  "error": "Error type",
  "message": "Detailed error message",
  "status": 400,
  "path": "/api/endpoint"
}
```

### HTTP Status Codes
| Code | Description |
|------|-------------|
| 200  | Success |
| 201  | Created |
| 204  | No Content (successful delete) |
| 400  | Bad Request (validation error) |
| 401  | Unauthorized (missing/invalid token) |
| 403  | Forbidden (insufficient permissions) |
| 404  | Not Found |
| 409  | Conflict (duplicate resource) |
| 500  | Internal Server Error |

### Common Error Messages
- `"User not authenticated"` - No valid auth token
- `"User not found"` - Referenced user doesn't exist
- `"Listing not found"` - Referenced listing doesn't exist
- `"Invalid token"` - JWT token is invalid or expired
- `"Access denied"` - User lacks required permissions

---

## Test Credentials

> ⚠️ **Important**: Only test accounts (created in V12 migration) have valid bcrypt password hashes. Demo accounts from V2 migration have placeholder hashes and cannot be used for login.

### Working Test Accounts

| Username | Password | Roles | Email | Notes |
|----------|----------|-------|-------|-------|
| `test_buyer` | `password123` | BUYER, SELLER | test.buyer@sjsu.edu | ✅ Primary test account for user flows |
| `test_admin` | `password123` | ADMIN | test.admin@sjsu.edu | ✅ Admin test account for admin features |

### Demo Accounts (Display Only)

These accounts exist in the database but have invalid password hashes. They are for demo data display purposes only:

| Username | Roles | Purpose |
|----------|-------|---------|
| `alice_buyer` | BUYER, SELLER | Demo buyer profile |
| `bob_buyer` | BUYER, SELLER | Demo buyer profile |
| `carol_seller` | BUYER, SELLER | Demo seller with listings |
| `david_techseller` | BUYER, SELLER | Demo seller with electronics |
| `sjsu_admin` | ADMIN | Demo admin profile |

### New User Registration

You can also register new users through the frontend or API. New users automatically receive both BUYER and SELLER roles:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@sjsu.edu",
    "password": "Password123!",
    "firstName": "New",
    "lastName": "User"
  }'
```

**Password Requirements:**
- Minimum 8 characters
- At least one letter (a-z or A-Z)
- At least one number (0-9)

---

## Quick Start Example

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test_buyer", "password": "password123"}'
```

### 2. Get Listings (with token)
```bash
curl http://localhost:8080/api/listings \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. Create Listing
```bash
curl -X POST http://localhost:8080/api/listings \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "iPhone 15 Pro",
    "description": "Like new condition",
    "price": 899.99,
    "category": "ELECTRONICS",
    "condition": "LIKE_NEW"
  }'
```

### 4. Logout (requires refresh token)
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'
```

---

## Frontend Integration Notes

1. **Token Storage**: Store `accessToken` in memory or sessionStorage, `refreshToken` in localStorage
2. **Token Refresh**: Implement automatic token refresh when receiving 401 response
3. **API Base URL**: Use environment variable `VITE_API_BASE_URL` or default to `http://localhost:8080/api`
4. **CORS**: Backend allows origins `localhost:3000`, `localhost:3001`, `localhost:3002`, `localhost:5173`
5. **Date Format**: All dates are in ISO-8601 format (e.g., `2025-11-27T10:00:00Z`)
6. **UUIDs**: All entity IDs are UUIDs (e.g., `550e8400-e29b-41d4-a716-446655440000`)

---

## ⚠️ Critical API Response Format Notes

### Discovery Endpoints - Field Naming
| Endpoint | Response Field | NOT |
|----------|---------------|-----|
| `GET /discovery/trending` | `{ "trending": [...] }` | ~~`{ "products": [...] }`~~ |
| `GET /discovery/recommended` | `{ "recommended": [...] }` | ~~`{ "recommendations": [...] }`~~ |
| `GET /discovery/recently-viewed` | `{ "recentlyViewed": [...] }` | ~~`{ "products": [...] }`~~ |
| `GET /discovery/similar/{id}` | `{ "similar": [...] }` | ~~`{ "products": [...] }`~~ |

### Chat Endpoints - Array vs Object
| Endpoint | Response Format |
|----------|----------------|
| `GET /chat/conversations` | Returns `ConversationResponse[]` directly |
| `GET /chat/conversations/{id}/messages` | Returns `MessageResponse[]` directly |

**Correct:**
```javascript
const conversations = await api.get('/chat/conversations'); // Array
conversations.forEach(conv => console.log(conv.id));
```

**Incorrect:**
```javascript
// DON'T DO THIS - no "conversations" wrapper
const { conversations } = await api.get('/chat/conversations'); // Wrong!
```

### Search Response
```javascript
const response = await api.post('/search', { query: 'laptop' });
// Use response.results, not response.products
const products = response.results;
```

### Report IDs
Report IDs are UUID strings, not numbers:
```javascript
// Correct
const reportId = "550e8400-e29b-41d4-a716-446655440000";

// Incorrect
const reportId = 12345; // Wrong!
```

---

*Generated for Campus Marketplace v1.1.0*
