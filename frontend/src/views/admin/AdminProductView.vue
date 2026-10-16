<template>
  <div class="admin-content">
    <!-- 左侧分类导航 -->
    <aside class="cat-nav">
      <el-menu :default-active="activeCategory" @select="selectCategory">
        <el-menu-item index="all">
          <el-icon><Menu /></el-icon><span>全部商品</span>
        </el-menu-item>
        <el-menu-item v-for="c in topCategories" :key="c.id" :index="String(c.id)">
          <el-icon><Goods /></el-icon><span>{{ c.name }}</span>
        </el-menu-item>
      </el-menu>
      <div class="nav-foot">{{ activeCategory === 'all' ? '共 ' + total + ' 件' : topCategories.find(c => String(c.id) === activeCategory)?.name + ' 分类' }}</div>
    </aside>

    <!-- 右侧商品列表 -->
    <section class="cat-main">
      <el-card>
        <div class="toolbar">
          <el-input v-model="query.keyword" placeholder="搜索商品名" clearable style="width: 200px"
            @keyup.enter="search" />
          <el-select v-model="query.shelfStatus" placeholder="上架状态" clearable style="width: 130px" @change="search">
            <el-option label="已上架" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
          <el-select v-model="query.brandId" placeholder="品牌" clearable style="width: 150px" @change="search">
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button type="success" @click="openProductForm()">＋ 新增商品</el-button>
        </div>

        <el-table :data="list" v-loading="loading" border stripe>
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="name" label="商品名" min-width="200" show-overflow-tooltip />
          <el-table-column label="分类" width="100">
            <template #default="{ row }">{{ categoryMap[row.categoryId] || '-' }}</template>
          </el-table-column>
          <el-table-column label="品牌" width="110">
            <template #default="{ row }">{{ brandMap[row.brandId] || '-' }}</template>
          </el-table-column>
          <el-table-column label="类型" width="80">
            <template #default="{ row }">
              <el-tag :type="row.saleType === 1 ? 'warning' : 'info'" size="small">
                {{ row.saleType === 1 ? '食品' : '用品' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="宠物" width="80">
            <template #default="{ row }">{{ petLabel(row.petType) }}</template>
          </el-table-column>
          <el-table-column label="临期" width="70">
            <template #default="{ row }">
              <el-tag v-if="row.isNearExpiry === 1" type="danger" size="small">临期</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="sales" label="销量" width="80" />
          <el-table-column label="上架" width="90">
            <template #default="{ row }">
              <el-switch :model-value="row.shelfStatus === 1" @change="(v: string | number | boolean) => toggleShelf(row, Boolean(v))" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openProductForm(row)">编辑</el-button>
              <el-button link type="primary" @click="openSkuDialog(row)">SKU</el-button>
              <el-button link type="warning" @click="openBatchDialog(row)">批次</el-button>
              <el-button link type="danger" @click="onDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="query.pageSize"
            :current-page="query.pageNum" @current-change="onPage" />
        </div>
      </el-card>
    </section>
  </div>

    <!-- 商品新增/编辑 -->
    <el-dialog v-model="productDialog" :title="productForm.id ? '编辑商品' : '新增商品'" width="560px">
      <el-form :model="productForm" label-width="90px">
        <el-form-item label="商品名" required>
          <el-input v-model="productForm.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="productForm.subtitle" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="productForm.brandId" clearable placeholder="选择品牌" style="width: 100%">
            <el-option v-for="b in brands" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="productForm.categoryId" clearable placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in flatCategories" :key="c.id" :label="c.label" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="productForm.saleType">
            <el-radio :value="1">食品</el-radio>
            <el-radio :value="0">用品</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="适用宠物">
          <el-radio-group v-model="productForm.petType">
            <el-radio :value="0">通用</el-radio>
            <el-radio :value="1">猫</el-radio>
            <el-radio :value="2">狗</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="封面图">
          <div class="img-edit">
            <img v-if="productForm.coverImg" :src="productForm.coverImg" class="img-preview" alt="封面" />
            <ImageUploader v-model="coverList" :limit="1" :upload-fn="doUploadImage" />
          </div>
          <el-input v-model="productForm.coverImg" placeholder="或直接填图片地址（/products/xxx.png）"
            style="margin-top: 8px" />
        </el-form-item>
        <el-form-item label="轮播图">
          <ImageUploader v-model="productForm.images" :limit="5" :upload-fn="doUploadImage" />
          <div class="img-hint">上传的图片会自动保存到商品图片文件夹（本地上传）</div>
        </el-form-item>
        <el-form-item label="详情HTML">
          <el-input v-model="productForm.detailHtml" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="上架">
          <el-radio-group v-model="productForm.shelfStatus">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingProduct" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>

    <!-- SKU 管理 -->
    <el-dialog v-model="skuDialog" :title="`SKU 管理 - ${currentProduct?.name || ''}`" width="720px">
      <el-table :data="skus" size="small" border>
        <el-table-column prop="specName" label="规格" />
        <el-table-column label="售价">
          <template #default="{ row }">
            <el-input-number v-model="row.price" :min="0" :precision="2" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="划线价">
          <template #default="{ row }">
            <el-input-number v-model="row.originalPrice" :min="0" :precision="2" size="small" :controls="false" />
          </template>
        </el-table-column>
        <el-table-column label="库存">
          <template #default="{ row }">
            <el-input-number v-model="row.stock" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="折扣" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.isDiscount === 1" type="danger" size="small">{{ row.discountPrice }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="saveSku(row)">保存</el-button>
            <el-button link type="primary" @click="openBatchDialog(row)">批次</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-divider>新增 SKU</el-divider>
      <el-form inline>
        <el-form-item label="规格"><el-input v-model="newSku.specName" style="width: 120px" /></el-form-item>
        <el-form-item label="售价"><el-input-number v-model="newSku.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="划线价"><el-input-number v-model="newSku.originalPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="newSku.stock" :min="0" /></el-form-item>
        <el-button type="primary" @click="addSku">新增</el-button>
      </el-form>
    </el-dialog>

    <!-- 批次管理 -->
    <el-dialog v-model="batchDialog" title="批次管理（食品）" width="680px">
      <el-table :data="batches" size="small" border>
        <el-table-column prop="batchNo" label="批次号" />
        <el-table-column prop="produceDate" label="生产日期" />
        <el-table-column prop="expiryDays" label="保质期(天)" />
        <el-table-column label="剩余占比">
          <template #default="{ row }">
            {{ ((row.remainRatio ?? 0) * 100).toFixed(1) }}%
          </template>
        </el-table-column>
        <el-table-column label="临期">
          <template #default="{ row }">
            <el-tag :type="row.isNearExpiry === 1 ? 'danger' : 'success'" size="small">
              {{ row.isNearExpiry === 1 ? '是 · 7折' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-divider>新增批次</el-divider>
      <el-form inline>
        <el-form-item label="批次号"><el-input v-model="newBatch.batchNo" style="width: 130px" /></el-form-item>
        <el-form-item label="生产日期">
          <el-date-picker v-model="newBatch.produceDate" type="date" value-format="YYYY-MM-DD" style="width: 140px" />
        </el-form-item>
        <el-form-item label="保质期(天)"><el-input-number v-model="newBatch.expiryDays" :min="1" /></el-form-item>
        <el-button type="primary" @click="addBatch">新增</el-button>
      </el-form>
      <el-alert type="info" :closable="false" title="新增批次后自动计算剩余保质期占比，≤1/3 自动标记临期并按 7 折设置折扣价。"
        style="margin-top: 12px" />
    </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminBatchAdd,
  adminProductCreate,
  adminProductDelete,
  adminProductPage,
  adminProductShelf,
  adminProductSkus,
  adminProductUpdate,
  adminSkuAdd,
  adminSkuBatches,
  adminSkuUpdate,
  adminBrandList,
  adminCategoryList,
  uploadProductImage,
  type AdminProduct,
  type ProductForm,
  type SkuRow,
  type SkuBatch,
  type AdminCategory,
} from '@/api/admin'
import type { Brand } from '@/api/product'
import ImageUploader from '@/components/ImageUploader.vue'

const loading = ref(false)
const list = ref<AdminProduct[]>([])
const total = ref(0)
const query = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  shelfStatus: undefined as number | undefined,
  brandId: undefined as number | undefined,
  pageNum: 1,
  pageSize: 14,
})

const brands = ref<Brand[]>([])
const categories = ref<AdminCategory[]>([])
const brandMap = computed(() => Object.fromEntries(brands.value.map((b) => [b.id, b.name])))

// 一级分类（左侧导航）。注意：id/parentId 为雪花 id，后端以字符串返回，需用 Number 比较
const topCategories = computed(() =>
  categories.value.filter((c) => Number(c.parentId ?? 0) === 0),
)
// 拍平用于下拉：一级 / 一级/二级
const flatCategories = computed(() => {
  const out: { id: number; label: string }[] = []
  const top = categories.value.filter((c) => Number(c.parentId ?? 0) === 0)
  for (const c of top) {
    out.push({ id: c.id, label: c.name })
    for (const sub of categories.value.filter((s) => Number(s.parentId) === Number(c.id))) {
      out.push({ id: sub.id, label: `${c.name} / ${sub.name}` })
    }
  }
  return out
})
const categoryMap = computed(() => Object.fromEntries(flatCategories.value.map((c) => [c.id, c.label])))
const activeCategory = ref('all')

function petLabel(petType: number | undefined): string {
  return petType === 1 ? '猫' : petType === 2 ? '狗' : '通用'
}

async function load() {
  loading.value = true
  try {
    const res = await adminProductPage(query)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}
function onPage(p: number) {
  query.pageNum = p
  load()
}
function selectCategory(key: string) {
  activeCategory.value = key
  query.categoryId = key === 'all' ? undefined : Number(key)
  search()
}

// ---------- 商品表单 ----------
const productDialog = ref(false)
const savingProduct = ref(false)
const coverList = ref<string[]>([])
const productForm = reactive<ProductForm>({} as ProductForm)

// 上传商品图片：自动保存到「商品图片文件夹」，返回 /products/xxx 路径
function doUploadImage(file: File) {
  return uploadProductImage(file, productForm.id)
}

// 封面图上传结果同步到 coverImg
watch(coverList, (v) => {
  productForm.coverImg = v[0] ?? ''
})

// 后端的 images 是 JSON 字符串（也可能已是数组），统一转成数组
function parseImages(raw: string | string[] | undefined): string[] {
  if (Array.isArray(raw)) return raw
  if (typeof raw === 'string' && raw.trim()) {
    try {
      const v = JSON.parse(raw)
      return Array.isArray(v) ? v : []
    } catch {
      return []
    }
  }
  return []
}

function openProductForm(row?: AdminProduct) {
  if (row) {
    Object.assign(productForm, {
      id: row.id, name: row.name, subtitle: row.subtitle, brandId: row.brandId,
      categoryId: row.categoryId, saleType: row.saleType, petType: row.petType,
      shelfStatus: row.shelfStatus, coverImg: row.coverImg, images: parseImages(row.images), detailHtml: '',
    })
    coverList.value = row.coverImg ? [row.coverImg] : []
  } else {
    Object.keys(productForm).forEach((k) => delete (productForm as Record<string, unknown>)[k])
    Object.assign(productForm, { saleType: 0, petType: 0, shelfStatus: 1, images: [] })
    coverList.value = []
  }
  productDialog.value = true
}

async function saveProduct() {
  if (!productForm.name) return ElMessage.warning('请输入商品名')
  savingProduct.value = true
  try {
    if (productForm.id) {
      await adminProductUpdate({ ...productForm })
      ElMessage.success('已更新')
    } else {
      await adminProductCreate({ ...productForm })
      ElMessage.success('已创建，请继续维护 SKU')
    }
    productDialog.value = false
    load()
  } finally {
    savingProduct.value = false
  }
}

async function toggleShelf(row: AdminProduct, on: boolean) {
  await adminProductShelf(row.id, on ? 1 : 0)
  row.shelfStatus = on ? 1 : 0
  ElMessage.success(on ? '已上架' : '已下架')
}

async function onDelete(row: AdminProduct) {
  await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'warning' })
  await adminProductDelete(row.id)
  ElMessage.success('已删除')
  load()
}

