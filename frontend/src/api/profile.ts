import { http } from './http'

export interface ProfileProduct {
  productId: number
  name: string
  subtitle?: string
  coverImg?: string
  minPrice?: number
  saleType: number
  isNearExpiry: number
  favoriteTime?: string
  lastViewTime?: string
  viewCount?: number
}

export function getFavorites() {
  return http.get<ProfileProduct[]>('/profile/favorite/list')
}

export function favoriteStatus(productId: number) {
  return http.get<{ favorited: boolean }>(`/profile/favorite/status/${productId}`)
}

export function toggleFavorite(productId: number) {
  return http.post<{ favorited: boolean }>('/profile/favorite/toggle', { productId })
}

export function recordFootprint(productId: number) {
  return http.post<void>('/profile/footprint', { productId })
}

export function getFootprints() {
  return http.get<ProfileProduct[]>('/profile/footprint/list')
}