<template>
  <div class="admin-order">
    <el-card>
      <el-tabs v-model="activeStatus" @tab-change="search">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待支付" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="210" />
        <el-table-column prop="orderId" label="ID" width="90" />
        <el-table-column label="商品" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ itemsText(row) }}
          </template>
        </el-table-column>
        <el-table-column label="实付" width="100">
          <template #default="{ row }">¥{{ num(row.payAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="OrderStatusTagType[row.status]" effect="dark">{{ OrderStatusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" link type="success" @click="openShip(row)">发货</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="pageSize"
          :current-page="pageNum" @current-change="onPage" />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-drawer v-model="detailVisible" title="订单详情" size="480px">
      <template v-if="detail">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="OrderStatusTagType[detail.status]">{{ OrderStatusMap[detail.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ fmt(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="实付">¥{{ num(detail.payAmount) }}（商品 {{ num(detail.goodsAmount) }} + 运费 {{ num(detail.freight) }}）</el-descriptions-item>
          <el-descriptions-item v-if="detail.payNo" label="支付单">{{ detail.payNo }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.logisticsCompany" label="物流">
            {{ detail.logisticsCompany }} {{ detail.trackingNo }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">收货信息</el-divider>
        <div v-if="receiver">
          {{ receiver.receiverName }} {{ receiver.phone }}
          <div class="gray">{{ [receiver.province, receiver.city, receiver.district, receiver.detail].filter(Boolean).join(' ') }}</div>
        </div>

        <el-divider content-position="left">商品</el-divider>
        <div v-for="it in detail.items" :key="it.skuId" class="item">
          <div class="gray">{{ it.productName }}（{{ it.spec }}）×{{ it.quantity }}</div>
          <div>¥{{ num(it.subtotal) }}</div>
        </div>
      </template>
    </el-drawer>

    <!-- 发货 -->
    <el-dialog v-model="shipDialog" title="订单发货" width="420px">
      <el-form label-position="top">
        <el-form-item label="物流公司">
          <el-input v-model="shipForm.logisticsCompany" placeholder="如：顺丰速运" />
        </el-form-item>
        <el-form-item label="运单号">
          <el-input v-model="shipForm.trackingNo" placeholder="填写物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialog = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="onShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminOrderDetail, adminOrderShip, adminOrders } from '@/api/admin'
import { OrderStatusMap, OrderStatusTagType, type OrderVO } from '@/api/order'

const loading = ref(false)
const list = ref<OrderVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const activeStatus = ref('all')

const detailVisible = ref(false)
const detail = ref<OrderVO | null>(null)

const shipDialog = ref(false)
const shipping = ref(false)
const shipForm = reactive({ orderId: 0, logisticsCompany: '', trackingNo: '' })

const receiver = computed(() => {
  if (!detail.value?.receiver) return null
  try {
    return JSON.parse(detail.value.receiver) as {
      receiverName: string; phone: string; province?: string; city?: string; district?: string; detail: string
    }
  } catch {
    return null
  }
})

function num(n: number | undefined): string {
  return Number(n ?? 0).toFixed(2)
}
function fmt(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 19) : '-'
}
function itemsText(row: OrderVO): string {
  return row.items.map((it) => `${it.productName} ×${it.quantity}`).join('；')
}

async function load() {
  loading.value = true
  try {
    const status = activeStatus.value === 'all' ? undefined : Number(activeStatus.value)
    const res = await adminOrders(status, pageNum.value, pageSize)
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

async function openDetail(row: OrderVO) {
  detail.value = await adminOrderDetail(row.orderId)
  detailVisible.value = true
}

function openShip(row: OrderVO) {
  Object.assign(shipForm, { orderId: row.orderId, logisticsCompany: '', trackingNo: '' })
  shipDialog.value = true
}

async function onShip() {
  if (!shipForm.logisticsCompany || !shipForm.trackingNo) {
    ElMessage.warning('请填写物流公司与运单号')
    return
  }
  shipping.value = true
  try {
    await adminOrderShip(shipForm.orderId, shipForm.logisticsCompany, shipForm.trackingNo)
    ElMessage.success('发货成功')
    shipDialog.value = false
    load()
  } finally {
    shipping.value = false
  }
}

onMounted(load)
</script>

<style scoped>
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
.admin-order {
  padding-bottom: 60px;
}
.gray {
  color: #909399;
}
.item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
}
</style>