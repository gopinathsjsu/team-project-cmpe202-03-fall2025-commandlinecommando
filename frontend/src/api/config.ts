export const API_CONFIG = {
  USE_MOCK_API: true,

  BACKEND_URL: import.meta.env.VITE_BACKEND_API_BASE_URL || 'http://localhost:8080/api',
  LISTING_API_URL: import.meta.env.VITE_LISTING_API_URL || 'http://localhost:8100/api',
  COMMUNICATION_URL: import.meta.env.VITE_COMMUNICATION_URL || 'http://localhost:8200/api',
  AI_SERVICE_URL: import.meta.env.VITE_AI_API_SERVICE_URL || 'http://localhost:3001',

  TIMEOUT: 5000,
};

export function isUsingMockApi(): boolean {
  return API_CONFIG.USE_MOCK_API;
}

export function toggleMockApi(useMock: boolean): void {
  API_CONFIG.USE_MOCK_API = useMock;
}
