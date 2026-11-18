import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router'
import { ErroApi } from '../api/http'
import { sair as sairNaApi, type Sessao } from '../api/sessao'
import { SessaoContexto, type ContextoSessao } from './sessaoContexto'

const CHAVE = 'granja.sessao'

// sessionStorage some ao fechar a aba. recarregar a pagina mantem a sessao
function lerGuardada(): Sessao | null {
  try {
    const texto = sessionStorage.getItem(CHAVE)
    return texto ? (JSON.parse(texto) as Sessao) : null
  } catch {
    return null
  }
}

function guardar(sessao: Sessao | null) {
  try {
    if (sessao) sessionStorage.setItem(CHAVE, JSON.stringify(sessao))
    else sessionStorage.removeItem(CHAVE)
  } catch {
    // modo privado ou storage cheio: a sessao fica so na memoria
  }
}

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(lerGuardada)
  const [aviso, setAviso] = useState<string | null>(null)
  const navegar = useNavigate()
  // o navigate do router muda a cada rota. na ref ele nao derruba as callbacks (nem refaz as buscas)
  const navegarRef = useRef(navegar)
  useEffect(() => {
    navegarRef.current = navegar
  })

  const entrar = useCallback((nova: Sessao) => {
    guardar(nova)
    setAviso(null)
    setSessao(nova)
  }, [])

  const sair = useCallback(async () => {
    if (sessao) await sairNaApi(sessao.token)
    guardar(null)
    setAviso(null)
    setSessao(null)
    navegarRef.current('/entrar', { replace: true })
  }, [sessao])

  const aoErro = useCallback(
    (erro: unknown) => {
      if (!(erro instanceof ErroApi) || erro.status !== 401) return false
      guardar(null)
      // quem redireciona e a RotaProtegida, levando o aviso. navegar daqui brigaria com ela
      setAviso('Sua sessão expirou. Entre de novo.')
      setSessao(null)
      return true
    },
    [],
  )

  const valor = useMemo<ContextoSessao>(
    () => ({ sessao, perfil: sessao?.perfil ?? null, aviso, entrar, sair, aoErro }),
    [sessao, aviso, entrar, sair, aoErro],
  )

  return <SessaoContexto value={valor}>{children}</SessaoContexto>
}
