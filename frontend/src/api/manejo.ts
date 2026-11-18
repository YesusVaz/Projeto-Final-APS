import { chamar } from './http'
import type { Especie } from './tipos'

export type TipoHigienizacao = 'LIMPEZA_GALPAO' | 'DESINFECCAO' | 'TROCA_DE_CAMA' | 'LIMPEZA_BEBEDOUROS_COMEDOUROS'

export const ROTULO_HIGIENIZACAO: Record<TipoHigienizacao, string> = {
  LIMPEZA_GALPAO: 'Limpeza do galpão',
  DESINFECCAO: 'Desinfecção',
  TROCA_DE_CAMA: 'Troca de cama',
  LIMPEZA_BEBEDOUROS_COMEDOUROS: 'Limpeza de bebedouros e comedouros',
}

export type NovaHigienizacao = {
  codigoLancamento: string
  tipo: TipoHigienizacao
  data: string
  observacao?: string
}

export type NovoPrograma = {
  codigoLancamento: string
  horasDeLuz: number
  intensidadeLux?: number
  dataInicio: string
  observacao?: string
}

export type ProgramaIluminacao = {
  id: number
  loteId: number
  horasDeLuz: number
  intensidadeLux: number | null
  dataInicio: string
  observacao: string | null
  codigo: string
}

export const registrarHigienizacao = (token: string, loteId: number, corpo: NovaHigienizacao) =>
  chamar<unknown>(`/lotes/${loteId}/higienizacoes`, { metodo: 'POST', token, corpo })

export const registrarPrograma = (token: string, loteId: number, corpo: NovoPrograma) =>
  chamar<ProgramaIluminacao>(`/lotes/${loteId}/iluminacao`, { metodo: 'POST', token, corpo })

export const listarProgramas = (token: string, loteId: number) => chamar<ProgramaIluminacao[]>(`/lotes/${loteId}/iluminacao`, { token })

// campos nulos = ainda nao ha dados pra calcular
export type Produtividade = {
  loteId: number
  codigoLote: string
  especie: Especie
  totalEntrado: number
  saldo: number
  mortes: number
  taxaDeMortalidade: number
  racaoConsumidaKg: number
  ganhoDePesoKg: number | null
  gmdMedio: number | null
  conversaoAlimentar: number | null
  ovosTotal: number | null
  taxaMediaDePostura: number | null
  racaoPorDuzia: number | null
}

// so o gestor consegue ver
export const consultarProdutividade = (token: string, loteId: number) => chamar<Produtividade>(`/lotes/${loteId}/produtividade`, { token })
