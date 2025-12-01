import api from './client'

export interface Listing {
  id: string  // UUID from backend
  title: string
  description: string
  category: string
  condition: string
  price: number
  location: string
  date: string
  createdAt: string
  sellerId: string
  seller?: {
    id: string
    name: string
    username: string
  }
  imageUrl?: string
  imageUrls?: string[]  // Array of all image URLs
  favorite?: boolean
  viewCount?: number
  favoriteCount?: number
  negotiable?: boolean
  quantity?: number
}

export interface ListingResponse {
  content: Listing[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export async function getListings(page = 0, size = 20) {
  const res = await api.get(`/listings?page=${page}&size=${size}`)
  return res.data
}

export async function createListing(listing: Omit<Listing, 'id' | 'sellerId' | 'date'>) {
  const res = await api.post('/listings', listing)
  return res.data
}

export async function searchListings(params: {
  status?: string
  keyword?: string
  category?: string
  condition?: string
  minPrice?: number
  maxPrice?: number
  location?: string
  page?: number
  size?: number
}) {
  // Backend uses POST /search with SearchRequest body
  // Backend expects categories (array) and conditions (array), not singular values
  const res = await api.post('/search', {
    query: params.keyword || '',
    categories: params.category ? [params.category] : undefined,
    conditions: params.condition ? [params.condition] : undefined,
    minPrice: params.minPrice,
    maxPrice: params.maxPrice,
    location: params.location,
    page: params.page || 0,
    size: params.size || 20,
    sortBy: 'relevance'
  })
  return res.data
}

export async function getListing(id: string) {
  const res = await api.get(`/listings/${id}`)
  return res.data
}

export async function updateListing(id: string, data: Partial<Listing>) {
  const res = await api.put(`/listings/${id}`, data)
  return res.data
}

export async function deleteListing(id: string) {
  const res = await api.delete(`/listings/${id}`)
  return res.data
}

export async function toggleFavorite(id: string) {
  const res = await api.post(`/favorites/${id}`)
  return res.data
}

export async function getFavorites() {
  const res = await api.get('/favorites')
  return res.data
}

export async function reportListing(id: string, data: { reportType: string; description: string }) {
  // reportType from frontend is actually the "reason" (SPAM, INAPPROPRIATE, etc.)
  // Backend expects: reportType=PRODUCT and reason=SPAM/INAPPROPRIATE/etc.
  const res = await api.post('/reports', {
    targetId: id,
    reportType: 'PRODUCT',  // Always PRODUCT when reporting a listing
    reason: data.reportType || 'OTHER',  // Frontend's reportType is actually the reason
    description: data.description
  })
  return res.data
}

export async function getTrending(limit = 10) {
  const res = await api.get(`/discovery/trending?limit=${limit}`)
  return res.data
}

export async function getRecommended(limit = 10) {
  const res = await api.get(`/discovery/recommended?limit=${limit}`)
  return res.data
}

export async function getSimilar(productId: string, limit = 5) {
  const res = await api.get(`/discovery/similar/${productId}?limit=${limit}`)
  return res.data
}

export async function getRecentlyViewed(limit = 10) {
  const res = await api.get(`/discovery/recently-viewed?limit=${limit}`)
  return res.data
}

export async function autocomplete(query: string) {
  const res = await api.get(`/search/autocomplete?q=${encodeURIComponent(query)}`)
  return res.data
}

export async function getSearchHistory(limit = 10) {
  const res = await api.get(`/search/history?limit=${limit}`)
  return res.data
}

/**
 * Upload images for temporary storage before creating a listing
 * Returns array of image URLs that can be used when creating the listing
 */
export async function uploadImages(files: File[]): Promise<{ imageUrls: string[], tempId: string }> {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  const res = await api.post('/images/upload/temp', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
  return res.data
}

/**
 * Upload images to an existing listing
 */
export async function uploadListingImages(listingId: string, files: File[]): Promise<{ imageUrls: string[] }> {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  const res = await api.post(`/images/upload/${listingId}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
  return res.data
}

/**
 * Delete an image from a listing
 */
export async function deleteListingImage(listingId: string, imageUrl: string): Promise<void> {
  await api.delete(`/images/${listingId}?imageUrl=${encodeURIComponent(imageUrl)}`)
}

/**
 * Set primary image for a listing
 */
export async function setPrimaryImage(listingId: string, imageUrl: string): Promise<void> {
  await api.put(`/images/${listingId}/primary?imageUrl=${encodeURIComponent(imageUrl)}`)
}