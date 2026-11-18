import { cn } from './cn'
import { Icone, type NomeIcone } from './Icone'

export type ItemInferior<T extends string> = { id: T; rotulo: string; icone: NomeIcone }

type NavInferiorProps<T extends string> = {
  itens: ItemInferior<T>[]
  ativo: T
  onMudar: (id: T) => void
  // botao azul do meio. no celular e por onde se registra mortalidade, alimentacao...
  acao: { rotulo: string; onClick: () => void }
}

// navbar do celular: pilula flutuante embaixo, ao alcance do polegar.
// so o item ativo mostra o nome. a acao fica no meio dos itens
export function NavInferior<T extends string>({ itens, ativo, onMudar, acao }: NavInferiorProps<T>) {
  const meio = Math.ceil(itens.length / 2)

  function item(i: ItemInferior<T>) {
    const aqui = i.id === ativo
    return (
      <button
        key={i.id}
        type="button"
        onClick={() => onMudar(i.id)}
        aria-current={aqui ? 'page' : undefined}
        aria-label={aqui ? undefined : i.rotulo}
        className={cn(
          'flex h-12 min-w-12 items-center justify-center gap-2 rounded-full text-[13.5px] transition-[background-color,color,padding] duration-300',
          aqui ? 'bg-paper px-4 font-medium text-ink' : 'px-3.5 text-paper-2',
        )}
      >
        <Icone nome={i.icone} tamanho={20} />
        {aqui ? i.rotulo : null}
      </button>
    )
  }

  return (
    <nav
      aria-label="Navegação principal"
      className="fixed bottom-[calc(1.25rem+env(safe-area-inset-bottom))] left-1/2 z-20 flex h-[60px] -translate-x-1/2 animate-descer items-center gap-0.5 rounded-full bg-ink/90 p-1.5 shadow-[0_12px_32px_rgb(11_11_11/0.22)] backdrop-blur-xl backdrop-saturate-150"
    >
      {itens.slice(0, meio).map(item)}
      <button
        type="button"
        onClick={acao.onClick}
        aria-label={acao.rotulo}
        className="mx-1 flex size-12 items-center justify-center rounded-full bg-accent text-white transition duration-150 hover:bg-accent-hover active:scale-[0.94]"
      >
        <Icone nome="mais" tamanho={22} />
      </button>
      {itens.slice(meio).map(item)}
    </nav>
  )
}
