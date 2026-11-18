// icones de traco, todos no mesmo grid de 24px. aqui so tem o que o sistema usa
const CAMINHOS = {
  inicio: 'M3 11l9-8 9 8v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z',
  lotes: 'M12 2l9 5-9 5-9-5 9-5zM3 12l9 5 9-5M3 17l9 5 9-5',
  estoque: 'M3 7l9-4 9 4v10l-9 4-9-4zM3 7l9 4 9-4M12 11v10',
  sanidade: 'M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6zM12 9v6M9 12h6',
  producao: 'M4 20V10M10 20V4M16 20v-7M22 20H2',
  alertas: 'M6 8a6 6 0 0 1 12 0c0 7 3 8 3 8H3s3-1 3-8M10 20a2 2 0 0 0 4 0',
  mais: 'M12 5v14M5 12h14',
  menos: 'M5 12h14',
  buscar: 'M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14zM21 21l-4.3-4.3',
  direita: 'M9 6l6 6-6 6',
  baixo: 'M6 9l6 6 6-6',
  voltar: 'M19 12H5M11 6l-6 6 6 6',
  seta: 'M5 12h14M13 6l6 6-6 6',
  fechar: 'M6 6l12 12M18 6L6 18',
  ok: 'M5 12l5 5 9-10',
  usuario: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4 21c0-4 4-6 8-6s8 2 8 6',
  usuarios: 'M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM2 21c0-4 3-6 7-6s7 2 7 6M16 4a4 4 0 0 1 0 7M22 21c0-3-2-5-4-5.6',
  caminhao: 'M2 6h11v10H2zM13 9h4l4 4v3h-8zM6.5 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4zM17.5 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4z',
  arquivo: 'M6 3h9l4 4v14H6zM14 3v5h5M9 13h7M9 17h7',
  ajustes: 'M4 6h10M18 6h2M4 12h4M12 12h8M4 18h12M16 4v4M10 10v4M18 16v4',
  sair: 'M10 4H5v16h5M15 8l4 4-4 4M19 12H9',
  aviso: 'M12 3l10 18H2zM12 10v5M12 18v.01',
  racao: 'M3 11h18a9 9 0 0 1-18 0zM8 7c0-2 2-2 2-4M14 7c0-2 2-2 2-4',
  baixa: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM8 12h8',
  vacina: 'M18 2l4 4M15 5l4 4M7 13l6-6 4 4-6 6zM9 15l-6 6M10 8l4 4',
  remedio: 'M10.5 20.5a5 5 0 0 1-7-7l10-10a5 5 0 0 1 7 7zM8.5 8.5l7 7',
  olho: 'M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12zM12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z',
  olhoFechado:
    'M3 3l18 18M10.6 6.1A10 10 0 0 1 12 5c6 0 10 7 10 7a17 17 0 0 1-3.2 4M6.6 6.6C3.7 8.4 2 12 2 12s4 7 10 7c1.5 0 2.9-.4 4.1-1M9.9 9.9a3 3 0 0 0 4.2 4.2',
  info: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM12 11v5M12 8v.01',
  cadeado: 'M6 11h12v10H6zM8 11V8a4 4 0 0 1 8 0v3',
  email: 'M3 6h18v12H3zM3 7l9 7 9-7',
  arroba: 'M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0zM16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-3.92 7.94',
  ovo: 'M12 3c4 0 7 6 7 10a7 7 0 0 1-14 0c0-4 3-10 7-10z',
  calendario: 'M4 6h16v15H4zM4 10h16M8 3v4M16 3v4',
  filtro: 'M3 5h18l-7 8v6l-4 2v-8z',
} as const

export type NomeIcone = keyof typeof CAMINHOS

type IconeProps = {
  nome: NomeIcone
  tamanho?: number
  className?: string
}

// decorativo por padrao. quando o icone estiver sozinho no botao, o botao leva o aria-label
export function Icone({ nome, tamanho = 18, className }: IconeProps) {
  return (
    <svg
      aria-hidden="true"
      width={tamanho}
      height={tamanho}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.75}
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className ?? 'shrink-0'}
    >
      <path d={CAMINHOS[nome]} />
    </svg>
  )
}
