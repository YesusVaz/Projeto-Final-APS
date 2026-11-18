import { AnimatePresence, motion } from 'motion/react'
import { useEffect, useRef, useState, type ReactNode } from 'react'
import { useLocation, useOutlet } from 'react-router'
import { propsPagina } from '../ui'

// guarda o que a rota mostrava quando montou. sem isso, durante a animacao de saida a pagina
// antiga trocaria pelo conteudo da nova rota
function Congelada({ children }: { children: ReactNode }) {
  const [guardado] = useState(children)
  return guardado
}

function Pagina({ children }: { children: ReactNode }) {
  const ref = useRef<HTMLDivElement>(null)

  // leitor de tela e teclado: a cada troca de rota o foco vai pro titulo da pagina
  useEffect(() => {
    window.scrollTo(0, 0)
    ref.current?.querySelector<HTMLElement>('[data-titulo-pagina]')?.focus({ preventScroll: true })
  }, [])

  return (
    <motion.div ref={ref} {...propsPagina}>
      {children}
    </motion.div>
  )
}

// troca de rota: a pagina antiga sai rapido e so depois a nova entra
export function RotaAnimada() {
  const outlet = useOutlet()
  const { pathname } = useLocation()

  return (
    <AnimatePresence mode="wait">
      <Pagina key={pathname}>
        <Congelada>{outlet}</Congelada>
      </Pagina>
    </AnimatePresence>
  )
}
