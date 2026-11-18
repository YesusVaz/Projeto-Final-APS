import { chamar } from './http'
import type { CategoriaInsumo } from './tipos'
import type { OrigemMovimento, TipoMovimentoEstoque } from './tiposExtras'

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

export type NovoInsumo = {
  nome: string
  categoria: CategoriaInsumo
  unidade: string
  nivelMinimo: number
}

export type NovoMovimento = {
  codigoLancamento: string
  tipo: TipoMovimentoEstoque
  quantidade: number
  origem: OrigemMovimento
}

export const listarInsumos = (token: string) => chamar<Insumo[]>('/insumos', { token })

// so o gestor consegue ver. os outros perfis recebem 403
export const listarAlertasEstoque = (token: string) => chamar<Insumo[]>('/insumos/alertas', { token })

export const cadastrarInsumo = (token: string, novo: NovoInsumo) =>
  chamar<Insumo>('/insumos', { metodo: 'POST', token, corpo: novo })

export const movimentarInsumo = (token: string, id: number, movimento: NovoMovimento) =>
  chamar<Insumo>(`/insumos/${id}/movimentos`, { metodo: 'POST', token, corpo: movimento })
