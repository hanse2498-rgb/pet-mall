<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="title">秒杀场次</span>
        <el-button type="primary" @click="openActivity()">＋ 新建场次</el-button>
      </div>

      <el-table :data="activities" v-loading="loading" border stripe>
        <el-table-column prop="name" label="场次名" min-width="160" />
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ fmt(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">{{ fmt(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openActivity(row)">编辑</el-button>
            <el-button link type="warning" @click="openItems(row)">商品</el-button>
            <el-button link type="danger" @click="onDeleteActivity(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 场次新增/编辑 -->
    <el-dialog v-model="actDialog" :title="actForm.id ? '编辑场次' : '新建场次'" width="480px">
      <el-form :model="actForm" label-width="90px">
        <el-form-item label="场次名"><el-input v-model="actForm.name" /></el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="actForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="actForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" v-if="actForm.id">
          <el-radio-group v-model="actForm.status">
            <el-radio :value="0">未开始</el-radio>
            <el-radio :value="3">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingAct" @click="saveActivity">保存</el-button>
      </template>
    </el-dialog>

    <!-- 秒杀商品管理 -->
    <el-dialog v-model="itemDialog" :title="`秒杀商品 - ${currentAct?.name || ''}`" width="860px">
      <el-table :data="items" size="small" border>
        <el-table-column prop="skuId" label="SKU" width="90" />
        <el-table-column label="秒杀价">
          <template #default="{ row }"><el-input-number v-model="row.seckillPrice" :min="0" :precision="2" size="small" /></template>
        </el-table-column>
        <el-table-column label="秒杀库存">
          <template #default="{ row }"><el-input-number v-model="row.seckillStock" :min="0" size="small" /></template>
        </el-table-column>
        <el-table-column prop="seckillSold" label="已抢" width="70" />
        <el-table-column label="限购">
          <template #default="{ row }"><el-input-number v-model="row.perUserLimit" :min="1" size="small" /></template>
        </el-table-column>
        <el-table-column label="启用" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(v: string | number | boolean) => saveRow(row, { status: Boolean(v) ? 1 : 0 })" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="saveRow(row)">保存</el-button>
            <el-button link type="danger" @click="onDeleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-divider>新增秒杀商品</el-divider>
      <el-form inline>
        <el-form-item label="SKU id"><el-input v-model="newItem.skuId" style="width: 110px" /></el-form-item>
        <el-form-item label="秒杀价"><el-input-number v-model="newItem.seckillPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="newItem.seckillStock" :min="0" /></el-form-item>
        <el-form-item label="限购"><el-input-number v-model="newItem.perUserLimit" :min="1" /></el-form-item>
        <el-button type="primary" @click="addItem">新增</el-button>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminSeckillActivities,
  adminSeckillCreateActivity,
  adminSeckillDeleteActivity,
  adminSeckillUpdateActivity,
  adminSeckillItems,
  adminSeckillAddItem,
  adminSeckillUpdateItem,
  adminSeckillDeleteItem,
  type SeckillActivityRow,
  type SeckillItemRow,
} from '@/api/admin'

const loading = ref(false)
const activities = ref<SeckillActivityRow[]>([])

const actDialog = ref(false)
const savingAct = ref(false)
const actForm = reactive<Partial<SeckillActivityRow>>({})

const itemDialog = ref(false)
const currentAct = ref<SeckillActivityRow>()
const items = ref<SeckillItemRow[]>([])
const newItem = reactive<Partial<SeckillItemRow>>({ skuId: undefined, seckillPrice: 0, seckillStock: 0, perUserLimit: 1 })

function fmt(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 19) : '-'
}
function statusText(s: number | undefined): string {
  return s === 3 ? '关闭' : s === 1 ? '进行中' : s === 2 ? '已结束' : '未开始'
}
function statusType(s: number | undefined): 'info' | 'success' | 'warning' {
  return s === 3 ? 'info' : s === 1 ? 'success' : 'warning'
}

async function load() {
  loading.value = true
  try {
    activities.value = await adminSeckillActivities()
  } finally {
    loading.value = false
  }
}

function openActivity(row?: SeckillActivityRow) {
  if (row) {
    Object.assign(actForm, row)
  } else {
    Object.keys(actForm).forEach((k) => delete (actForm as Record<string, unknown>)[k])
    Object.assign(actForm, { name: '', startTime: '', endTime: '', status: 0 })
  }
  actDialog.value = true
}

async function saveActivity() {
  if (!actForm.name || !actForm.startTime || !actForm.endTime) {
    ElMessage.warning('请填写场次名与起止时间')
    return
  }
  savingAct.value = true
  try {
    if (actForm.id) {
      await adminSeckillUpdateActivity(actForm.id, actForm)
      ElMessage.success('已更新')
    } else {
      await adminSeckillCreateActivity({
        name: actForm.name!,
        startTime: actForm.startTime!,
        endTime: actForm.endTime!,
      })
      ElMessage.success('已创建')
    }
    actDialog.value = false
    load()
  } finally {
    savingAct.value = false
  }
}

async function onDeleteActivity(row: SeckillActivityRow) {
  await ElMessageBox.confirm(`确定删除场次「${row.name}」及其商品吗？`, '提示', { type: 'warning' })
  await adminSeckillDeleteActivity(row.id)
  ElMessage.success('已删除')
  load()
}

async function openItems(row: SeckillActivityRow) {
  currentAct.value = row
  items.value = await adminSeckillItems(row.id)
  Object.assign(newItem, { skuId: undefined, seckillPrice: 0, seckillStock: 0, perUserLimit: 1 })
  itemDialog.value = true
}

async function saveRow(row: SeckillItemRow, extra?: Partial<SeckillItemRow>) {
  await adminSeckillUpdateItem(row.id, { ...row, ...extra })
  ElMessage.success('已保存')
}

async function addItem() {
  if (!currentAct.value) return
  if (!newItem.skuId || (newItem.seckillPrice ?? 0) <= 0) return ElMessage.warning('请填写 SKU 与秒杀价')
  await adminSeckillAddItem(currentAct.value.id, newItem)
  ElMessage.success('已新增')
  items.value = await adminSeckillItems(currentAct.value.id)
}

async function onDeleteItem(row: SeckillItemRow) {
  await ElMessageBox.confirm('确定删除该秒杀商品吗？', '提示', { type: 'warning' })
  await adminSeckillDeleteItem(row.id)
  items.value = await adminSeckillItems(currentAct.value!.id)
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.title {
  font-weight: 700;
}
</style>