import { chamar, perfilDaApi, perfilParaApi, type Perfil } from './http'

// o que qualquer pessoa preenche no cadastro publico
export type DadosCadastro = {
  login: string
  email: string
  nome: string
  senha: string
}

// o que so o gestor logado escolhe, alem dos dados basicos
export type NovoUsuario = DadosCadastro & {
  perfil: Perfil
  ativo: boolean
}

export type UsuarioCriado = {
  id: number
  login: string
  email: string
  nome: string
  perfil: Perfil
  ativo: boolean
}

type UsuarioApi = Omit<UsuarioCriado, 'perfil'> & { perfil: string }

// sem token. a API cria sempre como funcionario ativo, nunca pelo que vier no corpo
export async function cadastrarPublico(dados: DadosCadastro): Promise<UsuarioCriado> {
  const dadosApi = await chamar<UsuarioApi>('/cadastro', {
    metodo: 'POST',
    corpo: dados,
  })
  return { ...dadosApi, perfil: perfilDaApi(dadosApi.perfil) }
}

// so o gestor consegue. a API responde 403 pra qualquer outro perfil
export async function cadastrarUsuario(token: string, novo: NovoUsuario): Promise<UsuarioCriado> {
  const dados = await chamar<UsuarioApi>('/usuarios', {
    metodo: 'POST',
    token,
    corpo: { ...novo, perfil: perfilParaApi(novo.perfil) },
  })
  return { ...dados, perfil: perfilDaApi(dados.perfil) }
}

// so o gestor consegue. a API devolve o perfil em maiusculo
export async function listarUsuarios(token: string): Promise<UsuarioCriado[]> {
  const lista = await chamar<UsuarioApi[]>('/usuarios', { token })
  return lista.map((u) => ({ ...u, perfil: perfilDaApi(u.perfil) }))
}

// so o gestor consegue. o login nunca muda
export async function editarUsuario(
  token: string,
  id: number,
  dados: { nome: string; email: string; perfil: Perfil },
): Promise<UsuarioCriado> {
  const salvo = await chamar<UsuarioApi>(`/usuarios/${id}`, {
    metodo: 'PUT',
    token,
    corpo: { ...dados, perfil: perfilParaApi(dados.perfil) },
  })
  return { ...salvo, perfil: perfilDaApi(salvo.perfil) }
}

// muda so o campo ativo. usuario inativo nao consegue entrar
export async function alterarStatusUsuario(token: string, id: number, ativo: boolean): Promise<UsuarioCriado> {
  const dados = await chamar<UsuarioApi>(`/usuarios/${id}/status`, { metodo: 'PUT', token, corpo: { ativo } })
  return { ...dados, perfil: perfilDaApi(dados.perfil) }
}
