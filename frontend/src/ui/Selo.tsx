import type { ReactNode } from 'react'
import { cn } from './cn'

export type NivelSelo = 'normal' | 'atencao' | 'critico' | 'info' | 'neutro'

// cada nivel tem cor E forma: circulo, triangulo, quadrado. daltonico e foto preta e branca entendem igual
const NIVEIS: Record<NivelSelo, { cor: string; forma: string }> = {
  normal: { cor: 'bg-ok-bg text-ok', forma: 'size-[7px] rounded-full' },
  atencao: { cor: 'bg-atencao-bg text-atencao', forma: 'h-2 w-[9px] [clip-path:polygon(50%_0,100%_100%,0_100%)]' },
  critico: { cor: 'bg-critico-bg text-critico', forma: 'size-[7px] rounded-[2px]' },
  info: { cor: 'bg-info-bg text-info', forma: 'size-[7px] rounded-full' },
  neutro: { cor: 'bg-trilho text-ink-hover', forma: 'size-[7px] rounded-full' },
}

type SeloProps = {
  nivel?: NivelSelo
  className?: string
  children: ReactNode
}

export function Selo({ nivel = 'neutro', className, children }: SeloProps) {
  const { cor, forma } = NIVEIS[nivel]
  return (
    <span className={cn('inline-flex h-[26px] items-center gap-[7px] rounded-full px-[11px] text-[12.5px] font-medium whitespace-nowrap', cor, className)}>
      <i aria-hidden="true" className={cn('block shrink-0 bg-current', forma)} />
      {children}
    </span>
  )
}
