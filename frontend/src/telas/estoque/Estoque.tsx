import { motion } from 'motion/react'
import { useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import { listarInsumos, type Insumo } from '../../api/estoque'
import { numero } from '../../api/formatar'
import type { CategoriaInsumo } from '../../api/tipos'
import { ROTULO_CATEGORIA_INSUMO } from '../../api/tiposExtras'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import {
  Botao,
  CabecalhoPagina,
  classesBotao,
  Entrada,
  EsqueletoLista,
  EstadoVazio,
  Fichas,
  Icone,
  Pagina,
  Progresso,
  propsItem,
  Selo,
} from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { PainelEntrada } from './PainelEntrada'
import { PainelNovoInsumo } from './PainelNovoInsumo'

type Filtro = 'todos' | CategoriaInsumo

// barra cheia = saldo igual ou acima do minimo. abaixo disso ela encolhe e muda de cor
function nivel(i: Insumo): number {
  return i.nivelMinimo > 0 ? Math.min(100, (i.saldo / i.nivelMinimo) * 100) : 100
}

export function Estoque() {
  const { perfil } = useLogado()
  const { carga, recarregar } = useCarregar(listarInsumos)
  const [filtro, setFiltro] = useState<Filtro>('todos')
  const [busca, setBusca] = useState('')
  const [novoAberto, setNovoAberto] = useState(false)
  // a entrada abre por ?entrada=1 (e &insumo=ID pra ja vir escolhido), assim a lista nao recarrega
  const [params, setParams] = useSearchParams()

  const podeCadastrar = permite(perfil, 'cadastrarInsumo')
  const podeMovimentar = permite(perfil, 'movimentarEstoque')
  const todos = carga.situacao === 'pronto' ? carga.dados : []
  const baixos = todos.filter((i) => i.abaixoDoMinimo).length
  const termo = busca.trim().toLowerCase()
  const visiveis = todos.filter((i) => (filtro === 'todos' || i.categoria === filtro) && i.nome.toLowerCase().includes(termo))

  return (
    <Pagina>
      <CabecalhoPagina
        titulo="Estoque"
        descricao={
          carga.situacao === 'pronto'
            ? baixos > 0
              ? `${baixos} ${baixos === 1 ? 'insumo está abaixo' : 'insumos estão abaixo'} do mínimo.`
              : 'Todos os insumos estão acima do mínimo.'
            : undefined
        }
        acoes={
          <>
            {podeMovimentar ? (
              <Botao variante="contorno" icone="caminhao" onClick={() => setParams({ entrada: '1' })}>
                Movimentar
              </Botao>
            ) : null}
            {podeCadastrar ? (
              <Botao onClick={() => setNovoAberto(true)}>
                <Icone nome="mais" tamanho={16} />
                Novo insumo
              </Botao>
            ) : null}
          </>
        }
      />

      <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={5} />}>
        {() => (
          <>
            <div className="mb-5 flex flex-wrap items-center justify-between gap-4">
              <Fichas
                rotulo="Filtrar por categoria"
                valor={filtro}
                onMudar={setFiltro}
                opcoes={[
                  { id: 'todos', rotulo: 'Todos', contador: todos.length },
                  ...(Object.keys(ROTULO_CATEGORIA_INSUMO) as CategoriaInsumo[]).map((c) => ({
                    id: c,
                    rotulo: ROTULO_CATEGORIA_INSUMO[c],
                    contador: todos.filter((i) => i.categoria === c).length,
                  })),
                ]}
              />
              <div className="relative w-full sm:w-[280px]">
                <Icone nome="buscar" tamanho={18} className="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-ink-4" />
                <Entrada type="search" value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar insumo" aria-label="Buscar insumo pelo nome" className="pl-10" />
              </div>
            </div>

            {visiveis.length === 0 ? (
              <EstadoVazio
                icone="estoque"
                titulo={todos.length === 0 ? 'Ainda não há insumos' : 'Nenhum insumo encontrado'}
                texto={todos.length === 0 ? (podeCadastrar ? 'Cadastre o primeiro insumo para controlar o estoque.' : 'Quando o gestor cadastrar insumos, eles aparecem aqui.') : 'Troque a categoria ou apague a busca.'}
              />
            ) : (
              <ul>
                {visiveis.map((i, ordem) => (
                  <motion.li key={i.id} {...propsItem(ordem)} className="border-t border-line-soft first:border-t-0">
                    <div className="grid items-center gap-x-6 gap-y-2.5 py-4 md:grid-cols-[minmax(0,1.4fr)_minmax(0,1.2fr)_auto]">
                      <div className="min-w-0">
                        <p className="truncate font-medium">{i.nome}</p>
                        <p className="text-[13.5px] text-ink-3">{ROTULO_CATEGORIA_INSUMO[i.categoria]}{i.ativo ? '' : ' · inativo'}</p>
                      </div>
                      <div className="flex items-center gap-3">
                        <span className="w-28 shrink-0 font-mono text-[14px]">
                          <b className="font-medium">{numero(i.saldo)}</b>
                          <span className="text-ink-3"> / {numero(i.nivelMinimo)} {i.unidade}</span>
                        </span>
                        <Progresso valor={nivel(i)} tom={i.abaixoDoMinimo ? 'atencao' : 'normal'} indice={ordem} rotulo={`Nível de ${i.nome}`} className="flex-1" />
                      </div>
                      <div className="flex items-center gap-3 md:justify-end">
                        {i.abaixoDoMinimo ? <Selo nivel="atencao">Abaixo do mínimo</Selo> : <Selo nivel="normal">Em dia</Selo>}
                        {podeMovimentar && i.ativo ? (
                          <Link to={`/estoque?entrada=1&insumo=${i.id}`} className={classesBotao('fantasma', 'sm', 'min-h-11 md:min-h-0')}>
                            Movimentar
                          </Link>
                        ) : null}
                      </div>
                    </div>
                  </motion.li>
                ))}
              </ul>
            )}
          </>
        )}
      </Carregavel>

      <PainelNovoInsumo aberto={novoAberto} onFechar={() => setNovoAberto(false)} onConcluir={recarregar} />
      {podeMovimentar ? (
        <PainelEntrada
          aberto={params.has('entrada')}
          onFechar={() => setParams({}, { replace: true })}
          insumos={todos}
          insumoInicial={params.get('insumo') ?? ''}
          onConcluir={recarregar}
        />
      ) : null}
    </Pagina>
  )
}
