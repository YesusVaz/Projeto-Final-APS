import { motion } from 'motion/react'
import { useState, type FormEvent } from 'react'
import type { Perfil } from '../../api/http'
import { alterarStatusUsuario, cadastrarUsuario, editarUsuario, listarUsuarios, type UsuarioCriado } from '../../api/usuarios'
import { useCarregar } from '../../hooks/useCarregar'
import { ROTULO_PERFIL } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Botao, CabecalhoPagina, Campo, Dialogo, Entrada, EsqueletoLista, EstadoVazio, Icone, Interruptor, Pagina, Painel, propsItem, Selecao, Selo, useToast } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { EMAIL_VALIDO, LOGIN_VALIDO, MAX_BYTES_SENHA } from '../comum/validacao'
import { useEnvio } from '../lotes/useEnvio'

const PERFIS = Object.keys(ROTULO_PERFIL) as Perfil[]
type Erros = Partial<Record<'login' | 'email' | 'nome' | 'senha', string>>

export function Usuarios() {
  const { token, login: meuLogin } = useLogado()
  const avisar = useToast()
  const { carga, recarregar } = useCarregar(listarUsuarios)
  const cadastro = useEnvio()
  const status = useEnvio()
  const [novoAberto, setNovoAberto] = useState(false)
  const [alvo, setAlvo] = useState<UsuarioCriado | null>(null)
  const [confirmando, setConfirmando] = useState(false)
  const [editando, setEditando] = useState<UsuarioCriado | null>(null)
  const [edNome, setEdNome] = useState('')
  const [edEmail, setEdEmail] = useState('')
  const [edPerfil, setEdPerfil] = useState<Perfil>('funcionario')
  const [edAtivo, setEdAtivo] = useState(true)
  const [errosEd, setErrosEd] = useState<Pick<Erros, 'nome' | 'email'>>({})
  const edicao = useEnvio()

  const [login, setLogin] = useState('')
  const [email, setEmail] = useState('')
  const [nome, setNome] = useState('')
  const [senha, setSenha] = useState('')
  const [perfil, setPerfil] = useState<Perfil>('funcionario')
  const [ativo, setAtivo] = useState(true)
  const [erros, setErros] = useState<Erros>({})

  async function criar(e: FormEvent) {
    e.preventDefault()
    const novos: Erros = {}
    if (!LOGIN_VALIDO.test(login.trim())) novos.login = 'Use de 3 a 30 letras, números, ponto, traço ou sublinhado.'
    if (!EMAIL_VALIDO.test(email.trim())) novos.email = 'Informe um e-mail válido.'
    if (!nome.trim()) novos.nome = 'Informe o nome.'
    if (senha.length < 6) novos.senha = 'A senha precisa ter pelo menos 6 caracteres.'
    else if (new TextEncoder().encode(senha).length > MAX_BYTES_SENHA) novos.senha = 'A senha é longa demais.'
    setErros(novos)
    if (Object.keys(novos).length > 0) return
    const ok = await cadastro.enviar(async () => {
      const u = await cadastrarUsuario(token, { login: login.trim(), email: email.trim(), nome: nome.trim(), senha, perfil, ativo })
      avisar({ texto: `${u.nome} cadastrado como ${ROTULO_PERFIL[u.perfil].toLowerCase()}.` })
    })
    if (ok) {
      setLogin('')
      setEmail('')
      setNome('')
      setSenha('')
      setPerfil('funcionario')
      setAtivo(true)
      setNovoAberto(false)
      recarregar()
    }
  }

  function pedirTroca(u: UsuarioCriado) {
    setAlvo(u)
    setConfirmando(true)
  }

  async function confirmarTroca() {
    if (!alvo) return
    const ok = await status.enviar(async () => {
      const u = await alterarStatusUsuario(token, alvo.id, !alvo.ativo)
      avisar({ texto: `${u.nome} agora está ${u.ativo ? 'ativo' : 'inativo'}.` })
    })
    if (ok) {
      setConfirmando(false)
      recarregar()
    }
  }

  function abrirEdicao(u: UsuarioCriado) {
    setEditando(u)
    setEdNome(u.nome)
    setEdEmail(u.email)
    setEdPerfil(u.perfil)
    setEdAtivo(u.ativo)
    setErrosEd({})
  }

  async function salvarEdicao(e: FormEvent) {
    e.preventDefault()
    if (!editando) return
    const novos: typeof errosEd = {}
    if (!edNome.trim()) novos.nome = 'Informe o nome.'
    if (!EMAIL_VALIDO.test(edEmail.trim())) novos.email = 'Informe um e-mail válido.'
    setErrosEd(novos)
    if (Object.keys(novos).length > 0) return
    const dadosMudaram = edNome.trim() !== editando.nome || edEmail.trim() !== editando.email || edPerfil !== editando.perfil
    const ok = await edicao.enviar(async () => {
      if (dadosMudaram) await editarUsuario(token, editando.id, { nome: edNome.trim(), email: edEmail.trim(), perfil: edPerfil })
      if (edAtivo !== editando.ativo) await alterarStatusUsuario(token, editando.id, edAtivo)
      avisar({ texto: `${edNome.trim()} atualizado.` })
    })
    if (ok) {
      setEditando(null)
      recarregar()
    }
  }

  return (
    <Pagina>
      <CabecalhoPagina
        titulo="Usuários"
        descricao="Quem acessa o sistema e com qual perfil. Usuário inativo não consegue entrar."
        acoes={
          <Botao onClick={() => setNovoAberto(true)}>
            <Icone nome="mais" tamanho={16} />
            Novo usuário
          </Botao>
        }
      />
      <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={4} />}>
        {(usuarios) =>
          usuarios.length === 0 ? (
            <EstadoVazio icone="usuarios" titulo="Nenhum usuário" />
          ) : (
            <ul>
              {usuarios.map((u, i) => (
                <motion.li key={u.id} {...propsItem(i)} className="grid items-center gap-x-6 gap-y-2 border-t border-line-soft py-4 first:border-t-0 md:grid-cols-[minmax(0,1.3fr)_minmax(0,1fr)_110px_auto]">
                  <div className="min-w-0">
                    <p className="truncate font-medium">
                      {u.nome}
                      {u.login === meuLogin ? <span className="font-normal text-ink-3"> (você)</span> : null}
                    </p>
                    <p className="truncate font-mono text-[13px] text-ink-3">{u.login}</p>
                  </div>
                  <p className="truncate text-[14px] text-ink-2">{u.email}</p>
                  <span>
                    <Selo nivel="info">{ROTULO_PERFIL[u.perfil]}</Selo>
                  </span>
                  <div className="flex items-center gap-4 md:justify-end">
                    <label className="flex items-center gap-3 text-[14px] text-ink-2">
                      <Interruptor marcado={u.ativo} onMudar={() => pedirTroca(u)} rotulo={`${u.ativo ? 'Inativar' : 'Ativar'} ${u.nome}`} desativado={u.login === meuLogin} />
                      {u.ativo ? 'Ativo' : 'Inativo'}
                    </label>
                    <Botao variante="contorno" onClick={() => abrirEdicao(u)} aria-label={`Editar ${u.nome}`} className="min-h-10">
                      Editar
                    </Botao>
                  </div>
                </motion.li>
              ))}
            </ul>
          )
        }
      </Carregavel>

      <Painel
        aberto={novoAberto}
        onFechar={() => setNovoAberto(false)}
        titulo="Novo usuário"
        descricao="Só o gestor escolhe o perfil. Quem se cadastra sozinho entra como funcionário."
        rodape={
          <>
            <Botao variante="fantasma" onClick={() => setNovoAberto(false)} className="min-h-11">
              Cancelar
            </Botao>
            <Botao type="submit" form="form-usuario" disabled={cadastro.enviando} className="min-h-11">
              {cadastro.enviando ? 'Salvando...' : 'Cadastrar'}
            </Botao>
          </>
        }
      >
        <form id="form-usuario" onSubmit={(e) => void criar(e)} noValidate autoComplete="off" className="flex flex-col gap-4">
          {cadastro.erro ? <Aviso tom="critico">{cadastro.erro}</Aviso> : null}
          <Campo rotulo="Nome" erro={erros.nome}>
            {(c) => <Entrada {...c} value={nome} onChange={(e) => setNome(e.target.value)} maxLength={100} />}
          </Campo>
          <Campo rotulo="Usuário (login)" erro={erros.login}>
            {(c) => <Entrada {...c} mono autoComplete="off" value={login} onChange={(e) => setLogin(e.target.value)} maxLength={30} />}
          </Campo>
          <Campo rotulo="E-mail" erro={erros.email}>
            {(c) => <Entrada {...c} type="email" autoComplete="off" value={email} onChange={(e) => setEmail(e.target.value)} maxLength={254} />}
          </Campo>
          <Campo rotulo="Senha inicial" erro={erros.senha} dica="Mínimo de 6 caracteres.">
            {(c) => <Entrada {...c} type="password" autoComplete="new-password" value={senha} onChange={(e) => setSenha(e.target.value)} />}
          </Campo>
          <Campo rotulo="Perfil">
            {(c) => (
              <Selecao {...c} value={perfil} onChange={(e) => setPerfil(e.target.value as Perfil)}>
                {PERFIS.map((p) => (
                  <option key={p} value={p}>
                    {ROTULO_PERFIL[p]}
                  </option>
                ))}
              </Selecao>
            )}
          </Campo>
          <label className="flex items-center justify-between gap-3 text-[14px]">
            Usuário ativo
            <Interruptor marcado={ativo} onMudar={setAtivo} rotulo="Usuário ativo" />
          </label>
        </form>
      </Painel>

      <Painel
        aberto={editando !== null}
        onFechar={() => setEditando(null)}
        titulo="Editar usuário"
        descricao={editando ? `O login ${editando.login} não muda.` : undefined}
        rodape={
          <>
            <Botao variante="fantasma" onClick={() => setEditando(null)} className="min-h-11">
              Cancelar
            </Botao>
            <Botao type="submit" form="form-edicao" disabled={edicao.enviando} className="min-h-11">
              {edicao.enviando ? 'Salvando...' : 'Salvar'}
            </Botao>
          </>
        }
      >
        {editando ? (
          <form id="form-edicao" onSubmit={(e) => void salvarEdicao(e)} noValidate autoComplete="off" className="flex flex-col gap-4">
            {edicao.erro ? <Aviso tom="critico">{edicao.erro}</Aviso> : null}
            <Campo rotulo="Nome" erro={errosEd.nome}>
              {(c) => <Entrada {...c} value={edNome} onChange={(e) => setEdNome(e.target.value)} maxLength={100} />}
            </Campo>
            <Campo rotulo="E-mail" erro={errosEd.email}>
              {(c) => <Entrada {...c} type="email" autoComplete="off" value={edEmail} onChange={(e) => setEdEmail(e.target.value)} maxLength={254} />}
            </Campo>
            <Campo rotulo="Perfil" dica={editando.login === meuLogin ? 'Você não pode trocar o seu próprio perfil.' : undefined}>
              {(c) => (
                <Selecao {...c} value={edPerfil} onChange={(e) => setEdPerfil(e.target.value as Perfil)} disabled={editando.login === meuLogin}>
                  {PERFIS.map((p) => (
                    <option key={p} value={p}>
                      {ROTULO_PERFIL[p]}
                    </option>
                  ))}
                </Selecao>
              )}
            </Campo>
            <label className="flex items-center justify-between gap-3 text-[14px]">
              Usuário ativo
              <Interruptor marcado={edAtivo} onMudar={setEdAtivo} rotulo="Usuário ativo" desativado={editando.login === meuLogin} />
            </label>
          </form>
        ) : null}
      </Painel>

      <Dialogo
        aberto={confirmando}
        onFechar={() => setConfirmando(false)}
        titulo={alvo?.ativo ? 'Inativar usuário' : 'Ativar usuário'}
        descricao={
          alvo?.ativo
            ? `${alvo.nome} não vai mais conseguir entrar no sistema. Os registros que ele fez continuam.`
            : `${alvo?.nome ?? 'O usuário'} volta a poder entrar no sistema.`
        }
        rodape={
          <>
            <Botao variante="fantasma" onClick={() => setConfirmando(false)} className="min-h-11">
              Cancelar
            </Botao>
            <Botao variante={alvo?.ativo ? 'perigo' : 'primario'} disabled={status.enviando} onClick={() => void confirmarTroca()} className="min-h-11">
              {status.enviando ? 'Salvando...' : alvo?.ativo ? 'Inativar' : 'Ativar'}
            </Botao>
          </>
        }
      >
        {status.erro ? <Aviso tom="critico">{status.erro}</Aviso> : null}
      </Dialogo>
    </Pagina>
  )
}
