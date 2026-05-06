import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAppStore, DashboardData } from '../store/appStore'
import { Card, KPICard, AlertBanner, Button, Badge } from './shared'

export function Dashboard() {
  const { dashboardData, loading, error, fetchDashboard, clearError } = useAppStore()
  const navigate = useNavigate()

  useEffect(() => {
    fetchDashboard()
    const interval = setInterval(fetchDashboard, 60000) // Refresh every minute
    return () => clearInterval(interval)
  }, [fetchDashboard])

  if (loading && !dashboardData) {
    return (
      <div className="min-h-screen flex items-center justify-center px-6">
        <Card className="max-w-lg w-full text-center py-16 animate-floatSoft">
          <p className="premium-badge mx-auto mb-4">Premium workspace</p>
          <h1 className="text-3xl font-black tracking-tight">Loading your command center</h1>
          <p className="text-slate-300/75 mt-3">Pulling live rentals, invoices, and inventory telemetry.</p>
        </Card>
      </div>
    )
  }

  if (!dashboardData) {
    return (
      <div className="min-h-screen flex items-center justify-center px-6">
        <Card className="max-w-lg w-full text-center py-16">
          <p className="premium-badge mx-auto mb-4">Workspace offline</p>
          <h1 className="text-3xl font-black tracking-tight">Failed to load dashboard data</h1>
          <p className="text-slate-300/75 mt-3">Check the backend and reload once the API is available.</p>
        </Card>
      </div>
    )
  }

  const dash = dashboardData

  return (
    <div className="relative min-h-screen px-4 py-4 md:px-8 md:py-8">
      <div className="mx-auto max-w-7xl space-y-8">
        {/* Hero */}
        <div className="glass-card panel-glow rounded-[32px] p-6 md:p-8 overflow-hidden relative">
          <div className="absolute -right-10 -top-10 h-40 w-40 rounded-full bg-sky-400/15 blur-3xl animate-drift" />
          <div className="absolute -left-8 bottom-0 h-36 w-36 rounded-full bg-emerald-400/15 blur-3xl animate-drift" />
          <div className="relative flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-2xl">
              <p className="premium-badge mb-4">Agent command center</p>
              <h1 className="text-4xl md:text-5xl font-black tracking-tight">Operate rentals with real-time clarity.</h1>
              <p className="text-slate-200/70 mt-4 text-base md:text-lg max-w-xl">
                Premium live view for deployments, pickups, invoices, and inventory pressure points.
              </p>
            </div>
            <div className="flex flex-wrap gap-3">
              <Button variant="secondary" size="sm" onClick={fetchDashboard}>
                ↻ Refresh now
              </Button>
              <Button variant="primary" size="sm" onClick={() => navigate('/rentals', { state: { openCreateRental: true } })}>
                ⚡ Quick deploy
              </Button>
            </div>
          </div>
        </div>

        {/* Error Banner */}
        {error && <AlertBanner type="danger" title="Error" message={error} onClose={clearError} />}

        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <Card className="border border-white/10">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Live status</p>
            <p className="mt-3 text-2xl font-black">Operations online</p>
            <p className="mt-2 text-sm text-slate-300/70">Auto refresh every 60 seconds.</p>
          </Card>
          <Card className="border border-white/10">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Focus</p>
            <p className="mt-3 text-2xl font-black">Rentals, cashflow, stock</p>
            <p className="mt-2 text-sm text-slate-300/70">Designed for fast field operations.</p>
          </Card>
          <Card className="border border-white/10">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Today</p>
            <p className="mt-3 text-2xl font-black">{new Date().toLocaleDateString()}</p>
            <p className="mt-2 text-sm text-slate-300/70">Premium dashboard snapshot.</p>
          </Card>
        </div>

        {/* Top KPIs - Critical Metrics */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          <KPICard
            label="Active Rentals"
            value={dash.totalActiveRentals}
            variant="default"
          />
          <KPICard
            label="Overdue Pickups"
            value={dash.totalOverduePickups}
            variant={dash.totalOverduePickups > 0 ? 'danger' : 'success'}
          />
          <KPICard
            label="Unpaid Invoices"
            value={dash.totalUnpaidInvoices}
            variant={dash.totalUnpaidInvoices > 0 ? 'warning' : 'success'}
          />
          <KPICard
            label="Outstanding Revenue"
            value={`€${dash.totalUnpaidAmount.toFixed(2)}`}
            variant={dash.totalUnpaidAmount > 0 ? 'warning' : 'success'}
          />
        </div>

        {/* Financial Summary */}
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
          <Card className="border border-sky-300/15">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Expected Daily Revenue</p>
            <p className="mt-3 text-3xl font-black text-sky-100">€{dash.dailyRevenueExpected.toFixed(2)}</p>
          </Card>
          <Card className="border border-emerald-300/15">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Collected Today</p>
            <p className="mt-3 text-3xl font-black text-emerald-100">€{dash.dailyRevenueCollected.toFixed(2)}</p>
          </Card>
          <Card className="border border-violet-300/15">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Collection Rate</p>
            <p className="mt-3 text-3xl font-black text-violet-100">{dash.collectionRate.toFixed(1)}%</p>
          </Card>
        </div>

        {/* Urgent Items Section */}
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {/* Pickups Today */}
          <Card className="rounded-[28px]">
            <h2 className="text-lg font-black mb-4 flex items-center gap-2">
              Pickups Today
              <Badge variant={dash.pickupsToday.length > 0 ? 'danger' : 'success'}>
                {dash.pickupsToday.length}
              </Badge>
            </h2>
            {dash.pickupsToday.length === 0 ? (
              <p className="text-slate-300/70 text-sm">No pickups scheduled for today</p>
            ) : (
              <div className="space-y-3">
                {dash.pickupsToday.slice(0, 5).map((contract) => (
                  <div key={contract.id} className="rounded-2xl border border-white/10 bg-white/5 p-4">
                    <p className="font-semibold text-slate-50">{contract.clientName}</p>
                    <p className="text-sm text-slate-300/70 mt-1">{contract.deviceSerialNumber}</p>
                    <Button size="sm" variant="primary" className="mt-3" onClick={() => navigate('/rentals')}>
                      Mark Picked Up
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </Card>

          {/* Overdue Rentals */}
          <Card className="rounded-[28px]">
            <h2 className="text-lg font-black mb-4 flex items-center gap-2">
              Overdue Pickups
              <Badge variant={dash.overdueRentals.length > 0 ? 'danger' : 'success'}>
                {dash.overdueRentals.length}
              </Badge>
            </h2>
            {dash.overdueRentals.length === 0 ? (
              <p className="text-slate-300/70 text-sm">No overdue rentals</p>
            ) : (
              <div className="space-y-3">
                {dash.overdueRentals.slice(0, 5).map((contract) => (
                  <div key={contract.id} className="rounded-2xl border border-rose-300/15 bg-rose-500/8 p-4">
                    <p className="font-semibold text-rose-50">{contract.clientName}</p>
                    <p className="text-sm text-rose-100/75 mt-1">
                      {contract.daysOverdue} days overdue · {contract.deviceSerialNumber}
                    </p>
                    <Button size="sm" variant="danger" className="mt-3" onClick={() => navigate('/clients')}>
                      Contact Client
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>

        {/* Critical Alerts */}
        {dash.criticalAlerts.length > 0 && (
          <div>
            <h2 className="text-lg font-black mb-4 flex items-center gap-2">
              Critical Alerts
              <Badge variant="danger">{dash.criticalAlerts.length}</Badge>
            </h2>
            <div className="space-y-3">
              {dash.criticalAlerts.slice(0, 5).map((alert) => (
                <AlertBanner
                  key={alert.id}
                  type={alert.severity === 'CRITICAL' ? 'danger' : 'warning'}
                  title={alert.title}
                  message={alert.description}
                />
              ))}
            </div>
          </div>
        )}

        {/* Inventory Status */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <Card className="border border-emerald-300/15">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65 font-semibold">Available Devices</p>
            <p className="text-4xl font-black text-emerald-100 mt-3">{dash.totalDevicesAvailable}</p>
          </Card>
          <Card className="border border-sky-300/15">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65 font-semibold">Deployed Devices</p>
            <p className="text-4xl font-black text-sky-100 mt-3">{dash.totalDevicesDeployed}</p>
          </Card>
          <Card className="border border-amber-300/15">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65 font-semibold">In Maintenance</p>
            <p className="text-4xl font-black text-amber-100 mt-3">{dash.totalDevicesInMaintenance}</p>
          </Card>
        </div>

        {/* Low Stock & Maintenance */}
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2 pb-10">
          {/* Low Stock */}
          <Card className="rounded-[28px]">
            <h2 className="text-lg font-black mb-4">Low Stock Consumables</h2>
            {dash.lowStockDevices.length === 0 ? (
              <p className="text-slate-300/70 text-sm">All consumables adequately stocked</p>
            ) : (
              <div className="space-y-2">
                {dash.lowStockDevices.map((device) => (
                  <div key={device.id} className="flex justify-between items-center border-b border-white/10 pb-3">
                    <span className="font-medium text-slate-50">{device.displayName}</span>
                    <Badge variant="warning">{device.quantityInStock} units</Badge>
                  </div>
                ))}
              </div>
            )}
          </Card>

          {/* Maintenance Required */}
          <Card className="rounded-[28px]">
            <h2 className="text-lg font-black mb-4">Devices Needing Maintenance</h2>
            {dash.devicesNeedingMaintenance.length === 0 ? (
              <p className="text-slate-300/70 text-sm">No maintenance required</p>
            ) : (
              <div className="space-y-2">
                {dash.devicesNeedingMaintenance.slice(0, 5).map((device) => (
                  <div key={device.id} className="flex justify-between items-center border-b border-white/10 pb-3">
                    <span className="font-medium text-slate-50">{device.displayName}</span>
                    <span className="text-sm text-slate-300/70">{device.totalRentalDays} days used</span>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>
      </div>
    </div>
  )
}
