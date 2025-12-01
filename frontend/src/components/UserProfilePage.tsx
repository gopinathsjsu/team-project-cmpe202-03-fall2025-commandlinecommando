import React, { useState, useEffect } from 'react';
import { userApi, listingsApi, studentApi } from '../api';
import { useAuth } from '../context/AuthContext';

interface ProfileData {
  userId: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  major?: string;
  graduationYear?: number;
  avatarUrl?: string;
  createdAt: string;
}

interface MyListing {
  id: string;
  title: string;
  price: number;
  status: string;
  imageUrl: string;
  viewCount?: number;
  favoriteCount?: number;
  createdAt: string;
}

interface Props {
  onBack: () => void;
}

export function UserProfilePage({ onBack }: Props) {
  const { user, logout } = useAuth();
  const [profile, setProfile] = useState<ProfileData | null>(null);
  const [myListings, setMyListings] = useState<MyListing[]>([]);
  const [favorites, setFavorites] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'profile' | 'listings' | 'favorites'>('profile');
  const [isEditing, setIsEditing] = useState(false);
  const [editData, setEditData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    major: '',
    graduationYear: '',
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      const [profileData, listingsData, favoritesData] = await Promise.all([
        userApi.getProfile(),
        studentApi.getMyListings(),
        listingsApi.getFavorites(),
      ]);
      setProfile(profileData);
      setMyListings(listingsData || []);
      setFavorites(favoritesData || []);
      setEditData({
        firstName: profileData.firstName || '',
        lastName: profileData.lastName || '',
        phone: profileData.phone || '',
        major: profileData.major || '',
        graduationYear: profileData.graduationYear?.toString() || '',
      });
    } catch (err) {
      console.error('Failed to load profile data:', err);
    } finally {
      setLoading(false);
    }
  }

  async function handleSaveProfile() {
    try {
      setSaving(true);
      // Clean up empty strings - send undefined instead for optional fields
      await userApi.updateProfile({
        firstName: editData.firstName || undefined,
        lastName: editData.lastName || undefined,
        phone: editData.phone || undefined,
        major: editData.major || undefined,
        graduationYear: editData.graduationYear ? parseInt(editData.graduationYear) : undefined,
      });
      await loadData();
      setIsEditing(false);
    } catch (err) {
      console.error('Failed to update profile:', err);
      alert('Failed to update profile');
    } finally {
      setSaving(false);
    }
  }

  async function handleRemoveFavorite(listingId: string) {
    try {
      await listingsApi.toggleFavorite(listingId);
      setFavorites(prev => prev.filter(f => f.id !== listingId));
    } catch (err) {
      console.error('Failed to remove favorite:', err);
    }
  }

  async function handleDeleteListing(listingId: string) {
    if (!confirm('Are you sure you want to delete this listing?')) return;
    try {
      await listingsApi.deleteListing(listingId);
      setMyListings(prev => prev.filter(l => l.id !== listingId));
    } catch (err) {
      console.error('Failed to delete listing:', err);
      alert('Failed to delete listing');
    }
  }

  async function handleMarkAsSold(listingId: string) {
    if (!confirm('Mark this item as sold? This will remove it from the marketplace.')) return;
    try {
      await listingsApi.updateListing(listingId, { status: 'SOLD' });
      setMyListings(prev => prev.map(l => 
        l.id === listingId ? { ...l, status: 'SOLD' } : l
      ));
    } catch (err) {
      console.error('Failed to mark listing as sold:', err);
      alert('Failed to mark listing as sold');
    }
  }

  function formatDate(dateString: string) {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="fixed inset-0 -z-10 bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-pink-500/10 dark:from-indigo-900/20 dark:via-purple-900/10 dark:to-pink-900/20"></div>
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <div className="fixed inset-0 -z-10 bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-pink-500/10 dark:from-indigo-900/20 dark:via-purple-900/10 dark:to-pink-900/20"></div>
      
      <header className="nav-glass px-6 py-4 sticky top-0 z-10">
        <div className="max-w-4xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={onBack}
              className="nav-button"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
              Back
            </button>
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl gradient-primary flex items-center justify-center shadow-lg">
                <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <h1 className="text-xl font-bold gradient-text">My Profile</h1>
            </div>
          </div>
          <button
            onClick={logout}
            className="nav-button hover:bg-red-500/20 hover:text-red-500 hover:border-red-500/30"
          >
            Logout
          </button>
        </div>
      </header>

      <div className="max-w-4xl mx-auto px-6 py-8">
        <div className="glass-card mb-6 overflow-hidden">
          <div className="p-6 border-b border-white/10">
            <div className="flex items-center gap-4">
              <div className="w-20 h-20 rounded-full gradient-primary flex items-center justify-center overflow-hidden shadow-lg">
                {profile?.avatarUrl ? (
                  <img src={profile.avatarUrl} alt="" className="w-full h-full object-cover" />
                ) : (
                  <span className="text-3xl text-white font-semibold">
                    {profile?.firstName?.charAt(0) || profile?.username?.charAt(0) || 'U'}
                  </span>
                )}
              </div>
              <div>
                <h2 className="text-2xl font-bold gradient-text">
                  {profile?.firstName} {profile?.lastName}
                </h2>
                <p className="text-muted">@{profile?.username}</p>
                <p className="text-sm text-muted">Member since {profile?.createdAt ? formatDate(profile.createdAt) : 'N/A'}</p>
              </div>
            </div>
          </div>

          <div className="flex border-b border-white/10">
            {(['profile', 'listings', 'favorites'] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`flex-1 px-4 py-3 text-center font-medium transition-all ${
                  activeTab === tab 
                    ? 'gradient-text border-b-2 border-indigo-500' 
                    : 'text-muted hover:text-foreground'
                }`}
              >
                {tab === 'profile' && 'Profile'}
                {tab === 'listings' && `My Listings (${myListings.length})`}
                {tab === 'favorites' && `Favorites (${favorites.length})`}
              </button>
            ))}
          </div>

          <div className="p-6">
            {activeTab === 'profile' && (
              <div>
                {isEditing ? (
                  <div className="space-y-4">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium mb-1">First Name</label>
                        <input
                          type="text"
                          value={editData.firstName}
                          onChange={(e) => setEditData({ ...editData, firstName: e.target.value })}
                          className="input-glass"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-1">Last Name</label>
                        <input
                          type="text"
                          value={editData.lastName}
                          onChange={(e) => setEditData({ ...editData, lastName: e.target.value })}
                          className="input-glass"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-1">Phone</label>
                      <input
                        type="tel"
                        value={editData.phone}
                        onChange={(e) => setEditData({ ...editData, phone: e.target.value })}
                        className="input-glass"
                      />
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium mb-1">Major</label>
                        <input
                          type="text"
                          value={editData.major}
                          onChange={(e) => setEditData({ ...editData, major: e.target.value })}
                          className="input-glass"
                        />
                      </div>
                      <div>
                        <label className="block text-sm font-medium mb-1">Graduation Year</label>
                        <input
                          type="number"
                          value={editData.graduationYear}
                          onChange={(e) => setEditData({ ...editData, graduationYear: e.target.value })}
                          className="input-glass"
                          min="2020"
                          max="2030"
                        />
                      </div>
                    </div>
                    <div className="flex gap-3 pt-4">
                      <button
                        onClick={handleSaveProfile}
                        disabled={saving}
                        className="nav-button-primary"
                      >
                        {saving ? 'Saving...' : 'Save Changes'}
                      </button>
                      <button
                        onClick={() => setIsEditing(false)}
                        className="nav-button"
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                ) : (
                  <div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                      <div className="glass-card p-4">
                        <p className="text-sm text-muted">Email</p>
                        <p className="font-medium break-all">{profile?.email}</p>
                      </div>
                      <div className="glass-card p-4">
                        <p className="text-sm text-muted">Phone</p>
                        <p className="font-medium">{profile?.phone || 'Not provided'}</p>
                      </div>
                      <div className="glass-card p-4">
                        <p className="text-sm text-muted">Major</p>
                        <p className="font-medium">{profile?.major || 'Not provided'}</p>
                      </div>
                      <div className="glass-card p-4">
                        <p className="text-sm text-muted">Graduation Year</p>
                        <p className="font-medium">{profile?.graduationYear || 'Not provided'}</p>
                      </div>
                    </div>
                    <button
                      onClick={() => setIsEditing(true)}
                      className="mt-6 nav-button-primary"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                      </svg>
                      Edit Profile
                    </button>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'listings' && (
              <div>
                {myListings.length === 0 ? (
                  <div className="text-center py-8 text-muted">
                    <svg className="w-16 h-16 mx-auto text-muted mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                    </svg>
                    <p className="font-medium">You have not posted any listings yet</p>
                    <button
                      onClick={onBack}
                      className="mt-4 nav-button-primary"
                    >
                      Create a Listing
                    </button>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {myListings.map((listing) => (
                      <div key={listing.id} className="glass-card flex gap-4 p-4 card-hover">
                        <div className="w-20 h-20 rounded-xl overflow-hidden flex-shrink-0">
                          <img src={listing.imageUrl} alt="" className="w-full h-full object-cover" />
                        </div>
                        <div className="flex-1">
                          <div className="flex justify-between">
                            <div>
                              <h3 className="font-medium">{listing.title}</h3>
                              <p className="text-lg font-bold gradient-text">${listing.price.toFixed(2)}</p>
                            </div>
                            <span className={`badge h-fit ${
                              listing.status === 'ACTIVE' 
                                ? 'badge-success' 
                                : listing.status === 'SOLD' 
                                  ? 'badge-primary' 
                                  : 'badge-warning'
                            }`}>
                              {listing.status}
                            </span>
                          </div>
                          <div className="flex items-center gap-4 mt-2 text-sm text-muted">
                            <span className="flex items-center gap-1">
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                              </svg>
                              {listing.viewCount || 0}
                            </span>
                            <span className="flex items-center gap-1">
                              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                              </svg>
                              {listing.favoriteCount || 0}
                            </span>
                            <span>{formatDate(listing.createdAt)}</span>
                          </div>
                          <div className="flex gap-2 mt-3">
                            {listing.status === 'ACTIVE' && (
                              <>
                                <button
                                  onClick={() => handleMarkAsSold(listing.id)}
                                  className="nav-button text-xs hover:bg-emerald-500/20 hover:text-emerald-500 hover:border-emerald-500/30"
                                >
                                  <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                  </svg>
                                  Mark as Sold
                                </button>
                                <button
                                  onClick={() => handleDeleteListing(listing.id)}
                                  className="nav-button text-xs hover:bg-red-500/20 hover:text-red-500 hover:border-red-500/30"
                                >
                                  <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                  </svg>
                                  Delete
                                </button>
                              </>
                            )}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {activeTab === 'favorites' && (
              <div>
                {favorites.length === 0 ? (
                  <div className="text-center py-8 text-muted">
                    <svg className="w-16 h-16 mx-auto text-muted mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                    </svg>
                    <p className="font-medium">You have not saved any items yet</p>
                    <button
                      onClick={onBack}
                      className="mt-4 nav-button-primary"
                    >
                      Browse Marketplace
                    </button>
                  </div>
                ) : (
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                    {favorites.map((item) => (
                      <div key={item.id} className="glass-card overflow-hidden card-hover group">
                        <div className="aspect-square relative overflow-hidden">
                          <img src={item.imageUrl} alt="" className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                          <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent"></div>
                        </div>
                        <div className="p-3">
                          <h3 className="font-medium truncate">{item.title}</h3>
                          <p className="text-lg font-bold gradient-text">${item.price?.toFixed(2)}</p>
                          <button
                            onClick={() => handleRemoveFavorite(item.id)}
                            className="mt-2 nav-button text-xs w-full justify-center hover:bg-red-500/20 hover:text-red-500 hover:border-red-500/30"
                          >
                            <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                            Remove
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
