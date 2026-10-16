import { onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { sendSmsCode } from '@/api/auth'

/**
 * 发送短信验证码：60 秒倒计时；开发环境若接口返回 devCode 会自动填入
 */
export function useSmsCode() {
  const sending = ref(false)
  const countdown = ref(0)
  const devCode = ref('')
  let timer: number | undefined

  async function send(phone: string) {
    if (countdown.value > 0) return
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      ElMessage.warning('请输入正确的手机号')
      return false
    }
    sending.value = true
    try {
      const res = await sendSmsCode(phone)
      if (res.devCode) {
        devCode.value = res.devCode
        ElMessage.success(`验证码已发送（演示验证码：${res.devCode}）`)
      } else {
        ElMessage.success('验证码已发送')
      }
      countdown.value = 60
      timer = window.setInterval(() => {
        countdown.value -= 1
        if (countdown.value <= 0 && timer) clearInterval(timer)
      }, 1000)
      return res.devCode
    } finally {
      sending.value = false
    }
  }

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  return { sending, countdown, devCode, send }
}