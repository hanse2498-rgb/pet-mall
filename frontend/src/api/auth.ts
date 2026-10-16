import { http } from './http'

export interface LoginResult {
  token: string
  userId: number
  nickname: string
}

export interface SmsResult {
  devCode?: string
  expireSeconds: number
  resendSeconds: number
}

export interface UserInfo {
  id: number
  nickname: string
  avatar?: string
  phone?: string
}

export function sendSmsCode(phone: string) {
  return http.post<SmsResult>('/auth/sms-code', { phone })
}

export function registerApi(phone: string, code: string, password: string, nickname?: string) {
  return http.post<LoginResult>('/auth/register', { phone, code, password, nickname })
}

export function loginByCodeApi(phone: string, code: string) {
  return http.post<LoginResult>('/auth/login/code', { phone, code })
}

export function loginByPasswordApi(phone: string, password: string) {
  return http.post<LoginResult>('/auth/login/password', { phone, password })
}

export function forgotPasswordApi(phone: string, code: string, newPassword: string) {
  return http.post<void>('/auth/forgot-password', { phone, code, newPassword })
}

export function meApi() {
  return http.get<UserInfo>('/auth/me')
}

export function logoutApi() {
  return http.post<void>('/auth/logout')
}