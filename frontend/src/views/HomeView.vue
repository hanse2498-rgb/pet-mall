<template>
  <div class="page-container">
    <!-- 横幅 -->
    <div class="hero">
      <h1>你上班，我照顾毛孩子</h1>
      <p>猫粮 · 零食 · 玩具 · 清洁护理 · 智能设备 · 临期特惠</p>
      <el-button type="primary" size="large" @click="router.push('/products')">
        去逛逛
      </el-button>
    </div>

    <!-- 秒杀入口 -->
    <div class="seckill-banner" @click="router.push('/seckill')">
      <div class="sb-left">
        <el-icon :size="26" class="sb-fire"><Lightning /></el-icon>
        <div>
          <div class="sb-title">秒杀专区</div>
          <div class="sb-sub">限时限量 · 手慢无</div>
        </div>
      </div>
      <el-button type="danger" round>立即抢购 ›</el-button>
    </div>

    <!-- 分类快捷入口 -->
    <el-card v-if="categories.length" class="section">
      <template #header>商品分类</template>
      <div class="cats">
        <div v-for="c in categories" :key="c.id" class="cat" @click="gotoCategory(c.id)">
          <div class="cat-name">{{ c.name }}</div>
          <div class="cat-sub">
            <span v-for="sub in c.children" :key="sub.id">{{ sub.name }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 热卖好物：大卡片榜单，与常规网格区分 -->
    <el-card class="section" shadow="never" v-if="hot.length">
      <template #header>
        <div class="section-head">
          <span>热卖好物</span>
          <el-link type="primary" @click="router.push('/products?sort=sales')">全部榜单 ›</el-link>
        </div>
      </template>
      <div class="hot-list">
        <div v-for="(p, i) in hot" :key="p.id" class="hot-item" @click="router.push(`/products/${p.id}`)">
          <span class="rank" :class="`r${i + 1}`">{{ i + 1 }}</span>
          <img :src="p.coverImg || '/products/placeholder.svg'" loading="lazy" />
          <div class="hot-meta">
            <div class="hot-name">{{ p.name }}</div>
            <div class="hot-sub" v-if="p.brandName">{{ p.brandName }} · 自营</div>
            <div class="hot-price">
              <span>¥{{ num(p.minPrice) }}</span>
              <em>已售 {{ p.sales }}</em>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 按宠物分类推荐：猫/狗 × 食品/用品 -->
    <el-card class="section" v-loading="loading">
      <template #header>
        <div class="section-head">
          <span>为你推荐</span>
          <el-link type="primary" @click="router.push('/products')">更多 ›</el-link>
        </div>
      </template>

      <el-row :gutter="24">
        <el-col v-for="sec in sections" :key="sec.petType" :xs="24" :md="12">
          <div class="pet-col">
            <div class="pet-title">
              <span class="pet-dot"></span>
              <span>{{ sec.title }}</span>
              <el-link
                type="primary"
                @click="router.push({ name: 'ProductList', query: { petType: sec.petType } })"
              >
                查看全部 ›
              </el-link>
            </div>

            <div v-for="sub in sec.subs" :key="sub.saleType" class="sub-block">
              <div class="sub-title">
                <span class="sub-dot"></span>
                <span>{{ sub.label }}</span>
              </div>
              <el-row :gutter="8">
                <el-col
                  v-for="p in sub.items"
                  :key="p.id"
                  :span="12"
                  style="margin-bottom: 8px"
                >
                  <ProductCard :product="p" />
                </el-col>
              </el-row>
              <el-empty
                v-if="!loading && !sub.items.length"
                description="暂无推荐"
                :image-size="50"
              />
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import { getCategories, getProductList, type CategoryNode, type ProductCard as PC } from '@/api/product'

interface SubBlock {
  saleType: number
  label: string
  items: PC[]
}

interface PetSection {
  petType: number
  title: string
  subs: SubBlock[]
}

const router = useRouter()
const categories = ref<CategoryNode[]>([])
const loading = ref(false)
const hot = ref<PC[]>([])

function num(n: number | null | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

const sections = ref<PetSection[]>([
  {
    petType: 1,
    title: '宠物猫',
    subs: [
      { saleType: 1, label: '食品推荐', items: [] },
      { saleType: 0, label: '用品推荐', items: [] },
    ],
  },
  {
    petType: 2,
    title: '宠物狗',
    subs: [
      { saleType: 1, label: '食品推荐', items: [] },
      { saleType: 0, label: '用品推荐', items: [] },
    ],
  },
])

async function loadRecommend() {
  loading.value = true
  try {
    // 并发加载 猫/狗 × 食品/用品 四个推荐块，每块 2 张卡片（petType=0 通用品也会被带出）
    const tasks = sections.value.flatMap((sec) =>
      sec.subs.map(async (sub) => {
        const res = await getProductList({
          petType: sec.petType,
          saleType: sub.saleType,
          sort: 'sales',
          pageNum: 1,
          pageSize: 2,
        })
        sub.items = res.list
      }),
    )
    await Promise.all(tasks)
  } finally {
    loading.value = false
  }
}

async function load() {
  categories.value = await getCategories()
  loadRecommend()
  // 热卖榜单：按销量取前 3
  getProductList({ sort: 'sales', pageNum: 1, pageSize: 3 }).then((res) => {
    hot.value = res.list
  })
}

function gotoCategory(id: number) {
  router.push({ name: 'ProductList', query: { categoryId: id } })
}

onMounted(load)
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #ffe8dd 0%, #fff7f3 50%, #fff1e6 100%);
  border-radius: 16px;
  padding: 52px 32px;
  margin-bottom: 16px;
  text-align: center;
}
.hero h1 {
  margin: 0 0 8px;
}
.hero p {
  color: #909399;
  margin: 0 0 20px;
}
.section {
  margin-bottom: 16px;
}
.seckill-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #ff5340, #ff7a45);
  color: #fff;
  border-radius: 12px;
  padding: 18px 24px;
  margin-bottom: 16px;
  cursor: pointer;
}
.sb-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sb-fire {
  font-size: 26px;
}
.sb-title {
  font-size: 20px;
  font-weight: 800;
}
.sb-sub {
  font-size: 12px;
  opacity: 0.9;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.hot-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.hot-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border: 1px solid rgba(31, 35, 41, 0.04);
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}
.hot-item:hover {
  transform: translateY(-3px);
  box-shadow: var(--card-shadow-hover);
}
.hot-item img {
  width: 72px;
  height: 72px;
  border-radius: 10px;
  object-fit: cover;
}
.rank {
  font-size: 26px;
  font-weight: 800;
  font-style: italic;
  width: 40px;
  text-align: center;
  color: #c0c4cc;
}
.rank.r1 {
  color: #f0412f;
}
.rank.r2 {
  color: #ff8a50;
}
.rank.r3 {
  color: #e8a33d;
}
.hot-meta {
  min-width: 0;
}
.hot-name {
  font-size: 14px;
  font-weight: 600;
  color: #20242a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.hot-sub {
  font-size: 12px;
  color: #9aa1ab;
  margin-top: 2px;
}
.hot-price {
  margin-top: 6px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.hot-price span {
  color: #f0412f;
  font-size: 18px;
  font-weight: 800;
}
.hot-price em {
  font-style: normal;
  font-size: 12px;
  color: #9aa1ab;
}
@media (max-width: 760px) {
  .hot-list {
    grid-template-columns: 1fr;
  }
}
.cats {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}
.cat {
  background: #fff;
  border: 1px solid rgba(31, 35, 41, 0.04);
  border-radius: 10px;
  padding: 12px;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease, background 0.22s ease;
}
.cat:hover {
  transform: translateY(-3px);
  background: #fff4ef;
  box-shadow: var(--card-shadow-hover);
}
.cat-name {
  font-weight: 600;
  margin-bottom: 4px;
}
.cat-sub {
  font-size: 12px;
  color: #909399;
}
.cat-sub span {
  margin-right: 8px;
}
.pet-col {
  padding: 4px 12px 16px 4px;
}
.pet-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f5f7fa;
}
.pet-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ff7a45;
  box-shadow: 0 0 0 4px #ffe9df;
}
.pet-title .el-link {
  margin-left: auto;
  font-size: 13px;
}
.sub-block {
  margin-bottom: 16px;
}
.sub-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}
.sub-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ffb25e;
}
</style>