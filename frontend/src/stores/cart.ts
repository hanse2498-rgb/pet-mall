import { defineStore } from 'pinia'
import { getCartCount } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    count: 0,
  }),
  actions: {
    async fetchCount() {
      try {
        const res = await getCartCount()
        this.count = res.count
      } catch {
        this.count = 0
      }
    },
    setCount(n: number) {
      this.count = n
    },
  },
})