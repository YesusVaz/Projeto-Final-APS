import { useState, type FormEvent } from 'react'
import { dataHora } from '../../api/formatar'
import { ROTULO_CATEGORIA_CHAMADO, responderChamado, type Chamado } from '../../api/suporte'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, AreaTexto, Botao, Campo, Painel, Selecao, useToast } from '../../ui'
import { useEnvio } from '../lotes/useEnvio'

type Props = {
  // null = fechado
  chamado: Chamado | null
  onFechar: () => void
  onConcluir: () => void
}

type Andamento = 'EM_ATENDIMENTO' | 'RESOLVIDO'

// o gestor responde e muda o andamento
export function PainelResposta({ chamado, onFechar, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const [status, setStatus] = useState<Andamento>('RESOLVIDO')
  const [resposta, setResposta] = useState('')
  const [erroCampo, setErroCampo] = useState('')

  // o painel fica montado: quando abre outro chamado, comeca do que ele ja tem
  const [ultimo, setUltimo] = useState<number | null>(null)
  if (chamado && chamado.id !== ultimo) {
    setUltimo(chamado.id)
    setStatus('RESOLVIDO')
    setResposta(chamado.resposta ?? '')
    setErroCampo('')
  }

  function fechar() {
    setErroCampo('')
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    if (!chamado) return
    if (status === 'RESOLVIDO' && !resposta.trim()) {
      setErroCampo('Escreva a resposta antes de resolver.')
      return
    }
    setErroCampo('')
    const ok = await enviar(async () => {
      await responderChamado(token, chamado.id, status, resposta.trim())
      avisar({ texto: status === 'RESOLVIDO' ? 'Chamado resolvido.' : 'Chamado em atendimento.' })
    })
    if (ok) {
      fechar()
      onConcluir()
    }
  }

  return (
    <Painel
      aberto={chamado !== null}
      onFechar={fechar}
      titulo="Responder chamado"
      descricao={chamado ? `${ROTULO_CATEGORIA_CHAMADO[chamado.categoria]} de ${chamado.solicitante}, em ${dataHora(chamado.abertoEm)}.` : undefined}
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-resposta" disabled={enviando} className="min-h-11">
            {enviando ? 'Salvando...' : 'Salvar'}
          </Botao>
        </>
      }
    >
      {chamado ? (
        <form id="form-resposta" onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
          {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
          <div>
            <p className="font-medium">{chamado.assunto}</p>
            <p className="mt-1 text-[14.5px] whitespace-pre-line text-ink-2">{chamado.descricao}</p>
          </div>
          <Campo rotulo="Andamento">
            {(c) => (
              <Selecao {...c} value={status} onChange={(e) => setStatus(e.target.value as Andamento)}>
                <option value="EM_ATENDIMENTO">Em atendimento</option>
                <option value="RESOLVIDO">Resolvido</option>
              </Selecao>
            )}
          </Campo>
          <Campo rotulo="Resposta" erro={erroCampo || undefined}>
            {(c) => <AreaTexto {...c} className="h-36" value={resposta} onChange={(e) => setResposta(e.target.value)} maxLength={1000} />}
          </Campo>
        </form>
      ) : null}
    </Painel>
  )
}
