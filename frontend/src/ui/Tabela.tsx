import type { HTMLAttributes, TdHTMLAttributes, ThHTMLAttributes } from 'react'
import { cn } from './cn'

// tabela solta no fundo, so com linhas finas. nada de cartao em volta
export function Tabela({ className, ...resto }: HTMLAttributes<HTMLTableElement>) {
  return (
    <div className="w-full overflow-x-auto">
      <table className={cn('w-full border-collapse', className)} {...resto} />
    </div>
  )
}

export function TabelaCabeca(props: HTMLAttributes<HTMLTableSectionElement>) {
  return <thead {...props} />
}

export function TabelaCorpo(props: HTMLAttributes<HTMLTableSectionElement>) {
  return <tbody {...props} />
}

export function Linha({ className, ...resto }: HTMLAttributes<HTMLTableRowElement>) {
  return <tr className={cn('transition-colors hover:bg-ink/[0.028]', className)} {...resto} />
}

type NumericaProps = { numerica?: boolean }

export function Cab({ numerica, className, ...resto }: ThHTMLAttributes<HTMLTableCellElement> & NumericaProps) {
  return (
    <th
      scope="col"
      className={cn('border-b border-line pr-3.5 pb-2.5 text-left text-[12.5px] font-medium whitespace-nowrap text-ink-3 last:pr-0', numerica && 'text-right', className)}
      {...resto}
    />
  )
}

export function Cel({ numerica, className, ...resto }: TdHTMLAttributes<HTMLTableCellElement> & NumericaProps) {
  return (
    <td
      className={cn('border-b border-line-soft py-4 pr-3.5 align-middle last:pr-0', numerica && 'text-right font-mono tabular-nums', className)}
      {...resto}
    />
  )
}
