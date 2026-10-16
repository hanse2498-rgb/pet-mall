<template>
  <div class="dashboard" v-loading="loading">
    <!-- 顶部：时间范围 -->
    <div class="head">
      <span class="title">数据看板</span>
      <el-radio-group v-model="range" size="small" @change="load">
        <el-radio-button :value="1">今日</el-radio-button>
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
      </el-radio-group>
    </div>

    <!-- KPI 卡片 -->
    <div class="kpi-row">
      <div class="kpi-card">
        <div class="kpi-label">销售额</div>
        <div class="kpi-value red">¥{{ money(kpi.salesAmount) }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">退款金额</div>
        <div class="kpi-value gray">¥{{ money(kpi.refundAmount) }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">订单数</div>
        <div class="kpi-value">{{ kpi.orderCount ?? 0 }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">客单价</div>
        <div class="kpi-value">¥{{ money(kpi.avgPrice) }}</div>
      </div>
      <div class="kpi-card">
        <div class="kpi-label">新增用户</div>
        <div class="kpi-value">{{ kpi.newUsers ?? 0 }}</div>
      </div>
      <div class="kpi-card link" @click="router.push('/admin/orders')">
        <div class="kpi-label">待发货</div>
        <div class="kpi-value orange">{{ kpi.pendingShip ?? 0 }}</div>
      </div>
      <div class="kpi-card link" @click="router.push('/admin/after-sale')">
        <div class="kpi-label">待审核售后</div>
        <div class="kpi-value orange">{{ kpi.pendingAfterSale ?? 0 }}</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <el-card class="chart-card wide" shadow="never">
        <template #header>销售趋势（{{ rangeLabel }}）</template>
        <div ref="trendRef" class="chart"></div>
      </el-card>
      <el-card class="chart-card" shadow="never">
        <template #header>订单状态分布</template>
        <div ref="statusRef" class="chart"></div>
      </el-card>
    </div>

    <div class="chart-row">
      <el-card class="chart-card" shadow="never">
        <template #header>分类销售占比</template>
        <div ref="categoryRef" class="chart"></div>
      </el-card>
      <el-card class="chart-card wide" shadow="never">
        <template #header>热销商品 Top10</template>
        <el-table :data="data?.topProducts || []" size="small" height="300">
          <el-table-column type="index" label="排名" width="70" />
          <el-table-column prop="name" label="商品" min-width="180" show-overflow-tooltip />
          <el-table-column prop="qty" label="销量" width="90" />
          <el-table-column label="销售额" width="120">
            <template #default="{ row }">¥{{ money(row.amount) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <div class="chart-row">
      <el-card class="chart-card wide" shadow="never">
        <template #header>最新订单</template>
        <el-table :data="data?.latestOrders || []" size="small">
          <el-table-column prop="orderNo" label="订单号" width="200" show-overflow-tooltip />
          <el-table-column label="金额" width="110">
            <template #default="{ row }">¥{{ money(row.payAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="OrderStatusTagType[row.status]" size="small" effect="dark">
                {{ OrderStatusMap[row.status] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下单时间" width="170">
            <template #default="{ row }">{{ fmt(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
      <el-card class="chart-card" shadow="never">
        <template #header>库存预警（&lt; 10）</template>
        <el-empty v-if="!(data?.stockAlerts || []).length" description="暂无预警" :image-size="60" />
        <div v-for="(s, i) in data?.stockAlerts || []" :key="i" class="alert-item">
          <span class="alert-name">{{ s.productName }}</span>
          <span class="alert-spec">{{ s.spec }}</span>
          <span class="alert-stock">{{ s.stock }}</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { adminDashboard, type Dashboard } from '@/api/dashboard'
import { OrderStatusMap, OrderStatusTagType } from '@/api/order'

const router = useRouter()
const loading = ref(false)
const range = ref(30)
const data = ref<Dashboard | null>(null)
const kpi = ref<Record<string, number>>({})

const trendRef = ref<HTMLElement>()
const statusRef = ref<HTMLElement>()
const categoryRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null

const rangeLabel = ref('近 30 天')

function money(n: number | null | undefined): string {
  return Number(n ?? 0).toFixed(2)
}
function fmt(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 19) : '—'
}

// 生成完整日期轴（补零）
function buildAxis(rows: { date: string; salesAmount: number; orderCount: number }[]) {
  const map = new Map(rows.map((r) => [r.date, r]))
  const labels: string[] = []
  const sales: number[] = []
  const orders: number[] = []
  for (let i = range.value - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const key = d.toISOString().slice(0, 10)
    labels.push(key.slice(5))
    const r = map.get(key)
    sales.push(Number(r?.salesAmount ?? 0))
    orders.push(Number(r?.orderCount ?? 0))
  }
  return { labels, sales, orders }
}

function renderTrend() {
  if (!trendRef.value) return
  trendChart = trendChart || echarts.init(trendRef.value)
  const { labels, sales, orders } = buildAxis(data.value?.trend || [])
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单量'], top: 0 },
    grid: { left: 60, right: 60, top: 50, bottom: 30 },
    xAxis: { type: 'category', data: labels, boundaryGap: false },
    yAxis: [
      { type: 'value', name: '销售额' },
      { type: 'value', name: '订单量' },
    ],
    series: [
      { name: '销售额', type: 'line', smooth: true, areaStyle: { opacity: 0.15 }, data: sales, itemStyle: { color: '#ff6a3d' } },
      { name: '订单量', type: 'line', smooth: true, yAxisIndex: 1, data: orders, itemStyle: { color: '#409eff' } },
    ],
  })
}

function renderStatus() {
  if (!statusRef.value) return
  statusChart = statusChart || echarts.init(statusRef.value)
  const rows = (data.value?.statusDist || []).map((r) => ({
    name: OrderStatusMap[r.status] || String(r.status),
    value: r.count,
  }))
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['45%', '68%'],
        avoidLabelOverlap: true,
        label: { show: false },
        data: rows,
      },
    ],
  })
}

