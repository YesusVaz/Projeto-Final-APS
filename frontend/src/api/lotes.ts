import { chamar } from './http'
import type {
  CategoriaHistorico,
  Especie,
  MotivoSaida,
  NivelAlerta,
  StatusLote,
  TipoMovimentoLote,
} from './tipos'

export type Lote = {
  id: number
  codigo: string
  especie: Especie
  quantidadeInicial: number
  saldo: number
  status: StatusLote
}

export type MovimentoLote = {
  id: number | null
  tipo: TipoMovimentoLote
  quantidade: number
  codigo: string
  dataHora: string
  observacao: string | null
}

export type LoteDetalhe = Lote & {
  consistente: boolean
  historico: MovimentoLote[]
}

export type IdadeLote = {
  loteId: number
  codigo: string
  dataAlojamento: string
  idadeInicialDias: number
  data: string
  idadeDias: number
  semanas: number
}

export type ItemHistorico = {
  data: string
  hora: string | null
  categoria: CategoriaHistorico
  descricao: string
}

export type HistoricoLote = {
  lote: Lote
  consistente: boolean
  itens: ItemHistorico[]
}

export type NovoLote = {
  codigoLancamento: string
  especie: Especie
  quantidadeInicial: number
  dataAlojamento?: string
  idadeInicialDias?: number
}

export type RegistroMortalidade = Lote & { nivelAlerta: NivelAlerta }

export type AlertaMortalidade = {
  loteId: number
  codigoLote: string
  nivel: NivelAlerta
  dataHora: string
}

export const listarLotes = (token: string) => chamar<Lote[]>('/lotes', { token })

export const consultarLote = (token: string, id: number) => chamar<LoteDetalhe>(`/lotes/${id}`, { token })

export const idadeDoLote = (token: string, id: number) => chamar<IdadeLote>(`/lotes/${id}/idade`, { token })

export const historicoDoLote = (token: string, id: number) =>
  chamar<HistoricoLote>(`/lotes/${id}/historico`, { token })

export const cadastrarLote = (token: string, novo: NovoLote) =>
  chamar<Lote>('/lotes', { metodo: 'POST', token, corpo: novo })

type DadosSaida = { codigoLancamento: string; motivo: MotivoSaida }

export const encerrarLote = (token: string, id: number, dados: DadosSaida) =>
  chamar<Lote>(`/lotes/${id}/encerramento`, { metodo: 'POST', token, corpo: dados })

export const registrarBaixa = (
  token: string,
  id: number,
  dados: DadosSaida & { quantidade: number; observacao?: string },
) => chamar<Lote>(`/lotes/${id}/baixas`, { metodo: 'POST', token, corpo: dados })

export const registrarMortalidade = (
  token: string,
  id: number,
  dados: { codigoLancamento: string; quantidade: number; observacao?: string },
) => chamar<RegistroMortalidade>(`/lotes/${id}/mortalidades`, { metodo: 'POST', token, corpo: dados })

export const listarAlertasMortalidade = (token: string) =>
  chamar<AlertaMortalidade[]>('/alertas-mortalidade', { token })
