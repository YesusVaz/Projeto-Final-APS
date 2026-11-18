import { AnimatePresence } from 'motion/react'
import { useCallback, useRef, useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { ErroApi } from '../../api/http'
import { entrar as entrarNaApi, type Sessao } from '../../api/sessao'
import { useSessao } from '../../sessao/sessaoContexto'
import { Aviso, Botao, Campo } from '../../ui'
import { CabecalhoAcesso } from './CabecalhoAcesso'
import { EntradaAcesso, OlhoSenha } from './CampoAcesso'
import { TransicaoOvo, type FaseOvo } from './TransicaoOvo'

type EstadoLocal = { aviso?: string; de?: string } | null

// o ovo precisa aparecer por um tempo minimo, senao pisca quando a API responde rapido
const MS_MINIMO_CARREGANDO = 700

const esperar = (ms: number) => new Promise<void>((resolver) => setTimeout(resolver, ms))

export function Entrar() {
  const { sessao, entrar } = useSessao()
  const navegar = useNavigate()
  const estado = useLocation().state as EstadoLocal

  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  const [verSenha, setVerSenha] = useState(false)
  const [fase, setFase] = useState<FaseOvo | null>(null)
  const [erro, setErro] = useState('')
  // a sessao so e gravada quando o ovo termina de abrir. antes disso a tela de acesso continua de pe
  const nova = useRef<Sessao | null>(null)
  const destino = estado?.de ?? '/'

  const concluir = useCallback(() => {
    if (!nova.current) return
    entrar(nova.current)
    navegar(destino, { replace: true })
  }, [entrar, navegar, destino])

  if (sessao && !fase) return <Navigate to="/" replace />

  async function enviar(e: FormEvent) {
    e.preventDefault()
    if (fase) return
    if (!login.trim() || !senha) {
      setErro('Informe o usuário e a senha.')
      return
    }
    setFase('carregando')
    setErro('')
    const [resposta] = await Promise.all([entrarNaApi(login.trim(), senha).catch((falha: unknown) => falha), esperar(MS_MINIMO_CARREGANDO)])
    if (resposta instanceof Error || !resposta) {
      setErro(resposta instanceof ErroApi ? resposta.message : 'Não foi possível entrar. Tente de novo.')
      setFase(null)
      return
    }
    nova.current = resposta as Sessao
    setFase('abrindo')
  }

  return (
    <>
      <CabecalhoAcesso titulo="Entrar" descricao="Use o usuário e a senha da sua conta." />
      <form onSubmit={(e) => void enviar(e)} noValidate inert={fase !== null} className="mt-8 flex flex-col gap-5">
        {estado?.aviso ? <Aviso tom="info">{estado.aviso}</Aviso> : null}
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Usuário">
          {(c) => (
            <EntradaAcesso
              {...c}
              icone="usuario"
              value={login}
              onChange={(e) => setLogin(e.target.value)}
              placeholder="seu.usuario"
              autoComplete="username"
            />
          )}
        </Campo>
        <Campo rotulo="Senha">
          {(c) => (
            <EntradaAcesso
              {...c}
              icone="cadeado"
              type={verSenha ? 'text' : 'password'}
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              placeholder="Sua senha"
              autoComplete="current-password"
              fim={<OlhoSenha visivel={verSenha} onAlternar={() => setVerSenha((v) => !v)} />}
            />
          )}
        </Campo>
        <Botao type="submit" tamanho="xl" iconeDireita="seta" className="mt-2 w-full">
          Entrar
        </Botao>
      </form>
      <p inert={fase !== null} className="mt-6 text-center text-[15px] text-ink-3">
        Ainda não tem conta?{' '}
        <Link to="/cadastro" className="rounded font-medium text-accent hover:underline hover:underline-offset-4">
          Criar conta
        </Link>
      </p>
      <AnimatePresence>{fase ? <TransicaoOvo fase={fase} aoConcluir={concluir} /> : null}</AnimatePresence>
    </>
  )
}
