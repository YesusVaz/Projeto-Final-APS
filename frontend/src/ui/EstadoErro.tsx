import { Botao } from './Botao'
import { cn } from './cn'
import { Icone } from './Icone'

type EstadoErroProps = {
  mensagem: string
  onTentar: () => void
  className?: string
}

export function EstadoErro({ mensagem, onTentar, className }: EstadoErroProps) {
  return (
    <div role="alert" className={cn('flex flex-col items-center gap-3 py-14 text-center', className)}>
      <span className="flex size-12 items-center justify-center rounded-full bg-critico-bg text-critico">
        <Icone nome="aviso" tamanho={22} />
      </span>
      <p className="text-base font-medium">Não deu para carregar</p>
      <p className="max-w-[44ch] text-ink-3">{mensagem}</p>
      <Botao variante="contorno" onClick={onTentar} className="mt-2">
        Tentar de novo
      </Botao>
    </div>
  )
}
