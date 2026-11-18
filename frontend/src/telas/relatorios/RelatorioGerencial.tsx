import { useState } from 'react'
import { dataCompleta, hoje, numero, primeiroDiaDoMes, reais } from '../../api/formatar'
import { consultarRelatorioGerencial, type LinhaDoLote, type RelatorioGerencial as DadosRelatorio } from '../../api/gestao'
import { ROTULO_ESPECIE, ROTULO_STATUS_LOTE } from '../../api/tipos'
import { useCarregar } from '../../hooks/useCarregar'
import { Cab, Campo, Cel, Entrada, EsqueletoLista, EstadoVazio, Estatistica, Fichas, Linha, Selo, Tabela, TabelaCabeca, TabelaCorpo, TituloSecao } from '../../ui'
import { Carregavel } from '../comum/Carregavel'

type Periodo = 'mes' | '30dias' | 'ano' | 'outro'

function diasAtras(dias: number): string {
  const d = new Date()
  d.setDate(d.getDate() - dias)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const decimal = (v: number | null, casas = 2) => (v === null ? '—' : v.toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas }))

// UC25: o periodo em numeros. dinheiro do periodo escolhido; producao de cada lote desde o alojamento
export function RelatorioGerencial() {
  const [periodo, setPeriodo] = useState<Periodo>('mes')
  const [de, setDe] = useState(primeiroDiaDoMes())
  const [ate, setAte] = useState(hoje())
  const invalido = de > ate
  const { carga, recarregar } = useCarregar((t) => consultarRelatorioGerencial(t, invalido ? ate : de, ate), `${invalido ? ate : de}|${ate}`)

  function escolher(p: Periodo) {
    setPeriodo(p)
    const datas: Record<Exclude<Periodo, 'outro'>, string> = { mes: primeiroDiaDoMes(), '30dias': diasAtras(30), ano: `${hoje().slice(0, 4)}-01-01` }
    if (p !== 'outro') {
      setDe(datas[p])
      setAte(hoje())
    }
  }

  return (
    <>
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <Fichas
          rotulo="Período"
          valor={periodo}
          onMudar={escolher}
          opcoes={[
            { id: 'mes', rotulo: 'Este mês' },
            { id: '30dias', rotulo: 'Últimos 30 dias' },
            { id: 'ano', rotulo: 'Este ano' },
            { id: 'outro', rotulo: 'Outro período' },
          ]}
        />
        <div className="flex flex-wrap items-end gap-3">
          <Campo rotulo="De" erro={invalido ? 'A data inicial vem antes da final.' : undefined}>
            {(c) => (
              <Entrada
                {...c}
                type="date"
                max={ate}
                value={de}
                onChange={(e) => {
                  setPeriodo('outro')
                  setDe(e.target.value)
                }}
              />
            )}
          </Campo>
          <Campo rotulo="Até">
            {(c) => (
              <Entrada
                {...c}
                type="date"
                min={de}
                max={hoje()}
                value={ate}
                onChange={(e) => {
                  setPeriodo('outro')
                  setAte(e.target.value)
                }}
              />
            )}
          </Campo>
        </div>
      </div>

      {invalido ? null : (
        <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={5} />}>
          {(r) => <Conteudo r={r} />}
        </Carregavel>
      )}
    </>
  )
}

