import { http } from './http'

export interface SeckillItemVO {
  itemId: number
  skuId: number
  productId: number
  productName: string
  coverImg?: string
  specName?: string
  normalPrice: number
  seckillPrice: number
  seckillStock: number
  seckillSold: number
  percent: number
  perUserLimit: number
}

export interface SeckillActivity {
  activityId: number
  name: string
  startTime: string
  endTime: string
  /** 0未开始 1进行中 2已结束 */
  state: number
  serverTime: string
  items: SeckillItemVO[]
}

export function getCurrentSeckill() {
  return http.get<SeckillActivity>('/seckill/current')
}

export function getSeckillActivity(id: number | string) {
  return http.get<SeckillActivity>(`/seckill/activity/${id}`)
}

export function createSeckillOrder(data: {
  activityId: number | string
  itemId: number | string
  addressId: number
  quantity: number
}) {
  return http.post<{ orderId: number }>('/seckill/order', data)
}