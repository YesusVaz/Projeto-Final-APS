import { chamar, perfilDaApi, type Perfil } from './http'

export type Sessao = {
  token: string
  login: string
  nome: string
  perfil: Perfil
}

type SessaoApi = Omit<Sessao, 'perfil'> & { perfil: string }

// o login devolve um token. a senha nao fica guardada no front depois disso
export async function entrar(login: string, senha: string): Promise<Sessao> {
  const dados = await chamar<SessaoApi>('/login', { metodo: 'POST', corpo: { login, senha } })
  return { ...dados, perfil: perfilDaApi(dados.perfil) }
}

// logout. se a API nao responder, a tela esquece a sessao do mesmo jeito
export async function sair(token: string): Promise<void> {
  await chamar('/logout', { metodo: 'POST', token }).catch(() => undefined)
}
