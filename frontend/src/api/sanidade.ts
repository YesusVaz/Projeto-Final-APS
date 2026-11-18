import { chamar } from './http'
import type { StatusOcorrencia, TipoAlertaSanitario } from './tiposExtras'

export type Vacinacao = {
  id: number
  loteId: number
  insumoId: number
  doses: number
  data: string
  proximaDose: string | null
  codigo: string
  observacao: string | null
}

export type Tratamento = {
  id: number
  ocorrenciaId: number
  insumoId: number
  quantidade: number
  data: string
  codigo: string
  observacao: string | null
}

// tratamentos so vem preenchido na consulta de uma ocorrencia. na lista fica nulo
export type Ocorrencia = {
  id: number
  loteId: number
  descricao: string
  dataAbertura: string
  status: StatusOcorrencia
  dataEncerramento: string | null
  tratamentos: Tratamento[] | null
}

export type AlertaSanitario = {
  tipo: TipoAlertaSanitario
  loteId: number
  codigoLote: string
  descricao: string
  referencia: string
}

export const registrarVacinacao = (
  token: string,
  loteId: number,
  dados: {
    codigoLancamento: string
    insumoId: number
    doses: number
    data?: string
    proximaDose?: string
    observacao?: string
  },
) => chamar<Vacinacao>(`/lotes/${loteId}/vacinacoes`, { metodo: 'POST', token, corpo: dados })

export const listarVacinacoes = (token: string, loteId: number) =>
  chamar<Vacinacao[]>(`/lotes/${loteId}/vacinacoes`, { token })

export const abrirOcorrencia = (token: string, loteId: number, dados: { descricao: string; data?: string }) =>
  chamar<Ocorrencia>(`/lotes/${loteId}/ocorrencias`, { metodo: 'POST', token, corpo: dados })

export const listarOcorrencias = (token: string) => chamar<Ocorrencia[]>('/ocorrencias', { token })

export const consultarOcorrencia = (token: string, id: number) => chamar<Ocorrencia>(`/ocorrencias/${id}`, { token })

export const registrarTratamento = (
  token: string,
  ocorrenciaId: number,
  dados: { codigoLancamento: string; insumoId: number; quantidade: number; data?: string; observacao?: string },
) => chamar<Tratamento>(`/ocorrencias/${ocorrenciaId}/tratamentos`, { metodo: 'POST', token, corpo: dados })

export const encerrarOcorrencia = (token: string, id: number, dados: { data?: string } = {}) =>
  chamar<Ocorrencia>(`/ocorrencias/${id}/encerramento`, { metodo: 'POST', token, corpo: dados })

// gestor e tecnico. sem data, o backend verifica hoje
export const listarAlertasSanitarios = (token: string) => chamar<AlertaSanitario[]>('/alertas-sanitarios', { token })
