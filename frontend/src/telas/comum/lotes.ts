import type { Lote } from '../../api/lotes'
import type { NivelAlerta } from '../../api/tipos'
import type { NivelSelo } from '../../ui'

// o selo usa o vocabulario em minusculo do design. os enums da API chegam em maiusculo
export const NIVEL_SELO: Record<NivelAlerta, NivelSelo> = {
  NORMAL: 'normal',
  ATENCAO: 'atencao',
  CRITICO: 'critico',
}

export function porcentagemSaldo(lote: Pick<Lote, 'saldo' | 'quantidadeInicial'>): number {
  return lote.quantidadeInicial > 0 ? (lote.saldo / lote.quantidadeInicial) * 100 : 0
}
