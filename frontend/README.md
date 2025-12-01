# Campus Marketplace Frontend

React application for the Campus Marketplace built with Vite and TypeScript.

## Quick Start

### Prerequisites

- Node.js 18+
- npm or yarn

### Setup

```bash
# Install dependencies
npm install

# Configure environment
cp .env.example .env

# Start development server
npm run dev
```

The app runs at `http://localhost:5173`

## Environment Variables

Create a `.env` file:

```bash
# Backend API URL
VITE_API_BASE_URL=http://localhost:8080/api

# AI Service URL (optional)
VITE_AI_API_SERVICE_URL=http://localhost:3001
```

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |

## Project Structure

```
src/
├── api/                 # API client and endpoints
│   ├── auth.ts          # Authentication API
│   ├── listings.ts      # Listings API
│   ├── chat.ts          # Chat API
│   ├── admin.ts         # Admin API
│   └── config.ts        # API configuration
├── components/          # React components
│   ├── LoginForm.tsx
│   ├── RegisterForm.tsx
│   ├── MarketplacePage.tsx
│   ├── ListingDetailsModal.tsx
│   ├── MessagesPage.tsx
│   ├── UserProfilePage.tsx
│   ├── AdminDashboard.tsx
│   ├── AskAIPage.tsx
│   └── ...
├── context/             # React contexts
│   ├── AuthContext.tsx  # Authentication state
│   └── ThemeContext.tsx # Theme (dark/light mode)
├── types/               # TypeScript types
├── utils/               # Utility functions
├── styles/              # CSS styles
├── App.tsx              # Main app component
├── router.tsx           # Route definitions
└── main.tsx             # Entry point
```

## Features

### Authentication

- Login and registration forms
- JWT token management
- Protected routes
- Password reset

### Marketplace

- Browse listings with filters
- Search functionality
- Category filtering
- Listing details modal
- Image galleries

### User Features

- Create and manage listings
- Image upload to S3
- Favorites/wishlist
- User profile

### Messaging

- Real-time chat interface
- Conversation list
- Unread message counts

### Admin Dashboard

- User management
- Content moderation
- Report handling

### AI Chat

- Ask AI page for marketplace assistance
- Connects to AI integration server

## Tech Stack

| Library | Purpose |
|---------|---------|
| React 19 | UI framework |
| TypeScript | Type safety |
| Vite | Build tool |
| Tailwind CSS | Styling |
| Axios | HTTP client |
| React Router | Routing |
| Lucide React | Icons |

## API Integration

API calls are centralized in `src/api/`:

```typescript
// Example: Login
import { login } from './api/auth';

const response = await login({ username, password });
```

```typescript
// Example: Get listings
import { getListings } from './api/listings';

const listings = await getListings({ page: 0, size: 20 });
```

## Development Notes

### Mock API

During initial development, a mock API was used when the backend wasn't ready. The mock data is in `src/api/mockData.ts` and can be enabled in `src/api/config.ts`:

```typescript
export const API_CONFIG = {
  USE_MOCK_API: true,  // Set to false for real backend
  // ...
};
```

### Styling

Uses Tailwind CSS with custom configuration in `tailwind.config.cjs`. Global styles are in `src/styles/index.css`.

### Authentication Context

The `AuthContext` provides:

- `user` - Current user object
- `login()` - Login function
- `logout()` - Logout function
- `isAuthenticated` - Auth status

### Protected Routes

Use the `ProtectedRoute` component:

```tsx
<ProtectedRoute requiredRoles={['ADMIN']}>
  <AdminDashboard />
</ProtectedRoute>
```

## Building for Production

```bash
npm run build
```

Output is in the `dist/` folder, ready for deployment.
