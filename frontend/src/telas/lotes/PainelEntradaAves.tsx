import { useRef, useState, type FormEvent } from 'react'
import { numero } from '../../api/formatar'
import { registrarEntradaAnimais, type LoteDetalhe } from '../../api/lotes'
import { novoCodigoLancamento } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { AreaTexto, Aviso, Botao, Campo, Entrada, Painel, useToast } from '../../ui'
import { focarPrimeiroErro, inteiroPositivo } from '../comum/formulario'
import { useEnvio } from './useEnvio'

type Props = {
  lote: LoteDetalhe
  aberto: boolean
  onFechar: () => void
  onConcluir: () => void
}

// painel lateral pra acrescentar aves a um lote que ja existe
export function PainelEntradaAves({ lote, aberto, onFechar, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())
  const [quantidade, setQuantidade] = useState('')
  const [observacao, setObservacao] = useState('')
  const [erroCampo, setErroCampo] = useState<string>()

  const qtd = Number(quantidade)
  const novoSaldo = Number.isInteger(qtd) && qtd > 0 ? lote.saldo + qtd : null

  function fechar() {
    setQuantidade('')
    setObservacao('')
    setErroCampo(undefined)
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const invalido = inteiroPositivo(quantidade)
    setErroCampo(invalido)
    if (invalido) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      const r = await registrarEntradaAnimais(token, lote.id, {
        codigoLancamento: codigo.current,
        quantidade: qtd,
        observacao: observacao.trim() || undefined,
      })
      avisar({ texto: `Entrada registrada. Saldo: ${numero(r.saldo)}.` })
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
      titulo="Adicionar aves"
      descricao={`Lote ${lote.codigo}. Saldo atual: ${numero(lote.saldo)} aves.`}
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-entrada-aves" disabled={enviando} className="min-h-11">
            {enviando ? 'Registrando...' : 'Adicionar'}
          </Botao>
        </>
      }
    >
      <form id="form-entrada-aves" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Quantidade de aves" erro={erroCampo}>
          {(c) => (
            <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} placeholder="0" />
          )}
        </Campo>
        <Campo rotulo="Observação" dica="Opcional. Ex.: origem da remessa.">
          {(c) => <AreaTexto {...c} value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={500} />}
        </Campo>
        {novoSaldo !== null ? (
          <Aviso tom="info" destaque={`O lote passa de ${numero(lote.saldo)} para ${numero(novoSaldo)} aves.`}>
            A idade e a data de alojamento do lote continuam as mesmas.
          </Aviso>
        ) : null}
      </form>
    </Painel>
  )
}
