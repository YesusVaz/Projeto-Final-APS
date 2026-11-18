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

// dinheiro chega e sai da API em centavos inteiros. so a tela mostra em reais
export function reais(centavos: number): string {
  return (centavos / 100).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

// "1.250,50", "1250.5" ou "12" viram centavos. texto que nao e dinheiro, ou zero, vira null
export function paraCentavos(texto: string): number | null {
  const limpo = texto.trim().replace(/^R\$\s*/, '')
  if (!limpo) return null
  const normal = limpo.includes(',') ? limpo.replace(/\./g, '').replace(',', '.') : limpo
  if (!/^\d+(\.\d{1,2})?$/.test(normal)) return null
  const centavos = Math.round(Number(normal) * 100)
  return centavos > 0 ? centavos : null
}

export function primeiroDiaDoMes(): string {
  return `${hoje().slice(0, 8)}01`
}

// tamanho de arquivo em unidade que a pessoa le: 1,4 MB, 320 KB
export function tamanhoDeArquivo(bytes: number): string {
  if (bytes >= 1_048_576) return `${(bytes / 1_048_576).toLocaleString('pt-BR', { maximumFractionDigits: 1 })} MB`
  return `${Math.max(1, Math.round(bytes / 1024)).toLocaleString('pt-BR')} KB`
}
