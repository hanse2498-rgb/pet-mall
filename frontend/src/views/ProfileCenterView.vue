<template>
  <div class="page-container">
    <div class="profile-wrap">
      <!-- 左侧菜单 -->
      <aside class="profile-menu">
        <div class="user-card">
          <el-avatar :size="56" :src="userStore.userInfo?.avatar || undefined" class="avatar">{{ nickChar }}</el-avatar>
          <div class="user-info">
            <div class="nick">{{ userStore.userInfo?.nickname || '宠物家长' }}</div>
            <div class="sub">{{ maskedPhone }}</div>
          </div>
        </div>
        <el-menu :default-active="active" class="menu" @select="onSelect">
          <el-menu-item index="orders">
            <el-icon><List /></el-icon><span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="reviews">
            <el-icon><ChatDotRound /></el-icon><span>我的评价</span>
          </el-menu-item>
          <el-menu-item index="favorites">
            <el-icon><Star /></el-icon><span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="footprints">
            <el-icon><Clock /></el-icon><span>浏览足迹</span>
          </el-menu-item>
          <el-menu-item index="aftersales">
            <el-icon><RefreshLeft /></el-icon><span>我的售后</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- 右侧内容 -->
      <section class="profile-main">
        <!-- 我的订单 -->
        <el-card v-if="active === 'orders'">
          <template #header>
            <div class="inline-head">
              <span>我的订单</span>
              <el-button link type="primary" @click="router.push('/orders')">订单中心 ›</el-button>
            </div>
          </template>

          <el-tabs v-model="orderStatus" @tab-change="onOrderTab">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="待支付" name="0" />
            <el-tab-pane label="待发货" name="1" />
            <el-tab-pane label="待收货" name="2" />
            <el-tab-pane label="已完成" name="3" />
            <el-tab-pane label="已取消" name="4" />
          </el-tabs>

          <div v-loading="orderLoading">
            <el-empty v-if="!orderLoading && !orders.length" description="暂无订单" :image-size="70" />
            <div v-for="o in orders" :key="o.orderId" class="inline-order">
              <div class="io-head">
                <span class="gray">{{ o.orderNo }} · {{ fmt(o.createTime) }}</span>
                <el-tag :type="OrderStatusTagType[o.status]" size="small" effect="dark">
                  {{ OrderStatusMap[o.status] }}
                </el-tag>
              </div>
              <div class="io-body">
                <div class="io-items">
                  <span v-for="it in o.items" :key="it.skuId" class="io-tag">
                    {{ it.productName }} ×{{ it.quantity }}
                  </span>
                </div>
                <div class="io-amount">实付 <b class="red">¥{{ num(o.payAmount) }}</b></div>
                <div class="io-actions">
                  <el-button v-if="o.status === 0" size="small" type="danger" @click="router.push(`/pay/${o.orderId}`)">去支付</el-button>
                  <el-button v-if="o.status === 2" size="small" type="success" @click="onReceive(o)">确认收货</el-button>
                  <el-button v-if="o.status === 0 || o.status === 1" size="small" plain @click="onCancel(o)">取消订单</el-button>
                  <el-button size="small" link @click="router.push(`/orders/${o.orderId}`)">详情</el-button>
                </div>
              </div>
            </div>

            <div v-if="orderTotal > orderPageSize" class="pager">
              <el-pagination background layout="prev, pager, next" :total="orderTotal" :page-size="orderPageSize"
                :current-page="orderPage" @current-change="onOrderPage" />
            </div>
          </div>
        </el-card>

        <!-- 我的评价 -->
        <el-card v-else-if="active === 'reviews'" v-loading="reviewLoading">
          <template #header>我的评价（{{ reviewTotal }}）</template>
          <el-empty v-if="!reviewLoading && !reviews.length" description="还没有发表过评价" />
          <div v-for="rv in reviews" :key="rv.id" class="row-item">
            <img :src="rv.coverImg || placeholder" class="row-img" @click="goProduct(rv.productId)" />
            <div class="row-meta">
              <div class="row-title" @click="goProduct(rv.productId)">{{ rv.productName }}</div>
              <el-rate :model-value="rv.rating" disabled size="small" />
              <div class="row-text">{{ rv.content }}</div>
            </div>
            <div class="row-right">
              <div class="gray">{{ fmt(rv.createTime) }}</div>
              <el-button link type="primary" @click="goProduct(rv.productId)">查看商品</el-button>
            </div>
          </div>
        </el-card>

        <!-- 我的收藏 -->
        <el-card v-else-if="active === 'favorites'" v-loading="favLoading">
          <template #header>我的收藏（{{ favorites.length }}）</template>
          <el-empty v-if="!favLoading && !favorites.length" description="还没有收藏商品">
            <el-button type="primary" @click="router.push('/products')">去逛逛</el-button>
          </el-empty>
          <div v-for="p in favorites" :key="p.productId" class="row-item">
            <img :src="p.coverImg || placeholder" class="row-img" @click="goProduct(p.productId)" />
            <div class="row-meta" @click="goProduct(p.productId)">
              <div class="row-title">{{ p.name }}</div>
              <div class="row-price">¥{{ num(p.minPrice) }}</div>
            </div>
            <div class="row-right">
              <div class="gray">{{ fmt(p.favoriteTime) }} 收藏</div>
              <el-button link type="danger" @click="onUnfavorite(p)">取消收藏</el-button>
            </div>
          </div>
        </el-card>

        <!-- 浏览足迹 -->
        <el-card v-else-if="active === 'footprints'" v-loading="fpLoading">
          <template #header>浏览足迹（{{ footprints.length }}）</template>
          <el-empty v-if="!fpLoading && !footprints.length" description="还没有浏览记录" />
          <div v-for="p in footprints" :key="p.productId" class="row-item">
            <img :src="p.coverImg || placeholder" class="row-img" @click="goProduct(p.productId)" />
            <div class="row-meta" @click="goProduct(p.productId)">
              <div class="row-title">{{ p.name }}</div>
              <div class="gray">浏览 {{ p.viewCount || 1 }} 次</div>
            </div>
            <div class="row-right">
              <div class="gray">最近 {{ fmt(p.lastViewTime) }}</div>
              <el-button link type="primary" @click="goProduct(p.productId)">再次查看</el-button>
            </div>
          </div>
        </el-card>

        <!-- 我的售后 -->
        <el-card v-else-if="active === 'aftersales'" v-loading="asLoading">
          <template #header>我的售后（{{ asTotal }}）</template>
          <el-empty v-if="!asLoading && !afterSales.length" description="还没有售后记录" />
          <div v-for="a in afterSales" :key="a.id" class="row-item">
            <div class="row-meta">
              <div class="row-title">{{ a.afterSaleNo }}</div>
              <div class="gray">订单 {{ a.orderNo }} · {{ a.reason || '—' }}</div>
              <div class="row-price">退款 ¥{{ num(a.amount) }}</div>
            </div>
            <div class="row-right">
              <el-tag :type="AfterSaleStatusTagType[a.status]" size="small" effect="dark">
                {{ AfterSaleStatusMap[a.status] }}
              </el-tag>
              <div class="gray">{{ fmt(a.createTime) }}</div>
              <el-button v-if="a.status === 0" link type="danger" @click="onCancelAs(a)">撤销</el-button>
              <el-button link type="primary" @click="router.push(`/orders/${a.orderId}`)">订单详情</el-button>
            </div>
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getFavorites, getFootprints, type ProfileProduct } from '@/api/profile'
import { getMyReviews, type ReviewItem } from '@/api/review'
import { cancelOrder, getOrders, receiveOrder, OrderStatusMap, OrderStatusTagType, type OrderVO } from '@/api/order'
import { cancelAfterSale, getMyAfterSales, AfterSaleStatusMap, AfterSaleStatusTagType, type AfterSaleVO } from '@/api/afterSale'

