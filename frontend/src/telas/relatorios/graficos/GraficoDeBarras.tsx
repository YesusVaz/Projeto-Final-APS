import { useState } from 'react'
import { cn } from '../../../ui'
import { compacto, escala } from './escala'
import { FUNDO_DA_SERIE, PREENCHIMENTO_DA_SERIE, type Serie } from './series'
import { useLargura } from './useLargura'

type Props = {
  // um rotulo por coluna. o curto vai no eixo, o longo no balao
  categorias: { curto: string; longo: string }[]
  series: Serie[]
  formatar: (valor: number) => string
  // quais colunas ganham rotulo no eixo (num grafico de 30 dias nao cabe todo dia)
  mostrarRotulo?: (indice: number) => boolean
  descricao: string
  altura?: number
}

const MARGEM = { topo: 12, baixo: 28, esquerda: 52, direita: 8 }

// barras verticais, 1 ou 2 series lado a lado. ponteiro e teclado mostram o balao da coluna.
// a base e sempre o zero e a ponta da barra e arredondada
export function GraficoDeBarras({ categorias, series, formatar, mostrarRotulo = () => true, descricao, altura = 220 }: Props) {
  const { ref, largura } = useLargura<HTMLDivElement>()
  const [ativa, setAtiva] = useState<number | null>(null)

  const maximo = Math.max(0, ...series.flatMap((s) => s.valores))
  const { teto, ticks } = escala(maximo)
  const areaL = Math.max(0, largura - MARGEM.esquerda - MARGEM.direita)
  const areaA = altura - MARGEM.topo - MARGEM.baixo
  const faixa = categorias.length > 0 ? areaL / categorias.length : 0
  const y = (v: number) => MARGEM.topo + areaA - (v / teto) * areaA

  // ocupa 70% da faixa, com 2px de respiro entre as barras da mesma coluna
  const larguraGrupo = faixa * 0.7
  const larguraBarra = Math.max(1, Math.min(28, (larguraGrupo - 2 * (series.length - 1)) / series.length))
  const ocupado = larguraBarra * series.length + 2 * (series.length - 1)

  const colunaAtiva = ativa === null ? null : ativa
  const xCentro = (i: number) => MARGEM.esquerda + faixa * i + faixa / 2

  return (
    <div ref={ref} className="relative w-full" onPointerLeave={() => setAtiva(null)}>
      {largura > 0 ? (
        <svg width={largura} height={altura} role="img" aria-label={descricao} className="block overflow-visible">
          {ticks.map((t) => (
            <g key={t}>
              <line x1={MARGEM.esquerda} x2={largura - MARGEM.direita} y1={y(t)} y2={y(t)} className={t === 0 ? 'stroke-line' : 'stroke-line-soft'} strokeWidth={1} />
              <text x={MARGEM.esquerda - 10} y={y(t)} textAnchor="end" dominantBaseline="central" className="fill-ink-3 text-[11.5px]">
                {compacto(t)}
              </text>
            </g>
          ))}

          {categorias.map((c, i) => (
            <g key={c.longo}>
              {series.map((s, k) => {
                const altBarra = Math.max(0, (s.valores[i] / teto) * areaA)
                const x = xCentro(i) - ocupado / 2 + k * (larguraBarra + 2)
                const raio = Math.min(4, larguraBarra / 2, altBarra)
                return altBarra === 0 ? null : (
                  <path
                    key={s.nome}
                    d={`M${x},${MARGEM.topo + areaA} V${y(s.valores[i]) + raio} Q${x},${y(s.valores[i])} ${x + raio},${y(s.valores[i])} H${x + larguraBarra - raio} Q${x + larguraBarra},${y(s.valores[i])} ${x + larguraBarra},${y(s.valores[i]) + raio} V${MARGEM.topo + areaA} Z`}
                    className={cn(PREENCHIMENTO_DA_SERIE[s.tom], colunaAtiva !== null && colunaAtiva !== i && 'opacity-45', 'transition-opacity duration-150')}
                  />
                )
              })}
              {mostrarRotulo(i) ? (
                <text x={xCentro(i)} y={altura - 8} textAnchor="middle" className="fill-ink-3 text-[11.5px]">
                  {c.curto}
                </text>
              ) : null}
              {/* area de toque maior que a barra: a coluna inteira */}
              <rect
                x={MARGEM.esquerda + faixa * i}
                y={MARGEM.topo}
                width={faixa}
                height={areaA + MARGEM.baixo}
                fill="transparent"
                tabIndex={0}
                role="img"
                aria-label={`${c.longo}: ${series.map((s) => `${s.nome} ${formatar(s.valores[i])}`).join(', ')}`}
                className="outline-none focus-visible:stroke-accent focus-visible:stroke-2"
                onPointerEnter={() => setAtiva(i)}
                onFocus={() => setAtiva(i)}
                onBlur={() => setAtiva(null)}
              />
            </g>
          ))}
        </svg>
      ) : (
        <div style={{ height: altura }} />
      )}

      {colunaAtiva !== null && largura > 0 ? (
        <div
          role="status"
          className="pointer-events-none absolute z-10 min-w-36 rounded-xl border border-line-card bg-surface px-3 py-2 text-[13px] shadow-[0_6px_24px_rgb(0_0_0/0.08)]"
          style={{
            left: Math.min(Math.max(xCentro(colunaAtiva), 80), largura - 80),
            top: 0,
            transform: 'translateX(-50%)',
          }}
        >
          <p className="mb-1 font-medium">{categorias[colunaAtiva].longo}</p>
          {series.map((s) => (
            <p key={s.nome} className="flex items-center justify-between gap-4">
              <span className="flex items-center gap-2 text-ink-2">
                <span className={cn('size-2.5 rounded-sm', FUNDO_DA_SERIE[s.tom])} aria-hidden />
                {s.nome}
              </span>
              <span className="font-mono tabular-nums">{formatar(s.valores[colunaAtiva])}</span>
            </p>
          ))}
        </div>
      ) : null}
    </div>
  )
}
