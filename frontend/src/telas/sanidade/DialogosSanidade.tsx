import { useRef, useState, type FormEvent } from 'react'
import type { Insumo } from '../../api/estoque'
import { hoje } from '../../api/formatar'
import type { Lote } from '../../api/lotes'
import { abrirOcorrencia, encerrarOcorrencia, registrarTratamento } from '../../api/sanidade'
import { novoCodigoLancamento } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { AreaTexto, Aviso, Botao, Campo, Dialogo, Entrada, Selecao, useToast } from '../../ui'
import { CampoLote } from '../comum/CampoLote'
import { inteiroPositivo } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'

type Base = { aberto: boolean; onFechar: () => void; onConcluir: () => void }

function Rodape({ formulario, enviando, rotulo, rotuloEnviando, onFechar }: { formulario: string; enviando: boolean; rotulo: string; rotuloEnviando: string; onFechar: () => void }) {
  return (
    <>
      <Botao variante="fantasma" onClick={onFechar} className="min-h-11">
        Cancelar
      </Botao>
      <Botao type="submit" form={formulario} disabled={enviando} className="min-h-11">
        {enviando ? rotuloEnviando : rotulo}
      </Botao>
    </>
  )
}

export function DialogoNovaOcorrencia({ aberto, lotes, onFechar, onConcluir }: Base & { lotes: Lote[] }) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const [loteId, setLoteId] = useState('')
  const [descricao, setDescricao] = useState('')
  const [data, setData] = useState(hoje())
  const [erros, setErros] = useState<Partial<Record<'lote' | 'descricao', string>>>({})

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos: typeof erros = {}
    if (!loteId) novos.lote = 'Escolha o lote.'
    if (!descricao.trim()) novos.descricao = 'Descreva o que está acontecendo.'
    setErros(novos)
    if (Object.keys(novos).length > 0) return
    const ok = await enviar(async () => {
      await abrirOcorrencia(token, Number(loteId), { descricao: descricao.trim(), data })
      avisar({ texto: 'Ocorrência aberta.' })
    })
    if (ok) {
      setLoteId('')
      setDescricao('')
      onFechar()
      onConcluir()
    }
  }

  return (
    <Dialogo
      aberto={aberto}
      onFechar={onFechar}
      titulo="Nova ocorrência"
      descricao="Registre sintomas ou problemas de saúde. Depois dá para lançar os tratamentos."
      rodape={<Rodape formulario="form-ocorrencia" enviando={enviando} rotulo="Abrir ocorrência" rotuloEnviando="Abrindo..." onFechar={onFechar} />}
    >
      <form id="form-ocorrencia" onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <CampoLote lotes={lotes} valor={loteId} onMudar={setLoteId} erro={erros.lote} />
        <Campo rotulo="O que aconteceu" erro={erros.descricao}>
          {(c) => <AreaTexto {...c} value={descricao} onChange={(e) => setDescricao(e.target.value)} maxLength={500} />}
        </Campo>
        <Campo rotulo="Data">{(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}</Campo>
      </form>
    </Dialogo>
  )
}

export function DialogoTratamento({ aberto, ocorrenciaId, medicamentos, onFechar, onConcluir }: Base & { ocorrenciaId: number; medicamentos: Insumo[] }) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const codigo = useRef(novoCodigoLancamento())
  const [insumoId, setInsumoId] = useState('')
  const [quantidade, setQuantidade] = useState('')
  const [data, setData] = useState(hoje())
  const [observacao, setObservacao] = useState('')
  const [erros, setErros] = useState<Partial<Record<'insumo' | 'quantidade', string>>>({})

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const remedio = medicamentos.find((m) => String(m.id) === insumoId)
    const novos: typeof erros = {}
    if (!remedio) novos.insumo = 'Escolha o medicamento.'
    novos.quantidade = inteiroPositivo(quantidade)
    const limpos = Object.fromEntries(Object.entries(novos).filter(([, v]) => v)) as typeof erros
    setErros(limpos)
    if (Object.keys(limpos).length > 0 || !remedio) return
    const ok = await enviar(async () => {
      await registrarTratamento(token, ocorrenciaId, {
        codigoLancamento: codigo.current,
        insumoId: remedio.id,
        quantidade: Number(quantidade),
        data,
        observacao: observacao.trim() || undefined,
      })
      avisar({ texto: `Tratamento com ${remedio.nome} registrado.` })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      setQuantidade('')
      setObservacao('')
      onFechar()
      onConcluir()
    }
  }

  return (
    <Dialogo
      aberto={aberto}
      onFechar={onFechar}
      titulo="Registrar tratamento"
      descricao="A quantidade sai do estoque de medicamentos."
      rodape={<Rodape formulario="form-tratamento" enviando={enviando} rotulo="Registrar" rotuloEnviando="Registrando..." onFechar={onFechar} />}
    >
      <form id="form-tratamento" onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Medicamento" erro={erros.insumo} dica={medicamentos.length === 0 ? 'Não há medicamento cadastrado no estoque.' : undefined}>
          {(c) => (
            <Selecao {...c} value={insumoId} onChange={(e) => setInsumoId(e.target.value)}>
              <option value="">Escolha o medicamento</option>
              {medicamentos.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.nome} · saldo {m.saldo} {m.unidade}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo rotulo="Quantidade" erro={erros.quantidade}>
            {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} />}
          </Campo>
          <Campo rotulo="Data">{(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}</Campo>
        </div>
        <Campo rotulo="Observação" dica="Opcional.">
          {(c) => <AreaTexto {...c} value={observacao} onChange={(e) => setObservacao(e.target.value)} maxLength={300} />}
        </Campo>
      </form>
    </Dialogo>
  )
}

export function DialogoEncerrarOcorrencia({ aberto, ocorrenciaId, onFechar, onConcluir }: Base & { ocorrenciaId: number }) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const ok = await enviar(async () => {
      await encerrarOcorrencia(token, ocorrenciaId, { data: hoje() })
      avisar({ texto: 'Ocorrência encerrada.' })
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
      titulo="Encerrar ocorrência"
      descricao="Depois de encerrada, não dá mais para registrar tratamentos nela. Isso não pode ser desfeito."
      rodape={<Rodape formulario="form-encerrar-ocorrencia" enviando={enviando} rotulo="Encerrar" rotuloEnviando="Encerrando..." onFechar={onFechar} />}
    >
      <form id="form-encerrar-ocorrencia" onSubmit={(e) => void submeter(e)}>
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
      </form>
    </Dialogo>
  )
}
