import React, { useState, useEffect } from 'react';
import { getListings, createListing } from '../api/listings';
import { debounce } from '../utils/debounce';
import { useAuth } from '../context/AuthContext';

const categories = [
  { label: 'All Categories', value: 'all' },
  { label: 'Textbooks', value: 'TEXTBOOKS' },
  { label: 'Electronics', value: 'ELECTRONICS' },
  { label: 'Furniture', value: 'FURNITURE' },
  { label: 'Clothing', value: 'CLOTHING' },
  { label: 'Sports Equipment', value: 'SPORTS_EQUIPMENT' },
  { label: 'Services', value: 'SERVICES' },
  { label: 'Other', value: 'OTHER' },
];

const priceRanges = [
  { label: 'All Prices', value: 'all' },
  { label: 'Under $50', value: 'under50', min: 0, max: 50 },
  { label: '$50-$200', value: '50to200', min: 50, max: 200 },
  { label: 'Over $200', value: 'over200', min: 200, max: null },
];

export interface Listing {
  id: number;
  title: string;
  description: string;
  price: number;
  category: string;
  condition: string;
  imageUrl?: string;
  createdAt: string;
  seller: {
    id: number;
    name: string;
  };
}

export function MarketplacePage() {
  const { logout: authLogout, user } = useAuth()
  const [listings, setListings] = useState<Listing[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedPriceRange, setSelectedPriceRange] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newListing, setNewListing] = useState({
    title: '',
    description: '',
    price: 0,
    category: 'TEXTBOOKS',
    condition: 'NEW',
    location: '',
  });

  async function loadListings() {
    try {
      setLoading(true);
      const response = await getListings(page, 20);
      
      // Handle different response formats and ensure we always get an array
      let listingsArray: Listing[] = [];
      if (Array.isArray(response)) {
        // Response is directly an array
        listingsArray = response;
      } else if (Array.isArray(response.content)) {
        // Paginated response with content array
        listingsArray = response.content;
      } else if (Array.isArray(response.listings)) {
        // Response with listings array
        listingsArray = response.listings;
      } else {
        // Fallback to empty array if response format is unexpected
        console.warn('Unexpected listings response format:', response);
        listingsArray = [];
      }
      
      setListings(listingsArray);
      setTotalPages(response.totalPages || 1);
    } catch (err) {
      setError('Failed to load listings');
      console.error('Error loading listings:', err);
      setListings([]); // Ensure listings is always an array
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateListing() {
    try {
      const response = await createListing(newListing);
      alert('Listing created successfully!');
      setShowCreateModal(false);
      setNewListing({
        title: '',
        description: '',
        price: 0,
        category: 'TEXTBOOKS',
        condition: 'NEW',
        location: '',
      });
      // Reload listings to show the new one
      await loadListings();
    } catch (err: any) {
      alert(err?.response?.data?.message || err?.response?.data?.error || 'Failed to create listing');
    }
  }

  async function handleLogout() {
    try {
      await authLogout()
    } catch (err) {
      console.error('Logout failed:', err)
    }
  }

  useEffect(() => {
    loadListings();
  }, [page]);

  const filteredListings = listings.filter(listing => {
    // filter by category
    if (selectedCategory !== 'all' && listing.category !== selectedCategory) {
      return false;
    }

    // filter by price range
    if (selectedPriceRange !== 'all') {
      const range = priceRanges.find(r => r.value === selectedPriceRange);
      if (range) {
        if (typeof range.min === 'number' && listing.price < range.min) return false;
        if (typeof range.max === 'number' && listing.price > range.max) return false;
      }
    }

    // filter by search query (client-side fallback)
    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      const inTitle = listing.title?.toLowerCase().includes(q);
      const inDesc = listing.description?.toLowerCase().includes(q);
      if (!inTitle && !inDesc) return false;
    }

    return true;
  });

  return (
    <div className="min-h-screen bg-white">
      <header className="border-b px-8 py-4 flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold">CampusConnect</h1>
          <p className="text-sm text-gray-500">Campus Marketplace</p>
        </div>
        <div className="flex items-center gap-4">
          {user && (
            <div className="text-right">
              <p className="text-sm font-medium text-gray-900">
                Welcome, {user.firstName || user.username}!
              </p>
              <p className="text-xs text-gray-500">{user.email}</p>
            </div>
          )}
          <div className="flex gap-2">
            <button 
              onClick={() => setShowCreateModal(true)}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700"
            >
              + Create Listing
            </button>
            <button 
              onClick={handleLogout}
              className="bg-gray-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-gray-700"
            >
              Logout
            </button>
          </div>
        </div>
      </header>
      <main className="px-8 py-6">
        <h2 className="text-2xl font-bold mb-2">Campus Marketplace</h2>
        <p className="text-gray-600 mb-6">Browse {listings.length} listings from your fellow Spartans</p>
        <div className="flex gap-2 mb-4">
          {categories.map(cat => (
            <button
              key={cat.value}
              className={`px-4 py-2 rounded-full border flex items-center gap-2 font-medium ${selectedCategory === cat.value ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700'}`}
              onClick={() => setSelectedCategory(cat.value)}
            >
              {cat.label}
            </button>
          ))}
        </div>
        <div className="flex gap-2 mb-6">
          <input
            type="text"
            placeholder="Search for textbooks, electronics, furniture..."
            className="flex-1 px-4 py-2 rounded-lg border bg-gray-50"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
          <select
            className="px-4 py-2 rounded-lg border bg-gray-50"
            value={selectedPriceRange}
            onChange={e => setSelectedPriceRange(e.target.value)}
          >
            {priceRanges.map(pr => (
              <option key={pr.value} value={pr.value}>{pr.label}</option>
            ))}
          </select>
        </div>
        {loading ? (
          <div className="text-center py-8">Loading...</div>
        ) : error ? (
          <div className="text-red-500 text-center py-8">{error}</div>
        ) : (
          <>
            <p className="text-gray-500 mb-2">Showing {filteredListings.length} listings</p>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {filteredListings.length === 0 ? (
                <div className="col-span-3 text-center py-12">
                  <p className="text-gray-500 text-lg">No listings found</p>
                  <p className="text-gray-400 text-sm mt-2">Try adjusting your filters or create a new listing</p>
                </div>
              ) : (
                filteredListings.map(listing => (
                <div key={listing.id} className="bg-white rounded-xl shadow border p-4 hover:shadow-lg transition-shadow">
                  <div className="relative">
                    <img src={listing.imageUrl || '/placeholder.png'} alt={listing.title} className="rounded-lg w-full h-40 object-cover mb-2 bg-gray-200" />
                    <span className="absolute top-2 left-2 bg-blue-600 text-white text-xs px-2 py-1 rounded">{listing.category}</span>
                  </div>
                  <h3 className="font-semibold text-lg mb-1">{listing.title}</h3>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="bg-gray-200 text-xs px-2 py-1 rounded">{listing.condition}</span>
                    <span className="text-xs text-gray-400">
                      {listing.createdAt ? new Date(listing.createdAt).toLocaleDateString() : 
                       listing.date ? new Date(listing.date).toLocaleDateString() : 'Recently'}
                    </span>
                  </div>
                  <p className="text-gray-600 text-sm mb-2">{listing.description}</p>
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-green-600">${listing.price}</span>
                    <div className="flex gap-2">
                      <button className="bg-gray-100 px-3 py-1 rounded text-sm">Contact Seller</button>
                      <button className="bg-gray-100 px-3 py-1 rounded text-sm">Report</button>
                    </div>
                  </div>
                </div>
                ))
              )}
            </div>
            <div className="flex justify-center mt-8 gap-2">
              <button
                className="px-4 py-2 border rounded-lg disabled:opacity-50"
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Previous
              </button>
              <button
                className="px-4 py-2 border rounded-lg disabled:opacity-50"
                onClick={() => setPage(p => p + 1)}
                disabled={page >= totalPages - 1}
              >
                Next
              </button>
            </div>
          </>
        )}
      </main>

      {/* Create Listing Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <h2 className="text-2xl font-bold mb-4">Create New Listing</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">Title</label>
                <input
                  type="text"
                  className="w-full p-2 border rounded"
                  value={newListing.title}
                  onChange={(e) => setNewListing({ ...newListing, title: e.target.value })}
                  placeholder="e.g., iPhone 13 Pro"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Description</label>
                <textarea
                  className="w-full p-2 border rounded"
                  rows={4}
                  value={newListing.description}
                  onChange={(e) => setNewListing({ ...newListing, description: e.target.value })}
                  placeholder="Describe your item..."
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Category</label>
                  <select
                    className="w-full p-2 border rounded"
                    value={newListing.category}
                    onChange={(e) => setNewListing({ ...newListing, category: e.target.value })}
                  >
                    <option value="TEXTBOOKS">Textbooks</option>
                    <option value="ELECTRONICS">Electronics</option>
                    <option value="FURNITURE">Furniture</option>
                    <option value="CLOTHING">Clothing</option>
                    <option value="SPORTS_EQUIPMENT">Sports Equipment</option>
                    <option value="SERVICES">Services</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Condition</label>
                  <select
                    className="w-full p-2 border rounded"
                    value={newListing.condition}
                    onChange={(e) => setNewListing({ ...newListing, condition: e.target.value })}
                  >
                    <option value="NEW">New</option>
                    <option value="LIKE_NEW">Like New</option>
                    <option value="GOOD">Good</option>
                    <option value="FAIR">Fair</option>
                    <option value="POOR">Poor</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Price ($)</label>
                  <input
                    type="number"
                    className="w-full p-2 border rounded"
                    value={newListing.price}
                    onChange={(e) => setNewListing({ ...newListing, price: parseFloat(e.target.value) || 0 })}
                    min="0"
                    step="0.01"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Location</label>
                  <input
                    type="text"
                    className="w-full p-2 border rounded"
                    value={newListing.location}
                    onChange={(e) => setNewListing({ ...newListing, location: e.target.value })}
                    placeholder="e.g., Campus Center"
                  />
                </div>
              </div>
              <div className="flex gap-2 pt-4">
                <button
                  onClick={handleCreateListing}
                  className="flex-1 bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700"
                >
                  Create Listing
                </button>
                <button
                  onClick={() => setShowCreateModal(false)}
                  className="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-lg font-medium hover:bg-gray-300"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );

}
