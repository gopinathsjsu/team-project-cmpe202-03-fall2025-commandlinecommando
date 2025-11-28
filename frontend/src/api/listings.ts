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
  const res = await api.post('/search', {
    query: params.keyword || '',
    category: params.category,
    condition: params.condition,
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
  const res = await api.post('/reports', {
    targetId: id,
    reportType: data.reportType || 'PRODUCT',
    reason: 'INAPPROPRIATE_CONTENT',
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