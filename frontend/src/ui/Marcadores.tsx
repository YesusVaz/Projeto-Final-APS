import { cn } from './cn'
import { Icone } from './Icone'

type MarcadorProps = {
  marcado: boolean
  onMudar: (marcado: boolean) => void
  rotulo: string
  desativado?: boolean
}

// liga e desliga na hora (usuario ativo, aviso por e-mail)
export function Interruptor({ marcado, onMudar, rotulo, desativado }: MarcadorProps) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={marcado}
      aria-label={rotulo}
      disabled={desativado}
      onClick={() => onMudar(!marcado)}
      className={cn('relative h-6 w-[42px] shrink-0 rounded-full transition-colors duration-250 disabled:opacity-45', marcado ? 'bg-ink' : 'bg-ink-4')}
    >
      <span
        className={cn(
          'absolute top-[3px] left-[3px] size-[18px] rounded-full bg-white shadow-[0_1px_3px_rgb(0_0_0/0.25)] transition-transform duration-300 ease-[cubic-bezier(0.3,1.4,0.5,1)]',
          marcado && 'translate-x-[18px]',
        )}
      />
    </button>
  )
}

// caixa de marcar. o texto ao lado (rotulo) tambem clica
export function Caixa({ marcado, onMudar, rotulo, desativado }: MarcadorProps) {
  return (
    <label className={cn('inline-flex items-center gap-3', desativado && 'opacity-45')}>
      <button
        type="button"
        role="checkbox"
        aria-checked={marcado}
        disabled={desativado}
        onClick={() => onMudar(!marcado)}
        className={cn(
          'flex size-[18px] items-center justify-center rounded-md border-[1.5px] text-white transition-colors',
          marcado ? 'border-ink bg-ink' : 'border-ink-4 bg-surface',
        )}
      >
        {marcado ? <Icone nome="ok" tamanho={12} /> : null}
      </button>
      {rotulo}
    </label>
  )
}
