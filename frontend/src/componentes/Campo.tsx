import type { ReactNode } from 'react'

type CampoProps = {
  id: string
  rotulo: string
  dica?: string
  erro?: string
  // botao pequeno na linha do rotulo, tipo mostrar senha
  acao?: ReactNode
  children: ReactNode
}

// rotulo, controle e aviso. a linha do rotulo e a do aviso tem altura fixa,
// entao a tela nao pula quando uma mensagem aparece ou some
export function Campo({ id, rotulo, dica, erro, acao, children }: CampoProps) {
  return (
    <div className="flex flex-col">
      <div className="flex h-5 items-center justify-between">
        <label htmlFor={id} className="font-mono text-[11px] tracking-wide text-ink-3 uppercase">
          {rotulo}
        </label>
        {acao}
      </div>
      <div className="mt-2">{children}</div>
      <div className="mt-1.5 min-h-5">
        {erro ? (
          <p role="alert" className="border-l-2 border-accent pl-3 text-[13px] leading-5 text-ink-2">
            <span aria-hidden="true" className="font-mono text-[11px] font-medium text-accent">
              ■{' '}
            </span>
            {erro}
          </p>
        ) : dica ? (
          <p className="font-mono text-[11px] leading-5 text-ink-3">{dica}</p>
        ) : null}
      </div>
    </div>
  )
}