// ---------- SKU ----------
const skuDialog = ref(false)
const currentProduct = ref<AdminProduct>()
const skus = ref<SkuRow[]>([])
const newSku = reactive<Partial<SkuRow>>({ specName: '', price: 0, originalPrice: 0, stock: 0 })

async function openSkuDialog(row: AdminProduct) {
  currentProduct.value = row
  skus.value = await adminProductSkus(row.id)
  Object.assign(newSku, { specName: '', price: 0, originalPrice: 0, stock: 0 })
  skuDialog.value = true
}

async function saveSku(row: SkuRow) {
  await adminSkuUpdate(row)
  ElMessage.success('SKU 已保存')
}

async function addSku() {
  if (!currentProduct.value) return
  if (!newSku.specName || (newSku.price ?? 0) <= 0) return ElMessage.warning('请填写规格与售价')
  await adminSkuAdd({ productId: currentProduct.value.id, ...newSku })
  ElMessage.success('SKU 已新增')
  skus.value = await adminProductSkus(currentProduct.value.id)
}

// ---------- 批次 ----------
const batchDialog = ref(false)
const batches = ref<SkuBatch[]>([])
const currentSku = ref<SkuRow>()
const newBatch = reactive<{ batchNo?: string; produceDate: string; expiryDays: number }>({
  batchNo: '',
  produceDate: '',
  expiryDays: 365,
})

