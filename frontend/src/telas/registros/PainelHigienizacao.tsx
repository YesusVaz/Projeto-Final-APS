import { useRef, useState, type FormEvent } from 'react'
import { hoje } from '../../api/formatar'
import type { Lote } from '../../api/lotes'
import { registrarHigienizacao, ROTULO_HIGIENIZACAO, type TipoHigienizacao } from '../../api/manejo'
import { novoCodigoLancamento } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { AreaTexto, Aviso, Botao, Campo, Entrada, Painel, Selecao, useToast } from '../../ui'
import { CampoLote } from '../comum/CampoLote'
import { focarPrimeiroErro } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'

type Props = {
  aberto: boolean
  onFechar: () => void
  lotes: Lote[]
  loteInicial: string
  onConcluir: () => void
}

type Erros = Partial<Record<'lote' | 'tipo' | 'data', string>>

const TIPOS = Object.keys(ROTULO_HIGIENIZACAO) as TipoHigienizacao[]

export function PainelHigienizacao({ aberto, onFechar, lotes, loteInicial, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())

  const [escolhido, setEscolhido] = useState<string | null>(null)
  const [tipo, setTipo] = useState<TipoHigienizacao | ''>('')
  const [data, setData] = useState(hoje())
  const [observacao, setObservacao] = useState('')
  const [erros, setErros] = useState<Erros>({})

  const loteId = escolhido ?? loteInicial

  function fechar() {
    setEscolhido(null)
    setTipo('')
    setData(hoje())
    setObservacao('')
    setErros({})
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const lote = lotes.find((l) => String(l.id) === loteId)
    const novos: Erros = {}
    if (!lote) novos.lote = 'Escolha o lote.'
    if (!tipo) novos.tipo = 'Escolha o que foi feito.'
    if (!data || data > hoje()) novos.data = 'Informe uma data que não seja no futuro.'
    setErros(novos)
    if (Object.keys(novos).length > 0 || !lote || !tipo) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      await registrarHigienizacao(token, lote.id, { codigoLancamento: codigo.current, tipo, data, observacao: observacao.trim() || undefined })
      avisar({ texto: `${ROTULO_HIGIENIZACAO[tipo]} registrada no lote ${lote.codigo}.` })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      fechar()
      onConcluir()
    }
  }

  return (
    <Painel
      aberto={aberto}
      onFechar={fechar}
      titulo="Registrar higienização"
      descricao="Limpeza e desinfecção do galpão do lote."
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-higienizacao" disabled={enviando || lotes.length === 0} className="min-h-11">
            {enviando ? 'Registrando...' : 'Registrar higienização'}
          </Botao>
        </>
      }
    >
      <form id="form-higienizacao" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        {lotes.length === 0 ? <Aviso tom="atencao">Não há lote ativo.</Aviso> : null}
        <CampoLote lotes={lotes} valor={loteId} onMudar={setEscolhido} erro={erros.lote} />
        <Campo rotulo="O que foi feito" erro={erros.tipo}>
          {(c) => (
            <Selecao {...c} value={tipo} onChange={(e) => setTipo(e.target.value as TipoHigienizacao | '')}>
              <option value="">Escolha</option>
              {TIPOS.map((t) => (
                <option key={t} value={t}>
                  {ROTULO_HIGIENIZACAO[t]}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <Campo rotulo="Data" erro={erros.data}>
          {(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}
        </Campo>
        <Campo rotulo="Observação" dica="Opcional. Produto usado, por exemplo.">
          {(c) => <AreaTexto {...c} value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={300} />}
        </Campo>
      </form>
    </Painel>
  )
}
