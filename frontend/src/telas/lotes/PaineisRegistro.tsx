import { useSearchParams } from 'react-router'
import { listarInsumos } from '../../api/estoque'
import type { Lote } from '../../api/lotes'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import { soAtivos } from '../comum/formulario'
import { PainelAlimentacao } from '../registros/PainelAlimentacao'
import { PainelHigienizacao } from '../registros/PainelHigienizacao'
import { PainelIluminacao } from '../registros/PainelIluminacao'
import { PainelMortalidade } from '../registros/PainelMortalidade'

type Props = { lotes: Lote[]; onConcluir: () => void }

// os registros do dia abrem em painel lateral sobre a lista de lotes, por ?mortalidade=1, ?alimentacao=1,
// ?higienizacao=1 e ?iluminacao=1
// (com &lote=ID quando ja se sabe o lote). o painel de cada um so existe pra quem tem permissao
export function PaineisRegistro({ lotes, onConcluir }: Props) {
  const { perfil } = useLogado()
  const [params, setParams] = useSearchParams()
  const fechar = () => setParams({}, { replace: true })
  const loteInicial = params.get('lote') ?? ''
  return (
    <>
      {permite(perfil, 'registrarMortalidade') ? (
        <PainelMortalidade aberto={params.has('mortalidade')} onFechar={fechar} lotes={soAtivos(lotes)} loteInicial={loteInicial} onConcluir={onConcluir} />
      ) : null}
      {permite(perfil, 'registrarAlimentacao') ? (
        <PainelDeAlimentacao aberto={params.has('alimentacao')} onFechar={fechar} lotes={lotes} loteInicial={loteInicial} onConcluir={onConcluir} />
      ) : null}
      {permite(perfil, 'registrarHigienizacao') ? (
        <PainelHigienizacao aberto={params.has('higienizacao')} onFechar={fechar} lotes={soAtivos(lotes)} loteInicial={loteInicial} onConcluir={onConcluir} />
      ) : null}
      {permite(perfil, 'registrarIluminacao') ? (
        <PainelIluminacao aberto={params.has('iluminacao')} onFechar={fechar} lotes={soAtivos(lotes)} loteInicial={loteInicial} onConcluir={onConcluir} />
      ) : null}
    </>
  )
}

// busca o estoque so se a pessoa pode alimentar, e recarrega depois de gravar (o saldo da racao mudou)
function PainelDeAlimentacao({ lotes, onConcluir, ...resto }: { aberto: boolean; onFechar: () => void; lotes: Lote[]; loteInicial: string; onConcluir: () => void }) {
  const insumos = useCarregar(listarInsumos)
  const racoes = insumos.carga.situacao === 'pronto' ? insumos.carga.dados.filter((i) => i.categoria === 'RACAO' && i.ativo) : []
  return (
    <PainelAlimentacao
      {...resto}
      lotes={soAtivos(lotes)}
      racoes={racoes}
      onConcluir={() => {
        insumos.recarregar()
        onConcluir()
      }}
    />
  )
}
