import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截：附加 token（后台请求用 adminToken，其余用商城用户 token，避免混用导致 403）
instance.interceptors.request.use((config) => {
  const isAdmin = (config.url ?? '').startsWith('/admin')
  const token = isAdmin
    ? localStorage.getItem('adminToken')
    : localStorage.getItem('token') || localStorage.getItem('adminToken')
  if (token) {
    config.headers['satoken'] = token
  }
  return config
})

// 响应拦截：统一解包 Result
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const res = response.data
    if (res.code === 200) {
      // 解包，运行时返回 data；类型靠下方 http 包装还原
      return res.data as unknown as AxiosResponse
    }
    if (res.code === 401) {
      const from = window.location.pathname
      if (from.startsWith('/admin')) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminName')
        router.push({ name: 'AdminLogin' })
      } else {
        localStorage.removeItem('token')
        router.push({ name: 'Login' })
      }
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  (error) => {
    const msg = (error as { response?: { data?: { message?: string } } })?.response?.data?.message
    ElMessage.error(msg || '网络异常')
    return Promise.reject(error)
  },
)

/** 类型化请求方法：返回后端 Result.data */
export const http = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.get(url, config) as Promise<T>
  },
  post<T = unknown, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig): Promise<T> {
    return instance.post(url, data, config) as Promise<T>
  },
  put<T = unknown, D = unknown>(url: string, data?: D, config?: AxiosRequestConfig): Promise<T> {
    return instance.put(url, data, config) as Promise<T>
  },
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.delete(url, config) as Promise<T>
  },
}