<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header>
        <div class="auth-header">
          <span class="logo">宠物商城</span>
          <span class="sub">欢迎回来</span>
        </div>
      </template>

      <el-tabs v-model="tab">
        <!-- 验证码登录 -->
        <el-tab-pane label="验证码登录" name="code">
          <el-form label-position="top" @submit.prevent>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" clearable
                @keyup.enter="onCodeLogin" />
            </el-form-item>
            <el-form-item label="验证码">
              <div class="code-row">
                <el-input v-model="form.code" placeholder="6位验证码" maxlength="6" @keyup.enter="onCodeLogin" />
                <el-button :disabled="countdown > 0" :loading="sending" @click="onSendCode">
                  {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="submit-btn" :loading="loading" @click="onCodeLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 密码登录 -->
        <el-tab-pane label="密码登录" name="password">
          <el-form label-position="top" @submit.prevent>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" clearable
                @keyup.enter="onPasswordLogin" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码"
                @keyup.enter="onPasswordLogin" />
            </el-form-item>
            <div class="row-between">
              <el-link type="primary" @click="router.push('/forgot')">忘记密码？</el-link>
            </div>
            <el-form-item>
              <el-button type="primary" class="submit-btn" :loading="loading" @click="onPasswordLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="foot">
        还没有账号？
        <el-link type="primary" :underline="false" @click="router.push('/register')">立即注册</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useSmsCode } from '@/composables/useSmsCode'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const tab = ref('code')
const loading = ref(false)
const form = reactive({ phone: '', code: '', password: '' })

const { sending, countdown, devCode, send } = useSmsCode()

async function onSendCode() {
  const code = await send(form.phone)
  if (code) {
    form.code = code // 开发环境自动填入演示验证码
  }
}

function redirectAfterLogin() {
  cartStore.fetchCount()
  router.push((route.query.redirect as string) || '/')
}

async function onCodeLogin() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return ElMessage.warning('请输入正确的手机号')
  if (!/^\d{6}$/.test(form.code)) return ElMessage.warning('请输入6位验证码')
  loading.value = true
  try {
    await userStore.loginByCode(form.phone, form.code)
    ElMessage.success('登录成功')
    redirectAfterLogin()
  } finally {
    loading.value = false
  }
}

async function onPasswordLogin() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return ElMessage.warning('请输入正确的手机号')
  if (!form.password) return ElMessage.warning('请输入密码')
  loading.value = true
  try {
    await userStore.loginByPassword(form.phone, form.password)
    ElMessage.success('登录成功')
    redirectAfterLogin()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding: 60px 16px;
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
.row-between {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
.foot {
  text-align: center;
  color: #606266;
  font-size: 13px;
}
</style>