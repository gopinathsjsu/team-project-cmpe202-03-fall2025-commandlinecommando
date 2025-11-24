import api from './client'

export interface StudentDashboard {
  message: string
  myListings: number
  watchlist: number
  messages: number
}

export async function getStudentDashboard(): Promise<StudentDashboard> {
  const res = await api.get('/student/dashboard')
  return res.data
}

