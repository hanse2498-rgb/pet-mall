<template>
  <div class="assistant-page">
    <div class="chat-card">
      <div class="chat-head">
        <div class="title">
          <span>宠物问答助手</span>
          <span class="tag">仅限宠物话题</span>
        </div>
        <el-button link type="danger" @click="onClear">清空</el-button>
      </div>

      <div class="chat-body" ref="bodyRef">
        <div v-if="!msgs.length" class="welcome">
          <div class="w-icon"><el-icon :size="38"><ChatDotRound /></el-icon></div>
          <div class="w-text">你好呀，我是宠物专属小助手！只回答养宠相关的问题，比如换粮、洗澡、驱虫、疫苗、宠物行为等。</div>
        </div>

        <div v-for="(m, i) in msgs" :key="m.key" class="row" :class="m.role">
          <template v-if="m.role === 'assistant'">
            <div class="avatar"><el-icon :size="18"><ChatDotRound /></el-icon></div>
            <div class="bubble-box">
              <div class="bubble markdown" v-html="render(m.content)"></div>
              <span v-if="streaming && i === msgs.length - 1" class="caret"></span>
              <div
                v-if="!streaming && i === msgs.length - 1 && m.content"
                class="msg-ops"
              >
                <el-button link size="small" @click="copy(m.content)">
                  <el-icon><DocumentCopy /></el-icon> 复制
                </el-button>
                <el-button link size="small" @click="regenerate">
                  <el-icon><RefreshRight /></el-icon> 重新生成
                </el-button>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="bubble">{{ m.content }}</div>
          </template>
        </div>
      </div>

      <div class="chat-suggest" v-if="!msgs.length">
        <span v-for="q in suggests" :key="q" class="chip" @click="ask(q)">{{ q }}</span>
      </div>

      <div class="chat-input">
        <el-input
          v-model="input"
          placeholder="问我一个关于宠物的问题…"
          :disabled="streaming"
          @keyup.enter="ask(input)"
        />
        <el-button v-if="streaming" type="danger" plain @click="stop">停止</el-button>
        <el-button v-else type="primary" :disabled="!input.trim()" @click="ask(input)">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { clearChatHistory, getChatHistory, streamChat } from '@/api/assistant'

interface Msg {
  key: number
  role: 'user' | 'assistant'
  content: string
}

const msgs = ref<Msg[]>([])
const input = ref('')
const streaming = ref(false)
const bodyRef = ref<HTMLElement>()
let seq = 0
let controller: AbortController | null = null
const lastQuestion = ref('')
const suggests = [
  '猫咪多久换一次粮？',
  '怎么科学给狗狗驱虫？',
  '多久给猫洗一次澡？',
  '遛狗要注意什么？',
]

function render(md: string): string {
  if (!md) return ''
  try {
    const html = marked.parse(md) as string
    return DOMPurify.sanitize(html)
  } catch {
    return DOMPurify.sanitize(md)
  }
}

function scrollBottom() {
  nextTick(() => {
    const el = bodyRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function push(role: 'user' | 'assistant', content: string): Msg {
  const m = { key: seq++, role, content }
  msgs.value.push(m)
  scrollBottom()
  return m
}

async function ask(text: string, appendUser = true) {
  const q = (text ?? '').trim()
  if (!q || streaming.value) return
  input.value = ''
  lastQuestion.value = q
  if (appendUser) push('user', q)
  const answer = push('assistant', '')
  await stream(answer, q)
}

async function stream(answer: Msg, q: string) {
  streaming.value = true
  controller = new AbortController()
  try {
    await streamChat(
      q,
      (chunk) => {
        answer.content += chunk
        scrollBottom()
      },
      controller.signal,
    )
  } catch (e) {
    const aborted = (e as { name?: string })?.name === 'AbortError'
    if (aborted) {
      if (!answer.content) answer.content = '（已停止生成）'
    } else {
      answer.content = answer.content || '哎呀，回答出小差了，请稍后再试～'
    }
  } finally {
    streaming.value = false
    controller = null
  }
}

function stop() {
  controller?.abort()
}

function regenerate() {
  if (streaming.value) return
  for (let i = msgs.value.length - 1; i >= 0; i--) {
    if (msgs.value[i].role === 'assistant') {
      msgs.value.splice(i, 1)
      break
    }
  }
  if (lastQuestion.value) ask(lastQuestion.value, false)
}

async function copy(text: string) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败')
  }
}

async function onClear() {
  await ElMessageBox.confirm('确定清空所有对话吗？', '提示', { type: 'warning' })
  await clearChatHistory()
  msgs.value = []
}

onMounted(async () => {
  const history = await getChatHistory()
  msgs.value = history.map((h) => ({ key: seq++, role: h.role, content: h.content }))
  scrollBottom()
})

onBeforeUnmount(() => controller?.abort())
</script>

<style scoped>
.assistant-page {
  max-width: 760px;
  margin: 0 auto;
  padding: 16px;
}
.chat-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  min-height: 460px;
}
.chat-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #f0f2f5;
}
.title {
  font-size: 17px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
}
.tag {
  font-size: 12px;
  font-weight: 400;
  color: #f56c6c;
  background: #fef0f0;
  padding: 2px 8px;
  border-radius: 10px;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 18px;
  background: #f7f8fa;
}
.welcome {
  text-align: center;
  color: #909399;
  margin-top: 40px;
}
.w-icon {
  font-size: 46px;
  margin-bottom: 10px;
}
.row {
  display: flex;
  margin-bottom: 14px;
  align-items: flex-start;
}
.row.user {
  justify-content: flex-end;
}
.row.assistant {
  justify-content: flex-start;
  gap: 8px;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #fff3ee;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.bubble-box {
  max-width: 76%;
}
.bubble {
  padding: 10px 14px;
  border-radius: 10px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.row.assistant .bubble {
  background: #fff;
  border: 1px solid #ebeef5;
  color: #303133;
}
.row.user .bubble {
  background: #409eff;
  color: #fff;
}
/* markdown 内容 */
.markdown {
  white-space: normal;
}
.markdown :deep(p) {
  margin: 0 0 6px;
}
.markdown :deep(p:last-child) {
  margin-bottom: 0;
}
.markdown :deep(ul),
.markdown :deep(ol) {
  margin: 4px 0;
  padding-left: 20px;
}
.markdown :deep(li) {
  margin: 2px 0;
}
.markdown :deep(code) {
  background: #f4f1ee;
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 13px;
}
.markdown :deep(pre) {
  background: #f4f1ee;
  padding: 10px;
  border-radius: 8px;
  overflow-x: auto;
}
.caret {
  display: inline-block;
  width: 7px;
  height: 15px;
  background: #ff6a3d;
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink 1s step-start infinite;
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}
.msg-ops {
  margin-top: 4px;
  display: flex;
  gap: 10px;
}
.chat-suggest {
  display: flex;
  gap: 8px;
  padding: 8px 18px;
  flex-wrap: wrap;
  border-top: 1px solid #f0f2f5;
}
.chip {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 14px;
  padding: 4px 12px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
}
.chip:hover {
  color: #409eff;
  border-color: #409eff;
}
.chat-input {
  display: flex;
  gap: 10px;
  padding: 12px 18px 16px;
  border-top: 1px solid #f0f2f5;
}
</style>
