import React, { useState } from 'react'
import { requestPasswordReset, resetPassword } from '../api/auth'

export function ForgotPasswordForm({ onBackToLogin, onResetSuccess }: { onBackToLogin?: () => void; onResetSuccess?: () => void }) {
  const [step, setStep] = useState<'request' | 'reset'>('request')
  const [email, setEmail] = useState('')
  const [token, setToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  const handleRequestReset = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setSuccess(null)
    
    if (!email) {
      setError('Email is required')
      return
    }

    setLoading(true)
    try {
      const response = await requestPasswordReset(email)
      // In development, the backend returns the token directly
      // In production, this would be sent via email
      if (response.token) {
        setToken(response.token) // Pre-fill token for development
        setSuccess('Password reset token generated. Enter your new password below.')
        setStep('reset')
      } else {
        setSuccess('Password reset link sent to your email. Please check your inbox.')
        setTimeout(() => {
          setStep('reset')
          setSuccess('Enter the reset token from your email and your new password.')
        }, 2000)
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to send reset email')
    } finally {
      setLoading(false)
    }
  }

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setSuccess(null)

    if (!token || !newPassword || !confirmPassword) {
      setError('All fields are required')
      return
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    if (newPassword.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }

    setLoading(true)
    try {
      await resetPassword(token, newPassword)
      setSuccess('Password reset successful! You can now login with your new password.')
      setTimeout(() => {
        if (onResetSuccess) onResetSuccess()
        else if (onBackToLogin) onBackToLogin()
      }, 2000)
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to reset password')
    } finally {
      setLoading(false)
    }
  }

  if (step === 'reset') {
    return (
      <form className="p-6 bg-white rounded-lg shadow" onSubmit={handleResetPassword}>
        <h3 className="text-lg font-medium mb-4">Reset Password</h3>
        <div className="space-y-3">
          <input
            className="w-full p-2 border rounded"
            placeholder="Reset Token"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            required
          />
          <input
            className="w-full p-2 border rounded"
            placeholder="New Password"
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
          />
          <input
            className="w-full p-2 border rounded"
            placeholder="Confirm New Password"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
          {error && <div className="text-sm text-red-600">{error}</div>}
          {success && <div className="text-sm text-green-600">{success}</div>}
          <button
            className="w-full p-2 bg-blue-600 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed"
            type="submit"
            disabled={loading}
          >
            {loading ? 'Resetting...' : 'Reset Password'}
          </button>
          <button
            type="button"
            className="w-full p-2 border border-gray-300 text-gray-700 rounded bg-white hover:bg-gray-50"
            onClick={() => {
              setStep('request')
              setError(null)
              setSuccess(null)
            }}
          >
            Back
          </button>
        </div>
      </form>
    )
  }

  return (
    <form className="p-6 bg-white rounded-lg shadow" onSubmit={handleRequestReset}>
      <h3 className="text-lg font-medium mb-4">Forgot Password</h3>
      <p className="text-sm text-gray-600 mb-4">
        Enter your email address and we'll send you a link to reset your password.
      </p>
      <div className="space-y-3">
        <input
          className="w-full p-2 border rounded"
          placeholder="Email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        {error && <div className="text-sm text-red-600">{error}</div>}
        {success && <div className="text-sm text-green-600">{success}</div>}
        <button
          className="w-full p-2 bg-blue-600 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed"
          type="submit"
          disabled={loading}
        >
          {loading ? 'Sending...' : 'Send Reset Link'}
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

