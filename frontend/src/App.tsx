import React, { useState, useEffect } from 'react';
import { LoginForm } from './components/LoginForm';
import { RegisterForm } from './components/RegisterForm';
import { ForgotPasswordForm } from './components/ForgotPasswordForm';
import { ThemeToggle } from './components/ThemeToggle';
import { MarketplacePage } from './components/MarketplacePage';
import { ReportListingPage } from './components/ReportListingPage';
import { AdminDashboard } from './components/AdminDashboard';
import { AskAIPage } from './components/AskAIPage';
import { ProtectedRoute } from './components/ProtectedRoute';
import { useAuth } from './context/AuthContext';

export default function App() {
  const { isAuthenticated, user, isLoading, isAdmin } = useAuth();
  type PageType = 'login' | 'register' | 'forgot-password' | 'marketplace' | 'reports' | 'admin' | 'ask-ai';
  const [currentPage, setCurrentPage] = useState<PageType>('login');

  useEffect(() => {
    if (isAuthenticated && user) {
      // Use helper method to check for ADMIN role
      if (isAdmin()) {
        setCurrentPage('admin');
      } else {
        setCurrentPage('marketplace');
      }
    } else if (!isLoading) {
      setCurrentPage('login');
    }
  }, [isAuthenticated, user, isLoading]);

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="relative w-20 h-20 mx-auto mb-6">
            <div className="absolute inset-0 rounded-full border-4 border-transparent border-t-[hsl(var(--primary))] animate-spin"></div>
            <div className="absolute inset-2 rounded-full border-4 border-transparent border-t-[hsl(var(--secondary))] animate-spin" style={{ animationDirection: 'reverse', animationDuration: '1.5s' }}></div>
            <div className="absolute inset-4 rounded-full gradient-primary opacity-20"></div>
          </div>
          <p className="text-muted text-lg">Loading CampusConnect...</p>
        </div>
      </div>
    );
  }

  if (currentPage === 'login') {
    return (
      <div className="min-h-screen relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/20 via-purple-500/10 to-pink-500/20 dark:from-indigo-900/40 dark:via-purple-900/30 dark:to-pink-900/40"></div>
        <div className="absolute top-20 -left-20 w-72 h-72 bg-purple-500/30 rounded-full blur-3xl animate-pulse-slow"></div>
        <div className="absolute bottom-20 -right-20 w-96 h-96 bg-indigo-500/30 rounded-full blur-3xl animate-pulse-slow" style={{ animationDelay: '2s' }}></div>
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-to-r from-indigo-500/10 to-purple-500/10 rounded-full blur-3xl"></div>
        
        <header className="absolute top-0 left-0 right-0 z-20 p-6">
          <div className="flex items-center justify-between max-w-7xl mx-auto">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl gradient-primary flex items-center justify-center shadow-lg shadow-indigo-500/30">
                <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                </svg>
              </div>
              <div>
                <h1 className="text-2xl font-bold gradient-text">CampusConnect</h1>
                <p className="text-sm text-muted">Campus Marketplace</p>
              </div>
            </div>
            <ThemeToggle />
          </div>
        </header>

        <div className="min-h-screen flex items-center justify-center p-6 pt-28 relative z-10">
          <div className="w-full max-w-6xl mx-auto">
            <div className="grid lg:grid-cols-2 gap-16 items-center">
              <div className="space-y-8 text-center lg:text-left">
                <div className="space-y-6">
                  <div className="inline-flex items-center gap-2 glass-card px-4 py-2">
                    <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></div>
                    <span className="text-sm font-medium">Student Community</span>
                  </div>
                  
                  <h2 className="text-5xl lg:text-6xl font-bold leading-tight">
                    <span className="gradient-text">Buy & Sell</span>
                    <br />
                    <span className="text-[hsl(var(--foreground))]">Within Your Campus</span>
                  </h2>
                  
                  <p className="text-lg text-muted max-w-lg mx-auto lg:mx-0">
                    Connect with fellow students to trade textbooks, electronics, furniture, and more. 
                    Safe, convenient, and campus-exclusive.
                  </p>
                </div>

                <div className="flex flex-wrap gap-4 justify-center lg:justify-start">
                  <div className="glass-card px-5 py-4 card-hover">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center">
                        <svg className="w-6 h-6 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                        </svg>
                      </div>
                      <div>
                        <p className="font-semibold">Textbooks</p>
                        <p className="text-sm text-muted">Save up to 70%</p>
                      </div>
                    </div>
                  </div>
                  
                  <div className="glass-card px-5 py-4 card-hover">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 rounded-xl bg-blue-500/20 flex items-center justify-center">
                        <svg className="w-6 h-6 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                        </svg>
                      </div>
                      <div>
                        <p className="font-semibold">Electronics</p>
                        <p className="text-sm text-muted">Verified sellers</p>
                      </div>
                    </div>
                  </div>
                  
                  <div className="glass-card px-5 py-4 card-hover">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 rounded-xl bg-purple-500/20 flex items-center justify-center">
                        <svg className="w-6 h-6 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                        </svg>
                      </div>
                      <div>
                        <p className="font-semibold">Furniture</p>
                        <p className="text-sm text-muted">Local pickup</p>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-4 justify-center lg:justify-start">
                  <div className="flex -space-x-3">
                    {[1, 2, 3, 4].map((i) => (
                      <div key={i} className="w-10 h-10 rounded-full gradient-primary border-2 border-[hsl(var(--background))] flex items-center justify-center text-white text-xs font-bold">
                        {String.fromCharCode(64 + i)}
                      </div>
                    ))}
                  </div>
                  <div>
                    <p className="font-semibold">2,500+ Students</p>
                    <p className="text-sm text-muted">Already trading safely</p>
                  </div>
                </div>
              </div>

              <div className="flex justify-center lg:justify-end">
                <LoginForm 
                  onLogin={() => {
                    // Check if user has ADMIN role
                    if (isAdmin()) setCurrentPage('admin');
                    else setCurrentPage('marketplace');
                  }}
                  onSignUp={() => setCurrentPage('register')}
                  onForgotPassword={() => setCurrentPage('forgot-password')}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (currentPage === 'register') {
    return (
      <div className="min-h-screen relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-purple-500/20 via-pink-500/10 to-orange-500/20 dark:from-purple-900/40 dark:via-pink-900/30 dark:to-orange-900/40"></div>
        <div className="absolute top-40 -right-20 w-80 h-80 bg-pink-500/30 rounded-full blur-3xl animate-pulse-slow"></div>
        <div className="absolute bottom-40 -left-20 w-72 h-72 bg-purple-500/30 rounded-full blur-3xl animate-pulse-slow" style={{ animationDelay: '1s' }}></div>
        
        <header className="absolute top-0 left-0 right-0 z-20 p-6">
          <div className="flex items-center justify-between max-w-7xl mx-auto">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl gradient-accent flex items-center justify-center shadow-lg shadow-purple-500/30">
                <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                </svg>
              </div>
              <div>
                <h1 className="text-2xl font-bold gradient-text">CampusConnect</h1>
                <p className="text-sm text-muted">Campus Marketplace</p>
              </div>
            </div>
            <ThemeToggle />
          </div>
        </header>

        <div className="min-h-screen flex items-center justify-center p-6 pt-28 relative z-10">
          <RegisterForm 
            onRegister={(roles) => {
              // Check if returned roles include ADMIN
              const hasAdmin = roles?.includes('ADMIN');
              if (hasAdmin) {
                setCurrentPage('admin');
              } else {
                setCurrentPage('marketplace');
              }
            }}
            onBackToLogin={() => setCurrentPage('login')}
          />
        </div>
      </div>
    );
  }

  if (currentPage === 'forgot-password') {
    return (
      <div className="min-h-screen relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-amber-500/20 via-orange-500/10 to-red-500/20 dark:from-amber-900/40 dark:via-orange-900/30 dark:to-red-900/40"></div>
        <div className="absolute top-20 left-20 w-64 h-64 bg-amber-500/30 rounded-full blur-3xl animate-pulse-slow"></div>
        <div className="absolute bottom-20 right-20 w-80 h-80 bg-orange-500/30 rounded-full blur-3xl animate-pulse-slow" style={{ animationDelay: '1.5s' }}></div>
        
        <header className="absolute top-0 left-0 right-0 z-20 p-6">
          <div className="flex items-center justify-between max-w-7xl mx-auto">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-amber-500 to-orange-500 flex items-center justify-center shadow-lg shadow-amber-500/30">
                <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                </svg>
              </div>
              <div>
                <h1 className="text-2xl font-bold gradient-text">CampusConnect</h1>
                <p className="text-sm text-muted">Campus Marketplace</p>
              </div>
            </div>
            <ThemeToggle />
          </div>
        </header>

        <div className="min-h-screen flex items-center justify-center p-6 pt-28 relative z-10">
          <ForgotPasswordForm 
            onBackToLogin={() => setCurrentPage('login')}
            onResetSuccess={() => setCurrentPage('login')}
          />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen">
      {currentPage === 'ask-ai' && (
        <ProtectedRoute>
          <AskAIPage />
        </ProtectedRoute>
      )}
      {currentPage === 'marketplace' && (
        <ProtectedRoute>
          <MarketplacePage />
        </ProtectedRoute>
      )}
      {currentPage === 'reports' && (
        <ProtectedRoute>
          <ReportListingPage />
        </ProtectedRoute>
      )}
      {currentPage === 'admin' && (
        <ProtectedRoute requiredRole="ADMIN">
          <AdminDashboard />
        </ProtectedRoute>
      )}
      
      <div className="fixed bottom-4 sm:bottom-6 left-4 right-4 sm:right-auto sm:left-6 z-50 glass-card p-3 sm:p-4 shadow-xl">
        <div className="flex items-center gap-1.5 sm:gap-2 flex-wrap">
          <button
            className={`px-2 sm:px-4 py-1.5 sm:py-2 rounded-xl text-xs sm:text-sm font-medium transition-all duration-300 ${
              currentPage === 'ask-ai' 
                ? 'gradient-primary text-white shadow-lg shadow-indigo-500/30' 
                : 'glass-button'
            }`}
            onClick={() => setCurrentPage('ask-ai')}
          >
            Ask AI
          </button>
          <button
            className={`px-2 sm:px-4 py-1.5 sm:py-2 rounded-xl text-xs sm:text-sm font-medium transition-all duration-300 ${
              currentPage === 'marketplace' 
                ? 'gradient-primary text-white shadow-lg shadow-indigo-500/30' 
                : 'glass-button'
            }`}
            onClick={() => setCurrentPage('marketplace')}
          >
            Marketplace
          </button>
          <button
            className={`px-2 sm:px-4 py-1.5 sm:py-2 rounded-xl text-xs sm:text-sm font-medium transition-all duration-300 ${
              currentPage === 'admin' 
                ? 'gradient-primary text-white shadow-lg shadow-indigo-500/30' 
                : 'glass-button'
            }`}
            onClick={() => setCurrentPage('admin')}
          >
            Admin
          </button>
          <button
            className={`px-2 sm:px-4 py-1.5 sm:py-2 rounded-xl text-xs sm:text-sm font-medium transition-all duration-300 ${
              currentPage === 'reports' 
                ? 'gradient-primary text-white shadow-lg shadow-indigo-500/30' 
                : 'glass-button'
            }`}
            onClick={() => setCurrentPage('reports')}
          >
            Reports
          </button>
          <ThemeToggle />
        </div>
      </div>
    </div>
  );
}
