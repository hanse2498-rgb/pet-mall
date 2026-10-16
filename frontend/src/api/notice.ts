import { http } from './http'
import type { PageResult } from './order'

export interface Notice {
  id: string
  type: number
  title: string
  content?: string
  bizId?: string
  isRead: number
  createTime: string
}

/** 我的消息 */
export function getNotices(type: number | undefined, pageNum = 1, pageSize = 10) {
  return http.get<PageResult<Notice>>('/notice/list', { params: { type, pageNum, pageSize } })
}

/** 未读数 */
export function getUnreadCount() {
  return http.get<{ count: number }>('/notice/unread-count')
}

/** 单条已读 */
export function readNotice(id: number) {
  return http.post<void>(`/notice/${id}/read`)
}

/** 全部已读 */
export function readAllNotices() {
  return http.post<void>('/notice/read-all')
}
