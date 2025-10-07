import React from 'react'

export function LoginForm() {
  return (
    <div className="p-6 bg-white rounded-lg shadow">
      <h3 className="text-lg font-medium mb-4">Sign in</h3>
      {/* Minimal form placeholders - implement fields later */}
      <div className="space-y-3">
        <input className="w-full p-2 border rounded" placeholder="Email" />
        <input className="w-full p-2 border rounded" placeholder="Password" type="password" />
        <button className="w-full p-2 bg-blue-600 text-white rounded">Sign in</button>
        <button
          type="button"
          className="w-full p-2 border border-blue-600 text-blue-600 rounded bg-white hover:bg-blue-50"
          onClick={() => {
            // TODO: navigate to sign-up flow / open modal
            console.log('Sign up clicked')
          }}
        >
          Create account
        </button>
      </div>
    </div>
  )
}
