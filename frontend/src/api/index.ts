import { API_CONFIG } from './config';
import * as realAuth from './auth';
import * as realAdmin from './admin';
import * as realListings from './listings';
import * as realStudent from './student';
import * as realChat from './chat';
import * as realUser from './user';
import {
  mockAuthApi,
  mockListingsApi,
  mockDiscoveryApi,
  mockSearchApi,
  mockAdminApi,
  mockChatApi,
  mockStudentApi,
  mockUserApi,
} from './mockApi';

export const authApi = {
  async login(username: string, password: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAuthApi.login(username, password);
    }
    return realAuth.login(username, password);
  },

  async register(payload: Record<string, unknown>) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAuthApi.register(payload);
    }
    return realAuth.register(payload);
  },

  async me() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAuthApi.me();
    }
    return realAuth.me();
  },

  async validateToken() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAuthApi.validateToken();
    }
    return realAuth.validateToken();
  },

  async refresh(refreshToken: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAuthApi.refresh();
    }
    return realAuth.refresh(refreshToken);
  },

  async logout() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAuthApi.logout();
    }
    return realAuth.logout();
  },

  async requestPasswordReset(email: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAuthApi.requestPasswordReset(email);
    }
    return realAuth.requestPasswordReset(email);
  },

  async resetPassword(token: string, newPassword: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAuthApi.resetPassword();
    }
    return realAuth.resetPassword(token, newPassword);
  },
};

export const listingsApi = {
  async getListings(page = 0, size = 20) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockListingsApi.getListings(page, size);
    }
    return realListings.getListings(page, size);
  },

  async getListing(id: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockListingsApi.getListing(id);
    }
    return realListings.getListing(id);
  },

  async createListing(data: any) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockListingsApi.createListing(data);
    }
    return realListings.createListing(data);
  },

  async updateListing(id: string, data: any) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockListingsApi.updateListing(id, data);
    }
    return realListings.updateListing(id, data);
  },

  async deleteListing(id: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockListingsApi.deleteListing(id);
    }
    return realListings.deleteListing(id);
  },

  async searchListings(params: any) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockListingsApi.searchListings(params);
    }
    return realListings.searchListings(params);
  },

  async toggleFavorite(id: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockListingsApi.toggleFavorite(id);
    }
    return realListings.toggleFavorite(id);
  },

  async getFavorites() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockListingsApi.getFavorites();
    }
    return realListings.getFavorites();
  },

  async reportListing(id: string, data: any) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockListingsApi.reportListing(id, data);
    }
    return realListings.reportListing(id, data);
  },
};

export const discoveryApi = {
  async getTrending(limit = 10) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockDiscoveryApi.getTrending(limit);
    }
    return realListings.getTrending(limit);
  },

  async getRecommended(limit = 10) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockDiscoveryApi.getRecommended(limit);
    }
    return realListings.getRecommended(limit);
  },

  async getSimilar(productId: string, limit = 5) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockDiscoveryApi.getSimilar(productId, limit);
    }
    return realListings.getSimilar(productId, limit);
  },

  async getRecentlyViewed(limit = 10) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockDiscoveryApi.getRecentlyViewed(limit);
    }
    return realListings.getRecentlyViewed(limit);
  },
};

export const searchApi = {
  async autocomplete(query: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockSearchApi.autocomplete(query);
    }
    return realListings.autocomplete(query);
  },

  async getHistory(limit = 10) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockSearchApi.getHistory(limit);
    }
    return realListings.getSearchHistory(limit);
  },
};

export const adminApi = {
  async getDashboard() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAdminApi.getDashboard();
    }
    return realAdmin.getAdminDashboard();
  },

  async getAnalytics() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAdminApi.getAnalytics();
    }
    return realAdmin.getAnalytics();
  },

  async getAllUsers(page = 0, size = 20) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAdminApi.getAllUsers(page, size);
    }
    return realAdmin.getAllUsers();
  },

  async getUser(userId: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAdminApi.getUser(userId);
    }
    return realAdmin.getUser(userId);
  },

  async updateUser(userId: string, data: any) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAdminApi.updateUser(userId, data);
    }
    return realAdmin.updateUser(userId, data);
  },

  async deleteUser(userId: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAdminApi.deleteUser(userId);
    }
    return realAdmin.deleteUser(userId);
  },

  async getReports(status?: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAdminApi.getReports(status);
    }
    return realAdmin.getReports(status);
  },

  async updateReport(reportId: string, data: any) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAdminApi.updateReport(reportId, data);
    }
    return realAdmin.updateReport(reportId, data);
  },

  async moderateListing(listingId: string, action: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockAdminApi.moderateListing(listingId, action);
    }
    return realAdmin.moderateListing(listingId, action);
  },
};

export const chatApi = {
  async getConversations() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockChatApi.getConversations();
    }
    return realChat.getConversations();
  },

  async createConversation(data: any) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockChatApi.createConversation(data);
    }
    return realChat.createConversation(data);
  },

  async getMessages(conversationId: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockChatApi.getMessages(conversationId);
    }
    return realChat.getMessages(conversationId);
  },

  async sendMessage(data: any) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockChatApi.sendMessage(data);
    }
    return realChat.sendMessage(data);
  },

  async markAsRead(conversationId: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockChatApi.markAsRead(conversationId);
    }
    return realChat.markAsRead(conversationId);
  },
};

export const studentApi = {
  async getDashboard() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockStudentApi.getDashboard();
    }
    return realStudent.getStudentDashboard();
  },

  async getMyListings() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockStudentApi.getMyListings();
    }
    return realStudent.getMyListings();
  },
};

export const userApi = {
  async getProfile() {
    if (API_CONFIG.USE_MOCK_API) {
      return mockUserApi.getProfile();
    }
    return realUser.getProfile();
  },

  async updateProfile(data: any) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockUserApi.updateProfile(data);
    }
    return realUser.updateProfile(data);
  },

  async changePassword(currentPassword: string, newPassword: string) {
    if (API_CONFIG.USE_MOCK_API) {
      return mockUserApi.changePassword();
    }
    return realUser.changePassword(currentPassword, newPassword);
  },
};

export { API_CONFIG, isUsingMockApi, toggleMockApi } from './config';
