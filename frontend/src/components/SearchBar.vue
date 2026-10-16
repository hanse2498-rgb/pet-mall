<template>
  <div class="search-bar" ref="rootRef">
    <el-input
      v-model="keyword"
      :placeholder="placeholder"
      clearable
      class="search-input"
      @focus="onFocus"
      @input="onInput"
      @keydown.down.prevent="move(1)"
      @keydown.up.prevent="move(-1)"
      @keydown.enter.prevent="onEnter"
      @keydown.esc="close"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <transition name="fade">
      <div v-show="open" class="dropdown">
        <!-- 空输入：搜索历史 + 热门搜索 -->
        <template v-if="!keyword.trim()">
          <template v-if="history.length">
            <div class="drop-title">
              <span>搜索历史</span>
              <el-icon class="clear-his" title="清空历史" @click="clearHistory"><Delete /></el-icon>
            </div>
            <div class="hot-words">
              <span v-for="h in history" :key="h" class="hot-word" @click="pickWord(h)">{{ h }}</span>
            </div>
          </template>
          <div class="drop-title">热门搜索</div>
          <div class="hot-words">
            <span v-for="w in hotWords" :key="w" class="hot-word" @click="pickWord(w)">{{ w }}</span>
          </div>
        </template>

        <!-- 有输入 -->
        <template v-else>
          <div v-if="loading" class="hint">搜索中…</div>
          <template v-else-if="items.length">
            <div
              v-for="(it, i) in items"
              :key="it.id"
              class="sug-item"
              :class="{ active: i === activeIndex }"
              @mouseenter="activeIndex = i"
              @click="goDetail(it)"
            >
              <img :src="it.coverImg || '/products/placeholder.svg'" loading="lazy" />
              <div class="sug-info">
                <div class="sug-name" v-html="highlight(it.name)"></div>
                <div class="sug-sub">
                  <template v-if="it.brandName">{{ it.brandName }}</template>
                  <template v-if="it.minPrice != null"> · ¥{{ num(it.minPrice) }}</template>
                </div>
              </div>
            </div>
            <div class="sug-all" @click="goSearch">查看全部 {{ total }} 条结果 ›</div>
          </template>
          <div v-else class="hint">没有找到「{{ keyword }}」相关商品</div>
        </template>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { suggestProducts, type ProductSuggest } from '@/api/product'

const router = useRouter()
const route = useRoute()

const placeholder = '搜索猫粮、狗粮、玩具…'
const hotWords = ['猫粮', '犬粮', '猫砂', '罐头', '冻干', '逗猫棒', '驱虫', '猫抓板']

const keyword = ref('')
const open = ref(false)
const loading = ref(false)
const items = ref<ProductSuggest[]>([])
const total = ref(0)
const activeIndex = ref(-1)
const rootRef = ref<HTMLElement>()

// ---- 搜索历史（本地存储）----
const HISTORY_KEY = 'search_history'
const history = ref<string[]>(loadHistory())

function loadHistory(): string[] {
  try {
    const raw = localStorage.getItem(HISTORY_KEY)
    const arr = raw ? JSON.parse(raw) : []
    return Array.isArray(arr) ? arr.slice(0, 8) : []
  } catch {
    return []
  }
}
function saveHistory() {
  try {
    localStorage.setItem(HISTORY_KEY, JSON.stringify(history.value))
  } catch {
    /* 忽略存储异常 */
  }
}
function addHistory(kw: string) {
  const k = kw.trim()
  if (!k) return
  history.value = [k, ...history.value.filter((x) => x !== k)].slice(0, 8)
  saveHistory()
}
function clearHistory() {
  history.value = []
  saveHistory()
}

let debounceTimer: number | undefined
let seq = 0

// 与结果页 URL 双向同步：回填/清空关键词
watch(
  () => route.query.keyword,
  (k) => {
    keyword.value = (k as string) || ''
  },
  { immediate: true },
)

function onFocus() {
  open.value = true
  if (keyword.value.trim()) doSearch()
}

