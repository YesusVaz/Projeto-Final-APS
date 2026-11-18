import { useEffect, useRef, useState } from 'react'

// largura em pixels do elemento, pra desenhar o SVG no tamanho real e o texto nao ficar encolhido
export function useLargura<T extends HTMLElement>() {
  const ref = useRef<T>(null)
  const [largura, setLargura] = useState(0)
  useEffect(() => {
    const el = ref.current
    if (!el) return
    setLargura(el.clientWidth)
    const observador = new ResizeObserver(([entrada]) => setLargura(Math.round(entrada.contentRect.width)))
    observador.observe(el)
    return () => observador.disconnect()
  }, [])
  return { ref, largura }
}
