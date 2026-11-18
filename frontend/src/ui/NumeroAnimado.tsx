import { animate, motion, useMotionValue, useReducedMotion, useTransform } from 'motion/react'
import { useEffect } from 'react'
import { tempos } from './movimento'

// numero que sobe de 0 ate o valor. com "reduzir movimento" ja aparece no valor final
export function NumeroAnimado({ valor }: { valor: number }) {
  const reduzir = useReducedMotion()
  const corrente = useMotionValue(reduzir ? valor : 0)
  const texto = useTransform(corrente, (v) => Math.round(v).toLocaleString('pt-BR'))

  useEffect(() => {
    const controle = animate(corrente, valor, { duration: reduzir ? 0 : tempos.barra + 0.2, ease: 'easeOut' })
    return () => controle.stop()
  }, [corrente, valor, reduzir])

  return <motion.span>{texto}</motion.span>
}
