import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig(({ mode }) => {
  // Load env file based on `mode` in the current working directory.
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@assets': path.resolve(__dirname, '../attached_assets'),
      },
    },
    server: {
      port: 5000,
      host: '0.0.0.0',
      allowedHosts: true,
      proxy: {
        // AI Service - must be first to match /api/chat before general /api
        '/api/chat': {
          target: env.VITE_AI_API_SERVICE_URL || 'http://localhost:3001',
          changeOrigin: true,
        },
        '/api/health': {
          target: env.VITE_AI_API_SERVICE_URL || 'http://localhost:3001',
          changeOrigin: true,
        },
        // Listing Service
        '/api/student': {
          target: env.VITE_LISTING_API_URL || 'http://localhost:8100/api',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
        // Communication Service
        '/api/conversations': {
          target: env.VITE_COMMUNICATION_URL || 'http://localhost:8200/api',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
        '/api/messages': {
          target: env.VITE_COMMUNICATION_URL || 'http://localhost:8200/api',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
        // Main Backend (Auth, Users, Admin) - must be last as fallback
        '/api': {
          target: env.VITE_BACKEND_API_BASE_URL || 'http://localhost:8080/api',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    }
  }
})
