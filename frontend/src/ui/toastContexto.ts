import { createContext, useContext } from 'react'

export type PedidoToast = {
  texto: string
  // botao opcional, tipo "Desfazer". so vale enquanto o aviso estiver na tela
  acao?: { rotulo: string; aoClicar: () => void }
  // milissegundos. padrao 5000
  duracao?: number
}

export type Avisar = (pedido: PedidoToast) => void

export const ToastContexto = createContext<Avisar>(() => {})

// uso: const avisar = useToast(); avisar({ texto: 'Mortalidade registrada' })
export function useToast(): Avisar {
  return useContext(ToastContexto)
}
