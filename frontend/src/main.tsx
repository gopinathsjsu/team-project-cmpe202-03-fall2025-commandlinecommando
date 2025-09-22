import React from 'react'
import { createRoot } from 'react-dom/client'
// Ensure that App.tsx exists in the same directory, or update the path if necessary
import App from './App'
import './styles/index.css'

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
