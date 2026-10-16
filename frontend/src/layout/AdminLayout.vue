<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="aside">
      <div class="logo">宠物商城 · 后台</div>
      <el-menu :default-active="activeMenu" router background-color="#001529" text-color="#c0c4cc"
        active-text-color="#fff">
        <template v-for="m in menuItems" :key="m.path">
          <div v-if="m.divider" class="menu-divider" />
          <el-menu-item :index="m.path">
            <el-icon><component :is="m.icon" /></el-icon>
            <span>{{ m.label }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span>后台管理系统</span>
        <el-dropdown>
          <span class="admin-name">{{ adminStore.adminName || '管理员' }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="onLogout">退出登录</el-dropdown-item>
              <el-dropdown-item @click="router.push('/')">回到商城</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Odometer, Goods, List, User, Timer, RefreshLeft, Collection } from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

/** 后台菜单（数组驱动：调顺序/新增板块只改这个数组，第一项即默认落地页）
 *  divider: true 表示在该项之前插入一条分割线（用于分组）
 */
const menuItems = [
  { path: '/admin/users', icon: User, label: '用户管理' },
  { path: '/admin/products', icon: Goods, label: '商品管理' },
  { path: '/admin/orders', icon: List, label: '订单管理' },
  { path: '/admin/seckill', icon: Timer, label: '秒杀管理' },
  { path: '/admin/after-sale', icon: RefreshLeft, label: '售后管理' },
  { path: '/admin/dashboard', icon: Odometer, label: '数据看板', divider: true },
  { path: '/admin/pet-breed', icon: Collection, label: '宠物档案' },
]

/** 当前高亮菜单：前缀匹配，兜底为第一个菜单项 */
const activeMenu = computed(() => {
  const hit = menuItems.find((m) => route.path === m.path || route.path.startsWith(m.path + '/'))
  return hit ? hit.path : menuItems[0].path
})

async function onLogout() {
  await adminStore.logout()
  ElMessage.success('已退出')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.aside {
  background: #001529;
}
.logo {
  color: #fff;
  font-weight: 700;
  padding: 18px 16px;
  font-size: 15px;
}
.aside :deep(.el-menu) {
  border-right: none;
}
/* 菜单分组分割线（深色侧栏用低对比白） */
.menu-divider {
  height: 1px;
  margin: 8px 16px;
  background: rgba(255, 255, 255, 0.14);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}
.admin-name {
  cursor: pointer;
  color: #409eff;
}
</style>