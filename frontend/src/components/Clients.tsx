import { useEffect, useState } from 'react'
import { Card, Button, Badge, AlertBanner } from './shared'
import apiClient from '../store/appStore'

interface Client {
  id: number
  firstName: string
  lastName: string
  phone: string
  email?: string
  city?: string
}

export function Clients() {
  const [clients, setClients] = useState<Client[]>([])
  const [loading, setLoading] = useState(false)
  const [notice, setNotice] = useState<{ type: 'info' | 'success' | 'warning' | 'danger'; title: string; message: string } | null>(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [activeClientCount, setActiveClientCount] = useState(0)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [clientDraft, setClientDraft] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
    streetAddress: '',
    city: '',
    postalCode: '',
  })

  useEffect(() => {
    fetchClients()
    fetchStats()
  }, [])

  async function fetchStats() {
    try {
      const resp = await apiClient.get('/clients/stats/total-active')
      setActiveClientCount(Number(resp.data || 0))
    } catch (err) {
      console.error('Failed to load client stats', err)
    }
  }

  async function fetchClients() {
    try {
      const resp = await apiClient.get('/clients')
      setClients(resp.data)
    } catch (err) {
      console.error('Failed to load clients', err)
    }
  }

  async function submitCreateClient() {
    try {
      if (!clientDraft.firstName || !clientDraft.lastName || !clientDraft.phone || !clientDraft.streetAddress || !clientDraft.city || !clientDraft.postalCode) {
        setNotice({
          type: 'warning',
          title: 'Missing fields',
          message: 'First name, last name, phone, street address, city, and postal code are required.',
        })
        return
      }

      const params = new URLSearchParams()
      params.append('firstName', clientDraft.firstName)
      params.append('lastName', clientDraft.lastName)
      params.append('phone', clientDraft.phone)
      if (clientDraft.email) {
        params.append('email', clientDraft.email)
      }
      params.append('streetAddress', clientDraft.streetAddress)
      params.append('city', clientDraft.city)
      params.append('postalCode', clientDraft.postalCode)

      setLoading(true)
      const resp = await apiClient.post('/clients', params)
      setNotice({ type: 'success', title: 'Client created', message: `Client #${resp.data.id} has been created.` })
      await fetchClients()
      await fetchStats()
      setShowCreateForm(false)
    } catch (err:any) {
      console.error('Create client failed', err)
      setNotice({ type: 'danger', title: 'Create client failed', message: err?.response?.data?.message || 'Create client failed' })
    } finally {
      setLoading(false)
    }
  }

  async function deactivateClient(id: number) {
    try {
      await apiClient.delete(`/clients/${id}/deactivate`)
      await fetchClients()
      await fetchStats()
      setNotice({ type: 'info', title: 'Client deactivated', message: `Client #${id} is now inactive.` })
    } catch (err: any) {
      console.error('Deactivate client failed', err)
      setNotice({ type: 'danger', title: 'Deactivate failed', message: err?.response?.data?.message || 'Deactivate client failed' })
    }
  }

  const filteredClients = clients.filter((client) => {
    const haystack = `${client.firstName} ${client.lastName} ${client.phone} ${client.email || ''} ${client.city || ''}`.toLowerCase()
    return haystack.includes(searchTerm.toLowerCase())
  })

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
          <div className="relative flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p className="premium-badge mb-3">CRM</p>
              <h1 className="text-4xl font-black tracking-tight">Clients</h1>
              <p className="mt-3 text-slate-300/75 max-w-2xl">Manage clients and patient profiles.</p>
            </div>
            <div className="flex flex-wrap items-center gap-3">
              <Badge variant="primary">{activeClientCount} active</Badge>
              <input
                type="search"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search clients"
                className="premium-input min-w-56 text-sm"
              />
              <Button variant="primary" size="sm" onClick={() => setShowCreateForm((value) => !value)}>
                New client
              </Button>
            </div>
          </div>
        </Card>

        {showCreateForm && (
          <Card className="rounded-[28px] border border-emerald-300/15">
            <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
              <div>
                <p className="premium-badge mb-3">CRM</p>
                <h2 className="text-2xl font-black">Create a new client</h2>
                <p className="mt-2 text-sm text-slate-300/75">Store the real patient record in the database before you deploy equipment.</p>
              </div>
              <div className="flex gap-2">
                <Button variant="secondary" size="sm" onClick={() => setShowCreateForm(false)}>Cancel</Button>
                <Button variant="primary" size="sm" onClick={submitCreateClient}>Save client</Button>
              </div>
            </div>

            <div className="mt-5 grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-3">
              <input
                type="text"
                placeholder="First name"
                value={clientDraft.firstName}
                onChange={(e) => setClientDraft((draft) => ({ ...draft, firstName: e.target.value }))}
                className="premium-input"
              />
              <input
                type="text"
                placeholder="Last name"
                value={clientDraft.lastName}
                onChange={(e) => setClientDraft((draft) => ({ ...draft, lastName: e.target.value }))}
                className="premium-input"
              />
              <input
                type="tel"
                placeholder="Phone"
                value={clientDraft.phone}
                onChange={(e) => setClientDraft((draft) => ({ ...draft, phone: e.target.value }))}
                className="premium-input"
              />
              <input
                type="email"
                placeholder="Email"
                value={clientDraft.email}
                onChange={(e) => setClientDraft((draft) => ({ ...draft, email: e.target.value }))}
                className="premium-input"
              />
              <input
                type="text"
                placeholder="Street address"
                value={clientDraft.streetAddress}
                onChange={(e) => setClientDraft((draft) => ({ ...draft, streetAddress: e.target.value }))}
                className="premium-input"
              />
              <input
                type="text"
                placeholder="City"
                value={clientDraft.city}
                onChange={(e) => setClientDraft((draft) => ({ ...draft, city: e.target.value }))}
                className="premium-input"
              />
              <input
                type="text"
                placeholder="Postal code"
                value={clientDraft.postalCode}
                onChange={(e) => setClientDraft((draft) => ({ ...draft, postalCode: e.target.value }))}
                className="premium-input"
              />
            </div>
          </Card>
        )}

        <div className="grid grid-cols-1 gap-4">
          <Card className="rounded-[28px]">
            <div className="space-y-3 max-h-[34rem] overflow-y-auto pr-1 premium-scrollbar">
              {filteredClients.length === 0 ? (
                <div className="rounded-[24px] border border-white/10 bg-white/5 p-6">
                  <p className="text-slate-200 font-semibold">No clients</p>
                  <p className="text-slate-300/70 text-sm mt-2">Add clients with the New client button or clear the search filter.</p>
                </div>
              ) : (
                filteredClients.map((c) => (
                  <div key={c.id} className="p-4 border rounded-[24px] transition-all border-white/10 bg-white/5">
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <p className="font-semibold text-slate-50">{c.firstName} {c.lastName}</p>
                        <p className="text-sm text-slate-300/70">{c.phone} {c.email ? `· ${c.email}` : ''}</p>
                      </div>
                      <Button variant="secondary" size="sm" onClick={() => deactivateClient(c.id)}>
                        Deactivate
                      </Button>
                    </div>
                    <p className="text-sm text-slate-300/70">{c.city}</p>
                  </div>
                ))
              )}
            </div>
          </Card>
        </div>
      </div>
    </div>
  )
}
