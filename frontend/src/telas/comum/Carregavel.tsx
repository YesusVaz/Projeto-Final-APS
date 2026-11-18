import type { ReactNode } from 'react'
import type { Carga } from '../../hooks/useCarregar'
import { EstadoErro } from '../../ui'

type CarregavelProps<T> = {
  carga: Carga<T>
  recarregar: () => void
  // o que aparece enquanto busca. use Esqueleto, nunca spinner solto
  esqueleto: ReactNode
  children: (dados: T) => ReactNode
}

// os tres estados de uma busca num lugar so: carregando, erro com "Tentar de novo" e pronto
export function Carregavel<T>({ carga, recarregar, esqueleto, children }: CarregavelProps<T>) {
  if (carga.situacao === 'carregando') return esqueleto
  if (carga.situacao === 'erro') return <EstadoErro mensagem={carga.mensagem} onTentar={recarregar} />
  return children(carga.dados)
}
