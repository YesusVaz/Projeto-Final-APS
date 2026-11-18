import { motion } from 'motion/react'
import { useState } from 'react'
import type { Insumo } from '../../api/estoque'
import { dataCurta, diasDesde } from '../../api/formatar'
import type { Lote } from '../../api/lotes'
import { listarOcorrencias } from '../../api/sanidade'
import { ROTULO_STATUS_OCORRENCIA } from '../../api/tiposExtras'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import { Botao, EsqueletoLista, EstadoVazio, Fichas, Icone, propsItem, Selo } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { DialogoEncerrarOcorrencia, DialogoNovaOcorrencia, DialogoTratamento } from './DialogosSanidade'
import { PainelOcorrencia } from './PainelOcorrencia'

type Filtro = 'abertas' | 'todas'
type Dialogo = 'nova' | 'tratar' | 'encerrar' | null

export function ListaOcorrencias({ lotes, insumos }: { lotes: Lote[]; insumos: Insumo[] }) {
  const { perfil } = useLogado()
  const { carga, recarregar } = useCarregar(listarOcorrencias)
  const [filtro, setFiltro] = useState<Filtro>('abertas')
  const [selecionada, setSelecionada] = useState<number | null>(null)
  const [painelAberto, setPainelAberto] = useState(false)
  // sobe a cada gravacao, pro painel buscar a ocorrencia de novo
  const [versao, setVersao] = useState(0)
  const [dialogo, setDialogo] = useState<Dialogo>(null)

  const podeRegistrar = permite(perfil, 'registrarOcorrencia')
  const codigoDe = (id: number) => lotes.find((l) => l.id === id)?.codigo ?? `Lote ${id}`

  function depoisDeGravar() {
    recarregar()
    setVersao((v) => v + 1)
  }

  function abrir(id: number) {
    setSelecionada(id)
    setPainelAberto(true)
  }

  return (
    <>
      <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={4} />}>
        {(ocorrencias) => {
          const abertas = ocorrencias.filter((o) => o.status === 'ABERTA')
          const visiveis = filtro === 'abertas' ? abertas : ocorrencias
          return (
            <>
              <div className="mb-5 flex flex-wrap items-center justify-between gap-4">
                <Fichas
                  rotulo="Filtrar ocorrências"
                  valor={filtro}
                  onMudar={setFiltro}
                  opcoes={[
                    { id: 'abertas', rotulo: 'Abertas', contador: abertas.length },
                    { id: 'todas', rotulo: 'Todas', contador: ocorrencias.length },
                  ]}
                />
                {podeRegistrar ? (
                  <Botao variante="contorno" onClick={() => setDialogo('nova')}>
                    <Icone nome="mais" tamanho={16} />
                    Nova ocorrência
                  </Botao>
                ) : null}
              </div>

              {visiveis.length === 0 ? (
                <EstadoVazio
                  icone="sanidade"
                  titulo={filtro === 'abertas' ? 'Nenhuma ocorrência aberta' : 'Ainda não há ocorrências'}
                  texto="Sintomas e problemas de saúde dos lotes aparecem aqui quando alguém abre uma ocorrência."
                />
              ) : (
                <ul>
                  {visiveis.map((o, i) => (
                    <motion.li key={o.id} {...propsItem(i)} className="border-t border-line-soft first:border-t-0">
                      <button
                        type="button"
                        onClick={() => abrir(o.id)}
                        className="grid min-h-14 w-full items-center gap-x-6 gap-y-1 rounded py-4 text-left transition-colors hover:bg-ink/[0.025] md:grid-cols-[140px_minmax(0,1fr)_90px_80px_110px]"
                      >
                        <span className="font-mono font-medium">{codigoDe(o.loteId)}</span>
                        <span className="min-w-0 truncate">{o.descricao}</span>
                        <span className="text-[13.5px] text-ink-3">Aberta em {dataCurta(o.dataAbertura)}</span>
                        <span className="font-mono text-[13.5px] text-ink-3">
                          {o.status === 'ABERTA' ? `${diasDesde(o.dataAbertura)} dias` : '—'}
                        </span>
                        <span>
                          <Selo nivel={o.status === 'ABERTA' ? 'atencao' : 'neutro'}>{ROTULO_STATUS_OCORRENCIA[o.status]}</Selo>
                        </span>
                      </button>
                    </motion.li>
                  ))}
                </ul>
              )}
            </>
          )
        }}
      </Carregavel>

      {selecionada !== null ? (
        <PainelOcorrencia
          key={selecionada}
          id={selecionada}
          aberto={painelAberto}
          versao={versao}
          codigoLote={(id) => codigoDe(id)}
          insumos={insumos}
          podeAgir={podeRegistrar}
          onFechar={() => setPainelAberto(false)}
          onTratar={() => setDialogo('tratar')}
          onEncerrar={() => setDialogo('encerrar')}
        />
      ) : null}

      <DialogoNovaOcorrencia
        aberto={dialogo === 'nova'}
        lotes={lotes.filter((l) => l.status === 'ATIVO')}
        onFechar={() => setDialogo(null)}
        onConcluir={depoisDeGravar}
      />
      {selecionada !== null ? (
        <>
          <DialogoTratamento
            aberto={dialogo === 'tratar'}
            ocorrenciaId={selecionada}
            medicamentos={insumos.filter((i) => i.categoria === 'MEDICAMENTO' && i.ativo)}
            onFechar={() => setDialogo(null)}
            onConcluir={depoisDeGravar}
          />
          <DialogoEncerrarOcorrencia
            aberto={dialogo === 'encerrar'}
            ocorrenciaId={selecionada}
            onFechar={() => setDialogo(null)}
            onConcluir={depoisDeGravar}
          />
        </>
      ) : null}
    </>
  )
}
