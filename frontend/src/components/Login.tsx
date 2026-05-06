import { useState } from 'react'
import axios from 'axios'
import { Card, Button, AlertBanner } from './shared'

interface LoginProps {
  onLogin: (username: string) => void
}

interface LoginResponse {
  token: string
  username: string
  firstName: string
  lastName: string
  role: string
}

export function Login({ onLogin }: LoginProps) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      if (!username.trim() || !password.trim()) {
        setError('Please enter username and password')
        setLoading(false)
        return
      }

      // Call backend authentication endpoint
      const response = await axios.post<LoginResponse>('/api/auth/login', {
        username: username.trim(),
        password: password.trim(),
      })

      const { token, username: responseUsername, firstName, lastName } = response.data

      // Store auth data in localStorage
      localStorage.setItem('authToken', token)
      localStorage.setItem('username', responseUsername)
      localStorage.setItem('fullName', `${firstName} ${lastName}`)

      onLogin(responseUsername)
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Login failed. Please check your credentials.'
      setError(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 relative overflow-hidden">
      {/* Background effects */}
      <div className="pointer-events-none absolute inset-0 opacity-70">
        <div className="absolute left-10 top-16 h-56 w-56 rounded-full bg-sky-400/10 blur-3xl animate-drift" />
        <div className="absolute right-10 top-20 h-72 w-72 rounded-full bg-emerald-400/10 blur-3xl animate-drift" />
        <div className="absolute left-1/2 bottom-4 h-80 w-80 -translate-x-1/2 rounded-full bg-indigo-400/10 blur-3xl animate-floatSoft" />
      </div>

      {/* Login Card */}
      <div className="relative z-10 w-full max-w-md px-4">
        <Card className="rounded-[28px] border border-white/10 bg-gradient-to-br from-white/8 to-white/5 backdrop-blur-xl">
          <div className="text-center mb-8">
            <div className="inline-block mb-4">
              <div className="h-12 w-12 rounded-2xl bg-gradient-to-br from-sky-400 to-cyan-300 flex items-center justify-center font-black text-slate-950">
                O2
              </div>
            </div>
            <h1 className="text-2xl font-black tracking-tight text-slate-50">O2 Medical</h1>
            <p className="text-sm text-slate-300/70 mt-2">Premium Operations Dashboard</p>
          </div>

          {error && <AlertBanner type="danger" title="Login Failed" message={error} />}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs uppercase tracking-[0.14em] text-slate-300/70 font-semibold mb-2">
                Username
              </label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter username"
                autoComplete="username"
                className="w-full px-4 py-2.5 rounded-lg bg-white/15 border border-white/25 text-white placeholder-slate-300/50 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:border-transparent transition-all"
              />
            </div>

            <div>
              <label className="block text-xs uppercase tracking-[0.14em] text-slate-300/70 font-semibold mb-2">
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter password"
                autoComplete="current-password"
                className="w-full px-4 py-2.5 rounded-lg bg-white/15 border border-white/25 text-white placeholder-slate-300/50 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:border-transparent transition-all"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-6 px-4 py-3 rounded-lg bg-gradient-to-r from-sky-500 to-cyan-400 text-slate-950 font-semibold hover:shadow-lg hover:shadow-sky-500/50 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <p className="text-xs text-slate-400/60 text-center mt-6 leading-relaxed">
            Agent: hamouda / hamouda123<br/>
            Doctor: ahmed / ahmed123
          </p>
        </Card>
      </div>
    </div>
  )
}
