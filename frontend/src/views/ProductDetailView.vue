<template>
  <div class="page-container" v-loading="loading">
    <template v-if="detail">
      <el-breadcrumb separator="/" style="margin-bottom: 16px">
        <el-breadcrumb-item :to="{ name: 'ProductList' }">全部商品</el-breadcrumb-item>
        <el-breadcrumb-item>{{ detail.categoryName || '商品' }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ detail.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card class="detail-card">
        <div class="detail-main">
          <!-- 图片 -->
          <div class="gallery">
            <el-carousel v-if="detail.images?.length" height="420px" indicator-position="outside">
              <el-carousel-item v-for="(img, i) in detail.images" :key="i">
                <img :src="img" :alt="detail.name" class="gallery-img" />
              </el-carousel-item>
            </el-carousel>
            <div v-else class="gallery-single">
              <img :src="detail.coverImg" :alt="detail.name" />
            </div>
          </div>

          <!-- 信息 -->
          <div class="info">
            <h2 class="title">
              {{ detail.name }}
              <el-tag v-if="detail.isNearExpiry === 1" type="danger" effect="dark" size="small">
                临期 · 7折
              </el-tag>
            </h2>
            <p v-if="detail.subtitle" class="subtitle">{{ detail.subtitle }}</p>

            <div class="price-box" v-if="selectedSku">
              <span class="label">价格</span>
              <span class="price">¥ {{ effectivePrice(selectedSku) }}</span>
              <span v-if="selectedSku.originalPrice" class="origin">
                ¥ {{ Number(selectedSku.originalPrice).toFixed(2) }}
              </span>
              <el-tag v-if="selectedSku.isDiscount === 1" type="danger" size="small">折扣价</el-tag>
            </div>

            <div class="row">
              <span class="label">规格</span>
              <el-radio-group v-model="skuId">
                <el-radio-button
                  v-for="s in detail.skus"
                  :key="s.id"
                  :value="s.id"
                  :disabled="s.status !== 1 || s.stock <= 0"
                >
                  {{ s.specName || '默认' }}
                </el-radio-button>
              </el-radio-group>
            </div>

            <div class="row" v-if="selectedSku">
              <span class="label">库存</span>
              <span>{{ selectedSku.stock > 0 ? selectedSku.stock + ' 件' : '暂时缺货' }}</span>
            </div>

            <div class="row" v-if="selectedSku && selectedSku.stock > 0">
              <span class="label">数量</span>
              <el-input-number v-model="quantity" :min="1" :max="selectedSku.stock" />
            </div>

            <div class="actions">
              <el-button
                type="warning"
                size="large"
                :disabled="!canBuy"
                @click="onAddToCart"
              >
                加入购物车
              </el-button>
              <el-button
                type="danger"
                size="large"
                :disabled="!canBuy"
                @click="onBuyNow"
              >
                立即购买
              </el-button>
              <el-button
                :type="favorited ? 'primary' : 'default'"
                size="large"
                :icon="Star"
                @click="onToggleFavorite"
              >
                {{ favorited ? '已收藏' : '收藏' }}
              </el-button>
            </div>

            <el-alert
              v-if="detail.isNearExpiry === 1 && detail.saleType === 1"
              type="warning"
              :closable="false"
              title="临期食品：剩余保质期约为总保质期的 1/3，请尽快食用；临期商品不支持订阅。"
              style="margin-top: 16px"
            />
            <el-alert
              v-if="detail.shelfStatus !== 1"
              type="error"
              :closable="false"
              title="该商品已下架"
              style="margin-top: 16px"
            />

            <div class="meta">
              <span v-if="detail.brandName">品牌：{{ detail.brandName }}</span>
              <span>销量：{{ detail.sales }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-card v-if="detail.detailHtml" class="detail-html">
        <template #header>商品详情</template>
        <div v-html="detail.detailHtml" class="rich-text"></div>
      </el-card>

      <!-- 商品评价 -->
      <el-card class="review-card" id="reviews">
        <template #header>
          <div class="review-head">
            <span>商品评价</span>
            <span v-if="reviewSummary?.total" class="review-sum">
              ⭐ {{ reviewSummary.avgRating }} 分 · 共 {{ reviewSummary.total }} 条 · 好评率 {{ reviewSummary.goodRate }}%
            </span>
          </div>
        </template>

        <template v-if="reviews.length">
          <div v-for="rv in reviews" :key="rv.id" class="review-item">
            <div class="review-top">
              <span class="review-nick">{{ rv.nickname }}</span>
              <el-rate :model-value="rv.rating" disabled size="small" />
              <el-tag v-if="rv.skuSpec" size="small" type="info">{{ rv.skuSpec }}</el-tag>
              <span class="review-time">{{ fmtTime(rv.createTime) }}</span>
            </div>
            <div class="review-content">{{ rv.content }}</div>
            <div v-if="rv.images?.length" class="review-imgs">
              <img v-for="(img, i) in (rv.images || [])" :key="i" :src="img" class="review-img" loading="lazy" />
            </div>
          </div>
          <div v-if="reviewTotal > reviews.length" class="review-more">
            <el-button link type="primary" @click="loadMoreReviews">
              加载更多（{{ reviews.length }}/{{ reviewTotal }}）
            </el-button>
          </div>
        </template>
        <el-empty v-else description="该商品还没有评价" :image-size="60" />
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import { getProductDetail, type ProductDetail, type Sku } from '@/api/product'
import { addToCart } from '@/api/cart'
import { getProductReviews, type ReviewItem, type ReviewSummary } from '@/api/review'
import { favoriteStatus, recordFootprint, toggleFavorite } from '@/api/profile'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const loading = ref(false)
const detail = ref<ProductDetail | null>(null)
const skuId = ref<number>()
const quantity = ref(1)

// 评价
const reviews = ref<ReviewItem[]>([])
const reviewTotal = ref(0)
const reviewSummary = ref<ReviewSummary | null>(null)
const reviewPage = ref(1)

async function loadReviews(id: number) {
  const res = await getProductReviews(id, 1, 5)
  reviews.value = res.list
  reviewTotal.value = res.total
  reviewSummary.value = res.summary
  reviewPage.value = 1
}

async function loadMoreReviews() {
  if (!detail.value) return
  reviewPage.value += 1
  const res = await getProductReviews(detail.value.id, reviewPage.value, 5)
  reviews.value = reviews.value.concat(res.list)
}

function fmtTime(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

const selectedSku = computed(() => detail.value?.skus.find((s) => s.id === skuId.value))
const canBuy = computed(
  () => detail.value?.shelfStatus === 1 && selectedSku.value?.stock != null && selectedSku.value.stock > 0,
)

function effectivePrice(sku: Sku): string {
  const price = sku.isDiscount === 1 && sku.discountPrice != null ? sku.discountPrice : sku.price
  return Number(price).toFixed(2)
}

async function load(id: number) {
  loading.value = true
  try {
    detail.value = await getProductDetail(id)
    const first = detail.value.skus.find((s) => s.status === 1 && s.stock > 0)
    skuId.value = first?.id ?? detail.value.skus[0]?.id
    quantity.value = 1
    loadReviews(id)
    // 已登录：读取收藏状态 + 记录足迹
    if (userStore.isLogin) {
      try {
        const st = await favoriteStatus(id)
        favorited.value = st.favorited
      } catch {
        // 忽略
      }
      recordFootprint(id).catch(() => {})
    }
  } finally {
    loading.value = false
  }
}

const favorited = ref(false)

async function onToggleFavorite() {
  if (!skuId.value) return
  if (!(await ensureLogin())) return
  const res = await toggleFavorite(detail.value!.id)
  favorited.value = res.favorited
  ElMessage.success(res.favorited ? '已收藏' : '已取消收藏')
}

async function ensureLogin(): Promise<boolean> {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function onAddToCart() {
  if (!(await ensureLogin()) || !skuId.value) return
  await addToCart(skuId.value, quantity.value)
  cartStore.fetchCount()
  ElMessage.success('已加入购物车')
}

async function onBuyNow() {
  if (!(await ensureLogin()) || !skuId.value) return
  await addToCart(skuId.value, quantity.value)
  cartStore.fetchCount()
  router.push({
    name: 'Checkout',
    query: { skuId: String(skuId.value), quantity: String(quantity.value) },
  })
}

watch(() => skuId.value, () => (quantity.value = 1))

onMounted(() => load(Number(route.params.id)))
</script>

<style scoped>
.detail-main {
  display: flex;
  gap: 32px;
}
.gallery {
  flex: 0 0 460px;
}
.gallery-single {
  height: 420px;
  background: #f5f7fa;
}
.gallery-single img,
.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.info {
  flex: 1;
}
.title {
  margin: 0 0 8px;
}
.subtitle {
  color: #909399;
  margin: 0 0 16px;
}
.price-box {
  background: #fef5f5;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.label {
  color: #909399;
  margin-right: 8px;
  flex-shrink: 0;
}
.price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: 700;
}
.origin {
  color: #909399;
  text-decoration: line-through;
}
.row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
.actions {
  margin-top: 8px;
}
.meta {
  margin-top: 20px;
  color: #909399;
  font-size: 13px;
  display: flex;
  gap: 24px;
}
.rich-text {
  color: #303133;
  line-height: 1.8;
}
.review-card {
  margin-top: 16px;
}
.review-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.review-sum {
  color: #f56c6c;
  font-size: 13px;
}
.review-item {
  padding: 12px 0;
  border-bottom: 1px solid #f2f3f5;
}
.review-top {
  display: flex;
  align-items: center;
  gap: 10px;
}
.review-nick {
  font-weight: 600;
  font-size: 13px;
}
.review-time {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}
.review-content {
  margin-top: 8px;
  color: #303133;
  line-height: 1.6;
}
.review-imgs {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.review-img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
}
.review-more {
  text-align: center;
  margin-top: 12px;
}
@media (max-width: 768px) {
  .detail-main {
    flex-direction: column;
  }
  .gallery {
    flex: none;
    width: 100%;
  }
}
</style>