const router = useRouter()
const userStore = useUserStore()

const active = ref('orders')
const placeholder = '/products/placeholder.svg'

const nickChar = computed(() => (userStore.userInfo?.nickname || '宠')[0])
const maskedPhone = computed(() => {
  const p = userStore.userInfo?.phone
  return p ? p.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2') : ''
})

function onSelect(index: string) {
  active.value = index === 'orders' ? 'orders' : index
}

// ---------- 我的订单（内嵌） ----------
const orders = ref<OrderVO[]>([])
const orderTotal = ref(0)
const orderPage = ref(1)
const orderPageSize = 6
const orderLoading = ref(false)
const orderStatus = ref('all')

async function loadOrders() {
  orderLoading.value = true
  try {
    const status = orderStatus.value === 'all' ? undefined : Number(orderStatus.value)
    const res = await getOrders(status, orderPage.value, orderPageSize)
    orders.value = res.list
    orderTotal.value = res.total
  } finally {
    orderLoading.value = false
  }
}
function onOrderTab() {
  orderPage.value = 1
  loadOrders()
}
function onOrderPage(p: number) {
  orderPage.value = p
  loadOrders()
}
async function onCancel(o: OrderVO) {
  await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
  await cancelOrder(o.orderId)
  ElMessage.success('订单已取消')
  loadOrders()
}
async function onReceive(o: OrderVO) {
  await ElMessageBox.confirm('确认已收到货吗？', '提示')
  await receiveOrder(o.orderId)
  ElMessage.success('确认收货成功')
  loadOrders()
}

