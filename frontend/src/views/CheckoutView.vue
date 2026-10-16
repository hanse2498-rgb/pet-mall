<template>
  <div class="page-container" v-loading="loading">
    <el-card class="block">
      <template #header>收货地址</template>
      <div v-if="addresses.length" class="addr-list">
        <div
          v-for="a in addresses"
          :key="a.id"
          class="addr-item"
          :class="{ active: selectedAddressId === a.id }"
          @click="selectedAddressId = a.id"
        >
          <div class="addr-main">
            <b>{{ a.receiverName }}</b> {{ a.phone }}
            <el-tag v-if="a.isDefault === 1" size="small" type="primary" style="margin-left: 8px">默认</el-tag>
          </div>
          <div class="addr-detail">{{ [a.province, a.city, a.district, a.detail].filter(Boolean).join(' ') }}</div>
        </div>
      </div>
      <el-empty v-else description="还没有收货地址" :image-size="60" />
      <el-button type="primary" plain style="margin-top: 12px" @click="openAddressDialog()">
        ＋ 新增地址
      </el-button>
    </el-card>

    <el-card class="block">
      <template #header>商品清单</template>
      <div class="co-item">
        <div class="co-name">商品</div>
        <div class="co-qty">数量</div>
        <div class="co-amount">小计</div>
      </div>
      <div v-for="it in orderItems" :key="it.skuId" class="co-item">
        <div class="co-name">
          <img :src="it.coverImg || placeholder" class="thumb" />
          <div>
            <div>{{ it.productName }}</div>
            <div class="spec">{{ it.specName || '默认规格' }}</div>
          </div>
        </div>
        <div class="co-qty">×{{ it.quantity }}</div>
        <div class="co-amount">¥{{ num(it.subtotal) }}</div>
      </div>
      <el-input
        v-model="remark"
        placeholder="订单备注（选填）"
        maxlength="100"
        style="margin-top: 12px"
      />
    </el-card>

    <el-card class="block">
      <template #header>金额明细</template>
      <div class="line"><span>商品金额</span><span>¥{{ num(goodsAmount) }}</span></div>
      <div class="line">
        <span>运费</span>
        <span>{{ freight === 0 ? '包邮' : '¥' + num(freight) }}</span>
      </div>
      <el-divider style="margin: 8px 0" />
      <div class="line total">
        <span>应付总额</span>
        <b>¥{{ num(payAmount) }}</b>
      </div>
    </el-card>

    <div class="submit-bar">
      <el-button type="danger" size="large" :loading="submitting" :disabled="!canSubmit" @click="onSubmit">
        提交订单（¥{{ num(payAmount) }}）
      </el-button>
    </div>

    <!-- 新增/编辑地址 -->
    <el-dialog v-model="addrDialog" :title="addressForm.id ? '编辑地址' : '新增地址'" width="480px">
      <el-form :model="addressForm" label-width="80px">
        <el-form-item label="收货人"><el-input v-model="addressForm.receiverName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addressForm.phone" maxlength="11" /></el-form-item>
        <el-form-item label="省市区">
          <el-input v-model="addressForm.province" placeholder="省" style="width: 32%" />
          <el-input v-model="addressForm.city" placeholder="市" style="width: 32%" />
          <el-input v-model="addressForm.district" placeholder="区" style="width: 32%" />
        </el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detail" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addrDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingAddr" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listAddresses, addAddress, updateAddress, type Address } from '@/api/address'
import { getCartSelected, type CartItem } from '@/api/cart'
import { getSkuInfo } from '@/api/product'
import { createOrder } from '@/api/order'

interface OrderLine {
  skuId: number | string
  productName: string
  coverImg?: string
  specName?: string
  price: number
  quantity: number
  subtotal: number
}

const FREE_THRESHOLD = 200
const FLAT_FREIGHT = 10

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)

const isDirect = computed(() => !!route.query.skuId)
const addresses = ref<Address[]>([])
const selectedAddressId = ref<number>()
const orderItems = ref<OrderLine[]>([])
const remark = ref('')

