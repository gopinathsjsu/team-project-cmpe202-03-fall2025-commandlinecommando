import {
  mockUsers,
  mockListings,
  mockConversations,
  mockMessages,
  mockReports,
  mockAnalytics,
  mockFavorites,
  mockRecentlyViewed,
  mockSearchHistory,
} from './mockData';

const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

let currentUser: any = null;
let favorites = [...mockFavorites];
let recentlyViewed = [...mockRecentlyViewed];
let listings = [...mockListings];
let users = [...mockUsers];
let reports = [...mockReports];
let conversations = [...mockConversations];
let messages = { ...mockMessages };

export const mockAuthApi = {
  async login(username: string, password: string) {
    await delay(500);
    const user = users.find(u => u.username === username);
    if (!user) {
      throw { response: { status: 401, data: { message: 'Invalid username or password' } } };
    }
    currentUser = user;
    return {
      accessToken: 'mock-access-token-' + Date.now(),
      refreshToken: 'mock-refresh-token-' + Date.now(),
      tokenType: 'Bearer',
      expiresIn: 3600,
      userId: user.userId,
      username: user.username,
      roles: user.roles || ['BUYER', 'SELLER'],  // Updated to roles array
    };
  },

  async register(payload: any) {
    await delay(500);
    const existingUser = users.find(u => u.username === payload.username || u.email === payload.email);
    if (existingUser) {
      throw { response: { status: 409, data: { message: 'Username or email already exists' } } };
    }
    const newUser = {
      userId: crypto.randomUUID(),
      username: payload.username,
      email: payload.email,
      firstName: payload.firstName,
      lastName: payload.lastName,
      phone: '',
      roles: ['BUYER', 'SELLER'],  // Students get both roles by default
      verificationStatus: 'PENDING',
      universityId: '00000000-0000-0000-0000-000000000001',
      isActive: true,
      createdAt: new Date().toISOString(),
      lastLoginAt: new Date().toISOString(),
    };
    users.push(newUser);
    currentUser = newUser;
    return {
      accessToken: 'mock-access-token-' + Date.now(),
      refreshToken: 'mock-refresh-token-' + Date.now(),
      tokenType: 'Bearer',
      expiresIn: 3600,
      userId: newUser.userId,
      username: newUser.username,
      roles: newUser.roles,  // Updated to roles array
    };
  },

  async me() {
    await delay(200);
    if (!currentUser) {
      throw { response: { status: 401, data: { message: 'Not authenticated' } } };
    }
    return currentUser;
  },

  async validateToken() {
    await delay(200);
    return { valid: !!currentUser, username: currentUser?.username, roles: currentUser?.roles };
  },

  async refresh() {
    await delay(200);
    if (!currentUser) {
      throw { response: { status: 401, data: { message: 'Invalid refresh token' } } };
    }
    return {
      accessToken: 'mock-access-token-' + Date.now(),
      refreshToken: 'mock-refresh-token-' + Date.now(),
      tokenType: 'Bearer',
      expiresIn: 3600,
    };
  },

  async logout() {
    await delay(200);
    currentUser = null;
    return { message: 'Logged out successfully' };
  },

  async requestPasswordReset(email: string) {
    await delay(500);
    const user = users.find(u => u.email === email);
    if (!user) {
      return { message: 'If an account exists, a reset link has been sent' };
    }
    return { message: 'Password reset link sent', token: 'mock-reset-token-' + Date.now() };
  },

  async resetPassword() {
    await delay(500);
    return { message: 'Password reset successful' };
  },
};

