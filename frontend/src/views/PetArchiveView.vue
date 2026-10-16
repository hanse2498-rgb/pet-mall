<template>
  <div class="page-container pet-archive">
    <div class="head">
      <h2>宠物档案</h2>
      <p>犬猫品种图鉴 · 了解它们的性格与饲养要点</p>
    </div>

    <el-tabs v-model="activeType" class="type-tabs">
      <el-tab-pane label="猫" name="1" />
      <el-tab-pane label="狗" name="2" />
    </el-tabs>

    <div class="grid" v-loading="loading">
      <el-empty v-if="!loading && !list.length" description="暂无品种数据" />
      <div
        v-for="b in list"
        :key="b.id"
        class="card"
        @click="router.push(`/pets/${b.id}`)"
      >
        <div class="cover">
          <img :src="b.coverImg || '/products/placeholder.svg'" loading="lazy" :alt="b.name" />
        </div>
        <div class="info">
          <div class="name">{{ b.name }}</div>
          <div class="en" v-if="b.enName">{{ b.enName }}</div>
          <div class="intro">{{ b.intro }}</div>
          <div class="tags">
            <el-tag size="small" effect="plain">{{ b.size }}</el-tag>
            <el-tag size="small" effect="plain">{{ b.hair }}</el-tag>
            <el-tag size="small" effect="plain" type="warning">{{ b.characterTrait }}</el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getBreeds, type PetBreed } from '@/api/petBreed'

const router = useRouter()
const activeType = ref('1')
const loading = ref(false)
const list = ref<PetBreed[]>([])

async function load() {
  loading.value = true
  try {
    list.value = await getBreeds(Number(activeType.value))
  } finally {
    loading.value = false
  }
}

watch(activeType, load)
onMounted(load)
</script>

<style scoped>
.pet-archive {
  max-width: 1100px;
}
.head h2 {
  margin: 0 0 4px;
}
.head p {
  margin: 0;
  color: #9aa1ab;
  font-size: 13px;
}
.type-tabs {
  margin-top: 12px;
}
.type-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 600;
}
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 8px;
  min-height: 200px;
}
.card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(31, 35, 41, 0.04);
  box-shadow: var(--card-shadow);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}
.card:hover {
  transform: translateY(-4px);
  box-shadow: var(--card-shadow-hover);
}
.cover {
  overflow: hidden;
  background: #f7f4f0;
}
.cover img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  display: block;
  transition: transform 0.45s ease;
}
.card:hover .cover img {
  transform: scale(1.05);
}
.info {
  padding: 12px 14px 14px;
}
.name {
  font-size: 16px;
  font-weight: 700;
  color: #20242a;
}
.en {
  font-size: 12px;
  color: #9aa1ab;
  margin-top: 2px;
}
.intro {
  font-size: 13px;
  color: #6b7280;
  margin-top: 8px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}
@media (max-width: 900px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
