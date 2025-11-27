export const API_CONFIG = {
  USE_MOCK_API: true,
  
  BACKEND_URL: import.meta.env.VITE_API_BASE_URL || 'http://54.193.178.118:8080/api',
  LISTING_API_URL: import.meta.env.VITE_LISTING_API_URL || 'http://54.193.178.118:8100/api',
  COMMUNICATION_URL: import.meta.env.VITE_COMMUNICATION_URL || 'http://54.193.178.118:8200/api',
  
  TIMEOUT: 5000,
};

export function isUsingMockApi(): boolean {
  return API_CONFIG.USE_MOCK_API;
}

export function toggleMockApi(useMock: boolean): void {
  API_CONFIG.USE_MOCK_API = useMock;
}
