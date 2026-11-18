import { useRef, useState, type FormEvent } from 'react'
import { hoje } from '../../api/formatar'
import type { Lote } from '../../api/lotes'
import { registrarPrograma } from '../../api/manejo'
import { novoCodigoLancamento } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { AreaTexto, Aviso, Botao, Campo, Entrada, Painel, useToast } from '../../ui'
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

type Erros = Partial<Record<'lote' | 'horas' | 'lux' | 'data', string>>

export function PainelIluminacao({ aberto, onFechar, lotes, loteInicial, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())

  const [escolhido, setEscolhido] = useState<string | null>(null)
  const [horas, setHoras] = useState('')
  const [lux, setLux] = useState('')
  const [data, setData] = useState(hoje())
  const [observacao, setObservacao] = useState('')
  const [erros, setErros] = useState<Erros>({})

  const loteId = escolhido ?? loteInicial

  function fechar() {
    setEscolhido(null)
    setHoras('')
    setLux('')
    setData(hoje())
    setObservacao('')
    setErros({})
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const lote = lotes.find((l) => String(l.id) === loteId)
    const h = Number(horas)
    const l = Number(lux)
    const novos: Erros = {}
    if (!lote) novos.lote = 'Escolha o lote.'
    if (horas.trim() === '' || !Number.isInteger(h) || h < 1 || h > 24) novos.horas = 'Informe as horas de luz por dia, de 1 a 24.'
    if (lux.trim() !== '' && (!Number.isInteger(l) || l < 1)) novos.lux = 'A intensidade precisa ser um número inteiro maior que zero.'
    if (!data || data > hoje()) novos.data = 'Informe uma data que não seja no futuro.'
    setErros(novos)
    if (Object.keys(novos).length > 0 || !lote) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      await registrarPrograma(token, lote.id, {
        codigoLancamento: codigo.current,
        horasDeLuz: h,
        intensidadeLux: lux.trim() === '' ? undefined : l,
        dataInicio: data,
        observacao: observacao.trim() || undefined,
      })
      avisar({ texto: `Lote ${lote.codigo}: ${h} horas de luz por dia.` })
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
      titulo="Programa de iluminação"
      descricao="Quantas horas de luz o lote recebe por dia, a partir de uma data. O programa anterior fica no histórico."
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-iluminacao" disabled={enviando || lotes.length === 0} className="min-h-11">
            {enviando ? 'Registrando...' : 'Registrar programa'}
          </Botao>
        </>
      }
    >
      <form id="form-iluminacao" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        {lotes.length === 0 ? <Aviso tom="atencao">Não há lote ativo.</Aviso> : null}
        <CampoLote lotes={lotes} valor={loteId} onMudar={setEscolhido} erro={erros.lote} />
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo rotulo="Horas de luz por dia" erro={erros.horas}>
            {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={1} max={24} step={1} value={horas} onChange={(e) => setHoras(e.target.value)} placeholder="16" />}
          </Campo>
          <Campo rotulo="Intensidade (lux)" dica="Opcional." erro={erros.lux}>
            {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={lux} onChange={(e) => setLux(e.target.value)} placeholder="20" />}
          </Campo>
        </div>
        <Campo rotulo="Vale a partir de" erro={erros.data}>
          {(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}
        </Campo>
        <Campo rotulo="Observação" dica="Opcional.">
          {(c) => <AreaTexto {...c} value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={300} />}
        </Campo>
      </form>
    </Painel>
  )
}
