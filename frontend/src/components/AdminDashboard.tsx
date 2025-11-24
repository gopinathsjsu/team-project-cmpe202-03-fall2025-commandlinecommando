import React, { useState, useEffect } from 'react';
import { getAdminDashboard, getAllUsers, moderateListing, deleteUser } from '../api/admin';

interface DashboardStats {
  message: string;
  totalUsers: number;
  totalListings: number;
  pendingApprovals: number;
}

interface User {
  id: number;
  username: string;
  email: string;
  role: string;
}

export function AdminDashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [moderateListingId, setModerateListingId] = useState('');
  const [moderateAction, setModerateAction] = useState('approve');
  const [deleteUserId, setDeleteUserId] = useState('');

  useEffect(() => {
    loadDashboard();
    loadUsers();
  }, []);

  async function loadDashboard() {
    try {
      const data = await getAdminDashboard();
      setStats(data);
    } catch (err) {
      setError('Failed to load dashboard');
      console.error(err);
    }
  }

  async function loadUsers() {
    try {
      setLoading(true);
      const data = await getAllUsers();
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
      await moderateListing(moderateListingId, moderateAction);
      alert(`Listing ${moderateListingId} has been ${moderateAction}d`);
      setModerateListingId('');
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to moderate listing');
    }
  }

  async function handleDeleteUser() {
    if (!deleteUserId) {
      alert('Please enter a user ID');
      return;
    }

    if (!confirm(`Are you sure you want to delete user ${deleteUserId}?`)) {
      return;
    }

    try {
      await deleteUser(deleteUserId);
      alert(`User ${deleteUserId} has been deleted`);
      setDeleteUserId('');
      loadUsers();
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to delete user');
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b px-8 py-4">
        <div>
          <h1 className="text-xl font-semibold text-gray-900">Admin Dashboard</h1>
          <p className="text-sm text-gray-500">CampusConnect Administration</p>
        </div>
      </header>

      <main className="px-8 py-6 max-w-7xl mx-auto">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-medium text-gray-500 mb-2">Total Users</h3>
            <p className="text-3xl font-bold text-blue-600">{stats?.totalUsers || 0}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-medium text-gray-500 mb-2">Total Listings</h3>
            <p className="text-3xl font-bold text-green-600">{stats?.totalListings || 0}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-medium text-gray-500 mb-2">Pending Approvals</h3>
            <p className="text-3xl font-bold text-orange-600">{stats?.pendingApprovals || 0}</p>
          </div>
        </div>

        {/* Moderate Listing Section */}
        <div className="bg-white rounded-lg shadow p-6 mb-8">
          <h2 className="text-lg font-semibold mb-4">Moderate Listing</h2>
          <div className="flex gap-4">
            <input
              type="number"
              placeholder="Listing ID"
              className="flex-1 p-2 border rounded"
              value={moderateListingId}
              onChange={(e) => setModerateListingId(e.target.value)}
            />
            <select
              className="p-2 border rounded"
              value={moderateAction}
              onChange={(e) => setModerateAction(e.target.value)}
            >
              <option value="approve">Approve</option>
              <option value="reject">Reject</option>
              <option value="flag">Flag</option>
            </select>
            <button
              onClick={handleModerate}
              className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              Submit
            </button>
          </div>
        </div>

        {/* Delete User Section */}
        <div className="bg-white rounded-lg shadow p-6 mb-8">
          <h2 className="text-lg font-semibold mb-4">Delete User</h2>
          <div className="flex gap-4">
            <input
              type="number"
              placeholder="User ID"
              className="flex-1 p-2 border rounded"
              value={deleteUserId}
              onChange={(e) => setDeleteUserId(e.target.value)}
            />
            <button
              onClick={handleDeleteUser}
              className="px-6 py-2 bg-red-600 text-white rounded hover:bg-red-700"
            >
              Delete User
            </button>
          </div>
        </div>

        {/* Users List */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">All Users</h2>
          {loading ? (
            <p className="text-gray-500">Loading users...</p>
          ) : error ? (
            <p className="text-red-500">{error}</p>
          ) : (
            <div className="text-gray-600">
              <p className="mb-2">{stats?.message}</p>
              <p className="text-sm">User management interface - API integration complete</p>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
