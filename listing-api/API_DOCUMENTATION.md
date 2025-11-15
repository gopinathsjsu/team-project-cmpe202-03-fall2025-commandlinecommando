# Listing API Documentation

## Overview
This document provides detailed API documentation for the Listing API microservice, including endpoint descriptions, request/response formats, and usage examples.

## Base URL
```
http://localhost:8100/api
```

## Authentication

The API uses JWT (JSON Web Token) authentication for authorization. All protected endpoints require a valid JWT token.

### JWT Token Format
Include the JWT token in the `Authorization` header:
```
Authorization: Bearer <your-jwt-token>
```

### How Authentication Works
1. **Token Extraction**: The JWT token is extracted from the `Authorization` header
2. **User Identification**: User ID (`userId`) and role are extracted from the JWT claims
3. **Authorization**: 
   - For listings: Only the owner (matching `sellerId`) can update/delete their listings
   - For reports: Only the reporter (matching `reporterId`) can update/delete their reports
   - Admin endpoints: Require `ADMIN` role in the JWT token

### JWT Token Claims
The JWT token should contain the following claims:
- `userId`: User's unique identifier (UUID converted to Long)
- `role`: User's role (e.g., "ADMIN", "STUDENT")
- `sub`: Username (optional)

### Protected Endpoints
The following endpoints require JWT authentication:
- `POST /api/listings/` - Create listing
- `PUT /api/listings/{listingId}` - Update listing
- `PUT /api/listings/{listingId}/sold` - Mark as sold
- `PUT /api/listings/{listingId}/cancel` - Cancel listing
- `DELETE /api/listings/{listingId}` - Delete listing
- `POST /api/files/upload/{listingId}` - Upload image
- `POST /api/files/upload-multiple/{listingId}` - Upload multiple images
- `DELETE /api/files/listing/{listingId}/{imageId}` - Delete image
- `POST /api/reports/` - Create report
- `PUT /api/reports/{reportId}` - Update report
- `DELETE /api/reports/{reportId}` - Delete report
- All admin endpoints (GET /api/reports/*, PUT /api/reports/{reportId}/review, etc.)

### Unprotected Endpoints
The following endpoints do not require authentication:
- `GET /api/listings` - Get all listings
- `GET /api/listings/search` - Search listings
- `GET /api/listings/{listingId}` - Get listing by ID
- `GET /api/listings/seller/{sellerId}` - Get listings by seller
- `GET /api/files/listing/{listingId}` - Get listing images

### Error Responses

**401 Unauthorized** - No JWT token or invalid token:
```json
"Authentication required"
```

**403 Forbidden** - Valid token but insufficient permissions:
```json
"You are not allowed to [action] this [resource]"
```

## Response Format
All API responses follow a consistent format:
- **Success**: Returns data directly or wrapped in pagination object
- **Error**: Returns structured error response with consistent format using the global exception handler

## Endpoints

### 1. Listing Management

#### 1.1 Get All Listings
**Endpoint**: `GET /api/listings`

**Description**: Retrieves all active listings with pagination and sorting options.

**Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | 0 | Page number (0-based) |
| `size` | integer | 20 | Number of items per page |
| `sortBy` | string | "createdAt" | Field to sort by |
| `sortDirection` | string | "desc" | Sort direction ("asc" or "desc") |

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/listings?page=0&size=10&sortBy=price&sortDirection=asc"
```

**Example Response**:
```json
{
  "content": [
    {
      "listingId": 1,
      "sellerId": 123,
      "title": "MacBook Pro 13-inch",
      "description": "Excellent condition MacBook Pro, barely used",
      "category": "ELECTRONICS",
      "price": 1200.00,
      "condition": "LIKE_NEW",
      "status": "ACTIVE",
      "location": "San Jose, CA",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00",
      "viewCount": 25,
      "images": []
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 50,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

#### 1.2 Search Listings
**Endpoint**: `GET /api/listings/search`

**Description**: Advanced search functionality with multiple filters.

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | string | No | Listing status (PENDING, ACTIVE, SOLD, CANCELLED) |
| `keyword` | string | No | Search in title and description |
| `category` | string | No | Product category |
| `condition` | string | No | Item condition |
| `minPrice` | number | No | Minimum price filter |
| `maxPrice` | number | No | Maximum price filter |
| `location` | string | No | Location filter |
| `page` | integer | No | Page number (default: 0) |
| `size` | integer | No | Page size (default: 20) |
| `sortBy` | string | No | Sort field (default: createdAt) |
| `sortDirection` | string | No | Sort direction (default: desc) |

**Available Categories**:
- `TEXTBOOKS`
- `GADGETS`
- `ELECTRONICS`
- `STATIONARY`
- `OTHER`

**Available Conditions**:
- `NEW`
- `LIKE_NEW`
- `GOOD`
- `USED`

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/listings/search?category=ELECTRONICS&minPrice=500&maxPrice=1500&keyword=MacBook"
```

**Example Response**: Same format as Get All Listings

#### 1.3 Get Listing by ID
**Endpoint**: `GET /api/listings/{listingId}`

**Description**: Retrieves a specific listing by ID and increments view count.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/listings/1"
```

**Example Response**:
```json
{
  "listingId": 1,
  "sellerId": 123,
  "title": "MacBook Pro 13-inch",
  "description": "Excellent condition MacBook Pro, barely used",
  "category": "ELECTRONICS",
  "price": 1200.00,
  "condition": "LIKE_NEW",
  "status": "ACTIVE",
  "location": "San Jose, CA",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "viewCount": 26,
  "images": [
    {
      "imageId": 1,
      "fileName": "macbook1.jpg",
      "filePath": "/uploads/macbook1.jpg",
      "displayOrder": 1
    }
  ]
}
```

#### 1.4 Get Listings by Seller
**Endpoint**: `GET /api/listings/seller/{sellerId}`

**Description**: Retrieves all listings for a specific seller.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `sellerId` | integer | Seller's unique identifier |

**Query Parameters**: Same pagination parameters as Get All Listings

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/listings/seller/123?page=0&size=5"
```

**Example Response**: Same pagination format as Get All Listings

#### 1.5 Create Listing
**Endpoint**: `POST /api/listings/`

**Description**: Creates a new listing. Requires JWT authentication. The `sellerId` is automatically extracted from the JWT token.

**Headers**:
- `Content-Type: application/json`
- `Authorization: Bearer <jwt-token>` (Required)

**Request Body**:
```json
{
  "title": "MacBook Pro 13-inch",
  "description": "Excellent condition MacBook Pro, barely used",
  "price": 1200.00,
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "location": "San Jose, CA"
}
```

**Validation Rules**:
- `title`: Required, 2-255 characters
- `description`: Required, 10-1000 characters
- `price`: Required, positive number
- `category`: Required, must be valid enum value
- `condition`: Required, must be valid enum value
- `location`: Required, non-empty string

**Example Request**:
```bash
curl -X POST "http://localhost:8100/api/listings/" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "title": "iPhone 12 Pro",
    "description": "Great condition iPhone 12 Pro, unlocked",
    "price": 800.00,
    "category": "ELECTRONICS",
    "condition": "GOOD",
    "location": "San Francisco, CA"
  }'
```

**Postman Example**:
1. Method: `POST`
2. URL: `http://localhost:8100/api/listings/`
3. Headers:
   - `Content-Type`: `application/json`
   - `Authorization`: `Bearer <your-jwt-token>`
4. Body (raw, JSON):
```json
{
  "title": "MacBook Pro 13-inch 2023",
  "description": "Excellent condition MacBook Pro, barely used. Comes with original charger and box.",
  "price": 1200.00,
  "category": "ELECTRONICS",
  "condition": "LIKE_NEW",
  "location": "San Jose, CA"
}
```

**Example Response**:
```json
{
  "listingId": 2,
  "sellerId": 1,
  "title": "iPhone 12 Pro",
  "description": "Great condition iPhone 12 Pro, unlocked",
  "category": "ELECTRONICS",
  "price": 800.00,
  "condition": "GOOD",
  "status": "ACTIVE",
  "location": "San Francisco, CA",
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:00:00",
  "viewCount": 0,
  "images": []
}
```

#### 1.6 Update Listing
**Endpoint**: `PUT /api/listings/{listingId}`

**Description**: Updates an existing listing. Only the listing owner can update. The `sellerId` is automatically extracted from the JWT token.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Headers**:
- `Content-Type: application/json`
- `Authorization: Bearer <jwt-token>` (Required)

**Request Body**: Same format as Create Listing, with optional `images` array

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/listings/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "title": "MacBook Pro 13-inch (Updated)",
    "description": "Excellent condition MacBook Pro, barely used - price reduced!",
    "price": 1100.00,
    "category": "ELECTRONICS",
    "condition": "LIKE_NEW",
    "location": "San Jose, CA"
  }'
```

**Example Response**: Same format as Create Listing response

#### 1.7 Mark Listing as Sold
**Endpoint**: `PUT /api/listings/{listingId}/sold`

**Description**: Marks a listing as sold. Only the listing owner can perform this action. Requires JWT authentication. The `sellerId` is automatically extracted from the JWT token.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required)

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/listings/1/sold" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**:
```json
{
  "listingId": 1,
  "sellerId": 123,
  "title": "MacBook Pro 13-inch",
  "description": "Excellent condition MacBook Pro, barely used",
  "category": "ELECTRONICS",
  "price": 1200.00,
  "condition": "LIKE_NEW",
  "status": "SOLD",
  "location": "San Jose, CA",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T12:00:00",
  "viewCount": 26,
  "images": []
}
```

#### 1.8 Cancel Listing
**Endpoint**: `PUT /api/listings/{listingId}/cancel`

**Description**: Cancels a listing. Only the listing owner can perform this action. The `sellerId` is automatically extracted from the JWT token.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required)

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/listings/1/cancel" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
```

**Example Response**: Same format as Mark as Sold response, with status: "CANCELLED"

#### 1.9 Delete Listing
**Endpoint**: `DELETE /api/listings/{listingId}`

**Description**: Permanently deletes a listing. Requires JWT authentication. Only the listing owner can perform this action. The `sellerId` is automatically extracted from the JWT token and verified against the listing's owner. Returns 403 Forbidden if the user is not the owner.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `userId` claim

**Example Request**:
```bash
curl -X DELETE "http://localhost:8100/api/listings/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**:
```json
"Listing deleted successfully"
```

### 2. Image Management

#### 2.1 Upload Single Image
**Endpoint**: `POST /api/files/upload/{listingId}`

**Description**: Uploads a single image for a listing. Requires JWT authentication. The `sellerId` is automatically extracted from the JWT token and verified against the listing's owner. Only the listing owner can upload images. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user is not the listing owner.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `userId` claim

**Form Data**:
| Field | Type | Description |
|-------|------|-------------|
| `file` | file | Image file to upload |
| `displayOrder` | integer | Display order for the image |

**Supported File Types**: JPG, JPEG, PNG, GIF

**Example Request**:
```bash
curl -X POST "http://localhost:8100/api/files/upload/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "file=@/path/to/image.jpg" \
  -F "displayOrder=1"
```

**Example Response**:
```json
"File uploaded successfully"
```

#### 2.2 Upload Multiple Images
**Endpoint**: `POST /api/files/upload-multiple/{listingId}`

**Description**: Uploads multiple images for a listing. Requires JWT authentication. The `sellerId` is automatically extracted from the JWT token and verified against the listing's owner. Only the listing owner can upload images. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user is not the listing owner.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `userId` claim

**Form Data**:
| Field | Type | Description |
|-------|------|-------------|
| `files` | file[] | Array of image files |
| `displayOrders` | integer[] | Array of display orders |

**Example Request**:
```bash
curl -X POST "http://localhost:8100/api/files/upload-multiple/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg" \
  -F "displayOrders=1" \
  -F "displayOrders=2"
```

**Example Response**:
```json
"Files uploaded successfully"
```

#### 2.3 Get Listing Images
**Endpoint**: `GET /api/files/listing/{listingId}`

**Description**: Retrieves all images for a specific listing.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/files/listing/1"
```

**Example Response**:
```json
[
  {
    "imageId": 1,
    "fileName": "macbook1.jpg",
    "filePath": "/uploads/macbook1.jpg",
    "displayOrder": 1,
    "uploadedAt": "2024-01-15T11:30:00"
  },
  {
    "imageId": 2,
    "fileName": "macbook2.jpg",
    "filePath": "/uploads/macbook2.jpg",
    "displayOrder": 2,
    "uploadedAt": "2024-01-15T11:31:00"
  }
]
```

#### 2.4 Delete Image
**Endpoint**: `DELETE /api/files/listing/{listingId}/{imageId}`

**Description**: Deletes a specific image from a listing. Requires JWT authentication. The `sellerId` is automatically extracted from the JWT token and verified against the listing's owner. Only the listing owner can delete images. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user is not the listing owner.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |
| `imageId` | integer | Unique image identifier |

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `userId` claim

**Example Request**:
```bash
curl -X DELETE "http://localhost:8100/api/files/listing/1/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**:
```json
"Listing image deleted successfully"
```

### 3. Report Management

#### 3.1 Get All Reports
**Endpoint**: `GET /api/reports`

**Description**: Retrieves all reports with pagination and sorting options. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | 0 | Page number (0-based) |
| `size` | integer | 20 | Number of items per page |
| `sortBy` | string | "createdAt" | Field to sort by |
| `sortDirection` | string | "desc" | Sort direction ("asc" or "desc") |

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/reports?page=0&size=10&sortBy=createdAt&sortDirection=desc" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**:
```json
{
  "content": [
    {
      "reportId": 1,
      "reporterId": 123,
      "listingId": 456,
      "reportType": "INAPPROPRIATE_CONTENT",
      "description": "This listing contains inappropriate language",
      "status": "PENDING",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00",
      "reviewedBy": null,
      "reviewedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 25,
  "totalPages": 3,
  "last": false,
  "first": true,
  "numberOfElements": 10
}
```

#### 3.2 Search Reports
**Endpoint**: `GET /api/reports/search`

**Description**: Advanced search functionality for reports with multiple filters. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | string | No | Report status (PENDING, UNDER_REVIEW, RESOLVED, DISMISSED) |
| `reporterId` | integer | No | ID of the user who reported |
| `listingId` | integer | No | ID of the reported listing |
| `reportType` | string | No | Type of report |
| `reviewedBy` | integer | No | ID of the reviewer |
| `page` | integer | No | Page number (default: 0) |
| `size` | integer | No | Page size (default: 20) |
| `sortBy` | string | No | Sort field (default: createdAt) |
| `sortDirection` | string | No | Sort direction (default: desc) |

**Available Report Types**:
- `INAPPROPRIATE_CONTENT`
- `SPAM`
- `FAKE_LISTING`
- `HARASSMENT`
- `COPYRIGHT_VIOLATION`
- `OTHER`

**Available Statuses**:
- `PENDING`
- `UNDER_REVIEW`
- `RESOLVED`
- `DISMISSED`

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/reports/search?status=PENDING&reportType=INAPPROPRIATE_CONTENT" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**: Same format as Get All Reports

#### 3.3 Get Pending Reports
**Endpoint**: `GET /api/reports/pending`

**Description**: Retrieves all pending reports that need review. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**: Same pagination parameters as Get All Reports

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/reports/pending?page=0&size=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**: Same format as Get All Reports

#### 3.4 Get Reports by Reporter
**Endpoint**: `GET /api/reports/reporter/{reporterId}`

**Description**: Retrieves all reports submitted by a specific user. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `reporterId` | integer | Unique reporter identifier |

**Query Parameters**: Same pagination parameters as Get All Reports

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/reports/reporter/123?page=0&size=5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**: Same pagination format as Get All Reports

#### 3.5 Get Reports by Listing
**Endpoint**: `GET /api/reports/listing/{listingId}`

**Description**: Retrieves all reports for a specific listing. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Query Parameters**: Same pagination parameters as Get All Reports

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/reports/listing/456?page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**: Same pagination format as Get All Reports

#### 3.6 Get Reports by Type
**Endpoint**: `GET /api/reports/type/{reportType}`

**Description**: Retrieves all reports of a specific type. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `reportType` | string | Report type (INAPPROPRIATE_CONTENT, SPAM, etc.) |

**Query Parameters**: Same pagination parameters as Get All Reports

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/reports/type/SPAM?page=0&size=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**: Same pagination format as Get All Reports

#### 3.7 Get Reports by Status
**Endpoint**: `GET /api/reports/status/{status}`

**Description**: Retrieves all reports with a specific status. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `status` | string | Report status (PENDING, UNDER_REVIEW, etc.) |

**Query Parameters**: Same pagination parameters as Get All Reports

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/reports/status/PENDING?page=0&size=20" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**: Same pagination format as Get All Reports

#### 3.8 Get Report by ID
**Endpoint**: `GET /api/reports/{reportId}`

**Description**: Retrieves a specific report by ID. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `reportId` | integer | Unique report identifier |

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/reports/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**:
```json
{
  "reportId": 1,
  "reporterId": 123,
  "listingId": 456,
  "reportType": "INAPPROPRIATE_CONTENT",
  "description": "This listing contains inappropriate language",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "reviewedBy": null,
  "reviewedAt": null
}
```

#### 3.9 Create Report
**Endpoint**: `POST /api/reports/`

**Description**: Creates a new report for a listing. Requires JWT authentication. The `reporterId` is automatically extracted from the JWT token.

**Headers**:
- `Content-Type: application/json`
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Request Body**:
```json
{
  "listingId": 456,
  "reportType": "INAPPROPRIATE_CONTENT",
  "description": "This listing contains inappropriate language"
}
```

**Validation Rules**:
- `listingId`: Required, valid listing ID
- `reportType`: Required, must be valid enum value
- `description`: Required, non-empty string

**Note**: The `reporterId` is automatically extracted from the JWT token. You don't need to include it in the request body.

**Example Request**:
```bash
curl -X POST "http://localhost:8100/api/reports/" \
  -H "Content-Type: application/json" \
  -d '{
    "reporterId": 123,
    "listingId": 456,
    "reportType": "SPAM",
    "description": "This appears to be a spam listing"
  }'
```

**Example Response**:
```json
{
  "reportId": 2,
  "reporterId": 123,
  "listingId": 456,
  "reportType": "SPAM",
  "description": "This appears to be a spam listing",
  "status": "PENDING",
  "createdAt": "2024-01-15T12:00:00",
  "updatedAt": "2024-01-15T12:00:00",
  "reviewedBy": null,
  "reviewedAt": null
}
```

#### 3.10 Update Report
**Endpoint**: `PUT /api/reports/{reportId}`

**Description**: Updates an existing report. Requires JWT authentication.  Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Content-Type: application/json`
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `reportId` | integer | Unique report identifier |

**Request Body**:
```json
{
  "reportType": "FAKE_LISTING",
  "description": "Updated description of the issue"
}
```

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/reports/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "reportType": "FAKE_LISTING",
    "description": "This listing appears to be fake"
  }'
```

**Example Response**: Same format as Get Report by ID

#### 3.11 Mark Report as Reviewed
**Endpoint**: `PUT /api/reports/{reportId}/review`

**Description**: Marks a report as under review by an admin. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token, and the admin's user ID is stored as the reviewer. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN" and `userId` claim

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `reportId` | integer | Unique report identifier |

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/reports/1/review" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**:
```json
{
  "reportId": 1,
  "reporterId": 123,
  "listingId": 456,
  "reportType": "INAPPROPRIATE_CONTENT",
  "description": "This listing contains inappropriate language",
  "status": "UNDER_REVIEW",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T14:00:00",
  "reviewedBy": 1,
  "reviewedAt": "2024-01-15T14:00:00"
}
```

#### 3.12 Mark Report as Resolved
**Endpoint**: `PUT /api/reports/{reportId}/resolve`

**Description**: Marks a report as resolved by an admin. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `reportId` | integer | Unique report identifier |

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/reports/1/resolve" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**: Same format as Mark as Reviewed, with status: "RESOLVED"

#### 3.13 Mark Report as Dismissed
**Endpoint**: `PUT /api/reports/{reportId}/dismiss`

**Description**: Marks a report as dismissed by an admin. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `reportId` | integer | Unique report identifier |

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/reports/1/dismiss" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**: Same format as Mark as Reviewed, with status: "DISMISSED"

#### 3.14 Delete Report
**Endpoint**: `DELETE /api/reports/{reportId}`

**Description**: Permanently deletes a report. Requires JWT authentication. Only the reporter who created the report can delete it. The `reporterId` is automatically extracted from the JWT token and verified against the report's reporter. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user is not the report owner.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `userId` claim

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `reportId` | integer | Unique report identifier |

**Example Request**:
```bash
curl -X DELETE "http://localhost:8100/api/reports/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**:
```json
"Report deleted successfully"
```

#### 3.15 Get Report Counts
**Endpoint**: `GET /api/reports/count`

**Description**: Retrieves count of reports by status for dashboard statistics. Requires JWT authentication with `ADMIN` role. The role is automatically extracted from the JWT token. Returns 401 Unauthorized if no valid JWT token is provided, or 403 Forbidden if the user does not have ADMIN role.

**Headers**:
- `Authorization: Bearer <jwt-token>` (Required) - JWT token containing `role` claim with value "ADMIN"

**Example Request**:
```bash
curl -X GET "http://localhost:8100/api/reports/count" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example Response**:
```json
{
  "pending": 15,
  "underReview": 8,
  "resolved": 42,
  "dismissed": 12
}
```

## Error Responses

The API uses a comprehensive global exception handler (`GlobalExceptionHandler`) that provides consistent error responses across all endpoints. All errors are returned in a standardized `ErrorResponse` format.

### Common Error Codes

| Status Code | Description |
|-------------|-------------|
| 400 | Bad Request - Invalid input data, validation errors, malformed JSON |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions, unauthorized access |
| 404 | Not Found - Resource not found, endpoint not found |
| 405 | Method Not Allowed - HTTP method not supported for endpoint |
| 413 | Payload Too Large - File upload size exceeded |
| 500 | Internal Server Error - Server error, unexpected exceptions |

### Error Response Format
All error responses follow this consistent structure:
```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "status": 400,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/listings/",
  "validationErrors": ["field: error message", "field2: error message"]
}
```

### Field Descriptions
- `error`: Machine-readable error code for programmatic handling
- `message`: Human-readable error description
- `status`: HTTP status code
- `timestamp`: When the error occurred (auto-generated)
- `path`: The API endpoint where the error occurred
- `validationErrors`: Array of validation error details (only present for validation errors)

### Custom Business Exception Examples

#### Listing Not Found
```json
{
  "error": "LISTING_NOT_FOUND",
  "message": "Listing with ID 999 not found",
  "status": 404,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/listings/999"
}
```

#### Unauthorized Access
```json
{
  "error": "UNAUTHORIZED_ACCESS",
  "message": "You do not have permission to access this listing",
  "status": 403,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/listings/1"
}
```

#### File Upload Error
```json
{
  "error": "FILE_UPLOAD_ERROR",
  "message": "Failed to upload file: Invalid file type",
  "status": 400,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/files/upload/1"
}
```

### Validation Error Examples

#### Method Argument Validation
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "status": 400,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/listings/",
  "validationErrors": [
    "title: Title must be between 2 and 255 characters",
    "price: Price must be positive"
  ]
}
```

#### Constraint Violation
```json
{
  "error": "CONSTRAINT_VIOLATION",
  "message": "Constraint validation failed",
  "status": 400,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/listings/",
  "validationErrors": [
    "Category must be one of: TEXTBOOKS, GADGETS, ELECTRONICS, STATIONARY, OTHER"
  ]
}
```

### HTTP-Related Error Examples

#### Malformed JSON
```json
{
  "error": "MALFORMED_JSON",
  "message": "Request body is malformed or contains invalid JSON",
  "status": 400,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/listings/"
}
```

#### Missing Parameter
```json
{
  "error": "MISSING_PARAMETER",
  "message": "Required parameter 'listingId' is missing",
  "status": 400,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/listings/"
}
```

#### Method Not Allowed
```json
{
  "error": "METHOD_NOT_ALLOWED",
  "message": "HTTP method 'DELETE' is not supported for this endpoint",
  "status": 405,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/listings/"
}
```

#### File Too Large
```json
{
  "error": "FILE_TOO_LARGE",
  "message": "File size exceeds the maximum allowed limit",
  "status": 413,
  "timestamp": "2024-01-15T12:00:00",
  "path": "/api/files/upload/1"
}
```

### Global Exception Handler Features

The `GlobalExceptionHandler` provides:

1. **Consistent Error Format**: All errors follow the same `ErrorResponse` structure
2. **Comprehensive Coverage**: Handles custom business exceptions, validation errors, HTTP errors, and file upload errors
3. **Detailed Logging**: All exceptions are logged with appropriate log levels
4. **Proper HTTP Status Codes**: Returns appropriate status codes for different error types
5. **Validation Error Details**: Provides specific field-level validation error messages
6. **Security**: Prevents sensitive information leakage in error responses
7. **Developer-Friendly**: Includes request path and timestamp for debugging

### Error Categories Handled

1. **Custom Business Exceptions**:
   - `ListingNotFoundException`
   - `ReportNotFoundException`
   - `UnauthorizedAccessException`
   - `FileUploadException`
   - `FileStorageException`
   - `ListingException`
   - `ReportException`
   - `ValidationException`

2. **Spring Validation Exceptions**:
   - `MethodArgumentNotValidException`
   - `BindException`
   - `ConstraintViolationException`

3. **HTTP-Related Exceptions**:
   - `HttpMessageNotReadableException`
   - `MissingServletRequestParameterException`
   - `MethodArgumentTypeMismatchException`
   - `HttpRequestMethodNotSupportedException`
   - `NoHandlerFoundException`

4. **File Upload Exceptions**:
   - `MaxUploadSizeExceededException`

5. **Generic Exception Handler**: Catches any unexpected exceptions

## Rate Limiting
Currently no rate limiting is implemented. Consider implementing rate limiting for production use.

## CORS Configuration
CORS is configured to allow cross-origin requests. Update configuration for production environment.

## Security Considerations
1. **Authentication**: Implement proper authentication integration
2. **Authorization**: Verify user permissions for listing operations
3. **Input Validation**: All inputs are validated using Jakarta Validation
4. **File Upload Security**: Validate file types and sizes
5. **SQL Injection**: Protected by JPA/Hibernate

## Testing
Use the provided test endpoints or Postman collection for API testing:

```bash
# Test endpoint
curl -X GET "http://localhost:8100/api/test"

# Health check
curl -X GET "http://localhost:8100/actuator/health"
```
