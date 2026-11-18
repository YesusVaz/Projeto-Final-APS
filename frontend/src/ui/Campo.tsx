import { useId, type InputHTMLAttributes, type ReactNode, type TextareaHTMLAttributes } from 'react'
import { cn } from './cn'
import { Icone } from './Icone'

// o que o Campo entrega pro controle: id do rotulo e ligacao com a mensagem
export type PropsControle = {
  id: string
  'aria-describedby'?: string
  'aria-invalid'?: true
}

type CampoProps = {
  rotulo: string
  dica?: string
  erro?: string
  // botao pequeno na linha do rotulo, tipo "Mostrar" da senha
  acao?: ReactNode
  className?: string
  children: (controle: PropsControle) => ReactNode
}

// rotulo + controle + mensagem. uso: <Campo rotulo="Usuario">{(c) => <Entrada {...c} />}</Campo>
export function Campo({ rotulo, dica, erro, acao, className, children }: CampoProps) {
  const id = useId()
  const idMensagem = `${id}-msg`
  const mensagem = erro ?? dica

  return (
    <div className={cn('flex flex-col gap-1.5', className)}>
      <div className="flex items-center justify-between">
        <label htmlFor={id} className="text-[13px] font-medium text-ink-hover">
          {rotulo}
        </label>
        {acao}
      </div>
      {children({
        id,
        'aria-describedby': mensagem ? idMensagem : undefined,
        'aria-invalid': erro ? true : undefined,
      })}
      {mensagem ? (
        erro ? (
          <p id={idMensagem} role="alert" className="flex items-center gap-1.5 text-[12.5px] text-critico">
            <Icone nome="aviso" tamanho={14} />
            {erro}
          </p>
        ) : (
          <p id={idMensagem} className="text-[12.5px] text-ink-3">
            {dica}
          </p>
        )
      ) : null}
    </div>
  )
}

const CLASSE_CONTROLE =
  'block w-full rounded-xl border border-line-input bg-surface text-[15px] outline-none transition placeholder:text-ink-4 hover:border-line-hover focus:border-accent focus:ring-4 focus:ring-accent/12 aria-[invalid=true]:border-perigo disabled:bg-hover disabled:text-ink-3'

type EntradaProps = InputHTMLAttributes<HTMLInputElement> & {
  // numeros e codigos ficam na fonte mono, alinhados
  mono?: boolean
}

export function Entrada({ mono, className, ...resto }: EntradaProps) {
  return <input className={cn(CLASSE_CONTROLE, 'h-11 px-3.5', mono && 'font-mono tabular-nums', className)} {...resto} />
}

export function AreaTexto({ className, ...resto }: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea className={cn(CLASSE_CONTROLE, 'h-22 resize-none px-3.5 py-3', className)} {...resto} />
}

export { CLASSE_CONTROLE }

type ContadorProps = {
  valor: number
  onMudar: (valor: number) => void
  minimo?: number
  passo?: number
  // texto lido pelo leitor de tela nos botoes
  rotuloMenos: string
  rotuloMais: string
} & PropsControle

// campo de quantidade grande, com - e +, pensado pra usar com o polegar no celular
export function Contador({ valor, onMudar, minimo = 0, passo = 1, rotuloMenos, rotuloMais, ...controle }: ContadorProps) {
  const botao =
    'flex h-14 items-center justify-center rounded-2xl border border-line-input bg-surface transition duration-150 hover:bg-hover active:scale-[0.94] disabled:opacity-45'
  return (
    <div className="grid grid-cols-[56px_1fr_56px] items-center gap-2">
      <button type="button" aria-label={rotuloMenos} disabled={valor <= minimo} onClick={() => onMudar(Math.max(minimo, valor - passo))} className={botao}>
        <Icone nome="menos" tamanho={22} />
      </button>
      <input
        {...controle}
        type="number"
        inputMode="numeric"
        min={minimo}
        value={Number.isNaN(valor) ? '' : valor}
        onChange={(e) => onMudar(e.target.valueAsNumber)}
        className={cn(CLASSE_CONTROLE, 'h-[72px] text-center font-mono text-[34px] tracking-tight tabular-nums')}
      />
      <button type="button" aria-label={rotuloMais} onClick={() => onMudar((Number.isNaN(valor) ? 0 : valor) + passo)} className={botao}>
        <Icone nome="mais" tamanho={22} />
      </button>
    </div>
  )
}
