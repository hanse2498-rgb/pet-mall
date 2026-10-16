<template>
  <div class="page-container" v-loading="loading">
    <el-card class="block">
      <template #header>秒杀结算</template>
      <div v-if="item" class="item-line">
        <img :src="item.coverImg || placeholder" class="thumb" />
        <div class="meta">
          <div class="name">{{ item.productName }}</div>
          <div class="spec">{{ item.specName }}</div>
          <div class="prices">
            <span class="sec">秒杀价 ¥{{ num(item.seckillPrice) }}</span>
            <span class="ori">原价 ¥{{ num(item.normalPrice) }}</span>
          </div>
        </div>
        <div class="count">×1</div>
      </div>
    </el-card>

    <el-card class="block">
      <template #header>收货地址</template>
      <div v-if="addresses.length" class="addr-list">
        <div
          v-for="a in addresses"
          :key="a.id"
          class="addr-item"
          :class="{ active: addrId === a.id }"
          @click="addrId = a.id"
        >
          <b>{{ a.receiverName }}</b> {{ a.phone }}
          <div class="gray">{{ [a.province, a.city, a.district, a.detail].filter(Boolean).join(' ') }}</div>
        </div>
      </div>
      <el-empty v-else description="还没有收货地址" :image-size="60" />
      <el-button type="primary" plain style="margin-top: 12px" @click="openAddrDialog">＋ 新增地址</el-button>
    </el-card>

    <el-card class="block">
      <template #header>金额</template>
      <div class="line"><span>商品金额</span><span>¥{{ num(goodsAmount) }}</span></div>
      <div class="line"><span>运费</span><span>{{ freight === 0 ? '包邮' : '¥' + num(freight) }}</span></div>
      <el-divider style="margin: 8px 0" />
      <div class="line total"><span>应付</span><b>¥{{ num(payAmount) }}</b></div>
    </el-card>

    <div class="submit-bar">
      <el-button type="danger" size="large" :loading="submitting" :disabled="!addrId" @click="onSubmit">
        立即秒杀（¥{{ num(payAmount) }}）
      </el-button>
    </div>

    <!-- 新增地址 -->
    <el-dialog v-model="addrDialog" title="新增收货地址" width="460px">
      <el-form :model="addrForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="addrForm.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addrForm.phone" maxlength="11" /></el-form-item>
        <el-form-item label="省市区">
          <el-input v-model="addrForm.province" placeholder="省" style="width: 32%" />
          <el-input v-model="addrForm.city" placeholder="市" style="width: 32%" />
          <el-input v-model="addrForm.district" placeholder="区" style="width: 32%" />
        </el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addrForm.detail" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addrDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingAddr" @click="saveAddr">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSeckillActivity, createSeckillOrder, type SeckillItemVO } from '@/api/seckill'
import { addAddress, listAddresses, type Address } from '@/api/address'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const item = ref<SeckillItemVO | null>(null)
const addresses = ref<Address[]>([])
const addrId = ref<number>()
const placeholder = '/products/placeholder.svg'

// 新增地址
const addrDialog = ref(false)
const savingAddr = ref(false)
const addrForm = reactive<Partial<Address>>({})

function openAddrDialog() {
  Object.keys(addrForm).forEach((k) => delete (addrForm as Record<string, unknown>)[k])
  addrDialog.value = true
}

async function saveAddr() {
  if (!addrForm.receiverName || !/^1\d{10}$/.test(addrForm.phone || '') || !addrForm.detail) {
    ElMessage.warning('请完整填写收货人、手机号、详细地址')
    return
  }
  savingAddr.value = true
  try {
    const saved = await addAddress(addrForm as Address)
    addrId.value = saved.id
    addrDialog.value = false
    addresses.value = await listAddresses()
    ElMessage.success('地址已新增')
  } finally {
    savingAddr.value = false
  }
}

const goodsAmount = computed(() => Number(item.value?.seckillPrice ?? 0))
const freight = computed(() => (goodsAmount.value >= 200 ? 0 : 10))
const payAmount = computed(() => goodsAmount.value + freight.value)

function num(n: number | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

async function load() {
  loading.value = true
  try {
    const activityId = route.query.activityId as string
    const itemId = route.query.itemId as string
    const act = await getSeckillActivity(activityId)
    item.value = act.items.find((i) => String(i.itemId) === itemId) || null
    if (!item.value) {
      ElMessage.warning('秒杀商品不存在')
      router.replace('/seckill')
      return
    }
    addresses.value = await listAddresses()
    const def = addresses.value.find((a) => a.isDefault === 1) || addresses.value[0]
    addrId.value = def?.id
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!addrId.value) return ElMessage.warning('请选择收货地址')
  submitting.value = true
  try {
    const res = await createSeckillOrder({
      activityId: route.query.activityId as string,
      itemId: route.query.itemId as string,
      addressId: addrId.value,
      quantity: 1,
    })
    ElMessage.success('秒杀成功，请完成支付')
    router.push(`/pay/${res.orderId}`)
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.block {
  margin-bottom: 16px;
}
.item-line {
  display: flex;
  gap: 14px;
  align-items: center;
}
.thumb {
  width: 84px;
  height: 84px;
  object-fit: cover;
  border-radius: 8px;
}
.meta {
  flex: 1;
}
.name {
  font-weight: 600;
}
.spec {
  color: #909399;
  font-size: 13px;
}
.prices {
  margin-top: 6px;
}
.sec {
  color: #f56c6c;
  font-size: 20px;
  font-weight: 800;
  margin-right: 10px;
}
.ori {
  color: #c0c4cc;
  text-decoration: line-through;
}
.count {
  color: #909399;
}
.addr-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 10px;
}
.addr-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px 12px;
  cursor: pointer;
}
.addr-item.active {
  border-color: #f56c6c;
  background: #fef6f6;
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
.submit-bar {
  display: flex;
  justify-content: flex-end;
}
</style>