import type { Perfil } from '../api/http'
import { permite, type Acao } from '../sessao/permissoes'
import type { NomeIcone } from '../ui'

export type IdNav = 'inicio' | 'lotes' | 'estoque' | 'alertas' | 'sanidade' | 'producao' | 'fornecedores' | 'financeiro'

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
  { id: 'sanidade', rotulo: 'Sanidade', icone: 'sanidade', caminho: '/sanidade', acao: 'registrarVacinacao' },
  { id: 'producao', rotulo: 'Produção', icone: 'producao', caminho: '/producao', acao: 'registrarProducao' },
  { id: 'alertas', rotulo: 'Alertas', icone: 'alertas', caminho: '/alertas' },
  // so o gestor ve
  { id: 'fornecedores', rotulo: 'Fornecedores', icone: 'caminhao', caminho: '/fornecedores', acao: 'cadastrarFornecedor' },
  { id: 'financeiro', rotulo: 'Financeiro', icone: 'dinheiro', caminho: '/financeiro', acao: 'gerenciarFinanceiro' },
]

export function itensDoPerfil(perfil: Perfil): ItemNavegacao[] {
  return ITENS.filter((i) => !i.acao || permite(perfil, i.acao))
}

// qual item acende na navbar pra cada rota. telas sem item proprio (mortalidade, usuarios...)
// acendem o Inicio, que e de onde elas saem
// telas da conta: Configuracoes, Suporte, Relatorios, Dados, Usuarios e Auditoria. o destaque da navbar circula o avatar
const TELAS_DA_CONTA = ['/configuracao', '/suporte', '/relatorios', '/dados', '/usuarios', '/auditoria']

export function idAtivo(caminho: string): IdNav | 'conta' {
  if (TELAS_DA_CONTA.some((t) => caminho === t || caminho.startsWith(`${t}/`))) return 'conta'
  const achado = ITENS.find((i) => i.caminho !== '/' && (caminho === i.caminho || caminho.startsWith(`${i.caminho}/`)))
  return achado?.id ?? 'inicio'
}

type Atalho = { rotulo: string; descricao: string; icone: NomeIcone; caminho: string; acao: Acao }

// o que a pessoa faz no dia a dia. aparece no menu "Registrar" e no "+" do celular
export const REGISTROS: Atalho[] = [
  { rotulo: 'Registrar mortalidade', descricao: 'Aves perdidas no lote', icone: 'baixa', caminho: '/lotes?mortalidade=1', acao: 'registrarMortalidade' },
  { rotulo: 'Registrar alimentação', descricao: 'Ração usada no lote', icone: 'racao', caminho: '/lotes?alimentacao=1', acao: 'registrarAlimentacao' },
  { rotulo: 'Movimentar estoque', descricao: 'Entrada ou saída de insumo', icone: 'caminhao', caminho: '/estoque?entrada=1', acao: 'movimentarEstoque' },
  { rotulo: 'Registrar vacinação', descricao: 'Doses aplicadas no lote', icone: 'vacina', caminho: '/sanidade?vacinar=1', acao: 'registrarVacinacao' },
  { rotulo: 'Registrar produção', descricao: 'Postura ou pesagem', icone: 'producao', caminho: '/producao', acao: 'registrarProducao' },
  { rotulo: 'Novo lote', descricao: 'Alojar um novo grupo de aves', icone: 'lotes', caminho: '/lotes?novo=1', acao: 'cadastrarLote' },
]

export function registrosDoPerfil(perfil: Perfil): Atalho[] {
  return REGISTROS.filter((r) => permite(perfil, r.acao))
}
