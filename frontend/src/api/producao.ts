import { chamar } from './http'
import type { TipoProducao } from './tiposExtras'

// postura: valor = ovos, indicador = taxa de postura em %.
// pesagem: valor = peso medio em gramas, indicador = GMD em g/dia (nulo na primeira)
export type RegistroProducao = {
  id: number
  loteId: number
  tipo: TipoProducao
  data: string
  valor: number
  vivos: number | null
  indicador: number | null
  unidadeIndicador: string
  codigo: string
}

export const listarProducao = (token: string, loteId: number) =>
  chamar<RegistroProducao[]>(`/lotes/${loteId}/producao`, { token })

export const registrarPostura = (
  token: string,
  loteId: number,
  dados: { codigoLancamento: string; ovos: number; data?: string },
) => chamar<RegistroProducao>(`/lotes/${loteId}/posturas`, { metodo: 'POST', token, corpo: dados })

export const registrarPesagem = (
  token: string,
  loteId: number,
  dados: { codigoLancamento: string; pesoMedioGramas: number; data?: string },
) => chamar<RegistroProducao>(`/lotes/${loteId}/pesagens`, { metodo: 'POST', token, corpo: dados })
