import type { Insumo } from '../../api/estoque'
import { dataCompleta, diasDesde, numero } from '../../api/formatar'
import { consultarOcorrencia } from '../../api/sanidade'
import { ROTULO_STATUS_OCORRENCIA } from '../../api/tiposExtras'
import { useCarregar } from '../../hooks/useCarregar'
import { Aviso, Botao, EsqueletoLista, Painel, Selo, TituloSecao } from '../../ui'
import { Carregavel } from '../comum/Carregavel'

type Props = {
  id: number
  aberto: boolean
  // muda a cada gravacao: a ocorrencia e buscada de novo sem piscar o esqueleto
  versao: number
  codigoLote: (loteId: number) => string
  insumos: Insumo[]
  podeAgir: boolean
  onFechar: () => void
  onTratar: () => void
  onEncerrar: () => void
}

export function PainelOcorrencia({ id, aberto, versao, codigoLote, insumos, podeAgir, onFechar, onTratar, onEncerrar }: Props) {
  const { carga, recarregar } = useCarregar((token) => consultarOcorrencia(token, id), versao)
  const aberta = carga.situacao === 'pronto' && carga.dados.status === 'ABERTA'
  const nomeDe = (insumoId: number) => insumos.find((i) => i.id === insumoId)?.nome ?? `Insumo ${insumoId}`
  const unidadeDe = (insumoId: number) => insumos.find((i) => i.id === insumoId)?.unidade ?? ''

  return (
    <Painel
      aberto={aberto}
      onFechar={onFechar}
      titulo={carga.situacao === 'pronto' ? `Ocorrência no lote ${codigoLote(carga.dados.loteId)}` : 'Ocorrência'}
      descricao={
        carga.situacao === 'pronto'
          ? aberta
            ? `Aberta em ${dataCompleta(carga.dados.dataAbertura)}, há ${diasDesde(carga.dados.dataAbertura)} dias.`
            : `Encerrada em ${carga.dados.dataEncerramento ? dataCompleta(carga.dados.dataEncerramento) : '—'}.`
          : undefined
      }
      rodape={
        podeAgir && aberta ? (
          <>
            <Botao variante="contorno" onClick={onEncerrar} className="min-h-11">
              Encerrar
            </Botao>
            <Botao onClick={onTratar} className="min-h-11">
              Registrar tratamento
            </Botao>
          </>
        ) : undefined
      }
    >
      <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
        {(o) => (
          <div className="flex flex-col gap-5">
            <div className="flex items-start justify-between gap-3">
              <p className="text-[15px]">{o.descricao}</p>
              <Selo nivel={o.status === 'ABERTA' ? 'atencao' : 'neutro'}>{ROTULO_STATUS_OCORRENCIA[o.status]}</Selo>
            </div>
            <div>
              <TituloSecao className="mb-1">Tratamentos</TituloSecao>
              {!o.tratamentos || o.tratamentos.length === 0 ? (
                <Aviso tom="info">Nenhum tratamento registrado ainda.</Aviso>
              ) : (
                <ul>
                  {o.tratamentos.map((t) => (
                    <li key={t.id} className="border-t border-line-soft py-3.5 first:border-t-0">
                      <p className="font-medium">
                        {nomeDe(t.insumoId)}, {numero(t.quantidade)} {unidadeDe(t.insumoId)}
                      </p>
                      <p className="text-[13.5px] text-ink-3">
                        Tratamento · {dataCompleta(t.data)}
                        {t.observacao ? ` · ${t.observacao}` : ''}
                      </p>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        )}
      </Carregavel>
    </Painel>
  )
}
