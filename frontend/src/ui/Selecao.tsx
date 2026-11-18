import type { SelectHTMLAttributes } from 'react'
import { CLASSE_CONTROLE } from './Campo'
import { cn } from './cn'
import { Icone } from './Icone'

// select nativo (abre bem no celular) com a seta desenhada por cima
export function Selecao({ className, children, ...resto }: SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <div className="relative">
      <select className={cn(CLASSE_CONTROLE, 'h-11 appearance-none pr-10 pl-3.5', className)} {...resto}>
        {children}
      </select>
      <Icone nome="baixo" tamanho={16} className="pointer-events-none absolute top-1/2 right-3.5 -mt-2 text-ink-3" />
    </div>
  )
}
