import { motion } from 'motion/react'
import type { HTMLAttributes, ReactNode } from 'react'
import { Link } from 'react-router'
import { cn } from './cn'
import { Icone } from './Icone'
import { propsItem } from './movimento'
import { NumeroAnimado } from './NumeroAnimado'

// moldura da pagina: largura maxima e respiro dos lados
export function Pagina({ className, ...resto }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('relative mx-auto w-full max-w-[1440px] px-6 pb-20 md:px-12 2xl:px-16', className)} {...resto} />
}

type CabecalhoPaginaProps = {
  // caminho pequeno acima do titulo, tipo "Lotes / LOT-2026-003"
  trilha?: ReactNode
  titulo: string
  descricao?: string
  acoes?: ReactNode
}

// o h1 e focavel (tabIndex -1) pra o layout mandar o foco pra ele a cada troca de rota
export function CabecalhoPagina({ trilha, titulo, descricao, acoes }: CabecalhoPaginaProps) {
  return (
    <section className="flex flex-wrap items-end justify-between gap-6 pt-10 pb-8 md:pt-14">
      <div>
        {trilha ? <p className="text-[13.5px] text-ink-3">{trilha}</p> : null}
        <h1 data-titulo-pagina tabIndex={-1} className="mt-2 text-[32px] leading-[1.05] font-medium tracking-[-0.035em] outline-none md:text-[40px]">
          {titulo}
        </h1>
        {descricao ? <p className="mt-3 max-w-[52ch] text-[15px] text-ink-3">{descricao}</p> : null}
      </div>
      {acoes ? <div className="flex flex-wrap items-center gap-2.5">{acoes}</div> : null}
    </section>
  )
}

// o cartao e raro de proposito: so onde a pessoa precisa decidir ou preencher algo
export function Cartao({ className, ...resto }: HTMLAttributes<HTMLElement>) {
  return <section className={cn('rounded-3xl border border-line-card bg-surface p-6', className)} {...resto} />
}

// titulo pequeno de secao
export function TituloSecao({ className, ...resto }: HTMLAttributes<HTMLHeadingElement>) {
  return <h2 className={cn('text-[13px] font-medium text-ink-3', className)} {...resto} />
}

type ItemListaProps = {
  // link interno (router). sem recarregar a pagina
  to?: string
  href?: string
  className?: string
  children: ReactNode
}

// linha de lista com divisoria fina. vira link quando recebe to ou href
export function ItemLista({ to, href, className, children }: ItemListaProps) {
  const classes = cn(
    'flex items-center gap-3.5 border-t border-line-soft py-[15px] transition-colors first:border-t-0',
    (to || href) && 'hover:bg-ink/[0.025]',
    className,
  )
  if (to) {
    return (
      <Link to={to} className={classes}>
        {children}
      </Link>
    )
  }
  return href ? (
    <a href={href} className={classes}>
      {children}
    </a>
  ) : (
    <div className={classes}>{children}</div>
  )
}

type EstatisticaProps = {
  // numero sobe de 0 ate o valor. texto aparece como veio
  valor: string | number
  rotulo: string
  descricao?: string
  // posicao na fila de entrada
  indice?: number
  // grande (52px) no inicio; medio (40px) nos detalhes
  tamanho?: 'grande' | 'medio'
}

// numero grande de resumo, solto no fundo
export function Estatistica({ valor, rotulo, descricao, indice = 0, tamanho = 'grande' }: EstatisticaProps) {
  return (
    <motion.div {...propsItem(indice)}>
      <p
        className={cn(
          'font-mono leading-none tabular-nums',
          tamanho === 'medio' ? 'text-[40px] tracking-[-0.04em]' : 'text-[40px] tracking-[-0.045em] md:text-[52px]',
        )}
      >
        {typeof valor === 'number' ? <NumeroAnimado valor={valor} /> : valor}
      </p>
      <p className={cn('font-medium', tamanho === 'medio' ? 'mt-2' : 'mt-2.5')}>{rotulo}</p>
      {descricao ? <p className={cn('text-ink-3', tamanho === 'medio' ? 'text-[13px]' : 'text-[13.5px]')}>{descricao}</p> : null}
    </motion.div>
  )
}

export function Marca({ className }: { className?: string }) {
  return (
    <span className={cn('inline-flex items-center gap-2.5 text-[17px] font-semibold tracking-[-0.02em]', className)}>
      <span className="flex size-[30px] items-center justify-center rounded-[10px] bg-ink text-paper">
        <Icone nome="ovo" tamanho={16} />
      </span>
      Granja
    </span>
  )
}

export function Avatar({ iniciais, className }: { iniciais: string; className?: string }) {
  return (
    <span className={cn('flex size-8 shrink-0 items-center justify-center rounded-full bg-barra-fundo text-xs font-medium text-ink-hover', className)}>
      {iniciais}
    </span>
  )
}
