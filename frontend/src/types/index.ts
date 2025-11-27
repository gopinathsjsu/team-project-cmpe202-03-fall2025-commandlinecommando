export interface User {
  userId: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: 'STUDENT' | 'ADMIN';
  verificationStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
  universityId?: string;
  studentId?: string;
  major?: string;
  graduationYear?: number;
  isActive: boolean;
  avatarUrl?: string;
  createdAt: string;
  lastLoginAt?: string;
}

export interface Seller {
  id: string;
  name: string;
  username: string;
  avatarUrl?: string;
}

export interface ListingImage {
  imageId: number;
  imageUrl: string;
  altText?: string;
  displayOrder: number;
}

export interface Listing {
  id: string;
  title: string;
  description: string;
  category: string;
  condition: 'NEW' | 'LIKE_NEW' | 'GOOD' | 'FAIR' | 'POOR';
  price: number;
  location: string;
  sellerId: string;
  seller: Seller;
  imageUrl: string;
  images?: ListingImage[];
  status: 'ACTIVE' | 'SOLD' | 'PENDING' | 'REJECTED' | 'FLAGGED';
  viewCount?: number;
  favoriteCount?: number;
  negotiable?: boolean;
  favorite?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Conversation {
  conversationId: number;
  listingId: string;
  listing: {
    title: string;
    price: number;
    imageUrl: string;
  };
  buyer: {
    userId: string;
    username: string;
    firstName: string;
    lastName: string;
    avatarUrl?: string;
  };
  seller: {
    userId: string;
    username: string;
    firstName: string;
    lastName: string;
    avatarUrl?: string;
  };
  lastMessage: {
    content: string;
    createdAt: string;
    isRead: boolean;
    senderId: string;
  };
  unreadCount: number;
  updatedAt: string;
}

export interface Message {
  messageId: number;
  conversationId: number;
  senderId: string;
  senderName: string;
  content: string;
  isRead: boolean;
  createdAt: string;
}

export interface Report {
  reportId: number;
  reportType: 'SPAM' | 'INAPPROPRIATE' | 'SCAM' | 'WRONG_CATEGORY' | 'OTHER';
  description: string;
  listingId: string;
  listing?: Listing;
  reporterId: string;
  reporter?: User;
  status: 'PENDING' | 'UNDER_REVIEW' | 'RESOLVED' | 'DISMISSED';
  severity: 'low' | 'medium' | 'high';
  createdAt: string;
  resolvedAt?: string;
}

export interface Analytics {
  totalUsers: number;
  activeUsers: number;
  totalProducts: number;
  activeListings: number;
  totalOrders: number;
  completedOrders: number;
  pendingReports: number;
  revenue: number;
  popularCategories: {
    category: string;
    count: number;
    percentage: number;
  }[];
  recentActivity: {
    newUsersToday: number;
    newUsersThisWeek: number;
    newListingsToday: number;
    newListingsThisWeek: number;
    ordersToday: number;
    ordersThisWeek: number;
  };
  monthlyGrowth: {
    users: number;
    listings: number;
    orders: number;
  };
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: string;
  username: string;
  role: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
