import type { Perfil } from '../api/http'

// espelha o Perfil.java do backend. serve so pra esconder botao e menu: quem decide mesmo e o
// backend, que responde 403 se alguem tentar sem permissao
export type Acao =
  | 'cadastrarLote'
  | 'encerrarLote'
  | 'registrarBaixa'
  | 'registrarEntradaAnimais'
  | 'registrarMortalidade'
  | 'registrarAlimentacao'
  | 'movimentarEstoque'
  | 'cadastrarInsumo'
  | 'registrarVacinacao'
  | 'registrarOcorrencia'
  | 'registrarProducao'
  | 'consultarAlertasEstoque'
  | 'consultarAlertasSanitarios'
  | 'gerenciarUsuarios'
  | 'consultarAuditoria'
  | 'cadastrarFornecedor'
  | 'configurarLimites'
  | 'gerenciarFinanceiro'
  | 'registrarHigienizacao'
  | 'registrarIluminacao'
  | 'consultarProdutividade'
  | 'consultarGestao'
  | 'abrirChamado'
  | 'gerenciarChamados'
  | 'exportarDados'
  | 'gerenciarBackup'

const PERMITIDAS: Record<Exclude<Perfil, 'gestor'>, Acao[]> = {
  tecnico: [
    'cadastrarLote',
    'encerrarLote',
    'registrarBaixa',
    'registrarEntradaAnimais',
    'registrarVacinacao',
    'registrarOcorrencia',
    'consultarAlertasSanitarios',
    'registrarProducao',
    'registrarIluminacao',
    'abrirChamado',
  ],
  funcionario: ['registrarMortalidade', 'movimentarEstoque', 'registrarAlimentacao', 'registrarProducao', 'registrarHigienizacao', 'abrirChamado'],
}

export function permite(perfil: Perfil, acao: Acao): boolean {
  return perfil === 'gestor' || PERMITIDAS[perfil].includes(acao)
}

export const ROTULO_PERFIL: Record<Perfil, string> = {
  gestor: 'Gestor',
  tecnico: 'Técnico',
  funcionario: 'Funcionário',
}
