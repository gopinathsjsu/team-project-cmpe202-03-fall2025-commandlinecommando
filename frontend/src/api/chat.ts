import api from './client'

export interface Conversation {
  conversationId: number
  listingId: string
  listing: {
    title: string
    price: number
    imageUrl: string
  }
  buyer: {
    userId: string
    username: string
    firstName: string
    lastName: string
    avatarUrl?: string
  }
  seller: {
    userId: string
    username: string
    firstName: string
    lastName: string
    avatarUrl?: string
  }
  lastMessage: {
    content: string
    createdAt: string
    isRead: boolean
    senderId: string
  }
  unreadCount: number
  updatedAt: string
}

export interface Message {
  messageId: number
  conversationId: number
  senderId: string
  senderName: string
  content: string
  isRead: boolean
  createdAt: string
}

export async function getConversations() {
  const res = await api.get('/chat/conversations')
  return res.data
}

export async function createConversation(data: { listingId: string; initialMessage: string }) {
  const res = await api.post('/chat/conversations', data)
  return res.data
}

export async function getMessages(conversationId: number) {
  const res = await api.get(`/chat/conversations/${conversationId}/messages`)
  return res.data
}

export async function sendMessage(data: { conversationId: number; content: string }) {
  const res = await api.post(`/chat/conversations/${data.conversationId}/messages`, {
    content: data.content,
  })
  return res.data
}

export async function markAsRead(conversationId: number) {
  const res = await api.put(`/chat/conversations/${conversationId}/read`)
  return res.data
}
