import React, { useState, useEffect } from 'react'
import { register } from '../api/auth'
import { useAuth } from '../context/AuthContext'

export function RegisterForm({ onRegister, onBackToLogin }: { onRegister?: (role?: string) => void; onBackToLogin?: () => void }) {
  const { login, setAuthFromResponse } = useAuth() as any
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    role: 'STUDENT' // Default role
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [confirmPassword, setConfirmPassword] = useState('')

  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault()
    setError(null)

    console.log('Registration form submitted', formData)

    // Validation
    if (!formData.username || !formData.password || !formData.email || !formData.firstName || !formData.lastName) {
      setError('All fields are required')
      console.log('Validation failed: missing fields')
      return
    }

    if (formData.password !== confirmPassword) {
      setError('Passwords do not match')
      console.log('Validation failed: passwords do not match')
      return
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters')
      console.log('Validation failed: password too short')
      return
    }

    console.log('Starting registration API call...')
    setLoading(true)
      try {
        // Register the user
        // Ensure payload fields match backend DTO (firstName, lastName)
        const payload = {
          username: formData.username,
          password: formData.password,
          email: formData.email,
          firstName: formData.firstName,
          lastName: formData.lastName,
          role: formData.role
        }
        console.log('Calling register API with:', payload)
        const result = await register(payload)
        console.log('Registration successful:', result)

        // Use registration response to set auth state directly (no extra login request)
        try {
          // eslint-disable-next-line no-console
          console.log('Setting auth from registration response')
          if (setAuthFromResponse) {
            setAuthFromResponse(result)
          }
        } catch (e) {
          console.error('Failed to set auth from response:', e)
          // Fallback: attempt login
          // eslint-disable-next-line no-console
          console.log('Falling back to login() after registration')
          await login(formData.username, formData.password)
        }

        // Pass the role from the response to the callback
        // The useEffect in App.tsx will also handle navigation automatically
        if (onRegister) {
          onRegister(result?.role || formData.role)
        }
      } catch (err: any) {
      console.error('Registration error:', err)
      setError(err?.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line no-console
    console.log('RegisterForm mounted')
  }, [])

  return (
    <form className="p-6 bg-white rounded-lg shadow" onSubmit={handleSubmit}>
      <h3 className="text-lg font-medium mb-4">Create Account</h3>
      <div className="space-y-3">
        <input
          className="w-full p-2 border rounded"
          placeholder="First Name"
          value={formData.firstName}
          onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
        />
        <input
          className="w-full p-2 border rounded"
          placeholder="Last Name"
          value={formData.lastName}
          onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
        />
        <input
          className="w-full p-2 border rounded"
          placeholder="Username"
          value={formData.username}
          onChange={(e) => setFormData({ ...formData, username: e.target.value })}
        />
        <input
          className="w-full p-2 border rounded"
          placeholder="Email"
          type="email"
          value={formData.email}
          onChange={(e) => setFormData({ ...formData, email: e.target.value })}
        />
        <input
          className="w-full p-2 border rounded"
          placeholder="Password"
          type="password"
          value={formData.password}
          onChange={(e) => setFormData({ ...formData, password: e.target.value })}
        />
        <input
          className="w-full p-2 border rounded"
          placeholder="Confirm Password"
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
        <select
          className="w-full p-2 border rounded"
          value={formData.role}
          onChange={(e) => setFormData({ ...formData, role: e.target.value })}
        >
          <option value="STUDENT">Student</option>
          <option value="ADMIN">Admin</option>
        </select>
        {error && <div className="text-sm text-red-600">{error}</div>}
        <button
          className="w-full p-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          type="button"
          disabled={loading}
          onClick={(e) => {
            // Prevent default navigation and call the submit handler explicitly
            e.preventDefault()
            // eslint-disable-next-line no-console
            console.log('Sign up button clicked (explicit)', { username: formData.username })
            void handleSubmit(e)
          }}
        >
          {loading ? 'Creating account...' : 'Sign up'}
        </button>
        <button
          type="button"
          className="w-full p-2 border border-gray-300 text-gray-700 rounded bg-white hover:bg-gray-50"
          onClick={onBackToLogin}
        >
          Back to Sign In
        </button>
      </div>
    </form>
  )
}