async function openBatchDialog(row: SkuRow | AdminProduct) {
  if ('specName' in row) {
    currentSku.value = row as SkuRow
  } else {
    // 从商品行进入：取第一个SKU
    const skusList = await adminProductSkus((row as AdminProduct).id)
    currentSku.value = skusList[0]
  }
  if (!currentSku.value) {
    ElMessage.warning('该商品还没有 SKU，请先新增 SKU')
    return
  }
  batches.value = await adminSkuBatches(currentSku.value.id)
  Object.assign(newBatch, { batchNo: '', produceDate: '', expiryDays: 365 })
  batchDialog.value = true
}

async function addBatch() {
  if (!currentSku.value) return
  if (!newBatch.produceDate || !newBatch.expiryDays) return ElMessage.warning('请填写生产日期与保质期')
  await adminBatchAdd({ skuId: currentSku.value.id, ...newBatch })
  ElMessage.success('批次已新增')
  batches.value = await adminSkuBatches(currentSku.value.id)
}

onMounted(async () => {
  try {
    ;[brands.value, categories.value] = [await adminBrandList(), await adminCategoryList()]
  } catch {
    // 忽略
  }
  load()
})
</script>

<style scoped>
.admin-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.cat-nav {
  width: 180px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  overflow: hidden;
}
.cat-nav :deep(.el-menu) {
  border-right: none;
}
.nav-foot {
  padding: 10px 16px;
  color: #909399;
  font-size: 12px;
  border-top: 1px solid #f2f3f5;
}
.cat-main {
  flex: 1;
  min-width: 0;
}
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
/* 分页框悬浮吸底：始终吸附在窗口最底部 */
.pager {
  position: fixed;
  left: 200px; /* 侧栏宽度 */
  right: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  justify-content: center;
  padding: 10px 24px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}
/* 底部留出分页条高度，避免遮挡最后一行 */
.admin-content {
  padding-bottom: 60px;
}
/* 商品图片上传 */
.img-edit {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.img-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
}
.img-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
@media (max-width: 768px) {
  .admin-content {
    flex-direction: column;
  }
  .cat-nav {
    width: 100%;
  }
}
</style>