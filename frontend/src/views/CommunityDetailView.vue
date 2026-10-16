<template>
  <div class="page-container community" v-loading="loading">
    <el-button link @click="router.back()">‹ 返回社区</el-button>

    <PostCard v-if="detail" :post="detail" @deleted="backAfterDelete" />

    <!-- 评论 -->
    <div class="comments" v-if="detail">
      <div class="comments-title">评论区（{{ detail.comments.length }}）</div>

      <div class="comment-input">
        <el-input v-model="commentText" maxlength="200" placeholder="友善评论，一起吸宠～" @keyup.enter="onComment" />
        <el-button type="primary" :loading="commenting" @click="onComment">评论</el-button>
      </div>

      <div v-if="detail.comments.length" class="comment-list">
        <div v-for="c in detail.comments" :key="c.commentId" class="comment-item">
          <el-avatar :size="28">{{ (c.nickname || '宠')[0] }}</el-avatar>
          <div class="comment-body">
            <div class="comment-meta">
              <b>{{ c.nickname }}</b>
              <span class="gray">{{ fmt(c.createTime) }}</span>
              <el-button v-if="c.userId === userStore.userInfo?.id" link type="danger" size="small"
                @click="onDeleteComment(c.commentId)">删除</el-button>
            </div>
            <div>{{ c.content }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else description="还没有评论，抢个沙发" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PostCard from '@/components/PostCard.vue'
import { addPostComment, deletePostComment, getPostDetail, type PostDetail } from '@/api/community'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref<PostDetail | null>(null)
const commentText = ref('')
const commenting = ref(false)

function fmt(t: string): string {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

async function load() {
  loading.value = true
  try {
    detail.value = await getPostDetail(route.params.id as string)
  } finally {
    loading.value = false
  }
}

function backAfterDelete() {
  ElMessage.success('已删除')
  router.push('/community')
}

async function onComment() {
  if (!detail.value) return
  if (!commentText.value.trim()) return ElMessage.warning('评论不能为空')
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  commenting.value = true
  try {
    const c = await addPostComment(detail.value.postId, commentText.value.trim())
    detail.value.comments.push(c)
    detail.value.commentCount += 1
    commentText.value = ''
    ElMessage.success('评论成功')
  } finally {
    commenting.value = false
  }
}

async function onDeleteComment(commentId: number) {
  await deletePostComment(commentId)
  if (detail.value) {
    detail.value.comments = detail.value.comments.filter((c) => c.commentId !== commentId)
    detail.value.commentCount = Math.max(0, detail.value.commentCount - 1)
  }
  ElMessage.success('评论已删除')
}

onMounted(load)
</script>

<style scoped>
.community {
  max-width: 820px;
}
.comments {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 14px;
}
.comments-title {
  font-weight: 700;
  margin-bottom: 12px;
}
.comment-input {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.comment-list .comment-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f5f7fa;
}
.comment-body {
  flex: 1;
}
.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.gray {
  color: #909399;
  font-size: 12px;
}
</style>