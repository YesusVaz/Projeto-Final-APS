import { motion } from 'motion/react'
import type { ItemHistorico } from '../../api/lotes'
import { dataCompleta } from '../../api/formatar'
import { ROTULO_CATEGORIA_HISTORICO } from '../../api/tipos'
import { propsItem } from '../../ui'

// inicio e a posicao do primeiro item do grupo na lista inteira (vira atraso da cascata)
type Grupo = { data: string; inicio: number; itens: ItemHistorico[] }

// mais recente primeiro, agrupado por dia. a API manda data e hora separadas
function agrupar(itens: ItemHistorico[]): Grupo[] {
  const ordenados = [...itens].sort((a, b) => `${b.data} ${b.hora ?? ''}`.localeCompare(`${a.data} ${a.hora ?? ''}`))
  const grupos: Grupo[] = []
  let total = 0
  for (const item of ordenados) {
    const ultimo = grupos.at(-1)
    if (ultimo?.data === item.data) ultimo.itens.push(item)
    else grupos.push({ data: item.data, inicio: total, itens: [item] })
    total++
  }
  return grupos
}

export function LinhaDoTempo({ itens }: { itens: ItemHistorico[] }) {
  return (
    <div className="flex flex-col gap-7">
      {agrupar(itens).map((grupo) => (
        <section key={grupo.data} aria-label={`Registros de ${dataCompleta(grupo.data)}`}>
          <h3 className="mb-1 font-mono text-[13px] text-ink-3">{dataCompleta(grupo.data)}</h3>
          <ol className="border-l border-line">
            {grupo.itens.map((item, i) => (
              <motion.li key={`${item.data}-${item.hora}-${i}`} {...propsItem(grupo.inicio + i)} className="relative py-3 pl-5">
                <span aria-hidden="true" className="absolute top-[19px] -left-[4.5px] size-2 rounded-full bg-ink" />
                <div className="flex items-baseline justify-between gap-4">
                  <p className="font-medium">{ROTULO_CATEGORIA_HISTORICO[item.categoria]}</p>
                  {item.hora ? <p className="font-mono text-[13px] text-ink-3">{item.hora.slice(0, 5)}</p> : null}
                </div>
                <p className="text-[14px] text-ink-3">{item.descricao}</p>
              </motion.li>
            ))}
          </ol>
        </section>
      ))}
    </div>
  )
}
