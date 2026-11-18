import { AnimatePresence, motion } from 'motion/react'
import { useCallback, useRef, useState, type ReactNode } from 'react'
import { Icone } from './Icone'
import { rapida, suave } from './movimento'
import { ToastContexto, type PedidoToast } from './toastContexto'

type ToastNaTela = PedidoToast & { id: number }

// coloque uma vez perto da raiz. quem estiver embaixo chama useToast() pra avisar
export function ToastProvider({ children }: { children: ReactNode }) {
  const [lista, setLista] = useState<ToastNaTela[]>([])
  const proximo = useRef(1)

  const tirar = useCallback((id: number) => {
    setLista((atual) => atual.filter((t) => t.id !== id))
  }, [])

  const avisar = useCallback(
    (pedido: PedidoToast) => {
      const id = proximo.current++
      setLista((atual) => [...atual, { ...pedido, id }])
      window.setTimeout(() => tirar(id), pedido.duracao ?? 5000)
    },
    [tirar],
  )

  return (
    <ToastContexto value={avisar}>
      {children}
      <div
        aria-live="polite"
        className="pointer-events-none fixed bottom-24 left-1/2 z-50 flex -translate-x-1/2 flex-col items-center gap-2 md:bottom-6"
      >
        <AnimatePresence>
          {lista.map((t) => (
            <motion.div
              key={t.id}
              layout
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0, transition: suave }}
              exit={{ opacity: 0, transition: rapida }}
              className="pointer-events-auto inline-flex items-center gap-3 rounded-[18px] bg-ink py-3 pr-[18px] pl-3.5 text-paper shadow-[0_14px_40px_rgb(11_11_11/0.22)]"
            >
              <Icone nome="ok" tamanho={18} />
              <span>{t.texto}</span>
              {t.acao ? (
                <button
                  type="button"
                  onClick={() => {
                    t.acao?.aoClicar()
                    tirar(t.id)
                  }}
                  className="ml-1.5 rounded font-medium text-[#9cc0ff] hover:underline"
                >
                  {t.acao.rotulo}
                </button>
              ) : null}
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </ToastContexto>
  )
}
