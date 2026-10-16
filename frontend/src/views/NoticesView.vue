<template>
  <div class="page-container notices-page">
    <el-card>
      <template #header>
        <div class="head">
          <span>消息中心</span>
          <el-button link type="primary" @click="onReadAll">全部已读</el-button>
        </div>
      </template>

      <el-tabs v-model="activeType" @tab-change="onTab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="订单" name="1" />
        <el-tab-pane label="售后" name="2" />
      </el-tabs>

      <div v-loading="loading">
        <el-empty v-if="!loading && !list.length" description="暂无消息" :image-size="70" />
        <div
          v-for="n in list"
          :key="n.id"
          class="notice-item"
          :class="{ unread: n.isRead === 0 }"
          @click="openItem(n)"
        >
          <div class="ni-top">
            <span class="ni-title">{{ n.title }}</span>
            <span class="ni-time">{{ fmt(n.createTime) }}</span>
          </div>
          <div class="ni-content">{{ n.content }}</div>
        </div>

        <div v-if="total > pageSize" class="pager">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="pageNum"
            @current-change="onPage"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNotices, readNotice, readAllNotices, type Notice } from '@/api/notice'
import { useNoticeStore } from '@/stores/notice'

const router = useRouter()
const noticeStore = useNoticeStore()

const activeType = ref('all')
const list = ref<Notice[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)

function typeParam(): number | undefined {
  if (activeType.value === 'all') return undefined
  return Number(activeType.value)
}

function fmt(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

async function load() {
  loading.value = true
  try {
    const res = await getNotices(typeParam(), pageNum.value, pageSize)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onTab() {
  pageNum.value = 1
  load()
}
function onPage(p: number) {
  pageNum.value = p
  load()
}

async function openItem(n: Notice) {
  if (n.isRead === 0) {
    await readNotice(n.id)
    n.isRead = 1
    noticeStore.fetchCount()
  }
  if (n.bizId) {
    router.push(`/orders/${n.bizId}`)
  }
}

async function onReadAll() {
  await readAllNotices()
  list.value.forEach((n) => (n.isRead = 1))
  noticeStore.setCount(0)
}

onMounted(load)
</script>

<style scoped>
.notices-page {
  max-width: 820px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.notice-item {
  padding: 12px 4px;
  border-bottom: 1px solid #f2f3f5;
  cursor: pointer;
}
.notice-item:hover {
  background: #faf7f5;
}
.notice-item.unread .ni-title::before {
  content: '';
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f0412f;
  margin-right: 7px;
  vertical-align: middle;
}
.ni-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
}
.ni-title {
  font-size: 14px;
  font-weight: 600;
  color: #20242a;
}
.ni-time {
  font-size: 12px;
  color: #9aa1ab;
  flex-shrink: 0;
}
.ni-content {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
  line-height: 1.6;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
