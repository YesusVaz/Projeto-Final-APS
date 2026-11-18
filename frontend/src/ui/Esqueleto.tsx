import { cn } from './cn'

// bloco cinza pulsando no lugar do conteudo. o aria-hidden evita que o leitor de tela leia ruido
export function Esqueleto({ className }: { className?: string }) {
  return <div aria-hidden="true" className={cn('animate-pulse rounded-xl bg-trilho', className)} />
}

// varias linhas de lista. o role=status avisa o leitor de tela uma vez so
export function EsqueletoLista({ linhas = 4, className }: { linhas?: number; className?: string }) {
  return (
    <div role="status" aria-label="Carregando" className={cn('flex flex-col gap-3', className)}>
      {Array.from({ length: linhas }, (_, i) => (
        <Esqueleto key={i} className="h-14" />
      ))}
    </div>
  )
}
