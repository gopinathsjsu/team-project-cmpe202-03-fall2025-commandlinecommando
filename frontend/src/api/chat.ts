import api from './client'

export interface Conversation {
  conversationId: string  // Changed from number to string (UUID)
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
  messageId: string  // Changed from number to string (UUID)
  conversationId: string  // Changed from number to string (UUID)
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

export async function getMessages(conversationId: string) {
  const res = await api.get(`/chat/conversations/${conversationId}/messages`)
  return res.data
}

export async function sendMessage(data: { conversationId: string; content: string }) {
  const res = await api.post(`/chat/conversations/${data.conversationId}/messages`, {
    content: data.content,
  })
  return res.data
}

export async function markAsRead(conversationId: string) {
  // Backend uses PUT /chat/messages/{messageId}/read, but we'll use conversation endpoint if available
  // For now, mark all messages in conversation as read
  const res = await api.put(`/chat/conversations/${conversationId}/read`)
  return res.data
}
