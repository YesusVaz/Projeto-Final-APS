import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router'
import { ErroApi } from '../../api/http'
import { entrar as entrarNaApi } from '../../api/sessao'
import { useSessao } from '../../sessao/sessaoContexto'
import { Aviso, Botao, Campo, Entrada } from '../../ui'
import { LayoutAcesso } from './LayoutAcesso'

type EstadoLocal = { aviso?: string; de?: string } | null

export function Entrar() {
  const { sessao, entrar } = useSessao()
  const navegar = useNavigate()
  const estado = useLocation().state as EstadoLocal

  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  const [verSenha, setVerSenha] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')

  if (sessao) return <Navigate to="/" replace />

  async function enviar(e: FormEvent) {
    e.preventDefault()
    if (enviando) return
    if (!login.trim() || !senha) {
      setErro('Informe o usuário e a senha.')
      return
    }
    setEnviando(true)
    setErro('')
    try {
      const nova = await entrarNaApi(login.trim(), senha)
      entrar(nova)
      navegar(estado?.de ?? '/', { replace: true })
    } catch (falha) {
      setErro(falha instanceof ErroApi ? falha.message : 'Não foi possível entrar. Tente de novo.')
      setEnviando(false)
    }
  }

  return (
    <LayoutAcesso
      atual="entrar"
      titulo="Entrar"
      descricao="Use o usuário e a senha da sua conta."
      chamada="Lotes, ração e vacinas num lugar só."
      apoio="Registre o dia da granja em poucos toques e veja o que precisa de atenção."
    >
      <form onSubmit={(e) => void enviar(e)} noValidate className="mt-7 flex flex-col gap-[18px]">
        {estado?.aviso ? <Aviso tom="info">{estado.aviso}</Aviso> : null}
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Usuário">
          {(c) => (
            <Entrada {...c} value={login} onChange={(e) => setLogin(e.target.value)} placeholder="seu.usuario" autoComplete="username" />
          )}
        </Campo>
        <Campo
          rotulo="Senha"
          acao={
            <button
              type="button"
              onClick={() => setVerSenha((v) => !v)}
              aria-pressed={verSenha}
              className="inline-flex min-h-11 items-center rounded px-1 text-[13px] text-accent hover:underline"
            >
              {verSenha ? 'Ocultar' : 'Mostrar'}
            </button>
          }
        >
          {(c) => (
            <Entrada
              {...c}
              type={verSenha ? 'text' : 'password'}
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              autoComplete="current-password"
            />
          )}
        </Campo>
        <Botao type="submit" tamanho="lg" disabled={enviando} className="mt-1.5 w-full">
          {enviando ? 'Entrando...' : 'Entrar'}
        </Botao>
      </form>
    </LayoutAcesso>
  )
}
