import { useMemo, useState } from 'react'
import { dataHora } from '../../api/formatar'
import { listarAuditoria } from '../../api/admin'
import { useCarregar } from '../../hooks/useCarregar'
import { Botao, CabecalhoPagina, Cab, Cel, Entrada, EsqueletoLista, EstadoVazio, Fichas, Icone, Linha, Pagina, Tabela, TabelaCabeca, TabelaCorpo } from '../../ui'
import { Carregavel } from '../comum/Carregavel'

const POR_PAGINA = 25

export function Auditoria() {
  const { carga, recarregar } = useCarregar(listarAuditoria)
  const [categoria, setCategoria] = useState('todas')
  const [busca, setBusca] = useState('')
  const [pagina, setPagina] = useState(0)

  const registros = useMemo(() => (carga.situacao === 'pronto' ? [...carga.dados].sort((a, b) => b.dataHora.localeCompare(a.dataHora)) : []), [carga])
  const categorias = useMemo(() => [...new Set(registros.map((r) => r.categoria))].sort(), [registros])

  const termo = busca.trim().toLowerCase()
  const filtrados = registros.filter((r) => (categoria === 'todas' || r.categoria === categoria) && r.detalhe.toLowerCase().includes(termo))
  const paginas = Math.max(1, Math.ceil(filtrados.length / POR_PAGINA))
  const atual = Math.min(pagina, paginas - 1)
  const visiveis = filtrados.slice(atual * POR_PAGINA, (atual + 1) * POR_PAGINA)

  // trocar filtro ou busca volta pra primeira pagina
  const mudar = <T,>(definir: (v: T) => void) => (valor: T) => {
    definir(valor)
    setPagina(0)
  }

  return (
    <Pagina>
      <CabecalhoPagina titulo="Auditoria" descricao="Tudo que foi feito no sistema, do mais recente para o mais antigo." />
      <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={6} />}>
        {() => (
          <>
            <div className="mb-5 flex flex-wrap items-center justify-between gap-4">
              <Fichas
                rotulo="Filtrar por categoria"
                valor={categoria}
                onMudar={mudar(setCategoria)}
                opcoes={[{ id: 'todas', rotulo: 'Todas', contador: registros.length }, ...categorias.map((c) => ({ id: c, rotulo: c.replaceAll('_', ' ').toLowerCase().replace(/^./, (x) => x.toUpperCase()) }))]}
              />
              <div className="relative w-full sm:w-[280px]">
                <Icone nome="buscar" tamanho={18} className="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-ink-4" />
                <Entrada type="search" value={busca} onChange={(e) => mudar(setBusca)(e.target.value)} placeholder="Buscar no detalhe" aria-label="Buscar no detalhe do registro" className="pl-10" />
              </div>
            </div>

            {filtrados.length === 0 ? (
              <EstadoVazio icone="arquivo" titulo="Nenhum registro encontrado" texto="Troque a categoria ou apague a busca." />
            ) : (
              <>
                <Tabela>
                  <TabelaCabeca>
                    <Linha>
                      <Cab>Quando</Cab>
                      <Cab>Categoria</Cab>
                      <Cab>Detalhe</Cab>
                    </Linha>
                  </TabelaCabeca>
                  <TabelaCorpo>
                    {visiveis.map((r) => (
                      <Linha key={r.id}>
                        <Cel className="font-mono text-[13.5px] whitespace-nowrap text-ink-3">{dataHora(r.dataHora)}</Cel>
                        <Cel className="text-[13.5px] whitespace-nowrap">{r.categoria}</Cel>
                        <Cel className="min-w-[260px] text-[14px]">{r.detalhe}</Cel>
                      </Linha>
                    ))}
                  </TabelaCorpo>
                </Tabela>
                <nav aria-label="Paginação" className="mt-5 flex items-center justify-between gap-3">
                  <p className="text-[13.5px] text-ink-3">
                    {atual * POR_PAGINA + 1} a {atual * POR_PAGINA + visiveis.length} de {filtrados.length}
                  </p>
                  <div className="flex gap-2">
                    <Botao variante="contorno" disabled={atual === 0} onClick={() => setPagina(atual - 1)} className="min-h-11 md:min-h-10">
                      Anterior
                    </Botao>
                    <Botao variante="contorno" disabled={atual >= paginas - 1} onClick={() => setPagina(atual + 1)} className="min-h-11 md:min-h-10">
                      Próxima
                    </Botao>
                  </div>
                </nav>
              </>
            )}
          </>
        )}
      </Carregavel>
    </Pagina>
  )
}
