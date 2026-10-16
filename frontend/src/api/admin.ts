import { http } from './http'
import type { PageResult, OrderVO } from './order'
import type { Brand } from './product'

// ---------- 后台认证 ----------
export interface AdminLoginResult {
  token: string
  adminId: number
  nickname: string
}

export function adminLogin(username: string, password: string) {
  return http.post<AdminLoginResult>('/admin/auth/login', { username, password })
}

export function adminLogout() {
  return http.post<void>('/admin/auth/logout')
}

export interface AdminInfo {
  id: number
  username: string
  nickname: string
}

export function adminMe() {
  return http.get<AdminInfo>('/admin/auth/me')
}

// ---------- 商品 ----------
export interface AdminProduct {
  id: number
  brandId?: number
  categoryId?: number
  name: string
  subtitle?: string
  coverImg?: string
  images?: string | string[]
  saleType: number
  petType: number
  isNearExpiry: number
  shelfStatus: number
  sales: number
  createTime: string
}

export interface ProductForm {
  id?: number
  brandId?: number
  categoryId?: number
  name: string
  subtitle?: string
  coverImg?: string
  images?: string[]
  detailHtml?: string
  saleType?: number
  petType?: number
  shelfStatus?: number
}

export function adminProductPage(params: {
  keyword?: string
  categoryId?: number
  shelfStatus?: number
  brandId?: number
  pageNum: number
  pageSize: number
}) {
  return http.get<PageResult<AdminProduct>>('/admin/product', { params })
}

/** 上传商品图片 → 自动保存到「商品图片文件夹」，返回可访问 URL（/products/...） */
export function uploadProductImage(file: File, productId?: number) {
  const fd = new FormData()
  fd.append('file', file)
  const query = productId ? `?productId=${productId}` : ''
  return http.post<{ url: string }>(`/admin/product/image${query}`, fd)
}

/** 上传宠物（品种）图案 → 自动保存到「宠物图片文件夹」，返回可访问 URL（/breeds/...） */
export function uploadBreedImage(file: File, breedId?: number) {
  const fd = new FormData()
  fd.append('file', file)
  const query = breedId ? `?breedId=${breedId}` : ''
  return http.post<{ url: string }>(`/admin/pet/breed/image${query}`, fd)
}

export function adminProductCreate(form: ProductForm) {
  return http.post<AdminProduct>('/admin/product', form)
}

export function adminProductUpdate(form: ProductForm) {
  return http.put<AdminProduct>('/admin/product', form)
}

export function adminProductShelf(id: number, shelfStatus: number) {
  return http.put<void>(`/admin/product/${id}/shelf`, { shelfStatus })
}

export function adminProductDelete(id: number) {
  return http.delete<void>(`/admin/product/${id}`)
}

export function adminProductSkus(productId: number) {
  return http.get<SkuRow[]>(`/admin/product/${productId}/skus`)
}

export interface SkuRow {
  id: number
  productId: number
  specName?: string
  price: number
  originalPrice?: number
  stock: number
  status: number
  isDiscount: number
  discountPrice?: number
}

export function adminSkuAdd(form: Partial<SkuRow>) {
  return http.post<SkuRow>('/admin/product/sku', form)
}

export function adminSkuUpdate(form: Partial<SkuRow>) {
  return http.put<SkuRow>('/admin/product/sku', form)
}

export interface SkuBatch {
  id: number
  skuId: number
  batchNo?: string
  produceDate?: string
  expiryDays?: number
  remainRatio?: number
  isNearExpiry: number
}

export function adminSkuBatches(skuId: number) {
  return http.get<SkuBatch[]>(`/admin/product/sku/${skuId}/batches`)
}

export function adminBatchAdd(form: {
  skuId: number
  batchNo?: string
  produceDate: string
  expiryDays: number
}) {
  return http.post<SkuBatch>('/admin/product/batch', form)
}

// ---------- 订单 ----------
export function adminOrders(status?: number, pageNum = 1, pageSize = 10) {
  return http.get<PageResult<OrderVO>>('/admin/order', { params: { status, pageNum, pageSize } })
}

export function adminOrderDetail(id: number) {
  return http.get<OrderVO>(`/admin/order/${id}`)
}

export function adminOrderShip(id: number, logisticsCompany: string, trackingNo: string) {
  return http.post<void>(`/admin/order/${id}/ship`, { logisticsCompany, trackingNo })
}

// ---------- 售后管理 ----------
export function adminAfterSales(status: number | undefined, pageNum = 1, pageSize = 10) {
  return http.get<PageResult<import('./afterSale').AfterSaleVO>>('/admin/after-sale', {
    params: { status, pageNum, pageSize },
  })
}

