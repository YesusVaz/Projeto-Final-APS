import {
  useEffect,
  useLayoutEffect,
  useRef,
  useState,
  type KeyboardEvent,
  type PointerEvent as PointerReact,
} from 'react'

export type OpcaoNav<T extends string> = { id: T; rotulo: string }

type NavegacaoProps<T extends string> = {
  opcoes: OpcaoNav<T>[]
  ativo: T
  onMudar: (id: T) => void
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

// ilha de navegacao. o destaque desliza ate a opcao escolhida e pode ser arrastado.
// ao soltar, ele encaixa na opcao mais proxima
export function Navegacao<T extends string>({ opcoes, ativo, onMudar }: NavegacaoProps<T>) {
  const botoes = useRef(new Map<T, HTMLButtonElement>())
  const destaqueRef = useRef<HTMLSpanElement>(null)
  const gesto = useRef<Gesto<T> | null>(null)
  const removerEscutas = useRef<(() => void) | null>(null)
  // so a opcao sob o destaque entra no estado, pra cor do texto acompanhar o arraste
  const [previa, setPrevia] = useState<T | null>(null)

  function medir(id: T): Medida | null {
    const botao = botoes.current.get(id)
    return botao ? { x: botao.offsetLeft, largura: botao.offsetWidth } : null
  }

  // posiciona o destaque direto no DOM. sem estado, entao nao gera render extra.
  // animado=false tira a transicao, que so pode ficar ligada fora do arraste
  function posicionar(medida: Medida, animado: boolean) {
    const el = destaqueRef.current
    if (!el) return
    el.style.transition = animado ? '' : 'none'
    el.style.transform = `translateX(${medida.x}px)`
    el.style.width = `${medida.largura}px`
  }

  // a opcao cujo centro fica mais perto do centro do destaque
  function maisProxima(x: number, largura: number): T | null {
    const centro = x + largura / 2
    let escolhida: T | null = null
    let menorDistancia = Infinity
    for (const opcao of opcoes) {
      const medida = medir(opcao.id)
      if (!medida) continue
      const distancia = Math.abs(medida.x + medida.largura / 2 - centro)
      if (distancia < menorDistancia) {
        menorDistancia = distancia
        escolhida = opcao.id
      }
    }
    return escolhida
  }

  // o destaque acompanha a opcao ativa. quando o ativo muda, ele desliza ate la
  useLayoutEffect(() => {
    if (gesto.current) return
    const medida = medir(ativo)
    if (medida) posicionar(medida, true)
  }, [ativo, opcoes])

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
      const primeira = medir(opcoes[0].id)
      const ultima = medir(opcoes[opcoes.length - 1].id)
      if (!atual || !primeira || !ultima) return
      const dx = e.clientX - atual.inicioX
      const x = Math.min(Math.max(atual.origem.x + dx, primeira.x), ultima.x)
      atual.movido = atual.movido || Math.abs(dx) > LIMITE_ARRASTE
      posicionar({ x, largura: atual.origem.largura }, false)
      const proxima = maisProxima(x, atual.origem.largura)
      if (proxima !== null && proxima !== atual.previa) {
        atual.previa = proxima
        setPrevia(proxima)
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

      const destino = atual.previa
      const medida = medir(destino)
      if (medida) posicionar(medida, true)
      onMudar(destino)
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
    const indice = opcoes.findIndex((opcao) => opcao.id === ativo)
    const proxima = opcoes[(indice + passo + opcoes.length) % opcoes.length]
    onMudar(proxima.id)
    botoes.current.get(proxima.id)?.focus()
  }

  const selecionado = previa ?? ativo

  return (
    <nav
      aria-label="Navegação principal"
      onPointerDown={aoPressionar}
      onKeyDown={aoTeclar}
      className="fixed top-5 left-1/2 z-10 flex h-12 -translate-x-1/2 touch-none items-center rounded-full bg-ink p-1.5 whitespace-nowrap text-paper select-none"
    >
      <span
        ref={destaqueRef}
        aria-hidden="true"
        className="absolute inset-y-1.5 left-0 rounded-full bg-paper transition-[transform,width] duration-500 ease-[cubic-bezier(0.2,0.8,0.2,1)] motion-reduce:transition-none"
      >
        <span className="absolute top-1/2 left-2 size-1.5 -translate-y-1/2 rounded-full bg-accent" />
      </span>
      {opcoes.map((opcao) => (
        <button
          key={opcao.id}
          ref={(el) => {
            if (el) botoes.current.set(opcao.id, el)
            else botoes.current.delete(opcao.id)
          }}
          type="button"
          onClick={() => onMudar(opcao.id)}
          aria-current={opcao.id === ativo ? 'page' : undefined}
          className={`relative flex h-9 items-center justify-center px-5 text-[13px] transition-colors duration-300 ${
            opcao.id === selecionado ? 'font-medium text-ink' : 'text-paper-2 hover:text-paper'
          }`}
        >
          {opcao.rotulo}
        </button>
      ))}
    </nav>
  )
}
