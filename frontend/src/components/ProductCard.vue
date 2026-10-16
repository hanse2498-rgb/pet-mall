<template>
  <div class="product-card" @click="goDetail">
    <div class="cover">
      <img :src="product.coverImg || placeholder" :alt="product.name" loading="lazy" />
      <span v-if="product.isNearExpiry === 1" class="badge">临期 · 7折</span>
    </div>
    <div class="info">
      <div class="name">{{ product.name }}</div>
      <div class="subtitle" v-if="product.subtitle">{{ product.subtitle }}</div>
      <div class="footer">
        <div class="price">
          <span class="yen">¥</span>
          <span class="amount">{{ formatPrice(product.minPrice) }}</span>
        </div>
        <div class="meta">
          <span v-if="product.brandName" class="brand">{{ product.brandName }}</span>
          <span class="sales">已售 {{ product.sales }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { ProductCard } from '@/api/product'

const props = defineProps<{ product: ProductCard }>()

const placeholder = '/products/placeholder.svg'

const router = useRouter()

function goDetail() {
  router.push(`/products/${props.product.id}`)
}

function formatPrice(price: number | null | undefined): string {
  if (price === null || price === undefined) return '--'
  return Number(price).toFixed(2)
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(31, 35, 41, 0.04);
  box-shadow: var(--card-shadow);
  transition: transform 0.28s ease, box-shadow 0.28s ease;
  font-size: 12px;
  display: flex;
  flex-direction: column;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--card-shadow-hover);
  border-color: transparent;
}
.cover {
  position: relative;
  padding-top: 100%;
  background: linear-gradient(160deg, #fbeae2, #f2f4f8);
  overflow: hidden;
}
.cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease;
}
.product-card:hover .cover img {
  transform: scale(1.06);
}
.badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #f0412f;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 999px;
  box-shadow: 0 4px 10px rgba(240, 65, 47, 0.28);
}
.info {
  padding: 10px 12px 12px;
}
.name {
  font-size: 14px;
  font-weight: 600;
  color: #20242a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.subtitle {
  font-size: 12px;
  color: #8a919b;
  margin-top: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 8px;
}
.price .yen {
  color: #f0412f;
  font-size: 12px;
  font-weight: 600;
}
.price .amount {
  color: #f0412f;
  font-size: 18px;
  font-weight: 800;
}
.meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  font-size: 11px;
  color: #9aa1ab;
}
.brand {
  background: #fff4ef;
  color: #ff7a45;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
}
</style>