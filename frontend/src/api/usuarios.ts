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
