import { AnimatePresence, motion } from 'motion/react'
import { useId, type ReactNode } from 'react'
import { Botao } from './Botao'
import { rapida, suave } from './movimento'
import { useDialogo } from './useDialogo'

type PainelProps = {
  aberto: boolean
  onFechar: () => void
  titulo: string
  descricao?: string
  children: ReactNode
  // botoes do pe do painel (Cancelar, Salvar)
  rodape?: ReactNode
}

// painel lateral pra criar e editar. a pagina fica escurecida atras
export function Painel({ aberto, onFechar, titulo, descricao, children, rodape }: PainelProps) {
  const idTitulo = useId()
  const dialogo = useDialogo(aberto, onFechar)

  return (
    <dialog
      {...dialogo.props}
      aria-labelledby={idTitulo}
      className="painel fixed inset-y-3 right-3 left-auto m-0 h-[calc(100dvh-1.5rem)] max-h-none w-[440px] max-w-[calc(100vw-1.5rem)] overflow-hidden rounded-[28px] border-0 bg-surface p-0 text-ink shadow-[0_30px_80px_rgb(11_11_11/0.25)]"
    >
      <AnimatePresence onExitComplete={dialogo.fecharNativo}>
        {aberto ? (
          <motion.div
            key="conteudo"
            initial={{ opacity: 0, x: 28 }}
            animate={{ opacity: 1, x: 0, transition: suave }}
            exit={{ opacity: 0, x: 12, transition: rapida }}
            className="flex h-full flex-col gap-5 p-7"
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <h2 id={idTitulo} className="text-[22px] font-medium tracking-[-0.02em]">
                  {titulo}
                </h2>
                {descricao ? <p className="mt-1 text-sm text-ink-3">{descricao}</p> : null}
              </div>
              <Botao variante="fantasma" tamanho="icone" icone="fechar" aria-label="Fechar" onClick={onFechar} className="-mt-1 -mr-2" />
            </div>
            <div className="-mx-1 flex-1 overflow-y-auto px-1">{children}</div>
            {rodape ? <div className="flex justify-end gap-2.5">{rodape}</div> : null}
          </motion.div>
        ) : null}
      </AnimatePresence>
    </dialog>
  )
}
