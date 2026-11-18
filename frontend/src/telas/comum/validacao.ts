// mesmas regras do backend. o cadastro publico tem as suas copias; aqui ficam as das telas do gestor
export const LOGIN_VALIDO = /^[A-Za-z0-9._-]{3,30}$/
export const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
// o bcrypt so le os 72 primeiros bytes da senha. passar disso cortaria a senha sem avisar
export const MAX_BYTES_SENHA = 72

export function digitos(texto: string): string {
  return texto.replace(/\D/g, '')
}

// CPF tem 11 digitos e CNPJ 14. a conta dos digitos verificadores quem faz e o backend
export function documentoPlausivel(texto: string): boolean {
  const n = digitos(texto).length
  return n === 11 || n === 14
}

export function formatarDocumento(texto: string): string {
  const d = digitos(texto)
  if (d.length === 11) return d.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
  if (d.length === 14) return d.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5')
  return texto
}
