import { defineStore } from 'pinia'
import {
  loginByCodeApi,
  loginByPasswordApi,
  meApi,
  registerApi,
  type LoginResult,
} from '@/api/auth'

interface UserInfo {
  id: number
  nickname: string
  avatar?: string
  phone?: string
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null as UserInfo | null,
  }),
  getters: {
    isLogin: (state) => !!state.token,
  },
  actions: {
    applyAuth(res: LoginResult) {
      this.token = res.token
      localStorage.setItem('token', res.token)
      this.userInfo = { id: res.userId, nickname: res.nickname }
    },
    async loginByCode(phone: string, code: string) {
      this.applyAuth(await loginByCodeApi(phone, code))
    },
    async loginByPassword(phone: string, password: string) {
      this.applyAuth(await loginByPasswordApi(phone, password))
    },
    async register(phone: string, code: string, password: string, nickname?: string) {
      this.applyAuth(await registerApi(phone, code, password, nickname))
    },
    async fetchMe() {
      if (!this.token) return
      this.userInfo = await meApi()
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    },
  },
})