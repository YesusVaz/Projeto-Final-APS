// formatos de tela. datas da API chegam como YYYY-MM-DD: quebro a string em vez de usar Date
// pra nao deslocar um dia por causa do fuso

export function numero(valor: number): string {
  return valor.toLocaleString('pt-BR')
}

export function dataCurta(iso: string): string {
  const [, mes, dia] = iso.split('-')
  return `${dia}/${mes}`
}

export function dataCompleta(iso: string): string {
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

export function hoje(): string {
  const d = new Date()
  const mes = String(d.getMonth() + 1).padStart(2, '0')
  const dia = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mes}-${dia}`
}

export function dataHora(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
}

export function primeiroNome(nome: string): string {
  return nome.trim().split(/\s+/)[0] ?? nome
}

// dias corridos entre uma data YYYY-MM-DD e hoje. as duas viram meia-noite UTC, entao o fuso nao desloca
export function diasDesde(iso: string): number {
  return Math.max(0, Math.round((Date.parse(hoje()) - Date.parse(iso)) / 86_400_000))
}