// 切换 Tab 时懒加载对应数据
watch(active, (v) => {
  if (v === 'orders') loadOrders()
  else if (v === 'reviews') loadReviews()
  else if (v === 'favorites') loadFavorites()
  else if (v === 'footprints') loadFootprints()
  else if (v === 'aftersales') loadAfterSales()
})

// ---------- 我的评价 ----------
const reviewLoading = ref(false)
const reviews = ref<ReviewItem[]>([])
const reviewTotal = ref(0)

async function loadReviews() {
  reviewLoading.value = true
  try {
    const res = await getMyReviews(1, 20)
    reviews.value = res.list
    reviewTotal.value = res.total
  } finally {
    reviewLoading.value = false
  }
}

// ---------- 收藏 ----------
const favLoading = ref(false)
const favorites = ref<ProfileProduct[]>([])

async function loadFavorites() {
  favLoading.value = true
  try {
    favorites.value = await getFavorites()
  } finally {
    favLoading.value = false
  }
}

async function onUnfavorite(p: ProfileProduct) {
  const mod = await import('@/api/profile')
  const res = await mod.toggleFavorite(p.productId)
  if (!res.favorited) {
    favorites.value = favorites.value.filter((f) => f.productId !== p.productId)
    ElMessage.success('已取消收藏')
  }
}

// ---------- 足迹 ----------
const fpLoading = ref(false)
const footprints = ref<ProfileProduct[]>([])

async function loadFootprints() {
  fpLoading.value = true
  try {
    footprints.value = await getFootprints()
  } finally {
    fpLoading.value = false
  }
}

// ---------- 我的售后 ----------
const asLoading = ref(false)
const afterSales = ref<AfterSaleVO[]>([])
const asTotal = ref(0)

async function loadAfterSales() {
  asLoading.value = true
  try {
    const res = await getMyAfterSales(1, 20)
    afterSales.value = res.list
    asTotal.value = res.total
  } finally {
    asLoading.value = false
  }
}

async function onCancelAs(a: AfterSaleVO) {
  await ElMessageBox.confirm('确定撤销该售后申请吗？', '提示', { type: 'warning' })
  await cancelAfterSale(a.id)
  ElMessage.success('已撤销')
  loadAfterSales()
}

function goProduct(id: number | undefined) {
  if (id) router.push(`/products/${id}`)
}

function num(n: number | undefined): string {
  return Number(n ?? 0).toFixed(2)
}
function fmt(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

onMounted(loadOrders)
</script>

<style scoped>
.profile-wrap {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.profile-menu {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}
.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  background: linear-gradient(135deg, #fff5f0, #fff);
}
.avatar {
  background: #ffb08a;
  color: #fff;
}
.nick {
  font-weight: 700;
  margin-bottom: 2px;
}
.sub {
  color: #909399;
  font-size: 12px;
}
.menu {
  border-right: none;
}
.profile-main {
  flex: 1;
  min-width: 0;
}
.order-entry {
  padding: 24px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #606266;
}
.inline-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.inline-order {
  border: 1px solid #f0f2f5;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
}
.io-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.io-body {
  display: flex;
  align-items: center;
  gap: 16px;
}
.io-items {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.io-tag {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
  color: #606266;
}
.io-amount {
  font-size: 13px;
  color: #606266;
  flex-shrink: 0;
}
.red {
  color: #f56c6c;
}
.io-actions {
  display: flex;
  flex-shrink: 0;
  gap: 6px;
  align-items: center;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 12px;
}
.row-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid #f2f3f5;
}
.row-img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  background: #f5f7fa;
  flex-shrink: 0;
}
.row-meta {
  flex: 1;
  cursor: pointer;
}
.row-title {
  font-weight: 600;
  margin-bottom: 4px;
}
.row-text {
  color: #606266;
  font-size: 13px;
  margin-top: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.row-price {
  color: #f56c6c;
  font-weight: 700;
  font-size: 16px;
}
.row-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}
.gray {
  color: #909399;
  font-size: 12px;
}
@media (max-width: 768px) {
  .profile-wrap {
    flex-direction: column;
  }
  .profile-menu {
    width: 100%;
  }
}
</style>