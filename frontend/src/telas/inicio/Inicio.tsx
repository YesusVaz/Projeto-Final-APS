import { motion } from 'motion/react'
import { Link } from 'react-router'
import { listarInsumos, type Insumo } from '../../api/estoque'
import { dataHora, numero, primeiroNome } from '../../api/formatar'
import { listarAlertasMortalidade, listarLotes, type AlertaMortalidade } from '../../api/lotes'
import { ROTULO_ESPECIE, ROTULO_NIVEL } from '../../api/tipos'
import { useCarregar, type Busca, type Carga } from '../../hooks/useCarregar'
import { registrosDoPerfil } from '../../layout/navegacao'
import { useLogado } from '../../sessao/sessaoContexto'
import {
  CabecalhoPagina,
  classesBotao,
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
import { NIVEL_SELO, porcentagemSaldo } from '../comum/lotes'

function dataExtensa() {
  const texto = new Date().toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' })
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

// numero do resumo: se a busca falhou, mostra traco em vez de inventar zero
function valorDe<T>(carga: Carga<T>, extrair: (dados: T) => number): number | string {
  return carga.situacao === 'pronto' ? extrair(carga.dados) : '—'
}

export function Inicio() {
  const { nome, perfil } = useLogado()
  // cada busca e independente e comeca junto com as outras. se uma falhar, so o bloco dela cai
  const lotes = useCarregar(listarLotes)
  const alertas = useCarregar(listarAlertasMortalidade)
  const insumos = useCarregar(listarInsumos)

  const ativos = lotes.carga.situacao === 'pronto' ? lotes.carga.dados.filter((l) => l.status === 'ATIVO') : []
  const abaixoDoMinimo = insumos.carga.situacao === 'pronto' ? insumos.carga.dados.filter((i) => i.abaixoDoMinimo) : []
  const atalhos = registrosDoPerfil(perfil)

  const alertasAbertos = alertas.carga.situacao === 'pronto' ? alertas.carga.dados.length : 0
  const pendencias = alertasAbertos + abaixoDoMinimo.length

  return (
    <Pagina>
      <CabecalhoPagina trilha={`Olá, ${primeiroNome(nome)}`} titulo="Hoje na granja" descricao={dataExtensa()} />

      <section className="grid grid-cols-2 gap-x-6 gap-y-8 pb-11 lg:grid-cols-4">
        <Estatistica
          indice={0}
          valor={valorDe(lotes.carga, (l) => l.filter((x) => x.status === 'ATIVO').reduce((s, x) => s + x.saldo, 0))}
          rotulo="Aves vivas"
          descricao={lotes.carga.situacao === 'pronto' ? `em ${ativos.length} lotes ativos` : 'não carregou'}
        />
        <Estatistica indice={1} valor={valorDe(lotes.carga, () => ativos.length)} rotulo="Lotes ativos" />
        <Estatistica
          indice={2}
          valor={valorDe(alertas.carga, (a) => a.length)}
          rotulo="Alertas abertos"
          descricao="de mortalidade"
        />
        <Estatistica
          indice={3}
          valor={valorDe(insumos.carga, () => abaixoDoMinimo.length)}
          rotulo="Insumos abaixo do mínimo"
        />
      </section>

      {atalhos.length > 0 ? (
        <section aria-label="Atalhos" className="-mt-2 mb-10 flex flex-wrap gap-2.5">
          {atalhos.map((a) => (
            <Link key={a.caminho} to={a.caminho} className={classesBotao('contorno', 'md', 'min-h-11 md:min-h-0')}>
              <Icone nome={a.icone} tamanho={18} />
              {a.rotulo}
            </Link>
          ))}
        </section>
      ) : null}

      <div className="grid items-start gap-12 lg:grid-cols-2">
        <motion.section {...propsItem(3)} aria-labelledby="titulo-atencao">
          <div className="mb-1.5 flex items-center justify-between">
            <TituloSecao id="titulo-atencao">Precisa de atenção</TituloSecao>
            <Link to="/alertas" className="rounded text-[13px] text-accent hover:underline">
              Ver alertas
            </Link>
          </div>
          <BlocoAtencao alertas={alertas} insumos={insumos} pendencias={pendencias} />
        </motion.section>

        <motion.section {...propsItem(4)} aria-labelledby="titulo-lotes">
          <div className="mb-1.5 flex items-center justify-between">
            <TituloSecao id="titulo-lotes">Lotes ativos</TituloSecao>
            <Link to="/lotes" className="rounded text-[13px] text-accent hover:underline">
              Todos os lotes
            </Link>
          </div>
          <Carregavel carga={lotes.carga} recarregar={lotes.recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
            {(todos) => {
              const lista = todos.filter((l) => l.status === 'ATIVO').slice(0, 5)
              if (lista.length === 0) {
                return (
                  <EstadoVazio
                    icone="lotes"
                    titulo="Nenhum lote ativo"
                    texto="Quando um lote for alojado, ele aparece aqui."
                  />
                )
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
                  <Progresso valor={porcentagemSaldo(l)} indice={i} rotulo={`Saldo do lote ${l.codigo}`} />
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
      {insumos.carga.situacao === 'erro' ? (
        <EstadoFalha texto="Não deu para carregar o estoque." onTentar={insumos.recarregar} />
      ) : null}
      {listaAlertas.map((a) => (
        <ItemLista key={`${a.loteId}-${a.dataHora}`} to={`/lotes/${a.loteId}`}>
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
        <ItemLista key={i.id} to="/estoque">
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
