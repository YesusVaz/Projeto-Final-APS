import { chamar } from './http'
import type { CriterioAlerta } from './tiposExtras'

export type Auditoria = {
  id: number
  categoria: string
  detalhe: string
  dataHora: string
}

export type ConfiguracaoAlerta = {
  criterio: CriterioAlerta
  atencao: number
  critico: number
}

export type Parametros = {
  diasAvisoVacina: number
  diasOcorrenciaAberta: number
}

export const listarAuditoria = (token: string) => chamar<Auditoria[]>('/auditoria', { token })

export const consultarConfiguracaoAlerta = (token: string) =>
  chamar<ConfiguracaoAlerta>('/configuracao/alerta', { token })

export const salvarConfiguracaoAlerta = (token: string, dados: ConfiguracaoAlerta) =>
  chamar<ConfiguracaoAlerta>('/configuracao/alerta', { metodo: 'PUT', token, corpo: dados })

export const consultarParametros = (token: string) => chamar<Parametros>('/configuracao/parametros', { token })

export const salvarParametros = (token: string, dados: Parametros) =>
  chamar<Parametros>('/configuracao/parametros', { metodo: 'PUT', token, corpo: dados })
