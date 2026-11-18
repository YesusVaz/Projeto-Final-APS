// escala do eixo vertical: teto "redondo" e marcas igualmente espacadas, sempre comecando no zero
export function escala(maximo: number, marcas = 4): { teto: number; ticks: number[] } {
  if (maximo <= 0) return { teto: marcas, ticks: Array.from({ length: marcas + 1 }, (_, i) => i) }
  // passo sempre inteiro (1, 2, 5, 10, 20, 50...): o que se conta aqui e ave, quilo, ovo e real
  const bruto = Math.max(1, maximo / marcas)
  const potencia = 10 ** Math.floor(Math.log10(bruto))
  const fracao = bruto / potencia
  const passo = (fracao <= 1 ? 1 : fracao <= 2 ? 2 : fracao <= 5 ? 5 : 10) * potencia
  const n = Math.ceil(maximo / passo)
  return { teto: n * passo, ticks: Array.from({ length: n + 1 }, (_, i) => i * passo) }
}

const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']

// "2026-11" vira "nov/26"
export function mesCurto(anoMes: string): string {
  const [ano, mes] = anoMes.split('-')
  return `${MESES[Number(mes) - 1] ?? mes}/${ano.slice(2)}`
}

// numero curto pro eixo: 12500 vira "12,5 mil"
export function compacto(v: number): string {
  if (Math.abs(v) >= 1_000_000) return `${(v / 1_000_000).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} mi`
  if (Math.abs(v) >= 10_000) return `${(v / 1000).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} mil`
  return v.toLocaleString('pt-BR', { maximumFractionDigits: 1 })
}
