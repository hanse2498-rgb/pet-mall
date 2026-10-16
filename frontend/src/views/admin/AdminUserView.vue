<template>
  <div class="admin-user">
    <el-card>
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索手机号或昵称" clearable style="width: 220px"
          @keyup.enter="search" />
        <el-select v-model="query.status" placeholder="账号状态" clearable style="width: 130px" @change="search">
          <el-option label="正常" :value="1" />
          <el-option label="已禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="200" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="160" />
        <el-table-column label="账号状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="最近登录" width="170">
          <template #default="{ row }">{{ fmt(row.lastLoginTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="danger" @click="toggle(row, 0)">禁用</el-button>
            <el-button v-else link type="success" @click="toggle(row, 1)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="query.pageSize"
          :current-page="query.pageNum" @current-change="onPage" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminUserStatus, adminUsers, type AdminUser } from '@/api/admin'

const loading = ref(false)
const list = ref<AdminUser[]>([])
const total = ref(0)
const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
})

function fmt(t?: string): string {
  return t ? t.replace('T', ' ').slice(0, 19) : '-'
}

async function load() {
  loading.value = true
  try {
    const res = await adminUsers(query)
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}
function onPage(p: number) {
  query.pageNum = p
  load()
}

async function toggle(row: AdminUser, status: number) {
  const tip = status === 1 ? '确定启用该用户？' : `禁用后将无法登录，确定禁用「${row.nickname || row.phone}」？`
  await ElMessageBox.confirm(tip, '提示', { type: 'warning' })
  await adminUserStatus(row.id, status)
  row.status = status
  ElMessage.success(status === 1 ? '已启用' : '已禁用')
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
/* 分页框悬浮吸底：始终吸附在窗口最底部 */
.pager {
  position: fixed;
  left: 200px; /* 侧栏宽度 */
  right: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  justify-content: center;
  padding: 10px 24px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}
/* 底部留出分页条高度，避免遮挡最后一行 */
.admin-user {
  padding-bottom: 60px;
}
</style>