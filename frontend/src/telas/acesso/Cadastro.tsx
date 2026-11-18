import { useRef, useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { ErroApi } from '../../api/http'
import { cadastrarPublico } from '../../api/usuarios'
import { useSessao } from '../../sessao/sessaoContexto'
import { Aviso, Botao, Campo, Entrada, useToast } from '../../ui'
import { LayoutAcesso } from './LayoutAcesso'

// mesmas regras do backend
const LOGIN_VALIDO = /^[A-Za-z0-9._-]{3,30}$/
const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const MAX_CARACTERES_EMAIL = 254
const MAX_CARACTERES_NOME = 100
// o bcrypt so le os 72 primeiros bytes da senha. passar disso cortaria a senha sem avisar
const MAX_BYTES_SENHA = 72

type Erros = Partial<Record<'login' | 'email' | 'nome' | 'senha' | 'confirma', string>>

export function Cadastro() {
  const { sessao } = useSessao()
  const navegar = useNavigate()
  const avisar = useToast()
  const formulario = useRef<HTMLFormElement>(null)

  const [login, setLogin] = useState('')
  const [email, setEmail] = useState('')
  const [nome, setNome] = useState('')
  const [senha, setSenha] = useState('')
  const [confirma, setConfirma] = useState('')
  const [erros, setErros] = useState<Erros>({})
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (sessao) return <Navigate to="/" replace />

  function validar(): Erros {
    const novos: Erros = {}
    if (!LOGIN_VALIDO.test(login.trim())) novos.login = 'Use de 3 a 30 letras, números, ponto, traço ou sublinhado.'
    const emailLimpo = email.trim()
    if (!EMAIL_VALIDO.test(emailLimpo) || emailLimpo.length > MAX_CARACTERES_EMAIL) novos.email = 'Informe um e-mail válido.'
    if (!nome.trim()) novos.nome = 'Informe o nome.'
    else if (nome.trim().length > MAX_CARACTERES_NOME) novos.nome = 'O nome passou de 100 caracteres.'
    if (senha.length < 6) novos.senha = 'A senha precisa ter pelo menos 6 caracteres.'
    else if (new TextEncoder().encode(senha).length > MAX_BYTES_SENHA) novos.senha = 'A senha é longa demais.'
    if (confirma !== senha) novos.confirma = 'As senhas não conferem.'
    return novos
  }

  async function enviar(e: FormEvent) {
    e.preventDefault()
    if (enviando) return
    const novos = validar()
    setErros(novos)
    setFalha('')
    if (Object.keys(novos).length > 0) {
      // espera o React pintar o aria-invalid pra achar o primeiro campo com erro
      requestAnimationFrame(() => formulario.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus())
      return
    }
    setEnviando(true)
    try {
      await cadastrarPublico({ login: login.trim(), email: email.trim(), nome: nome.trim(), senha })
      avisar({ texto: 'Conta criada. Agora é só entrar.' })
      navegar('/entrar', { replace: true })
    } catch (erro) {
      setFalha(erro instanceof ErroApi ? erro.message : 'Não foi possível criar a conta. Tente de novo.')
      setEnviando(false)
    }
  }

  return (
    <LayoutAcesso
      atual="cadastro"
      titulo="Criar conta"
      descricao="Sua conta começa como funcionário. O gestor pode mudar o perfil depois."
      chamada="Comece pelo registro do dia."
      apoio="Depois de criar a conta, entre e veja os lotes que você acompanha."
    >
      <form ref={formulario} onSubmit={(e) => void enviar(e)} noValidate className="mt-6 flex flex-col gap-4">
        {falha ? <Aviso tom="critico">{falha}</Aviso> : null}
        <Campo rotulo="Nome completo" erro={erros.nome}>
          {(c) => <Entrada {...c} value={nome} onChange={(e) => setNome(e.target.value)} placeholder="Como no seu documento" autoComplete="name" />}
        </Campo>
        <Campo rotulo="E-mail" erro={erros.email}>
          {(c) => (
            <Entrada {...c} type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="voce@exemplo.com" autoComplete="email" />
          )}
        </Campo>
        <Campo rotulo="Usuário" dica="É o que você digita para entrar." erro={erros.login}>
          {(c) => <Entrada {...c} value={login} onChange={(e) => setLogin(e.target.value)} placeholder="seu.usuario" autoComplete="username" />}
        </Campo>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo rotulo="Senha" erro={erros.senha}>
            {(c) => <Entrada {...c} type="password" value={senha} onChange={(e) => setSenha(e.target.value)} autoComplete="new-password" />}
          </Campo>
          <Campo rotulo="Repetir senha" erro={erros.confirma}>
            {(c) => <Entrada {...c} type="password" value={confirma} onChange={(e) => setConfirma(e.target.value)} autoComplete="new-password" />}
          </Campo>
        </div>
        <Botao type="submit" tamanho="lg" disabled={enviando} className="mt-1.5 w-full">
          {enviando ? 'Criando...' : 'Criar conta'}
        </Botao>
      </form>
    </LayoutAcesso>
  )
}
