import { useEffect, useRef, useState } from 'react'
import { ErroApi } from '../api/http'
import { useLogado, useSessao } from '../sessao/sessaoContexto'

export type Carga<T> =
  | { situacao: 'carregando' }
  | { situacao: 'erro'; mensagem: string }
  | { situacao: 'pronto'; dados: T }

// resultado de useCarregar, pra passar pra componentes filhos
export type Busca<T> = { carga: Carga<T>; recarregar: () => void }

// busca dados da API com o token da sessao. `chave` muda => busca de novo (ex.: pathname).
// se a tela mostra dados de um id, monte ela com key={id}: assim nao aparece o dado do id anterior
// recarregar() busca de novo sem piscar o esqueleto: os dados antigos ficam na tela ate chegar
// os novos. 401 cai no aoErro da sessao e a tela nem chega a ver o erro
export function useCarregar<T>(buscar: (token: string) => Promise<T>, chave: string | number = '') {
  const { token } = useLogado()
  const { aoErro } = useSessao()
  const [carga, setCarga] = useState<Carga<T>>({ situacao: 'carregando' })
  const [tentativa, setTentativa] = useState(0)
  // a funcao muda a cada render. a ref evita refazer a busca por isso
  const buscarRef = useRef(buscar)
  useEffect(() => {
    buscarRef.current = buscar
  })

  useEffect(() => {
    let vivo = true
    buscarRef.current(token).then(
      (dados) => {
        if (vivo) setCarga({ situacao: 'pronto', dados })
      },
      (erro: unknown) => {
        if (!vivo || aoErro(erro)) return
        const mensagem = erro instanceof ErroApi ? erro.message : 'Não foi possível carregar.'
        setCarga({ situacao: 'erro', mensagem })
      },
    )
    return () => {
      vivo = false
    }
  }, [token, chave, tentativa, aoErro])

  return {
    carga,
    // tentar de novo depois de erro mostra o esqueleto outra vez
    recarregar: () => {
      setCarga((atual) => (atual.situacao === 'erro' ? { situacao: 'carregando' } : atual))
      setTentativa((n) => n + 1)
    },
  }
}
