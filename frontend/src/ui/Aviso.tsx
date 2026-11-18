import type { ReactNode } from 'react'
import { cn } from './cn'
import { Icone, type NomeIcone } from './Icone'

export type TomAviso = 'info' | 'atencao' | 'critico' | 'ok'

const TONS: Record<TomAviso, { cor: string; icone: NomeIcone }> = {
  info: { cor: 'bg-info-bg text-info', icone: 'info' },
  atencao: { cor: 'bg-atencao-bg text-atencao-texto', icone: 'aviso' },
  critico: { cor: 'bg-critico-bg text-critico-texto', icone: 'aviso' },
  ok: { cor: 'bg-ok-bg text-ok', icone: 'ok' },
}

type AvisoProps = {
  tom?: TomAviso
  // frase curta em negrito, antes do texto
  destaque?: string
  icone?: NomeIcone
  className?: string
  children?: ReactNode
}

// aviso dentro da tela. atencao e critico sao anunciados na hora pelo leitor de tela
export function Aviso({ tom = 'info', destaque, icone, className, children }: AvisoProps) {
  const { cor, icone: padrao } = TONS[tom]
  return (
    <div
      role={tom === 'atencao' || tom === 'critico' ? 'alert' : 'status'}
      className={cn('flex items-start gap-3 rounded-2xl px-4 py-3.5 text-sm leading-[1.45]', cor, className)}
    >
      <Icone nome={icone ?? padrao} tamanho={18} className="mt-0.5 shrink-0" />
      <p>
        {destaque ? <b className="font-medium">{destaque} </b> : null}
        {children}
      </p>
    </div>
  )
}
