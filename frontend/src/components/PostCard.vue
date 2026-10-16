<template>
  <div class="post-card">
    <div class="post-head" @click="goDetail">
      <el-avatar :size="36" :src="post.avatar || undefined">{{ nickChar }}</el-avatar>
      <div class="head-meta">
        <span class="nick">{{ post.nickname }}</span>
        <span class="time">{{ fmt(post.createTime) }}</span>
      </div>
      <el-button v-if="isMine" link type="danger" size="small" style="margin-left: auto" @click.stop="onDelete">
        删除
      </el-button>
    </div>

    <div v-if="post.content" class="post-content" @click="goDetail">{{ post.content }}</div>

    <div v-if="post.images?.length" class="post-imgs" :class="`imgs-${Math.min(post.images.length, 3)}`" @click="goDetail">
      <img v-for="(img, i) in post.images.slice(0, 9)" :key="i" :src="img" loading="lazy" />
    </div>

    <div class="post-foot">
      <span class="action" :class="{ active: post.liked }" @click="onLike">
        <el-icon><Star /></el-icon> 赞 {{ post.likeCount || 0 }}
      </span>
      <span class="action" @click="goDetail">
        <el-icon><ChatDotRound /></el-icon> 评论 {{ post.commentCount || 0 }}
      </span>
      <span class="action read">
        <el-icon><View /></el-icon> {{ post.viewCount || 0 }} 浏览
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deletePost, togglePostLike, type PostVO } from '@/api/community'
import { useUserStore } from '@/stores/user'

const props = defineProps<{ post: PostVO }>()
const emit = defineEmits<{ (e: 'deleted'): void }>()

const router = useRouter()
const userStore = useUserStore()

const nickChar = computed(() => (props.post.nickname || '宠')[0])
const isMine = computed(() => userStore.userInfo?.id === props.post.userId)

function fmt(t: string): string {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

function goDetail() {
  router.push(`/community/${props.post.postId}`)
}

async function ensureLogin(): Promise<boolean> {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
    return false
  }
  return true
}

async function onLike() {
  if (!(await ensureLogin())) return
  const res = await togglePostLike(props.post.postId)
  props.post.liked = res.liked
  props.post.likeCount = res.likeCount
}

async function onDelete() {
  await ElMessageBox.confirm('确定删除这条动态吗？', '提示', { type: 'warning' })
  await deletePost(props.post.postId)
  ElMessage.success('已删除')
  emit('deleted')
}
</script>

<style scoped>
.post-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 14px;
}
.post-head {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.head-meta {
  display: flex;
  flex-direction: column;
}
.nick {
  font-weight: 600;
  font-size: 14px;
}
.time {
  color: #909399;
  font-size: 12px;
}
.post-content {
  margin: 10px 0;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  cursor: pointer;
}
.post-imgs {
  display: grid;
  gap: 6px;
  margin-bottom: 6px;
  cursor: pointer;
}
.imgs-1 {
  grid-template-columns: 1fr;
  max-width: 420px;
}
.imgs-2 {
  grid-template-columns: repeat(2, 1fr);
}
.imgs-3 {
  grid-template-columns: repeat(3, 1fr);
}
.post-imgs img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
}
.post-foot {
  display: flex;
  gap: 24px;
  color: #606266;
  font-size: 13px;
  border-top: 1px solid #f5f7fa;
  padding-top: 8px;
  margin-top: 4px;
}
.action {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
.action.active {
  color: #f56c6c;
}
.action.read {
  cursor: default;
  color: #909399;
}
</style>