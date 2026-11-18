import { AnimatePresence, motion } from 'motion/react'
import { cn, Marca, propsItem } from '../../ui'

const COR_ENTRAR = '#0b0b0b'
const COR_CADASTRO = '#1f3bff'

// alturas das barrinhas decorativas. a azul e a "do dia"
const BARRAS = [60, 96, 78, 132, 110, 168, 140]

const PASSOS = [
  { titulo: 'Crie sua conta', texto: 'Nome, e-mail e um usuário.' },
  { titulo: 'Entre na granja', texto: 'Sua conta começa como funcionário.' },
  { titulo: 'O gestor ajusta o perfil', texto: 'Quando você precisar de mais acesso.' },
] as const

const troca = {
  initial: { opacity: 0, y: 12 },
  animate: { opacity: 1, y: 0, transition: { duration: 0.3, ease: 'easeOut' } },
  exit: { opacity: 0, y: -8, transition: { duration: 0.12, ease: 'easeIn' } },
} as const

// card lateral das telas de acesso. a cor e o conteudo mudam junto com a aba
export function PainelLateral({ cadastro }: { cadastro: boolean }) {
  return (
    <motion.aside
      initial={false}
      animate={{ backgroundColor: cadastro ? COR_CADASTRO : COR_ENTRAR }}
      transition={{ duration: 0.45, ease: 'easeInOut' }}
      className="relative hidden h-[700px] w-[500px] shrink-0 flex-col overflow-hidden rounded-[32px] p-11 text-paper md:flex"
    >
      <Marca className="[&>span]:bg-paper [&>span]:text-ink" />
      <div className="relative mt-8 flex-1">
        <AnimatePresence mode="wait" initial={false}>
          {cadastro ? (
            <motion.div key="cadastro" {...troca} className="absolute inset-0 flex flex-col justify-between">
              <ol className="flex flex-col gap-7">
                {PASSOS.map((passo, i) => (
                  <motion.li key={passo.titulo} {...propsItem(i)} className="flex items-start gap-3.5">
                    <span className="flex size-10 shrink-0 items-center justify-center rounded-full border border-paper/45 font-mono text-sm">
                      {i + 1}
                    </span>
                    <span>
                      <span className="block text-[17px] font-medium">{passo.titulo}</span>
                      <span className="block text-[15px] text-paper/75">{passo.texto}</span>
                    </span>
                  </motion.li>
                ))}
              </ol>
              <div>
                <p className="max-w-[14ch] text-[40px] leading-[1.08] font-medium tracking-[-0.04em]">Comece pelo registro do dia.</p>
              </div>
            </motion.div>
          ) : (
            <motion.div key="entrar" {...troca} className="absolute inset-0 flex flex-col justify-between">
              <div aria-hidden="true" className="grid h-[170px] grid-cols-7 items-end gap-2.5">
                {BARRAS.map((altura, i) => (
                  <motion.i
                    key={altura}
                    initial={{ scaleY: 0 }}
                    animate={{ scaleY: 1 }}
                    transition={{ duration: 0.6, ease: 'easeOut', delay: 0.15 + i * 0.06 }}
                    style={{ height: altura }}
                    className={cn('block origin-bottom rounded-md', i === 5 ? 'bg-accent' : 'bg-paper/16')}
                  />
                ))}
              </div>
              <div>
                <p className="max-w-[14ch] text-[40px] leading-[1.08] font-medium tracking-[-0.04em]">Lotes, ração e vacinas num lugar só.</p>
                <p className="mt-4 max-w-[34ch] text-base text-paper-2">Registre o dia da granja em poucos toques e veja o que precisa de atenção.</p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </motion.aside>
  )
}
