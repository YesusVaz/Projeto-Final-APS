import { AnimatePresence, motion } from 'motion/react'
import { useId, type ReactNode } from 'react'
import { rapida, suave } from './movimento'
import { useDialogo } from './useDialogo'

type DialogoProps = {
  aberto: boolean
  onFechar: () => void
  titulo: string
  descricao?: string
  children?: ReactNode
  // botoes de decisao, alinhados a direita
  rodape: ReactNode
}

// confirmacao curta no meio da tela (inativar usuario, encerrar lote...)
export function Dialogo({ aberto, onFechar, titulo, descricao, children, rodape }: DialogoProps) {
  const idTitulo = useId()
  const dialogo = useDialogo(aberto, onFechar)

  return (
    <dialog
      {...dialogo.props}
      aria-labelledby={idTitulo}
      className="dialogo m-auto w-[420px] max-w-[calc(100vw-2rem)] rounded-3xl border border-line-card bg-surface p-0 text-ink shadow-[0_24px_70px_rgb(11_11_11/0.2)]"
    >
      <AnimatePresence onExitComplete={dialogo.fecharNativo}>
        {aberto ? (
          <motion.div
            key="conteudo"
            initial={{ opacity: 0, y: 8, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1, transition: suave }}
            exit={{ opacity: 0, transition: rapida }}
            className="p-6"
          >
            <h2 id={idTitulo} className="text-lg font-medium tracking-[-0.015em]">
              {titulo}
            </h2>
            {descricao ? <p className="mt-2 text-ink-3">{descricao}</p> : null}
            {children ? <div className="mt-4">{children}</div> : null}
            <div className="mt-6 flex justify-end gap-2.5">{rodape}</div>
          </motion.div>
        ) : null}
      </AnimatePresence>
    </dialog>
  )
}
