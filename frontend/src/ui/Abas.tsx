import type { KeyboardEvent } from 'react'
import { cn } from './cn'

export type OpcaoAba<T extends string> = { id: T; rotulo: string; contador?: number }

type AbasProps<T extends string> = {
  opcoes: OpcaoAba<T>[]
  valor: T
  onMudar: (id: T) => void
  rotulo: string
  className?: string
}

// abas em pilula: troca o que a tela mostra. setas esquerda e direita andam entre elas
export function Abas<T extends string>({ opcoes, valor, onMudar, rotulo, className }: AbasProps<T>) {
  function teclar(e: KeyboardEvent<HTMLDivElement>) {
    const passo = e.key === 'ArrowRight' ? 1 : e.key === 'ArrowLeft' ? -1 : 0
    if (passo === 0) return
    e.preventDefault()
    const i = opcoes.findIndex((o) => o.id === valor)
    const proxima = opcoes[(i + passo + opcoes.length) % opcoes.length]
    onMudar(proxima.id)
    e.currentTarget.querySelector<HTMLElement>(`[data-aba="${proxima.id}"]`)?.focus()
  }

  return (
    <div role="tablist" aria-label={rotulo} onKeyDown={teclar} className={cn('inline-flex gap-0.5 rounded-full bg-trilho p-1', className)}>
      {opcoes.map((o) => {
        const ativa = o.id === valor
        return (
          <button
            key={o.id}
            type="button"
            role="tab"
            data-aba={o.id}
            aria-selected={ativa}
            tabIndex={ativa ? 0 : -1}
            onClick={() => onMudar(o.id)}
            className={cn(
              'inline-flex h-11 items-center md:h-[34px] gap-1.5 rounded-full px-4 text-sm transition-[background-color,color,box-shadow] duration-250',
              ativa ? 'bg-surface font-medium text-ink shadow-[0_1px_3px_rgb(0_0_0/0.1)]' : 'text-ink-2 hover:text-ink',
            )}
          >
            {o.rotulo}
            {o.contador !== undefined ? <span className="font-mono text-xs tabular-nums">{o.contador}</span> : null}
          </button>
        )
      })}
    </div>
  )
}

type FichasProps<T extends string> = {
  opcoes: OpcaoAba<T>[]
  valor: T
  onMudar: (id: T) => void
  rotulo: string
  className?: string
}

// fichas de filtro de lista (Todos, Ativos...). uma escolhida por vez
export function Fichas<T extends string>({ opcoes, valor, onMudar, rotulo, className }: FichasProps<T>) {
  return (
    <div role="group" aria-label={rotulo} className={cn('flex flex-wrap gap-2', className)}>
      {opcoes.map((o) => {
        const ativa = o.id === valor
        return (
          <button
            key={o.id}
            type="button"
            aria-pressed={ativa}
            onClick={() => onMudar(o.id)}
            className={cn(
              'inline-flex h-11 items-center md:h-[34px] gap-1.5 rounded-full border px-3.5 text-[13.5px] whitespace-nowrap transition duration-200 active:scale-[0.97]',
              ativa ? 'border-ink bg-ink text-white' : 'border-line-input bg-surface hover:bg-hover',
            )}
          >
            {o.rotulo}
            {o.contador !== undefined ? <span className="font-mono text-xs tabular-nums">{o.contador}</span> : null}
          </button>
        )
      })}
    </div>
  )
}
