<template>
  <div class="page-container">
    <el-card class="pay-card" v-loading="loading">
      <template #header>收银台</template>

      <template v-if="order">
        <div class="amount" :class="{ done: paid }">
          <span>{{ paid ? '支付成功' : '应付金额' }}</span>
          <b>¥{{ num(order.payAmount) }}</b>
        </div>
        <div class="no">订单号：{{ order.orderNo }}</div>

        <!-- 待支付：展示模拟支付按钮 -->
        <template v-if="order.status === 0 && !paid">
          <div class="btns">
            <el-button type="success" size="large" :loading="paying" @click="doPay(false)">
              模拟支付成功
            </el-button>
            <el-button size="large" :loading="paying" @click="doPay(true)">模拟支付失败</el-button>
          </div>
          <el-alert
            type="info"
            :closable="false"
            title="当前为模拟支付：点击成功订单进入「待发货」，点击失败可查看失败状态。"
            style="margin-top: 16px"
          />
        </template>

        <el-result v-else-if="paid" icon="success" title="支付成功" sub-title="商家正在为你准备发货">
          <template #extra>
            <el-button type="primary" @click="router.push(`/orders/${order.orderId}`)">查看订单</el-button>
            <el-button @click="router.push('/products')">继续逛逛</el-button>
          </template>
        </el-result>

        <el-result v-else-if="order.status === 4" icon="info" title="订单已取消" />
        <el-result v-else-if="order.status !== 0" icon="success" :title="OrderStatusMap[order.status]" />
        <el-result v-else-if="failed" icon="error" title="支付失败（模拟）" sub-title="你可以重新发起支付">
          <template #extra>
            <el-button type="primary" @click="paid = false; failed = false">重新支付</el-button>
          </template>
        </el-result>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrder, payOrder, OrderStatusMap, type OrderVO } from '@/api/order'
import { removeCartBatch } from '@/api/cart'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const order = ref<OrderVO | null>(null)
const loading = ref(false)
const paying = ref(false)
const paid = ref(false)
const failed = ref(false)

function num(n: number | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

async function load() {
  loading.value = true
  try {
    order.value = await getOrder(route.params.orderId as string)
  } finally {
    loading.value = false
  }
}

async function doPay(simulateFail: boolean) {
  if (!order.value) return
  paying.value = true
  try {
    const res = await payOrder(order.value.orderId, simulateFail)
    if (res.paid) {
      paid.value = true
      ElMessage.success('支付成功')
      // 清理购物车中已购买的商品
      const skuIds = order.value!.items.map((it) => it.skuId)
      if (skuIds.length) {
        await removeCartBatch(skuIds)
        cartStore.fetchCount()
      }
      await load()
    } else {
      failed.value = true
      ElMessage.warning(res.message || '支付失败')
    }
  } finally {
    paying.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.pay-card {
  max-width: 640px;
  margin: 40px auto;
}
.amount {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  padding: 24px 0 8px;
  color: #606266;
}
.amount b {
  color: #f56c6c;
  font-size: 36px;
}
.amount.done b {
  color: #67c23a;
}
.no {
  text-align: center;
  color: #909399;
  margin-bottom: 20px;
}
.btns {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 12px;
}
</style>