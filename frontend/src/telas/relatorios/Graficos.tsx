import { dataCompleta, dataCurta, numero, reais } from '../../api/formatar'
import { consultarGraficos, type Graficos as DadosGraficos, type PontoDia } from '../../api/gestao'
import { useCarregar } from '../../hooks/useCarregar'
import { EsqueletoLista } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { CartaoGrafico } from './graficos/CartaoGrafico'
import { mesCurto } from './graficos/escala'
import { GraficoDeBarras } from './graficos/GraficoDeBarras'
import type { Serie } from './graficos/series'

// UC26: como a granja vem andando. financeiro dos ultimos seis meses e o dia a dia do ultimo mes
export function Graficos() {
  const { carga, recarregar } = useCarregar(consultarGraficos)
  return (
    <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={4} />}>
      {(g) => <Cartoes g={g} />}
    </Carregavel>
  )
}

// um rotulo no eixo a cada sete dias, contando de hoje pra tras, pra o dia mais recente sempre aparecer
const aCadaSeteDias = (total: number) => (i: number) => (total - 1 - i) % 7 === 0

function Cartoes({ g }: { g: DadosGraficos }) {
  const meses = g.financeiroPorMes
  const seriesFinanceiras: Serie[] = [
    { nome: 'Receitas', tom: 'accent', valores: meses.map((m) => m.receitasCentavos / 100) },
    { nome: 'Despesas', tom: 'atencao', valores: meses.map((m) => m.despesasCentavos / 100) },
  ]
  const reaisInteiros = (v: number) => reais(Math.round(v * 100))

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <div className="lg:col-span-2">
        <CartaoGrafico
          titulo="Receitas e despesas"
          descricao="Por mês, nos últimos seis meses."
          series={seriesFinanceiras}
          rotulosDaTabela={meses.map((m) => mesCurto(m.mes))}
          tituloDaPrimeiraColuna="Mês"
          formatar={reaisInteiros}
        >
          <GraficoDeBarras
            categorias={meses.map((m) => ({ curto: mesCurto(m.mes), longo: mesCurto(m.mes) }))}
            series={seriesFinanceiras}
            formatar={reaisInteiros}
            descricao="Receitas e despesas por mês, nos últimos seis meses"
          />
        </CartaoGrafico>
      </div>

      <PorDia titulo="Mortalidade" descricao="Aves perdidas por dia, nos últimos 30 dias." nome="Mortes" pontos={g.mortesPorDia} formatar={numero} />
      <PorDia titulo="Consumo de ração" descricao="Quilos servidos por dia, nos últimos 30 dias." nome="Ração (kg)" pontos={g.racaoPorDia} formatar={(v) => `${numero(v)} kg`} />
      <div className="lg:col-span-2">
        <PorDia titulo="Postura" descricao="Ovos coletados por dia, nos últimos 30 dias." nome="Ovos" pontos={g.ovosPorDia} formatar={numero} />
      </div>
    </div>
  )
}

type PorDiaProps = {
  titulo: string
  descricao: string
  nome: string
  pontos: PontoDia[]
  formatar: (valor: number) => string
}

function PorDia({ titulo, descricao, nome, pontos, formatar }: PorDiaProps) {
  const series: Serie[] = [{ nome, tom: 'ink', valores: pontos.map((p) => p.valor) }]
  return (
    <CartaoGrafico
      titulo={titulo}
      descricao={descricao}
      series={series}
      rotulosDaTabela={pontos.map((p) => dataCompleta(p.data))}
      tituloDaPrimeiraColuna="Dia"
      formatar={formatar}
    >
      <GraficoDeBarras
        categorias={pontos.map((p) => ({ curto: dataCurta(p.data), longo: dataCompleta(p.data) }))}
        series={series}
        formatar={formatar}
        mostrarRotulo={aCadaSeteDias(pontos.length)}
        descricao={`${titulo}: ${descricao}`}
      />
    </CartaoGrafico>
  )
}
