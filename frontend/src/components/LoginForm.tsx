import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'

export function LoginForm({ onLogin, onSignUp, onForgotPassword }: { onLogin?: () => void; onSignUp?: () => void; onForgotPassword?: () => void }) {
  const { login } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login(username, password)
      if (onLogin) onLogin()
    } catch (err: any) {
      // Handle 401 or other authentication errors
      if (err?.response?.status === 401 || err?.response?.status === 403) {
        setError('Incorrect details')
      } else {
        setError(err?.response?.data?.message || err?.response?.data?.error || 'Login failed')
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line no-console
    console.log('LoginForm mounted')
  }, [])

  return (
    <form className="p-6 bg-white rounded-lg shadow" onSubmit={handleSubmit}>
      <h3 className="text-lg font-medium mb-4">Sign in</h3>
      <div className="space-y-3">
        <input
          className="w-full p-2 border rounded"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <input
          className="w-full p-2 border rounded"
          placeholder="Password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button
          className="w-full p-2 bg-blue-600 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed"
          type="button"
          disabled={loading}
          onClick={(e) => {
            e.preventDefault()
            // eslint-disable-next-line no-console
            console.log('Sign in button clicked (explicit)', { username })
            void handleSubmit(e)
          }}
        >
          {loading ? 'Signing in...' : 'Sign in'}
        </button>
        {error && <div className="text-sm text-red-600 mt-2">{error}</div>}
        <div className="text-right">
          <button
            type="button"
            className="text-sm text-blue-600 hover:text-blue-800 hover:underline"
            onClick={onForgotPassword}
          >
            Forgot password?
          </button>
        </div>
        <button
          type="button"
          className="w-full p-2 border border-blue-600 text-blue-600 rounded bg-white hover:bg-blue-50"
          onClick={onSignUp}
        >
          Create account
        </button>
      </div>
    </form>
  )
}
