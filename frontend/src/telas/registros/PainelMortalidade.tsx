import { useRef, useState, type FormEvent } from 'react'
import { numero } from '../../api/formatar'
import { registrarMortalidade, type Lote } from '../../api/lotes'
import { novoCodigoLancamento, ROTULO_NIVEL } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { AreaTexto, Aviso, Botao, Campo, Contador, Painel, Selecao, useToast } from '../../ui'
import { CampoLote } from '../comum/CampoLote'
import { focarPrimeiroErro } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'

const CAUSAS = ['Doença respiratória', 'Estresse térmico', 'Acidente', 'Outra causa']

type Props = {
  aberto: boolean
  onFechar: () => void
  lotes: Lote[]
  // lote que ja vem escolhido (link do detalhe e dos alertas)
  loteInicial: string
  onConcluir: () => void
}

// painel lateral sobre a lista de lotes: conta as aves mortas, e o saldo e o alerta voltam no aviso
export function PainelMortalidade({ aberto, onFechar, lotes, loteInicial, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  // chave de idempotencia: so troca depois de gravar
  const codigo = useRef(novoCodigoLancamento())

  const [escolhido, setEscolhido] = useState<string | null>(null)
  const [quantidade, setQuantidade] = useState(0)
  const [causa, setCausa] = useState(CAUSAS[0])
  const [observacao, setObservacao] = useState('')
  const [erros, setErros] = useState<Partial<Record<'lote' | 'quantidade', string>>>({})

  const loteId = escolhido ?? loteInicial
  const lote = lotes.find((l) => String(l.id) === loteId)

  function fechar() {
    setEscolhido(null)
    setQuantidade(0)
    setCausa(CAUSAS[0])
    setObservacao('')
    setErros({})
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos: typeof erros = {}
    if (!lote) novos.lote = 'Escolha o lote.'
    if (!Number.isInteger(quantidade) || quantidade < 1) novos.quantidade = 'Informe quantas aves morreram (1 ou mais).'
    else if (lote && quantidade > lote.saldo) novos.quantidade = `O lote só tem ${numero(lote.saldo)} aves.`
    setErros(novos)
    if (Object.keys(novos).length > 0 || !lote) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      const detalhe = observacao.trim()
      const r = await registrarMortalidade(token, lote.id, {
        codigoLancamento: codigo.current,
        quantidade,
        observacao: detalhe ? `${causa}: ${detalhe}` : causa,
      })
      const nivel = r.nivelAlerta === 'NORMAL' ? '' : ` Alerta em nível ${ROTULO_NIVEL[r.nivelAlerta].toLowerCase()}.`
      avisar({ texto: `Lote ${lote.codigo}: saldo de ${numero(r.saldo)} aves.${nivel}` })
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
      titulo="Registrar mortalidade"
      descricao="Conte as aves mortas do lote. O sistema recalcula o saldo e o alerta do dia."
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-mortalidade" disabled={enviando || lotes.length === 0} className="min-h-11">
            {enviando ? 'Registrando...' : 'Registrar mortalidade'}
          </Botao>
        </>
      }
    >
      <form id="form-mortalidade" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        {lotes.length === 0 ? <Aviso tom="atencao">Não há lote ativo. Só dá para registrar mortalidade em lote ativo.</Aviso> : null}
        <CampoLote lotes={lotes} valor={loteId} onMudar={setEscolhido} erro={erros.lote} />
        <Campo rotulo="Aves mortas" erro={erros.quantidade}>
          {(c) => <Contador {...c} valor={quantidade} onMudar={setQuantidade} rotuloMenos="Uma ave a menos" rotuloMais="Uma ave a mais" />}
        </Campo>
        <Campo rotulo="Causa">
          {(c) => (
            <Selecao {...c} value={causa} onChange={(e) => setCausa(e.target.value)}>
              {CAUSAS.map((x) => (
                <option key={x}>{x}</option>
              ))}
            </Selecao>
          )}
        </Campo>
        <Campo rotulo="Observação" dica="Opcional.">
          {(c) => <AreaTexto {...c} value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={400} />}
        </Campo>
        {lote && quantidade > 0 && quantidade <= lote.saldo ? (
          <Aviso tom="info" destaque={`O saldo passa de ${numero(lote.saldo)} para ${numero(lote.saldo - quantidade)} aves.`}>
            O alerta do dia é recalculado ao registrar.
          </Aviso>
        ) : null}
      </form>
    </Painel>
  )
}
