import { AnimatePresence, motion, type Variants } from 'motion/react'
import { useState, type ReactNode } from 'react'
import { Link, useLocation, useOutlet } from 'react-router'
import { cn, Marca } from '../../ui'
import { PainelLateral } from './PainelLateral'

const ABAS = [
  { id: 'entrar', caminho: '/entrar', rotulo: 'Entrar' },
  { id: 'cadastro', caminho: '/cadastro', rotulo: 'Criar conta' },
] as const

const DESLOCAMENTO = 36

// o formulario sai pra um lado e o outro entra do lado oposto. `custom` e o sentido (1 ou -1)
const deslizar: Variants = {
  entra: (sentido: number) => ({ opacity: 0, x: sentido * DESLOCAMENTO }),
  centro: { opacity: 1, x: 0, transition: { duration: 0.32, ease: 'easeOut' } },
  sai: (sentido: number) => ({ opacity: 0, x: sentido * -DESLOCAMENTO, transition: { duration: 0.14, ease: 'easeIn' } }),
}

// guarda o que a rota mostrava quando montou. sem isso, durante a saida o formulario
// antigo trocaria pelo conteudo da rota nova
function Congelada({ children }: { children: ReactNode }) {
  const [guardado] = useState(children)
  return guardado
}

// moldura das duas telas publicas. ela fica montada na troca Entrar <-> Criar conta:
// so o formulario desliza, o seletor move a pilula e o card lateral muda de cor
export function LayoutAcesso() {
  const outlet = useOutlet()
  const { pathname } = useLocation()
  const indice = pathname === '/cadastro' ? 1 : 0

  const [aba, setAba] = useState({ indice, sentido: 0 })
  if (aba.indice !== indice) setAba({ indice, sentido: indice > aba.indice ? 1 : -1 })

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.3 }}
      className="mx-auto flex min-h-dvh w-full max-w-[1160px] items-center justify-center gap-14 px-5 py-8"
    >
      <PainelLateral cadastro={indice === 1} />

      <section className="flex w-full max-w-[460px] min-w-0 flex-col md:min-h-[700px] md:flex-1">
        <Marca className="mb-8 self-center md:hidden" />
        <nav aria-label="Acesso" className="flex justify-center">
          <div className="relative inline-flex rounded-full bg-trilho p-1">
            {ABAS.map((item, i) => {
              const ativa = i === indice
              return (
                <Link
                  key={item.id}
                  to={item.caminho}
                  aria-current={ativa ? 'page' : undefined}
                  className={cn(
                    'relative z-10 flex h-12 items-center rounded-full px-6 text-[15px] transition-colors duration-200',
                    ativa ? 'font-medium text-ink' : 'text-ink-3 hover:text-ink',
                  )}
                >
                  {ativa ? (
                    <motion.span
                      layoutId="pilula-acesso"
                      transition={{ type: 'spring', duration: 0.6, bounce: 0 }}
                      className="absolute inset-0 -z-10 rounded-full bg-surface shadow-sm"
                    />
                  ) : null}
                  {item.rotulo}
                </Link>
              )
            })}
          </div>
        </nav>

        <div className="-mx-3 mt-8 flex flex-1 flex-col justify-center overflow-x-clip px-3 py-2">
          <AnimatePresence mode="wait" initial={false} custom={aba.sentido}>
            <motion.div key={pathname} custom={aba.sentido} variants={deslizar} initial="entra" animate="centro" exit="sai">
              <Congelada>{outlet}</Congelada>
            </motion.div>
          </AnimatePresence>
        </div>
      </section>
    </motion.div>
  )
}
