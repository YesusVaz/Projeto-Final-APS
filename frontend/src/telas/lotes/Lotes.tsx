import { motion } from 'motion/react'
import { useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import { numero } from '../../api/formatar'
import { idadeDoLote, listarAlertasMortalidade, listarLotes, type Lote } from '../../api/lotes'
import { ROTULO_ESPECIE, ROTULO_NIVEL, ROTULO_STATUS_LOTE, type NivelAlerta } from '../../api/tipos'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import {
  Cab,
  CabecalhoPagina,
  Cel,
  Botao,
  Entrada,
  EsqueletoLista,
  EstadoVazio,
  Fichas,
  Icone,
  ItemLista,
  Linha,
  Pagina,
  Progresso,
  propsItem,
  Selo,
  Tabela,
  TabelaCabeca,
  TabelaCorpo,
} from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { NIVEL_SELO, nivelPorLote, porcentagemSaldo, TOM_PROGRESSO } from '../comum/lotes'
import { PainelNovoLote } from './PainelNovoLote'

type Filtro = 'todos' | 'ativos' | 'encerrados'

// situacao do lote: ativo mostra o nivel da mortalidade (normal, atencao, critico). encerrado fica neutro
function SeloStatus({ lote, nivel }: { lote: Lote; nivel: NivelAlerta }) {
  if (lote.status !== 'ATIVO') return <Selo nivel="neutro">{ROTULO_STATUS_LOTE[lote.status]}</Selo>
  return <Selo nivel={NIVEL_SELO[nivel]}>{ROTULO_NIVEL[nivel]}</Selo>
}

export function Lotes() {
  const { perfil } = useLogado()
  const { carga, recarregar } = useCarregar(listarLotes)
  const alertas = useCarregar(listarAlertasMortalidade)
  // o painel de novo lote abre por ?novo=1, assim a lista nao recarrega e o menu Registrar aponta pra ca
  const [params, setParams] = useSearchParams()
  const [filtro, setFiltro] = useState<Filtro>('todos')
  const [busca, setBusca] = useState('')

  const podeCadastrar = permite(perfil, 'cadastrarLote')
  const novoAberto = podeCadastrar && params.has('novo')
  const niveis = nivelPorLote(alertas.carga.situacao === 'pronto' ? alertas.carga.dados : [])
  const abrirNovo = () => setParams({ novo: '1' })
  const fecharNovo = () => setParams({}, { replace: true })
  const todos = carga.situacao === 'pronto' ? carga.dados : []
  const ativos = todos.filter((l) => l.status === 'ATIVO').length

  const termo = busca.trim().toLowerCase()
  const visiveis = todos.filter(
    (l) =>
      (filtro === 'todos' || (filtro === 'ativos') === (l.status === 'ATIVO')) &&
      l.codigo.toLowerCase().includes(termo),
  )

  return (
    <Pagina>
      <CabecalhoPagina
        titulo="Lotes"
        descricao={
          carga.situacao === 'pronto'
            ? `${ativos} ${ativos === 1 ? 'ativo' : 'ativos'} e ${todos.length - ativos} ${todos.length - ativos === 1 ? 'encerrado' : 'encerrados'}. Toque num lote para ver o histórico.`
            : undefined
        }
        acoes={
          podeCadastrar ? (
            <Botao icone="mais" onClick={abrirNovo}>
              Novo lote
            </Botao>
          ) : undefined
        }
      />

      <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={5} />}>
        {() => (
          <>
            <div className="mb-5 flex flex-wrap items-center justify-between gap-4">
              <Fichas
                rotulo="Filtrar por situação"
                valor={filtro}
                onMudar={setFiltro}
                opcoes={[
                  { id: 'todos', rotulo: 'Todos', contador: todos.length },
                  { id: 'ativos', rotulo: 'Ativos', contador: ativos },
                  { id: 'encerrados', rotulo: 'Encerrados', contador: todos.length - ativos },
                ]}
              />
              <div className="relative w-full sm:w-[280px]">
                <Icone nome="buscar" tamanho={18} className="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-ink-4" />
                <Entrada
                  type="search"
                  value={busca}
                  onChange={(e) => setBusca(e.target.value)}
                  placeholder="Buscar lote"
                  aria-label="Buscar lote pelo código"
                  className="pl-10"
                />
              </div>
            </div>

            {visiveis.length === 0 ? (
              todos.length === 0 ? (
                <EstadoVazio
                  icone="lotes"
                  titulo="Ainda não há lotes"
                  texto={podeCadastrar ? 'Cadastre o primeiro lote para começar a registrar o dia da granja.' : 'Quando um lote for cadastrado, ele aparece aqui.'}
                  acao={
                    podeCadastrar ? (
                      <Botao onClick={abrirNovo}>Novo lote</Botao>
                    ) : undefined
                  }
                />
              ) : (
                <EstadoVazio icone="buscar" titulo="Nenhum lote encontrado" texto="Troque o filtro ou apague a busca." />
              )
            ) : (
              <>
                <TabelaLotes lotes={visiveis} niveis={niveis} />
                <ListaLotes lotes={visiveis} niveis={niveis} />
                <p className="mt-5 max-w-[60ch] text-[13.5px] text-ink-3">
                  O saldo é a soma de tudo que entrou e saiu do lote. Os movimentos não podem ser editados, só acrescentados.
                </p>
              </>
            )}
          </>
        )}
      </Carregavel>
      {podeCadastrar ? <PainelNovoLote aberto={novoAberto} onFechar={fecharNovo} /> : null}
    </Pagina>
  )
}

