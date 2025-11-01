# Listing API Documentation

## Overview
This document provides detailed API documentation for the Listing API microservice, including endpoint descriptions, request/response formats, and usage examples.

## Base URL
```
http://localhost:8100/api
```

## Authentication
**Note**: Currently uses placeholder authentication. In production, integrate with the main authentication service.

## Response Format
All API responses follow a consistent format:
- **Success**: Returns data directly or wrapped in pagination object
- **Error**: Returns error message with appropriate HTTP status code

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

**Description**: Creates a new listing.

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
  -d '{
    "title": "iPhone 12 Pro",
    "description": "Great condition iPhone 12 Pro, unlocked",
    "price": 800.00,
    "category": "ELECTRONICS",
    "condition": "GOOD",
    "location": "San Francisco, CA"
  }'
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

**Description**: Updates an existing listing. Only the listing owner can update.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Request Body**: Same format as Create Listing, with optional `images` array

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/listings/1" \
  -H "Content-Type: application/json" \
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

**Description**: Marks a listing as sold. Only the listing owner can perform this action.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/listings/1/sold"
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

**Description**: Cancels a listing. Only the listing owner can perform this action.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Example Request**:
```bash
curl -X PUT "http://localhost:8100/api/listings/1/cancel"
```

**Example Response**: Same format as Mark as Sold response, with status: "CANCELLED"

#### 1.9 Delete Listing
**Endpoint**: `DELETE /api/listings/{listingId}`

**Description**: Permanently deletes a listing. Only the listing owner can perform this action.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Example Request**:
```bash
curl -X DELETE "http://localhost:8100/api/listings/1"
```

**Example Response**:
```json
"Listing deleted successfully"
```

### 2. Image Management

#### 2.1 Upload Single Image
**Endpoint**: `POST /api/files/upload/{listingId}`

**Description**: Uploads a single image for a listing.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Form Data**:
| Field | Type | Description |
|-------|------|-------------|
| `file` | file | Image file to upload |
| `displayOrder` | integer | Display order for the image |

**Supported File Types**: JPG, JPEG, PNG, GIF

**Example Request**:
```bash
curl -X POST "http://localhost:8100/api/files/upload/1" \
  -F "file=@/path/to/image.jpg" \
  -F "displayOrder=1"
```

**Example Response**:
```json
"File uploaded successfully"
```

#### 2.2 Upload Multiple Images
**Endpoint**: `POST /api/files/upload-multiple/{listingId}`

**Description**: Uploads multiple images for a listing.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |

**Form Data**:
| Field | Type | Description |
|-------|------|-------------|
| `files` | file[] | Array of image files |
| `displayOrders` | integer[] | Array of display orders |

**Example Request**:
```bash
curl -X POST "http://localhost:8100/api/files/upload-multiple/1" \
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

**Description**: Deletes a specific image from a listing.

**Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `listingId` | integer | Unique listing identifier |
| `imageId` | integer | Unique image identifier |

**Example Request**:
```bash
curl -X DELETE "http://localhost:8100/api/files/listing/1/1"
```

**Example Response**:
```json
"Listing image deleted successfully"
```

## Error Responses

### Common Error Codes

| Status Code | Description |
|-------------|-------------|
| 400 | Bad Request - Invalid input data |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error - Server error |

### Error Response Format
```json
{
  "timestamp": "2024-01-15T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for argument",
  "path": "/api/listings/"
}
```

### Validation Error Example
```json
{
  "timestamp": "2024-01-15T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for argument",
  "path": "/api/listings/",
  "details": {
    "title": "Title must be between 2 and 255 characters",
    "price": "Price must be positive"
  }
}
```

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
