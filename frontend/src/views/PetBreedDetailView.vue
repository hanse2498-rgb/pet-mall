<template>
  <div class="page-container breed-detail" v-loading="loading">
    <el-button link @click="router.back()" class="back">‹ 返回</el-button>

    <template v-if="breed">
      <!-- 头部：图 + 介绍 + 品种信息 -->
      <el-card class="block">
        <div class="top">
          <div class="cover">
            <img :src="breed.coverImg || '/products/placeholder.svg'" :alt="breed.name" />
          </div>
          <div class="meta">
            <div class="name">{{ breed.name }}</div>
            <div class="en" v-if="breed.enName">{{ breed.enName }}</div>
            <div class="intro">{{ breed.intro }}</div>
            <div class="traits">
              <div class="trait"><span class="k">体型</span><span class="v">{{ breed.size }}</span></div>
              <div class="trait"><span class="k">毛长</span><span class="v">{{ breed.hair }}</span></div>
              <div class="trait"><span class="k">性格</span><span class="v">{{ breed.characterTrait }}</span></div>
              <div class="trait"><span class="k">原产地</span><span class="v">{{ breed.origin }}</span></div>
              <div class="trait"><span class="k">寿命</span><span class="v">{{ breed.lifeSpan }}</span></div>
              <div class="trait"><span class="k">饲养难度</span><span class="v stars">{{ stars(breed.difficulty) }}</span></div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 详细介绍 -->
      <el-card class="block">
        <template #header>详细介绍</template>
        <div class="section" v-if="breed.description">
          <div class="sec-title">总述</div>
          <p class="sec-text">{{ breed.description }}</p>
        </div>
        <div class="section" v-if="breed.habits">
          <div class="sec-title">生活习性</div>
          <p class="sec-text">{{ breed.habits }}</p>
        </div>
        <div class="section" v-if="breed.appearance">
          <div class="sec-title">外观与颜色</div>
          <p class="sec-text">{{ breed.appearance }}</p>
        </div>
        <div class="section" v-if="breed.careTips">
          <div class="sec-title">饲养建议</div>
          <p class="sec-text">{{ breed.careTips }}</p>
        </div>
      </el-card>

      <!-- 推荐搭配 -->
      <el-card class="block" v-if="recommend.length">
        <template #header>推荐搭配</template>
        <div class="rec-list">
          <div v-for="p in recommend" :key="p.id" class="rec-card" @click="router.push(`/products/${p.id}`)">
            <div class="rec-cover">
              <img :src="p.coverImg || '/products/placeholder.svg'" loading="lazy" />
            </div>
            <div class="rec-name">{{ p.name }}</div>
            <div class="rec-sub">{{ p.brandName }} · {{ p.categoryName }}</div>
            <div class="rec-price">¥{{ num(p.minPrice) }}</div>
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBreed, type PetBreedDetail } from '@/api/petBreed'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const breed = ref<PetBreedDetail | null>(null)

const recommend = computed(() => breed.value?.recommendProducts || [])

function stars(n: number | undefined): string {
  const d = Math.max(0, Math.min(3, n ?? 0))
  return '★'.repeat(d) + '☆'.repeat(3 - d)
}

function num(n: number | null | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

async function load() {
  loading.value = true
  try {
    breed.value = await getBreed(Number(route.params.id))
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.breed-detail {
  max-width: 1000px;
}
.back {
  margin-bottom: 10px;
}
.block {
  margin-bottom: 16px;
}
.top {
  display: flex;
  gap: 24px;
}
.cover {
  flex: 0 0 320px;
}
.cover img {
  width: 320px;
  height: 320px;
  object-fit: cover;
  border-radius: 12px;
  background: #f7f4f0;
}
.meta {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 24px;
  font-weight: 800;
  color: #20242a;
}
.en {
  font-size: 14px;
  color: #9aa1ab;
  margin-top: 4px;
}
.intro {
  margin-top: 12px;
  color: #4a4f57;
  line-height: 1.7;
}
.traits {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px 20px;
  margin-top: 18px;
}
.trait {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}
.trait .k {
  color: #9aa1ab;
  width: 64px;
  flex-shrink: 0;
}
.trait .v {
  color: #20242a;
  font-weight: 600;
}
.stars {
  color: #f5b301;
  letter-spacing: 2px;
}
/* 详细介绍小节 */
.section {
  margin-bottom: 16px;
}
.section:last-child {
  margin-bottom: 0;
}
.sec-title {
  font-size: 15px;
  font-weight: 700;
  color: #20242a;
  margin-bottom: 6px;
  padding-left: 10px;
  position: relative;
}
.sec-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 3px;
  bottom: 3px;
  width: 4px;
  border-radius: 2px;
  background: #ff6a3d;
}
.sec-text {
  margin: 0;
  line-height: 1.9;
  color: #3a3f45;
  padding-left: 10px;
}
/* 推荐 */
.rec-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.rec-card {
  border: 1px solid rgba(31, 35, 41, 0.05);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  box-shadow: var(--card-shadow);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  padding-bottom: 12px;
}
.rec-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--card-shadow-hover);
}
.rec-cover img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  display: block;
}
.rec-name {
  padding: 8px 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #20242a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rec-sub {
  padding: 2px 12px 0;
  font-size: 12px;
  color: #9aa1ab;
}
.rec-price {
  padding: 6px 12px 0;
  color: #f0412f;
  font-size: 18px;
  font-weight: 800;
}
@media (max-width: 760px) {
  .top {
    flex-direction: column;
  }
  .cover,
  .cover img {
    flex: none;
    width: 100%;
    height: auto;
    aspect-ratio: 1;
  }
  .rec-list {
    grid-template-columns: 1fr;
  }
}
</style>