function renderCategory() {
  if (!categoryRef.value) return
  categoryChart = categoryChart || echarts.init(categoryRef.value)
  const rows = (data.value?.categoryShare || []).map((r) => ({ name: r.categoryName, value: Number(r.amount) }))
  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: '62%',
        center: ['50%', '45%'],
        data: rows.length ? rows : [{ name: '暂无数据', value: 1, itemStyle: { color: '#eee' } }],
        label: { show: false },
      },
    ],
  })
}

function resize() {
  trendChart?.resize()
  statusChart?.resize()
  categoryChart?.resize()
}

async function load() {
  rangeLabel.value = range.value === 1 ? '今日' : `近 ${range.value} 天`
  loading.value = true
  try {
    data.value = await adminDashboard(range.value)
    kpi.value = (data.value.kpi || {}) as unknown as Record<string, number>
    await nextTick()
    renderTrend()
    renderStatus()
    renderCategory()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  window.addEventListener('resize', resize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  trendChart?.dispose()
  statusChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 4px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.head .title {
  font-size: 18px;
  font-weight: 700;
}
.kpi-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.kpi-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 2px 10px rgba(31, 35, 41, 0.05);
}
.kpi-card.link {
  cursor: pointer;
}
.kpi-card.link:hover {
  box-shadow: 0 8px 22px rgba(255, 106, 61, 0.16);
}
.kpi-label {
  font-size: 13px;
  color: #8a919b;
}
.kpi-value {
  font-size: 22px;
  font-weight: 800;
  margin-top: 6px;
  color: #20242a;
}
.kpi-value.red {
  color: #f0412f;
}
.kpi-value.orange {
  color: #ff8a50;
}
.kpi-value.gray {
  color: #8a919b;
}
.chart-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
.chart-card {
  flex: 1;
  min-width: 0;
}
.chart-card.wide {
  flex: 2;
}
.chart {
  height: 300px;
}
.alert-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  border-bottom: 1px solid #f5f2ef;
  font-size: 13px;
}
.alert-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.alert-spec {
  color: #9aa1ab;
  font-size: 12px;
}
.alert-stock {
  color: #f0412f;
  font-weight: 700;
}
@media (max-width: 1200px) {
  .kpi-row {
    grid-template-columns: repeat(4, 1fr);
  }
  .chart-row {
    flex-direction: column;
  }
}
</style>