// desktop: tabela. no celular ela some e entra a lista de linhas
type PropsLista = { lotes: Lote[]; niveis: Map<number, NivelAlerta> }

function TabelaLotes({ lotes, niveis }: PropsLista) {
  return (
    <motion.div {...propsItem(0)} className="hidden md:block">
      <Tabela>
        <TabelaCabeca>
          <Linha>
            <Cab>Código</Cab>
            <Cab>Espécie</Cab>
            <Cab>Idade</Cab>
            <Cab numerica>Inicial</Cab>
            <Cab numerica>Saldo</Cab>
            <Cab>Situação</Cab>
            <Cab>
              <span className="sr-only">Abrir</span>
            </Cab>
          </Linha>
        </TabelaCabeca>
        <TabelaCorpo>
          {lotes.map((l, i) => (
            <Linha key={l.id}>
              <Cel>
                <Link to={`/lotes/${l.id}`} className="rounded font-mono font-medium hover:underline">
                  {l.codigo}
                </Link>
              </Cel>
              <Cel>{ROTULO_ESPECIE[l.especie]}</Cel>
              <Cel className="tabular-nums">{l.status === 'ATIVO' ? <IdadeDoLote id={l.id} /> : <span className="text-ink-3">—</span>}</Cel>
              <Cel numerica className="font-mono">
                {numero(l.quantidadeInicial)}
              </Cel>
              <Cel>
                <div className="flex items-center justify-end gap-3">
                  <span className={l.status === 'ATIVO' ? 'font-mono font-medium' : 'font-mono font-medium text-ink-3'}>{numero(l.saldo)}</span>
                  {l.status === 'ATIVO' ? (
                    <Progresso
                      valor={porcentagemSaldo(l)}
                      tom={TOM_PROGRESSO[niveis.get(l.id) ?? 'NORMAL']}
                      indice={i}
                      rotulo={`Saldo do lote ${l.codigo}`}
                      className="w-24"
                    />
                  ) : (
                    <span className="text-ink-3">—</span>
                  )}
                </div>
              </Cel>
              <Cel>
                <SeloStatus lote={l} nivel={niveis.get(l.id) ?? 'NORMAL'} />
              </Cel>
              <Cel className="w-6 text-ink-4">
                <Icone nome="direita" tamanho={18} />
              </Cel>
            </Linha>
          ))}
        </TabelaCorpo>
      </Tabela>
    </motion.div>
  )
}

// cada linha busca a idade do proprio lote. se falhar, mostra traco e o resto da tabela segue
function IdadeDoLote({ id }: { id: number }) {
  const { carga } = useCarregar((token) => idadeDoLote(token, id), id)
  if (carga.situacao !== 'pronto') return <span className="text-ink-3">—</span>
  return <>{carga.dados.idadeDias} dias</>
}

function ListaLotes({ lotes, niveis }: PropsLista) {
  return (
    <ul className="md:hidden">
      {lotes.map((l, i) => (
        <motion.li key={l.id} {...propsItem(i)}>
          <ItemLista to={`/lotes/${l.id}`} className="min-h-14 flex-col items-stretch gap-2.5">
            <div className="flex items-center justify-between gap-3">
              <span>
                <b className="font-mono font-medium">{l.codigo}</b>
                <span className="text-ink-3"> · {ROTULO_ESPECIE[l.especie]}</span>
              </span>
              <SeloStatus lote={l} nivel={niveis.get(l.id) ?? 'NORMAL'} />
            </div>
            <div className="flex items-center gap-3">
              <span className="font-mono font-medium">{numero(l.saldo)}</span>
              <Progresso
                valor={porcentagemSaldo(l)}
                tom={TOM_PROGRESSO[niveis.get(l.id) ?? 'NORMAL']}
                indice={i}
                rotulo={`Saldo do lote ${l.codigo}`}
                className="flex-1"
              />
              <span className="font-mono text-[13px] text-ink-3">de {numero(l.quantidadeInicial)}</span>
            </div>
          </ItemLista>
        </motion.li>
      ))}
    </ul>
  )
}