export function adminAfterSaleAudit(id: number, pass: boolean, remark?: string) {
  return http.post<void>(`/admin/after-sale/${id}/audit`, { pass, remark })
}

// ---------- 用户管理 ----------
export interface AdminUser {
  id: number
  phone?: string
  nickname?: string
  status: number
  createTime?: string
  lastLoginTime?: string
}

export function adminUsers(params: {
  keyword?: string
  status?: number
  pageNum: number
  pageSize: number
}) {
  return http.get<PageResult<AdminUser>>('/admin/user', { params })
}

export function adminUserStatus(id: number, status: number) {
  return http.put<void>(`/admin/user/${id}/status`, { status })
}

// ---------- 秒杀管理 ----------
export interface SeckillActivityRow {
  id: number
  name: string
  startTime: string
  endTime: string
  status: number
}

export interface SeckillItemRow {
  id: number
  activityId: number
  skuId: number
  seckillPrice: number
  seckillStock: number
  seckillSold: number
  perUserLimit: number
  status: number
  sort?: number
}

export function adminSeckillActivities() {
  return http.get<SeckillActivityRow[]>('/admin/seckill/activity')
}

export function adminSeckillCreateActivity(data: { name: string; startTime: string; endTime: string }) {
  return http.post<SeckillActivityRow>('/admin/seckill/activity', data)
}

export function adminSeckillUpdateActivity(id: number, data: Partial<SeckillActivityRow>) {
  return http.put<SeckillActivityRow>(`/admin/seckill/activity/${id}`, data)
}

export function adminSeckillDeleteActivity(id: number) {
  return http.delete<void>(`/admin/seckill/activity/${id}`)
}

export function adminSeckillItems(activityId: number) {
  return http.get<SeckillItemRow[]>(`/admin/seckill/activity/${activityId}/items`)
}

export function adminSeckillAddItem(activityId: number, data: Partial<SeckillItemRow>) {
  return http.post<SeckillItemRow>(`/admin/seckill/activity/${activityId}/item`, data)
}

export function adminSeckillUpdateItem(id: number, data: Partial<SeckillItemRow>) {
  return http.put<SeckillItemRow>(`/admin/seckill/item/${id}`, data)
}

export function adminSeckillDeleteItem(id: number) {
  return http.delete<void>(`/admin/seckill/item/${id}`)
}

// ---------- 宠物档案 ----------
export function adminPetBreeds(params: {
  type?: number
  keyword?: string
  pageNum: number
  pageSize: number
}) {
  return http.get<PageResult<import('./petBreed').PetBreed>>('/admin/pet/breed', { params })
}

export function adminPetBreedCreate(data: Partial<import('./petBreed').PetBreed>) {
  return http.post<import('./petBreed').PetBreed>('/admin/pet/breed', data)
}

export function adminPetBreedUpdate(data: Partial<import('./petBreed').PetBreed>) {
  return http.put<import('./petBreed').PetBreed>('/admin/pet/breed', data)
}

export function adminPetBreedStatus(id: number, status: number) {
  return http.put<void>(`/admin/pet/breed/${id}/status`, { status })
}

export function adminPetBreedDelete(id: number) {
  return http.delete<void>(`/admin/pet/breed/${id}`)
}

export interface BreedRecommendRow {
  id: number
  breedId: number
  productId: number
  type: number
  sort: number
}

export function adminPetBreedRecommends(breedId: number) {
  return http.get<BreedRecommendRow[]>(`/admin/pet/breed/${breedId}/recommend`)
}

export function adminPetBreedSaveRecommends(breedId: number, items: { productId: number; type: number }[]) {
  return http.put<void>(`/admin/pet/breed/${breedId}/recommend`, items)
}

// ---------- 品牌 / 分类 ----------
export function adminBrandList() {
  return http.get<Brand[]>('/admin/brand')
}

export function adminBrandCreate(data: Partial<Brand>) {
  return http.post<Brand>('/admin/brand', data)
}

export function adminBrandUpdate(data: Partial<Brand>) {
  return http.put<Brand>('/admin/brand', data)
}

export function adminBrandDelete(id: number) {
  return http.delete<void>(`/admin/brand/${id}`)
}

export interface AdminCategory {
  id: number
  parentId?: number
  name: string
  sort?: number
  status?: number
}

export function adminCategoryList() {
  return http.get<AdminCategory[]>('/admin/category')
}

export function adminCategoryCreate(data: Partial<AdminCategory>) {
  return http.post<AdminCategory>('/admin/category', data)
}

export function adminCategoryUpdate(data: Partial<AdminCategory>) {
  return http.put<AdminCategory>('/admin/category', data)
}

export function adminCategoryDelete(id: number) {
  return http.delete<void>(`/admin/category/${id}`)
}