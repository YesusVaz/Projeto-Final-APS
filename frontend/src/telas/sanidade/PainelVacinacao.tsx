import { useRef, useState, type FormEvent } from 'react'
import type { Insumo } from '../../api/estoque'
import { hoje, numero } from '../../api/formatar'
import type { Lote } from '../../api/lotes'
import { registrarVacinacao } from '../../api/sanidade'
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
  vacinas: Insumo[]
  // lote que ja vem escolhido (link dos alertas de vacina atrasada)
  loteInicial: string
  onConcluir: () => void
}

type Erros = Partial<Record<'lote' | 'vacina' | 'doses' | 'data' | 'proxima', string>>

// painel lateral sobre a tela de sanidade, como no desenho
export function PainelVacinacao({ aberto, onFechar, lotes, vacinas, loteInicial, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())

  const [lote, setLote] = useState<string | null>(null)
  const [vacinaId, setVacinaId] = useState('')
  const [doses, setDoses] = useState('')
  const [data, setData] = useState(hoje())
  const [proxima, setProxima] = useState('')
  const [erros, setErros] = useState<Erros>({})

  const loteId = lote ?? loteInicial
  const vacina = vacinas.find((v) => String(v.id) === vacinaId)
  const qtd = Number(doses)
  const saldoDepois = vacina && Number.isInteger(qtd) && qtd > 0 ? vacina.saldo - qtd : null

  function limpar() {
    setLote(null)
    setVacinaId('')
    setDoses('')
    setData(hoje())
    setProxima('')
    setErros({})
  }
  function fechar() {
    limpar()
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const alvo = lotes.find((l) => String(l.id) === loteId)
    const novos: Erros = {}
    if (!alvo) novos.lote = 'Escolha o lote.'
    if (!vacina) novos.vacina = 'Escolha a vacina.'
    novos.doses = inteiroPositivo(doses, undefined, 'O número de doses')
    if (!data || data > hoje()) novos.data = 'Não pode ser no futuro.'
    if (proxima && proxima <= data) novos.proxima = 'Precisa ser depois da data.'
    const limpos = Object.fromEntries(Object.entries(novos).filter(([, v]) => v)) as Erros
    setErros(limpos)
    if (Object.keys(limpos).length > 0 || !alvo || !vacina) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      await registrarVacinacao(token, alvo.id, {
        codigoLancamento: codigo.current,
        insumoId: vacina.id,
        doses: Number(doses),
        data,
        proximaDose: proxima || undefined,
      })
      avisar({ texto: `Vacinação com ${vacina.nome} registrada no lote ${alvo.codigo}.` })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      fechar()
      onConcluir()
    }
  }

  const semDados = lotes.length === 0 || vacinas.length === 0

  return (
    <Painel
      aberto={aberto}
      onFechar={fechar}
      titulo="Registrar vacinação"
      descricao="As doses aplicadas saem do estoque de vacinas."
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-vacinacao" disabled={enviando || semDados} className="min-h-11">
            {enviando ? 'Registrando...' : 'Registrar vacinação'}
          </Botao>
        </>
      }
    >
      <form id="form-vacinacao" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        {lotes.length === 0 ? <Aviso tom="atencao">Não há lote ativo. Só dá para vacinar lote ativo.</Aviso> : null}
        {vacinas.length === 0 ? <Aviso tom="atencao">Não há vacina cadastrada. Peça ao gestor para cadastrar no estoque, na categoria Vacina.</Aviso> : null}
        <CampoLote lotes={lotes} valor={loteId} onMudar={setLote} erro={erros.lote} />
        <Campo rotulo="Vacina" erro={erros.vacina}>
          {(c) => (
            <Selecao {...c} value={vacinaId} onChange={(e) => setVacinaId(e.target.value)}>
              <option value="">Escolha a vacina</option>
              {vacinas.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.nome} · {numero(v.saldo)} {v.unidade} em estoque
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <Campo rotulo="Doses aplicadas" dica="Uma por ave viva." erro={erros.doses}>
          {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={doses} onChange={(e) => setDoses(e.target.value)} placeholder="0" />}
        </Campo>
        <div className="grid gap-3 sm:grid-cols-2">
          <Campo rotulo="Data" erro={erros.data}>
            {(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}
          </Campo>
          <Campo rotulo="Reforço em" erro={erros.proxima}>
            {(c) => <Entrada {...c} type="date" min={data} value={proxima} onChange={(e) => setProxima(e.target.value)} />}
          </Campo>
        </div>
        {vacina && saldoDepois !== null ? (
          <Aviso
            tom={saldoDepois < 0 || saldoDepois < vacina.nivelMinimo ? 'atencao' : 'info'}
            destaque={`Baixa ${numero(qtd)} doses de ${vacina.nome}.`}
          >
            {saldoDepois < 0
              ? 'Não há doses suficientes no estoque.'
              : saldoDepois < vacina.nivelMinimo
                ? `O estoque vai a ${numero(saldoDepois)}, abaixo do mínimo de ${numero(vacina.nivelMinimo)}.`
                : `O estoque vai a ${numero(saldoDepois)}.`}
          </Aviso>
        ) : null}
      </form>
    </Painel>
  )
}
