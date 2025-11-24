import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { login as apiLogin, logout as apiLogout, validateToken, me, refresh as apiRefresh } from '../api/auth';

interface User {
  id: string;
  username: string;
  email: string;
  role: string;
  firstName?: string;
  lastName?: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshAuth: () => Promise<void>;
  setAuthFromResponse: (response: any) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [skipInitialCheck, setSkipInitialCheck] = useState(false);

  // Check if user is authenticated on mount
  useEffect(() => {
    // Skip if we just set auth from a response (like registration)
    // Check localStorage flag that was set during registration
    const justAuthenticated = localStorage.getItem('_justAuthenticated');
    const authTimestamp = justAuthenticated ? parseInt(justAuthenticated, 10) : 0;
    const now = Date.now();
    
    // Skip check if we authenticated within the last 2 seconds
    if (skipInitialCheck || (authTimestamp && (now - authTimestamp) < 2000)) {
      setIsLoading(false);
      // Clear the flag after using it
      if (authTimestamp) {
        localStorage.removeItem('_justAuthenticated');
      }
      return;
    }

    // Add a safety timeout to ensure we don't stay in loading state forever
    const timeoutId = setTimeout(() => {
      console.warn('Auth check timeout - showing login page');
      setIsLoading(false);
      // If we've been loading for too long and have tokens, they might be invalid
      // But don't clear if we're already authenticated
      if (!isAuthenticated) {
        const token = localStorage.getItem('accessToken');
        if (token) {
          console.warn('Clearing potentially invalid tokens due to timeout');
          clearAuth();
        }
      }
    }, 5000); // 5 second safety timeout
    
    checkAuth().catch((error) => {
      console.error('Error in checkAuth:', error);
      setIsLoading(false);
      // Only clear tokens if we're not authenticated and got a 401
      if (!isAuthenticated && error?.response?.status === 401) {
        console.warn('Clearing tokens after 401 error');
        clearAuth();
      }
    }).finally(() => {
      clearTimeout(timeoutId);
    });
    
    return () => clearTimeout(timeoutId);
  }, [skipInitialCheck]);

