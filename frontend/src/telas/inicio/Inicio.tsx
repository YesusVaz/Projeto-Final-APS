import { motion } from 'motion/react'
import { Link } from 'react-router'
import { listarInsumos, type Insumo } from '../../api/estoque'
import { dataHora, numero } from '../../api/formatar'
import { listarAlertasMortalidade, listarLotes, type AlertaMortalidade } from '../../api/lotes'
import { ROTULO_ESPECIE, ROTULO_NIVEL } from '../../api/tipos'
import { useCarregar, type Busca, type Carga } from '../../hooks/useCarregar'
import { registrosDoPerfil } from '../../layout/navegacao'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import {
  CabecalhoPagina,
  EstadoVazio,
  EsqueletoLista,
  Estatistica,
  Icone,
  ItemLista,
  Pagina,
  Progresso,
  propsItem,
  Selo,
  TituloSecao,
} from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { NIVEL_SELO, nivelPorLote, porcentagemSaldo, TOM_PROGRESSO } from '../comum/lotes'
import { NoMes } from './NoMes'

// no celular os titulos de secao sao pequenos (.sec do desenho); no desktop viram h2 de 18px
const TITULO_SECAO = 'md:text-lg md:tracking-[-0.015em] md:text-ink'
const LINK_SECAO = 'rounded text-[13.5px] font-medium text-accent hover:underline hover:underline-offset-4'

