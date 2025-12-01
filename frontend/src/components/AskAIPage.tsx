import React, { useState, useRef, useEffect } from 'react';
import { listingsApi, adminApi } from '../api';
import { Listing } from '../types';

// UUID generator with fallback for browsers that don't support crypto.randomUUID
function generateUUID(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  // Fallback UUID v4 generator
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

interface Report {
  reportId: number;
  reportType: string;
  description: string;
  listingId: string;
  listing?: Listing;
  reporterId: string;
  reporter?: { username: string; firstName: string; lastName: string };
  status: string;
  severity: string;
  createdAt: string;
}

export function AskAIPage() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [listings, setListings] = useState<Listing[]>([]);
  const [reports, setReports] = useState<Report[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  async function loadData() {
    try {
      // Fetch listings (available to all users)
      const listingsResponse = await listingsApi.getListings(0, 100);

      let listingsArray: Listing[] = [];
      if (Array.isArray(listingsResponse)) {
        listingsArray = listingsResponse;
      } else if (Array.isArray(listingsResponse.content)) {
        listingsArray = listingsResponse.content;
      } else if (Array.isArray(listingsResponse.listings)) {
        listingsArray = listingsResponse.listings;
      }
      setListings(listingsArray);
      console.log('Loaded listings:', listingsArray.length);

      // Try to fetch reports (admin only - will fail gracefully for non-admin users)
      try {
        const reportsResponse = await adminApi.getReports();
        if (Array.isArray(reportsResponse)) {
          setReports(reportsResponse);
          console.log('Loaded reports:', reportsResponse.length);
        }
      } catch (reportErr: any) {
        // Reports require admin access - this is expected for non-admin users
        console.log('Reports not available (admin access required)');
        setReports([]);
      }
    } catch (err) {
      console.error('Failed to load data:', err);
      setListings([]);
      setReports([]);
    }
  }

  function scrollToBottom() {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }

  function formatListingsContext(listings: Listing[]): string {
    return listings.map(l => 
      `- "${l.title}" | Category: ${l.category} | Condition: ${l.condition} | Price: $${l.price} | Location: ${l.location || 'N/A'} | Description: ${l.description?.slice(0, 100) || 'N/A'}`
    ).join('\n');
  }

  function formatReportsContext(reports: Report[]): string {
    return reports.map(r => {
      const listingTitle = r.listing?.title || 'Unknown Listing';
      const reporterName = r.reporter ? `${r.reporter.firstName} ${r.reporter.lastName}` : 'Unknown';
      return `- Report #${r.reportId} | Type: ${r.reportType} | Listing: "${listingTitle}" | Reporter: ${reporterName} | Status: ${r.status} | Severity: ${r.severity} | Reason: ${r.description} | Date: ${new Date(r.createdAt).toLocaleDateString()}`;
    }).join('\n');
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!input.trim() || loading) {
      console.log('Submit blocked:', { input: input.trim(), loading });
      return;
    }

    const userMessage: Message = {
      id: generateUUID(),
      role: 'user',
      content: input.trim(),
      timestamp: new Date(),
    };

    setMessages(prev => [...prev, userMessage]);
    const currentInput = input.trim();
    setInput('');
    setLoading(true);

    try {
      const listingsContext = formatListingsContext(listings);
      const reportsContext = formatReportsContext(reports);
      
      console.log('Sending AI request to /ai/chat');
      
      // Use AI service endpoint - /ai/chat will be proxied by Nginx to ai-integration-server:3001/api/chat
      const response = await fetch('/ai/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          messages: [
            ...messages.map(m => ({ role: m.role, content: m.content })),
            { role: 'user', content: currentInput },
          ],
          listingsContext,
          reportsContext,
        }),
      });

      console.log('AI response status:', response.status, response.statusText);

      if (!response.ok) {
        let errorMessage = 'Failed to get AI response';
        try {
          const error = await response.json();
          errorMessage = error.error || error.message || errorMessage;
        } catch (e) {
          errorMessage = `HTTP ${response.status}: ${response.statusText}`;
        }
        throw new Error(errorMessage);
      }

      const data = await response.json();
      console.log('AI response data:', data);

      const assistantMessage: Message = {
        id: generateUUID(),
        role: 'assistant',
        content: data.message || data.response || 'No response received',
        timestamp: new Date(),
      };

      setMessages(prev => [...prev, assistantMessage]);
    } catch (err: any) {
      console.error('AI Error:', err);
      const errorMessage: Message = {
        id: generateUUID(),
        role: 'assistant',
        content: `Sorry, I encountered an error: ${err.message || 'Unknown error'}. Please check the browser console for details.`,
        timestamp: new Date(),
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  }

  const suggestedQuestions = [
    "What's the cheapest laptop available?",
    "Show me textbooks under $50",
    "Which listings have been reported?",
    "What are the pending reports?",
  ];

  return (
    <div className="min-h-screen pb-24">
      <div className="fixed inset-0 -z-10 bg-gradient-to-br from-indigo-500/10 via-purple-500/5 to-pink-500/10 dark:from-indigo-900/20 dark:via-purple-900/10 dark:to-pink-900/20"></div>
      
      <header className="nav-glass px-6 py-4 sticky top-0 z-10">
        <div className="max-w-4xl mx-auto flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl gradient-primary flex items-center justify-center shadow-lg">
            <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
            </svg>
          </div>
          <div>
            <h1 className="text-xl font-bold gradient-text">Ask AI</h1>
            <p className="text-sm text-muted">Get help finding products</p>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 sm:px-6 py-6">
        {messages.length === 0 ? (
          <div className="text-center py-12">
            <div className="w-20 h-20 mx-auto mb-6 rounded-2xl gradient-primary flex items-center justify-center shadow-xl">
              <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold mb-2">How can I help you today?</h2>
            <p className="text-muted mb-8 max-w-md mx-auto">
              Ask me anything about the marketplace! I can help you find products, compare prices, and discover the best deals.
            </p>
            
            <div className="grid sm:grid-cols-2 gap-3 max-w-lg mx-auto">
              {suggestedQuestions.map((question, i) => (
                <button
                  key={i}
                  onClick={() => setInput(question)}
                  className="glass-card p-4 text-left text-sm hover:border-indigo-500/50 transition-colors card-hover"
                >
                  <span className="text-muted">{question}</span>
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="space-y-4 mb-4">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`flex ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`max-w-[85%] sm:max-w-[75%] rounded-2xl px-4 py-3 ${
                    message.role === 'user'
                      ? 'gradient-primary text-white'
                      : 'glass-card'
                  }`}
                >
                  {message.role === 'assistant' && (
                    <div className="flex items-center gap-2 mb-2 pb-2 border-b border-white/10 dark:border-white/10">
                      <div className="w-6 h-6 rounded-lg gradient-primary flex items-center justify-center">
                        <svg className="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                        </svg>
                      </div>
                      <span className="text-xs font-medium text-muted">AI Assistant</span>
                    </div>
                  )}
                  <div className="whitespace-pre-wrap text-sm leading-relaxed">
                    {message.content}
                  </div>
                </div>
              </div>
            ))}
            
            {loading && (
              <div className="flex justify-start">
                <div className="glass-card rounded-2xl px-4 py-3">
                  <div className="flex items-center gap-2">
                    <div className="w-6 h-6 rounded-lg gradient-primary flex items-center justify-center">
                      <svg className="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                      </svg>
                    </div>
                    <div className="flex gap-1">
                      <span className="w-2 h-2 bg-indigo-500 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></span>
                      <span className="w-2 h-2 bg-indigo-500 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></span>
                      <span className="w-2 h-2 bg-indigo-500 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></span>
                    </div>
                  </div>
                </div>
              </div>
            )}
            
            <div ref={messagesEndRef} />
          </div>
        )}

        <form onSubmit={handleSubmit} className="fixed bottom-20 left-4 right-4 sm:left-auto sm:right-auto sm:max-w-4xl sm:mx-auto sm:w-full sm:px-6">
          <div className="glass-card p-2 flex items-center gap-2">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask about products, prices, recommendations..."
              className="flex-1 bg-transparent px-4 py-3 outline-none text-sm placeholder:text-muted"
              disabled={loading}
            />
            <button
              type="submit"
              onClick={(e) => {
                e.preventDefault();
                if (input.trim() && !loading) {
                  handleSubmit(e);
                }
              }}
              disabled={!input.trim() || loading}
              className="w-10 h-10 rounded-xl gradient-primary flex items-center justify-center text-white disabled:opacity-50 disabled:cursor-not-allowed transition-opacity hover:opacity-90"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}
