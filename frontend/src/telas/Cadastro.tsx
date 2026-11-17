import { useState, type FormEvent, type ReactNode } from 'react'
import type { Sessao } from '../api/sessao'
import { ErroApi, type Perfil } from '../api/http'
import { cadastrarPublico, cadastrarUsuario, type UsuarioCriado } from '../api/usuarios'
import { Botao } from '../componentes/Botao'
import { Campo } from '../componentes/Campo'

const PERFIS: Perfil[] = ['gestor', 'tecnico', 'funcionario']

const ROTULOS: Record<Perfil, string> = {
  gestor: 'Gestor',
  tecnico: 'Técnico',
  funcionario: 'Funcionário',
}

// mesmas regras do backend
const LOGIN_VALIDO = /^[A-Za-z0-9._-]{3,30}$/
const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const MAX_CARACTERES_EMAIL = 254
const MAX_CARACTERES_NOME = 100
// o bcrypt so le os 72 primeiros bytes da senha. passar disso cortaria a senha sem avisar
const MAX_BYTES_SENHA = 72

type CampoDoFormulario = 'login' | 'email' | 'nome' | 'senha' | 'confirma'
type Erros = Partial<Record<CampoDoFormulario, string>>

type Criado = {
  usuario: UsuarioCriado
  // cadastro publico nao escolhe perfil. o gestor sim, entao a mensagem muda
  publico: boolean
}

type CadastroProps = {
  sessao: Sessao | null
  onIrEntrar: () => void
  onSessaoInvalida: () => void
}

