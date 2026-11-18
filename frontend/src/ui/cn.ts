// junta classes ignorando o que for vazio. sem dependencia extra
export function cn(...partes: Array<string | false | null | undefined>): string {
  return partes.filter(Boolean).join(' ')
}
