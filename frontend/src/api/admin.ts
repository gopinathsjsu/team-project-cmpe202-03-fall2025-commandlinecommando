import api from './client'

export interface AdminDashboard {
  message: string
  totalUsers: number
  totalListings: number
  pendingApprovals: number
}

export interface UserListResponse {
  message: string
  userCount: number
  users?: any[]
}

export async function getAdminDashboard(): Promise<AdminDashboard> {
  const res = await api.get('/admin/dashboard')
  return res.data
}

export async function getAllUsers(): Promise<UserListResponse> {
  const res = await api.get('/admin/users')
  return res.data
}

export async function moderateListing(listingId: string, action: string): Promise<any> {
  const res = await api.post(`/admin/moderate/${listingId}?action=${action}`)
  return res.data
}

export async function deleteUser(userId: string): Promise<any> {
  const res = await api.delete(`/admin/users/${userId}`)
  return res.data
}

export async function getAnalytics() {
  const res = await api.get('/admin/analytics')
  return res.data
}

export async function getUser(userId: string) {
  const res = await api.get(`/admin/users/${userId}`)
  return res.data
}

export async function updateUser(userId: string, data: any) {
  const res = await api.put(`/admin/users/${userId}`, data)
  return res.data
}

export async function getReports(status?: string) {
  const url = status ? `/reports/admin?status=${status}` : '/reports/admin'
  const res = await api.get(url)
  return res.data
}

export async function updateReport(reportId: string, data: { status: string; resolutionNotes?: string }) {
  // Backend expects PUT /reports/admin/{reportId} with body containing status and notes (not resolutionNotes)
  const res = await api.put(`/reports/admin/${reportId}`, {
    status: data.status,
    notes: data.resolutionNotes || (data.status === 'APPROVED' ? 'Report approved' : data.status === 'REJECTED' ? 'Report rejected' : 'Report flagged')
  })
  return res.data
}

