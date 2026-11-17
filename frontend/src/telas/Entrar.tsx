import { useState, type FormEvent } from 'react'
import { entrar, type Sessao } from '../api/sessao'
import { Botao } from '../componentes/Botao'
import { Campo } from '../componentes/Campo'

const ROTULOS: Record<Sessao['perfil'], string> = {
  gestor: 'Gestor',
  tecnico: 'Técnico',
  funcionario: 'Funcionário',
}

type EntrarProps = {
  sessao: Sessao | null
  onEntrar: (sessao: Sessao) => void
  onSair: () => void
}

export function Entrar({ sessao, onEntrar, onSair }: EntrarProps) {
  const [login, setLogin] = useState('')
  const [senha, setSenha] = useState('')
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    setErro(null)

    if (!login.trim() || !senha) {
      setErro('Informe login e senha.')
      return
    }

    setEnviando(true)
    try {
      const nova = await entrar(login.trim(), senha)
      setSenha('')
      onEntrar(nova)
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Erro inesperado.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="grid grid-cols-1 gap-10 pt-16 lg:grid-cols-12 lg:gap-6 lg:pt-24">
      <section className="lg:col-span-5">
        <p className="font-mono text-[11px] tracking-wide text-ink-3 uppercase">Autenticação</p>
        <h1 className="mt-3 text-[56px] leading-[0.95] font-light tracking-[-0.045em] md:text-[88px]">Entrar</h1>
        <p className="mt-6 max-w-[40ch] text-[15px] leading-normal text-ink-2">
          Use seu usuário e sua senha para entrar.
        </p>
        <div className="mt-12 border-l-2 border-ink py-2.5 pl-3.5 font-mono text-[11px] leading-[1.7] text-ink-2">
          Senha guardada como hash bcrypt
          <br />
          Sessão por token, válida por 8 horas
          <br />
          Login errado e usuário inativo recebem a mesma resposta
        </div>
      </section>

      <section className="lg:col-span-5 lg:col-start-8">
        {sessao ? (
          <div className="flex flex-col gap-6">
            <p className="font-mono text-[11px] tracking-wide text-ink-3 uppercase">Sessão ativa</p>
            <p className="text-lg font-medium">Entrou como {sessao.nome}</p>
            <p className="font-mono text-[11px] leading-5 text-ink-3">
              {sessao.login} · {ROTULOS[sessao.perfil]}
            </p>
            <p className="font-mono text-[11px] leading-5 text-ink-3">
              Telas de lotes, mortalidade e estoque: não implementado.
            </p>
            <Botao variante="texto" onClick={onSair}>
              Sair
            </Botao>
          </div>
        ) : (
          <form onSubmit={enviar} noValidate className="flex flex-col gap-7">
            <Campo id="login" rotulo="Usuário">
              <input
                id="login"
                name="login"
                type="text"
                autoComplete="username"
                placeholder="seu.login"
                value={login}
                onChange={(e) => setLogin(e.target.value)}
                className="campo font-mono"
              />
            </Campo>

            <Campo
              id="senha"
              rotulo="Senha"
              erro={erro ?? undefined}
              acao={
                <button
                  type="button"
                  onClick={() => setMostrarSenha((v) => !v)}
                  className="font-mono text-[11px] tracking-wide text-ink-2 uppercase underline underline-offset-4"
                >
                  {mostrarSenha ? 'Ocultar' : 'Mostrar'}
                </button>
              }
            >
              <input
                id="senha"
                name="senha"
                type={mostrarSenha ? 'text' : 'password'}
                autoComplete="current-password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                className="campo font-mono"
              />
            </Campo>

            <Botao type="submit" seta disabled={enviando}>
              {enviando ? 'Verificando…' : 'Entrar'}
            </Botao>

            <p className="font-mono text-[11px] leading-5 text-ink-3">
              Ainda não tem conta? Crie uma pela aba Cadastro de usuário.
            </p>
          </form>
        )}
      </section>
    </div>
  )
}
