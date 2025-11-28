import React, { useState, useEffect } from 'react';
import { adminApi } from '../api';
import { useAuth } from '../context/AuthContext';
import { ThemeToggle } from './ThemeToggle';

interface DashboardStats {
  message: string;
  totalUsers: number;
  totalListings: number;
  pendingApprovals: number;
  pendingReports?: number;
}

interface Analytics {
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

interface User {
  userId: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];  // Changed from single role to array
  verificationStatus?: string;
  isActive?: boolean;
  createdAt?: string;
  lastLoginAt?: string;
}

type TabType = 'overview' | 'users' | 'moderation';

export function AdminDashboard() {
  const { logout } = useAuth();
  const [activeTab, setActiveTab] = useState<TabType>('overview');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [moderateListingId, setModerateListingId] = useState('');
  const [moderateAction, setModerateAction] = useState('approve');

  useEffect(() => {
    loadDashboard();
    loadAnalytics();
    loadUsers();
  }, []);

  async function loadDashboard() {
    try {
      const data = await adminApi.getDashboard();
      setStats(data);
    } catch (err) {
      setError('Failed to load dashboard');
      console.error(err);
    }
  }

  async function loadAnalytics() {
    try {
      const data = await adminApi.getAnalytics();
      setAnalytics(data);
    } catch (err) {
      console.error('Failed to load analytics:', err);
    }
  }

  async function loadUsers() {
    try {
      setLoading(true);
      const data = await adminApi.getAllUsers();
      setUsers(data.users || []);
    } catch (err) {
      setError('Failed to load users');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  async function handleModerate() {
    if (!moderateListingId) {
      alert('Please enter a listing ID');
      return;
    }
    
    try {
      await adminApi.moderateListing(moderateListingId, moderateAction);
      alert(`Listing ${moderateListingId} has been ${moderateAction}d`);
      setModerateListingId('');
      loadDashboard();
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to moderate listing');
    }
  }

  async function handleDeleteUser(userId: string) {
    if (!confirm('Are you sure you want to delete this user?')) return;
    try {
      await adminApi.deleteUser(userId);
      loadUsers();
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to delete user');
    }
  }

  function formatDate(dateString: string) {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  return (
    <div className="min-h-screen">
      <div className="fixed inset-0 -z-10 bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-pink-500/10 dark:from-indigo-900/20 dark:via-purple-900/10 dark:to-pink-900/20"></div>
      <header className="nav-glass px-4 sm:px-8 py-4 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2 sm:gap-4">
            <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-xl gradient-primary flex items-center justify-center shadow-lg">
              <svg className="w-4 h-4 sm:w-5 sm:h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
            <div className="hidden sm:block">
              <h1 className="text-xl font-bold gradient-text">CampusConnect</h1>
              <p className="text-sm text-muted">Administration Panel</p>
            </div>
            <h1 className="sm:hidden text-lg font-bold gradient-text">Admin</h1>
          </div>
          <div className="flex items-center gap-2 sm:gap-3">
            <ThemeToggle />
            <button
              onClick={logout}
              className="nav-button !px-2 sm:!px-4 hover:bg-red-500/20 hover:text-red-500 hover:border-red-500/30"
            >
              <span className="hidden sm:inline">Logout</span>
              <svg className="w-4 h-4 sm:hidden" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </button>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-8 py-6">
        <div className="flex gap-1 sm:gap-2 mb-6 glass-card p-1.5 sm:p-2 overflow-x-auto">
          {(['overview', 'users', 'moderation'] as TabType[]).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-2 sm:px-4 py-2 rounded-lg text-xs sm:text-sm font-medium capitalize transition-all whitespace-nowrap flex-shrink-0 ${activeTab === tab ? 'gradient-primary text-white shadow-md' : 'text-muted hover:bg-white/50 dark:hover:bg-white/10'}`}
            >
              {tab}
            </button>
          ))}
        </div>

        {activeTab === 'overview' && (
          <div>
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 mb-8">
              <div className="stat-card">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 rounded-lg bg-blue-500/20 flex items-center justify-center">
                    <svg className="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                    </svg>
                  </div>
                  <h3 className="text-sm font-medium text-muted">Total Users</h3>
                </div>
                <p className="stat-value">{analytics?.totalUsers || stats?.totalUsers || 0}</p>
                {analytics?.monthlyGrowth && (
                  <p className="text-sm text-green-500 mt-1 flex items-center gap-1">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" /></svg>
                    +{analytics.monthlyGrowth.users}% this month
                  </p>
                )}
              </div>
              <div className="stat-card">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 rounded-lg bg-green-500/20 flex items-center justify-center">
                    <svg className="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                    </svg>
                  </div>
                  <h3 className="text-sm font-medium text-muted">Active Listings</h3>
                </div>
                <p className="stat-value">{analytics?.activeListings || stats?.totalListings || 0}</p>
                {analytics?.monthlyGrowth && (
                  <p className="text-sm text-green-500 mt-1 flex items-center gap-1">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" /></svg>
                    +{analytics.monthlyGrowth.listings}% this month
                  </p>
                )}
              </div>
              <div className="stat-card">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 rounded-lg bg-purple-500/20 flex items-center justify-center">
                    <svg className="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <h3 className="text-sm font-medium text-muted">Completed Orders</h3>
                </div>
                <p className="stat-value">{analytics?.completedOrders || 0}</p>
                {analytics?.monthlyGrowth && (
                  <p className="text-sm text-green-500 mt-1 flex items-center gap-1">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" /></svg>
                    +{analytics.monthlyGrowth.orders}% this month
                  </p>
                )}
              </div>
              <div className="stat-card">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 rounded-lg bg-orange-500/20 flex items-center justify-center">
                    <svg className="w-5 h-5 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                  </div>
                  <h3 className="text-sm font-medium text-muted">Pending Reports</h3>
                </div>
                <p className="stat-value">{analytics?.pendingReports || stats?.pendingReports || 0}</p>
              </div>
            </div>

            {analytics?.recentActivity && (
              <div className="glass-card p-6 mb-8">
                <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                  <svg className="w-5 h-5 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                  </svg>
                  Recent Activity
                </h2>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
                  <div className="p-4 rounded-lg bg-white/50 dark:bg-white/5">
                    <p className="text-2xl font-bold gradient-text">{analytics.recentActivity.newUsersToday}</p>
                    <p className="text-sm text-muted">New users today</p>
                  </div>
                  <div className="p-4 rounded-lg bg-white/50 dark:bg-white/5">
                    <p className="text-2xl font-bold gradient-text">{analytics.recentActivity.newUsersThisWeek}</p>
                    <p className="text-sm text-muted">New users this week</p>
                  </div>
                  <div className="p-4 rounded-lg bg-white/50 dark:bg-white/5">
                    <p className="text-2xl font-bold gradient-text">{analytics.recentActivity.newListingsToday}</p>
                    <p className="text-sm text-muted">New listings today</p>
                  </div>
                  <div className="p-4 rounded-lg bg-white/50 dark:bg-white/5">
                    <p className="text-2xl font-bold gradient-text">{analytics.recentActivity.newListingsThisWeek}</p>
                    <p className="text-sm text-muted">New listings this week</p>
                  </div>
                  <div className="p-4 rounded-lg bg-white/50 dark:bg-white/5">
                    <p className="text-2xl font-bold gradient-text">{analytics.recentActivity.ordersToday}</p>
                    <p className="text-sm text-muted">Orders today</p>
                  </div>
                  <div className="p-4 rounded-lg bg-white/50 dark:bg-white/5">
                    <p className="text-2xl font-bold gradient-text">{analytics.recentActivity.ordersThisWeek}</p>
                    <p className="text-sm text-muted">Orders this week</p>
                  </div>
                </div>
              </div>
            )}

            {analytics?.popularCategories && (
              <div className="glass-card p-6">
                <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                  <svg className="w-5 h-5 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                  Popular Categories
                </h2>
                <div className="space-y-4">
                  {analytics.popularCategories.map(cat => (
                    <div key={cat.category} className="flex items-center gap-4">
                      <span className="w-32 text-sm text-muted">{cat.category.replace(/_/g, ' ')}</span>
                      <div className="flex-1 bg-white/30 dark:bg-white/10 rounded-full h-3 overflow-hidden">
                        <div
                          className="gradient-primary h-3 rounded-full transition-all duration-500"
                          style={{ width: `${cat.percentage}%` }}
                        />
                      </div>
                      <span className="w-20 text-sm font-medium text-right">{cat.count} items</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {activeTab === 'users' && (
          <div className="glass-card overflow-hidden">
            <div className="p-6 border-b border-white/10">
              <h2 className="text-lg font-semibold">All Users ({users.length})</h2>
            </div>
            {loading ? (
              <div className="p-8 text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-white/30 dark:bg-white/5">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-muted uppercase">User</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-muted uppercase">Email</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-muted uppercase">Roles</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-muted uppercase">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-muted uppercase">Joined</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-muted uppercase">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-white/10">
                    {users.map(user => (
                      <tr key={user.userId} className="hover:bg-white/30 dark:hover:bg-white/5 transition-colors">
                        <td className="px-6 py-4">
                          <div>
                            <p className="font-medium">{user.firstName} {user.lastName}</p>
                            <p className="text-sm text-muted">@{user.username}</p>
                          </div>
                        </td>
                        <td className="px-6 py-4 text-sm">{user.email}</td>
                        <td className="px-6 py-4">
                          <div className="flex flex-wrap gap-1">
                            {(user.roles || []).map(role => (
                              <span 
                                key={role} 
                                className={`badge ${
                                  role === 'ADMIN' ? 'badge-primary' : 
                                  role === 'SELLER' ? 'badge-warning' : 
                                  'badge-success'
                                }`}
                              >
                                {role}
                              </span>
                            ))}
                          </div>
                        </td>
                        <td className="px-6 py-4">
                          <span className={`badge ${user.isActive ? 'badge-success' : 'badge-danger'}`}>
                            {user.isActive ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-sm text-muted">
                          {user.createdAt ? formatDate(user.createdAt) : 'N/A'}
                        </td>
                        <td className="px-6 py-4">
                          <button
                            onClick={() => handleDeleteUser(user.userId)}
                            className="nav-button text-xs hover:bg-red-500/20 hover:text-red-500 hover:border-red-500/30"
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {activeTab === 'moderation' && (
          <div className="space-y-6">
            <div className="glass-card p-6">
              <h2 className="text-lg font-semibold mb-4">Moderate Listing</h2>
              <p className="text-sm text-muted mb-4">
                Enter a listing ID to approve, reject, or flag it for review.
              </p>
              <div className="flex gap-4">
                <input
                  type="text"
                  placeholder="Listing ID"
                  className="input-glass flex-1"
                  value={moderateListingId}
                  onChange={(e) => setModerateListingId(e.target.value)}
                />
                <select
                  className="filter-select"
                  value={moderateAction}
                  onChange={(e) => setModerateAction(e.target.value)}
                >
                  <option value="approve">Approve</option>
                  <option value="reject">Reject</option>
                  <option value="flag">Flag</option>
                </select>
                <button
                  onClick={handleModerate}
                  className="nav-button-primary"
                >
                  Submit
                </button>
              </div>
            </div>

            <div className="glass-card p-6">
              <h2 className="text-lg font-semibold mb-4">Moderation Guidelines</h2>
              <div className="space-y-4 text-sm text-muted">
                <div className="glass-card p-4">
                  <h3 className="font-medium gradient-text mb-1">Approve</h3>
                  <p>Listing meets community guidelines and is safe to display.</p>
                </div>
                <div className="glass-card p-4">
                  <h3 className="font-medium gradient-text mb-1">Reject</h3>
                  <p>Listing violates community guidelines and should be removed.</p>
                </div>
                <div className="glass-card p-4">
                  <h3 className="font-medium gradient-text mb-1">Flag</h3>
                  <p>Listing needs further review or contains questionable content.</p>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

    </div>
  );
}
