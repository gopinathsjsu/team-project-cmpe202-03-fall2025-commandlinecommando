import api from './client'

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  role: string
  username: string
  userId: string
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  const res = await api.post('/auth/login', { username, password })
  return res.data as LoginResponse
}

export async function register(payload: Record<string, unknown>): Promise<LoginResponse> {
  const res = await api.post('/auth/register', payload)
  return res.data as LoginResponse
}

export async function me(): Promise<any> {
  const res = await api.get('/auth/me')
  return res.data
}

export async function refresh(refreshToken: string): Promise<LoginResponse> {
  const res = await api.post('/auth/refresh', { refreshToken })
  return res.data as LoginResponse
}

export async function logout(): Promise<void> {
  const refreshToken = localStorage.getItem('refreshToken')
  if (refreshToken) {
    await api.post('/auth/logout', { refreshToken })
  }
}

export async function logoutAll(): Promise<void> {
  await api.post('/auth/logout-all')
}

export async function validateToken(): Promise<any> {
  const res = await api.get('/auth/validate')
  return res.data
}

export async function requestPasswordReset(email: string): Promise<any> {
  const res = await api.post('/auth/forgot-password', { email })
  return res.data
}

export async function resetPassword(token: string, newPassword: string): Promise<any> {
  const res = await api.post('/auth/reset-password', { token, newPassword })
  return res.data
}
