import { http } from './http'

export const OrderStatusMap: Record<number, string> = {
  0: '待支付',
  1: '待发货',
  2: '待收货',
  3: '已完成',
  4: '已取消',
  5: '售后中',
}

export const OrderStatusTagType: Record<number, 'warning' | 'primary' | 'success' | 'info' | 'danger'> = {
  0: 'warning',
  1: 'primary',
  2: 'success',
  3: 'success',
  4: 'info',
  5: 'danger',
}

export interface OrderItemVO {
  skuId: number
  productName: string
  skuImg?: string
  spec?: string
  price: number
  quantity: number
  subtotal: number
  produceDate?: string
  expiryDate?: string
}

export interface OrderVO {
  orderId: number
  orderNo: string
  status: number
  goodsAmount: number
  discountAmount: number
  freight: number
  payAmount: number
  remark?: string
  createTime: string
  payTime?: string
  shipTime?: string
  finishTime?: string
  cancelTime?: string
  items: OrderItemVO[]
  receiver?: string
  payNo?: string
  payStatus?: number
  paymentTime?: string
  logisticsCompany?: string
  trackingNo?: string
}

export interface PageResult<T> {
  total: number
  list: T[]
}

export interface CreateOrderParam {
  addressId: number
  remark?: string
  clientToken: string
  items: { skuId: number; quantity: number }[]
}

export function createOrder(data: CreateOrderParam) {
  return http.post<OrderVO>('/order', data)
}

export function getOrders(status?: number, pageNum = 1, pageSize = 10) {
  return http.get<PageResult<OrderVO>>('/order', { params: { status, pageNum, pageSize } })
}

export function getOrder(id: number | string) {
  return http.get<OrderVO>(`/order/${id}`)
}

export function cancelOrder(id: number | string) {
  return http.post<void>(`/order/${id}/cancel`)
}

export function receiveOrder(id: number | string) {
  return http.post<void>(`/order/${id}/receive`)
}

export interface PayResult {
  orderId: number
  orderNo: string
  amount: number
  paid: boolean
  message: string
}

export function payOrder(id: number | string, simulateFail = false) {
  return http.post<PayResult>(`/order/${id}/pay`, { simulateFail })
}