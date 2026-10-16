import { defineStore } from 'pinia'
import { adminLogin, adminLogout, type AdminInfo } from '@/api/admin'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem('adminToken') || '',
    adminName: localStorage.getItem('adminName') || '',
  }),
  getters: {
    isLogin: (state) => !!state.token,
  },
  actions: {
    async login(username: string, password: string) {
      const res = await adminLogin(username, password)
      this.token = res.token
      this.adminName = res.nickname
      localStorage.setItem('adminToken', res.token)
      localStorage.setItem('adminName', res.nickname)
    },
    async logout() {
      try {
        await adminLogout()
      } catch {
        // 忽略登出异常
      }
      this.token = ''
      this.adminName = ''
      localStorage.removeItem('adminToken')
      localStorage.removeItem('adminName')
    },
  },
})