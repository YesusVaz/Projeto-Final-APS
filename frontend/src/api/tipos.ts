// Tipos e rotulos compartilhados com a API. Os enums chegam em MAIUSCULO (nome do enum do backend)
// e ficam assim no front, sem converter. So o perfil e minusculo (veja http.ts).

export type Especie = 'FRANGO_CORTE' | 'POEDEIRA'
export type StatusLote = 'ATIVO' | 'ENCERRADO'
export type NivelAlerta = 'NORMAL' | 'ATENCAO' | 'CRITICO'
export type CategoriaInsumo = 'RACAO' | 'VACINA' | 'MEDICAMENTO' | 'MATERIAL'

// motivo do encerramento e da baixa parcial. sao os mesmos tres valores no backend
export type MotivoSaida = 'VENDA' | 'DESCARTE' | 'TRANSFERENCIA'

export type TipoMovimentoLote =
  | 'ENTRADA_INICIAL'
  | 'MORTALIDADE'
  | 'BAIXA_VENDA'
  | 'BAIXA_DESCARTE'
  | 'BAIXA_TRANSFERENCIA'

export type CategoriaHistorico = 'MOVIMENTO' | 'ALIMENTACAO' | 'VACINACAO' | 'OCORRENCIA' | 'TRATAMENTO' | 'PRODUCAO'

export const ROTULO_ESPECIE: Record<Especie, string> = {
  FRANGO_CORTE: 'Frango de corte',
  POEDEIRA: 'Poedeira',
}

export const ROTULO_STATUS_LOTE: Record<StatusLote, string> = {
  ATIVO: 'Ativo',
  ENCERRADO: 'Encerrado',
}

export const ROTULO_MOTIVO: Record<MotivoSaida, string> = {
  VENDA: 'Venda',
  DESCARTE: 'Descarte',
  TRANSFERENCIA: 'Transferência',
}

export const ROTULO_CATEGORIA_HISTORICO: Record<CategoriaHistorico, string> = {
  MOVIMENTO: 'Movimento',
  ALIMENTACAO: 'Alimentação',
  VACINACAO: 'Vacinação',
  OCORRENCIA: 'Ocorrência',
  TRATAMENTO: 'Tratamento',
  PRODUCAO: 'Produção',
}

export const ROTULO_NIVEL: Record<NivelAlerta, string> = {
  NORMAL: 'Normal',
  ATENCAO: 'Atenção',
  CRITICO: 'Crítico',
}

// chave de idempotencia (RN22). uma por tentativa: so troca depois de um envio com sucesso,
// assim reenviar ou dar duplo clique nao duplica o lancamento
export function novoCodigoLancamento(): string {
  return `LAN-${crypto.randomUUID()}`
}
