import { chamar } from './http'
import type { Especie, StatusLote } from './tipos'
import type { Produtividade } from './manejo'

// area do gestor: painel do mes, graficos e relatorio gerencial. dinheiro em centavos

export type PainelGestao = {
  lotesAtivos: number
  avesVivas: number
  mortesNoMes: number
  racaoNoMesKg: number
  ovosNoMes: number
  receitasDoMesCentavos: number
  despesasDoMesCentavos: number
  resultadoDoMesCentavos: number
  insumosAbaixoDoMinimo: number
}

export type PontoDia = { data: string; valor: number }
export type MesFinanceiro = { mes: string; receitasCentavos: number; despesasCentavos: number }

export type Graficos = {
  financeiroPorMes: MesFinanceiro[]
  mortesPorDia: PontoDia[]
  racaoPorDia: PontoDia[]
  ovosPorDia: PontoDia[]
}

export type LinhaDoLote = {
  produtividade: Produtividade
  status: StatusLote
  receitasCentavos: number
  despesasCentavos: number
  resultadoCentavos: number
}

export type RelatorioGerencial = {
  de: string
  ate: string
  receitasCentavos: number
  despesasCentavos: number
  resultadoCentavos: number
  lotes: LinhaDoLote[]
  insumosAbaixoDoMinimo: { nome: string; unidade: string; saldo: number; nivelMinimo: number }[]
}

export const consultarPainel = (token: string) => chamar<PainelGestao>('/gestao/painel', { token })
export const consultarGraficos = (token: string) => chamar<Graficos>('/gestao/graficos', { token })
export const consultarRelatorioGerencial = (token: string, de: string, ate: string) =>
  chamar<RelatorioGerencial>(`/gestao/relatorio?de=${de}&ate=${ate}`, { token })

export type { Especie }
