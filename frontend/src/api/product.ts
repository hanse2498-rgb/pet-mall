import { http } from './http'

export interface CategoryNode {
  id: number
  name: string
  sort: number
  children: CategoryNode[]
}

export interface Brand {
  id: number
  name: string
  logo?: string
  description?: string
  status: number
  sort: number
}

export interface ProductCard {
  id: number
  name: string
  subtitle?: string
  coverImg?: string
  saleType: number
  isNearExpiry: number
  sales: number
  minPrice: number | null
  brandName?: string
  categoryName?: string
}

export interface Sku {
  id: number
  productId: number
  specName?: string
  price: number
  originalPrice?: number
  isDiscount: number
  discountPrice?: number
  stock: number
  status: number
}

export interface ProductDetail {
  id: number
  name: string
  subtitle?: string
  coverImg?: string
  images: string[]
  detailHtml?: string
  saleType: number
  isNearExpiry: number
  shelfStatus: number
  sales: number
  brandName?: string
  categoryName?: string
  skus: Sku[]
}

export interface PageResult<T> {
  total: number
  list: T[]
}

export interface ProductSuggest {
  id: number
  name: string
  coverImg?: string
  minPrice: number | null
  brandName?: string
}

export interface SuggestResult {
  total: number
  items: ProductSuggest[]
}

export interface ProductListParams {
  keyword?: string
  categoryId?: number
  brandId?: number
  priceMin?: number
  priceMax?: number
  saleType?: number
  petType?: number
  isNearExpiry?: number
  sort?: 'default' | 'sales' | 'price_asc' | 'price_desc' | 'new'
  pageNum: number
  pageSize: number
}

export function getCategories() {
  return http.get<CategoryNode[]>('/product/categories')
}

export function getBrands() {
  return http.get<Brand[]>('/product/brands')
}

export function getProductList(params: ProductListParams) {
  return http.get<PageResult<ProductCard>>('/product/list', { params })
}

/** 搜索联想（在售商品） */
export function suggestProducts(q: string) {
  return http.get<SuggestResult>('/product/suggest', { params: { q } })
}

export function getNearExpiryProducts(params: { pageNum: number; pageSize: number }) {
  return http.get<PageResult<ProductCard>>('/product/near-expiry', { params })
}

export interface SkuInfo {
  skuId: number | string
  productId: number
  productName: string
  coverImg?: string
  specName?: string
  price: number
  originalPrice?: number
  isDiscount: number
  stock: number
  isNearExpiry: number
  shelfStatus: number
}

export function getProductDetail(id: number) {
  return http.get<ProductDetail>(`/product/${id}`)
}

export function getSkuInfo(skuId: number | string) {
  return http.get<SkuInfo>(`/product/sku/${skuId}`)
}