function Conteudo({ r }: { r: DadosRelatorio }) {
  const lucro = r.resultadoCentavos >= 0
  return (
    <>
      <p className="mb-6 text-[13.5px] text-ink-3">
        De {dataCompleta(r.de)} até {dataCompleta(r.ate)}.
      </p>
      <div className="mb-12 grid gap-8 sm:grid-cols-3">
        <Estatistica indice={0} tamanho="medio" valor={reais(r.receitasCentavos)} rotulo="Receitas" descricao="O que entrou" />
        <Estatistica indice={1} tamanho="medio" valor={reais(r.despesasCentavos)} rotulo="Despesas" descricao="O que saiu" />
        <Estatistica indice={2} tamanho="medio" valor={reais(r.resultadoCentavos)} rotulo={lucro ? 'Lucro' : 'Prejuízo'} descricao="Receitas menos despesas" />
      </div>

      <section aria-labelledby="titulo-por-lote" className="mb-12">
        <TituloSecao id="titulo-por-lote" className="mb-1 md:text-lg md:tracking-[-0.015em] md:text-ink">
          Lote por lote
        </TituloSecao>
        <p className="mb-5 max-w-[70ch] text-[13.5px] text-ink-3">
          Mortalidade, ração e produção valem desde o alojamento de cada lote. Receitas e despesas são só as do período.
        </p>
        {r.lotes.length === 0 ? (
          <EstadoVazio icone="lotes" titulo="Ainda não há lotes" texto="Quando um lote for alojado, ele aparece aqui." />
        ) : (
          <Tabela>
            <TabelaCabeca>
              <tr>
                <Cab>Lote</Cab>
                <Cab numerica>Aves</Cab>
                <Cab numerica>Mortalidade</Cab>
                <Cab numerica>Ração</Cab>
                <Cab numerica>Rendimento</Cab>
                <Cab numerica>Receitas</Cab>
                <Cab numerica>Despesas</Cab>
                <Cab numerica>Resultado</Cab>
              </tr>
            </TabelaCabeca>
            <TabelaCorpo>
              {r.lotes.map((l) => (
                <LinhaDoRelatorio key={l.produtividade.loteId} l={l} />
              ))}
            </TabelaCorpo>
          </Tabela>
        )}
      </section>

      {r.insumosAbaixoDoMinimo.length > 0 ? (
        <section aria-labelledby="titulo-insumos-baixos">
          <TituloSecao id="titulo-insumos-baixos" className="mb-4 md:text-lg md:tracking-[-0.015em] md:text-ink">
            Insumos abaixo do mínimo
          </TituloSecao>
          <ul>
            {r.insumosAbaixoDoMinimo.map((i) => (
              <li key={i.nome} className="flex items-center gap-3 border-b border-line-soft py-3.5">
                <Selo nivel="atencao">Atenção</Selo>
                <span className="min-w-0 flex-1 font-medium">{i.nome}</span>
                <span className="font-mono text-[14px] tabular-nums text-ink-2">
                  {numero(i.saldo)} de {numero(i.nivelMinimo)} {i.unidade}
                </span>
              </li>
            ))}
          </ul>
        </section>
      ) : null}
    </>
  )
}

// o "rendimento" depende da especie: conversao alimentar no frango, ovos na poedeira
function LinhaDoRelatorio({ l }: { l: LinhaDoLote }) {
  const p = l.produtividade
  const rendimento =
    p.especie === 'FRANGO_CORTE'
      ? p.conversaoAlimentar === null
        ? '—'
        : `${decimal(p.conversaoAlimentar)} de conversão`
      : p.ovosTotal === null
        ? '—'
        : `${numero(p.ovosTotal)} ovos`
  return (
    <Linha>
      <Cel>
        <b className="font-mono font-medium">{p.codigoLote}</b>
        <span className="block text-[13px] text-ink-3">
          {ROTULO_ESPECIE[p.especie]} · {ROTULO_STATUS_LOTE[l.status].toLowerCase()}
        </span>
      </Cel>
      <Cel numerica>{numero(p.saldo)}</Cel>
      <Cel numerica>{decimal(p.taxaDeMortalidade)}%</Cel>
      <Cel numerica>{numero(p.racaoConsumidaKg)} kg</Cel>
      <Cel numerica className="whitespace-nowrap">
        {rendimento}
      </Cel>
      <Cel numerica>{reais(l.receitasCentavos)}</Cel>
      <Cel numerica>{reais(l.despesasCentavos)}</Cel>
      <Cel numerica className={l.resultadoCentavos < 0 ? 'text-critico' : undefined}>
        {reais(l.resultadoCentavos)}
      </Cel>
    </Linha>
  )
}
