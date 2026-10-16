import { http } from './http'
import type { OrderVO } from './order'

export interface DashboardKpi {
  salesAmount: number
  orderCount: number
  paidOrderCount: number
  avgPrice: number
  refundAmount: number
  newUsers: number
  pendingShip: number
  pendingAfterSale: number
}
export interface TrendPoint {
  date: string
  salesAmount: number
  orderCount: number
}
export interface StatusCount {
  status: number
  count: number
}
export interface CategoryShare {
  categoryName: string
  amount: number
  qty: number
}
export interface TopProduct {
  productId: number
  name: string
  qty: number
  amount: number
}
export interface StockAlert {
  productName: string
  spec?: string
  stock: number
}
export interface Dashboard {
  range: number
  kpi: DashboardKpi
  trend: TrendPoint[]
  statusDist: StatusCount[]
  categoryShare: CategoryShare[]
  topProducts: TopProduct[]
  latestOrders: OrderVO[]
  stockAlerts: StockAlert[]
}

/** 后台数据看板 */
export function adminDashboard(range = 30) {
  return http.get<Dashboard>('/admin/dashboard', { params: { range } })
}
