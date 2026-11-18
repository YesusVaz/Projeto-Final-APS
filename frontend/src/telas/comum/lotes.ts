import type { AlertaMortalidade, Lote } from '../../api/lotes'
import type { NivelAlerta } from '../../api/tipos'
import type { NivelSelo, TomProgresso } from '../../ui'

// o selo usa o vocabulario em minusculo do design. os enums da API chegam em maiusculo
export const NIVEL_SELO: Record<NivelAlerta, NivelSelo> = {
  NORMAL: 'normal',
  ATENCAO: 'atencao',
  CRITICO: 'critico',
}

export function porcentagemSaldo(lote: Pick<Lote, 'saldo' | 'quantidadeInicial'>): number {
  return lote.quantidadeInicial > 0 ? (lote.saldo / lote.quantidadeInicial) * 100 : 0
}

export const TOM_PROGRESSO: Record<NivelAlerta, TomProgresso> = {
  NORMAL: 'normal',
  ATENCAO: 'atencao',
  CRITICO: 'critico',
}

const PESO: Record<NivelAlerta, number> = { NORMAL: 0, ATENCAO: 1, CRITICO: 2 }

// pior nivel de cada lote entre os alertas abertos. lote sem alerta fica NORMAL
export function nivelPorLote(alertas: AlertaMortalidade[]): Map<number, NivelAlerta> {
  const mapa = new Map<number, NivelAlerta>()
  for (const a of alertas) {
    const atual = mapa.get(a.loteId)
    if (!atual || PESO[a.nivel] > PESO[atual]) mapa.set(a.loteId, a.nivel)
  }
  return mapa
}
