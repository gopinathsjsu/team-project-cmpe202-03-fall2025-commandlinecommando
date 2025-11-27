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
  const res = await api.get('/user/profile')
  return res.data
}

export async function updateProfile(data: Partial<UserProfile>) {
  const res = await api.put('/user/profile', data)
  return res.data
}

export async function changePassword(currentPassword: string, newPassword: string) {
  const res = await api.put('/user/password', { currentPassword, newPassword })
  return res.data
}
