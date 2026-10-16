<template>
  <div class="page-container" v-loading="loading">
    <template v-if="order">
      <el-card class="block">
        <template #header>
          <div class="head">
            <span>订单详情</span>
            <el-tag :type="OrderStatusTagType[order.status]" effect="dark" size="large">
              {{ OrderStatusMap[order.status] }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ order.payStatus === 1 ? '模拟支付' : '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatTime(order.payTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="order.payNo" label="支付单号">{{ order.payNo }}</el-descriptions-item>
          <el-descriptions-item v-if="order.logisticsCompany" label="物流公司">
            {{ order.logisticsCompany }}
          </el-descriptions-item>
          <el-descriptions-item v-if="order.trackingNo" label="运单号">{{ order.trackingNo }}</el-descriptions-item>
          <el-descriptions-item v-if="order.cancelTime" label="取消时间">
            {{ formatTime(order.cancelTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="block" v-if="receiver">
        <template #header>收货信息</template>
        <div>{{ receiver.receiverName }} {{ receiver.phone }}</div>
        <div class="gray">
          {{ [receiver.province, receiver.city, receiver.district, receiver.detail].filter(Boolean).join(' ') }}
        </div>
      </el-card>

      <el-card class="block">
        <template #header>商品清单</template>
        <div v-for="it in order.items" :key="it.skuId" class="item">
          <img :src="it.skuImg || placeholder" class="thumb" />
          <div class="meta">
            <div>{{ it.productName }}</div>
            <div class="gray">{{ it.spec }} ×{{ it.quantity }}</div>
            <div v-if="it.produceDate" class="gray">
              批次：生产 {{ it.produceDate }} · 到期 {{ it.expiryDate }}
            </div>
          </div>
          <div class="price">
            <div>¥{{ num(it.price) }}</div>
            <div class="gray">小计 ¥{{ num(it.subtotal) }}</div>
          </div>
          <el-button v-if="order.status === 3" link type="primary" @click="openReview(it)">评价</el-button>
        </div>
      </el-card>

      <el-card class="block">
        <template #header>金额明细</template>
        <div class="line"><span>商品金额</span><span>¥{{ num(order.goodsAmount) }}</span></div>
        <div class="line"><span>运费</span><span>{{ Number(order.freight) === 0 ? '包邮' : '¥' + num(order.freight) }}</span></div>
        <div class="line total"><span>实付金额</span><b>¥{{ num(order.payAmount) }}</b></div>
        <div class="line" v-if="order.remark"><span>备注</span><span>{{ order.remark }}</span></div>
      </el-card>

      <div class="actions">
        <el-button
          v-if="order.status === 0"
          type="danger"
          @click="router.push(`/pay/${order.orderId}`)"
        >
          去支付
        </el-button>
        <el-button v-if="order.status === 0 || order.status === 1" @click="onCancel">取消订单</el-button>
        <el-button v-if="order.status === 2" type="success" @click="onReceive">确认收货</el-button>
        <el-button v-if="canAfterSale" type="warning" plain @click="openAfterSale">申请售后</el-button>
        <el-tag v-if="order.status === 5" type="danger" effect="plain">售后处理中</el-tag>
        <el-button @click="router.push('/orders')">返回列表</el-button>
      </div>
    </template>

    <!-- 申请售后 -->
    <el-dialog v-model="asDialog" title="申请售后" width="460px">
      <el-form label-position="top">
        <el-form-item label="售后原因">
          <el-select v-model="asForm.reason" placeholder="请选择原因" style="width: 100%">
            <el-option v-for="r in reasonOptions" :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="asForm.description" type="textarea" :rows="3" maxlength="200" show-word-limit
            placeholder="选填" />
        </el-form-item>
        <div class="gray">退款金额 ¥{{ num(order?.payAmount) }}（整单退款，审核通过后原路退回）</div>
      </el-form>
      <template #footer>
        <el-button @click="asDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingAs" @click="submitAfterSale">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 写评价 -->
    <el-dialog v-model="reviewDialog" title="发表评价" width="480px">
      <div class="review-name">{{ reviewForm.productName }}</div>
      <el-form label-position="top">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit
            placeholder="说说你的使用体验吧" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="submittingReview" @click="onSubmitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrder, getOrder, receiveOrder, OrderStatusMap, OrderStatusTagType, type OrderVO, type OrderItemVO } from '@/api/order'
import { submitReview } from '@/api/review'
import { applyAfterSale } from '@/api/afterSale'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref<OrderVO | null>(null)
const placeholder = '/products/placeholder.svg'

const receiver = computed(() => {
  if (!order.value?.receiver) return null
  try {
    return JSON.parse(order.value.receiver) as {
      receiverName: string
      phone: string
      province?: string
      city?: string
      district?: string
      detail: string
    }
  } catch {
    return null
  }
})

function num(n: number | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

function formatTime(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 19) : '—'
}

async function load() {
  loading.value = true
  try {
    order.value = await getOrder(route.params.id as string)
  } finally {
    loading.value = false
  }
}

async function onCancel() {
  await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
  await cancelOrder(order.value!.orderId)
  ElMessage.success('订单已取消')
  load()
}

async function onReceive() {
  await ElMessageBox.confirm('确认已收到货吗？', '提示')
  await receiveOrder(order.value!.orderId)
  ElMessage.success('确认收货成功')
  load()
}

// ---------- 申请售后 ----------
const canAfterSale = computed(
  () => !!order.value && order.value.payStatus === 1 && [1, 2, 3].includes(order.value.status),
)
const reasonOptions = ['质量问题', '商品破损', '与描述不符', '少件/漏发', '不想要了', '其他']
const asDialog = ref(false)
const submittingAs = ref(false)
const asForm = reactive({ reason: '', description: '' })

function openAfterSale() {
  asForm.reason = ''
  asForm.description = ''
  asDialog.value = true
}

async function submitAfterSale() {
  if (!asForm.reason) return ElMessage.warning('请选择售后原因')
  submittingAs.value = true
  try {
    await applyAfterSale({
      orderId: order.value!.orderId,
      reason: asForm.reason,
      description: asForm.description.trim() || undefined,
    })
    ElMessage.success('售后申请已提交，等待审核')
    asDialog.value = false
    load()
  } finally {
    submittingAs.value = false
  }
}

// ---------- 评价 ----------
const reviewDialog = ref(false)
const submittingReview = ref(false)
const reviewForm = reactive({
  orderId: 0,
  productId: 0,
  skuId: 0,
  productName: '',
  rating: 5,
  content: '',
})

function openReview(it: OrderItemVO) {
  Object.assign(reviewForm, {
    orderId: order.value!.orderId,
    productId: 0,
    skuId: it.skuId,
    productName: it.productName,
    rating: 5,
    content: '',
  })
  reviewDialog.value = true
}

async function onSubmitReview() {
  if (reviewForm.rating < 1) return ElMessage.warning('请选择评分')
  if (!reviewForm.content.trim()) return ElMessage.warning('请输入评价内容')
  // productId 需通过详情查询
  const prod = await import('@/api/product').then((m) => {
    // 由 skuId 反查 productId
    return m.getSkuInfo(reviewForm.skuId)
  })
  reviewForm.productId = prod.productId
  submittingReview.value = true
  try {
    await submitReview({
      orderId: reviewForm.orderId,
      productId: reviewForm.productId,
      skuId: reviewForm.skuId,
      rating: reviewForm.rating,
      content: reviewForm.content.trim(),
    })
    ElMessage.success('评价成功，感谢你的反馈')
    reviewDialog.value = false
  } finally {
    submittingReview.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.block {
  margin-bottom: 16px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f7fa;
}
.thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  background: #f5f7fa;
}
.meta {
  flex: 1;
}
.price {
  text-align: right;
}
.gray {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}
.line {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  color: #606266;
}
.line.total b {
  color: #f56c6c;
  font-size: 18px;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.review-name {
  font-weight: 600;
  margin-bottom: 8px;
}
</style>