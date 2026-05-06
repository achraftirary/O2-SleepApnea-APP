import { useEffect, useMemo, useState } from 'react'
import { Card, Button, Badge, AlertBanner } from './shared'
import apiClient from '../store/appStore'

type DeviceStatus = 'AVAILABLE' | 'DEPLOYED' | 'IN_MAINTENANCE' | 'RETIRED'

interface Device {
  id: number
  deviceType: string
  serialNumber: string
  manufacturer?: string
  model?: string
  deviceStatus: DeviceStatus
  purchasePrice?: number
  acquisitionDate?: string
  decommissionDate?: string
  quantityInStock?: number
  isConsumable: boolean
  displayName: string
  totalRentalHours?: number
  totalRentalDays?: number
  requiresMaintenance?: boolean
  daysUntilMaintenance?: number
}

type DeviceFilter = 'ALL' | 'AVAILABLE' | 'DEPLOYED' | 'IN_MAINTENANCE' | 'CONSUMABLES' | 'LOW_STOCK' | 'MAINTENANCE_REQUIRED'

export function Inventory() {
  const [devices, setDevices] = useState<Device[]>([])
  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null)
  const [activeFilter, setActiveFilter] = useState<DeviceFilter>('ALL')
  const [loading, setLoading] = useState(false)
  const [notice, setNotice] = useState<{ type: 'info' | 'success' | 'warning' | 'danger'; title: string; message: string } | null>(null)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [createDraft, setCreateDraft] = useState({
    deviceType: 'OXYGEN_CONCENTRATOR_5L',
    serialNumber: '',
    manufacturer: '',
    model: '',
    purchasePrice: '',
  })

  const isLowStock = (device: Device) => device.isConsumable && (device.quantityInStock || 0) < 5
  const isMaintenanceDue = (device: Device) => Boolean(device.requiresMaintenance)
  const isInMaintenance = (device: Device) => device.deviceStatus === 'IN_MAINTENANCE'

  useEffect(() => {
    fetchInventory()
  }, [])

  async function fetchInventory() {
    try {
      const [availableResp, deployedResp, maintenanceStatusResp, lowStockResp, maintenanceResp] = await Promise.all([
        apiClient.get('/devices/available'),
        apiClient.get('/devices/deployed'),
        apiClient.get('/devices/status/IN_MAINTENANCE'),
        apiClient.get('/devices/low-stock'),
        apiClient.get('/devices/maintenance-required'),
      ])

      const merged = [
        ...availableResp.data,
        ...deployedResp.data,
        ...maintenanceStatusResp.data,
        ...lowStockResp.data,
        ...maintenanceResp.data,
      ].filter((device, index, array) => array.findIndex((candidate: Device) => candidate.id === device.id) === index)

      setDevices(merged)
      setSelectedDevice((current) => current && merged.some((device) => device.id === current.id) ? current : merged[0] || null)
      setInventoryCounts({
        available: merged.filter((device) => device.deviceStatus === 'AVAILABLE' && !isLowStock(device) && !isMaintenanceDue(device)).length,
        deployed: merged.filter((device) => device.deviceStatus === 'DEPLOYED').length,
        maintenance: merged.filter((device) => isInMaintenance(device)).length,
        lowStock: merged.filter((device) => isLowStock(device)).length,
      })
    } catch (err) {
      console.error('Failed to load inventory', err)
    }
  }

  const [inventoryCounts, setInventoryCounts] = useState({
    available: 0,
    deployed: 0,
    maintenance: 0,
    lowStock: 0,
  })

  const filteredDevices = useMemo(() => {
    switch (activeFilter) {
      case 'AVAILABLE':
        return devices.filter((device) => device.deviceStatus === 'AVAILABLE' && !isLowStock(device) && !isMaintenanceDue(device))
      case 'DEPLOYED':
        return devices.filter((device) => device.deviceStatus === 'DEPLOYED')
      case 'IN_MAINTENANCE':
        return devices.filter((device) => isInMaintenance(device))
      case 'CONSUMABLES':
        return devices.filter((device) => device.isConsumable)
      case 'LOW_STOCK':
        return devices.filter((device) => isLowStock(device))
      case 'MAINTENANCE_REQUIRED':
        return devices.filter((device) => isMaintenanceDue(device))
      default:
        return devices
    }
  }, [activeFilter, devices])

  async function submitCreateDevice() {
    try {
      if (!createDraft.deviceType || !createDraft.serialNumber.trim()) {
        setNotice({ type: 'warning', title: 'Missing fields', message: 'Device type and serial number are required.' })
        return
      }

      const params = new URLSearchParams()
      params.append('deviceType', createDraft.deviceType)
      params.append('serialNumber', createDraft.serialNumber.trim())
      if (createDraft.manufacturer.trim()) params.append('manufacturer', createDraft.manufacturer.trim())
      if (createDraft.model.trim()) params.append('model', createDraft.model.trim())
      if (createDraft.purchasePrice.trim()) params.append('purchasePrice', createDraft.purchasePrice.trim())

      setLoading(true)
      const resp = await apiClient.post('/devices', params)
      setNotice({ type: 'success', title: 'Device registered', message: `Serial ${resp.data.serialNumber} is now in inventory.` })
      setCreateDraft({
        deviceType: 'OXYGEN_CONCENTRATOR_5L',
        serialNumber: '',
        manufacturer: '',
        model: '',
        purchasePrice: '',
      })
      setShowCreateForm(false)
      await fetchInventory()
    } catch (err: any) {
      console.error('Create device failed', err)
      setNotice({ type: 'danger', title: 'Create device failed', message: err?.response?.data?.message || 'Create device failed' })
    } finally {
      setLoading(false)
    }
  }

  async function decommissionDevice(deviceId: number) {
    try {
      setLoading(true)
      await apiClient.put(`/devices/${deviceId}/decommission`)
      await fetchInventory()
      if (selectedDevice?.id === deviceId) {
        setSelectedDevice(null)
      }
      setNotice({ type: 'info', title: 'Device decommissioned', message: `Device #${deviceId} has been retired.` })
    } catch (err: any) {
      console.error('Decommission failed', err)
      setNotice({ type: 'danger', title: 'Decommission failed', message: err?.response?.data?.message || 'Decommission failed' })
    } finally {
      setLoading(false)
    }
  }

  async function flagMaintenance(deviceId: number) {
    try {
      setLoading(true)
      await apiClient.put(`/devices/${deviceId}/flag-maintenance`)
      await fetchInventory()
      setNotice({ type: 'warning', title: 'Maintenance flagged', message: `Device #${deviceId} marked as maintenance due.` })
    } catch (err: any) {
      console.error('Flag maintenance failed', err)
      setNotice({ type: 'danger', title: 'Flag maintenance failed', message: err?.response?.data?.message || 'Flag maintenance failed' })
    } finally {
      setLoading(false)
    }
  }

  const filterOptions: Array<{ key: DeviceFilter; label: string }> = [
    { key: 'ALL', label: 'All devices' },
    { key: 'AVAILABLE', label: 'Available' },
    { key: 'DEPLOYED', label: 'Deployed' },
    { key: 'IN_MAINTENANCE', label: 'In maintenance' },
    { key: 'CONSUMABLES', label: 'Consumables' },
    { key: 'LOW_STOCK', label: 'Low stock' },
    { key: 'MAINTENANCE_REQUIRED', label: 'Maintenance due' },
  ]

  const statusVariantMap: Record<DeviceStatus, 'success' | 'primary' | 'warning' | 'danger'> = {
    AVAILABLE: 'success',
    DEPLOYED: 'primary',
    IN_MAINTENANCE: 'warning',
    RETIRED: 'danger',
  }

  return (
    <div className="relative min-h-screen px-4 py-4 md:px-8 md:py-8">
      <div className="mx-auto max-w-7xl space-y-6">
        {notice && (
          <AlertBanner
            type={notice.type}
            title={notice.title}
            message={notice.message}
            onClose={() => setNotice(null)}
          />
        )}

        <Card className="overflow-hidden relative">
          <div className="absolute -right-16 -top-16 h-48 w-48 rounded-full bg-cyan-400/15 blur-3xl animate-drift" />
          <div className="absolute -left-16 bottom-0 h-40 w-40 rounded-full bg-amber-400/10 blur-3xl animate-drift" />
          <div className="relative flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <p className="premium-badge mb-3">Inventory control</p>
              <h1 className="text-4xl font-black tracking-tight">Device Inventory</h1>
              <p className="mt-3 text-slate-300/75 max-w-2xl">
                Manage live equipment stock, deployment status, maintenance flags, and consumables from the database.
              </p>
            </div>
            <div className="flex flex-wrap gap-3">
              <Button variant="secondary" size="sm" onClick={fetchInventory}>Refresh inventory</Button>
              <Button variant="primary" size="sm" onClick={() => setShowCreateForm((value) => !value)}>Register device</Button>
            </div>
          </div>
        </Card>

        {showCreateForm && (
          <Card className="rounded-[28px] border border-cyan-300/15">
            <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
              <div>
                <p className="premium-badge mb-3">New device</p>
                <h2 className="text-2xl font-black">Register a real device</h2>
                <p className="mt-2 text-sm text-slate-300/75">This creates an inventory item in the database and auto-registers usage tracking for non-consumables.</p>
              </div>
              <div className="flex gap-2">
                <Button variant="secondary" size="sm" onClick={() => setShowCreateForm(false)}>Cancel</Button>
                <Button variant="primary" size="sm" onClick={submitCreateDevice} disabled={loading}>Save device</Button>
              </div>
            </div>

            <div className="mt-5 grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-3">
              <select
                value={createDraft.deviceType}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, deviceType: e.target.value }))}
                className="premium-select"
              >
                <option value="OXYGEN_CONCENTRATOR_5L">Oxygen Concentrator 5L</option>
                <option value="OXYGEN_CONCENTRATOR_10L">Oxygen Concentrator 10L</option>
                <option value="OXYGEN_CONCENTRATOR_15L">Oxygen Concentrator 15L</option>
                <option value="SLEEP_APNEA_DEVICE">Sleep Apnea Device</option>
                <option value="OXYGEN_MASK">Oxygen Mask</option>
              </select>
              <input
                type="text"
                placeholder="Serial number"
                value={createDraft.serialNumber}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, serialNumber: e.target.value }))}
                className="premium-input"
              />
              <input
                type="text"
                placeholder="Manufacturer"
                value={createDraft.manufacturer}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, manufacturer: e.target.value }))}
                className="premium-input"
              />
              <input
                type="text"
                placeholder="Model"
                value={createDraft.model}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, model: e.target.value }))}
                className="premium-input"
              />
              <input
                type="number"
                step="0.01"
                min="0"
                placeholder="Purchase price"
                value={createDraft.purchasePrice}
                onChange={(e) => setCreateDraft((draft) => ({ ...draft, purchasePrice: e.target.value }))}
                className="premium-input"
              />
            </div>
          </Card>
        )}

        <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
          <Card><p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Available</p><p className="mt-3 text-3xl font-black text-emerald-100">{inventoryCounts.available}</p></Card>
          <Card><p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Deployed</p><p className="mt-3 text-3xl font-black text-sky-100">{inventoryCounts.deployed}</p></Card>
          <Card><p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">In maintenance</p><p className="mt-3 text-3xl font-black text-amber-100">{inventoryCounts.maintenance}</p></Card>
          <Card><p className="text-xs uppercase tracking-[0.22em] text-slate-300/65">Low stock</p><p className="mt-3 text-3xl font-black text-rose-100">{inventoryCounts.lowStock}</p></Card>
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

        <div className="grid grid-cols-1 gap-6 xl:grid-cols-3 pb-10">
          <div className="xl:col-span-2">
            <Card className="rounded-[28px]">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-black">Inventory assets</h2>
                <Badge variant={filteredDevices.length > 0 ? 'primary' : 'success'}>{filteredDevices.length}</Badge>
              </div>

              <div className="space-y-3 max-h-[36rem] overflow-y-auto pr-1 premium-scrollbar">
                {filteredDevices.length === 0 ? (
                  <div className="rounded-[24px] border border-white/10 bg-white/5 p-6">
                    <p className="text-slate-200 font-semibold">No devices match this filter</p>
                    <p className="text-slate-300/70 text-sm mt-2">Try another inventory segment or register a new device.</p>
                  </div>
                ) : (
                  filteredDevices.map((device) => (
                    <div
                      key={device.id}
                      onClick={() => setSelectedDevice(device)}
                      className={`p-4 border rounded-[24px] cursor-pointer transition-all ${
                        selectedDevice?.id === device.id
                          ? 'border-cyan-300/35 bg-cyan-400/10 shadow-[0_16px_40px_rgba(47,126,220,0.12)]'
                          : 'border-white/10 bg-white/5 hover:border-cyan-300/20 hover:bg-white/8'
                      }`}
                    >
                      <div className="flex justify-between items-start gap-4">
                        <div>
                          <p className="font-semibold text-slate-50">{device.displayName}</p>
                          <p className="text-sm text-slate-300/70">{device.manufacturer || 'Unknown manufacturer'} {device.model ? `· ${device.model}` : ''}</p>
                        </div>
                        <Badge variant={statusVariantMap[device.deviceStatus]}>{device.deviceStatus.replace(/_/g, ' ')}</Badge>
                      </div>

                      <div className="mt-3 grid grid-cols-2 gap-3 text-sm text-slate-300/75 md:grid-cols-4">
                        <div>
                          <p className="uppercase tracking-[0.16em] text-xs text-slate-400">Type</p>
                          <p className="mt-1 font-medium text-slate-100">{device.deviceType.replace(/_/g, ' ')}</p>
                        </div>
                        <div>
                          <p className="uppercase tracking-[0.16em] text-xs text-slate-400">Stock</p>
                          <p className="mt-1 font-medium text-slate-100">{device.quantityInStock ?? 1}</p>
                        </div>
                        <div>
                          <p className="uppercase tracking-[0.16em] text-xs text-slate-400">Consumable</p>
                          <p className="mt-1 font-medium text-slate-100">{device.isConsumable ? 'Yes' : 'No'}</p>
                        </div>
                        <div>
                          <p className="uppercase tracking-[0.16em] text-xs text-slate-400">Maintenance</p>
                          <p className="mt-1 font-medium text-slate-100">{device.requiresMaintenance ? 'Required' : 'Not due'}</p>
                        </div>
                      </div>

                      {device.isConsumable && (device.quantityInStock || 0) < 5 && (
                        <p className="mt-3 text-sm font-semibold text-amber-200">Low stock alert: {device.quantityInStock} units remaining</p>
                      )}
                    </div>
                  ))
                )}
              </div>
            </Card>
          </div>

          <div className="space-y-6">
            <Card className="rounded-[28px] sticky top-4">
              {selectedDevice ? (
                <>
                  <h3 className="font-black text-lg mb-4">Device Details</h3>
                  <div className="space-y-3 mb-6 text-sm">
                    <div>
                      <p className="text-slate-300/65">Display Name</p>
                      <p className="font-semibold text-slate-50">{selectedDevice.displayName}</p>
                    </div>
                    <div>
                      <p className="text-slate-300/65">Serial</p>
                      <p className="font-semibold text-slate-50">{selectedDevice.serialNumber}</p>
                    </div>
                    <div>
                      <p className="text-slate-300/65">Status</p>
                      <Badge variant={statusVariantMap[selectedDevice.deviceStatus]}>{selectedDevice.deviceStatus.replace(/_/g, ' ')}</Badge>
                    </div>
                    <div>
                      <p className="text-slate-300/65">Purchase Price</p>
                      <p className="font-semibold text-slate-50">{selectedDevice.purchasePrice != null ? `€${Number(selectedDevice.purchasePrice).toFixed(2)}` : '—'}</p>
                    </div>
                    <div>
                      <p className="text-slate-300/65">Usage</p>
                      <p className="font-semibold text-slate-50">{selectedDevice.totalRentalDays || 0} days · {selectedDevice.totalRentalHours || 0} hours</p>
                    </div>
                    <div>
                      <p className="text-slate-300/65">Maintenance Flag</p>
                      <p className="font-semibold text-slate-50">{selectedDevice.requiresMaintenance ? 'Yes' : 'No'}</p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Button variant="secondary" className="w-full" onClick={() => flagMaintenance(selectedDevice.id)} disabled={loading}>
                      Flag maintenance
                    </Button>
                    <Button variant="danger" className="w-full" onClick={() => decommissionDevice(selectedDevice.id)} disabled={loading || selectedDevice.deviceStatus === 'RETIRED'}>
                      Decommission device
                    </Button>
                  </div>
                </>
              ) : (
                <p className="text-slate-300/70 text-center py-8">Select a device to view details and manage inventory actions</p>
              )}
            </Card>

            <Card className="rounded-[28px]">
              <h3 className="font-black text-lg mb-4">Inventory alerts</h3>
              <div className="space-y-3">
                {devices.filter((device) => device.requiresMaintenance).slice(0, 3).map((device) => (
                  <AlertBanner
                    key={`maint-${device.id}`}
                    type="warning"
                    title="Maintenance required"
                    message={`${device.displayName} needs maintenance review.`}
                  />
                ))}
                {devices.filter((device) => device.isConsumable && (device.quantityInStock || 0) < 5).slice(0, 3).map((device) => (
                  <AlertBanner
                    key={`stock-${device.id}`}
                    type="danger"
                    title="Low stock"
                    message={`${device.displayName} has only ${device.quantityInStock} units in stock.`}
                  />
                ))}
                {devices.length === 0 && (
                  <p className="text-slate-300/70 text-sm">No inventory records loaded yet. Register devices to populate this view.</p>
                )}
              </div>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}