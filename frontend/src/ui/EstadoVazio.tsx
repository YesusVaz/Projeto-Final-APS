import type { ReactNode } from 'react'
import { cn } from './cn'
import { Icone, type NomeIcone } from './Icone'

type EstadoVazioProps = {
  icone?: NomeIcone
  titulo: string
  // diz o que fazer em seguida, nao so que esta vazio
  texto?: string
  acao?: ReactNode
  className?: string
}

export function EstadoVazio({ icone = 'info', titulo, texto, acao, className }: EstadoVazioProps) {
  return (
    <div className={cn('flex flex-col items-center gap-3 py-14 text-center', className)}>
      <span className="flex size-12 items-center justify-center rounded-full bg-trilho text-ink-2">
        <Icone nome={icone} tamanho={22} />
      </span>
      <p className="text-base font-medium">{titulo}</p>
      {texto ? <p className="max-w-[44ch] text-ink-3">{texto}</p> : null}
      {acao ? <div className="mt-2">{acao}</div> : null}
    </div>
  )
}
