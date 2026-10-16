import { http } from './http'

export interface UploadResult {
  url: string
  size: number
}

/** 本地上传图片，返回可访问 URL */
export function uploadImage(file: File | Blob) {
  const fd = new FormData()
  fd.append('file', file)
  return http.post<UploadResult>('/upload/image', fd)
}