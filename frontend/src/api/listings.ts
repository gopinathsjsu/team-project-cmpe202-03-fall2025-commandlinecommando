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
  const res = await api.get(`/student/listings?page=${page}&size=${size}`)
  return res.data
}

export async function createListing(listing: Omit<Listing, 'id' | 'sellerId' | 'date'>) {
  const res = await api.post('/student/listings', listing)
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
  const queryParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined) {
      queryParams.append(key, String(value))
    }
  })
  
  const res = await api.get(`/student/listings/search?${queryParams.toString()}`)
  return res.data
}