import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/notice'
import { useUserStore } from './user'

export const useNoticeStore = defineStore('notice', () => {
  const count = ref(0)

  async function fetchCount() {
    const userStore = useUserStore()
    if (!userStore.isLogin) {
      count.value = 0
      return
    }
    try {
      const r = await getUnreadCount()
      count.value = Number(r.count ?? 0)
    } catch {
      // 忽略未登录/网络异常
    }
  }

  function setCount(n: number) {
    count.value = Number(n) || 0
  }

  function clear() {
    count.value = 0
  }

  return { count, fetchCount, setCount, clear }
})
