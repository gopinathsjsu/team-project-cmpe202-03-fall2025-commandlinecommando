// Route configuration placeholder — add routes when pages exist
import React from 'react'
import { BrowserRouter } from 'react-router-dom'

export default function RouterProvider({ children }: { children: React.ReactNode }) {
  return <BrowserRouter>{children}</BrowserRouter>
}
