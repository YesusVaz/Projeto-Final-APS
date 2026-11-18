import { animate, motion, useMotionValue, useReducedMotion } from 'motion/react'
import {
  useCallback,
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
  // botao de acao (o avatar que abre o menu da conta): fica dentro da pilula, mas o destaque nao vai ate ele
  acao?: boolean
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

// sem quique (bounce 0): com quique o destaque passa do item e vaza do fundo da pilula. 0.7s e o ritmo calmo
const MOLA = { type: 'spring', duration: 0.7, bounce: 0 } as const

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
  const navRef = useRef<HTMLElement>(null)
  // o que o destaque pode escolher. o avatar de conta e so um botao
  const selecionaveis = itens.filter((item) => !item.acao)
  // posicao e largura do destaque. sao motion values: o arraste mexe neles sem render
  const x = useMotionValue(0)
  const largura = useMotionValue(0)
  const animacoes = useRef<ReturnType<typeof animate>[]>([])
  const posicionado = useRef(false)
  // true enquanto o destaque desliza. quem remede no meio do caminho refaz o trajeto em vez de pular
  const animando = useRef(false)
  const rodada = useRef(0)
  // o ativo mais recente, pra os escutadores de resize e fonte nao precisarem ser refeitos a cada troca
  const ativoRef = useRef(ativo)
  const reduzir = useReducedMotion()
  const gesto = useRef<Gesto<T> | null>(null)
  const removerEscutas = useRef<(() => void) | null>(null)
  // so o item sob o destaque entra no estado, pra cor do texto acompanhar o arraste
  const [previa, setPrevia] = useState<T | null>(null)

  function medir(id: T): Medida | null {
    const botao = botoes.current.get(id)
    return botao ? { x: botao.offsetLeft, largura: botao.offsetWidth } : null
  }

  // leva o destaque ate a medida. animado=true usa a mola do Motion, a mesma do seletor do login.
  // durante o arraste e na primeira medida ele vai direto, sem animar
  const posicionar = useCallback(
    (medida: Medida, animado: boolean) => {
      animacoes.current.forEach((controle) => controle.stop())
      const id = ++rodada.current
      if (!animado || reduzir) {
        animacoes.current = []
        animando.current = false
        x.set(medida.x)
        largura.set(medida.largura)
        return
      }
      animando.current = true
      animacoes.current = [animate(x, medida.x, MOLA), animate(largura, medida.largura, MOLA)]
      // so a ultima rodada pode dizer que terminou; uma parada no meio nao conta
      void Promise.all(animacoes.current).then(() => {
        if (rodada.current === id) animando.current = false
      })
    },
    [x, largura, reduzir],
  )

  // o item cujo centro fica mais perto do centro do destaque
  function maisProximo(x: number, largura: number): T | null {
    const centro = x + largura / 2
    let escolhido: T | null = null
    let menor = Infinity
    for (const item of selecionaveis) {
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
    ativoRef.current = ativo
    if (gesto.current) return
    const medida = medir(ativo)
    if (medida) posicionar(medida, posicionado.current)
    posicionado.current = true
  }, [ativo, posicionar])

  // janela redimensionada, fonte carregada depois ou contador aparecendo (a pilula muda de largura):
  // mede de novo. se o destaque esta deslizando, so corrige o destino. nao pode depender de `ativo`:
  // senao a cada troca o document.fonts.ready (ja resolvido) rodava na hora e fazia o destaque pular
  useEffect(() => {
    function reposicionar() {
      if (gesto.current) return
      const medida = medir(ativoRef.current)
      if (medida) posicionar(medida, animando.current)
    }
    const observador = new ResizeObserver(reposicionar)
    if (navRef.current) observador.observe(navRef.current)
    void document.fonts.ready.then(reposicionar)
    return () => observador.disconnect()
  }, [posicionar])

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
      const primeiro = medir(selecionaveis[0].id)
      const ultimo = medir(selecionaveis[selecionaveis.length - 1].id)
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
    const indice = selecionaveis.findIndex((item) => item.id === ativo)
    const proximo = selecionaveis[(indice + passo + selecionaveis.length) % selecionaveis.length]
    onMudar(proximo.id)
    botoes.current.get(proximo.id)?.focus()
  }

  const selecionado = previa ?? ativo

  return (
    <nav
      ref={navRef}
      aria-label="Navegação principal"
      onPointerDown={aoPressionar}
      onKeyDown={aoTeclar}
      className={cn(
        'relative flex h-13 animate-descer touch-none items-center rounded-full bg-ink/90 p-1.5 whitespace-nowrap text-paper shadow-[0_10px_30px_rgb(11_11_11/0.14)] backdrop-blur-xl backdrop-saturate-150 select-none',
        className,
      )}
    >
      <motion.span aria-hidden="true" style={{ x, width: largura }} className="absolute inset-y-1.5 left-0 rounded-full bg-paper" />
      {itens.map((item) => {
        const aqui = !item.acao && item.id === selecionado
        return (
          <button
            key={item.id}
            ref={(el) => {
              if (el) botoes.current.set(item.id, el)
              else botoes.current.delete(item.id)
            }}
            type="button"
            onClick={() => onMudar(item.id)}
            aria-current={!item.acao && item.id === ativo ? 'page' : undefined}
            aria-label={item.descricao}
            data-abre-menu={item.acao ? '' : undefined}
            aria-haspopup={item.acao ? 'menu' : undefined}
            className={cn(
              'relative flex h-10 items-center justify-center gap-2 rounded-full text-sm transition-colors duration-300',
              item.compacto ? 'ml-1 w-10 text-[13px]' : 'px-4',
              item.acao
                ? 'bg-[#2a2a28] font-medium text-paper hover:bg-[#3a3a37]'
                : aqui
                  ? 'font-medium text-ink'
                  : 'text-paper-2 hover:text-paper',
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
