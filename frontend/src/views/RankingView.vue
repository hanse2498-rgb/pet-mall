<template>
  <div class="page-container rank-page">
    <div class="rank-head">
      <h2>好用排行</h2>
      <p>近 7 天各分类热销 Top3 · 每日更新</p>
    </div>

    <!-- 分类锚点 -->
    <div class="anchor-bar" v-if="list.length">
      <span
        v-for="c in list"
        :key="c.categoryId"
        class="anchor"
        @click="scrollTo(c.categoryId)"
      >
        {{ c.categoryName }}
      </span>
    </div>

    <div v-loading="loading" class="rank-body">
      <el-empty v-if="!loading && !list.length" description="暂无榜单数据" />

      <section
        v-for="c in list"
        :key="c.categoryId"
        :id="`cat-${c.categoryId}`"
        class="rank-section"
      >
        <div class="section-head">
          <span class="bar"></span>
          <span class="cat">{{ c.categoryName }}</span>
          <el-link
            type="primary"
            :underline="false"
            class="more"
            @click="router.push({ path: '/products', query: { categoryId: String(c.categoryId) } })"
          >
            查看全部 ›
          </el-link>
        </div>

        <div class="cards">
          <div
            v-for="it in c.items"
            :key="it.productId"
            class="rank-card"
            :class="`r${it.rank}`"
            @click="router.push(`/products/${it.productId}`)"
          >
            <span class="medal" :class="`m${it.rank}`">
              <el-icon v-if="it.rank === 1"><Trophy /></el-icon>
              <template v-else>{{ it.rank }}</template>
            </span>
            <div class="cover">
              <img :src="it.coverImg || '/products/placeholder.svg'" loading="lazy" />
            </div>
            <div class="info">
              <div class="name">{{ it.name }}</div>
              <div class="row">
                <span class="price">¥{{ num(it.price) }}</span>
                <span class="sales">近7天 {{ it.sales7d }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getRankTop, type CategoryRank } from '@/api/rank'

const router = useRouter()
const loading = ref(false)
const list = ref<CategoryRank[]>([])

function num(n: number | null | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

function scrollTo(categoryId: number) {
  const el = document.getElementById(`cat-${categoryId}`)
  el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function load() {
  loading.value = true
  try {
    list.value = await getRankTop()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.rank-page {
  max-width: 1100px;
}
.rank-head h2 {
  margin: 0 0 4px;
}
.rank-head p {
  margin: 0;
  color: #9aa1ab;
  font-size: 13px;
}
.anchor-bar {
  position: sticky;
  top: 60px;
  z-index: 10;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 0;
  margin-top: 8px;
  background: #faf7f5;
}
.anchor {
  font-size: 13px;
  color: #4a4f57;
  background: #fff;
  border: 1px solid #f0ece8;
  padding: 5px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.anchor:hover {
  background: #fff4ef;
  color: var(--el-color-primary);
  border-color: #ffd9c9;
}
.rank-body {
  min-height: 200px;
}
.rank-section {
  background: #fff;
  border-radius: 14px;
  padding: 12px 14px 14px;
  margin-bottom: 12px;
  scroll-margin-top: 120px;
  box-shadow: var(--card-shadow);
}
.section-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}
.section-head .bar {
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: #ff6a3d;
}
.section-head .cat {
  font-size: 16px;
  font-weight: 700;
  color: #20242a;
}
.section-head .more {
  margin-left: auto;
  font-size: 13px;
}
.cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.rank-card {
  position: relative;
  border: 1px solid #f0ece8;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}
.rank-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--card-shadow-hover);
}
.rank-card.r1 {
  border-color: #ffdca0;
  background: linear-gradient(180deg, #fffaf0, #fff);
}
.medal {
  position: absolute;
  top: -10px;
  left: 12px;
  min-width: 26px;
  height: 26px;
  padding: 0 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 13px;
  font-size: 14px;
  font-weight: 800;
  color: #fff;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.12);
}
.medal.m1 {
  background: #f5b301;
}
.medal.m2 {
  background: #b8bdc4;
}
.medal.m3 {
  background: #cd7f32;
}
.cover {
  border-radius: 10px;
  overflow: hidden;
  background: #f7f4f0;
}
.cover img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  display: block;
}
.info {
  padding-top: 8px;
}
.name {
  font-size: 14px;
  font-weight: 600;
  color: #20242a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 6px;
}
.price {
  color: #f0412f;
  font-size: 18px;
  font-weight: 800;
}
.sales {
  font-size: 12px;
  color: #9aa1ab;
}
@media (max-width: 760px) {
  .cards {
    grid-template-columns: 1fr;
  }
}
</style>