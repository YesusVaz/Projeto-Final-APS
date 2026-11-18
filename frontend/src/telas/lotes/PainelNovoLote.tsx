import { useRef, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router'
import { hoje } from '../../api/formatar'
import { cadastrarLote } from '../../api/lotes'
import { novoCodigoLancamento, ROTULO_ESPECIE, type Especie } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Botao, Campo, Entrada, Painel, Selecao, useToast } from '../../ui'
import { focarPrimeiroErro } from '../comum/formulario'
import { useEnvio } from './useEnvio'

type Props = { aberto: boolean; onFechar: () => void }

type Erros = Partial<Record<'quantidade' | 'data' | 'idade', string>>

const ESPECIES = Object.keys(ROTULO_ESPECIE) as Especie[]

// painel lateral, como no desenho: a lista de lotes continua atras, escurecida.
// o painel so monta o conteudo quando abre, entao o formulario sempre comeca limpo
export function PainelNovoLote({ aberto, onFechar }: Props) {
  const { token } = useLogado()
  const navegar = useNavigate()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  // chave de idempotencia: so troca depois de sucesso, entao reenviar nao cria lote em dobro
  const codigoLancamento = useRef(novoCodigoLancamento())

  const [especie, setEspecie] = useState<Especie>('FRANGO_CORTE')
  const [quantidade, setQuantidade] = useState('')
  const [data, setData] = useState('')
  const [idade, setIdade] = useState('')
  const [erros, setErros] = useState<Erros>({})

  function validar(): Erros {
    const novos: Erros = {}
    const qtd = Number(quantidade)
    if (quantidade.trim() === '' || !Number.isInteger(qtd) || qtd < 1) novos.quantidade = 'Informe um número inteiro maior que zero.'
    if (data && data > hoje()) novos.data = 'A data não pode ser no futuro.'
    if (idade !== '' && (!Number.isInteger(Number(idade)) || Number(idade) < 0)) novos.idade = 'Use dias inteiros, zero ou mais.'
    return novos
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos = validar()
    setErros(novos)
    if (Object.keys(novos).length > 0) {
      focarPrimeiroErro(formulario.current)
      return
    }
    let criado: { id: number; codigo: string } | null = null
    const ok = await enviar(async () => {
      criado = await cadastrarLote(token, {
        codigoLancamento: codigoLancamento.current,
        especie,
        quantidadeInicial: Number(quantidade),
        dataAlojamento: data || undefined,
        idadeInicialDias: idade === '' ? undefined : Number(idade),
      })
    })
    if (!ok || !criado) return
    const lote: { id: number; codigo: string } = criado
    codigoLancamento.current = novoCodigoLancamento()
    setQuantidade('')
    setData('')
    setIdade('')
    avisar({ texto: `Lote ${lote.codigo} criado.` })
    onFechar()
    navegar(`/lotes/${lote.id}`)
  }

  return (
    <Painel
      aberto={aberto}
      onFechar={onFechar}
      titulo="Novo lote"
      descricao="O código do lote é criado pelo sistema."
      rodape={
        <>
          <Botao variante="fantasma" onClick={onFechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-lote" disabled={enviando} className="min-h-11">
            {enviando ? 'Criando...' : 'Criar lote'}
          </Botao>
        </>
      }
    >
      <form id="form-lote" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Espécie">
          {(c) => (
            <Selecao {...c} value={especie} onChange={(e) => setEspecie(e.target.value as Especie)}>
              {ESPECIES.map((x) => (
                <option key={x} value={x}>
                  {ROTULO_ESPECIE[x]}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <Campo rotulo="Quantidade de aves" erro={erros.quantidade}>
          {(c) => (
            <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} placeholder="0" />
          )}
        </Campo>
        <div className="grid gap-3 sm:grid-cols-2">
          <Campo rotulo="Data de alojamento" dica="Em branco vale hoje." erro={erros.data}>
            {(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}
          </Campo>
          <Campo rotulo="Idade na chegada" dica="Em dias." erro={erros.idade}>
            {(c) => (
              <Entrada {...c} mono type="number" inputMode="numeric" min={0} step={1} value={idade} onChange={(e) => setIdade(e.target.value)} placeholder="0" />
            )}
          </Campo>
        </div>
        <Aviso tom="info">A idade do lote é contada a partir da chegada.</Aviso>
      </form>
    </Painel>
  )
}
