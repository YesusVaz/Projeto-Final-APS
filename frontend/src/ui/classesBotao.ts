import { cn } from './cn'

export type VarianteBotao = 'primario' | 'acao' | 'contorno' | 'fantasma' | 'perigo'
export type TamanhoBotao = 'sm' | 'md' | 'lg' | 'xl' | 'icone'

const BASE =
  'inline-flex shrink-0 items-center justify-center gap-2 rounded-full border border-transparent font-medium whitespace-nowrap select-none transition duration-200 active:scale-[0.97] disabled:pointer-events-none disabled:opacity-45'

const VARIANTES: Record<VarianteBotao, string> = {
  primario: 'bg-ink text-paper hover:bg-ink-hover',
  acao: 'bg-accent text-white hover:bg-accent-hover',
  contorno: 'border-line-input bg-surface hover:bg-hover',
  fantasma: 'text-ink-hover hover:bg-trilho',
  perigo: 'bg-perigo text-white hover:bg-perigo-hover',
}

const TAMANHOS: Record<TamanhoBotao, string> = {
  sm: 'h-8 px-3.5 text-[13px]',
  md: 'h-10 px-[18px] text-sm',
  lg: 'h-[52px] px-6 text-[15px]',
  xl: 'h-14 px-7 text-base',
  icone: 'size-10',
}

// as classes ficam separadas do componente pra um link (<a>) poder ter cara de botao
export function classesBotao(variante: VarianteBotao = 'primario', tamanho: TamanhoBotao = 'md', extra?: string) {
  return cn(BASE, VARIANTES[variante], TAMANHOS[tamanho], extra)
}
