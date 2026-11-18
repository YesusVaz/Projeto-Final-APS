import { chamar } from './http'
import type { CategoriaInsumo } from './tipos'

export type Insumo = {
  id: number
  nome: string
  categoria: CategoriaInsumo
  unidade: string
  nivelMinimo: number
  saldo: number
  ativo: boolean
  abaixoDoMinimo: boolean
}

export const listarInsumos = (token: string) => chamar<Insumo[]>('/insumos', { token })
