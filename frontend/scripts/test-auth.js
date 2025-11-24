// Simple node script to test POST /auth/login
// Usage:
// USERNAME=john_student PASSWORD=Test1234! node frontend/scripts/test-auth.js

const baseUrl = process.env.BASE_URL || 'http://localhost:8080/api'
const username = process.env.USERNAME
const password = process.env.PASSWORD

if (!username || !password) {
  console.error('Provide USERNAME and PASSWORD env vars. Example:')
  console.error('  USERNAME=john_student PASSWORD=Test1234! node frontend/scripts/test-auth.js')
  process.exit(1)
}

(async () => {
  try {
    const res = await fetch(`${baseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })

    console.log('Response status:', res.status)
    const data = await res.text()
    try {
      console.log('Response body:', JSON.parse(data))
    } catch (e) {
      console.log('Response body (raw):', data)
    }
  } catch (err) {
    console.error('Request failed:', err)
    process.exit(2)
  }
})()
