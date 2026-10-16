<template>
  <div class="image-uploader">
    <el-upload
      v-model:file-list="fileList"
      list-type="picture-card"
      :limit="limit"
      :http-request="doUpload"
      :before-upload="beforeUpload"
      :on-exceed="onExceed"
      :on-remove="onChange"
      accept="image/*"
      multiple
    >
      <el-icon><Plus /></el-icon>
    </el-upload>
    <div class="hint">最多 {{ limit }} 张，单张 ≤ {{ maxMb }}MB（jpg/png/webp/gif）</div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, type UploadFile, type UploadRequestOptions } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'

const props = withDefaults(
  defineProps<{
    modelValue?: string[]
    limit?: number
    /** 自定义上传实现（默认走通用 /upload/image，可指定如商品图片目录） */
    uploadFn?: (file: File) => Promise<{ url: string }>
  }>(),
  { modelValue: () => [], limit: 9 },
)
const emit = defineEmits<{ (e: 'update:modelValue', value: string[]): void }>()

const maxMb = 5

const fileList = ref<UploadFile[]>([])

// 与 modelValue 同步：外部传入已有图片时渲染出来；外部清空时同步清空
watch(
  () => props.modelValue,
  (val) => {
    const urls = val ?? []
    const current = fileList.value.map((f) => f.url).filter(Boolean) as string[]
    if (urls.length === 0) {
      fileList.value = []
    } else if (JSON.stringify(urls) !== JSON.stringify(current)) {
      fileList.value = urls.map((u, i) => ({ name: `image-${i}`, url: u }) as UploadFile)
    }
  },
  { immediate: true },
)

function sync() {
  const urls = fileList.value
    .map((f) => (f.url || (f.response as { url?: string } | null)?.url) as string | undefined)
    .filter((u): u is string => !!u)
  emit('update:modelValue', urls)
}

function onChange() {
  sync()
}

function beforeUpload(file: File) {
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('仅支持图片文件')
    return false
  }
  if (file.size > maxMb * 1024 * 1024) {
    ElMessage.warning(`单张图片不能超过 ${maxMb}MB`)
    return false
  }
  return true
}

async function doUpload(options: UploadRequestOptions) {
  try {
    const fn = props.uploadFn ?? uploadImage
    const res = await fn(options.file as File)
    ;(options.file as unknown as { url?: string }).url = res.url
    options.onSuccess(res)
  } catch (e) {
    options.onError(e as never)
  } finally {
    sync()
  }
}

function onExceed() {
  ElMessage.warning(`最多上传 ${props.limit} 张图片`)
}
</script>

<style scoped>
.hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>