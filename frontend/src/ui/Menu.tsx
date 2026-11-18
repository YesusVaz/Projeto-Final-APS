import { createContext, useContext, useEffect, useRef, useState, type KeyboardEvent, type ReactNode } from 'react'
import { cn } from './cn'
import { Icone, type NomeIcone } from './Icone'

// o que o gatilho recebe pra se ligar ao menu
export type PropsGatilho = {
  onClick: () => void
  'aria-expanded': boolean
  'aria-haspopup': 'menu'
}

type MenuProps = {
  gatilho: (props: PropsGatilho) => ReactNode
  children: ReactNode
  alinhar?: 'direita' | 'esquerda'
}

const FecharMenu = createContext<() => void>(() => {})

// menu que abre embaixo do gatilho. fecha com Esc, clique fora ou ao escolher um item.
// setas pra cima e pra baixo andam pelos itens
export function Menu({ gatilho, children, alinhar = 'direita' }: MenuProps) {
  const [aberto, setAberto] = useState(false)
  const raiz = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!aberto) return
    // foca o primeiro item quando abre
    raiz.current?.querySelector<HTMLElement>('[role="menuitem"]')?.focus()

    function aoClicarFora(e: PointerEvent) {
      if (!raiz.current?.contains(e.target as Node)) setAberto(false)
    }
    function aoTeclar(e: globalThis.KeyboardEvent) {
      if (e.key !== 'Escape') return
      setAberto(false)
      raiz.current?.querySelector<HTMLElement>('[aria-haspopup]')?.focus()
    }
    window.addEventListener('pointerdown', aoClicarFora)
    window.addEventListener('keydown', aoTeclar)
    return () => {
      window.removeEventListener('pointerdown', aoClicarFora)
      window.removeEventListener('keydown', aoTeclar)
    }
  }, [aberto])

  function andar(e: KeyboardEvent<HTMLDivElement>) {
    const passo = e.key === 'ArrowDown' ? 1 : e.key === 'ArrowUp' ? -1 : 0
    if (passo === 0) return
    e.preventDefault()
    const itens = Array.from(e.currentTarget.querySelectorAll<HTMLElement>('[role="menuitem"]'))
    const atual = itens.indexOf(document.activeElement as HTMLElement)
    itens[(atual + passo + itens.length) % itens.length]?.focus()
  }

  return (
    <div ref={raiz} className="relative inline-block">
      {gatilho({ onClick: () => setAberto((a) => !a), 'aria-expanded': aberto, 'aria-haspopup': 'menu' })}
      {aberto ? (
        <FecharMenu value={() => setAberto(false)}>
          <div
            role="menu"
            onKeyDown={andar}
            className={cn(
              'absolute top-[calc(100%+8px)] z-30 w-[272px] origin-top-right animate-surgir rounded-[20px] border border-line-card bg-surface p-1.5 shadow-[0_20px_56px_rgb(11_11_11/0.18)]',
              alinhar === 'direita' ? 'right-0' : 'left-0 origin-top-left',
            )}
          >
            {children}
          </div>
        </FecharMenu>
      ) : null}
    </div>
  )
}

type MenuItemProps = {
  icone?: NomeIcone
  descricao?: string
  href?: string
  onSelecionar?: () => void
  children: ReactNode
}

const CLASSE_ITEM =
  'flex min-h-12 w-full items-center gap-3 rounded-[14px] px-3 py-1.5 text-left text-sm transition-colors hover:bg-[#f2f2ee] focus-visible:bg-[#f2f2ee]'

export function MenuItem({ icone, descricao, href, onSelecionar, children }: MenuItemProps) {
  const fechar = useContext(FecharMenu)
  const miolo = (
    <>
      {icone ? <Icone nome={icone} tamanho={20} /> : null}
      <span>
        {children}
        {descricao ? <small className="block text-[12.5px] leading-tight text-ink-3">{descricao}</small> : null}
      </span>
    </>
  )

  if (href) {
    return (
      <a role="menuitem" href={href} onClick={fechar} className={CLASSE_ITEM}>
        {miolo}
      </a>
    )
  }
  return (
    <button
      type="button"
      role="menuitem"
      onClick={() => {
        onSelecionar?.()
        fechar()
      }}
      className={CLASSE_ITEM}
    >
      {miolo}
    </button>
  )
}
