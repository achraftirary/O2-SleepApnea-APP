import { create } from 'zustand'
import axios, { AxiosInstance } from 'axios'

export interface DashboardData {
  totalActiveRentals: number
  totalOverduePickups: number
  totalUnpaidInvoices: number
  totalUnpaidAmount: number
  lowStockAlerts: number
  dailyRevenueExpected: number
  dailyRevenueCollected: number
  collectionRate: number
  pickupsToday: any[]
  overdueRentals: any[]
  overdueInvoices: any[]
  criticalAlerts: any[]
  lowStockDevices: any[]
  devicesNeedingMaintenance: any[]
  totalDevicesAvailable: number
  totalDevicesDeployed: number
  totalDevicesInMaintenance: number
}

interface AppStore {
  dashboardData: DashboardData | null
  loading: boolean
  error: string | null
  fetchDashboard: () => Promise<void>
  clearError: () => void
}

const apiClient = axios.create({
  baseURL: '/api',
})

// Interceptors for error handling and auth can be added here

export const useAppStore = create<AppStore>((set) => ({
  dashboardData: null,
  loading: false,
  error: null,

  fetchDashboard: async () => {
    set({ loading: true, error: null })
    try {
      const response = await apiClient.get('/dashboard/agent')
      set({ dashboardData: response.data, loading: false })
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || 'Failed to load dashboard'
      set({ error: errorMsg, loading: false })
    }
  },

  clearError: () => set({ error: null }),
}))

export default apiClient
