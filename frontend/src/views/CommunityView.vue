<template>
  <div class="page-container community">
    <!-- 发布框 -->
    <div class="publish">
      <template v-if="userStore.isLogin">
        <el-avatar :size="36" :src="userStore.userInfo?.avatar || undefined">{{ '宠' }}</el-avatar>
        <div class="publish-main">
          <el-input
            v-model="draft"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="今天你家毛孩子有什么开心事？晒出来～"
          />
          <div class="publish-actions">
            <div class="upload-box">
              <ImageUploader v-model="images" :limit="9" />
            </div>
            <el-button type="primary" :loading="publishing" @click="onPublish">发 布</el-button>
          </div>
        </div>
      </template>
      <div v-else class="publish-login">
        <span>登录后晒出你的毛孩子日常</span>
        <el-button type="primary" round @click="router.push('/login')">去登录</el-button>
      </div>
    </div>

    <!-- 最新 / 热门 -->
    <el-tabs v-model="sort" @tab-change="reload">
      <el-tab-pane label="最新" name="latest" />
      <el-tab-pane label="热门" name="hot" />
    </el-tabs>

    <div v-loading="loading">
      <div>
        <PostCard v-for="p in list" :key="p.postId" :post="p" @deleted="reload" />
      </div>
      <el-empty v-if="!loading && !list.length" description="还没有动态，快来发第一条" :image-size="80" />
      <div class="more" v-if="total > list.length">
        <el-button :loading="loading" @click="loadMore">加载更多（{{ list.length }}/{{ total }}）</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PostCard from '@/components/PostCard.vue'
import ImageUploader from '@/components/ImageUploader.vue'
import { createPost, getCommunityPosts, type PostVO } from '@/api/community'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const sort = ref<'latest' | 'hot'>('latest')
const list = ref<PostVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)

const draft = ref('')
const images = ref<string[]>([])
const publishing = ref(false)

async function load(reset = true) {
  loading.value = true
  try {
    if (reset) pageNum.value = 1
    const res = await getCommunityPosts(sort.value, pageNum.value, pageSize)
    list.value = reset ? res.list : list.value.concat(res.list)
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reload() {
  load(true)
}
function loadMore() {
  pageNum.value += 1
  load(false)
}

async function onPublish() {
  if (!draft.value.trim() && !images.value.length) {
    ElMessage.warning('写点什么或传张图吧')
    return
  }
  publishing.value = true
  try {
    await createPost({ content: draft.value.trim(), images: images.value })
    ElMessage.success('发布成功')
    draft.value = ''
    images.value = []
    reload()
  } finally {
    publishing.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.community {
  max-width: 820px;
}
.publish {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 14px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 14px;
}
.publish :deep(.el-textarea) {
  flex: 1;
}
.publish-main {
  flex: 1;
}
.publish-actions {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-top: 8px;
}
.upload-box {
  flex: 1;
  min-width: 0;
}
.publish-login {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #909399;
  padding: 8px 4px;
}
.more {
  text-align: center;
  margin: 12px 0;
}
</style>