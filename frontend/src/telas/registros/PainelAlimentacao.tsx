import { useRef, useState, type FormEvent } from 'react'
import { registrarAlimentacao } from '../../api/alimentacao'
import type { Insumo } from '../../api/estoque'
import { hoje, numero } from '../../api/formatar'
import type { Lote } from '../../api/lotes'
import { novoCodigoLancamento } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Botao, Campo, Entrada, Painel, Selecao, useToast } from '../../ui'
import { CampoLote } from '../comum/CampoLote'
import { focarPrimeiroErro, inteiroPositivo } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'

type Props = {
  aberto: boolean
  onFechar: () => void
  lotes: Lote[]
  racoes: Insumo[]
  loteInicial: string
  onConcluir: () => void
}

type Erros = Partial<Record<'lote' | 'insumo' | 'quantidade' | 'data', string>>

// painel lateral: a racao servida ao lote sai do estoque na mesma hora
export function PainelAlimentacao({ aberto, onFechar, lotes, racoes, loteInicial, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())

  const [escolhido, setEscolhido] = useState<string | null>(null)
  const [insumoId, setInsumoId] = useState('')
  const [quantidade, setQuantidade] = useState('')
  const [data, setData] = useState(hoje())
  const [erros, setErros] = useState<Erros>({})

  const loteId = escolhido ?? loteInicial
  const racao = racoes.find((r) => String(r.id) === insumoId)
  const kg = Number(quantidade)

  function fechar() {
    setEscolhido(null)
    setInsumoId('')
    setQuantidade('')
    setData(hoje())
    setErros({})
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const lote = lotes.find((l) => String(l.id) === loteId)
    const novos: Erros = {}
    if (!lote) novos.lote = 'Escolha o lote.'
    if (!racao) novos.insumo = 'Escolha a ração.'
    novos.quantidade = inteiroPositivo(quantidade, undefined, 'A quantidade')
    if (!data || data > hoje()) novos.data = 'Informe uma data que não seja no futuro.'
    const limpos = Object.fromEntries(Object.entries(novos).filter(([, v]) => v)) as Erros
    setErros(limpos)
    if (Object.keys(limpos).length > 0 || !lote || !racao) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      await registrarAlimentacao(token, lote.id, { codigoLancamento: codigo.current, insumoId: racao.id, quantidadeKg: kg, data })
      avisar({ texto: `${numero(kg)} kg de ${racao.nome} registrados no lote ${lote.codigo}.` })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      fechar()
      onConcluir()
    }
  }

  const semDados = lotes.length === 0 || racoes.length === 0

  return (
    <Painel
      aberto={aberto}
      onFechar={fechar}
      titulo="Registrar alimentação"
      descricao="Ração servida ao lote. A quantidade sai do estoque na hora."
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-alimentacao" disabled={enviando || semDados} className="min-h-11">
            {enviando ? 'Registrando...' : 'Registrar alimentação'}
          </Botao>
        </>
      }
    >
      <form id="form-alimentacao" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        {lotes.length === 0 ? <Aviso tom="atencao">Não há lote ativo. Só dá para registrar alimentação em lote ativo.</Aviso> : null}
        {racoes.length === 0 ? <Aviso tom="atencao">Não há ração cadastrada. Cadastre um insumo da categoria Ração no estoque.</Aviso> : null}
        <CampoLote lotes={lotes} valor={loteId} onMudar={setEscolhido} erro={erros.lote} />
        <Campo rotulo="Ração" erro={erros.insumo}>
          {(c) => (
            <Selecao {...c} value={insumoId} onChange={(e) => setInsumoId(e.target.value)}>
              <option value="">Escolha a ração</option>
              {racoes.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.nome} · saldo {numero(r.saldo)} {r.unidade}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo rotulo="Quantidade (kg)" erro={erros.quantidade}>
            {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} placeholder="0" />}
          </Campo>
          <Campo rotulo="Data" erro={erros.data}>
            {(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}
          </Campo>
        </div>
        {racao && kg > racao.saldo ? (
          <Aviso tom="atencao" destaque="Passa do saldo.">
            O estoque de {racao.nome} tem {numero(racao.saldo)} {racao.unidade}. O sistema vai recusar o registro.
          </Aviso>
        ) : null}
      </form>
    </Painel>
  )
}
