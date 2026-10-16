import { http } from './http'
import type { PageResult } from './order'

export interface AfterSaleVO {
  id: number
  afterSaleNo: string
  orderId: number
  orderNo?: string
  reason?: string
  description?: string
  amount: number
  status: number
  adminRemark?: string
  auditTime?: string
  refundTime?: string
  createTime: string
}

export const AfterSaleStatusMap: Record<number, string> = {
  0: '待审核',
  1: '已通过',
  2: '已拒绝',
  3: '已撤销',
}

export const AfterSaleStatusTagType: Record<number, 'warning' | 'success' | 'danger' | 'info'> = {
  0: 'warning',
  1: 'success',
  2: 'danger',
  3: 'info',
}

/** 申请售后（整单） */
export function applyAfterSale(data: { orderId: number; reason?: string; description?: string }) {
  return http.post<number>('/after-sale', data)
}

/** 我的售后列表 */
export function getMyAfterSales(pageNum = 1, pageSize = 10) {
  return http.get<PageResult<AfterSaleVO>>('/after-sale', { params: { pageNum, pageSize } })
}

export function getAfterSale(id: number) {
  return http.get<AfterSaleVO>(`/after-sale/${id}`)
}

export function cancelAfterSale(id: number) {
  return http.post<void>(`/after-sale/${id}/cancel`)
}
