import {
  useEffect,
  useLayoutEffect,
  useRef,
  useState,
  type KeyboardEvent,
  type PointerEvent as PointerReact,
} from 'react'
import { cn } from './cn'

export type ItemNav<T extends string> = {
  id: T
  rotulo: string
  // numero pequeno ao lado do nome, tipo alertas abertos
  contador?: number
  // item redondo so com o rotulo curto (avatar). precisa de descricao pro leitor de tela
  compacto?: boolean
  descricao?: string
}

type Medida = { x: number; largura: number }

// o gesto mora numa ref: muda a cada movimento do ponteiro e nao precisa render
type Gesto<T> = {
  inicioX: number
  origem: Medida
  movido: boolean
  previa: T
}

// quanto o ponteiro precisa andar pra contar como arraste e nao como clique
const LIMITE_ARRASTE = 4

type NavPilulaProps<T extends string> = {
  itens: ItemNav<T>[]
  ativo: T
  onMudar: (id: T) => void
  className?: string
}

// navbar em pilula, estilo Apple: vidro escuro, destaque que desliza ate o item escolhido
// e pode ser arrastado. ao soltar, encaixa no item mais proximo. nao fica fixa: quem usa
// decide onde ela fica (o cabecalho do layout)
export function NavPilula<T extends string>({ itens, ativo, onMudar, className }: NavPilulaProps<T>) {
  const botoes = useRef(new Map<T, HTMLButtonElement>())
  const destaqueRef = useRef<HTMLSpanElement>(null)
  const gesto = useRef<Gesto<T> | null>(null)
  const removerEscutas = useRef<(() => void) | null>(null)
  // so o item sob o destaque entra no estado, pra cor do texto acompanhar o arraste
  const [previa, setPrevia] = useState<T | null>(null)

  function medir(id: T): Medida | null {
    const botao = botoes.current.get(id)
    return botao ? { x: botao.offsetLeft, largura: botao.offsetWidth } : null
  }

  // posiciona o destaque direto no DOM. animado=false tira a transicao, que so pode ficar fora do arraste
  function posicionar(medida: Medida, animado: boolean) {
    const el = destaqueRef.current
    if (!el) return
    el.style.transition = animado ? '' : 'none'
    el.style.transform = `translateX(${medida.x}px)`
    el.style.width = `${medida.largura}px`
  }

  // o item cujo centro fica mais perto do centro do destaque
  function maisProximo(x: number, largura: number): T | null {
    const centro = x + largura / 2
    let escolhido: T | null = null
    let menor = Infinity
    for (const item of itens) {
      const medida = medir(item.id)
      if (!medida) continue
      const distancia = Math.abs(medida.x + medida.largura / 2 - centro)
      if (distancia < menor) {
        menor = distancia
        escolhido = item.id
      }
    }
    return escolhido
  }

  // o destaque acompanha o item ativo. quando o ativo muda, ele desliza ate la
  useLayoutEffect(() => {
    if (gesto.current) return
    const medida = medir(ativo)
    if (medida) posicionar(medida, true)
  }, [ativo, itens])

  // janela redimensionada ou fonte carregada depois: mede de novo sem animar
  useEffect(() => {
    function reposicionar() {
      if (gesto.current) return
      const medida = medir(ativo)
      if (medida) posicionar(medida, false)
    }
    window.addEventListener('resize', reposicionar)
    document.fonts.ready.then(reposicionar)
    return () => window.removeEventListener('resize', reposicionar)
  }, [ativo])

  // se o componente sair da tela no meio de um arraste, tira as escutas da janela
  useEffect(() => () => removerEscutas.current?.(), [])

  function aoPressionar(evento: PointerReact<HTMLElement>) {
    if (evento.button !== 0) return
    const origem = medir(ativo)
    if (!origem) return
    gesto.current = { inicioX: evento.clientX, origem, movido: false, previa: ativo }
    setPrevia(ativo)

    function aoMover(e: PointerEvent) {
      const atual = gesto.current
      const primeiro = medir(itens[0].id)
      const ultimo = medir(itens[itens.length - 1].id)
      if (!atual || !primeiro || !ultimo) return
      const dx = e.clientX - atual.inicioX
      const x = Math.min(Math.max(atual.origem.x + dx, primeiro.x), ultimo.x)
      atual.movido = atual.movido || Math.abs(dx) > LIMITE_ARRASTE
      posicionar({ x, largura: atual.origem.largura }, false)
      const proximo = maisProximo(x, atual.origem.largura)
      if (proximo !== null && proximo !== atual.previa) {
        atual.previa = proximo
        setPrevia(proximo)
      }
    }

    function aoSoltar() {
      removerEscutas.current?.()
      removerEscutas.current = null
      const atual = gesto.current
      gesto.current = null
      setPrevia(null)
      if (!atual) return

      // sem arraste, o destaque volta pro ativo e o clique normal do botao faz a troca
      if (!atual.movido) {
        const medida = medir(ativo)
        if (medida) posicionar(medida, true)
        return
      }

      const medida = medir(atual.previa)
      if (medida) posicionar(medida, true)
      onMudar(atual.previa)
    }

    window.addEventListener('pointermove', aoMover)
    window.addEventListener('pointerup', aoSoltar)
    removerEscutas.current = () => {
      window.removeEventListener('pointermove', aoMover)
      window.removeEventListener('pointerup', aoSoltar)
    }
  }

  function aoTeclar(evento: KeyboardEvent<HTMLElement>) {
    const passo = evento.key === 'ArrowRight' ? 1 : evento.key === 'ArrowLeft' ? -1 : 0
    if (passo === 0) return
    evento.preventDefault()
    const indice = itens.findIndex((item) => item.id === ativo)
    const proximo = itens[(indice + passo + itens.length) % itens.length]
    onMudar(proximo.id)
    botoes.current.get(proximo.id)?.focus()
  }

  const selecionado = previa ?? ativo

  return (
    <nav
      aria-label="Navegação principal"
      onPointerDown={aoPressionar}
      onKeyDown={aoTeclar}
      className={cn(
        'relative flex h-13 animate-descer touch-none items-center rounded-full bg-ink/90 p-1.5 whitespace-nowrap text-paper shadow-[0_10px_30px_rgb(11_11_11/0.14)] backdrop-blur-xl backdrop-saturate-150 select-none',
        className,
      )}
    >
      <span
        ref={destaqueRef}
        aria-hidden="true"
        className="absolute inset-y-1.5 left-0 rounded-full bg-paper transition-[transform,width] duration-500 ease-[cubic-bezier(0.2,0.8,0.2,1)] motion-reduce:transition-none"
      />
      {itens.map((item) => {
        const aqui = item.id === selecionado
        return (
          <button
            key={item.id}
            ref={(el) => {
              if (el) botoes.current.set(item.id, el)
              else botoes.current.delete(item.id)
            }}
            type="button"
            onClick={() => onMudar(item.id)}
            aria-current={item.id === ativo ? 'page' : undefined}
            aria-label={item.descricao}
            className={cn(
              'relative flex h-10 items-center justify-center gap-2 rounded-full text-sm transition-colors duration-300',
              item.compacto ? 'ml-1 w-10 text-[13px]' : 'px-4',
              aqui ? 'font-medium text-ink' : 'text-paper-2 hover:text-paper',
            )}
          >
            {item.rotulo}
            {item.contador ? (
              <span className="inline-flex h-5 min-w-5 animate-pulso items-center justify-center rounded-full bg-contador px-1.5 text-[11.5px] font-semibold text-white">
                {item.contador}
              </span>
            ) : null}
          </button>
        )
      })}
    </nav>
  )
}
