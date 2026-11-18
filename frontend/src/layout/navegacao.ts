import type { Perfil } from '../api/http'
import { permite, type Acao } from '../sessao/permissoes'
import type { NomeIcone } from '../ui'

export type IdNav = 'inicio' | 'lotes' | 'estoque' | 'alertas' | 'sanidade' | 'producao'

type ItemNavegacao = {
  id: IdNav
  rotulo: string
  icone: NomeIcone
  caminho: string
  // sem acao, todo perfil logado ve
  acao?: Acao
}

const ITENS: ItemNavegacao[] = [
  { id: 'inicio', rotulo: 'Início', icone: 'inicio', caminho: '/' },
  { id: 'lotes', rotulo: 'Lotes', icone: 'lotes', caminho: '/lotes' },
  { id: 'estoque', rotulo: 'Estoque', icone: 'estoque', caminho: '/estoque', acao: 'movimentarEstoque' },
  { id: 'alertas', rotulo: 'Alertas', icone: 'alertas', caminho: '/alertas' },
  { id: 'sanidade', rotulo: 'Sanidade', icone: 'sanidade', caminho: '/sanidade', acao: 'registrarVacinacao' },
  { id: 'producao', rotulo: 'Produção', icone: 'producao', caminho: '/producao', acao: 'registrarProducao' },
]

export function itensDoPerfil(perfil: Perfil): ItemNavegacao[] {
  return ITENS.filter((i) => !i.acao || permite(perfil, i.acao))
}

// qual item acende na navbar pra cada rota. telas sem item proprio (mortalidade, usuarios...)
// acendem o Inicio, que e de onde elas saem
export function idAtivo(caminho: string): IdNav {
  const achado = ITENS.find((i) => i.caminho !== '/' && (caminho === i.caminho || caminho.startsWith(`${i.caminho}/`)))
  return achado?.id ?? 'inicio'
}

type Atalho = { rotulo: string; descricao: string; icone: NomeIcone; caminho: string; acao: Acao }

// o que a pessoa faz no dia a dia. aparece no menu "Registrar" e no "+" do celular
export const REGISTROS: Atalho[] = [
  { rotulo: 'Registrar mortalidade', descricao: 'Aves perdidas no lote', icone: 'baixa', caminho: '/mortalidade', acao: 'registrarMortalidade' },
  { rotulo: 'Registrar alimentação', descricao: 'Ração usada no lote', icone: 'racao', caminho: '/alimentacao', acao: 'registrarAlimentacao' },
  { rotulo: 'Movimentar estoque', descricao: 'Entrada ou saída de insumo', icone: 'caminhao', caminho: '/estoque/entrada', acao: 'movimentarEstoque' },
  { rotulo: 'Registrar vacinação', descricao: 'Doses aplicadas no lote', icone: 'vacina', caminho: '/sanidade/vacinacao', acao: 'registrarVacinacao' },
  { rotulo: 'Registrar produção', descricao: 'Postura ou pesagem', icone: 'producao', caminho: '/producao', acao: 'registrarProducao' },
  { rotulo: 'Novo lote', descricao: 'Alojar um novo grupo de aves', icone: 'lotes', caminho: '/lotes/novo', acao: 'cadastrarLote' },
]

export function registrosDoPerfil(perfil: Perfil): Atalho[] {
  return REGISTROS.filter((r) => permite(perfil, r.acao))
}

type ItemAdmin = { rotulo: string; icone: NomeIcone; caminho: string; acao: Acao }

const ADMIN: ItemAdmin[] = [
  { rotulo: 'Fornecedores', icone: 'caminhao', caminho: '/fornecedores', acao: 'cadastrarFornecedor' },
  { rotulo: 'Usuários', icone: 'usuarios', caminho: '/usuarios', acao: 'gerenciarUsuarios' },
  { rotulo: 'Auditoria', icone: 'arquivo', caminho: '/auditoria', acao: 'consultarAuditoria' },
  { rotulo: 'Parâmetros', icone: 'ajustes', caminho: '/configuracao', acao: 'configurarLimites' },
]

export function adminDoPerfil(perfil: Perfil): ItemAdmin[] {
  return ADMIN.filter((a) => permite(perfil, a.acao))
}
