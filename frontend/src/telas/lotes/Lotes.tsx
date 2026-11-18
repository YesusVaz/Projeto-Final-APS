import { motion } from 'motion/react'
import { useState } from 'react'
import { Link } from 'react-router'
import { numero } from '../../api/formatar'
import { listarLotes, type Lote } from '../../api/lotes'
import { ROTULO_ESPECIE, ROTULO_STATUS_LOTE } from '../../api/tipos'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import {
  Cab,
  CabecalhoPagina,
  Cel,
  classesBotao,
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
import { porcentagemSaldo } from '../comum/lotes'

type Filtro = 'todos' | 'ativos' | 'encerrados'

function SeloStatus({ lote }: { lote: Lote }) {
  return <Selo nivel={lote.status === 'ATIVO' ? 'normal' : 'neutro'}>{ROTULO_STATUS_LOTE[lote.status]}</Selo>
}

export function Lotes() {
  const { perfil } = useLogado()
  const { carga, recarregar } = useCarregar(listarLotes)
  const [filtro, setFiltro] = useState<Filtro>('todos')
  const [busca, setBusca] = useState('')

  const podeCadastrar = permite(perfil, 'cadastrarLote')
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
            <Link to="/lotes/novo" className={classesBotao('primario')}>
              <Icone nome="mais" tamanho={16} />
              Novo lote
            </Link>
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
                      <Link to="/lotes/novo" className={classesBotao('primario')}>
                        Novo lote
                      </Link>
                    ) : undefined
                  }
                />
              ) : (
                <EstadoVazio icone="buscar" titulo="Nenhum lote encontrado" texto="Troque o filtro ou apague a busca." />
              )
            ) : (
              <>
                <TabelaLotes lotes={visiveis} />
                <ListaLotes lotes={visiveis} />
              </>
            )}
          </>
        )}
      </Carregavel>
    </Pagina>
  )
}

// desktop: tabela. no celular ela some e entra a lista de linhas
function TabelaLotes({ lotes }: { lotes: Lote[] }) {
  return (
    <motion.div {...propsItem(0)} className="hidden md:block">
      <Tabela>
        <TabelaCabeca>
          <Linha>
            <Cab>Código</Cab>
            <Cab>Espécie</Cab>
            <Cab numerica>Inicial</Cab>
            <Cab>Saldo</Cab>
            <Cab>Situação</Cab>
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
              <Cel numerica className="font-mono">
                {numero(l.quantidadeInicial)}
              </Cel>
              <Cel>
                <div className="flex items-center gap-3">
                  <span className="w-14 font-mono font-medium">{numero(l.saldo)}</span>
                  <Progresso valor={porcentagemSaldo(l)} indice={i} rotulo={`Saldo do lote ${l.codigo}`} className="w-24" />
                </div>
              </Cel>
              <Cel>
                <SeloStatus lote={l} />
              </Cel>
            </Linha>
          ))}
        </TabelaCorpo>
      </Tabela>
    </motion.div>
  )
}

function ListaLotes({ lotes }: { lotes: Lote[] }) {
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
              <SeloStatus lote={l} />
            </div>
            <div className="flex items-center gap-3">
              <span className="font-mono font-medium">{numero(l.saldo)}</span>
              <Progresso valor={porcentagemSaldo(l)} indice={i} rotulo={`Saldo do lote ${l.codigo}`} className="flex-1" />
              <span className="font-mono text-[13px] text-ink-3">de {numero(l.quantidadeInicial)}</span>
            </div>
          </ItemLista>
        </motion.li>
      ))}
    </ul>
  )
}
