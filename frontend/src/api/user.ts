import api from './client'

export interface UserProfile {
  userId: string
  username: string
  email: string
  firstName: string
  lastName: string
  phone?: string
  major?: string
  graduationYear?: number
  avatarUrl?: string
  createdAt: string
}

export async function getProfile() {
  const res = await api.get('/users/profile')
  return res.data
}

export async function updateProfile(data: Partial<UserProfile>) {
  const res = await api.put('/users/profile', data)
  return res.data
}

export async function changePassword(currentPassword: string, newPassword: string) {
  const res = await api.post('/users/change-password', { currentPassword, newPassword })
  return res.data
}
