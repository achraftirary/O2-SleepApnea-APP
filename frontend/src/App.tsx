import { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom'
import { Dashboard } from './components/Dashboard'
import { RentalManagement } from './components/RentalManagement'
import { InvoiceManagement } from './components/InvoiceManagement'
import { Clients } from './components/Clients'
import { Inventory } from './components/Inventory'
import { Login } from './components/Login'
import './styles/main.css'

export function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Check if user is already logged in
    const authToken = localStorage.getItem('authToken')
    if (authToken) {
      setIsAuthenticated(true)
    }
    setLoading(false)
  }, [])

  if (loading) {
    return null // Or a loading spinner
  }

  if (!isAuthenticated) {
    return (
      <Login
        onLogin={() => {
          setIsAuthenticated(true)
        }}
      />
    )
  }

  return (
    <Router>
      <AppContent sidebarOpen={sidebarOpen} setSidebarOpen={setSidebarOpen} onLogout={() => setIsAuthenticated(false)} />
    </Router>
  )
}

function AppContent({ sidebarOpen, setSidebarOpen, onLogout }: { sidebarOpen: boolean; setSidebarOpen: (open: boolean) => void; onLogout: () => void }) {
  const navigate = useNavigate()

  const handleLogout = () => {
    localStorage.removeItem('authToken')
    localStorage.removeItem('username')
    onLogout()
  }

  return (
    <div className="app-shell flex h-screen text-slate-100">
        <div className="pointer-events-none absolute inset-0 opacity-70">
          <div className="absolute left-10 top-16 h-56 w-56 rounded-full bg-sky-400/10 blur-3xl animate-drift" />
          <div className="absolute right-10 top-20 h-72 w-72 rounded-full bg-emerald-400/10 blur-3xl animate-drift" />
          <div className="absolute left-1/2 bottom-4 h-80 w-80 -translate-x-1/2 rounded-full bg-indigo-400/10 blur-3xl animate-floatSoft" />
        </div>
        {/* Sidebar */}
        <aside
          className={`${
            sidebarOpen ? 'w-64' : 'w-20'
          } glass-panel relative z-10 flex flex-col transition-all duration-300 border-r border-white/10`}
        >
          {/* Logo */}
          <div className="p-4 flex items-center justify-between border-b border-white/10">
            {sidebarOpen ? (
              <div>
                <p className="premium-badge mb-2">Premium Ops</p>
                <h1 className="text-xl font-black tracking-tight">O2 Medical</h1>
              </div>
            ) : (
              <div className="h-10 w-10 rounded-2xl bg-white/10 border border-white/10 flex items-center justify-center font-black">O2</div>
            )}
            <button
              onClick={() => setSidebarOpen(!sidebarOpen)}
              className="h-10 w-10 rounded-2xl border border-white/10 bg-white/10 hover:bg-white/20 transition-all"
            >
              {sidebarOpen ? '◀' : '▶'}
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 p-3 space-y-2 overflow-y-auto premium-scrollbar">
            <NavLink to="/" sidebarOpen={sidebarOpen} label="Dashboard" />
            <NavLink to="/rentals" sidebarOpen={sidebarOpen} label="Rentals" />
            <NavLink to="/invoices" sidebarOpen={sidebarOpen} label="Invoices" />
            <NavLink to="/inventory" sidebarOpen={sidebarOpen} label="Inventory" />
            <NavLink to="/clients" sidebarOpen={sidebarOpen} label="Clients" />
          </nav>

          {/* User Profile */}
          <div className="p-4 border-t border-white/10">
            {sidebarOpen && (
              <div className="text-sm glass-card rounded-[20px] p-4">
                <p className="text-xs uppercase tracking-[0.24em] text-slate-300/70">Current Operator</p>
                <p className="font-semibold mt-2">Agent Name</p>
                <button onClick={handleLogout} className="premium-button premium-button--secondary mt-3 w-full">Logout</button>
              </div>
            )}
          </div>
        </aside>

        {/* Main Content */}
        <main className="relative z-10 flex-1 overflow-auto premium-scrollbar">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/rentals" element={<RentalManagement />} />
            <Route path="/invoices" element={<InvoiceManagement />} />
            <Route path="/inventory" element={<Inventory />} />
            <Route path="/clients" element={<Clients />} />
            <Route path="*" element={<Dashboard />} />
          </Routes>
        </main>
      </div>
    )
}

function NavLink({ to, sidebarOpen, label }: { to: string; sidebarOpen: boolean; label: string }) {
  return (
    <Link
      to={to}
      className="group flex items-center gap-3 px-4 py-3 rounded-2xl border border-transparent bg-white/0 hover:bg-white/8 hover:border-white/10 transition-all duration-200"
    >
      {sidebarOpen && <span className="text-sm font-medium tracking-wide">{label}</span>}
    </Link>
  )
}

export default App
