<template>
  <div class="page-container">
    <el-card>
      <template #header>🛒 购物车</template>

      <el-empty v-if="!loading && !items.length" description="购物车还是空的，去逛逛吧">
        <el-button type="primary" @click="router.push('/products')">去逛逛</el-button>
      </el-empty>

      <template v-else>
        <div class="cart-head">
          <el-checkbox :model-value="allSelected" :indeterminate="indeterminate" @change="toggleAll">
            全选
          </el-checkbox>
          <span>已选 <b>{{ selectedCount }}</b> 件商品</span>
        </div>

        <div v-for="it in items" :key="it.cartId" class="cart-item" :class="{ off: it.stock <= 0 }">
          <el-checkbox
            :model-value="it.selected === 1"
            :disabled="it.stock <= 0"
            @change="(v: string | number | boolean) => onSelect(it, Boolean(v))"
          />
          <img :src="it.coverImg || placeholder" class="thumb" @click="goDetail(it.productId)" />
          <div class="meta">
            <div class="name" @click="goDetail(it.productId)">
              {{ it.productName }}
              <el-tag v-if="it.isNearExpiry === 1" type="danger" size="small" effect="dark">临期</el-tag>
            </div>
            <div class="spec">{{ it.specName || '默认规格' }}</div>
            <div v-if="it.stock <= 0" class="out">缺货</div>
            <div v-else-if="it.stock < 10" class="out">仅剩 {{ it.stock }} 件</div>
          </div>
          <div class="price">¥ {{ num(it.price) }}</div>
          <el-input-number
            :model-value="it.quantity"
            :min="1"
            :max="Math.max(it.stock, 1)"
            :disabled="it.stock <= 0"
            @change="(v: number | undefined) => onQuantity(it, v ?? 1)"
          />
          <div class="subtotal">¥ {{ num(it.subtotal) }}</div>
          <el-button link type="danger" @click="onRemove(it)">删除</el-button>
        </div>

        <div class="cart-foot">
          <span class="tip">满 ¥200 包邮，否则运费 ¥10</span>
          <div class="total-box">
            <span>合计：<b class="total">¥ {{ num(totalAmount) }}</b></span>
            <el-button type="danger" size="large" :disabled="selectedCount === 0" @click="goCheckout">
              去结算（{{ selectedCount }}）
            </el-button>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCart, removeCartItem, updateCartItem, type CartItem } from '@/api/cart'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const items = ref<CartItem[]>([])
const placeholder = '/products/placeholder.svg'

const selectedItems = computed(() => items.value.filter((it) => it.selected === 1 && it.stock > 0))
const selectedCount = computed(() => selectedItems.value.reduce((s, it) => s + it.quantity, 0))
const totalAmount = computed(() => selectedItems.value.reduce((s, it) => s + Number(it.subtotal), 0))
const allSelected = computed(
  () => items.value.length > 0 && items.value.every((it) => it.stock <= 0 || it.selected === 1),
)
const indeterminate = computed(
  () => items.value.some((it) => it.selected === 1) && !allSelected.value,
)

function num(n: number | string | undefined): string {
  return Number(n ?? 0).toFixed(2)
}

async function load() {
  loading.value = true
  try {
    items.value = await getCart()
  } finally {
    loading.value = false
  }
}

async function onSelect(it: CartItem, checked: boolean) {
  await updateCartItem(it.cartId, undefined, checked ? 1 : 0)
  it.selected = checked ? 1 : 0
}

async function toggleAll(checked: string | number | boolean) {
  const v = checked ? 1 : 0
  await Promise.all(
    items.value.filter((it) => it.stock > 0).map((it) => updateCartItem(it.cartId, undefined, v)),
  )
  await load()
}

async function onQuantity(it: CartItem, quantity: number) {
  await updateCartItem(it.cartId, quantity)
  await load()
  cartStore.fetchCount()
}

async function onRemove(it: CartItem) {
  await ElMessageBox.confirm(`确定删除「${it.productName}」吗？`, '提示', { type: 'warning' })
  await removeCartItem(it.cartId)
  ElMessage.success('已删除')
  await load()
  cartStore.fetchCount()
}

function goDetail(productId: number) {
  router.push(`/products/${productId}`)
}

function goCheckout() {
  router.push('/checkout')
}

onMounted(load)
</script>

<style scoped>
.cart-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 8px;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid #f2f3f5;
}
.cart-item.off {
  opacity: 0.6;
}
.thumb {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  background: #f5f7fa;
}
.meta {
  flex: 1;
}
.name {
  cursor: pointer;
  font-weight: 600;
}
.name:hover {
  color: #409eff;
}
.spec {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
.out {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
}
.price,
.subtotal {
  width: 90px;
  text-align: right;
}
.subtotal {
  color: #f56c6c;
  font-weight: 600;
}
.cart-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}
.tip {
  color: #909399;
  font-size: 13px;
}
.total-box {
  display: flex;
  align-items: center;
  gap: 16px;
}
.total {
  color: #f56c6c;
  font-size: 22px;
}
</style>