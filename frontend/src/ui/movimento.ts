import type { Transition, Variants } from 'motion/react'

// durações e variantes compartilhadas: toda animação de interface sai daqui,
// pra a sensação ser a mesma em qualquer tela. só transform e opacity, que não causam reflow

export const tempos = {
  entrada: 0.25,
  saida: 0.12,
  barra: 0.6,
  // atraso entre um item e o próximo numa lista
  passo: 0.035,
} as const

// listas longas só animam os primeiros itens. o resto já entra pronto
export const MAX_ITENS_ANIMADOS = 10

export const suave: Transition = { duration: tempos.entrada, ease: 'easeOut' }
export const rapida: Transition = { duration: tempos.saida, ease: 'easeIn' }

// página inteira: fade + subida curta
export const entrada: Variants = {
  oculto: { opacity: 0, y: 8 },
  visivel: { opacity: 1, y: 0, transition: suave },
  saida: { opacity: 0, transition: rapida },
}

// item de lista: o `custom` é a posição na lista, que vira atraso (em cascata)
export const item: Variants = {
  oculto: { opacity: 0, y: 8 },
  visivel: (indice: number) => ({
    opacity: 1,
    y: 0,
    transition: { ...suave, delay: Math.min(indice, MAX_ITENS_ANIMADOS - 1) * tempos.passo },
  }),
}

// espalha no motion.div do item: <motion.li {...propsItem(i)}>
export function propsItem(indice: number) {
  return { variants: item, custom: indice, initial: 'oculto', animate: 'visivel' } as const
}

// página: espalha no motion.div da raiz da rota
export const propsPagina = { variants: entrada, initial: 'oculto', animate: 'visivel', exit: 'saida' } as const
