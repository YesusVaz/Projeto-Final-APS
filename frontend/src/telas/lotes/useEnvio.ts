import { useRef, useState } from 'react'
import { ErroApi } from '../../api/http'
import { useSessao } from '../../sessao/sessaoContexto'

// envio de formulario dentro de dialogo. a ref barra o duplo clique na hora (o estado so
// atualiza no proximo render) e o estado desabilita o botao e troca o texto
export function useEnvio() {
  const { aoErro } = useSessao()
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')
  const emCurso = useRef(false)

  async function enviar(acao: () => Promise<void>): Promise<boolean> {
    if (emCurso.current) return false
    emCurso.current = true
    setEnviando(true)
    setErro('')
    try {
      await acao()
      return true
    } catch (falha) {
      if (!aoErro(falha)) {
        setErro(falha instanceof ErroApi ? falha.message : 'Não foi possível concluir. Tente de novo.')
      }
      return false
    } finally {
      emCurso.current = false
      setEnviando(false)
    }
  }

  return { enviando, erro, enviar }
}
