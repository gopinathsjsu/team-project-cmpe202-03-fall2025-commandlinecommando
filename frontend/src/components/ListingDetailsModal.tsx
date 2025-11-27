import React, { useState, useEffect } from 'react';
import { listingsApi, chatApi, discoveryApi } from '../api';
import { useAuth } from '../context/AuthContext';

interface Listing {
  id: string;
  title: string;
  description: string;
  category: string;
  condition: string;
  price: number;
  location: string;
  sellerId: string;
  seller: {
    id: string;
    name: string;
    username: string;
    avatarUrl?: string;
  };
  imageUrl: string;
  images?: Array<{
    imageId: number;
    imageUrl: string;
    altText?: string;
    displayOrder: number;
  }>;
  status: string;
  viewCount?: number;
  favoriteCount?: number;
  negotiable?: boolean;
  favorite?: boolean;
  createdAt: string;
  updatedAt: string;
}

interface SimilarListing {
  id: string;
  title: string;
  price: number;
  imageUrl: string;
  condition: string;
  similarity?: number;
}

interface Props {
  listingId: string;
  onClose: () => void;
  onViewListing?: (id: string) => void;
}

export function ListingDetailsModal({ listingId, onClose, onViewListing }: Props) {
  const { user, isAuthenticated } = useAuth();
  const [listing, setListing] = useState<Listing | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [isFavorite, setIsFavorite] = useState(false);
  const [showMessageInput, setShowMessageInput] = useState(false);
  const [message, setMessage] = useState('');
  const [sendingMessage, setSendingMessage] = useState(false);
  const [similarListings, setSimilarListings] = useState<SimilarListing[]>([]);
  const [showReportModal, setShowReportModal] = useState(false);
  const [reportType, setReportType] = useState('SPAM');
  const [reportDescription, setReportDescription] = useState('');

  useEffect(() => {
    loadListing();
    loadSimilarListings();
  }, [listingId]);

  async function loadListing() {
    try {
      setLoading(true);
      const data = await listingsApi.getListing(listingId);
      setListing(data);
      setIsFavorite(data.favorite || false);
    } catch (err) {
      setError('Failed to load listing details');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }

  async function loadSimilarListings() {
    try {
      const response = await discoveryApi.getSimilar(listingId, 4);
      setSimilarListings(response.similar || []);
    } catch (err) {
      console.error('Failed to load similar listings:', err);
    }
  }

  async function handleToggleFavorite() {
    if (!isAuthenticated) {
      alert('Please log in to save items to your favorites');
      return;
    }
    try {
      const result = await listingsApi.toggleFavorite(listingId);
      setIsFavorite(result.favorited);
    } catch (err) {
      console.error('Failed to toggle favorite:', err);
    }
  }

  async function handleSendMessage() {
    if (!message.trim()) return;
    if (!isAuthenticated) {
      alert('Please log in to send messages');
      return;
    }

    try {
      setSendingMessage(true);
      await chatApi.createConversation({
        listingId,
        initialMessage: message,
      });
      alert('Message sent successfully!');
      setMessage('');
      setShowMessageInput(false);
    } catch (err) {
      console.error('Failed to send message:', err);
      alert('Failed to send message');
    } finally {
      setSendingMessage(false);
    }
  }

  async function handleReport() {
    if (!isAuthenticated) {
      alert('Please log in to report listings');
      return;
    }
    if (!reportDescription.trim()) {
      alert('Please provide a description for your report');
      return;
    }

    try {
      await listingsApi.reportListing(listingId, {
        reportType,
        description: reportDescription,
      });
      alert('Report submitted successfully');
      setShowReportModal(false);
      setReportDescription('');
    } catch (err) {
      console.error('Failed to submit report:', err);
      alert('Failed to submit report');
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatCondition = (condition: string) => {
    return condition.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  };

  const getConditionColor = (condition: string) => {
    switch (condition) {
      case 'NEW': return 'bg-green-100 text-green-800 dark:bg-green-500/20 dark:text-green-400';
      case 'LIKE_NEW': return 'bg-blue-100 text-blue-800 dark:bg-blue-500/20 dark:text-blue-400';
      case 'GOOD': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-500/20 dark:text-yellow-400';
      case 'FAIR': return 'bg-orange-100 text-orange-800 dark:bg-orange-500/20 dark:text-orange-400';
      case 'POOR': return 'bg-red-100 text-red-800 dark:bg-red-500/20 dark:text-red-400';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-500/20 dark:text-gray-400';
    }
  };

  if (loading) {
    return (
      <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
        <div className="bg-white dark:bg-slate-800 rounded-2xl p-8 shadow-2xl">
          <div className="animate-spin rounded-full h-12 w-12 border-4 border-indigo-500/30 border-t-indigo-500 mx-auto"></div>
          <p className="text-center mt-4 text-gray-600 dark:text-gray-300">Loading...</p>
        </div>
      </div>
    );
  }

  if (error || !listing) {
    return (
      <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50">
        <div className="bg-white dark:bg-slate-800 rounded-2xl p-8 max-w-md shadow-2xl">
          <p className="text-red-600 dark:text-red-400 text-center">{error || 'Listing not found'}</p>
          <button
            onClick={onClose}
            className="mt-4 w-full px-4 py-2 bg-gray-200 dark:bg-slate-700 text-gray-800 dark:text-gray-200 rounded-xl hover:bg-gray-300 dark:hover:bg-slate-600 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    );
  }

  const images = listing.images?.length ? listing.images : [{ imageId: 0, imageUrl: listing.imageUrl, altText: listing.title, displayOrder: 1 }];
  const isOwner = user?.id === listing.sellerId || user?.username === listing.seller.username;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4 overflow-y-auto">
      <div className="bg-white dark:bg-slate-800 rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
        <div className="sticky top-0 bg-white dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700 px-6 py-4 flex items-center justify-between z-10">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white truncate pr-4">{listing.title}</h2>
          <button
            onClick={onClose}
            className="text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 text-2xl transition-colors"
          >
            ×
          </button>
        </div>

        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <div className="relative aspect-square rounded-xl overflow-hidden bg-gray-100 dark:bg-slate-700">
                <img
                  src={images[currentImageIndex]?.imageUrl}
                  alt={images[currentImageIndex]?.altText || listing.title}
                  className="w-full h-full object-cover"
                />
                {images.length > 1 && (
                  <>
                    <button
                      onClick={() => setCurrentImageIndex((prev) => (prev - 1 + images.length) % images.length)}
                      className="absolute left-2 top-1/2 -translate-y-1/2 bg-white/90 dark:bg-slate-800/90 rounded-full p-2 hover:bg-white dark:hover:bg-slate-700 transition-colors shadow-lg"
                    >
                      <svg className="w-5 h-5 text-gray-700 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                      </svg>
                    </button>
                    <button
                      onClick={() => setCurrentImageIndex((prev) => (prev + 1) % images.length)}
                      className="absolute right-2 top-1/2 -translate-y-1/2 bg-white/90 dark:bg-slate-800/90 rounded-full p-2 hover:bg-white dark:hover:bg-slate-700 transition-colors shadow-lg"
                    >
                      <svg className="w-5 h-5 text-gray-700 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                      </svg>
                    </button>
                    <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-2">
                      {images.map((_, index) => (
                        <button
                          key={index}
                          onClick={() => setCurrentImageIndex(index)}
                          className={`w-2.5 h-2.5 rounded-full transition-colors ${index === currentImageIndex ? 'bg-indigo-500' : 'bg-white/60 dark:bg-slate-400/60'}`}
                        />
                      ))}
                    </div>
                  </>
                )}
              </div>

              {images.length > 1 && (
                <div className="flex gap-2 mt-4 overflow-x-auto pb-2">
                  {images.map((img, index) => (
                    <button
                      key={img.imageId}
                      onClick={() => setCurrentImageIndex(index)}
                      className={`flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 transition-colors ${index === currentImageIndex ? 'border-indigo-500' : 'border-transparent hover:border-gray-300 dark:hover:border-slate-600'}`}
                    >
                      <img src={img.imageUrl} alt="" className="w-full h-full object-cover" />
                    </button>
                  ))}
                </div>
              )}
            </div>

            <div>
              <div className="flex items-center justify-between mb-4">
                <span className="text-3xl font-bold text-indigo-600 dark:text-indigo-400">${listing.price.toFixed(2)}</span>
                <button
                  onClick={handleToggleFavorite}
                  className={`p-2.5 rounded-full transition-colors ${isFavorite ? 'bg-red-100 dark:bg-red-500/20 text-red-600 dark:text-red-400' : 'bg-gray-100 dark:bg-slate-700 text-gray-600 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-slate-600'}`}
                >
                  <svg className="w-6 h-6" fill={isFavorite ? "currentColor" : "none"} stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                  </svg>
                </button>
              </div>

              <div className="flex flex-wrap gap-2 mb-4">
                <span className={`px-3 py-1 rounded-full text-sm font-medium ${getConditionColor(listing.condition)}`}>
                  {formatCondition(listing.condition)}
                </span>
                <span className="px-3 py-1 bg-gray-100 dark:bg-slate-700 text-gray-700 dark:text-gray-300 rounded-full text-sm">
                  {listing.category.replace(/_/g, ' ')}
                </span>
                {listing.negotiable && (
                  <span className="px-3 py-1 bg-purple-100 dark:bg-purple-500/20 text-purple-700 dark:text-purple-400 rounded-full text-sm">
                    Negotiable
                  </span>
                )}
              </div>

              <div className="mb-4">
                <h3 className="font-semibold text-gray-900 dark:text-white mb-2">Description</h3>
                <p className="text-gray-700 dark:text-gray-300 whitespace-pre-line">{listing.description}</p>
              </div>

              <div className="mb-4">
                <h3 className="font-semibold text-gray-900 dark:text-white mb-2">Location</h3>
                <p className="text-gray-700 dark:text-gray-300">{listing.location}</p>
              </div>

              <div className="mb-4">
                <h3 className="font-semibold text-gray-900 dark:text-white mb-2">Seller</h3>
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-indigo-100 dark:bg-indigo-500/20 flex items-center justify-center overflow-hidden">
                    {listing.seller.avatarUrl ? (
                      <img src={listing.seller.avatarUrl} alt="" className="w-full h-full object-cover" />
                    ) : (
                      <span className="text-indigo-600 dark:text-indigo-400 font-semibold">
                        {listing.seller.name?.charAt(0) || listing.seller.username.charAt(0)}
                      </span>
                    )}
                  </div>
                  <div>
                    <p className="font-medium text-gray-900 dark:text-white">{listing.seller.name}</p>
                    <p className="text-sm text-gray-500 dark:text-gray-400">@{listing.seller.username}</p>
                  </div>
                </div>
              </div>

              <div className="text-sm text-gray-500 dark:text-gray-400 mb-6">
                <p>Posted: {formatDate(listing.createdAt)}</p>
                {listing.viewCount !== undefined && (
                  <p>{listing.viewCount} views | {listing.favoriteCount || 0} favorites</p>
                )}
              </div>

              {!isOwner && (
                <div className="space-y-3">
                  {showMessageInput ? (
                    <div className="space-y-2">
                      <textarea
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        placeholder="Hi, I'm interested in this item..."
                        className="w-full p-3 border border-gray-300 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                        rows={3}
                      />
                      <div className="flex gap-2">
                        <button
                          onClick={handleSendMessage}
                          disabled={sendingMessage || !message.trim()}
                          className="flex-1 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-medium disabled:opacity-50 transition-colors"
                        >
                          {sendingMessage ? 'Sending...' : 'Send Message'}
                        </button>
                        <button
                          onClick={() => setShowMessageInput(false)}
                          className="px-4 py-2.5 bg-gray-200 dark:bg-slate-700 text-gray-800 dark:text-gray-200 rounded-xl hover:bg-gray-300 dark:hover:bg-slate-600 transition-colors"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <button
                      onClick={() => setShowMessageInput(true)}
                      className="w-full px-4 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-medium transition-colors"
                    >
                      Contact Seller
                    </button>
                  )}
                  <button
                    onClick={() => setShowReportModal(true)}
                    className="w-full px-4 py-2 text-gray-600 dark:text-gray-400 hover:text-red-600 dark:hover:text-red-400 text-sm transition-colors"
                  >
                    Report this listing
                  </button>
                </div>
              )}

              {isOwner && (
                <div className="p-4 bg-indigo-50 dark:bg-indigo-500/10 rounded-xl border border-indigo-100 dark:border-indigo-500/20">
                  <p className="text-indigo-800 dark:text-indigo-300 font-medium">This is your listing</p>
                  <p className="text-sm text-indigo-600 dark:text-indigo-400 mt-1">You can edit or delete it from your dashboard</p>
                </div>
              )}
            </div>
          </div>

          {similarListings.length > 0 && (
            <div className="mt-8 pt-6 border-t border-gray-200 dark:border-slate-700">
              <h3 className="font-semibold text-lg text-gray-900 dark:text-white mb-4">Similar Items</h3>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {similarListings.map((item) => (
                  <button
                    key={item.id}
                    onClick={() => onViewListing?.(item.id)}
                    className="text-left bg-white dark:bg-slate-700 border border-gray-200 dark:border-slate-600 rounded-xl overflow-hidden hover:shadow-lg dark:hover:shadow-slate-900/50 transition-shadow"
                  >
                    <div className="aspect-square bg-gray-100 dark:bg-slate-600">
                      <img src={item.imageUrl} alt={item.title} className="w-full h-full object-cover" />
                    </div>
                    <div className="p-3">
                      <p className="font-medium text-gray-900 dark:text-white truncate">{item.title}</p>
                      <p className="text-indigo-600 dark:text-indigo-400 font-semibold">${item.price.toFixed(2)}</p>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {showReportModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-[60]">
          <div className="bg-white dark:bg-slate-800 rounded-2xl p-6 max-w-md w-full mx-4 shadow-2xl">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Report Listing</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Report Type
                </label>
                <select
                  value={reportType}
                  onChange={(e) => setReportType(e.target.value)}
                  className="w-full p-3 border border-gray-300 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="SPAM">Spam</option>
                  <option value="INAPPROPRIATE">Inappropriate Content</option>
                  <option value="SCAM">Suspected Scam</option>
                  <option value="WRONG_CATEGORY">Wrong Category</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                  Description
                </label>
                <textarea
                  value={reportDescription}
                  onChange={(e) => setReportDescription(e.target.value)}
                  placeholder="Please describe the issue..."
                  className="w-full p-3 border border-gray-300 dark:border-slate-600 rounded-xl bg-white dark:bg-slate-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:ring-2 focus:ring-indigo-500"
                  rows={3}
                />
              </div>
              <div className="flex gap-2">
                <button
                  onClick={handleReport}
                  className="flex-1 px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-xl font-medium transition-colors"
                >
                  Submit Report
                </button>
                <button
                  onClick={() => setShowReportModal(false)}
                  className="px-4 py-2.5 bg-gray-200 dark:bg-slate-700 text-gray-800 dark:text-gray-200 rounded-xl hover:bg-gray-300 dark:hover:bg-slate-600 transition-colors"
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
