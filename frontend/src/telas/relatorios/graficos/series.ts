export type TomSerie = 'ink' | 'accent' | 'atencao'

// cor da barra e do quadradinho da legenda. a cor identifica a serie, o texto fica sempre na tinta normal
export const FUNDO_DA_SERIE: Record<TomSerie, string> = { ink: 'bg-ink', accent: 'bg-accent', atencao: 'bg-barra-atencao' }
export const PREENCHIMENTO_DA_SERIE: Record<TomSerie, string> = { ink: 'fill-ink', accent: 'fill-accent', atencao: 'fill-barra-atencao' }

export type Serie = { nome: string; tom: TomSerie; valores: number[] }
