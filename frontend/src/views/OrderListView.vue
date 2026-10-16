<template>
  <div class="page-container">
    <el-card>
      <template #header>我的订单</template>

      <el-tabs v-model="activeStatus" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待支付" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>

      <div v-loading="loading">
        <el-empty v-if="!loading && !orders.length" description="暂无订单" />

        <el-card v-for="o in orders" :key="o.orderId" class="order-card" shadow="never">
          <div class="order-head">
            <div class="order-no">
              {{ o.orderNo }}
              <span class="time">{{ formatTime(o.createTime) }}</span>
            </div>
            <el-tag :type="OrderStatusTagType[o.status]" effect="dark">{{ OrderStatusMap[o.status] }}</el-tag>
          </div>

          <div class="order-body">
            <div class="items">
              <div v-for="it in o.items" :key="it.skuId" class="item">
                <img :src="it.skuImg || placeholder" class="thumb" />
                <div class="item-meta">
                  <div>{{ it.productName }}</div>
                  <div class="spec">{{ it.spec }} ×{{ it.quantity }}</div>
                </div>
                <div class="item-price">¥{{ num(it.subtotal) }}</div>
              </div>
            </div>
            <div class="summary">
              <div>实付：<b class="pay">¥{{ num(o.payAmount) }}</b></div>
              <div class="actions">
                <el-button
                  v-if="o.status === 0"
                  type="danger"
                  @click="router.push(`/pay/${o.orderId}`)"
                >
                  去支付
                </el-button>
                <el-button
                  v-if="o.status === 0 || o.status === 1"
                  plain
                  @click="onCancel(o)"
                >
                  取消订单
                </el-button>
                <el-button
                  v-if="o.status === 2"
                  type="success"
                  @click="onReceive(o)"
                >
                  确认收货
                </el-button>
                <el-button plain @click="router.push(`/orders/${o.orderId}`)">查看详情</el-button>
              </div>
            </div>
          </div>
        </el-card>

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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrder, getOrders, receiveOrder, OrderStatusMap, OrderStatusTagType, type OrderVO } from '@/api/order'

const router = useRouter()
const activeStatus = ref('all')
const orders = ref<OrderVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const placeholder = '/products/placeholder.svg'

function num(n: number | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

function formatTime(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 19) : ''
}

async function load() {
  loading.value = true
  try {
    const status = activeStatus.value === 'all' ? undefined : Number(activeStatus.value)
    const res = await getOrders(status, pageNum.value, pageSize)
    orders.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  pageNum.value = 1
  load()
}

function onPageChange(p: number) {
  pageNum.value = p
  load()
}

async function onCancel(o: OrderVO) {
  await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
  await cancelOrder(o.orderId)
  ElMessage.success('订单已取消')
  load()
}

async function onReceive(o: OrderVO) {
  await ElMessageBox.confirm('确认已收到货吗？', '提示')
  await receiveOrder(o.orderId)
  ElMessage.success('确认收货成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.order-card {
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
}
.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #f2f3f5;
}
.order-no {
  font-weight: 600;
}
.time {
  font-weight: 400;
  color: #909399;
  margin-left: 12px;
  font-size: 13px;
}
.order-body {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
  margin-top: 10px;
}
.items {
  flex: 1;
}
.item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 0;
}
.thumb {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 6px;
  background: #f5f7fa;
}
.item-meta {
  flex: 1;
}
.spec {
  color: #909399;
  font-size: 12px;
}
.item-price {
  width: 90px;
  text-align: right;
}
.summary {
  width: 180px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}
.pay {
  color: #f56c6c;
  font-size: 16px;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}
.pager {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}
</style>