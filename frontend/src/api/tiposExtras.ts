// tipos e rotulos das telas do lote 3. mesma regra do tipos.ts: enum chega em MAIUSCULO e fica assim

export type TipoMovimentoEstoque = 'ENTRADA' | 'SAIDA'
export type OrigemMovimento = 'COMPRA' | 'ALIMENTACAO' | 'VACINACAO' | 'TRATAMENTO' | 'USO_GERAL'
export type CriterioAlerta = 'DIARIO' | 'ACUMULADO'
export type StatusOcorrencia = 'ABERTA' | 'ENCERRADA'
export type TipoProducao = 'POSTURA' | 'PESAGEM'
export type TipoAlertaSanitario = 'VACINA_PROXIMA' | 'VACINA_ATRASADA' | 'OCORRENCIA_PROLONGADA'

export const ROTULO_CATEGORIA_INSUMO = {
  RACAO: 'Ração',
  VACINA: 'Vacina',
  MEDICAMENTO: 'Medicamento',
  MATERIAL: 'Material',
} as const

export const ROTULO_ORIGEM: Record<OrigemMovimento, string> = {
  COMPRA: 'Compra',
  ALIMENTACAO: 'Alimentação',
  VACINACAO: 'Vacinação',
  TRATAMENTO: 'Tratamento',
  USO_GERAL: 'Uso geral',
}

export const ROTULO_CRITERIO: Record<CriterioAlerta, string> = {
  DIARIO: 'Por dia',
  ACUMULADO: 'Acumulado no lote',
}

export const ROTULO_STATUS_OCORRENCIA: Record<StatusOcorrencia, string> = {
  ABERTA: 'Aberta',
  ENCERRADA: 'Encerrada',
}

export const ROTULO_ALERTA_SANITARIO: Record<TipoAlertaSanitario, string> = {
  VACINA_PROXIMA: 'Vacina próxima',
  VACINA_ATRASADA: 'Vacina atrasada',
  OCORRENCIA_PROLONGADA: 'Ocorrência aberta há muito tempo',
}