function dataExtensa() {
  const texto = new Date().toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' })
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

// numero do resumo: se a busca falhou, mostra traco em vez de inventar zero
function valorDe<T>(carga: Carga<T>, extrair: (dados: T) => number): number | string {
  return carga.situacao === 'pronto' ? extrair(carga.dados) : '—'
}

export function Inicio() {
  const { perfil } = useLogado()
  // cada busca e independente e comeca junto com as outras. se uma falhar, so o bloco dela cai
  const lotes = useCarregar(listarLotes)
  const alertas = useCarregar(listarAlertasMortalidade)
  const insumos = useCarregar(listarInsumos)

  const ativos = lotes.carga.situacao === 'pronto' ? lotes.carga.dados.filter((l) => l.status === 'ATIVO') : []
  const abaixoDoMinimo = insumos.carga.situacao === 'pronto' ? insumos.carga.dados.filter((i) => i.abaixoDoMinimo) : []
  // no celular os dois registros mais usados viram cartoes grandes, ao alcance do polegar
  const atalhos = registrosDoPerfil(perfil).filter((r) => r.caminho.includes('mortalidade') || r.caminho.includes('alimentacao'))
  const niveis = nivelPorLote(alertas.carga.situacao === 'pronto' ? alertas.carga.dados : [])

  const alertasAbertos = alertas.carga.situacao === 'pronto' ? alertas.carga.dados.length : 0
  const pendencias = alertasAbertos + abaixoDoMinimo.length

  return (
    <Pagina>
      <CabecalhoPagina trilha={dataExtensa()} titulo="Hoje na granja" />

      {atalhos.length > 0 ? (
        <section aria-label="Atalhos" className="-mt-4 mb-8 flex gap-3 md:hidden">
          {atalhos.map((a, i) => (
            <motion.div key={a.caminho} {...propsItem(i)} className="flex-1">
              <Link
                to={a.caminho}
                className="flex h-24 flex-col justify-between rounded-3xl border border-line-card bg-surface p-4 transition duration-150 active:scale-[0.97]"
              >
                <Icone nome={a.icone} tamanho={22} />
                <span className="text-[15px] font-medium">{a.rotulo.replace('Registrar ', '').replace(/^./, (c) => c.toUpperCase())}</span>
              </Link>
            </motion.div>
          ))}
        </section>
      ) : null}

      <section className="grid grid-cols-2 gap-x-8 gap-y-8 pb-11 lg:grid-cols-4">
        <Estatistica
          indice={0}
          valor={valorDe(lotes.carga, (l) => l.filter((x) => x.status === 'ATIVO').reduce((s, x) => s + x.saldo, 0))}
          rotulo="Aves vivas"
          descricao={lotes.carga.situacao === 'pronto' ? `em ${ativos.length} ${ativos.length === 1 ? 'lote ativo' : 'lotes ativos'}` : 'não carregou'}
        />
        <Estatistica indice={1} valor={valorDe(lotes.carga, () => ativos.length)} rotulo="Lotes ativos" />
        <Estatistica indice={2} valor={valorDe(alertas.carga, (a) => a.length)} rotulo="Alertas abertos" descricao="de mortalidade" />
        <Estatistica indice={3} valor={valorDe(insumos.carga, () => abaixoDoMinimo.length)} rotulo="Insumos abaixo do mínimo" />
      </section>

      {permite(perfil, 'consultarGestao') ? <NoMes /> : null}

      <div className="grid items-start gap-12 lg:grid-cols-2">
        <motion.div {...propsItem(3)}>
          <section aria-labelledby="titulo-atencao" className="md:rounded-3xl md:border md:border-line-card md:bg-surface md:p-6">
            <div className="mb-1.5 flex items-center justify-between">
              <TituloSecao id="titulo-atencao" className={TITULO_SECAO}>
                Precisa de atenção
              </TituloSecao>
              <Link to="/alertas" className={LINK_SECAO}>
                Ver alertas
              </Link>
            </div>
            <BlocoAtencao alertas={alertas} insumos={insumos} pendencias={pendencias} />
          </section>
        </motion.div>

        <motion.section {...propsItem(4)} aria-labelledby="titulo-lotes">
          <div className="mb-1.5 flex items-center justify-between">
            <TituloSecao id="titulo-lotes" className={TITULO_SECAO}>
              Lotes ativos
            </TituloSecao>
            <Link to="/lotes" className={LINK_SECAO}>
              Todos os lotes
            </Link>
          </div>
          <Carregavel carga={lotes.carga} recarregar={lotes.recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
            {(todos) => {
              const lista = todos.filter((l) => l.status === 'ATIVO').slice(0, 5)
              if (lista.length === 0) {
                return <EstadoVazio icone="lotes" titulo="Nenhum lote ativo" texto="Quando um lote for alojado, ele aparece aqui." />
              }
              return lista.map((l, i) => (
                <ItemLista key={l.id} to={`/lotes/${l.id}`} className="flex-col items-stretch gap-2.5">
                  <div className="flex items-baseline justify-between gap-3">
                    <span>
                      <b className="font-mono font-medium">{l.codigo}</b>
                      <span className="text-ink-3"> · {ROTULO_ESPECIE[l.especie]}</span>
                    </span>
                    <span className="font-mono font-medium">{numero(l.saldo)}</span>
                  </div>
                  <Progresso
                    valor={porcentagemSaldo(l)}
                    tom={TOM_PROGRESSO[niveis.get(l.id) ?? 'NORMAL']}
                    indice={i}
                    rotulo={`Saldo do lote ${l.codigo}`}
                  />
                </ItemLista>
              ))
            }}
          </Carregavel>
        </motion.section>
      </div>
    </Pagina>
  )
}

type BlocoAtencaoProps = {
  alertas: Busca<AlertaMortalidade[]>
  insumos: Busca<Insumo[]>
  pendencias: number
}

// mortalidade e estoque juntos. se um dos dois falhar, o outro continua aparecendo
function BlocoAtencao({ alertas, insumos, pendencias }: BlocoAtencaoProps) {
  const carregando = alertas.carga.situacao === 'carregando' || insumos.carga.situacao === 'carregando'
  if (carregando) return <EsqueletoLista linhas={3} />

  const listaAlertas = alertas.carga.situacao === 'pronto' ? alertas.carga.dados.slice(0, 4) : []
  const listaInsumos = insumos.carga.situacao === 'pronto' ? insumos.carga.dados.filter((i) => i.abaixoDoMinimo).slice(0, 3) : []

  return (
    <>
      {alertas.carga.situacao === 'erro' ? (
        <EstadoFalha texto="Não deu para carregar os alertas de mortalidade." onTentar={alertas.recarregar} />
      ) : null}
      {insumos.carga.situacao === 'erro' ? <EstadoFalha texto="Não deu para carregar o estoque." onTentar={insumos.recarregar} /> : null}
      {listaAlertas.map((a) => (
        <ItemLista key={`${a.loteId}-${a.dataHora}`} to={`/lotes/${a.loteId}`} className="px-0.5">
          <Selo nivel={NIVEL_SELO[a.nivel]}>{ROTULO_NIVEL[a.nivel]}</Selo>
          <span className="min-w-0 flex-1">
            <b className="font-medium">{a.codigoLote} · mortalidade</b>
            <br />
            <span className="text-[13.5px] text-ink-3">Registrada em {dataHora(a.dataHora)}.</span>
          </span>
          <Icone nome="direita" tamanho={18} className="text-ink-4" />
        </ItemLista>
      ))}
      {listaInsumos.map((i) => (
        <ItemLista key={i.id} to="/estoque" className="px-0.5">
          <Selo nivel="atencao">Atenção</Selo>
          <span className="min-w-0 flex-1">
            <b className="font-medium">{i.nome}</b>
            <br />
            <span className="text-[13.5px] text-ink-3">
              {numero(i.saldo)} de {numero(i.nivelMinimo)} {i.unidade}. Está abaixo do mínimo.
            </span>
          </span>
          <Icone nome="direita" tamanho={18} className="text-ink-4" />
        </ItemLista>
      ))}
      {pendencias === 0 && alertas.carga.situacao === 'pronto' && insumos.carga.situacao === 'pronto' ? (
        <EstadoVazio icone="ok" titulo="Tudo em ordem" texto="Nenhum alerta aberto e nenhum insumo abaixo do mínimo." />
      ) : null}
    </>
  )
}

function EstadoFalha({ texto, onTentar }: { texto: string; onTentar: () => void }) {
  return (
    <p role="alert" className="flex items-center justify-between gap-3 py-3 text-sm text-critico">
      {texto}
      <button type="button" onClick={onTentar} className="min-h-11 rounded px-2 font-medium underline">
        Tentar de novo
      </button>
    </p>
  )
}
