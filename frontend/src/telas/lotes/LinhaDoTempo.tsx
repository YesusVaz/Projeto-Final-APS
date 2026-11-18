import { motion } from 'motion/react'
import { dataCurta } from '../../api/formatar'
import type { ItemHistorico } from '../../api/lotes'
import { ROTULO_CATEGORIA_HISTORICO } from '../../api/tipos'
import { cn, propsItem } from '../../ui'

// mais recente primeiro. a API manda data e hora separadas
function ordenar(itens: ItemHistorico[]): ItemHistorico[] {
  return [...itens].sort((a, b) => `${b.data} ${b.hora ?? ''}`.localeCompare(`${a.data} ${a.hora ?? ''}`))
}

// linha do tempo do desenho: fio fino na esquerda, bolinha oca em cada registro e cheia nos que
// mexem no saldo do lote (entrada, mortalidade, baixa)
export function LinhaDoTempo({ itens }: { itens: ItemHistorico[] }) {
  return (
    <ol className="relative ml-1 border-l border-[#d9d8d2] pl-7">
      {ordenar(itens).map((item, i) => (
        <motion.li key={`${item.data}-${item.hora}-${i}`} {...propsItem(i)} className="relative pb-6 last:pb-0">
          <span
            aria-hidden="true"
            className={cn('absolute top-[5px] -left-[34px] size-[11px] rounded-full border-2 border-ink', item.categoria === 'MOVIMENTO' ? 'bg-ink' : 'bg-paper')}
          />
          <p className="font-medium">{ROTULO_CATEGORIA_HISTORICO[item.categoria]}</p>
          <p className="text-[13.5px] text-ink-3">{item.descricao}</p>
          <p className="mt-0.5 font-mono text-xs text-ink-3">
            {dataCurta(item.data)}
            {item.hora ? ` · ${item.hora.slice(0, 5)}` : ''}
          </p>
        </motion.li>
      ))}
    </ol>
  )
}
