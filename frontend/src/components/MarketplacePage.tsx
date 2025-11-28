import React, { useState, useEffect } from 'react';
import { listingsApi, discoveryApi, searchApi } from '../api';
import { useAuth } from '../context/AuthContext';
import { ListingDetailsModal } from './ListingDetailsModal';
import { MessagesPage } from './MessagesPage';
import { UserProfilePage } from './UserProfilePage';
import { ThemeToggle } from './ThemeToggle';

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

const conditions = [
  { label: 'All Conditions', value: 'all' },
  { label: 'New', value: 'NEW' },
  { label: 'Like New', value: 'LIKE_NEW' },
  { label: 'Good', value: 'GOOD' },
  { label: 'Fair', value: 'FAIR' },
  { label: 'Poor', value: 'POOR' },
];

const priceRanges = [
  { label: 'All Prices', value: 'all' },
  { label: 'Under $25', value: 'under25', min: 0, max: 25 },
  { label: '$25-$100', value: '25to100', min: 25, max: 100 },
  { label: '$100-$500', value: '100to500', min: 100, max: 500 },
  { label: 'Over $500', value: 'over500', min: 500, max: null },
];

export interface Listing {
  id: string;
  title: string;
  description: string;
  price: number;
  category: string;
  condition: string;
  location?: string;
  imageUrl?: string;
  createdAt: string;
  seller: {
    id: string;
    name: string;
    username?: string;
    avatarUrl?: string;
  };
  viewCount?: number;
  favoriteCount?: number;
  favorite?: boolean;
  negotiable?: boolean;
}

type ViewMode = 'marketplace' | 'messages' | 'profile';

