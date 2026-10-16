import { http } from './http'

export interface ReviewItem {
  id: number
  productId?: number
  productName?: string
  coverImg?: string
  nickname: string
  avatar?: string
  skuSpec?: string
  rating: number
  content: string
  images: string[]
  createTime: string
}

export interface ReviewSummary {
  avgRating: number
  total: number
  goodRate: number
  c5: number
  c4: number
  c3: number
  c2: number
  c1: number
}

export interface ReviewPage {
  summary: ReviewSummary
  total: number
  list: ReviewItem[]
}

export function getProductReviews(productId: number, pageNum = 1, pageSize = 5) {
  return http.get<ReviewPage>(`/review/product/${productId}`, { params: { pageNum, pageSize } })
}

export function getMyReviews(pageNum = 1, pageSize = 10) {
  return http.get<ReviewPage>('/review/mine', { params: { pageNum, pageSize } })
}

export function submitReview(data: {
  orderId: number
  productId: number
  skuId: number
  rating: number
  content: string
  images?: string[]
}) {
  return http.post<void>('/review', data)
}