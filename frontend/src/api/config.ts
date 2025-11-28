export const API_CONFIG = {
  USE_MOCK_API: false,  // Disable mock API to use real backend
  
  // Unified backend URL - all services consolidated
  BACKEND_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  
  // AI Service URL (optional, for AI-powered features)
  AI_SERVICE_URL: import.meta.env.VITE_AI_API_SERVICE_URL || 'http://localhost:3001',
  
  TIMEOUT: 5000,
};

export function isUsingMockApi(): boolean {
  return API_CONFIG.USE_MOCK_API;
}

export function toggleMockApi(useMock: boolean): void {
  API_CONFIG.USE_MOCK_API = useMock;
}
