<template>
  <el-container class="layout">
    <el-header class="layout-header">
      <div class="header-left">
        <el-icon class="menu-btn" :size="22" @click="drawer = true"><Menu /></el-icon>
        <router-link class="logo" to="/">宠物商城</router-link>
      </div>
      <div class="header-search">
        <SearchBar />
      </div>
      <div class="header-right">
        <router-link class="assistant-link" to="/assistant">
          <el-icon><ChatDotRound /></el-icon><span>问答助手</span>
        </router-link>
        <el-popover
          :width="320"
          trigger="click"
          placement="bottom-end"
          :popper-style="{ padding: '0' }"
          popper-class="notice-pop"
          @show="loadRecent"
        >
          <template #reference>
            <el-badge :value="noticeStore.count" :hidden="noticeStore.count === 0" :max="99" class="notice-badge">
              <el-icon class="bell-icon" :size="20" title="消息"><Bell /></el-icon>
            </el-badge>
          </template>
          <div class="np-head">
            <span>消息</span>
            <el-link type="primary" :underline="false" @click="onReadAll">全部已读</el-link>
          </div>
          <div v-if="!recent.length" class="np-empty">暂无消息</div>
          <div
            v-for="n in recent"
            :key="n.id"
            class="np-item"
            :class="{ unread: n.isRead === 0 }"
            @click="openNotice(n)"
          >
            <div class="np-title">{{ n.title }}</div>
            <div class="np-time">{{ fmtTime(n.createTime) }}</div>
          </div>
          <div class="np-foot" @click="router.push('/notices')">查看全部 ›</div>
        </el-popover>
        <el-badge :value="cartStore.count" :hidden="cartStore.count === 0" :max="99" class="cart-badge">
          <el-icon class="cart-icon" @click="router.push('/cart')" title="购物车">
            <ShoppingCart />
          </el-icon>
        </el-badge>
        <template v-if="userStore.isLogin">
          <el-dropdown>
            <span class="user-name">
              <el-avatar :size="26" class="avatar">{{ (userStore.userInfo?.nickname || '我').slice(0, 1) }}</el-avatar>
              <span class="uname">{{ userStore.userInfo?.nickname || '我' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <el-button v-else type="primary" round @click="router.push('/login')">登录 / 注册</el-button>
      </div>
    </el-header>

    <el-container class="layout-body">
      <el-aside class="layout-aside" width="180px">
        <nav class="side-nav">
          <router-link
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="side-item"
          >
            <el-icon :size="18"><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>
        </nav>
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>

    <!-- 小屏汉堡抽屉 -->
    <el-drawer v-model="drawer" direction="ltr" size="220px" :with-header="false">
      <div class="drawer-logo">宠物商城</div>
      <nav class="side-nav">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="side-item"
          @click="drawer = false"
        >
          <el-icon :size="18"><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
    </el-drawer>
  </el-container>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useNoticeStore } from '@/stores/notice'
import { getNotices, readNotice, readAllNotices, type Notice } from '@/api/notice'
import SearchBar from '@/components/SearchBar.vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const noticeStore = useNoticeStore()

const drawer = ref(false)
const recent = ref<Notice[]>([])
let noticeTimer: number | undefined

async function loadRecent() {
  if (!userStore.isLogin) {
    recent.value = []
    return
  }
  try {
    const res = await getNotices(undefined, 1, 6)
    recent.value = res.list
  } catch {
    recent.value = []
  }
}

async function openNotice(n: Notice) {
  if (n.isRead === 0) {
    await readNotice(n.id)
    n.isRead = 1
    noticeStore.fetchCount()
  }
  if (n.bizId) {
    router.push(`/orders/${n.bizId}`)
  }
}

async function onReadAll() {
  await readAllNotices()
  recent.value.forEach((n) => (n.isRead = 1))
  noticeStore.setCount(0)
}

function fmtTime(t?: string): string {
  return t ? t.replace('T', ' ').slice(5, 16) : ''
}

const navItems = [
  { to: '/', label: '首页', icon: 'HomeFilled' },
  { to: '/products', label: '全部商品', icon: 'Goods' },
  { to: '/ranking', label: '好用排行', icon: 'Trophy' },
  { to: '/near-expiry', label: '临期专区', icon: 'Timer' },
  { to: '/seckill', label: '秒杀专区', icon: 'Lightning' },
  { to: '/pets', label: '宠物档案', icon: 'Collection' },
  { to: '/community', label: '宠物社区', icon: 'ChatDotRound' },
]

function onLogout() {
  userStore.logout()
  cartStore.setCount(0)
  ElMessage.success('已退出登录')
  router.push('/')
}

watch(
  () => userStore.token,
  (token) => {
    if (token) {
      cartStore.fetchCount()
      noticeStore.fetchCount()
    } else {
      cartStore.setCount(0)
      noticeStore.clear()
    }
  },
  { immediate: true },
)

onMounted(() => {
  if (userStore.isLogin) {
    cartStore.fetchCount()
  }
  noticeStore.fetchCount()
  noticeTimer = window.setInterval(() => noticeStore.fetchCount(), 30000)
})

onBeforeUnmount(() => {
  if (noticeTimer) clearInterval(noticeTimer)
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
}
/* ===== 顶部栏：左 标志 / 中 搜索 / 右 操作 ===== */
.layout-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  box-shadow: 0 1px 12px rgba(31, 35, 41, 0.06);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-self: start;
  min-width: 0;
}
.header-search {
  width: 440px;
  max-width: 100%;
}
.header-right {
  display: flex;
  align-items: center;
  justify-self: end;
}
.logo {
  font-size: 20px;
  font-weight: 800;
  color: #ff6a3d;
  white-space: nowrap;
}
.menu-btn {
  display: none;
  cursor: pointer;
  color: #4a4f57;
}
.menu-btn:hover {
  color: var(--el-color-primary);
}

/* ===== 左侧导航栏 ===== */
.layout-body {
  align-items: stretch;
}
.layout-aside {
  position: sticky;
  top: 60px;
  height: calc(100vh - 60px);
  background: #fff;
  border-right: 1px solid #f0ece8;
  padding: 16px 12px;
  overflow-y: auto;
}
.side-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.side-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  color: #4a4f57;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  white-space: nowrap;
}
.side-item:hover {
  background: #fff4ef;
  color: var(--el-color-primary);
}
.side-item.router-link-exact-active {
  background: #ff6a3d;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(255, 106, 61, 0.28);
}
.layout-main {
  padding: 0;
  background: #faf7f5;
}

