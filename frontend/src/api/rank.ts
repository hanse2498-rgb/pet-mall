import { http } from './http'

export interface RankItem {
  rank: number
  productId: number
  name: string
  coverImg?: string
  price: number | null
  sales7d: number
}

export interface CategoryRank {
  categoryId: number
  categoryName: string
  items: RankItem[]
}

/** 各分类近 7 天销量 Top3 */
export function getRankTop() {
  return http.get<CategoryRank[]>('/rank/top')
}
