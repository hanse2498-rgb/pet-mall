import { http } from './http'

export interface Address {
  id: number
  userId: number
  receiverName: string
  phone: string
  province?: string
  city?: string
  district?: string
  detail: string
  isDefault: number
}

export function listAddresses() {
  return http.get<Address[]>('/address')
}

export function addAddress(data: Partial<Address>) {
  return http.post<Address>('/address', data)
}

export function updateAddress(data: Partial<Address>) {
  return http.put<void>('/address', data)
}

export function deleteAddress(id: number) {
  return http.delete<void>(`/address/${id}`)
}

export function setDefaultAddress(id: number) {
  return http.put<void>(`/address/${id}/default`)
}