<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="title">商城后台管理</div>
      </template>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码"
            @keyup.enter="onLogin" />
        </el-form-item>
        <el-button type="primary" class="btn" :loading="loading" @click="onLogin">登 录</el-button>
      </el-form>
      <div class="tip">
        默认账号：admin / Admin@123
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function onLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await adminStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/admin/products')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}
.login-card {
  width: 380px;
}
.title {
  font-weight: 700;
  text-align: center;
}
.btn {
  width: 100%;
}
.tip {
  margin-top: 16px;
  color: #909399;
  font-size: 12px;
  text-align: center;
}
</style>