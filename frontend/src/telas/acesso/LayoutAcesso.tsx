import { motion } from 'motion/react'
import type { ReactNode } from 'react'
import { Link } from 'react-router'
import { cn, Marca, propsPagina } from '../../ui'

type LayoutAcessoProps = {
  atual: 'entrar' | 'cadastro'
  titulo: string
  descricao: string
  // frase grande do painel escuro
  chamada: string
  apoio: string
  children: ReactNode
}

// alturas das barrinhas decorativas do painel escuro. a azul e a "do dia"
const BARRAS = [60, 96, 78, 132, 110, 168, 140]

const ABAS = [
  { id: 'entrar', caminho: '/entrar', rotulo: 'Entrar' },
  { id: 'cadastro', caminho: '/cadastro', rotulo: 'Criar conta' },
] as const

// moldura das duas telas publicas: painel escuro de um lado, formulario do outro
export function LayoutAcesso({ atual, titulo, descricao, chamada, apoio, children }: LayoutAcessoProps) {
  return (
    <motion.div {...propsPagina} className="mx-auto flex min-h-dvh w-full max-w-[1280px] flex-wrap gap-4 p-4">
      <aside className="relative hidden min-h-[640px] flex-[1_1_440px] flex-col justify-between overflow-hidden rounded-[32px] bg-ink p-10 text-paper md:flex">
        <Marca className="[&>span]:bg-paper [&>span]:text-ink" />
        <div aria-hidden="true" className="grid h-[180px] grid-cols-7 items-end gap-2.5">
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
          <p className="max-w-[14ch] text-[34px] leading-[1.1] font-medium tracking-[-0.035em]">{chamada}</p>
          <p className="mt-3.5 max-w-[36ch] text-[15px] text-paper-2">{apoio}</p>
        </div>
      </aside>

      <section className="flex flex-[1_1_420px] items-center justify-center px-2 py-10 md:px-6">
        <div className="w-full max-w-[400px]">
          <Marca className="mb-8 md:hidden" />
          <nav aria-label="Acesso" className="flex justify-center">
            <div className="inline-flex rounded-full bg-trilho p-1">
              {ABAS.map((aba) => (
                <Link
                  key={aba.id}
                  to={aba.caminho}
                  aria-current={atual === aba.id ? 'page' : undefined}
                  className={cn(
                    'flex h-11 items-center rounded-full px-5 text-sm transition-colors',
                    atual === aba.id ? 'bg-surface font-medium text-ink shadow-sm' : 'text-ink-3 hover:text-ink',
                  )}
                >
                  {aba.rotulo}
                </Link>
              ))}
            </div>
          </nav>
          <h1 data-titulo-pagina tabIndex={-1} className="mt-9 text-[34px] font-medium tracking-[-0.035em] outline-none">
            {titulo}
          </h1>
          <p className="mt-2 text-[15px] text-ink-3">{descricao}</p>
          {children}
        </div>
      </section>
    </motion.div>
  )
}
