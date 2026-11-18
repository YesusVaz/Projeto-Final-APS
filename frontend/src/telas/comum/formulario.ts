import { numero } from '../../api/formatar'
import type { Lote } from '../../api/lotes'

export const soAtivos = (lotes: Lote[]) => lotes.filter((l) => l.status === 'ATIVO')

// texto do campo -> mensagem de erro, ou undefined se estiver certo
export function inteiroPositivo(texto: string, maximo?: number, rotulo = 'A quantidade'): string | undefined {
  const n = Number(texto)
  if (texto.trim() === '' || !Number.isInteger(n) || n < 1) return `${rotulo} precisa ser um número inteiro maior que zero.`
  if (maximo !== undefined && n > maximo) return `${rotulo} passa do limite de ${numero(maximo)}.`
  return undefined
}

// depois de uma validacao que falhou, o foco vai pro primeiro campo com erro
export function focarPrimeiroErro(formulario: HTMLFormElement | null) {
  requestAnimationFrame(() => formulario?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus())
}
