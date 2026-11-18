import { useRef, useState, type FormEvent } from 'react'
import { encerrarLote, registrarBaixa, registrarMortalidade, type LoteDetalhe } from '../../api/lotes'
import { novoCodigoLancamento, ROTULO_MOTIVO, ROTULO_NIVEL, type MotivoSaida } from '../../api/tipos'
import { numero } from '../../api/formatar'
import { useLogado } from '../../sessao/sessaoContexto'
import { AreaTexto, Aviso, Botao, Campo, Dialogo, Entrada, Selecao, useToast } from '../../ui'
import { useEnvio } from './useEnvio'

type PropsDialogoLote = {
  lote: LoteDetalhe
  aberto: boolean
  onFechar: () => void
  // chamado depois de gravar, pra tela buscar os dados de novo
  onConcluir: () => void
}

const MOTIVOS = Object.keys(ROTULO_MOTIVO) as MotivoSaida[]

function inteiroPositivo(texto: string, maximo: number): string | undefined {
  const n = Number(texto)
  if (!Number.isInteger(n) || n < 1) return 'Informe um número inteiro maior que zero.'
  if (n > maximo) return `O lote só tem ${numero(maximo)} aves.`
  return undefined
}

function SelecaoMotivo({ valor, onMudar }: { valor: MotivoSaida; onMudar: (m: MotivoSaida) => void }) {
  return (
    <Campo rotulo="Motivo">
      {(c) => (
        <Selecao {...c} value={valor} onChange={(e) => onMudar(e.target.value as MotivoSaida)}>
          {MOTIVOS.map((m) => (
            <option key={m} value={m}>
              {ROTULO_MOTIVO[m]}
            </option>
          ))}
        </Selecao>
      )}
    </Campo>
  )
}

export function DialogoMortalidade({ lote, aberto, onFechar, onConcluir }: PropsDialogoLote) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const codigo = useRef(novoCodigoLancamento())
  const [quantidade, setQuantidade] = useState('')
  const [observacao, setObservacao] = useState('')
  const [erroCampo, setErroCampo] = useState<string>()

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const invalido = inteiroPositivo(quantidade, lote.saldo)
    setErroCampo(invalido)
    if (invalido) return
    const ok = await enviar(async () => {
      const r = await registrarMortalidade(token, lote.id, {
        codigoLancamento: codigo.current,
        quantidade: Number(quantidade),
        observacao: observacao.trim() || undefined,
      })
      const nivel = r.nivelAlerta === 'NORMAL' ? '' : ` Nível de alerta: ${ROTULO_NIVEL[r.nivelAlerta]}.`
      avisar({ texto: `Mortalidade registrada.${nivel} Saldo: ${numero(r.saldo)}.` })
    })
    if (ok) {
      onFechar()
      onConcluir()
    }
  }

  return (
    <Dialogo
      aberto={aberto}
      onFechar={onFechar}
      titulo="Registrar mortalidade"
      descricao={`Lote ${lote.codigo}. Saldo atual: ${numero(lote.saldo)} aves.`}
      rodape={
        <>
          <Botao variante="fantasma" onClick={onFechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-mortalidade" disabled={enviando} className="min-h-11">
            {enviando ? 'Registrando...' : 'Registrar'}
          </Botao>
        </>
      }
    >
      <form id="form-mortalidade" onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Quantidade de aves" erro={erroCampo}>
          {(c) => (
            <Entrada {...c} mono type="number" inputMode="numeric" min={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} />
          )}
        </Campo>
        <Campo rotulo="Observação" dica="Opcional. Ex.: causa provável.">
          {(c) => <AreaTexto {...c} value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={500} />}
        </Campo>
      </form>
    </Dialogo>
  )
}

export function DialogoBaixa({ lote, aberto, onFechar, onConcluir }: PropsDialogoLote) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const codigo = useRef(novoCodigoLancamento())
  const [quantidade, setQuantidade] = useState('')
  const [motivo, setMotivo] = useState<MotivoSaida>('VENDA')
  const [observacao, setObservacao] = useState('')
  const [erroCampo, setErroCampo] = useState<string>()

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const invalido = inteiroPositivo(quantidade, lote.saldo)
    setErroCampo(invalido)
    if (invalido) return
    const ok = await enviar(async () => {
      const r = await registrarBaixa(token, lote.id, {
        codigoLancamento: codigo.current,
        quantidade: Number(quantidade),
        motivo,
        observacao: observacao.trim() || undefined,
      })
      avisar({ texto: `Baixa registrada. Saldo: ${numero(r.saldo)}.` })
    })
    if (ok) {
      onFechar()
      onConcluir()
    }
  }

  return (
    <Dialogo
      aberto={aberto}
      onFechar={onFechar}
      titulo="Registrar baixa"
      descricao={`Saída de aves do lote ${lote.codigo} (venda, descarte ou transferência).`}
      rodape={
        <>
          <Botao variante="fantasma" onClick={onFechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-baixa" disabled={enviando} className="min-h-11">
            {enviando ? 'Registrando...' : 'Registrar'}
          </Botao>
        </>
      }
    >
      <form id="form-baixa" onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Quantidade de aves" erro={erroCampo}>
          {(c) => (
            <Entrada {...c} mono type="number" inputMode="numeric" min={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} />
          )}
        </Campo>
        <SelecaoMotivo valor={motivo} onMudar={setMotivo} />
        <Campo rotulo="Observação" dica="Opcional.">
          {(c) => <AreaTexto {...c} value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={500} />}
        </Campo>
      </form>
    </Dialogo>
  )
}

export function DialogoEncerrar({ lote, aberto, onFechar, onConcluir }: PropsDialogoLote) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const codigo = useRef(novoCodigoLancamento())
  const [motivo, setMotivo] = useState<MotivoSaida>('VENDA')

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const ok = await enviar(async () => {
      await encerrarLote(token, lote.id, { codigoLancamento: codigo.current, motivo })
      avisar({ texto: `Lote ${lote.codigo} encerrado.` })
    })
    if (ok) {
      onFechar()
      onConcluir()
    }
  }

  return (
    <Dialogo
      aberto={aberto}
      onFechar={onFechar}
      titulo="Encerrar lote"
      descricao={`O lote ${lote.codigo} deixa de receber registros. As ${numero(lote.saldo)} aves restantes saem pelo motivo escolhido. Isso não pode ser desfeito.`}
      rodape={
        <>
          <Botao variante="fantasma" onClick={onFechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-encerrar" variante="perigo" disabled={enviando} className="min-h-11">
            {enviando ? 'Encerrando...' : 'Encerrar lote'}
          </Botao>
        </>
      }
    >
      <form id="form-encerrar" onSubmit={(e) => void submeter(e)} className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <SelecaoMotivo valor={motivo} onMudar={setMotivo} />
      </form>
    </Dialogo>
  )
}
