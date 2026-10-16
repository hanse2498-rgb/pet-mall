<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-form inline style="display: flex; flex-wrap: nowrap; align-items: center; gap: 6px; overflow-x: auto">
        <el-form-item label="分类">
          <el-select
            v-model="filters.categoryId"
            placeholder="全部分类"
            clearable
            style="width: 130px"
          >
            <el-option
              v-for="c in flatCategories"
              :key="c.id"
              :label="c.label"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select
            v-model="filters.brandId"
            placeholder="全部品牌"
            clearable
            style="width: 130px"
          >
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物">
          <el-select v-model="filters.petType" placeholder="全部宠物" clearable style="width: 100px">
            <el-option label="猫" :value="1" />
            <el-option label="狗" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="filters.sort" style="width: 120px">
            <el-option label="综合" value="default" />
            <el-option label="销量" value="sales" />
            <el-option label="价格 ↑" value="price_asc" />
            <el-option label="价格 ↓" value="price_desc" />
            <el-option label="最新" value="new" />
          </el-select>
        </el-form-item>
        <el-form-item style="margin-left: auto">
          <el-button type="primary" round @click="onSearch">筛选</el-button>
          <el-button round @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="result-hint" v-if="filters.keyword">
      搜索「{{ filters.keyword }}」，共 <b>{{ total }}</b> 件商品
      <el-link type="primary" :underline="false" @click="clearKeyword">清空</el-link>
    </div>

    <div class="prod-grid">
      <template v-if="loading">
        <div v-for="i in 10" :key="i" class="sk">
          <el-skeleton animated :throttle="500">
            <template #template>
              <el-skeleton-item variant="image" class="sk-img" />
              <el-skeleton-item variant="h3" style="width: 82%; margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 55%; margin-top: 8px" />
            </template>
          </el-skeleton>
        </div>
      </template>
      <template v-else>
        <ProductCard v-for="p in list" :key="p.id" :product="p" />
      </template>
    </div>
    <el-empty
      v-if="!loading && !list.length"
      description="没有找到相关商品"
      style="margin-top: 40px"
    />

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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import { getBrands, getCategories, getProductList, type Brand, type ProductCard as PC } from '@/api/product'

const route = useRoute()
const router = useRouter()

type SortKey = 'default' | 'sales' | 'price_asc' | 'price_desc' | 'new'

const filters = reactive<{
  keyword: string
  categoryId?: number
  brandId?: number
  petType?: number
  sort: SortKey
}>({
  keyword: (route.query.keyword as string) || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  brandId: undefined,
  petType: route.query.petType ? Number(route.query.petType) : undefined,
  sort: (route.query.sort as SortKey) || 'default',
})

const brands = ref<Brand[]>([])
const list = ref<PC[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 30 // 每行5个 × 每页6行
const loading = ref(false)

// 分类树拍平为可选项
const flatCategories = computed(() => {
  const result: { id: number; label: string }[] = []
  for (const c of categories.value) {
    result.push({ id: c.id, label: c.name })
    for (const sub of c.children) {
      result.push({ id: sub.id, label: `${c.name} / ${sub.name}` })
    }
  }
  return result
})
const categories = ref<{ id: number; name: string; children: { id: number; name: string }[] }[]>([])

async function load() {
  loading.value = true
  try {
    const res = await getProductList({
      ...filters,
      pageNum: pageNum.value,
      pageSize,
    })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pageNum.value = 1
  const kw = filters.keyword.trim() || undefined
  // 关键词有变则同步到 URL（由 watch 触发加载），否则直接查询
  if ((route.query.keyword || undefined) !== kw) {
    router.replace({ query: { ...route.query, keyword: kw } })
  } else {
    load()
  }
}

function clearKeyword() {
  filters.keyword = ''
  onSearch()
}

function onReset() {
  filters.keyword = ''
  filters.categoryId = undefined
  filters.brandId = undefined
  filters.petType = undefined
  filters.sort = 'default'
  pageNum.value = 1
  if (Object.keys(route.query).length) {
    router.replace({ query: {} })
  } else {
    load()
  }
}

function onPageChange(p: number) {
  pageNum.value = p
  load()
}

// URL 参数变化（含顶部搜索框跳转）→ 应用筛选并加载
watch(
  () => route.query,
  (q) => {
    filters.keyword = (q.keyword as string) || ''
    filters.categoryId = q.categoryId != null ? Number(q.categoryId) : undefined
    filters.petType = q.petType != null ? Number(q.petType) : undefined
    filters.sort = (q.sort as SortKey) || 'default'
    pageNum.value = 1
    load()
  },
)

onMounted(async () => {
  const [cats, brs] = await Promise.all([getCategories(), getBrands()])
  categories.value = cats
  brands.value = brs
  load()
})
</script>

<style scoped>
.filter-card {
  border: none;
  border-radius: 12px;
  box-shadow: var(--card-shadow);
}
.filter-card :deep(.el-card__body) {
  padding: 12px 16px;
}
.prod-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px 12px;
  margin-top: 16px;
}
.sk {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}
.sk-img {
  width: 100%;
  aspect-ratio: 1/1;
}
/* 小屏自适应：5 → 3 → 2 列 */
@media (max-width: 900px) {
  .prod-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 600px) {
  .prod-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 6px;
}
.result-hint {
  margin: 16px 4px 0;
  font-size: 13px;
  color: #6b7280;
}
.result-hint b {
  color: var(--el-color-primary);
}
.result-hint .el-link {
  margin-left: 8px;
  font-size: 13px;
}
.pager {
  display: flex;
  justify-content: center;
  margin: 24px 0;
}
</style>