import { motion } from 'motion/react'
import { useEffect } from 'react'

export type FaseOvo = 'carregando' | 'abrindo'

type TransicaoOvoProps = {
  fase: FaseOvo
  // chamada quando o ovo termina de abrir: hora de mostrar a home
  aoConcluir: () => void
}

const OVO = 'M60 8C88 8 108 62 108 88a48 48 0 0 1-96 0C12 62 32 8 60 8z'
// a trinca em zigue-zague atravessa o ovo. as duas metades se recortam por ela
const TRINCA = 'M8 82L14 80L30 70L44 88L60 72L76 90L90 72L106 82L112 80'
const RECORTE_TOPO = '-20,-20 140,-20 140,80 112,80 106,82 90,72 76,90 60,72 44,88 30,70 14,80 8,82 -20,82'
const RECORTE_BASE = '-20,82 8,82 14,80 30,70 44,88 60,72 76,90 90,72 106,82 112,80 140,80 140,160 -20,160'

// duracao da abertura: trinca (0.4s), metades se afastando (ate ~1s), sumiço
const MS_ABERTURA = 1250

const estiloMetade = { transformBox: 'fill-box', transformOrigin: 'center' } as const

// tela cheia: o ovo balanca enquanto o login responde. quando a resposta chega ele trinca, abre e
// some. so entao o app mostra a home
export function TransicaoOvo({ fase, aoConcluir }: TransicaoOvoProps) {
  const abrindo = fase === 'abrindo'

  useEffect(() => {
    if (!abrindo) return
    const id = window.setTimeout(aoConcluir, MS_ABERTURA)
    return () => window.clearTimeout(id)
  }, [abrindo, aoConcluir])

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.25 }}
      className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-paper"
    >
      <motion.div
        initial={{ scale: 0.7, opacity: 0 }}
        animate={abrindo ? { scale: 1, opacity: [1, 1, 0], transition: { duration: 1.2, times: [0, 0.78, 1] } } : { scale: 1, opacity: 1 }}
        transition={{ duration: 0.3, ease: 'easeOut' }}
        className="flex flex-col items-center"
      >
        <motion.div
          animate={
            abrindo
              ? { rotate: [0, -5, 5, -3, 3, 0], y: 0, transition: { duration: 0.4 } }
              : { rotate: [0, -7, 7, -5, 5, 0], y: [0, -8, 0], transition: { duration: 1.3, repeat: Infinity, ease: 'easeInOut' } }
          }
        >
          <svg viewBox="0 0 120 140" width={132} height={154} aria-hidden="true" overflow="visible">
            <defs>
              <clipPath id="ovo-topo">
                <polygon points={RECORTE_TOPO} />
              </clipPath>
              <clipPath id="ovo-base">
                <polygon points={RECORTE_BASE} />
              </clipPath>
            </defs>

            <motion.g
              style={estiloMetade}
              animate={abrindo ? { y: 30, x: 8, rotate: 14 } : { y: 0, x: 0, rotate: 0 }}
              transition={abrindo ? { delay: 0.45, duration: 0.5, ease: 'easeOut' } : { duration: 0 }}
            >
              <g clipPath="url(#ovo-base)">
                <path d={OVO} className="fill-ink" />
              </g>
            </motion.g>

            <motion.g
              style={estiloMetade}
              animate={abrindo ? { y: -30, x: -8, rotate: -14 } : { y: 0, x: 0, rotate: 0 }}
              transition={abrindo ? { delay: 0.45, duration: 0.5, ease: 'easeOut' } : { duration: 0 }}
            >
              <g clipPath="url(#ovo-topo)">
                <path d={OVO} className="fill-ink" />
                <path d="M34 56C38 38 48 26 58 20" fill="none" strokeWidth={3} strokeLinecap="round" className="stroke-paper/25" />
              </g>
              <motion.path
                d={TRINCA}
                fill="none"
                strokeWidth={3}
                strokeLinecap="round"
                strokeLinejoin="round"
                className="stroke-paper"
                initial={{ pathLength: 0 }}
                animate={{ pathLength: abrindo ? 1 : 0 }}
                transition={abrindo ? { duration: 0.4, ease: 'easeOut' } : { duration: 0 }}
              />
            </motion.g>
          </svg>
        </motion.div>

        <motion.div
          aria-hidden="true"
          animate={abrindo ? { scaleX: 1.15, opacity: 0.6 } : { scaleX: [1, 0.78, 1], opacity: [1, 0.6, 1] }}
          transition={abrindo ? { duration: 0.3 } : { duration: 1.3, repeat: Infinity, ease: 'easeInOut' }}
          className="mt-3 h-2 w-20 rounded-full bg-ink/10 blur-[3px]"
        />
        <p role="status" className="mt-7 text-sm text-ink-3">
          {abrindo ? 'Tudo pronto.' : 'Entrando na granja...'}
        </p>
      </motion.div>
    </motion.div>
  )
}
