<template>
  <div class="admin-after-sale">
    <el-card>
      <template #header>
        <div class="head">
          <span>售后管理</span>
          <el-select v-model="status" placeholder="全部状态" clearable style="width: 140px" @change="search">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已撤销" :value="3" />
          </el-select>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="afterSaleNo" label="售后单号" width="190" />
        <el-table-column prop="orderNo" label="订单号" width="190" />
        <el-table-column prop="reason" label="原因" width="130" />
        <el-table-column label="退款金额" width="110">
          <template #default="{ row }">¥{{ num(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="AfterSaleStatusTagType[row.status]" effect="dark" size="small">
              {{ AfterSaleStatusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="170">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="120">
          <template #default="{ row }">{{ row.adminRemark || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="openAudit(row, true)">通过退款</el-button>
              <el-button size="small" type="danger" plain @click="openAudit(row, false)">拒绝</el-button>
            </template>
            <span v-else class="gray">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="pageSize"
          :current-page="pageNum" @current-change="onPage" />
      </div>
    </el-card>

    <el-dialog v-model="auditDialog" :title="auditPass ? '通过并退款' : '拒绝售后'" width="420px">
      <div class="audit-line">售后单：{{ current?.afterSaleNo }}</div>
      <div class="audit-line">退款金额：¥{{ num(current?.amount) }}</div>
      <el-form label-position="top" style="margin-top: 12px">
        <el-form-item :label="auditPass ? '备注（选填）' : '拒绝原因'">
          <el-input v-model="remark" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog = false">取消</el-button>
        <el-button :type="auditPass ? 'success' : 'danger'" :loading="submitting" @click="doAudit">
          {{ auditPass ? '确认退款' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAfterSales, adminAfterSaleAudit } from '@/api/admin'
import { AfterSaleStatusMap, AfterSaleStatusTagType, type AfterSaleVO } from '@/api/afterSale'

const loading = ref(false)
const list = ref<AfterSaleVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const status = ref<number>()

const auditDialog = ref(false)
const auditPass = ref(true)
const remark = ref('')
const submitting = ref(false)
const current = ref<AfterSaleVO | null>(null)

function num(n: number | undefined): string {
  return Number(n ?? 0).toFixed(2)
}
function fmt(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 19) : '—'
}

async function load() {
  loading.value = true
  try {
    const res = await adminAfterSales(status.value, pageNum.value, pageSize)
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

function openAudit(row: AfterSaleVO, pass: boolean) {
  current.value = row
  auditPass.value = pass
  remark.value = ''
  auditDialog.value = true
}

async function doAudit() {
  if (!current.value) return
  if (!auditPass.value && !remark.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  submitting.value = true
  try {
    await adminAfterSaleAudit(current.value.id, auditPass.value, remark.value.trim() || undefined)
    ElMessage.success(auditPass.value ? '已通过并退款' : '已拒绝')
    auditDialog.value = false
    load()
  } finally {
    submitting.value = false
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
.admin-after-sale {
  padding-bottom: 60px;
}
.gray {
  color: #909399;
}
.audit-line {
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
}
</style>
