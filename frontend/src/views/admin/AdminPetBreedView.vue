<template>
  <div class="admin-pet">
    <el-card>
      <template #header>
        <div class="head">
          <span>宠物档案管理</span>
          <div class="ops">
            <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 120px" @change="search">
              <el-option label="猫" :value="1" />
              <el-option label="狗" :value="2" />
            </el-select>
            <el-input v-model="query.keyword" placeholder="品种名/英文名" clearable style="width: 180px"
              @keyup.enter="search" />
            <el-button type="primary" @click="search">查询</el-button>
            <el-button type="primary" plain @click="openCreate">新增品种</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <img :src="row.coverImg || '/products/placeholder.svg'" class="thumb" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="品种名" min-width="140" />
        <el-table-column prop="enName" label="英文名" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ row.type === 1 ? '猫' : '狗' }}</template>
        </el-table-column>
        <el-table-column label="体型/毛长" width="150">
          <template #default="{ row }">{{ row.size }} / {{ row.hair }}</template>
        </el-table-column>
        <el-table-column label="难度" width="90">
          <template #default="{ row }">
            <span class="stars">{{ '★'.repeat(row.difficulty || 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(v: any) => toggle(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRecommend(row)">推荐</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="pageSize"
          :current-page="pageNum" @current-change="onPage" />
      </div>
    </el-card>

    <el-dialog v-model="dialog" :title="form.id ? '编辑品种' : '新增品种'" width="680px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">猫</el-radio>
            <el-radio :value="2">狗</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="品种名"><el-input v-model="form.name" maxlength="32" /></el-form-item>
        <el-form-item label="英文名"><el-input v-model="form.enName" maxlength="48" /></el-form-item>
        <el-form-item label="封面图">
          <div class="cover-edit">
            <img v-if="form.coverImg" :src="form.coverImg" class="cover-preview" />
            <ImageUploader v-model="coverList" :limit="1" :upload-fn="doUploadImage" />
          </div>
        </el-form-item>
        <el-form-item label="一句话简介">
          <el-input v-model="form.intro" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="详细介绍">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="生活习性">
          <el-input v-model="form.habits" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="外观与颜色">
          <el-input v-model="form.appearance" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="饲养建议">
          <el-input v-model="form.careTips" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="体型"><el-input v-model="form.size" maxlength="16" /></el-form-item>
        <el-form-item label="毛长"><el-input v-model="form.hair" maxlength="16" /></el-form-item>
        <el-form-item label="性格"><el-input v-model="form.characterTrait" maxlength="32" /></el-form-item>
        <el-form-item label="原产地"><el-input v-model="form.origin" maxlength="32" /></el-form-item>
        <el-form-item label="寿命"><el-input v-model="form.lifeSpan" maxlength="16" /></el-form-item>
        <el-form-item label="饲养难度">
          <el-input-number v-model="form.difficulty" :min="1" :max="3" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 推荐映射 -->
    <el-dialog v-model="recDialog" :title="`推荐搭配 - ${recBreed?.name || ''}`" width="560px">
      <div class="rec-hint">为该品种设置推荐：2 个主粮 + 1 个其他（留空则不推荐）</div>
      <div class="rec-row" v-for="(slot, i) in recSlots" :key="i">
        <span class="rec-label">{{ slot.label }}</span>
        <el-select v-model="slot.productId" filterable clearable placeholder="选择商品" style="flex: 1">
          <el-option v-for="p in productOptions" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="recDialog = false">取消</el-button>
        <el-button type="primary" :loading="recSaving" @click="saveRecommend">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUploader from '@/components/ImageUploader.vue'
import {
  adminPetBreeds,
  adminPetBreedCreate,
  adminPetBreedUpdate,
  adminPetBreedStatus,
  adminPetBreedDelete,
  adminPetBreedRecommends,
  adminPetBreedSaveRecommends,
  adminProductPage,
  uploadBreedImage,
} from '@/api/admin'
import type { PetBreed } from '@/api/petBreed'

const loading = ref(false)
const list = ref<PetBreed[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const query = reactive<{ type?: number; keyword: string }>({ type: undefined, keyword: '' })

const dialog = ref(false)
const saving = ref(false)
const coverList = ref<string[]>([])
const form = reactive<Partial<PetBreed>>({
  id: undefined,
  type: 1,
  name: '',
  enName: '',
  coverImg: '',
  intro: '',
  description: '',
  habits: '',
  appearance: '',
  careTips: '',
  size: '',
  hair: '',
  characterTrait: '',
  origin: '',
  lifeSpan: '',
  difficulty: 1,
  sort: 0,
})

// 上传新图后写回 coverImg
watch(coverList, (v) => {
  if (v.length) form.coverImg = v[0]
})

// 品种图案上传：自动保存到「宠物图片文件夹」，返回 /breeds/xxx
function doUploadImage(file: File) {
  return uploadBreedImage(file, form.id)
}

async function load() {
  loading.value = true
  try {
    const res = await adminPetBreeds({
      type: query.type,
      keyword: query.keyword || undefined,
      pageNum: pageNum.value,
      pageSize,
    })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function search() {
  pageNum.value = 1
  load()
}
function onPage(p: number) {
  pageNum.value = p
  load()
}

function openCreate() {
  Object.assign(form, {
    id: undefined,
    type: query.type || 1,
    name: '',
    enName: '',
    coverImg: '',
    intro: '',
    description: '',
    habits: '',
    appearance: '',
    careTips: '',
    size: '',
    hair: '',
    characterTrait: '',
    origin: '',
    lifeSpan: '',
    difficulty: 1,
    sort: 0,
  })
  coverList.value = []
  dialog.value = true
}

function openEdit(row: PetBreed) {
  Object.assign(form, { ...row })
  coverList.value = []
  dialog.value = true
}

async function save() {
  if (!form.name?.trim()) return ElMessage.warning('请填写品种名')
  saving.value = true
  try {
    if (form.id) {
      await adminPetBreedUpdate(form)
      ElMessage.success('已保存')
    } else {
      await adminPetBreedCreate(form)
      ElMessage.success('已新增')
    }
    dialog.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function toggle(row: PetBreed, on: boolean) {
  await adminPetBreedStatus(row.id, on ? 1 : 0)
  row.status = on ? 1 : 0
  ElMessage.success(on ? '已上架' : '已下架')
}

async function onDelete(row: PetBreed) {
  await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'warning' })
  await adminPetBreedDelete(row.id)
  ElMessage.success('已删除')
  load()
}

// ---------- 推荐映射 ----------
const recDialog = ref(false)
const recSaving = ref(false)
const recBreed = ref<PetBreed | null>(null)
const productOptions = ref<{ id: number; name: string }[]>([])
const recSlots = reactive([
  { label: '主粮 1', type: 1, productId: undefined as number | undefined },
  { label: '主粮 2', type: 1, productId: undefined as number | undefined },
  { label: '其他 1', type: 2, productId: undefined as number | undefined },
])

async function loadProductOptions() {
  if (productOptions.value.length) return
  const res = await adminProductPage({ pageNum: 1, pageSize: 500 })
  productOptions.value = res.list.map((p) => ({ id: p.id, name: p.name }))
}

async function openRecommend(row: PetBreed) {
  recBreed.value = row
  recSlots.forEach((s) => (s.productId = undefined))
  await loadProductOptions()
  const rows = await adminPetBreedRecommends(row.id)
  const main = rows.filter((r) => r.type === 1)
  const other = rows.find((r) => r.type === 2)
  if (main[0]) recSlots[0].productId = main[0].productId
  if (main[1]) recSlots[1].productId = main[1].productId
  if (other) recSlots[2].productId = other.productId
  recDialog.value = true
}

async function saveRecommend() {
  if (!recBreed.value) return
  const items = recSlots
    .filter((s) => s.productId)
    .map((s) => ({ productId: s.productId as number, type: s.type }))
  recSaving.value = true
  try {
    await adminPetBreedSaveRecommends(recBreed.value.id, items)
    ElMessage.success('推荐已保存')
    recDialog.value = false
  } finally {
    recSaving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ops {
  display: flex;
  gap: 8px;
}
.thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 6px;
  background: #f5f7fa;
}
.stars {
  color: #f5b301;
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
.admin-pet {
  padding-bottom: 60px;
}
.cover-edit {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}
.cover-preview {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}
.rec-hint {
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}
.rec-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.rec-label {
  width: 64px;
  color: #606266;
  font-size: 14px;
}
</style>
