import { http } from './http'

export interface PageResult<T> {
  total: number
  list: T[]
}

export interface PostVO {
  postId: number
  userId: number
  nickname: string
  avatar?: string
  content?: string
  images: string[]
  likeCount: number
  commentCount: number
  viewCount: number
  liked: boolean
  createTime: string
}

export interface PostCommentVO {
  commentId: number
  userId: number
  nickname: string
  content: string
  createTime: string
}

export interface PostDetail extends PostVO {
  comments: PostCommentVO[]
}

export function getCommunityPosts(sort: 'latest' | 'hot', pageNum = 1, pageSize = 10) {
  return http.get<PageResult<PostVO>>('/community/posts', { params: { sort, pageNum, pageSize } })
}

export function getPostDetail(id: number | string) {
  return http.get<PostDetail>(`/community/posts/${id}`)
}

export function getPostComments(id: number) {
  return http.get<PostCommentVO[]>(`/community/posts/${id}/comments`)
}

export function createPost(data: { content?: string; images?: string[] }) {
  return http.post<PostVO>('/community/post', data)
}

export function deletePost(id: number) {
  return http.delete<void>(`/community/post/${id}`)
}

export function togglePostLike(id: number) {
  return http.post<{ liked: boolean; likeCount: number }>(`/community/post/${id}/like`)
}

export function addPostComment(id: number, content: string) {
  return http.post<PostCommentVO>(`/community/post/${id}/comment`, { content })
}

export function deletePostComment(id: number) {
  return http.delete<void>(`/community/comment/${id}`)
}