const addrDialog = ref(false)
const savingAddr = ref(false)
const addressForm = reactive<Partial<Address>>({})

const placeholder = '/products/placeholder.svg'

const goodsAmount = computed(() => orderItems.value.reduce((s, it) => s + Number(it.subtotal), 0))
const freight = computed(() => (goodsAmount.value >= FREE_THRESHOLD ? 0 : FLAT_FREIGHT))
const payAmount = computed(() => goodsAmount.value + freight.value)
const canSubmit = computed(() => !!selectedAddressId.value && orderItems.value.length > 0 && goodsAmount.value > 0)

function num(n: number | string | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

async function loadAddresses() {
  addresses.value = await listAddresses()
  if (!selectedAddressId.value) {
    const def = addresses.value.find((a) => a.isDefault === 1) || addresses.value[0]
    selectedAddressId.value = def?.id
  }
}

async function load() {
  loading.value = true
  try {
    await loadAddresses()
    if (isDirect.value) {
      const skuId = route.query.skuId as string
      const qty = Number(route.query.quantity || 1)
      const info = await getSkuInfo(skuId)
      orderItems.value = [
        {
          skuId: info.skuId,
          productName: info.productName,
          coverImg: info.coverImg,
          specName: info.specName,
          price: Number(info.price),
          quantity: qty,
          subtotal: Number(info.price) * qty,
        },
      ]
    } else {
      const selected: CartItem[] = await getCartSelected()
      orderItems.value = selected.map((c) => ({
        skuId: c.skuId,
        productName: c.productName,
        coverImg: c.coverImg,
        specName: c.specName,
        price: Number(c.price),
        quantity: c.quantity,
        subtotal: Number(c.subtotal),
      }))
    }
  } finally {
    loading.value = false
  }
}

function openAddressDialog(address?: Address) {
  if (address) {
    Object.assign(addressForm, address)
  } else {
    Object.keys(addressForm).forEach((k) => delete (addressForm as Record<string, unknown>)[k])
  }
  addrDialog.value = true
}

async function saveAddress() {
  if (!addressForm.receiverName || !/^1\d{10}$/.test(addressForm.phone || '') || !addressForm.detail) {
    ElMessage.warning('请完整填写收货人、手机号、详细地址')
    return
  }
  savingAddr.value = true
  try {
    if (addressForm.id) {
      await updateAddress(addressForm as Address)
      ElMessage.success('地址已更新')
    } else {
      const saved = await addAddress(addressForm as Address)
      selectedAddressId.value = saved.id
      ElMessage.success('地址已新增')
    }
    addrDialog.value = false
    await loadAddresses()
  } finally {
    savingAddr.value = false
  }
}

async function onSubmit() {
  if (!canSubmit.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  submitting.value = true
  try {
    const res = await createOrder({
      addressId: selectedAddressId.value!,
      remark: remark.value,
      clientToken: crypto.randomUUID(),
      items: orderItems.value.map((it) => ({ skuId: it.skuId, quantity: it.quantity })),
    })
    ElMessage.success('订单已创建，请完成支付')
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
.addr-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}
.addr-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
}
.addr-item.active {
  border-color: #f56c6c;
  background: #fef6f6;
}
.addr-main {
  margin-bottom: 4px;
}
.addr-detail {
  color: #909399;
  font-size: 13px;
}
.co-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f7fa;
}
.co-name {
  flex: 1;
  display: flex;
  gap: 12px;
  align-items: center;
}
.thumb {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 6px;
  background: #f5f7fa;
}
.spec {
  color: #909399;
  font-size: 12px;
}
.co-qty {
  width: 80px;
  text-align: center;
  color: #909399;
}
.co-amount {
  width: 100px;
  text-align: right;
}
.line {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  color: #606266;
}
.line.total {
  font-size: 16px;
  color: #303133;
}
.line.total b {
  color: #f56c6c;
  font-size: 20px;
}
.submit-bar {
  display: flex;
  justify-content: flex-end;
}
</style>