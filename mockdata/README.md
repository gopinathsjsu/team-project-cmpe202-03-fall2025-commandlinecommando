# Mock Data

Mock API and sample data used during frontend development when the backend was not yet available.

## Purpose

This folder contains mock implementations that allowed the frontend team to develop and test UI components independently of the backend API. It simulates API responses and provides realistic sample data for:

- User authentication
- Product listings
- Categories and filters
- Chat conversations
- User profiles

## Files

### mockData.ts

Contains sample data objects:

- `mockUsers` - Sample user accounts with different roles
- `mockListings` - Product listings with images, prices, categories
- `mockConversations` - Chat conversation data
- `mockMessages` - Sample chat messages
- `mockCategories` - Product categories

### mockApi.ts

Simulates API endpoints with realistic delays and responses:

- `login()` - Mock authentication
- `getListings()` - Returns paginated listings
- `getListing()` - Get single listing by ID
- `createListing()` - Simulate listing creation
- `getConversations()` - Get user conversations
- `sendMessage()` - Simulate sending messages

## Usage

The mock API was used during development by setting a flag in the frontend config:

```typescript
// frontend/src/api/config.ts
export const API_CONFIG = {
  USE_MOCK_API: true,  // Use mock data
  // ...
};
```

When the backend became available, this was switched to `false` to use real API endpoints.

## Sample Data Structure

### User

```typescript
{
  id: "user-1",
  username: "john_doe",
  email: "john@sjsu.edu",
  firstName: "John",
  lastName: "Doe",
  roles: ["BUYER", "SELLER"],
  university: "San Jose State University"
}
```

### Listing

```typescript
{
  id: "listing-1",
  title: "MacBook Pro 2021",
  description: "14-inch, M1 Pro chip, 16GB RAM",
  price: 1200.00,
  category: "ELECTRONICS",
  condition: "LIKE_NEW",
  images: ["https://..."],
  seller: { id: "user-1", username: "john_doe" },
  createdAt: "2024-01-15T10:00:00Z"
}
```

## Notes

- Mock data is not used in production
- Response delays simulate real network latency
- Data is reset on page refresh
- Useful for offline development and UI testing

## Transition to Real API

When moving from mock to real backend:

1. Ensure backend is running (`./run-with-postgres.sh`)
2. Set `USE_MOCK_API: false` in config
3. Verify API endpoints match expected format
4. Test authentication flow
5. Confirm data structures match

The mock API matches the real API contract, so switching over is straightforward.

