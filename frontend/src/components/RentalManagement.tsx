import { useMemo, useState, useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Card, Button, Badge, AlertBanner } from './shared'
import apiClient from '../store/appStore'

interface RentalContract {
  id: number
  contractNumber: string
  clientName: string
  deviceSerialNumber: string
  contractStatus: string
  rentalStartDate: string
  expectedReturnDate: string
  pickupDate?: string
  isOverdue: boolean
  daysOverdue: number
}

export function RentalManagement() {
  const [rentals, setRentals] = useState<RentalContract[]>([])
  const [selectedRental, setSelectedRental] = useState<RentalContract | null>(null)
  const [deploymentNotes, setDeploymentNotes] = useState('')
  const [loading, setLoading] = useState(false)
  const [notice, setNotice] = useState<{ type: 'info' | 'success' | 'warning' | 'danger'; title: string; message: string } | null>(null)
  const [dateDialog, setDateDialog] = useState<{
    mode: 'pickup' | 'complete'
    rentalId: number
    rentalLabel: string
    value: string
  } | null>(null)
  const [activeFilter, setActiveFilter] = useState<'ALL' | 'PENDING_DEPLOYMENT' | 'ACTIVE_RENTAL' | 'PENDING_PICKUP'>('ALL')
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [createDraft, setCreateDraft] = useState({
    clientId: '',
    deviceId: '',
    startDate: new Date().toISOString().slice(0, 10),
    endDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10),
    dailyRate: '25.00',
    agentId: '1',
  })
  const location = useLocation()
  const navigate = useNavigate()

  const statusVariantMap = {
    PENDING_DEPLOYMENT: 'warning',
    ACTIVE_RENTAL: 'primary',
    PENDING_PICKUP: 'success',
    COMPLETED: 'success',
    CANCELLED: 'danger',
  } as const

  const filteredRentals = useMemo(() => {
    if (activeFilter === 'ALL') {
      return rentals
    }

    return rentals.filter((rental) => rental.contractStatus === activeFilter)
  }, [activeFilter, rentals])

  useEffect(() => {
    fetchActiveRentals()
  }, [])

  useEffect(() => {
    const openCreateRental = (location.state as { openCreateRental?: boolean } | null)?.openCreateRental
    if (openCreateRental) {
      setShowCreateForm(true)
      navigate('/rentals', { replace: true, state: {} })
    }
  }, [location.state, navigate])

  async function fetchActiveRentals() {
    try {
      const [pendingDeploymentResp, activeResp, pendingPickupResp] = await Promise.all([
        apiClient.get('/rental-contracts/pending-deployment'),
        apiClient.get('/rental-contracts/active'),
        apiClient.get('/rental-contracts/pending-pickup'),
      ])

      const merged = [
        ...pendingDeploymentResp.data,
        ...activeResp.data,
        ...pendingPickupResp.data,
      ].filter((contract, index, array) => array.findIndex((candidate: RentalContract) => candidate.id === contract.id) === index)

      setRentals(merged)
      setSelectedRental((current) => current && merged.some((contract) => contract.id === current.id) ? current : merged[0] || null)
    } catch (err) {
      console.error('Failed to load rentals', err)
    }
  }

  async function exportList() {
    try {
      const rows = filteredRentals
      const csv = [
        ['id', 'contractNumber', 'clientName', 'deviceSerialNumber', 'status', 'startDate', 'expectedReturnDate'],
        ...rows.map((r) => [r.id, r.contractNumber, r.clientName, r.deviceSerialNumber, r.contractStatus, r.rentalStartDate, r.expectedReturnDate]),
      ]
        .map((r) => r.join(','))
        .join('\n')

      const blob = new Blob([csv], { type: 'text/csv' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `rentals_export_${new Date().toISOString()}.csv`
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)
      setNotice({
        type: 'info',
        title: 'Export complete',
        message: `Exported ${rows.length} rental${rows.length === 1 ? '' : 's'}`,
      })
    } catch (err) {
      console.error('Export failed', err)
      setNotice({ type: 'danger', title: 'Export failed', message: 'Unable to export the current rental list.' })
    }
  }

  async function submitCreateRental() {
    try {
      if (!createDraft.clientId || !createDraft.deviceId || !createDraft.startDate || !createDraft.endDate || !createDraft.dailyRate || !createDraft.agentId) {
        setNotice({
          type: 'warning',
          title: 'Missing fields',
          message: 'Client, device, dates, daily rate, and agent are required to create a rental.',
        })
        return
      }

      const params = new URLSearchParams()
      params.append('clientId', createDraft.clientId)
      params.append('deviceId', createDraft.deviceId)
      params.append('startDate', createDraft.startDate)
      params.append('endDate', createDraft.endDate)
      params.append('dailyRate', createDraft.dailyRate)
      params.append('agentId', createDraft.agentId)

      const resp = await apiClient.post('/rental-contracts', params)
      fetchActiveRentals()
      setSelectedRental(null)
      setShowCreateForm(false)
      setNotice({ type: 'success', title: 'Rental created', message: `Contract ${resp.data.contractNumber} is ready.` })
    } catch (err: any) {
      console.error('Create rental failed', err)
      setNotice({ type: 'danger', title: 'Create rental failed', message: err?.response?.data?.message || 'Create rental failed' })
    }
  }

  async function submitDateDialog() {
    if (!dateDialog) return

    const dateValue = dateDialog.value.trim()
    if (!/^\d{4}-\d{2}-\d{2}$/.test(dateValue)) {
      setNotice({ type: 'warning', title: 'Invalid date', message: 'Please use YYYY-MM-DD format.' })
      return
    }

    setLoading(true)
    try {
      if (dateDialog.mode === 'pickup') {
        await apiClient.post(`/rental-contracts/${dateDialog.rentalId}/schedule-pickup`, null, {
          params: { pickupDate: dateValue },
        })
        setNotice({ type: 'success', title: 'Pickup scheduled', message: `Pickup planned for ${dateValue}.` })
      } else {
        await apiClient.post(`/rental-contracts/${dateDialog.rentalId}/complete`, null, {
          params: { actualReturnDate: dateValue },
        })
        setSelectedRental(null)
        setNotice({ type: 'success', title: 'Rental completed', message: `Contract marked complete on ${dateValue}.` })
      }

      setDateDialog(null)
      await fetchActiveRentals()
    } catch (err: any) {
      console.error('Date workflow action failed', err)
      setNotice({
        type: 'danger',
        title: dateDialog.mode === 'pickup' ? 'Schedule pickup failed' : 'Complete rental failed',
        message: err?.response?.data?.message || 'Operation failed',
      })
    } finally {
      setLoading(false)
    }
  }

  const filterOptions: Array<{ key: typeof activeFilter; label: string }> = [
    { key: 'ALL', label: 'All stages' },
    { key: 'PENDING_DEPLOYMENT', label: 'Pending deploy' },
    { key: 'ACTIVE_RENTAL', label: 'Active' },
    { key: 'PENDING_PICKUP', label: 'Pickup scheduled' },
  ]

  const pendingDeployments = rentals.filter((rental) => rental.contractStatus === 'PENDING_DEPLOYMENT').length
  const awaitingPickup = rentals.filter((rental) => rental.contractStatus === 'PENDING_PICKUP').length

  return (
    <div className="relative min-h-screen px-4 py-4 md:px-8 md:py-8">
      <div className="mx-auto max-w-6xl space-y-6">
        {notice && (
          <AlertBanner
            type={notice.type}
            title={notice.title}
            message={notice.message}
            onClose={() => setNotice(null)}
          />
        )}

        <Card className="overflow-hidden relative">
          <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-sky-400/15 blur-3xl animate-drift" />
          <div className="absolute -left-16 bottom-0 h-40 w-40 rounded-full bg-emerald-400/10 blur-3xl animate-drift" />
          <div className="relative flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p className="premium-badge mb-3">Rental workflow</p>
              <h1 className="text-4xl font-black tracking-tight">Rental Management</h1>
              <p className="mt-3 text-slate-300/75 max-w-2xl">
                Deploy devices, schedule pickups, and complete contracts from a single high-clarity workspace.
              </p>
            </div>
            <div className="flex flex-wrap gap-3">
              <Button variant="secondary" size="sm" onClick={exportList}>Export list</Button>
              <Button variant="primary" size="sm" onClick={() => setShowCreateForm((value) => !value)}>New deployment</Button>
            </div>
          </div>
        </Card>

        {showCreateForm && (
          <Card className="rounded-[28px] border border-sky-300/15">
            <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
              <div>
                <p className="premium-badge mb-3">Create contract</p>
                <h2 className="text-2xl font-black">New rental deployment</h2>
                <p className="mt-2 text-sm text-slate-300/75">Create a real contract from the live database. The assigned agent must exist in users.</p>
              </div>
              <div className="flex gap-2">
                <Button variant="secondary" size="sm" onClick={() => setShowCreateForm(false)}>Cancel</Button>
                <Button variant="primary" size="sm" onClick={submitCreateRental}>Create rental</Button>
              </div>
            </div>

            <div className="mt-5 grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-3">
              <input
                type="number"
                min="1"
                placeholder="Client ID"
                value={createDraft.clientId}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, clientId: e.target.value }))}
                className="premium-input"
              />
              <input
                type="number"
                min="1"
                placeholder="Device ID"
                value={createDraft.deviceId}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, deviceId: e.target.value }))}
                className="premium-input"
              />
              <input
                type="number"
                min="1"
                step="0.01"
                placeholder="Daily rate"
                value={createDraft.dailyRate}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, dailyRate: e.target.value }))}
                className="premium-input"
              />
              <input
                type="date"
                value={createDraft.startDate}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, startDate: e.target.value }))}
                className="premium-input"
              />
              <input
                type="date"
                value={createDraft.endDate}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, endDate: e.target.value }))}
                className="premium-input"
              />
              <input
                type="number"
                min="1"
                placeholder="Agent ID"
                value={createDraft.agentId}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, agentId: e.target.value }))}
                className="premium-input"
              />
            </div>
          </Card>
        )}

        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <Card><p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Open contracts</p><p className="mt-3 text-3xl font-black">{rentals.length}</p></Card>
          <Card><p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Ready to deploy</p><p className="mt-3 text-3xl font-black text-sky-100">{pendingDeployments}</p></Card>
          <Card><p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Awaiting pickup</p><p className="mt-3 text-3xl font-black text-emerald-100">{awaitingPickup}</p></Card>
        </div>

        <div className="flex flex-wrap gap-2">
          {filterOptions.map((filter) => (
            <button
              key={filter.key}
              onClick={() => setActiveFilter(filter.key)}
              className={`premium-button px-4 py-2 text-sm ${activeFilter === filter.key ? 'premium-button--primary' : 'premium-button--secondary'}`}
            >
              {filter.label}
            </button>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Rental List */}
          <div className="lg:col-span-2">
            <Card className="rounded-[28px]">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-black">Live Contracts</h2>
                <Badge variant={filteredRentals.length > 0 ? 'primary' : 'success'}>{filteredRentals.length}</Badge>
              </div>
              <div className="space-y-3 max-h-[34rem] overflow-y-auto pr-1 premium-scrollbar">
                {filteredRentals.length === 0 ? (
                  <div className="rounded-[24px] border border-white/10 bg-white/5 p-6">
                    <p className="text-slate-200 font-semibold">No active rentals</p>
                    <p className="text-slate-300/70 text-sm mt-2">Use the new deployment button to start a contract.</p>
                  </div>
                ) : (
                  filteredRentals.map((rental) => (
                    <div
                      key={rental.id}
                      onClick={() => setSelectedRental(rental)}
                      className={`p-4 border rounded-[24px] cursor-pointer transition-all ${
                        selectedRental?.id === rental.id
                          ? 'border-sky-300/35 bg-sky-400/10 shadow-[0_16px_40px_rgba(47,126,220,0.12)]'
                          : 'border-white/10 bg-white/5 hover:border-sky-300/20 hover:bg-white/8'
                      }`}
                    >
                      <div className="flex justify-between items-start mb-2">
                        <div>
                          <p className="font-semibold text-slate-50">{rental.clientName}</p>
                          <p className="text-sm text-slate-300/70">{rental.contractNumber}</p>
                        </div>
                        <Badge variant={statusVariantMap[rental.contractStatus as keyof typeof statusVariantMap]}>
                          {rental.contractStatus.replace(/_/g, ' ')}
                        </Badge>
                      </div>
                      <p className="text-sm text-slate-300/70">Device: {rental.deviceSerialNumber}</p>
                      <p className="text-sm text-slate-300/70 mt-1">
                        Until: {new Date(rental.expectedReturnDate).toLocaleDateString()}
                      </p>
                      {rental.isOverdue && <p className="text-sm text-red-600 font-bold">{rental.daysOverdue} days overdue</p>}
                    </div>
                  ))
                )}
              </div>
            </Card>
          </div>

          {/* Details & Actions Panel */}
          <div>
            {selectedRental ? (
              <Card className="sticky top-4 rounded-[28px]">
                <h3 className="font-black text-lg mb-4">Rental Details</h3>

                <div className="space-y-3 mb-6 text-sm">
                  <div>
                    <p className="text-slate-300/65">Client</p>
                    <p className="font-semibold text-slate-50">{selectedRental.clientName}</p>
                  </div>
                  <div>
                    <p className="text-slate-300/65">Contract #</p>
                    <p className="font-semibold text-slate-50">{selectedRental.contractNumber}</p>
                  </div>
                  <div>
                    <p className="text-slate-300/65">Equipment</p>
                    <p className="font-semibold text-slate-50">{selectedRental.deviceSerialNumber}</p>
                  </div>
                  <div>
                    <p className="text-slate-300/65">Return Date</p>
                    <p className="font-semibold text-slate-50">{new Date(selectedRental.expectedReturnDate).toLocaleDateString()}</p>
                  </div>
                  <div>
                    <p className="text-slate-300/65">Status</p>
                    <p className="font-semibold">
                      <Badge variant={statusVariantMap[selectedRental.contractStatus as keyof typeof statusVariantMap]}>
                        {selectedRental.contractStatus.replace(/_/g, ' ')}
                      </Badge>
                    </p>
                  </div>
                </div>

                {/* Workflow Actions */}
                {selectedRental.contractStatus === 'PENDING_DEPLOYMENT' && (
                  <div className="space-y-2">
                    <textarea
                      placeholder="Deployment notes..."
                      value={deploymentNotes}
                      onChange={(e) => setDeploymentNotes(e.target.value)}
                      className="premium-textarea text-sm min-h-28"
                      rows={3}
                    />
                    <Button
                      variant="primary"
                      onClick={async () => {
                        setLoading(true)
                        try {
                          await apiClient.post(`/rental-contracts/${selectedRental.id}/deploy`, null, {
                            params: { deploymentNotes },
                          })
                          await fetchActiveRentals()
                          setSelectedRental(null)
                          setNotice({ type: 'success', title: 'Device deployed', message: 'The rental is now active.' })
                        } catch (err) {
                          console.error('Deploy failed', err)
                          setNotice({ type: 'danger', title: 'Deploy failed', message: 'Could not deploy this rental.' })
                        } finally {
                          setLoading(false)
                        }
                      }}
                      disabled={loading}
                      className="w-full"
                    >
                      {loading ? 'Deploying...' : 'Deploy Device'}
                    </Button>
                  </div>
                )}

                {selectedRental.contractStatus === 'ACTIVE_RENTAL' && (
                  <Button
                    variant="success"
                    onClick={() => {
                      setDateDialog({
                        mode: 'pickup',
                        rentalId: selectedRental.id,
                        rentalLabel: selectedRental.contractNumber,
                        value: new Date().toISOString().slice(0, 10),
                      })
                    }}
                    className="w-full"
                  >
                    Schedule Pickup
                  </Button>
                )}

                {selectedRental.contractStatus === 'PENDING_PICKUP' && (
                  <Button
                    variant="primary"
                    onClick={() => {
                      setDateDialog({
                        mode: 'complete',
                        rentalId: selectedRental.id,
                        rentalLabel: selectedRental.contractNumber,
                        value: new Date().toISOString().slice(0, 10),
                      })
                    }}
                    className="w-full"
                  >
                    Complete Rental
                  </Button>
                )}
              </Card>
            ) : (
              <Card className="rounded-[28px]">
                <p className="text-slate-300/70 text-center py-8">Select a rental to view details and manage workflow</p>
              </Card>
            )}
          </div>
        </div>
      </div>

      {dateDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/65 p-4">
          <div className="glass-card panel-glow w-full max-w-md rounded-[24px] border border-white/15 p-6">
            <p className="premium-badge mb-3">Workflow action</p>
            <h3 className="text-xl font-black text-slate-50">
              {dateDialog.mode === 'pickup' ? 'Schedule Pickup' : 'Complete Rental'}
            </h3>
            <p className="mt-2 text-sm text-slate-300/75">Contract {dateDialog.rentalLabel}</p>

            <label className="mt-5 block text-xs uppercase tracking-[0.18em] text-slate-300/65">
              {dateDialog.mode === 'pickup' ? 'Pickup Date' : 'Actual Return Date'}
            </label>
            <input
              type="date"
              value={dateDialog.value}
              onChange={(e) => setDateDialog((current) => current ? { ...current, value: e.target.value } : current)}
              className="premium-input mt-2"
            />

            <div className="mt-5 flex justify-end gap-2">
              <Button
                variant="secondary"
                size="sm"
                onClick={() => setDateDialog(null)}
                disabled={loading}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                size="sm"
                onClick={submitDateDialog}
                disabled={loading}
              >
                {loading ? 'Saving...' : dateDialog.mode === 'pickup' ? 'Schedule' : 'Complete'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
