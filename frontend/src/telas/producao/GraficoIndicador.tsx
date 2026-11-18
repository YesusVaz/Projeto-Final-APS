type Ponto = { rotulo: string; valor: number }

type Props = {
  pontos: Ponto[]
  unidade: string
  // o que o grafico mostra, pro leitor de tela
  descricao: string
}

const L = 640
const A = 220
const MARGEM = { topo: 16, direita: 12, baixo: 28, esquerda: 44 }

// linha simples em SVG, sem biblioteca. os mesmos numeros estao na lista de registros ao lado,
// entao o grafico e so reforco visual e o leitor de tela recebe um resumo no aria-label
export function GraficoIndicador({ pontos, unidade, descricao }: Props) {
  const valores = pontos.map((p) => p.valor)
  const minimo = Math.min(...valores)
  const maximo = Math.max(...valores)
  // folga em volta pra linha nao colar nas bordas. se todos forem iguais, abre uma faixa fixa
  const folga = maximo === minimo ? Math.max(1, maximo * 0.05) : (maximo - minimo) * 0.15
  const baixo = Math.max(0, minimo - folga)
  const alto = maximo + folga

  const largura = L - MARGEM.esquerda - MARGEM.direita
  const altura = A - MARGEM.topo - MARGEM.baixo
  const x = (i: number) => MARGEM.esquerda + (pontos.length === 1 ? largura / 2 : (i / (pontos.length - 1)) * largura)
  const y = (v: number) => MARGEM.topo + altura - ((v - baixo) / (alto - baixo)) * altura

  const linha = pontos.map((p, i) => `${i === 0 ? 'M' : 'L'}${x(i).toFixed(1)} ${y(p.valor).toFixed(1)}`).join(' ')
  const graduacao = [baixo, (baixo + alto) / 2, alto]
  // rotulos do eixo X: no maximo 6, espalhados
  const passo = Math.max(1, Math.ceil(pontos.length / 6))

  const resumo = `${descricao}: de ${valores[0].toLocaleString('pt-BR', { maximumFractionDigits: 1 })} a ${valores.at(-1)?.toLocaleString('pt-BR', { maximumFractionDigits: 1 })} ${unidade} em ${pontos.length} registros.`

  return (
    <svg viewBox={`0 0 ${L} ${A}`} role="img" aria-label={resumo} className="h-auto w-full text-ink">
      {graduacao.map((g) => (
        <g key={g}>
          <line x1={MARGEM.esquerda} x2={L - MARGEM.direita} y1={y(g)} y2={y(g)} className="stroke-line-soft" strokeWidth={1} />
          <text x={MARGEM.esquerda - 8} y={y(g) + 4} textAnchor="end" className="fill-ink-3 font-mono text-[11px]">
            {g.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}
          </text>
        </g>
      ))}
      <path d={linha} fill="none" stroke="currentColor" strokeWidth={2} strokeLinejoin="round" strokeLinecap="round" />
      {pontos.map((p, i) => (
        <g key={`${p.rotulo}-${i}`}>
          <circle cx={x(i)} cy={y(p.valor)} r={3.5} className="fill-surface stroke-current" strokeWidth={2} />
          {i % passo === 0 || i === pontos.length - 1 ? (
            <text x={x(i)} y={A - 8} textAnchor="middle" className="fill-ink-3 font-mono text-[11px]">
              {p.rotulo}
            </text>
          ) : null}
        </g>
      ))}
    </svg>
  )
}
