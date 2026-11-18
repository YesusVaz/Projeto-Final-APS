import type { InputHTMLAttributes, ReactNode } from 'react'
import { cn, Icone, type NomeIcone } from '../../ui'

type EntradaAcessoProps = InputHTMLAttributes<HTMLInputElement> & {
  icone: NomeIcone
  // elemento colado na direita do campo, tipo o olho da senha
  fim?: ReactNode
}

// campo das telas de acesso: icone na esquerda, altura maior e foco em preto, sem aquele anel azul pesado
export function EntradaAcesso({ icone, fim, className, ...resto }: EntradaAcessoProps) {
  return (
    <div className="group relative">
      <Icone
        nome={icone}
        tamanho={20}
        className="pointer-events-none absolute top-1/2 left-4.5 shrink-0 -translate-y-1/2 text-ink-4 transition-colors duration-200 group-focus-within:text-accent"
      />
      <input
        className={cn(
          'block h-14 w-full rounded-2xl border border-line-input bg-surface pl-12 text-base outline-none transition-[border-color,box-shadow] duration-200 placeholder:text-ink-4 hover:border-line-hover focus:border-accent focus:shadow-[inset_0_0_0_1px_var(--color-accent)] aria-[invalid=true]:border-perigo aria-[invalid=true]:focus:shadow-[inset_0_0_0_1px_var(--color-perigo)]',
          fim ? 'pr-14' : 'pr-4',
          className,
        )}
        {...resto}
      />
      {fim ? <div className="absolute inset-y-0 right-2 flex items-center">{fim}</div> : null}
    </div>
  )
}

type OlhoSenhaProps = {
  visivel: boolean
  onAlternar: () => void
}

export function OlhoSenha({ visivel, onAlternar }: OlhoSenhaProps) {
  return (
    <button
      type="button"
      onClick={onAlternar}
      aria-pressed={visivel}
      aria-label={visivel ? 'Ocultar senha' : 'Mostrar senha'}
      className="flex size-11 items-center justify-center rounded-xl text-ink-3 transition-colors hover:bg-hover hover:text-ink"
    >
      <Icone nome={visivel ? 'olhoFechado' : 'olho'} tamanho={18} />
    </button>
  )
}
