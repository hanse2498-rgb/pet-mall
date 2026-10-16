<template>
  <div class="page-container seckill">
    <!-- 场次头 -->
    <div class="header" v-loading="loading">
      <template v-if="activity">
        <div class="title">
          <span class="fire"><el-icon :size="22"><Lightning /></el-icon></span> {{ activity.name || '秒杀专区' }}
          <el-tag :type="stateTag" effect="dark" size="small" class="state">{{ stateText }}</el-tag>
        </div>
        <div class="countdown" v-if="activity.state !== 2">
          <span class="cd-label">{{ activity.state === 0 ? '距开始' : '距结束' }}</span>
          <span class="cd-nums">{{ countdownText }}</span>
        </div>
        <div class="countdown done" v-else>本场已结束，下一场敬请期待</div>
      </template>
      <el-empty v-else-if="!loading" description="暂无秒杀场次" :image-size="70" />
    </div>

    <!-- 商品 -->
    <div class="grid" v-if="activity && activity.items.length">
      <div v-for="it in activity.items" :key="it.itemId" class="card" @click="goProduct(it)">
        <div class="cover">
          <img :src="it.coverImg || placeholder" loading="lazy" />
          <span class="badge">秒杀</span>
        </div>
        <div class="name">{{ it.productName }}</div>
        <div class="spec">{{ it.specName }}</div>
        <div class="price-row">
          <span class="price">¥{{ num(it.seckillPrice) }}</span>
          <span class="origin">¥{{ num(it.normalPrice) }}</span>
        </div>
        <div class="progress">
          <el-progress :percentage="it.percent" :stroke-width="6" :show-text="false" />
          <span class="left">已抢 {{ it.percent }}% · 剩 {{ it.seckillStock }} 件</span>
        </div>
        <el-button
          class="buy"
          type="danger"
          :disabled="activity.state !== 1 || it.seckillStock <= 0"
          @click.stop="onBuy(it)"
        >
          {{ activity.state !== 1 ? (activity.state === 0 ? '未开始' : '已结束') : it.seckillStock <= 0 ? '已抢光' : '立即秒杀' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCurrentSeckill, getSeckillActivity, type SeckillActivity, type SeckillItemVO } from '@/api/seckill'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activity = ref<SeckillActivity | null>(null)
const placeholder = '/products/placeholder.svg'

let timer: number | undefined
const nowMs = ref(Date.now())
// 服务器时间 - 客户端时间：仅在加载场次时校准一次，供倒计时持续递减
const clockOffset = ref(0)
const autoRefreshed = ref(false)

const stateTag = computed(() => (activity.value?.state === 1 ? 'danger' : activity.value?.state === 0 ? 'warning' : 'info'))
const stateText = computed(() => (activity.value?.state === 1 ? '抢购中' : activity.value?.state === 0 ? '即将开始' : '已结束'))

const countdownText = computed(() => {
  const a = activity.value
  if (!a) return '--'
  const target = a.state === 0 ? new Date(a.startTime).getTime() : new Date(a.endTime).getTime()
  let diff = target - (nowMs.value + clockOffset.value)
  if (diff < 0) diff = 0
  const s = Math.floor(diff / 1000)
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = s % 60
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${pad(h)}:${pad(m)}:${pad(sec)}`
})

function num(n: number | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

function goProduct(it: SeckillItemVO) {
  router.push(`/products/${it.productId}`)
}

async function onBuy(it: SeckillItemVO) {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  router.push({
    name: 'SeckillCheckout',
    query: { activityId: String(activity.value!.activityId), itemId: String(it.itemId) },
  })
}

async function load() {
  loading.value = true
  try {
    activity.value = route.params.activityId
      ? await getSeckillActivity(route.params.activityId as string)
      : await getCurrentSeckill()
    if (activity.value?.serverTime) {
      clockOffset.value = new Date(activity.value.serverTime).getTime() - Date.now()
    }
    autoRefreshed.value = false
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  timer = window.setInterval(() => {
    nowMs.value = Date.now()
    // 倒计时到 0：自动刷新获取新状态（未开始→抢购中→下一场）
    const a = activity.value
    if (a && a.state !== 2 && !autoRefreshed.value) {
      const target = a.state === 0 ? new Date(a.startTime).getTime() : new Date(a.endTime).getTime()
      if (target - (nowMs.value + clockOffset.value) <= 0) {
        autoRefreshed.value = true
        load()
      }
    }
  }, 1000)
})
onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.seckill {
  max-width: 1100px;
}
.header {
  background: linear-gradient(135deg, #fff1f0, #fff8f6);
  border: 1px solid #ffd9d9;
  border-radius: 12px;
  padding: 22px 24px;
  margin-bottom: 18px;
}
.title {
  font-size: 22px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
}
.state {
  margin-left: auto;
}
.countdown {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.cd-label {
  color: #909399;
}
.cd-nums {
  font-family: Consolas, monospace;
  font-size: 26px;
  font-weight: 700;
  color: #f56c6c;
  letter-spacing: 2px;
}
.countdown.done {
  color: #909399;
}
.grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}
.card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  padding-bottom: 12px;
}
.cover {
  position: relative;
}
.cover img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  display: block;
}
.badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}
.name {
  padding: 8px 10px 0;
  font-weight: 600;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.spec {
  padding: 0 10px;
  color: #909399;
  font-size: 12px;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 4px 10px 0;
}
.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: 800;
}
.origin {
  color: #c0c4cc;
  text-decoration: line-through;
  font-size: 12px;
}
.progress {
  padding: 6px 10px;
}
.left {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-top: 2px;
}
.buy {
  margin: 4px 10px 0;
  width: calc(100% - 20px);
}
@media (max-width: 1100px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 700px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>