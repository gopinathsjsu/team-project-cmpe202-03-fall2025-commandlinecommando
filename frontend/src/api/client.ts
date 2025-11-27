import axios from 'axios'

// Vite exposes env vars on import.meta.env
const baseURL = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_BACKEND_API_BASE_URL)
  ? String(import.meta.env.VITE_BACKEND_API_BASE_URL)
  : 'http://localhost:8080/api'

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false,
  timeout: 5000, // 5 second timeout to prevent hanging requests
})

// Request interceptor: Attach access token if present
api.interceptors.request.use(
  (config) => {
    // Debug: log outgoing request method/url/data
    if (typeof window !== 'undefined') {
      try {
        // eslint-disable-next-line no-console
        console.log('[api] Request:', (config.method || '').toUpperCase(), config.baseURL + (config.url || ''), config.data)
      } catch (err) {
        // ignore logging errors
      }
    }
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('accessToken')
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor: Handle token refresh on 401
let isRefreshing = false
let failedQueue: any[] = []

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // If error is 401 and we haven't tried to refresh yet
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // If already refreshing, queue this request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          if (token && originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${token}`
          }
          return api(originalRequest)
        }).catch(err => {
          return Promise.reject(err)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      const refreshToken = localStorage.getItem('refreshToken')
      
      if (!refreshToken) {
        // No refresh token, clear auth. Navigation removed to avoid full-page reload.
        localStorage.clear()
        return Promise.reject(error)
      }

      try {
        const response = await axios.post(`${baseURL}/auth/refresh`, {
          refreshToken
        })

        const { accessToken, refreshToken: newRefreshToken } = response.data
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', newRefreshToken)

        // Update authorization header
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
        }

        processQueue(null, accessToken)
        isRefreshing = false

        // Retry original request
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        isRefreshing = false
        
        // Refresh failed, clear auth. Navigation removed to avoid full-page reload.
        localStorage.clear()
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default api