export const mockListingsApi = {
  async getListings(page = 0, size = 20) {
    await delay(300);
    const start = page * size;
    const end = start + size;
    const activeListings = listings.filter(l => l.status === 'ACTIVE');
    return {
      content: activeListings.slice(start, end).map(l => ({
        ...l,
        favorite: favorites.includes(l.id),
      })),
      totalElements: activeListings.length,
      totalPages: Math.ceil(activeListings.length / size),
      number: page,
      size,
    };
  },

  async getListing(id: string) {
    await delay(200);
    const listing = listings.find(l => l.id === id);
    if (!listing) {
      throw { response: { status: 404, data: { message: 'Listing not found' } } };
    }
    if (!recentlyViewed.includes(id)) {
      recentlyViewed.unshift(id);
      recentlyViewed = recentlyViewed.slice(0, 10);
    }
    listing.viewCount = (listing.viewCount || 0) + 1;
    return { ...listing, favorite: favorites.includes(id) };
  },

  async createListing(data: any) {
    await delay(500);
    if (!currentUser) {
      throw { response: { status: 401, data: { message: 'Not authenticated' } } };
    }
    const newListing = {
      id: crypto.randomUUID(),
      ...data,
      sellerId: currentUser.userId,
      seller: {
        id: currentUser.userId,
        name: `${currentUser.firstName} ${currentUser.lastName}`,
        username: currentUser.username,
        avatarUrl: currentUser.avatarUrl,
      },
      imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=300&fit=crop',
      images: [],
      status: 'ACTIVE',
      viewCount: 0,
      favoriteCount: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    listings.unshift(newListing);
    return { listingId: newListing.id, status: 'ACTIVE', message: 'Listing created successfully' };
  },

  async updateListing(id: string, data: any) {
    await delay(300);
    const index = listings.findIndex(l => l.id === id);
    if (index === -1) {
      throw { response: { status: 404, data: { message: 'Listing not found' } } };
    }
    listings[index] = { ...listings[index], ...data, updatedAt: new Date().toISOString() };
    return { message: 'Listing updated successfully', listingId: id };
  },

  async deleteListing(id: string) {
    await delay(300);
    const index = listings.findIndex(l => l.id === id);
    if (index === -1) {
      throw { response: { status: 404, data: { message: 'Listing not found' } } };
    }
    listings.splice(index, 1);
    return { message: 'Listing deleted successfully' };
  },

  async searchListings(params: any) {
    await delay(400);
    let results = listings.filter(l => l.status === 'ACTIVE');

    if (params.keyword) {
      const keyword = params.keyword.toLowerCase();
      results = results.filter(
        l => l.title.toLowerCase().includes(keyword) || l.description.toLowerCase().includes(keyword)
      );
    }
    if (params.category && params.category !== 'all') {
      results = results.filter(l => l.category === params.category);
    }
    if (params.condition && params.condition !== 'all') {
      results = results.filter(l => l.condition === params.condition);
    }
    if (params.minPrice !== undefined) {
      results = results.filter(l => l.price >= params.minPrice);
    }
    if (params.maxPrice !== undefined) {
      results = results.filter(l => l.price <= params.maxPrice);
    }
    if (params.location) {
      results = results.filter(l => l.location.toLowerCase().includes(params.location.toLowerCase()));
    }

    const page = params.page || 0;
    const size = params.size || 20;
    const start = page * size;
    const end = start + size;
    const paginatedResults = results.slice(start, end).map(l => ({ ...l, favorite: favorites.includes(l.id) }));
    const totalPages = Math.ceil(results.length / size);

    // Backend SearchResponse uses 'results' field, but we also include 'content' for compatibility
    return {
      results: paginatedResults,
      content: paginatedResults,  // For backwards compatibility with getListings-style responses
      totalResults: results.length,
      totalElements: results.length,
      totalPages,
      currentPage: page,
      pageSize: size,
      hasNext: page < totalPages - 1,
      hasPrevious: page > 0,
    };
  },

  async toggleFavorite(id: string) {
    await delay(200);
    const index = favorites.indexOf(id);
    if (index === -1) {
      favorites.push(id);
      const listing = listings.find(l => l.id === id);
      if (listing) listing.favoriteCount = (listing.favoriteCount || 0) + 1;
      return { favorited: true };
    } else {
      favorites.splice(index, 1);
      const listing = listings.find(l => l.id === id);
      if (listing) listing.favoriteCount = Math.max(0, (listing.favoriteCount || 0) - 1);
      return { favorited: false };
    }
  },

  async getFavorites() {
    await delay(300);
    return listings.filter(l => favorites.includes(l.id)).map(l => ({ ...l, favorite: true }));
  },

  async reportListing(id: string, data: any) {
    await delay(400);
    const listing = listings.find(l => l.id === id);
    if (!listing) {
      throw { response: { status: 404, data: { message: 'Listing not found' } } };
    }
    // Backend uses UUID for reportId
    const reportId = crypto.randomUUID();
    const newReport = {
      reportId,
      reportType: data.reportType,
      reason: data.reportType,  // Backend expects 'reason' field
      description: data.description,
      listingId: id,
      listing,
      reporterId: currentUser?.userId,
      reporter: currentUser,
      status: 'PENDING',
      severity: 'medium',
      priority: 'MEDIUM',  // Backend uses priority
      createdAt: new Date().toISOString(),
    };
    reports.unshift(newReport);
    return { reportId: newReport.reportId, status: 'PENDING', message: 'Report submitted successfully' };
  },
};

export const mockDiscoveryApi = {
  async getTrending(limit = 10) {
    await delay(300);
    const sorted = [...listings]
      .filter(l => l.status === 'ACTIVE')
      .sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
      .slice(0, limit);
    // Backend returns { trending: [...] } per TrendingResponse.java
    return { trending: sorted.map(l => ({ ...l, favorite: favorites.includes(l.id) })) };
  },

  async getRecommended(limit = 10) {
    await delay(300);
    const recent = recentlyViewed.slice(0, 3);
    const recentCategories = listings
      .filter(l => recent.includes(l.id))
      .map(l => l.category);
    
    let recommendations = listings.filter(
      l => l.status === 'ACTIVE' && recentCategories.includes(l.category) && !recent.includes(l.id)
    );
    
    if (recommendations.length < limit) {
      const others = listings.filter(
        l => l.status === 'ACTIVE' && !recommendations.find(r => r.id === l.id) && !recent.includes(l.id)
      );
      recommendations = [...recommendations, ...others].slice(0, limit);
    }

    // Backend returns { recommended: [...] } per RecommendedResponse.java
    return {
      recommended: recommendations.slice(0, limit).map((l, i) => ({
        ...l,
        favorite: favorites.includes(l.id),
        reason: 'Based on your browsing history',
        score: 0.95 - i * 0.05,
      })),
    };
  },

  async getSimilar(productId: string, limit = 5) {
    await delay(300);
    const product = listings.find(l => l.id === productId);
    if (!product) return { similar: [] };

    const similar = listings
      .filter(l => l.status === 'ACTIVE' && l.id !== productId && l.category === product.category)
      .slice(0, limit)
      .map((l, i) => ({ ...l, favorite: favorites.includes(l.id), similarity: 0.9 - i * 0.1 }));
    return { similar };
  },

  async getRecentlyViewed(limit = 10) {
    await delay(200);
    const viewed = recentlyViewed
      .slice(0, limit)
      .map(id => listings.find(l => l.id === id))
      .filter(Boolean)
      .map(l => ({ ...l!, favorite: favorites.includes(l!.id), viewedAt: new Date().toISOString() }));
    // Backend returns { recentlyViewed: [...] } per RecentlyViewedResponse.java
    return { recentlyViewed: viewed };
  },
};

export const mockSearchApi = {
  async autocomplete(query: string) {
    await delay(150);
    if (query.length < 2) return { suggestions: [] };
    const q = query.toLowerCase();
    const titles = listings.map(l => l.title.toLowerCase());
    const suggestions = titles
      .filter(t => t.includes(q))
      .map(t => t.split(' ').slice(0, 4).join(' '))
      .slice(0, 5);
    return { suggestions: [...new Set(suggestions)] };
  },

  async getHistory(limit = 10) {
    await delay(200);
    return { searches: mockSearchHistory.slice(0, limit) };
  },
};

export const mockAdminApi = {
  async getDashboard() {
    await delay(300);
    return {
      message: 'Admin dashboard loaded',
      totalUsers: users.length,
      totalListings: listings.length,
      pendingApprovals: listings.filter(l => l.status === 'PENDING').length,
      pendingReports: reports.filter(r => r.status === 'PENDING').length,
    };
  },

  async getAnalytics() {
    await delay(400);
    return mockAnalytics;
  },

  async getAllUsers(page = 0, size = 20) {
    await delay(300);
    const start = page * size;
    const end = start + size;
    return {
      users: users.slice(start, end),
      totalElements: users.length,
      totalPages: Math.ceil(users.length / size),
      currentPage: page,
    };
  },

  async getUser(userId: string) {
    await delay(200);
    const user = users.find(u => u.userId === userId);
    if (!user) {
      throw { response: { status: 404, data: { message: 'User not found' } } };
    }
    return {
      ...user,
      productsListed: listings.filter(l => l.sellerId === userId).length,
      ordersMade: Math.floor(Math.random() * 10),
    };
  },

  async updateUser(userId: string, data: any) {
    await delay(300);
    const index = users.findIndex(u => u.userId === userId);
    if (index === -1) {
      throw { response: { status: 404, data: { message: 'User not found' } } };
    }
    users[index] = { ...users[index], ...data };
    return { message: 'User updated successfully', userId };
  },

  async deleteUser(userId: string) {
    await delay(300);
    const index = users.findIndex(u => u.userId === userId);
    if (index === -1) {
      throw { response: { status: 404, data: { message: 'User not found' } } };
    }
    users.splice(index, 1);
    return { message: 'User deleted successfully' };
  },

  async getReports(status?: string) {
    await delay(300);
    let filtered = [...reports];
    if (status && status !== 'all') {
      filtered = filtered.filter(r => r.status === status);
    }
    // Backend returns paginated response with 'content' field
    return { content: filtered, reports: filtered, totalElements: filtered.length, totalPages: 1 };
  },

  async updateReport(reportId: string, data: any) {
    await delay(300);
    // Find by string reportId (UUID)
    const index = reports.findIndex(r => r.reportId === reportId || r.reportId?.toString() === reportId);
    if (index === -1) {
      throw { response: { status: 404, data: { message: 'Report not found' } } };
    }
    reports[index] = { ...reports[index], ...data };
    return { message: 'Report updated successfully' };
  },

  async moderateListing(listingId: string, action: string) {
    await delay(400);
    const listing = listings.find(l => l.id === listingId);
    if (!listing) {
      throw { response: { status: 404, data: { message: 'Listing not found' } } };
    }
    if (action === 'approve') {
      listing.status = 'ACTIVE';
    } else if (action === 'reject') {
      listing.status = 'REJECTED';
    } else if (action === 'flag') {
      listing.status = 'FLAGGED';
    }
    return { message: `Listing ${action}d successfully` };
  },
};

export const mockChatApi = {
  async getConversations() {
    await delay(300);
    // Backend returns List<ConversationResponse> (array directly, not wrapped)
    return conversations.filter(
      c => c.buyer.userId === currentUser?.userId || c.seller.userId === currentUser?.userId
    );
  },

  async createConversation(data: any) {
    await delay(400);
    const listing = listings.find(l => l.id === data.listingId);
    if (!listing) {
      throw { response: { status: 404, data: { message: 'Listing not found' } } };
    }
    const seller = users.find(u => u.userId === listing.sellerId);
    const newConversation = {
      conversationId: conversations.length + 1,
      listingId: data.listingId,
      listing: {
        title: listing.title,
        price: listing.price,
        imageUrl: listing.imageUrl,
      },
      buyer: {
        userId: currentUser?.userId,
        username: currentUser?.username,
        firstName: currentUser?.firstName,
        lastName: currentUser?.lastName,
        avatarUrl: currentUser?.avatarUrl,
      },
      seller: {
        userId: seller?.userId,
        username: seller?.username,
        firstName: seller?.firstName,
        lastName: seller?.lastName,
        avatarUrl: seller?.avatarUrl,
      },
      lastMessage: {
        content: data.initialMessage,
        createdAt: new Date().toISOString(),
        isRead: false,
        senderId: currentUser?.userId,
      },
      unreadCount: 0,
      updatedAt: new Date().toISOString(),
    };
    conversations.push(newConversation);
    messages[newConversation.conversationId] = [
      {
        messageId: Date.now(),
        conversationId: newConversation.conversationId,
        senderId: currentUser?.userId,
        senderName: `${currentUser?.firstName} ${currentUser?.lastName}`,
        content: data.initialMessage,
        isRead: false,
        createdAt: new Date().toISOString(),
      },
    ];
    return { conversationId: newConversation.conversationId, message: 'Conversation created successfully' };
  },

  async getMessages(conversationId: string | number) {
    await delay(200);
    const numId = typeof conversationId === 'string' ? parseInt(conversationId) : conversationId;
    // Backend returns List<MessageResponse> (array directly, not wrapped)
    return messages[numId] || [];
  },

  async sendMessage(data: any) {
    await delay(300);
    const conversationMessages = messages[data.conversationId] || [];
    const newMessage = {
      messageId: Date.now(),
      conversationId: data.conversationId,
      senderId: currentUser?.userId,
      senderName: `${currentUser?.firstName} ${currentUser?.lastName}`,
      content: data.content,
      isRead: false,
      createdAt: new Date().toISOString(),
    };
    conversationMessages.push(newMessage);
    messages[data.conversationId] = conversationMessages;

    const conv = conversations.find(c => c.conversationId === data.conversationId);
    if (conv) {
      conv.lastMessage = {
        content: data.content,
        createdAt: new Date().toISOString(),
        isRead: false,
        senderId: currentUser?.userId,
      };
      conv.updatedAt = new Date().toISOString();
    }

    return { messageId: newMessage.messageId, conversationId: data.conversationId, createdAt: newMessage.createdAt };
  },

  async markAsRead(conversationId: string | number) {
    await delay(200);
    const numId = typeof conversationId === 'string' ? parseInt(conversationId) : conversationId;
    const conversationMessages = messages[numId] || [];
    conversationMessages.forEach(m => {
      if (m.senderId !== currentUser?.userId) {
        m.isRead = true;
      }
    });
    const conv = conversations.find(c => c.conversationId === numId);
    if (conv) {
      conv.unreadCount = 0;
    }
    return { message: 'Messages marked as read', count: conversationMessages.length };
  },
};

export const mockStudentApi = {
  async getDashboard() {
    await delay(300);
    const myListings = listings.filter(l => l.sellerId === currentUser?.userId).length;
    return {
      message: 'Student dashboard loaded',
      myListings,
      watchlist: favorites.length,
      messages: conversations.filter(
        c => c.buyer.userId === currentUser?.userId || c.seller.userId === currentUser?.userId
      ).length,
    };
  },

  async getMyListings() {
    await delay(300);
    return listings.filter(l => l.sellerId === currentUser?.userId);
  },
};

export const mockUserApi = {
  async getProfile() {
    await delay(200);
    if (!currentUser) {
      throw { response: { status: 401, data: { message: 'Not authenticated' } } };
    }
    return currentUser;
  },

  async updateProfile(data: any) {
    await delay(400);
    if (!currentUser) {
      throw { response: { status: 401, data: { message: 'Not authenticated' } } };
    }
    const index = users.findIndex(u => u.userId === currentUser.userId);
    if (index !== -1) {
      users[index] = { ...users[index], ...data };
      currentUser = users[index];
    }
    return { message: 'Profile updated successfully', user: currentUser };
  },

  async changePassword() {
    await delay(400);
    return { message: 'Password changed successfully' };
  },

  async getMyReports() {
    await delay(300);
    if (!currentUser) {
      throw { response: { status: 401, data: { message: 'Not authenticated' } } };
    }
    // Filter reports submitted by the current user
    const myReports = reports.filter(r => r.reporterId === currentUser?.userId);
    return { content: myReports, totalElements: myReports.length, totalPages: 1 };
  },
};

export function setMockCurrentUser(user: any) {
  currentUser = user;
}

export function getMockCurrentUser() {
  return currentUser;
}
