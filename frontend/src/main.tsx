import React from 'react'
import { createRoot } from 'react-dom/client'
// Ensure that App.tsx exists in the same directory, or update the path if necessary
import App from './App'
import './styles/index.css'
import { AuthProvider } from './context/AuthContext'

// add global class so @layer components .app-body applies
document.documentElement.classList.add('app-body')

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </React.StrictMode>
)