export function Cadastro({ sessao, onIrEntrar, onSessaoInvalida }: CadastroProps) {
  const [login, setLogin] = useState('')
  const [email, setEmail] = useState('')
  const [nome, setNome] = useState('')
  const [senha, setSenha] = useState('')
  const [confirma, setConfirma] = useState('')
  const [perfil, setPerfil] = useState<Perfil>('funcionario')
  const [ativo, setAtivo] = useState(true)
  const [erros, setErros] = useState<Erros>({})
  const [enviando, setEnviando] = useState(false)
  const [falha, setFalha] = useState<string | null>(null)
  const [criado, setCriado] = useState<Criado | null>(null)

  const gestor = sessao?.perfil === 'gestor'

  function validar(): Erros {
    const novos: Erros = {}
    if (!LOGIN_VALIDO.test(login.trim())) {
      novos.login = 'Use de 3 a 30 letras, números, ponto, traço ou sublinhado.'
    }
    const emailLimpo = email.trim()
    if (!EMAIL_VALIDO.test(emailLimpo) || emailLimpo.length > MAX_CARACTERES_EMAIL) {
      novos.email = 'Informe um e-mail válido.'
    }
    if (!nome.trim()) {
      novos.nome = 'Informe o nome.'
    } else if (nome.trim().length > MAX_CARACTERES_NOME) {
      novos.nome = 'O nome passou de 100 caracteres.'
    }
    if (senha.length < 6) {
      novos.senha = 'A senha precisa ter pelo menos 6 caracteres.'
    } else if (new TextEncoder().encode(senha).length > MAX_BYTES_SENHA) {
      novos.senha = 'A senha é longa demais.'
    }
    if (confirma !== senha) {
      novos.confirma = 'As senhas não conferem.'
    }
    return novos
  }

  function limpar() {
    setLogin('')
    setEmail('')
    setNome('')
    setSenha('')
    setConfirma('')
    setPerfil('funcionario')
    setAtivo(true)
    setErros({})
    setFalha(null)
    setCriado(null)
  }

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()

    const novos = validar()
    setErros(novos)
    setFalha(null)
    setCriado(null)
    if (Object.keys(novos).length > 0) return

    const dados = {
      login: login.trim(),
      email: email.trim(),
      nome: nome.trim(),
      senha,
    }

    setEnviando(true)
    try {
      if (sessao && gestor) {
        const usuario = await cadastrarUsuario(sessao.token, { ...dados, perfil, ativo })
        setCriado({ usuario, publico: false })
      } else {
        const usuario = await cadastrarPublico(dados)
        setCriado({ usuario, publico: true })
      }
      setLogin('')
      setEmail('')
      setNome('')
      setSenha('')
      setConfirma('')
      setErros({})
    } catch (e) {
      if (e instanceof ErroApi && e.status === 401 && gestor) {
        // a sessao do gestor acabou. o formulario continua aqui, so que agora como cadastro publico
        onSessaoInvalida()
        setFalha('Sua sessão terminou. Entre de novo como gestor para escolher o perfil.')
      } else {
        setFalha(e instanceof Error ? e.message : 'Erro inesperado.')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="grid grid-cols-1 gap-10 pt-16 lg:grid-cols-12 lg:gap-6 lg:pt-24">
      <section className="lg:col-span-5">
        <p className="font-mono text-[11px] tracking-wide text-ink-3 uppercase">Usuários · novo cadastro</p>
        <h1 className="mt-3 text-[56px] leading-[0.95] font-light tracking-[-0.045em] md:text-[88px]">
          Criar
          <br />
          conta
        </h1>
        <p className="mt-6 max-w-[40ch] text-[15px] leading-normal text-ink-2">
          Qualquer pessoa pode criar uma conta. Ela entra como Funcionário e já fica ativa.
          {gestor ? ' Como gestor logado, você também escolhe o perfil e o status do usuário.' : null}
        </p>
      </section>

      <section className="lg:col-span-5 lg:col-start-8">
        <div className="flex flex-col gap-7">
          {falha ? <Aviso tom="erro">{falha}</Aviso> : null}
          {criado ? (
            <Aviso tom="ok">
              {criado.publico ? (
                <>
                  <p>Conta criada para {criado.usuario.login}. Agora é só entrar.</p>
                  <div className="mt-4">
                    <Botao variante="linha" onClick={onIrEntrar}>
                      Ir para Entrar
                    </Botao>
                  </div>
                </>
              ) : (
                <p>
                  Usuário {criado.usuario.login} cadastrado como {ROTULOS[criado.usuario.perfil]}
                  {criado.usuario.ativo ? '' : ', inativo'}.
                </p>
              )}
            </Aviso>
          ) : null}

          <form onSubmit={enviar} noValidate>
            <div className="flex flex-col gap-7">
              <Campo id="nome" rotulo="Nome completo" erro={erros.nome}>
                <input
                  id="nome"
                  type="text"
                  autoComplete="name"
                  placeholder="Nome e sobrenome"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  className="campo"
                />
              </Campo>

              <div className="grid grid-cols-1 gap-7 md:grid-cols-2">
                <Campo id="login" rotulo="Usuário" erro={erros.login} dica="Não pode ser alterado depois">
                  <input
                    id="login"
                    type="text"
                    autoComplete="username"
                    placeholder="seu.usuario"
                    value={login}
                    onChange={(e) => setLogin(e.target.value)}
                    className="campo font-mono"
                  />
                </Campo>
                <Campo id="email" rotulo="E-mail" erro={erros.email}>
                  <input
                    id="email"
                    type="email"
                    autoComplete="email"
                    placeholder="voce@exemplo.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="campo font-mono"
                  />
                </Campo>
              </div>

              <div className="grid grid-cols-1 gap-7 md:grid-cols-2">
                <Campo id="senha" rotulo="Senha" erro={erros.senha} dica="Mínimo de 6 caracteres">
                  <input
                    id="senha"
                    type="password"
                    autoComplete="new-password"
                    value={senha}
                    onChange={(e) => setSenha(e.target.value)}
                    className="campo font-mono"
                  />
                </Campo>
                <Campo id="confirma" rotulo="Confirmar senha" erro={erros.confirma}>
                  <input
                    id="confirma"
                    type="password"
                    autoComplete="new-password"
                    value={confirma}
                    onChange={(e) => setConfirma(e.target.value)}
                    className="campo font-mono"
                  />
                </Campo>
              </div>

              {gestor ? (
                <div className="flex flex-col gap-7">
                  <fieldset className="m-0 min-w-0 border-0 p-0">
                    <legend className="mb-2 flex h-5 items-center p-0 font-mono text-[11px] tracking-wide text-ink-3 uppercase">
                      Perfil · só o gestor escolhe
                    </legend>
                    <div className="grid grid-cols-3 border-t border-b border-ink">
                      {PERFIS.map((p) => (
                        <label
                          key={p}
                          className={`flex h-12 cursor-pointer items-center gap-2.5 text-[15px] ${
                            p === perfil ? 'font-medium text-ink' : 'text-ink-2'
                          }`}
                        >
                          <input
                            type="radio"
                            name="perfil"
                            value={p}
                            checked={perfil === p}
                            onChange={() => setPerfil(p)}
                            className="m-0 accent-ink"
                          />
                          {ROTULOS[p]}
                        </label>
                      ))}
                    </div>
                  </fieldset>

                  <div className="flex flex-col">
                    <label className="flex h-12 cursor-pointer items-center gap-3 text-[15px]">
                      <input
                        type="checkbox"
                        checked={ativo}
                        onChange={(e) => setAtivo(e.target.checked)}
                        className="m-0 accent-ink"
                      />
                      Usuário ativo
                    </label>
                    <p className="mt-1.5 min-h-5 font-mono text-[11px] leading-5 text-ink-3">
                      Inativo não entra no sistema. O histórico fica preservado.
                    </p>
                  </div>
                </div>
              ) : null}

              <div className="flex flex-col gap-2">
                <Botao type="submit" seta disabled={enviando}>
                  {enviando ? 'Enviando…' : gestor ? 'Cadastrar usuário' : 'Criar conta'}
                </Botao>
                <Botao variante="texto" onClick={limpar}>
                  Limpar campos
                </Botao>
              </div>
            </div>
          </form>
        </div>
      </section>
    </div>
  )
}

type AvisoProps = {
  tom: 'erro' | 'ok'
  children: ReactNode
}

// bloco de aviso da tela. erro usa a cor de destaque, sucesso usa a tinta
function Aviso({ tom, children }: AvisoProps) {
  return (
    <div
      role={tom === 'erro' ? 'alert' : 'status'}
      className={`border-l-2 pl-3.5 text-sm leading-normal text-ink-2 ${tom === 'erro' ? 'border-accent' : 'border-ink'}`}
    >
      {children}
    </div>
  )
}
