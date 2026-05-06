import { useMemo, useState, useEffect } from 'react'
import { Card, Button, Badge, AlertBanner } from './shared'
import apiClient from '../store/appStore'

interface Invoice {
  id: number
  invoiceNumber: string
  clientName: string
  totalAmount: number
  paidAmount: number
  balanceDue: number
  paymentStatus: string
  dueDate: string
  isOverdue: boolean
  daysOverdue: number
}

export function InvoiceManagement() {
  const [invoices, setInvoices] = useState<Invoice[]>([])
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null)
  const [paymentAmount, setPaymentAmount] = useState('')
  const [paymentMethod, setPaymentMethod] = useState('BANK_TRANSFER')
  const [loading, setLoading] = useState(false)
  const [notice, setNotice] = useState<{ type: 'info' | 'success' | 'warning' | 'danger'; title: string; message: string } | null>(null)
  const [activeFilter, setActiveFilter] = useState<'ALL' | 'UNPAID' | 'OVERDUE'>('ALL')
  const [totalUnpaidAmount, setTotalUnpaidAmount] = useState(0)
  const [totalPaidThisMonth, setTotalPaidThisMonth] = useState(0)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [createDraft, setCreateDraft] = useState({
    contractId: '',
    paymentTermsDays: '30',
  })

  const statusVariantMap = {
    PAID: 'success',
    PARTIAL: 'warning',
    UNPAID: 'danger',
  } as const

  const filterOptions: Array<{ key: typeof activeFilter; label: string }> = [
    { key: 'ALL', label: 'All invoices' },
    { key: 'UNPAID', label: 'Unpaid' },
    { key: 'OVERDUE', label: 'Overdue' },
  ]

  const filteredInvoices = useMemo(() => {
    if (activeFilter === 'ALL') {
      return invoices
    }

    if (activeFilter === 'UNPAID') {
      return invoices.filter((invoice) => invoice.paymentStatus !== 'PAID')
    }

    return invoices.filter((invoice) => invoice.isOverdue)
  }, [activeFilter, invoices])

  useEffect(() => {
    fetchInvoices()
    fetchFinancialStats()
  }, [])

  async function fetchFinancialStats() {
    try {
      const now = new Date()
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
      const formatDate = (date: Date) =>
        `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`

      const [unpaidResp, paidResp] = await Promise.all([
        apiClient.get('/invoices/stats/total-unpaid'),
        apiClient.get('/invoices/stats/total-paid', {
          params: { startDate: formatDate(startOfMonth), endDate: formatDate(now) },
        }),
      ])

      setTotalUnpaidAmount(Number(unpaidResp.data || 0))
      setTotalPaidThisMonth(Number(paidResp.data || 0))
    } catch (err) {
      console.error('Failed to load invoice stats', err)
    }
  }

  async function fetchInvoices() {
    try {
      const unpaid = await apiClient.get('/invoices/unpaid')
      const overdue = await apiClient.get('/invoices/overdue')
      const merged = [...unpaid.data, ...overdue.data].filter((v: any, i: number, a: any[]) => a.findIndex((x: any) => x.id === v.id) === i)
      setInvoices(merged)
    } catch (err) {
      console.error('Failed to load invoices', err)
    }
  }

  async function exportInvoices() {
    try {
      const rows = filteredInvoices
      const csv = [
        ['id', 'invoiceNumber', 'clientName', 'totalAmount', 'paidAmount', 'balanceDue', 'paymentStatus', 'dueDate'],
        ...rows.map((r) => [r.id, r.invoiceNumber, r.clientName, r.totalAmount, r.paidAmount, r.balanceDue, r.paymentStatus, r.dueDate]),
      ]
        .map((r) => r.join(','))
        .join('\n')

      const blob = new Blob([csv], { type: 'text/csv' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `invoices_export_${new Date().toISOString()}.csv`
      document.body.appendChild(a)
      a.click()
      a.remove()
      URL.revokeObjectURL(url)
      setNotice({ type: 'info', title: 'Export complete', message: `Exported ${rows.length} invoice${rows.length === 1 ? '' : 's'}` })
    } catch (err) {
      console.error('Export invoices failed', err)
      setNotice({ type: 'danger', title: 'Export failed', message: 'Unable to export invoices.' })
    }
  }

  async function submitCreateInvoice() {
    try {
      if (!createDraft.contractId) {
        setNotice({ type: 'warning', title: 'Missing contract', message: 'Contract ID is required to create an invoice.' })
        return
      }

      const resp = await apiClient.post(`/invoices/from-contract/${createDraft.contractId}`, null, {
        params: { paymentTermsDays: createDraft.paymentTermsDays || '30' },
      })
      setNotice({ type: 'success', title: 'Invoice created', message: `Invoice ${resp.data.invoiceNumber} is ready.` })
      fetchInvoices()
      fetchFinancialStats()
      setShowCreateForm(false)
    } catch (err: any) {
      console.error('Create invoice failed', err)
      setNotice({ type: 'danger', title: 'Create invoice failed', message: err?.response?.data?.message || 'Create invoice failed' })
    }
  }

  const getPaymentProgress = (invoice: Invoice) => {
    return (invoice.paidAmount / invoice.totalAmount) * 100
  }

  const partialBalance = invoices
    .filter((invoice) => invoice.paymentStatus === 'PARTIAL')
    .reduce((sum, invoice) => sum + Number(invoice.balanceDue || 0), 0)

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
          <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-violet-400/15 blur-3xl animate-drift" />
          <div className="absolute -left-16 bottom-0 h-40 w-40 rounded-full bg-emerald-400/10 blur-3xl animate-drift" />
          <div className="relative flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p className="premium-badge mb-3">Cashflow control</p>
              <h1 className="text-4xl font-black tracking-tight">Invoice & Payment Management</h1>
              <p className="mt-3 text-slate-300/75 max-w-2xl">
                A polished billing workspace with progress tracking, payment capture, and overdue visibility.
              </p>
            </div>
            <div className="flex flex-wrap gap-3">
              <Button variant="secondary" size="sm" onClick={exportInvoices}>Export invoices</Button>
              <Button variant="primary" size="sm" onClick={() => setShowCreateForm((value) => !value)}>Create invoice</Button>
            </div>
          </div>
        </Card>

        {showCreateForm && (
          <Card className="rounded-[28px] border border-violet-300/15">
            <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
              <div>
                <p className="premium-badge mb-3">Billing</p>
                <h2 className="text-2xl font-black">Create invoice from contract</h2>
                <p className="mt-2 text-sm text-slate-300/75">Build an invoice directly from a rental contract record in the database.</p>
              </div>
              <div className="flex gap-2">
                <Button variant="secondary" size="sm" onClick={() => setShowCreateForm(false)}>Cancel</Button>
                <Button variant="primary" size="sm" onClick={submitCreateInvoice}>Create invoice</Button>
              </div>
            </div>

            <div className="mt-5 grid grid-cols-1 gap-3 md:grid-cols-2">
              <input
                type="number"
                min="1"
                placeholder="Rental contract ID"
                value={createDraft.contractId}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, contractId: e.target.value }))}
                className="premium-input"
              />
              <input
                type="number"
                min="1"
                placeholder="Payment terms (days)"
                value={createDraft.paymentTermsDays}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, paymentTermsDays: e.target.value }))}
                className="premium-input"
              />
            </div>
          </Card>
        )}

        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <Card className="border border-rose-300/15">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Total Unpaid</p>
            <p className="text-3xl font-black text-rose-100 mt-3">€{totalUnpaidAmount.toFixed(2)}</p>
          </Card>
          <Card className="border border-amber-300/15">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Partial Balance</p>
            <p className="text-3xl font-black text-amber-100 mt-3">€{partialBalance.toFixed(2)}</p>
          </Card>
          <Card className="border border-emerald-300/15">
            <p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Collected This Month</p>
            <p className="text-3xl font-black text-emerald-100 mt-3">€{totalPaidThisMonth.toFixed(2)}</p>
          </Card>
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
          {/* Invoice List */}
          <div className="lg:col-span-2">
            <Card className="rounded-[28px]">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-black">Invoices</h2>
                <Badge variant={filteredInvoices.length > 0 ? 'primary' : 'success'}>{filteredInvoices.length}</Badge>
              </div>

              <div className="space-y-3 max-h-[34rem] overflow-y-auto pr-1 premium-scrollbar">
                {filteredInvoices.length === 0 ? (
                  <div className="rounded-[24px] border border-white/10 bg-white/5 p-6">
                    <p className="text-slate-200 font-semibold">No invoices</p>
                    <p className="text-slate-300/70 text-sm mt-2">Create your first invoice after a rental completes.</p>
                  </div>
                ) : (
                  filteredInvoices.map((invoice) => (
                    <div
                      key={invoice.id}
                      onClick={() => setSelectedInvoice(invoice)}
                      className={`p-4 border rounded-[24px] cursor-pointer transition-all ${
                        selectedInvoice?.id === invoice.id
                          ? 'border-sky-300/35 bg-sky-400/10 shadow-[0_16px_40px_rgba(47,126,220,0.12)]'
                          : 'border-white/10 bg-white/5 hover:border-sky-300/20 hover:bg-white/8'
                      }`}
                    >
                      <div className="flex justify-between items-start mb-3">
                        <div>
                          <p className="font-semibold text-slate-50">{invoice.invoiceNumber}</p>
                          <p className="text-sm text-slate-300/70">{invoice.clientName}</p>
                        </div>
                        <Badge variant={statusVariantMap[invoice.paymentStatus as keyof typeof statusVariantMap]}>
                          {invoice.paymentStatus}
                        </Badge>
                      </div>

                      {/* Payment Progress Bar */}
                      <div className="mb-2">
                        <div className="flex justify-between text-xs text-slate-300/70 mb-1">
                          <span>€{invoice.paidAmount.toFixed(2)}</span>
                          <span>€{invoice.totalAmount.toFixed(2)}</span>
                        </div>
                        <div className="w-full bg-white/10 rounded-full h-2 overflow-hidden">
                          <div
                            className="bg-gradient-to-r from-emerald-300 via-cyan-300 to-sky-400 h-2 rounded-full transition-all"
                            style={{ width: `${getPaymentProgress(invoice)}%` }}
                          />
                        </div>
                      </div>

                      <div className="flex justify-between items-center text-sm">
                        <p className="text-slate-300/70">Due: {new Date(invoice.dueDate).toLocaleDateString()}</p>
                        {invoice.isOverdue && <p className="text-red-600 font-bold">{invoice.daysOverdue} days overdue</p>}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </Card>
          </div>

          {/* Payment Panel */}
          <div>
            {selectedInvoice ? (
              <Card className="sticky top-4 rounded-[28px]">
                <h3 className="font-black text-lg mb-4">Invoice Details</h3>

                {selectedInvoice.isOverdue && (
                  <AlertBanner type="danger" title="Overdue Invoice" message={`${selectedInvoice.daysOverdue} days overdue`} />
                )}

                <div className="space-y-3 mb-6 text-sm">
                  <div className="border-b pb-2">
                    <p className="text-slate-300/65">Invoice #</p>
                    <p className="font-semibold text-slate-50">{selectedInvoice.invoiceNumber}</p>
                  </div>
                  <div className="border-b pb-2">
                    <p className="text-slate-300/65">Total Amount</p>
                    <p className="font-semibold text-lg text-slate-50">€{selectedInvoice.totalAmount.toFixed(2)}</p>
                  </div>
                  <div className="border-b pb-2">
                    <p className="text-slate-300/65">Paid</p>
                    <p className="font-semibold text-slate-50">€{selectedInvoice.paidAmount.toFixed(2)}</p>
                  </div>
                  <div className="border-b pb-2">
                    <p className="text-slate-300/65">Balance Due</p>
                    <p className="font-semibold text-lg text-rose-200">€{selectedInvoice.balanceDue.toFixed(2)}</p>
                  </div>
                  <div>
                    <p className="text-slate-300/65">Status</p>
                    <p className="font-semibold">
                      <Badge variant={statusVariantMap[selectedInvoice.paymentStatus as keyof typeof statusVariantMap]}>
                        {selectedInvoice.paymentStatus}
                      </Badge>
                    </p>
                  </div>
                </div>

                {selectedInvoice.paymentStatus !== 'PAID' && (
                  <div className="space-y-3 border-t pt-4">
                    <h4 className="font-semibold text-sm tracking-wide uppercase text-slate-300/65">Record Payment</h4>

                    <input
                      type="number"
                      placeholder="Amount"
                      value={paymentAmount}
                      onChange={(e) => setPaymentAmount(e.target.value)}
                      className="premium-input text-sm"
                      max={selectedInvoice.balanceDue}
                    />

                    <select
                      value={paymentMethod}
                      onChange={(e) => setPaymentMethod(e.target.value)}
                      className="premium-select text-sm"
                    >
                      <option value="BANK_TRANSFER">Bank Transfer</option>
                      <option value="CASH">Cash</option>
                      <option value="CHECK">Check</option>
                      <option value="CARD">Card</option>
                    </select>

                    <Button
                      variant="success"
                      onClick={async () => {
                        if (!paymentAmount) return
                        setLoading(true)
                        try {
                          await apiClient.post(`/invoices/${selectedInvoice?.id}/payments`, null, {
                            params: {
                              amount: paymentAmount,
                              method: paymentMethod,
                              recordedByUserId: 1,
                            },
                          })
                          setPaymentAmount('')
                          await fetchInvoices()
                          await fetchFinancialStats()
                          setNotice({ type: 'success', title: 'Payment recorded', message: 'The payment was saved successfully.' })
                        } catch (err) {
                          console.error('Record payment failed', err)
                          setNotice({ type: 'danger', title: 'Record payment failed', message: 'Could not record this payment.' })
                        } finally {
                          setLoading(false)
                        }
                      }}
                      disabled={loading || !paymentAmount}
                      className="w-full"
                    >
                      {loading ? 'Processing...' : 'Record Payment'}
                    </Button>
                  </div>
                )}

                <div className="mt-4 pt-4 border-t">
                  <Button variant="secondary" size="sm" className="w-full" onClick={() => window.print()}>
                    Print Invoice
                  </Button>
                </div>
              </Card>
            ) : (
              <Card className="rounded-[28px]">
                <p className="text-slate-300/70 text-center py-8">Select an invoice to record payment</p>
              </Card>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
