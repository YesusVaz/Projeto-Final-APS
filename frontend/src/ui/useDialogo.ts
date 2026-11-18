import { useEffect, useRef, type MouseEvent, type SyntheticEvent } from 'react'

// liga o estado "aberto" ao <dialog> nativo. showModal prende o foco e da o fundo escuro.
// o conteudo anima a saida com Motion (AnimatePresence), entao o dialog so fecha de verdade
// quando a animacao acaba: quem usa o hook chama `fecharNativo` no onExitComplete
export function useDialogo(aberto: boolean, aoFechar: () => void) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialogo = ref.current
    if (aberto && dialogo && !dialogo.open) dialogo.showModal()
  }, [aberto])

  // trava a rolagem da pagina enquanto o dialogo esta aberto
  useEffect(() => {
    if (!aberto) return
    const antes = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = antes
    }
  }, [aberto])

  return {
    props: {
      ref,
      onClose: aoFechar,
      // Esc passa pelo estado, pra a animacao de saida rodar antes de fechar
      onCancel: (e: SyntheticEvent<HTMLDialogElement>) => {
        e.preventDefault()
        aoFechar()
      },
      // clicar no fundo escuro tambem fecha: la o alvo do clique e o proprio dialog
      onClick: (e: MouseEvent<HTMLDialogElement>) => {
        if (e.target === e.currentTarget) aoFechar()
      },
    },
    fecharNativo: () => ref.current?.close(),
  }
}
