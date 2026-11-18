import { motion } from 'motion/react'
import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { listarInsumos } from '../../api/estoque'
import { consultarRelatorio, ROTULO_CATEGORIA_FINANCEIRA, type LancamentoFinanceiro, type RelatorioFinanceiro, type TipoFinanceiro } from '../../api/financeiro'
import { listarFornecedores } from '../../api/fornecedores'
import { dataCompleta, hoje, primeiroDiaDoMes, reais } from '../../api/formatar'
import { listarLotes } from '../../api/lotes'
import { useCarregar } from '../../hooks/useCarregar'
import {
  Botao,
  CabecalhoPagina,
  Campo,
  Entrada,
  EsqueletoLista,
  EstadoVazio,
  Estatistica,
  Fichas,
  Pagina,
  Progresso,
  propsItem,
  Selecao,
  Selo,
  TituloSecao,
} from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { PainelCompra } from './PainelCompra'
import { PainelLancamento } from './PainelLancamento'

type Periodo = 'mes' | '30dias' | 'ano' | 'outro'

function diasAtras(dias: number): string {
  const d = new Date()
  d.setDate(d.getDate() - dias)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export function Financeiro() {
  // os paineis abrem por ?compra=1, ?despesa=1 e ?receita=1, como nas outras telas
  const [params, setParams] = useSearchParams()
  const [periodo, setPeriodo] = useState<Periodo>('mes')
  const [de, setDe] = useState(primeiroDiaDoMes())
  const [ate, setAte] = useState(hoje())
  const [lote, setLote] = useState('')

  const lotes = useCarregar(listarLotes)
  const insumos = useCarregar(listarInsumos)
  const fornecedores = useCarregar(listarFornecedores)

  const periodoInvalido = de > ate
  // com as datas trocadas a busca nao sai: usa a inicial igual a final ate a pessoa corrigir
  const deValido = periodoInvalido ? ate : de
  const relatorio = useCarregar((t) => consultarRelatorio(t, deValido, ate, lote), `${deValido}|${ate}|${lote}`)
  const lancamento: TipoFinanceiro | null = params.has('despesa') ? 'DESPESA' : params.has('receita') ? 'RECEITA' : null

  function escolherPeriodo(p: Periodo) {
    setPeriodo(p)
    const datas: Record<Exclude<Periodo, 'outro'>, string> = { mes: primeiroDiaDoMes(), '30dias': diasAtras(30), ano: `${hoje().slice(0, 4)}-01-01` }
    if (p !== 'outro') {
      setDe(datas[p])
      setAte(hoje())
    }
  }
  const fechar = () => setParams({}, { replace: true })

  return (
    <Pagina>
      <CabecalhoPagina
        titulo="Financeiro"
        descricao="O que a granja gastou e vendeu no período."
        acoes={
          <>
            <Botao variante="contorno" icone="caminhao" onClick={() => setParams({ compra: '1' })}>
              Registrar compra
            </Botao>
            <Botao variante="contorno" icone="mais" onClick={() => setParams({ despesa: '1' })}>
              Nova despesa
            </Botao>
            <Botao icone="mais" onClick={() => setParams({ receita: '1' })}>
              Nova receita
            </Botao>
          </>
        }
      />

      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <Fichas
          rotulo="Período"
          valor={periodo}
          onMudar={escolherPeriodo}
          opcoes={[
            { id: 'mes', rotulo: 'Este mês' },
            { id: '30dias', rotulo: 'Últimos 30 dias' },
            { id: 'ano', rotulo: 'Este ano' },
            { id: 'outro', rotulo: 'Outro período' },
          ]}
        />
        <div className="flex flex-wrap items-end gap-3">
          <Campo rotulo="De" erro={periodoInvalido ? 'A data inicial vem antes da final.' : undefined}>
            {(c) => <Entrada {...c} type="date" max={ate} value={de} onChange={(e) => {
                setPeriodo('outro')
                setDe(e.target.value)
              }} />}
          </Campo>
          <Campo rotulo="Até">
            {(c) => <Entrada {...c} type="date" min={de} max={hoje()} value={ate} onChange={(e) => {
                setPeriodo('outro')
                setAte(e.target.value)
              }} />}
          </Campo>
          <Campo rotulo="Lote">
            {(c) => (
              <Selecao {...c} value={lote} onChange={(e) => setLote(e.target.value)}>
                <option value="">Todos</option>
                {(lotes.carga.situacao === 'pronto' ? lotes.carga.dados : []).map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.codigo}
                  </option>
                ))}
              </Selecao>
            )}
          </Campo>
        </div>
      </div>

      {periodoInvalido ? null : (
        <Carregavel carga={relatorio.carga} recarregar={relatorio.recarregar} esqueleto={<EsqueletoLista linhas={5} />}>
          {(r) => <Resumo relatorio={r} />}
        </Carregavel>
      )}

      <PainelCompra
        aberto={params.has('compra')}
        onFechar={fechar}
        insumos={insumos.carga.situacao === 'pronto' ? insumos.carga.dados : []}
        fornecedores={fornecedores.carga.situacao === 'pronto' ? fornecedores.carga.dados : []}
        onConcluir={relatorio.recarregar}
      />
      <PainelLancamento tipo={lancamento} onFechar={fechar} lotes={lotes.carga.situacao === 'pronto' ? lotes.carga.dados : []} onConcluir={relatorio.recarregar} />
    </Pagina>
  )
}