  // Auto-refresh token before expiry
  useEffect(() => {
    if (!isAuthenticated) return;

    const interval = setInterval(async () => {
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          const response = await apiRefresh(refreshToken);
          localStorage.setItem('accessToken', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
        }
      } catch (error) {
        console.error('Token refresh failed:', error);
        await logout();
      }
    }, 14 * 60 * 1000); // Refresh every 14 minutes (tokens typically expire in 15)

    return () => clearInterval(interval);
  }, [isAuthenticated]);

  async function checkAuth() {
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setIsLoading(false);
        return;
      }

      // If we're already authenticated and have user data, skip validation
      // This prevents clearing auth immediately after registration
      if (isAuthenticated && user) {
        setIsLoading(false);
        return;
      }

      // Validate token (axios timeout is now configured in client.ts)
      try {
        const validation = await validateToken();
        
        if (validation?.valid) {
          // Get user info
          try {
            const userInfo = await me();
            
            setUser({
              id: userInfo?.id || userInfo?.userId,
              username: userInfo?.username || '',
              email: userInfo?.email || '',
              role: userInfo?.role || 'STUDENT',
              firstName: userInfo?.firstName,
              lastName: userInfo?.lastName,
            });
            setIsAuthenticated(true);
          } catch (err: any) {
            console.error('Failed to fetch user info:', err);
            // Only clear auth on 401 (unauthorized), not on network errors
            // This prevents clearing auth right after registration if network is slow
            if (err?.response?.status === 401) {
              clearAuth();
            } else {
              // For network errors or timeouts, keep the token but don't set user
              // This allows the app to continue working
              console.warn('Network error fetching user info, but keeping auth state');
              // Try to use localStorage data if available
              const storedUsername = localStorage.getItem('username');
              const storedRole = localStorage.getItem('role');
              if (storedUsername && storedRole) {
                setUser({
                  id: token.substring(0, 10), // Use a simple ID from token
                  username: storedUsername,
                  email: '',
                  role: storedRole,
                });
                setIsAuthenticated(true);
              }
            }
          }
        } else {
          // Try to refresh token
          const refreshToken = localStorage.getItem('refreshToken');
          if (refreshToken) {
            try {
              const response = await apiRefresh(refreshToken);
              localStorage.setItem('accessToken', response.accessToken);
              localStorage.setItem('refreshToken', response.refreshToken);
              await checkAuth(); // Retry
              return; // Don't execute finally block yet
            } catch (refreshErr: any) {
              console.error('Token refresh failed:', refreshErr);
              // Only clear auth on 401, not on network errors
              if (refreshErr?.response?.status === 401) {
                clearAuth();
              }
            }
          } else {
            clearAuth();
          }
        }
      } catch (validateErr: any) {
        console.error('Token validation failed:', validateErr);
        // Only clear auth on 401, not on network errors or timeouts
        // This prevents issues right after registration
        if (validateErr?.response?.status === 401) {
          clearAuth();
        } else {
          // For network errors, try to use stored data
          const storedUsername = localStorage.getItem('username');
          const storedRole = localStorage.getItem('role');
          if (storedUsername && storedRole && token) {
            console.warn('Network error validating token, but keeping auth state from localStorage');
            setUser({
              id: token.substring(0, 10),
              username: storedUsername,
              email: '',
              role: storedRole,
            });
            setIsAuthenticated(true);
          }
        }
      }
    } catch (error) {
      console.error('Auth check failed:', error);
      // Don't clear auth on unexpected errors, just log them
    } finally {
      setIsLoading(false);
    }
  }

  async function login(username: string, password: string) {
    try {
      const response = await apiLogin(username, password);
      
      // Store tokens
      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      localStorage.setItem('username', response.username);
      localStorage.setItem('role', response.role);
      
      // Set user state
      setUser({
        id: response.userId,
        username: response.username,
        email: '',
        role: response.role,
      });
      setIsAuthenticated(true);

      // Fetch complete user info
      try {
        const userInfo = await me();
        setUser({
          id: userInfo.id || response.userId,
          username: userInfo.username,
          email: userInfo.email,
          role: userInfo.role,
          firstName: userInfo.firstName,
          lastName: userInfo.lastName,
        });
      } catch (err) {
        console.error('Failed to fetch user info:', err);
      }
    } catch (error) {
      clearAuth();
      throw error;
    }
  }

  // Set auth state directly from an AuthResponse (useful after registration)
  function setAuthFromResponse(response: any) {
    if (!response) return

    try {
      const accessToken = response.accessToken
      const refreshToken = response.refreshToken

      if (accessToken) localStorage.setItem('accessToken', accessToken)
      if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
      if (response.username) localStorage.setItem('username', response.username)
      if (response.role) localStorage.setItem('role', response.role)
      
      // Set a flag to skip checkAuth for a short period after registration
      localStorage.setItem('_justAuthenticated', Date.now().toString())

      setUser({
        id: response.userId,
        username: response.username,
        email: response.email || '',
        role: response.role || 'STUDENT',
        firstName: response.firstName,
        lastName: response.lastName,
      })
      setIsAuthenticated(true)
      setIsLoading(false) // Ensure loading state is cleared
      setSkipInitialCheck(true) // Skip the initial checkAuth on next mount
    } catch (err) {
      console.error('Failed to set auth from response', err)
      setIsLoading(false) // Ensure loading state is cleared even on error
    }
  }

  async function logout() {
    try {
      await apiLogout();
    } catch (error) {
      console.error('Logout API call failed:', error);
    } finally {
      clearAuth();
    }
  }

  async function refreshAuth() {
    await checkAuth();
  }

  function clearAuth() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    setUser(null);
    setIsAuthenticated(false);
  }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, isLoading, login, logout, refreshAuth, setAuthFromResponse }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
