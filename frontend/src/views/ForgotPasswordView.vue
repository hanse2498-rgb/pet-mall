<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header>
        <div class="auth-header">
          <span class="logo">🔑 找回密码</span>
          <span class="sub">通过手机验证码重置密码</span>
        </div>
      </template>

      <el-form label-position="top" @submit.prevent>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入注册手机号" maxlength="11" clearable />
        </el-form-item>

        <el-form-item label="验证码">
          <div class="code-row">
            <el-input v-model="form.code" placeholder="6位验证码" maxlength="6" />
            <el-button :disabled="countdown > 0" :loading="sending" @click="onSendCode">
              {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码">
          <el-input v-model="form.password" type="password" show-password
            placeholder="8-20位，需包含字母和数字" />
        </el-form-item>

        <el-form-item label="确认新密码">
          <el-input v-model="form.confirm" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="onReset">
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <div class="foot">
        想起密码了？
        <el-link type="primary" :underline="false" @click="router.push('/login')">去登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { forgotPasswordApi } from '@/api/auth'
import { useSmsCode } from '@/composables/useSmsCode'

const router = useRouter()
const loading = ref(false)
const form = reactive({ phone: '', code: '', password: '', confirm: '' })
const { sending, countdown, send } = useSmsCode()

async function onSendCode() {
  const code = await send(form.phone)
  if (code) form.code = code
}

async function onReset() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return ElMessage.warning('请输入正确的手机号')
  if (!/^\d{6}$/.test(form.code)) return ElMessage.warning('请输入6位验证码')
  if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&_.]{8,20}$/.test(form.password))
    return ElMessage.warning('密码需8-20位，且包含字母和数字')
  if (form.password !== form.confirm) return ElMessage.warning('两次输入的密码不一致')

  loading.value = true
  try {
    await forgotPasswordApi(form.phone, form.code, form.password)
    ElMessage.success('密码重置成功，请重新登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding: 80px 16px;
}
.auth-card {
  width: 420px;
}
.auth-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.auth-header .logo {
  font-size: 20px;
  font-weight: 700;
}
.auth-header .sub {
  color: #909399;
  font-size: 13px;
}
.code-row {
  display: flex;
  width: 100%;
  gap: 8px;
}
.submit-btn {
  width: 100%;
}
.foot {
  text-align: center;
  color: #606266;
  font-size: 13px;
}
</style>