function onInput() {
  open.value = true
  activeIndex.value = -1
  if (debounceTimer) clearTimeout(debounceTimer)
  const kw = keyword.value.trim()
  if (!kw) {
    items.value = []
    total.value = 0
    loading.value = false
    return
  }
  loading.value = true
  debounceTimer = window.setTimeout(doSearch, 250)
}

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  const current = ++seq
  loading.value = true
  try {
    const res = await suggestProducts(kw)
    if (current !== seq) return // 丢弃过期响应
    items.value = res.items
    total.value = res.total
  } catch {
    if (current === seq) {
      items.value = []
      total.value = 0
    }
  } finally {
    if (current === seq) loading.value = false
  }
}

function move(step: number) {
  if (!items.value.length) return
  open.value = true
  let i = activeIndex.value + step
  if (i < 0) i = items.value.length - 1
  if (i >= items.value.length) i = 0
  activeIndex.value = i
}

function onEnter() {
  const it = activeIndex.value >= 0 ? items.value[activeIndex.value] : undefined
  if (it) {
    goDetail(it)
  } else {
    goSearch()
  }
}

function goSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  addHistory(kw)
  close()
  router.push({ path: '/products', query: { keyword: kw } })
}

function goDetail(it: ProductSuggest) {
  addHistory(keyword.value)
  close()
  router.push(`/products/${it.id}`)
}

function pickWord(w: string) {
  keyword.value = w
  onInput()
}

function close() {
  open.value = false
  activeIndex.value = -1
}

function num(n: number | null): string {
  return Number(n ?? 0).toFixed(2)
}

function escapeHtml(s: string): string {
  return s.replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c] as string)
}

function highlight(name: string): string {
  const kw = keyword.value.trim()
  const safe = escapeHtml(name)
  if (!kw) return safe
  const safeKw = escapeHtml(kw).replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  try {
    return safe.replace(new RegExp(`(${safeKw})`, 'gi'), '<em class="hl">$1</em>')
  } catch {
    return safe
  }
}

function onClickOutside(e: MouseEvent) {
  if (rootRef.value && !rootRef.value.contains(e.target as Node)) close()
}

onMounted(() => document.addEventListener('click', onClickOutside))
onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside)
  if (debounceTimer) clearTimeout(debounceTimer)
})
</script>

<style scoped>
.search-bar {
  position: relative;
  width: 100%;
  max-width: 420px;
}
.search-input :deep(.el-input__wrapper) {
  border-radius: 999px;
  background: #f4f1ee;
  box-shadow: none;
}
.search-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}
.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 32px rgba(31, 35, 41, 0.14);
  padding: 8px;
  z-index: 2000;
  max-height: 420px;
  overflow-y: auto;
}
.drop-title {
  font-size: 12px;
  color: #9aa1ab;
  padding: 6px 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.clear-his {
  cursor: pointer;
  color: #9aa1ab;
}
.clear-his:hover {
  color: var(--el-color-primary);
}
.hot-words {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px 8px 8px;
}
.hot-word {
  font-size: 13px;
  color: #4a4f57;
  background: #f4f1ee;
  padding: 4px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s;
}
.hot-word:hover {
  background: #fff4ef;
  color: var(--el-color-primary);
}
.sug-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
}
.sug-item.active,
.sug-item:hover {
  background: #f7f4f0;
}
.sug-item img {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  object-fit: cover;
  flex: 0 0 auto;
}
.sug-info {
  min-width: 0;
}
.sug-name {
  font-size: 13px;
  color: #20242a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sug-name :deep(.hl) {
  color: var(--el-color-primary);
  font-style: normal;
  font-weight: 700;
}
.sug-sub {
  font-size: 12px;
  color: #9aa1ab;
  margin-top: 2px;
}
.sug-all {
  text-align: center;
  font-size: 13px;
  color: var(--el-color-primary);
  padding: 8px;
  margin-top: 4px;
  border-top: 1px solid #f0ece8;
  cursor: pointer;
}
.hint {
  text-align: center;
  color: #9aa1ab;
  font-size: 13px;
  padding: 18px 8px;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>