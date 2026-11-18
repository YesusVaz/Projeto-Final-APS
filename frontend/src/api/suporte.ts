import { chamar } from './http'

export type CategoriaChamado = 'DUVIDA' | 'PROBLEMA' | 'SUGESTAO'
export type StatusChamado = 'ABERTO' | 'EM_ATENDIMENTO' | 'RESOLVIDO'

export const ROTULO_CATEGORIA_CHAMADO: Record<CategoriaChamado, string> = {
  DUVIDA: 'Dúvida',
  PROBLEMA: 'Problema',
  SUGESTAO: 'Sugestão',
}

export const ROTULO_STATUS_CHAMADO: Record<StatusChamado, string> = {
  ABERTO: 'Aberto',
  EM_ATENDIMENTO: 'Em atendimento',
  RESOLVIDO: 'Resolvido',
}

export type Chamado = {
  id: number
  solicitanteId: number
  solicitante: string
  categoria: CategoriaChamado
  assunto: string
  descricao: string
  status: StatusChamado
  abertoEm: string
  resposta: string | null
  atualizadoEm: string
}

export type NovoChamado = {
  codigoLancamento: string
  categoria: CategoriaChamado
  assunto: string
  descricao: string
}

export const abrirChamado = (token: string, chamado: NovoChamado) => chamar<Chamado>('/chamados', { metodo: 'POST', token, corpo: chamado })
export const listarMeusChamados = (token: string) => chamar<Chamado[]>('/chamados', { token })
// so o gestor
export const listarTodosChamados = (token: string) => chamar<Chamado[]>('/chamados/todos', { token })
export const responderChamado = (token: string, id: number, status: Exclude<StatusChamado, 'ABERTO'>, resposta: string) =>
  chamar<Chamado>(`/chamados/${id}`, { metodo: 'PUT', token, corpo: { status, resposta } })
