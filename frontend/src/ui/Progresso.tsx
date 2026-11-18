import { motion } from 'motion/react'
import { cn } from './cn'
import { suave, tempos } from './movimento'

export type TomProgresso = 'normal' | 'atencao' | 'critico' | 'acao'

const COR: Record<TomProgresso, string> = {
  normal: 'bg-ink',
  atencao: 'bg-barra-atencao',
  critico: 'bg-perigo',
  acao: 'bg-accent',
}

type ProgressoProps = {
  // 0 a 100
  valor: number
  tom?: TomProgresso
  // posicao na lista: a barra comeca a crescer um pouco depois da anterior
  indice?: number
  rotulo: string
  className?: string
}

// nivel de estoque, saldo do lote... a barra cresce da esquerda ate o valor.
// a largura fica fixa e quem anima e o scaleX, que nao refaz o layout
export function Progresso({ valor, tom = 'normal', indice = 0, rotulo, className }: ProgressoProps) {
  const limitado = Math.min(100, Math.max(0, valor))
  return (
    <div
      role="progressbar"
      aria-label={rotulo}
      aria-valuenow={Math.round(limitado)}
      aria-valuemin={0}
      aria-valuemax={100}
      className={cn('h-1.5 overflow-hidden rounded-full bg-barra-fundo', className)}
    >
      <motion.div
        initial={{ scaleX: 0 }}
        animate={{ scaleX: 1 }}
        transition={{ ...suave, duration: tempos.barra, delay: 0.25 + indice * 0.07 }}
        className={cn('h-full origin-left rounded-full', COR[tom])}
        style={{ width: `${limitado}%` }}
      />
    </div>
  )
}
