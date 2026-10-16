import { http } from './http'

export interface PetBreed {
  id: number
  type: number
  name: string
  enName?: string
  coverImg?: string
  intro?: string
  description?: string
  habits?: string
  appearance?: string
  careTips?: string
  size?: string
  hair?: string
  characterTrait?: string
  origin?: string
  lifeSpan?: string
  difficulty: number
  sort: number
  status: number
}

export interface PetBreedDetail extends PetBreed {
  recommendProducts: import('./product').ProductCard[]
}

/** 品种列表（type: 1猫 2狗） */
export function getBreeds(type?: number) {
  return http.get<PetBreed[]>('/pet/breed', { params: { type } })
}

/** 品种详情（含推荐商品） */
export function getBreed(id: number) {
  return http.get<PetBreedDetail>(`/pet/breed/${id}`)
}
