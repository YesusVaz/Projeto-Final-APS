import { chamar } from './http'

export type Fornecedor = {
  id: number
  nome: string
  documento: string
  tipoDocumento: 'CPF' | 'CNPJ'
  telefone: string | null
  email: string | null
}

export type NovoFornecedor = {
  nome: string
  documento: string
  telefone?: string
  email?: string
}

export const listarFornecedores = (token: string) => chamar<Fornecedor[]>('/fornecedores', { token })

export const cadastrarFornecedor = (token: string, novo: NovoFornecedor) =>
  chamar<Fornecedor>('/fornecedores', { metodo: 'POST', token, corpo: novo })
