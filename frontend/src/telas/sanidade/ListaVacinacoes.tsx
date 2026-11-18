import { motion } from 'motion/react'
import { useState } from 'react'
import type { Insumo } from '../../api/estoque'
import { dataCompleta, hoje, numero } from '../../api/formatar'
import type { Lote } from '../../api/lotes'
import { listarVacinacoes } from '../../api/sanidade'
import { useCarregar } from '../../hooks/useCarregar'
import { Campo, EsqueletoLista, EstadoVazio, propsItem, Selecao, Selo } from '../../ui'
import { Carregavel } from '../comum/Carregavel'

export function ListaVacinacoes({ lotes, insumos }: { lotes: Lote[]; insumos: Insumo[] }) {
  const [loteId, setLoteId] = useState(lotes[0] ? String(lotes[0].id) : '')

  if (lotes.length === 0) {
    return <EstadoVazio icone="lotes" titulo="Ainda não há lotes" texto="As vacinações aparecem aqui quando houver lote cadastrado." />
  }

  return (
    <>
      <div className="mb-5 max-w-[360px]">
        <Campo rotulo="Lote">
          {(c) => (
            <Selecao {...c} value={loteId} onChange={(e) => setLoteId(e.target.value)}>
              {lotes.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.codigo}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
      </div>
      {/* key: trocar de lote monta de novo, e nunca aparece a lista do lote anterior */}
      <VacinacoesDoLote key={loteId} loteId={Number(loteId)} insumos={insumos} />
    </>
  )
}

function VacinacoesDoLote({ loteId, insumos }: { loteId: number; insumos: Insumo[] }) {
  const { carga, recarregar } = useCarregar((token) => listarVacinacoes(token, loteId))
  return (
    <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
      {(vacinacoes) =>
        vacinacoes.length === 0 ? (
          <EstadoVazio icone="vacina" titulo="Nenhuma vacinação neste lote" texto="Use “Registrar vacinação” para lançar a primeira." />
        ) : (
          <ul>
            {vacinacoes.map((v, i) => {
              const vacina = insumos.find((x) => x.id === v.insumoId)
              const atrasada = v.proximaDose !== null && v.proximaDose < hoje()
              return (
                <motion.li key={v.id} {...propsItem(i)} className="flex flex-wrap items-center gap-x-4 gap-y-1 border-t border-line-soft py-4 first:border-t-0">
                  <span className="flex-1">
                    <b className="font-medium">{vacina?.nome ?? `Insumo ${v.insumoId}`}</b>
                    <span className="text-ink-3">
                      {' '}
                      · {numero(v.doses)} {v.doses === 1 ? 'dose' : 'doses'} · aplicada em {dataCompleta(v.data)}
                    </span>
                  </span>
                  {v.proximaDose ? (
                    <Selo nivel={atrasada ? 'critico' : 'info'}>
                      Reforço {dataCompleta(v.proximaDose)}
                      {atrasada ? ' · atrasado' : ''}
                    </Selo>
                  ) : null}
                </motion.li>
              )
            })}
          </ul>
        )
      }
    </Carregavel>
  )
}
