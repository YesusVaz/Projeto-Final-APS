import { baixar, chamar, salvarComo } from './http'

// so o gestor: exportacao em CSV (UC29) e backup do banco (UC28)

export type ConjuntoExportavel = {
  chave: string
  titulo: string
  descricao: string
  // false = retrato de hoje, nao depende do periodo
  usaPeriodo: boolean
}

export const CONJUNTOS: ConjuntoExportavel[] = [
  { chave: 'lotes', titulo: 'Lotes', descricao: 'Todos os lotes, com o saldo e a situação de hoje.', usaPeriodo: false },
  { chave: 'estoque', titulo: 'Estoque', descricao: 'Os insumos e o saldo de hoje.', usaPeriodo: false },
  { chave: 'alimentacao', titulo: 'Alimentação', descricao: 'A ração servida a cada lote, dia a dia.', usaPeriodo: true },
  { chave: 'mortalidade', titulo: 'Mortalidade', descricao: 'As aves perdidas em cada lote, dia a dia.', usaPeriodo: true },
  { chave: 'producao', titulo: 'Produção', descricao: 'Postura e pesagens de cada lote.', usaPeriodo: true },
  { chave: 'financeiro', titulo: 'Financeiro', descricao: 'Receitas e despesas lançadas.', usaPeriodo: true },
]

export async function exportar(token: string, conjunto: ConjuntoExportavel, de: string, ate: string) {
  const consulta = conjunto.usaPeriodo ? `?de=${de}&ate=${ate}` : ''
  const { blob, nome } = await baixar(`/exportacoes/${conjunto.chave}${consulta}`, token)
  salvarComo(blob, nome)
  return nome
}

export type Backup = { nome: string; tamanhoBytes: number; criadoEm: string }

export const listarBackups = (token: string) => chamar<Backup[]>('/backups', { token })
export const criarBackup = (token: string) => chamar<Backup>('/backups', { metodo: 'POST', token })

export async function baixarBackup(token: string, backup: Backup) {
  const { blob, nome } = await baixar(`/backups/${encodeURIComponent(backup.nome)}/arquivo`, token)
  salvarComo(blob, nome)
  return nome
}
