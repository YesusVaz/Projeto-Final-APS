import { motion } from 'motion/react'
import { useRef, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router'
import { dataCompleta, dataCurta, hoje, numero } from '../../api/formatar'
import { listarLotes, type Lote } from '../../api/lotes'
import { listarProducao, registrarPesagem, registrarPostura, type RegistroProducao } from '../../api/producao'
import { novoCodigoLancamento, ROTULO_ESPECIE } from '../../api/tipos'
import { useCarregar } from '../../hooks/useCarregar'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Botao, Campo, CabecalhoPagina, Cartao, Entrada, EsqueletoLista, EstadoVazio, Estatistica, Pagina, propsItem, Selecao, TituloSecao, useToast } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { focarPrimeiroErro, inteiroPositivo } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'
import { GraficoIndicador } from './GraficoIndicador'

export function Producao() {
  const { carga, recarregar } = useCarregar(listarLotes)
  return (
    <Pagina>
      <CabecalhoPagina titulo="Produção" descricao="Postura das poedeiras e peso dos frangos de corte." />
      <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
        {(lotes) => <Conteudo lotes={lotes.filter((l) => l.status === 'ATIVO')} />}
      </Carregavel>
    </Pagina>
  )
}

function Conteudo({ lotes }: { lotes: Lote[] }) {
  const [params] = useSearchParams()
  const inicial = params.get('lote')
  const [loteId, setLoteId] = useState(lotes.find((l) => String(l.id) === inicial)?.id ?? lotes[0]?.id ?? 0)
  const lote = lotes.find((l) => l.id === loteId)

  if (!lote) {
    return <EstadoVazio icone="producao" titulo="Não há lote ativo" texto="A produção é registrada em lotes ativos." />
  }

  return (
    <>
      <div className="mb-8 max-w-[360px]">
        <Campo rotulo="Lote">
          {(c) => (
            <Selecao {...c} value={loteId} onChange={(e) => setLoteId(Number(e.target.value))}>
              {lotes.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.codigo} · {ROTULO_ESPECIE[l.especie]}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
      </div>
      {/* key: cada lote monta do zero, sem restos do anterior */}
      <ProducaoDoLote key={lote.id} lote={lote} />
    </>
  )
}

function ProducaoDoLote({ lote }: { lote: Lote }) {
  const { carga, recarregar } = useCarregar((token) => listarProducao(token, lote.id))
  const postura = lote.especie === 'POEDEIRA'

  return (
    <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={4} />}>
      {(registros) => {
        const ordenados = [...registros].sort((a, b) => a.data.localeCompare(b.data))
        const ultimo = ordenados.at(-1)
        const serie = ordenados.filter((r) => r.indicador !== null)
        return (
          <div className="grid gap-10 lg:grid-cols-[minmax(0,1fr)_380px]">
            <div className="flex flex-col gap-8">
              {ultimo ? (
                <div className="grid grid-cols-2 gap-6">
                  <Estatistica
                    valor={postura ? numero(ultimo.valor) : `${numero(ultimo.valor)} g`}
                    rotulo={postura ? 'Ovos no último registro' : 'Peso médio'}
                    descricao={dataCompleta(ultimo.data)}
                  />
                  <Estatistica
                    indice={1}
                    valor={ultimo.indicador === null ? '—' : `${ultimo.indicador.toLocaleString('pt-BR', { maximumFractionDigits: 1 })} ${ultimo.unidadeIndicador}`}
                    rotulo={postura ? 'Taxa de postura' : 'Ganho de peso por dia'}
                    descricao={ultimo.indicador === null ? 'Precisa de duas pesagens' : undefined}
                  />
                </div>
              ) : null}

              {serie.length >= 2 ? (
                <div>
                  <TituloSecao className="mb-3">{postura ? 'Taxa de postura ao longo dos dias' : 'Ganho de peso por dia'}</TituloSecao>
                  <GraficoIndicador pontos={serie.map((r) => ({ rotulo: dataCurta(r.data), valor: r.indicador ?? 0 }))} unidade={serie[0].unidadeIndicador} descricao={postura ? 'Taxa de postura' : 'Ganho de peso'} />
                </div>
              ) : null}

              <div>
                <TituloSecao className="mb-1">Registros recentes</TituloSecao>
                {ordenados.length === 0 ? (
                  <EstadoVazio icone="producao" titulo="Nenhum registro ainda" texto={postura ? 'Lance a coleta de ovos do dia ao lado.' : 'Lance a primeira pesagem ao lado.'} />
                ) : (
                  <ul>
                    {[...ordenados].reverse().slice(0, 15).map((r, i) => (
                      <motion.li key={r.id} {...propsItem(i)} className="flex flex-wrap items-center gap-x-4 border-t border-line-soft py-3.5 first:border-t-0">
                        <span className="w-20 font-mono text-[13.5px] text-ink-3">{dataCurta(r.data)}</span>
                        <span className="flex-1">{resumo(r)}</span>
                      </motion.li>
                    ))}
                  </ul>
                )}
              </div>
            </div>

            <FormularioProducao lote={lote} aoGravar={recarregar} />
          </div>
        )
      }}
    </Carregavel>
  )
}

function resumo(r: RegistroProducao): string {
  const indicador = r.indicador === null ? '' : ` · ${r.indicador.toLocaleString('pt-BR', { maximumFractionDigits: 1 })} ${r.unidadeIndicador}`
  return r.tipo === 'POSTURA' ? `${numero(r.valor)} ovos${indicador}` : `${numero(r.valor)} g de peso médio${indicador}`
}

function FormularioProducao({ lote, aoGravar }: { lote: Lote; aoGravar: () => void }) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())
  const [valor, setValor] = useState('')
  const [data, setData] = useState(hoje())
  const [erros, setErros] = useState<Partial<Record<'valor' | 'data', string>>>({})
  const postura = lote.especie === 'POEDEIRA'

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos = {
      valor: inteiroPositivo(valor, undefined, postura ? 'A quantidade de ovos' : 'O peso'),
      data: !data || data > hoje() ? 'Informe uma data que não seja no futuro.' : undefined,
    }
    setErros(novos)
    if (novos.valor || novos.data) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      if (postura) await registrarPostura(token, lote.id, { codigoLancamento: codigo.current, ovos: Number(valor), data })
      else await registrarPesagem(token, lote.id, { codigoLancamento: codigo.current, pesoMedioGramas: Number(valor), data })
      avisar({ texto: postura ? 'Postura registrada.' : 'Pesagem registrada.' })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      setValor('')
      aoGravar()
    }
  }

  return (
    <Cartao className="h-fit">
      <h2 className="text-lg font-medium tracking-[-0.015em]">{postura ? 'Registrar postura' : 'Registrar pesagem'}</h2>
      <p className="mt-1 mb-5 text-sm text-ink-3">Cada lote tem um registro por dia.</p>
      <form ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo={postura ? 'Ovos coletados' : 'Peso médio das aves (g)'} erro={erros.valor}>
          {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={valor} onChange={(e) => setValor(e.target.value)} placeholder="0" />}
        </Campo>
        <Campo rotulo="Data" erro={erros.data}>
          {(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}
        </Campo>
        <Botao type="submit" disabled={enviando} className="min-h-11">
          {enviando ? 'Registrando...' : postura ? 'Registrar postura' : 'Registrar pesagem'}
        </Botao>
      </form>
    </Cartao>
  )
}
