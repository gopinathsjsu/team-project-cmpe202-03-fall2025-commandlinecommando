import React, { useState, useEffect, useRef } from 'react';
import { chatApi } from '../api';
import { useAuth } from '../context/AuthContext';

interface Conversation {
  conversationId: number;
  listingId: string;
  listing: {
    title: string;
    price: number;
    imageUrl: string;
  };
  buyer: {
    userId: string;
    username: string;
    firstName: string;
    lastName: string;
    avatarUrl?: string;
  };
  seller: {
    userId: string;
    username: string;
    firstName: string;
    lastName: string;
    avatarUrl?: string;
  };
  lastMessage: {
    content: string;
    createdAt: string;
    isRead: boolean;
    senderId: string;
  };
  unreadCount: number;
  updatedAt: string;
}

interface Message {
  messageId: number;
  conversationId: number;
  senderId: string;
  senderName: string;
  content: string;
  isRead: boolean;
  createdAt: string;
}

interface Props {
  onBack: () => void;
}

export function MessagesPage({ onBack }: Props) {
  const { user } = useAuth();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConversation, setSelectedConversation] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [sendingMessage, setSendingMessage] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadConversations();
  }, []);

  useEffect(() => {
    if (selectedConversation) {
      loadMessages(selectedConversation.conversationId);
      markAsRead(selectedConversation.conversationId);
    }
  }, [selectedConversation?.conversationId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  async function loadConversations() {
    try {
      setLoading(true);
      const response = await chatApi.getConversations();
      setConversations(response.conversations || []);
    } catch (err) {
      console.error('Failed to load conversations:', err);
    } finally {
      setLoading(false);
    }
  }

  async function loadMessages(conversationId: number) {
    try {
      const response = await chatApi.getMessages(conversationId);
      setMessages(response.messages || []);
    } catch (err) {
      console.error('Failed to load messages:', err);
    }
  }

  async function markAsRead(conversationId: number) {
    try {
      await chatApi.markAsRead(conversationId);
      setConversations(prev =>
        prev.map(c =>
          c.conversationId === conversationId ? { ...c, unreadCount: 0 } : c
        )
      );
    } catch (err) {
      console.error('Failed to mark as read:', err);
    }
  }

  async function handleSendMessage() {
    if (!newMessage.trim() || !selectedConversation) return;

    try {
      setSendingMessage(true);
      const result = await chatApi.sendMessage({
        conversationId: selectedConversation.conversationId,
        content: newMessage,
      });

      const newMsg: Message = {
        messageId: result.messageId,
        conversationId: selectedConversation.conversationId,
        senderId: user?.id || '',
        senderName: `${user?.firstName || ''} ${user?.lastName || ''}`.trim() || user?.username || 'You',
        content: newMessage,
        isRead: false,
        createdAt: result.createdAt || new Date().toISOString(),
      };

      setMessages(prev => [...prev, newMsg]);
      setNewMessage('');
      loadConversations();
    } catch (err) {
      console.error('Failed to send message:', err);
      alert('Failed to send message');
    } finally {
      setSendingMessage(false);
    }
  }

  function scrollToBottom() {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }

  function formatTime(dateString: string) {
    const date = new Date(dateString);
    const now = new Date();
    const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
      return date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
    } else if (diffDays === 1) {
      return 'Yesterday';
    } else if (diffDays < 7) {
      return date.toLocaleDateString('en-US', { weekday: 'short' });
    } else {
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }
  }

  function getOtherParty(conversation: Conversation) {
    const isBuyer = conversation.buyer.userId === user?.id;
    return isBuyer ? conversation.seller : conversation.buyer;
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="fixed inset-0 -z-10 bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-pink-500/10 dark:from-indigo-900/20 dark:via-purple-900/10 dark:to-pink-900/20"></div>
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      <div className="fixed inset-0 -z-10 bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-pink-500/10 dark:from-indigo-900/20 dark:via-purple-900/10 dark:to-pink-900/20"></div>
      
      <header className="nav-glass px-6 py-4 sticky top-0 z-10">
        <div className="max-w-6xl mx-auto flex items-center gap-4">
          <button
            onClick={onBack}
            className="nav-button"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back
          </button>
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl gradient-primary flex items-center justify-center shadow-lg">
              <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </div>
            <h1 className="text-xl font-bold gradient-text">Messages</h1>
          </div>
        </div>
      </header>

      <div className="max-w-6xl mx-auto px-6 py-6">
        <div className="flex h-[calc(100vh-120px)] glass-card overflow-hidden">
          <div className={`w-full md:w-80 border-r border-white/10 flex-shrink-0 flex flex-col ${selectedConversation ? 'hidden md:flex' : ''}`}>
            <div className="p-4 border-b border-white/10">
              <h2 className="font-semibold">Conversations</h2>
            </div>
            {conversations.length === 0 ? (
              <div className="p-8 text-center text-muted flex-1 flex flex-col items-center justify-center">
                <svg className="w-16 h-16 text-muted mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                <p className="font-medium">No messages yet</p>
                <p className="text-sm mt-2">Start a conversation by contacting a seller</p>
              </div>
            ) : (
              <div className="overflow-y-auto flex-1">
                {conversations.map((conversation) => {
                  const otherParty = getOtherParty(conversation);
                  return (
                    <button
                      key={conversation.conversationId}
                      onClick={() => setSelectedConversation(conversation)}
                      className={`w-full p-4 flex gap-3 hover:bg-white/50 dark:hover:bg-white/10 border-b border-white/10 text-left transition-colors ${
                        selectedConversation?.conversationId === conversation.conversationId 
                          ? 'bg-indigo-500/10' 
                          : ''
                      }`}
                    >
                      <div className="w-12 h-12 rounded-full gradient-primary flex items-center justify-center flex-shrink-0 overflow-hidden">
                        {otherParty.avatarUrl ? (
                          <img src={otherParty.avatarUrl} alt="" className="w-full h-full object-cover" />
                        ) : (
                          <span className="text-white font-semibold">
                            {otherParty.firstName?.charAt(0) || otherParty.username.charAt(0)}
                          </span>
                        )}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex justify-between items-start">
                          <span className="font-medium truncate">
                            {otherParty.firstName} {otherParty.lastName}
                          </span>
                          <span className="text-xs text-muted flex-shrink-0">
                            {formatTime(conversation.updatedAt)}
                          </span>
                        </div>
                        <p className="text-sm truncate gradient-text font-medium">{conversation.listing.title}</p>
                        <p className="text-sm text-muted truncate">{conversation.lastMessage.content}</p>
                        {conversation.unreadCount > 0 && (
                          <span className="inline-block mt-1 badge badge-primary text-xs">
                            {conversation.unreadCount} new
                          </span>
                        )}
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          <div className={`flex-1 flex flex-col ${!selectedConversation ? 'hidden md:flex' : ''}`}>
            {selectedConversation ? (
              <>
                <div className="p-4 border-b border-white/10 flex items-center gap-3">
                  <button
                    onClick={() => setSelectedConversation(null)}
                    className="md:hidden nav-button"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                  </button>
                  <div className="w-12 h-12 rounded-xl overflow-hidden flex-shrink-0 shadow-md">
                    <img
                      src={selectedConversation.listing.imageUrl}
                      alt=""
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium truncate">{selectedConversation.listing.title}</p>
                    <p className="text-lg font-bold gradient-text">${selectedConversation.listing.price.toFixed(2)}</p>
                  </div>
                </div>

                <div className="flex-1 overflow-y-auto p-4 space-y-4">
                  {messages.map((message) => {
                    const isMe = message.senderId === user?.id;
                    return (
                      <div
                        key={message.messageId}
                        className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}
                      >
                        <div
                          className={`max-w-[70%] rounded-2xl p-4 ${
                            isMe 
                              ? 'gradient-primary text-white shadow-lg' 
                              : 'glass-card'
                          }`}
                        >
                          <p className="break-words">{message.content}</p>
                          <p className={`text-xs mt-2 ${isMe ? 'text-white/70' : 'text-muted'}`}>
                            {formatTime(message.createdAt)}
                          </p>
                        </div>
                      </div>
                    );
                  })}
                  <div ref={messagesEndRef} />
                </div>

                <div className="p-4 border-t border-white/10">
                  <div className="flex gap-3">
                    <input
                      type="text"
                      value={newMessage}
                      onChange={(e) => setNewMessage(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                      placeholder="Type a message..."
                      className="input-glass flex-1"
                    />
                    <button
                      onClick={handleSendMessage}
                      disabled={sendingMessage || !newMessage.trim()}
                      className="nav-button-primary"
                    >
                      {sendingMessage ? (
                        <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                      ) : (
                        <>
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
                          </svg>
                          Send
                        </>
                      )}
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex-1 flex items-center justify-center text-muted">
                <div className="text-center">
                  <svg className="w-20 h-20 mx-auto text-muted mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                  </svg>
                  <p className="text-lg font-medium">Select a conversation</p>
                  <p className="text-sm mt-2">Choose from your existing conversations or start a new one</p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
