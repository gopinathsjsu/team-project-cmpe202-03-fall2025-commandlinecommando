export const API_CONFIG = {
  USE_MOCK_API: false,  // Disable mock API to use real backend
  
  // Unified backend URL - use relative path so Nginx can proxy it
  // Nginx proxies /api/ to backend:8080 in production
  // For local dev, set VITE_API_BASE_URL=http://localhost:8080/api
  BACKEND_URL: import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '/api',
  
  // AI Service URL - use relative path (Nginx proxies /ai/ to AI service)
  // For local dev, set VITE_AI_API_SERVICE_URL=http://localhost:3001
  AI_SERVICE_URL: import.meta.env.VITE_AI_API_SERVICE_URL || import.meta.env.VITE_AI_SERVICE_URL || '/ai',
  
  TIMEOUT: 5000,
};

export function isUsingMockApi(): boolean {
  return API_CONFIG.USE_MOCK_API;
}

export function toggleMockApi(useMock: boolean): void {
  API_CONFIG.USE_MOCK_API = useMock;
}
