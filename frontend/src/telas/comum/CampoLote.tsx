import { numero } from '../../api/formatar'
import type { Lote } from '../../api/lotes'
import { ROTULO_ESPECIE } from '../../api/tipos'
import { Campo, Selecao } from '../../ui'

type CampoLoteProps = {
  lotes: Lote[]
  valor: string
  onMudar: (id: string) => void
  erro?: string
  rotulo?: string
  dica?: string
}

// escolha de lote nos formularios de registro. o valor e o id em texto, "" = nenhum
export function CampoLote({ lotes, valor, onMudar, erro, rotulo = 'Lote', dica }: CampoLoteProps) {
  return (
    <Campo rotulo={rotulo} erro={erro} dica={dica}>
      {(c) => (
        <Selecao {...c} value={valor} onChange={(e) => onMudar(e.target.value)}>
          <option value="">Escolha o lote</option>
          {lotes.map((l) => (
            <option key={l.id} value={l.id}>
              {l.codigo} · {ROTULO_ESPECIE[l.especie]} · saldo {numero(l.saldo)}
            </option>
          ))}
        </Selecao>
      )}
    </Campo>
  )
}