export function MarketplacePage() {
  const { logout: authLogout, user } = useAuth();
  const [viewMode, setViewMode] = useState<ViewMode>('marketplace');
  const [listings, setListings] = useState<Listing[]>([]);
  const [trendingListings, setTrendingListings] = useState<Listing[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedCondition, setSelectedCondition] = useState('all');
  const [selectedPriceRange, setSelectedPriceRange] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedListingId, setSelectedListingId] = useState<string | null>(null);
  const [searchSuggestions, setSearchSuggestions] = useState<string[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [newListing, setNewListing] = useState({
    title: '',
    description: '',
    price: 0,
    category: 'TEXTBOOKS',
    condition: 'NEW',
    location: '',
    negotiable: false,
  });

  useEffect(() => {
    if (viewMode === 'marketplace') {
      loadListings();
      loadTrending();
    }
  }, [page, viewMode]);

  useEffect(() => {
    if (searchQuery.length >= 2) {
      loadSearchSuggestions(searchQuery);
    } else {
      setSearchSuggestions([]);
    }
  }, [searchQuery]);

  async function loadListings() {
    try {
      setLoading(true);
      const response = await listingsApi.getListings(page, 20);
      
      let listingsArray: Listing[] = [];
      if (Array.isArray(response)) {
        listingsArray = response;
      } else if (Array.isArray(response.content)) {
        listingsArray = response.content;
      } else if (Array.isArray(response.listings)) {
        listingsArray = response.listings;
      } else {
        console.warn('Unexpected listings response format:', response);
        listingsArray = [];
      }
      
      setListings(listingsArray);
      setTotalPages(response.totalPages || 1);
    } catch (err) {
      setError('Failed to load listings');
      console.error('Error loading listings:', err);
      setListings([]);
    } finally {
      setLoading(false);
    }
  }

  async function loadTrending() {
    try {
      const response = await discoveryApi.getTrending(4);
      // Backend returns { trending: [...] }
      setTrendingListings(response.trending || response.products || []);
    } catch (err) {
      console.error('Failed to load trending:', err);
    }
  }

  async function loadSearchSuggestions(query: string) {
    try {
      const response = await searchApi.autocomplete(query);
      setSearchSuggestions(response.suggestions || []);
    } catch (err) {
      console.error('Failed to load suggestions:', err);
    }
  }

  async function handleSearch() {
    if (!searchQuery.trim()) {
      loadListings();
      return;
    }
    try {
      setLoading(true);
      const priceRange = priceRanges.find(r => r.value === selectedPriceRange);
      const response = await listingsApi.searchListings({
        keyword: searchQuery,
        category: selectedCategory !== 'all' ? selectedCategory : undefined,
        condition: selectedCondition !== 'all' ? selectedCondition : undefined,
        minPrice: priceRange?.min,
        maxPrice: priceRange?.max || undefined,
        page,
        size: 20,
      });
      let listingsArray: Listing[] = [];
      if (Array.isArray(response.content)) {
        listingsArray = response.content;
      } else if (Array.isArray(response)) {
        listingsArray = response;
      }
      setListings(listingsArray);
      setTotalPages(response.totalPages || 1);
    } catch (err) {
      console.error('Search failed:', err);
      setError('Search failed');
    } finally {
      setLoading(false);
      setShowSuggestions(false);
    }
  }

  async function handleCreateListing() {
    try {
      await listingsApi.createListing(newListing);
      alert('Listing created successfully!');
      setShowCreateModal(false);
      setNewListing({
        title: '',
        description: '',
        price: 0,
        category: 'TEXTBOOKS',
        condition: 'NEW',
        location: '',
        negotiable: false,
      });
      await loadListings();
    } catch (err: any) {
      alert(err?.response?.data?.message || err?.response?.data?.error || 'Failed to create listing');
    }
  }

  async function handleToggleFavorite(listingId: string, e: React.MouseEvent) {
    e.stopPropagation();
    try {
      const result = await listingsApi.toggleFavorite(listingId);
      setListings(prev =>
        prev.map(l =>
          l.id === listingId ? { ...l, favorite: result.favorited } : l
        )
      );
    } catch (err) {
      console.error('Failed to toggle favorite:', err);
    }
  }

  async function handleLogout() {
    try {
      await authLogout();
    } catch (err) {
      console.error('Logout failed:', err);
    }
  }

  const filteredListings = listings.filter(listing => {
    if (selectedCategory !== 'all' && listing.category !== selectedCategory) {
      return false;
    }
    if (selectedCondition !== 'all' && listing.condition !== selectedCondition) {
      return false;
    }
    if (selectedPriceRange !== 'all') {
      const range = priceRanges.find(r => r.value === selectedPriceRange);
      if (range) {
        if (typeof range.min === 'number' && listing.price < range.min) return false;
        if (typeof range.max === 'number' && listing.price > range.max) return false;
      }
    }
    if (searchQuery && !loading) {
      const q = searchQuery.toLowerCase();
      const inTitle = listing.title?.toLowerCase().includes(q);
      const inDesc = listing.description?.toLowerCase().includes(q);
      if (!inTitle && !inDesc) return false;
    }
    return true;
  });

  if (viewMode === 'messages') {
    return <MessagesPage onBack={() => setViewMode('marketplace')} />;
  }

  if (viewMode === 'profile') {
    return <UserProfilePage onBack={() => setViewMode('marketplace')} />;
  }

  return (
    <div className="min-h-screen">
      <div className="fixed inset-0 -z-10 bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-pink-500/10 dark:from-indigo-900/20 dark:via-purple-900/10 dark:to-pink-900/20"></div>
      <header className="nav-glass px-4 sm:px-8 py-4 sticky top-0 z-20">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2 sm:gap-4">
            <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-xl gradient-primary flex items-center justify-center shadow-lg">
              <svg className="w-4 h-4 sm:w-5 sm:h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
              </svg>
            </div>
            <div className="hidden sm:block">
              <h1 className="text-xl font-bold gradient-text">CampusConnect</h1>
              <p className="text-sm text-muted">Campus Marketplace</p>
            </div>
          </div>
          <div className="flex items-center gap-1 sm:gap-3">
            <button
              onClick={() => setViewMode('messages')}
              className="nav-button !px-2 sm:!px-4"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
              <span className="hidden sm:inline">Messages</span>
            </button>
            <button
              onClick={() => setViewMode('profile')}
              className="nav-button !px-2 sm:!px-4"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              <span className="hidden sm:inline">Profile</span>
            </button>
            {user && (
              <div className="hidden lg:flex items-center gap-3 glass-card px-3 py-1.5">
                <div className="w-7 h-7 rounded-full gradient-primary flex items-center justify-center text-white text-xs font-bold">
                  {(user.firstName || user.username)?.charAt(0).toUpperCase()}
                </div>
                <div className="text-right">
                  <p className="text-sm font-medium">{user.firstName || user.username}</p>
                  <p className="text-xs text-muted">{user.roles?.join(', ') || 'Student'}</p>
                </div>
              </div>
            )}
            <button 
              onClick={() => setShowCreateModal(true)}
              className="nav-button-primary !px-2 sm:!px-4"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              <span className="hidden sm:inline">Sell Item</span>
            </button>
            <ThemeToggle />
            <button 
              onClick={handleLogout}
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

      <main className="max-w-7xl mx-auto px-4 sm:px-8 py-6">
        {trendingListings.length > 0 && !searchQuery && (
          <div className="mb-8">
            <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
              <svg className="w-5 h-5 text-orange-500" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M12.395 2.553a1 1 0 00-1.45-.385c-.345.23-.614.558-.822.88-.214.33-.403.713-.57 1.116-.334.804-.614 1.768-.84 2.734a31.365 31.365 0 00-.613 3.58 2.64 2.64 0 01-.945-1.067c-.328-.68-.398-1.534-.398-2.654A1 1 0 005.05 6.05 6.981 6.981 0 003 11a7 7 0 1011.95-4.95c-.592-.591-.98-.985-1.348-1.467-.363-.476-.724-1.063-1.207-2.03zM12.12 15.12A3 3 0 017 13s.879.5 2.5.5c0-1 .5-4 1.25-4.5.5 1 .786 1.293 1.371 1.879A2.99 2.99 0 0113 13a2.99 2.99 0 01-.879 2.121z" clipRule="evenodd" />
              </svg>
              Trending Now
            </h2>
            <div className="flex gap-4 overflow-x-auto pb-2 scrollbar-thin">
              {trendingListings.map(item => (
                <button
                  key={item.id}
                  onClick={() => setSelectedListingId(item.id)}
                  className="flex-shrink-0 w-56 glass-card overflow-hidden card-hover text-left group"
                >
                  <div className="h-40 bg-gray-100 dark:bg-gray-800 relative overflow-hidden">
                    <img src={item.imageUrl} alt="" className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
                  </div>
                  <div className="p-4">
                    <p className="font-semibold truncate">{item.title}</p>
                    <div className="flex flex-wrap items-center gap-1.5 mt-2">
                      <span className="badge badge-primary text-xs">
                        {item.category?.replace(/_/g, ' ')}
                      </span>
                      <span className="badge badge-success text-xs">
                        {item.condition?.replace(/_/g, ' ')}
                      </span>
                      {item.negotiable && (
                        <span className="badge badge-warning text-xs">Negotiable</span>
                      )}
                    </div>
                    <p className="text-lg font-bold gradient-text mt-3">${item.price?.toFixed(2)}</p>
                  </div>
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="glass-card p-6 mb-6">
          <div className="flex flex-wrap gap-4 mb-4">
            <div className="relative flex-1 min-w-[200px]">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-muted">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </span>
              <input
                type="text"
                placeholder="Search for textbooks, electronics, furniture..."
                className="input-glass pl-12"
                value={searchQuery}
                onChange={e => {
                  setSearchQuery(e.target.value);
                  setShowSuggestions(true);
                }}
                onKeyDown={e => e.key === 'Enter' && handleSearch()}
                onFocus={() => setShowSuggestions(true)}
              />
              {showSuggestions && searchSuggestions.length > 0 && (
                <div className="absolute top-full left-0 right-0 glass-card mt-2 z-10 overflow-hidden">
                  {searchSuggestions.map((suggestion, index) => (
                    <button
                      key={index}
                      onClick={() => {
                        setSearchQuery(suggestion);
                        setShowSuggestions(false);
                        handleSearch();
                      }}
                      className="w-full text-left px-4 py-3 hover:bg-white/50 dark:hover:bg-white/10 transition-colors"
                    >
                      {suggestion}
                    </button>
                  ))}
                </div>
              )}
            </div>
            <button
              onClick={handleSearch}
              className="btn-primary flex items-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              Search
            </button>
          </div>

          <div className="flex flex-wrap gap-3 items-center">
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted pointer-events-none">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
                </svg>
              </span>
              <select
                className="filter-select pl-9"
                value={selectedCategory}
                onChange={e => setSelectedCategory(e.target.value)}
              >
                {categories.map(cat => (
                  <option key={cat.value} value={cat.value}>{cat.label}</option>
                ))}
              </select>
            </div>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted pointer-events-none">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </span>
              <select
                className="filter-select pl-9"
                value={selectedCondition}
                onChange={e => setSelectedCondition(e.target.value)}
              >
                {conditions.map(cond => (
                  <option key={cond.value} value={cond.value}>{cond.label}</option>
                ))}
              </select>
            </div>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted pointer-events-none">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </span>
              <select
                className="filter-select pl-9"
                value={selectedPriceRange}
                onChange={e => setSelectedPriceRange(e.target.value)}
              >
                {priceRanges.map(pr => (
                  <option key={pr.value} value={pr.value}>{pr.label}</option>
                ))}
              </select>
            </div>
            {(selectedCategory !== 'all' || selectedCondition !== 'all' || selectedPriceRange !== 'all' || searchQuery) && (
              <button
                onClick={() => {
                  setSelectedCategory('all');
                  setSelectedCondition('all');
                  setSelectedPriceRange('all');
                  setSearchQuery('');
                  loadListings();
                }}
                className="nav-button hover:bg-red-500/20 hover:text-red-500 hover:border-red-500/30"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
                Clear
              </button>
            )}
          </div>
        </div>

        {loading ? (
          <div className="text-center py-12">
            <div className="relative w-16 h-16 mx-auto mb-4">
              <div className="absolute inset-0 rounded-full border-4 border-transparent border-t-[hsl(var(--primary))] animate-spin"></div>
              <div className="absolute inset-2 rounded-full border-4 border-transparent border-t-[hsl(var(--secondary))] animate-spin" style={{ animationDirection: 'reverse', animationDuration: '1.5s' }}></div>
            </div>
            <p className="text-muted">Loading listings...</p>
          </div>
        ) : error ? (
          <div className="glass-card p-8 text-center">
            <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-red-500/20 flex items-center justify-center">
              <svg className="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <p className="text-red-500 font-medium">{error}</p>
          </div>
        ) : (
          <>
            <p className="text-muted mb-4 flex items-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
              {filteredListings.length} items found
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {filteredListings.length === 0 ? (
                <div className="col-span-full glass-card p-12 text-center">
                  <div className="w-20 h-20 mx-auto mb-4 rounded-full bg-gray-500/20 flex items-center justify-center">
                    <svg className="w-10 h-10 text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                    </svg>
                  </div>
                  <p className="text-lg font-medium">No listings found</p>
                  <p className="text-muted mt-2">Try adjusting your filters or create a new listing</p>
                </div>
              ) : (
                filteredListings.map(listing => (
                  <div
                    key={listing.id}
                    onClick={() => setSelectedListingId(listing.id)}
                    className="glass-card overflow-hidden card-hover cursor-pointer group"
                  >
                    <div className="relative aspect-square bg-gray-100 dark:bg-gray-800">
                      <img
                        src={listing.imageUrl || 'https://via.placeholder.com/300'}
                        alt={listing.title}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
                      <button
                        onClick={(e) => handleToggleFavorite(listing.id, e)}
                        className={`absolute top-3 right-3 w-9 h-9 rounded-full flex items-center justify-center transition-all ${listing.favorite ? 'bg-red-500 text-white scale-110' : 'glass-button'}`}
                      >
                        <svg className="w-5 h-5" fill={listing.favorite ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                        </svg>
                      </button>
                    </div>
                    <div className="p-4">
                      <h3 className="font-semibold truncate">{listing.title}</h3>
                      <div className="flex flex-wrap items-center gap-2 mt-2">
                        <span className="badge badge-primary text-xs">
                          {listing.category?.replace(/_/g, ' ')}
                        </span>
                        <span className="badge badge-success text-xs">
                          {listing.condition?.replace(/_/g, ' ')}
                        </span>
                        {listing.negotiable && (
                          <span className="badge badge-warning text-xs">Negotiable</span>
                        )}
                      </div>
                      <p className="text-muted text-sm mt-2 line-clamp-2">{listing.description}</p>
                      <div className="flex items-center justify-between mt-4">
                        <span className="text-xl font-bold gradient-text">${listing.price?.toFixed(2)}</span>
                        <span className="text-xs text-muted flex items-center gap-1">
                          <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                          </svg>
                          {listing.viewCount || 0}
                        </span>
                      </div>
                      {listing.seller && (
                        <div className="flex items-center gap-2 mt-4 pt-4 border-t border-glass">
                          <div className="w-7 h-7 rounded-full gradient-primary flex items-center justify-center overflow-hidden text-white text-xs font-bold">
                            {listing.seller.avatarUrl ? (
                              <img src={listing.seller.avatarUrl} alt="" className="w-full h-full object-cover" />
                            ) : (
                              listing.seller.name?.charAt(0) || listing.seller.username?.charAt(0) || 'S'
                            )}
                          </div>
                          <span className="text-sm text-muted truncate">
                            {listing.seller.name || listing.seller.username}
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>

            {totalPages > 1 && (
              <div className="flex justify-center mt-8 gap-2">
                <button
                  className="px-4 py-2 border border-gray-200 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-300 disabled:opacity-50 hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  disabled={page === 0}
                >
                  Previous
                </button>
                <span className="px-4 py-2 text-gray-600 dark:text-gray-400 flex items-center">
                  Page {page + 1} of {totalPages}
                </span>
                <button
                  className="px-4 py-2 border border-gray-200 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-800 text-gray-700 dark:text-gray-300 disabled:opacity-50 hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
                  onClick={() => setPage(p => p + 1)}
                  disabled={page >= totalPages - 1}
                >
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </main>

      {showCreateModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-slate-800 rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700 px-6 py-4 flex items-center justify-between z-10">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl gradient-primary flex items-center justify-center">
                  <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                </div>
                <h2 className="text-xl font-bold text-gray-900 dark:text-white">Create New Listing</h2>
              </div>
              <button 
                onClick={() => setShowCreateModal(false)} 
                className="text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 text-2xl transition-colors"
              >
                ×
              </button>
            </div>
            <div className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Title *</label>
                <input
                  type="text"
                  className="w-full p-3 border border-gray-200 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-colors"
                  value={newListing.title}
                  onChange={(e) => setNewListing({ ...newListing, title: e.target.value })}
                  placeholder="e.g., iPhone 13 Pro - Great Condition"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Description *</label>
                <textarea
                  className="w-full p-3 border border-gray-200 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-colors resize-none"
                  rows={4}
                  value={newListing.description}
                  onChange={(e) => setNewListing({ ...newListing, description: e.target.value })}
                  placeholder="Describe your item in detail..."
                />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Category *</label>
                  <div className="relative">
                    <select
                      className="appearance-none w-full p-3 pr-10 border border-gray-200 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50 cursor-pointer transition-colors"
                      value={newListing.category}
                      onChange={(e) => setNewListing({ ...newListing, category: e.target.value })}
                    >
                      {categories.slice(1).map(cat => (
                        <option key={cat.value} value={cat.value}>{cat.label}</option>
                      ))}
                    </select>
                    <svg className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 dark:text-gray-400 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Condition *</label>
                  <div className="relative">
                    <select
                      className="appearance-none w-full p-3 pr-10 border border-gray-200 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500/50 cursor-pointer transition-colors"
                      value={newListing.condition}
                      onChange={(e) => setNewListing({ ...newListing, condition: e.target.value })}
                    >
                      {conditions.slice(1).map(cond => (
                        <option key={cond.value} value={cond.value}>{cond.label}</option>
                      ))}
                    </select>
                    <svg className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500 dark:text-gray-400 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </div>
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Price ($) *</label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 dark:text-gray-400">$</span>
                    <input
                      type="number"
                      className="w-full p-3 pl-7 border border-gray-200 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-colors"
                      value={newListing.price || ''}
                      onChange={(e) => setNewListing({ ...newListing, price: parseFloat(e.target.value) || 0 })}
                      min="0"
                      step="0.01"
                      placeholder="0.00"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Location</label>
                  <input
                    type="text"
                    className="w-full p-3 border border-gray-200 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/50 transition-colors"
                    value={newListing.location}
                    onChange={(e) => setNewListing({ ...newListing, location: e.target.value })}
                    placeholder="e.g., SJSU Library"
                  />
                </div>
              </div>
              <div className="flex items-center gap-3 p-4 bg-gray-50 dark:bg-slate-700/50 rounded-xl">
                <input
                  type="checkbox"
                  id="negotiable"
                  checked={newListing.negotiable}
                  onChange={(e) => setNewListing({ ...newListing, negotiable: e.target.checked })}
                  className="w-5 h-5 rounded border-gray-300 dark:border-slate-600 text-indigo-600 focus:ring-indigo-500 dark:bg-slate-700"
                />
                <label htmlFor="negotiable" className="text-sm text-gray-700 dark:text-gray-300">Price is negotiable</label>
              </div>
              <div className="flex flex-col sm:flex-row justify-end gap-3 pt-4 border-t border-gray-200 dark:border-slate-700">
                <button
                  onClick={() => setShowCreateModal(false)}
                  className="px-6 py-2.5 border border-gray-200 dark:border-slate-600 rounded-xl text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors font-medium"
                >
                  Cancel
                </button>
                <button
                  onClick={handleCreateListing}
                  disabled={!newListing.title || !newListing.description || !newListing.price}
                  className="px-6 py-2.5 gradient-primary text-white rounded-xl hover:opacity-90 disabled:opacity-50 transition-all font-medium shadow-lg shadow-indigo-500/30"
                >
                  Create Listing
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {selectedListingId && (
        <ListingDetailsModal
          listingId={selectedListingId}
          onClose={() => setSelectedListingId(null)}
          onViewListing={(id) => setSelectedListingId(id)}
        />
      )}
    </div>
  );
}
