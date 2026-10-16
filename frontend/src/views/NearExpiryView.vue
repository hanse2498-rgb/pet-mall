<template>
  <div class="page-container">
    <div class="banner">
      <h2>临期特惠专区</h2>
      <p>剩余保质期 ≤ 1/3，统一 7 折，赶紧薅！临期商品不可订阅。</p>
    </div>

    <el-row :gutter="12" v-loading="loading" style="margin-top: 16px">
      <el-col v-for="p in list" :key="p.id" :xs="12" :sm="6" :md="4" style="margin-bottom: 12px">
        <ProductCard :product="p" />
      </el-col>
    </el-row>
    <el-empty v-if="!loading && !list.length" description="暂无临期商品" style="margin-top: 40px" />

    <div class="pager" v-if="total > pageSize">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ProductCard from '@/components/ProductCard.vue'
import { getNearExpiryProducts, type ProductCard as PC } from '@/api/product'

const list = ref<PC[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getNearExpiryProducts({ pageNum: pageNum.value, pageSize })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  pageNum.value = p
  load()
}

onMounted(load)
</script>

<style scoped>
.banner {
  background: linear-gradient(135deg, #fef0f0, #fff);
  border-radius: 12px;
  padding: 28px 24px;
  text-align: center;
}
.banner h2 {
  margin: 0 0 8px;
  color: #f56c6c;
}
.banner p {
  margin: 0;
  color: #909399;
}
.pager {
  display: flex;
  justify-content: center;
  margin: 24px 0;
}
</style>