/* 右侧操作区 */
.user-name {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #4a4f57;
}
.avatar {
  background: #ff8a50;
  font-weight: 600;
}
.cart-badge {
  margin: 0 20px;
}
.assistant-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #4a4f57;
  margin-right: 20px;
  cursor: pointer;
  font-size: 14px;
}
.assistant-link:hover {
  color: var(--el-color-primary);
}
.cart-icon {
  font-size: 22px;
  cursor: pointer;
  color: #4a4f57;
}
.cart-icon:hover {
  color: var(--el-color-primary);
}

/* 抽屉 */
.drawer-logo {
  font-size: 18px;
  font-weight: 800;
  color: #ff6a3d;
  padding: 4px 8px 16px;
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .assistant-link span,
  .uname {
    display: none;
  }
}
@media (max-width: 900px) {
  .layout-aside {
    display: none;
  }
  .menu-btn {
    display: inline-flex;
  }
  .header-search {
    width: auto;
  }
}
.notice-badge {
  margin-right: 20px;
}
.bell-icon {
  font-size: 20px;
  cursor: pointer;
  color: #4a4f57;
}
.bell-icon:hover {
  color: var(--el-color-primary);
}
</style>

<!-- 消息下拉面板（popover 内容挂在 body，需非 scoped） -->
<style>
.notice-pop .np-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f2f5;
  font-weight: 600;
}
.notice-pop .np-empty {
  padding: 24px;
  text-align: center;
  color: #9aa1ab;
  font-size: 13px;
}
.notice-pop .np-item {
  padding: 10px 14px;
  border-bottom: 1px solid #f7f5f3;
  cursor: pointer;
}
.notice-pop .np-item:hover {
  background: #faf7f5;
}
.notice-pop .np-item.unread .np-title::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #f0412f;
  margin-right: 6px;
  vertical-align: middle;
}
.notice-pop .np-title {
  font-size: 13px;
  color: #20242a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-pop .np-time {
  font-size: 12px;
  color: #9aa1ab;
  margin-top: 2px;
}
.notice-pop .np-foot {
  text-align: center;
  padding: 10px;
  font-size: 13px;
  color: var(--el-color-primary);
  cursor: pointer;
}
</style>