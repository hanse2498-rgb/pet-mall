import { http } from './http'

export interface CartItem {
  cartId: number
  skuId: number
  productId: number
  productName: string
  coverImg?: string
  specName?: string
  price: number
  originalPrice?: number
  isDiscount: number
  stock: number
  quantity: number
  selected: number
  isNearExpiry: number
  subtotal: number
}

export function getCart() {
  return http.get<CartItem[]>('/cart')
}

export function getCartSelected() {
  return http.get<CartItem[]>('/cart/selected')
}

export function getCartCount() {
  return http.get<{ count: number }>('/cart/count')
}

export function addToCart(skuId: number, quantity: number) {
  return http.post<CartItem>('/cart', { skuId, quantity })
}

export function updateCartItem(id: number, quantity?: number, selected?: number) {
  return http.put<void>(`/cart/${id}`, null, { params: { quantity, selected } })
}

export function removeCartItem(id: number) {
  return http.delete<void>(`/cart/${id}`)
}

export function removeCartBatch(skuIds: number[]) {
  return http.post<void>('/cart/remove-batch', skuIds)
}