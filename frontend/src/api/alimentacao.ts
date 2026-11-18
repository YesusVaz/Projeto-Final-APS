import { chamar } from './http'

export type RegistroAlimentacao = {
  id: number | null
  loteId: number
  insumoId: number
  quantidadeKg: number
  data: string
  codigo: string
}

export const registrarAlimentacao = (
  token: string,
  loteId: number,
  dados: { codigoLancamento: string; insumoId: number; quantidadeKg: number; data?: string },
) => chamar<RegistroAlimentacao>(`/lotes/${loteId}/alimentacoes`, { metodo: 'POST', token, corpo: dados })
