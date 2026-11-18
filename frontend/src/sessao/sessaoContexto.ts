import { createContext, useContext } from 'react'
import type { Perfil } from '../api/http'
import type { Sessao } from '../api/sessao'

export type ContextoSessao = {
  sessao: Sessao | null
  perfil: Perfil | null
  // mensagem pra tela de entrada quando a sessao caiu sozinha (401)
  aviso: string | null
  entrar: (sessao: Sessao) => void
  sair: () => Promise<void>
  // chame no catch de qualquer chamada. se for 401, limpa a sessao, manda pra /entrar e devolve true
  aoErro: (erro: unknown) => boolean
}

export const SessaoContexto = createContext<ContextoSessao | null>(null)

export function useSessao(): ContextoSessao {
  const contexto = useContext(SessaoContexto)
  if (!contexto) throw new Error('useSessao precisa estar dentro do SessaoProvider.')
  return contexto
}

// pras telas que so existem logado: a RotaProtegida garante que a sessao existe
export function useLogado(): { token: string; perfil: Perfil; nome: string; login: string } {
  const { sessao } = useSessao()
  if (!sessao) throw new Error('Tela protegida aberta sem sessão.')
  return sessao
}
