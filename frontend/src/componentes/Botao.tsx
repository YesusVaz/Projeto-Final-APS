import type { ButtonHTMLAttributes } from 'react'

type Variante = 'cheio' | 'linha' | 'texto'

type BotaoProps = Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'type'> & {
  variante?: Variante
  seta?: boolean
  type?: 'button' | 'submit'
}

// todo botao de acao tem a mesma altura (3rem) e o mesmo texto (15px).
// a variante muda so o visual. cada uma tem a classe inteira, sem misturar uma com a outra
const CLASSES: Record<Variante, string> = {
  cheio: 'flex h-12 w-full items-center justify-between bg-ink px-4 text-[15px] font-medium tracking-tight text-paper',
  linha: 'flex h-12 w-full items-center justify-between border border-ink px-4 text-[15px] font-medium tracking-tight text-ink',
  texto: 'flex h-12 items-center self-start text-[15px] text-ink-2 underline underline-offset-4',
}

export function Botao({ variante = 'cheio', seta = false, type = 'button', children, ...resto }: BotaoProps) {
  return (
    <button type={type} className={CLASSES[variante]} {...resto}>
      <span>{children}</span>
      {seta ? <span aria-hidden="true">→</span> : null}
    </button>
  )
}