function Resumo({ relatorio: r }: { relatorio: RelatorioFinanceiro }) {
  const lucro = r.resultadoCentavos >= 0
  const despesas = r.porCategoria.filter((c) => c.tipo === 'DESPESA')
  const receitas = r.porCategoria.filter((c) => c.tipo === 'RECEITA')
  return (
    <>
      <div className="mb-12 grid gap-8 sm:grid-cols-3">
        <Estatistica indice={0} tamanho="medio" valor={reais(r.receitasCentavos)} rotulo="Receitas" descricao="O que entrou" />
        <Estatistica indice={1} tamanho="medio" valor={reais(r.despesasCentavos)} rotulo="Despesas" descricao="O que saiu" />
        <Estatistica indice={2} tamanho="medio" valor={reais(r.resultadoCentavos)} rotulo={lucro ? 'Lucro' : 'Prejuízo'} descricao="Receitas menos despesas" />
      </div>

      {r.lancamentos.length === 0 ? (
        <EstadoVazio icone="dinheiro" titulo="Nada lançado neste período" texto="Registre uma compra, uma despesa ou uma receita pelos botões no alto." />
      ) : (
        <div className="grid gap-12 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.4fr)]">
          <section aria-labelledby="titulo-categorias">
            <TituloSecao id="titulo-categorias" className="mb-4 md:text-lg md:tracking-[-0.015em] md:text-ink">
              Por categoria
            </TituloSecao>
            <Categorias titulo="Despesas" itens={despesas} total={r.despesasCentavos} tom="atencao" />
            <Categorias titulo="Receitas" itens={receitas} total={r.receitasCentavos} tom="normal" />
          </section>
          <section aria-labelledby="titulo-lancamentos">
            <TituloSecao id="titulo-lancamentos" className="mb-4 md:text-lg md:tracking-[-0.015em] md:text-ink">
              Lançamentos
            </TituloSecao>
            <ul>
              {r.lancamentos.map((l, i) => (
                <LinhaLancamento key={l.id} l={l} indice={i} />
              ))}
            </ul>
          </section>
        </div>
      )}
    </>
  )
}

function Categorias({ titulo, itens, total, tom }: { titulo: string; itens: RelatorioFinanceiro['porCategoria']; total: number; tom: 'atencao' | 'normal' }) {
  if (itens.length === 0) return null
  return (
    <div className="mb-8">
      <p className="mb-3 text-[13px] font-medium text-ink-3">{titulo}</p>
      <ul className="flex flex-col gap-4">
        {itens.map((c, i) => (
          <li key={c.categoria}>
            <div className="mb-1.5 flex items-baseline justify-between gap-4 text-[14.5px]">
              <span>{ROTULO_CATEGORIA_FINANCEIRA[c.categoria]}</span>
              <span className="font-mono tabular-nums">{reais(c.totalCentavos)}</span>
            </div>
            <Progresso valor={total > 0 ? (c.totalCentavos / total) * 100 : 0} tom={tom} indice={i} rotulo={`${ROTULO_CATEGORIA_FINANCEIRA[c.categoria]}: parte do total`} />
          </li>
        ))}
      </ul>
    </div>
  )
}

function LinhaLancamento({ l, indice }: { l: LancamentoFinanceiro; indice: number }) {
  const receita = l.tipo === 'RECEITA'
  return (
    <motion.li {...propsItem(Math.min(indice, 8))} className="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-x-4 gap-y-1 border-t border-line-soft py-3.5 first:border-t-0">
      <div className="min-w-0">
        <p className="truncate font-medium">{l.descricao}</p>
        <p className="mt-1 flex flex-wrap items-center gap-2 text-[13px] text-ink-3">
          <Selo nivel={receita ? 'normal' : 'neutro'}>{ROTULO_CATEGORIA_FINANCEIRA[l.categoria]}</Selo>
          <span>{dataCompleta(l.data)}</span>
        </p>
      </div>
      <p className="font-mono text-[15px] tabular-nums">
        {receita ? '+' : '−'} {reais(l.valorCentavos)}
      </p>
    </motion.li>
  )
}
