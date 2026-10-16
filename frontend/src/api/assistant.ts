import { http } from './http'

export interface ChatMsg {
  id: number
  role: 'user' | 'assistant'
  content: string
  createTime: string
}

export function getChatHistory() {
  return http.get<ChatMsg[]>('/assistant/history')
}

export function clearChatHistory() {
  return http.delete<void>('/assistant/history')
}

/**
 * SSE 流式问答：每收到一段内容回调 onChunk，到 [DONE] 结束。
 * 传入 signal 可中断（用于"停止生成"）。
 */
export async function streamChat(
  question: string,
  onChunk: (c: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  const token = localStorage.getItem('token') || ''
  const url = `/api/assistant/chat?question=${encodeURIComponent(question)}`
  const res = await fetch(url, { headers: { satoken: token }, signal })
  if (!res.ok || !res.body) throw new Error('请求失败')
  const reader = res.body.getReader()
  const decoder = new TextDecoder()
  let buf = ''
  let dataLines: string[] = []

  // 按 SSE 事件解析：一个事件由多行 data: 组成，以空行结束，多行用 \n 连接（保留换行）
  const flush = (): boolean => {
    if (!dataLines.length) return false
    const payload = dataLines.join('\n')
    dataLines = []
    if (payload === '[DONE]') return true
    if (payload) onChunk(payload)
    return false
  }

  for (;;) {
    const { done, value } = await reader.read()
    if (done) break
    buf += decoder.decode(value, { stream: true })
    let idx: number
    while ((idx = buf.indexOf('\n')) >= 0) {
      const line = buf.slice(0, idx).replace(/\r$/, '')
      buf = buf.slice(idx + 1)
      if (line === '') {
        if (flush()) return
      } else if (line.startsWith('data:')) {
        dataLines.push(line.slice(5).replace(/^ /, ''))
      }
    }
  }
  flush()
}
