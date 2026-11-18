import { chamar } from './http'

export type CategoriaFinanceira =
  | 'COMPRA_INSUMO'
  | 'MAO_DE_OBRA'
  | 'ENERGIA_AGUA'
  | 'MANUTENCAO'
  | 'TRANSPORTE'
  | 'OUTRA_DESPESA'
  | 'VENDA_OVOS'
  | 'VENDA_AVES'
  | 'OUTRA_RECEITA'

export type TipoFinanceiro = 'DESPESA' | 'RECEITA'

export const ROTULO_CATEGORIA_FINANCEIRA: Record<CategoriaFinanceira, string> = {
  COMPRA_INSUMO: 'Compra de insumos',
  MAO_DE_OBRA: 'Mão de obra',
  ENERGIA_AGUA: 'Energia e água',
  MANUTENCAO: 'Manutenção',
  TRANSPORTE: 'Transporte',
  OUTRA_DESPESA: 'Outras despesas',
  VENDA_OVOS: 'Venda de ovos',
  VENDA_AVES: 'Venda de aves',
  OUTRA_RECEITA: 'Outras receitas',
}

// compra de insumo nao entra aqui: ela tem painel proprio, porque tambem da entrada no estoque
export const CATEGORIAS_DE_DESPESA: CategoriaFinanceira[] = ['MAO_DE_OBRA', 'ENERGIA_AGUA', 'MANUTENCAO', 'TRANSPORTE', 'OUTRA_DESPESA']
export const CATEGORIAS_DE_RECEITA: CategoriaFinanceira[] = ['VENDA_OVOS', 'VENDA_AVES', 'OUTRA_RECEITA']

export type LancamentoFinanceiro = {
  id: number
  tipo: TipoFinanceiro
  categoria: CategoriaFinanceira
  descricao: string
  valorCentavos: number
  data: string
  loteId: number | null
  fornecedorId: number | null
  insumoId: number | null
  quantidade: number | null
  codigo: string
}

export type RelatorioFinanceiro = {
  de: string
  ate: string
  receitasCentavos: number
  despesasCentavos: number
  resultadoCentavos: number
  porCategoria: { categoria: CategoriaFinanceira; tipo: TipoFinanceiro; totalCentavos: number }[]
  lancamentos: LancamentoFinanceiro[]
}

export const consultarRelatorio = (token: string, de: string, ate: string, loteId?: string) =>
  chamar<RelatorioFinanceiro>(`/financeiro?de=${de}&ate=${ate}${loteId ? `&lote=${loteId}` : ''}`, { token })

export type NovaCompra = {
  codigoLancamento: string
  insumoId: number
  fornecedorId: number
  quantidade: number
  valorCentavos: number
  data: string
}

export type NovoLancamento = {
  codigoLancamento: string
  categoria: CategoriaFinanceira
  descricao: string
  valorCentavos: number
  data: string
  loteId?: number
}

export const registrarCompra = (token: string, compra: NovaCompra) =>
  chamar<LancamentoFinanceiro>('/compras', { metodo: 'POST', token, corpo: compra })

export const registrarDespesa = (token: string, despesa: NovoLancamento) =>
  chamar<LancamentoFinanceiro>('/despesas', { metodo: 'POST', token, corpo: despesa })

export const registrarReceita = (token: string, receita: NovoLancamento) =>
  chamar<LancamentoFinanceiro>('/receitas', { metodo: 'POST', token, corpo: receita })
