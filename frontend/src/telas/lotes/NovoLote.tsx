import { useRef, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { hoje } from '../../api/formatar'
import { ErroApi } from '../../api/http'
import { cadastrarLote } from '../../api/lotes'
import { novoCodigoLancamento, type Especie } from '../../api/tipos'
import { useLogado, useSessao } from '../../sessao/sessaoContexto'
import { Aviso, Botao, CabecalhoPagina, Campo, classesBotao, Entrada, Fichas, Pagina, useToast } from '../../ui'

type Erros = Partial<Record<'quantidade' | 'data' | 'idade', string>>

export function NovoLote() {
  const { token } = useLogado()
  const { aoErro } = useSessao()
  const navegar = useNavigate()
  const avisar = useToast()
  const formulario = useRef<HTMLFormElement>(null)
  // chave de idempotencia: so troca depois de sucesso, entao reenviar nao cria lote em dobro
  const codigoLancamento = useRef(novoCodigoLancamento())

  const [especie, setEspecie] = useState<Especie>('FRANGO_CORTE')
  const [quantidade, setQuantidade] = useState('')
  const [data, setData] = useState('')
  const [idade, setIdade] = useState('')
  const [erros, setErros] = useState<Erros>({})
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)

  function validar(): Erros {
    const novos: Erros = {}
    const qtd = Number(quantidade)
    if (!Number.isInteger(qtd) || qtd < 1) novos.quantidade = 'Informe um número inteiro maior que zero.'
    if (data && data > hoje()) novos.data = 'A data de alojamento não pode ser no futuro.'
    if (idade !== '' && (!Number.isInteger(Number(idade)) || Number(idade) < 0)) {
      novos.idade = 'Informe um número inteiro de dias, zero ou mais.'
    }
    return novos
  }

  async function enviar(e: FormEvent) {
    e.preventDefault()
    if (enviando) return
    const novos = validar()
    setErros(novos)
    setFalha('')
    if (Object.keys(novos).length > 0) {
      requestAnimationFrame(() => formulario.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus())
      return
    }
    setEnviando(true)
    try {
      const lote = await cadastrarLote(token, {
        codigoLancamento: codigoLancamento.current,
        especie,
        quantidadeInicial: Number(quantidade),
        dataAlojamento: data || undefined,
        idadeInicialDias: idade === '' ? undefined : Number(idade),
      })
      avisar({ texto: `Lote ${lote.codigo} criado.` })
      navegar(`/lotes/${lote.id}`, { replace: true })
    } catch (erro) {
      if (aoErro(erro)) return
      setFalha(erro instanceof ErroApi ? erro.message : 'Não foi possível criar o lote. Tente de novo.')
      setEnviando(false)
    }
  }

  return (
    <Pagina>
      <CabecalhoPagina
        trilha={
          <Link to="/lotes" className="rounded text-accent hover:underline">
            Lotes
          </Link>
        }
        titulo="Novo lote"
        descricao="O código do lote é criado pelo sistema quando você salvar."
      />

      <form ref={formulario} onSubmit={(e) => void enviar(e)} noValidate className="flex max-w-[480px] flex-col gap-5 pb-24 md:pb-0">
        {falha ? <Aviso tom="critico">{falha}</Aviso> : null}

        <div className="flex flex-col gap-1.5">
          <span className="text-[13px] font-medium text-ink-hover">
            Espécie
          </span>
          <Fichas
            rotulo="Espécie"
            valor={especie}
            onMudar={setEspecie}
            opcoes={[
              { id: 'FRANGO_CORTE', rotulo: 'Frango de corte' },
              { id: 'POEDEIRA', rotulo: 'Poedeira' },
            ]}
          />
        </div>

        <Campo rotulo="Quantidade de aves" erro={erros.quantidade}>
          {(c) => (
            <Entrada
              {...c}
              mono
              type="number"
              inputMode="numeric"
              min={1}
              step={1}
              value={quantidade}
              onChange={(e) => setQuantidade(e.target.value)}
              placeholder="0"
            />
          )}
        </Campo>

        <div className="grid gap-5 sm:grid-cols-2">
          <Campo rotulo="Data de alojamento" dica="Se ficar em branco, vale hoje." erro={erros.data}>
            {(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}
          </Campo>
          <Campo rotulo="Idade na chegada" dica="Em dias. Opcional." erro={erros.idade}>
            {(c) => (
              <Entrada
                {...c}
                mono
                type="number"
                inputMode="numeric"
                min={0}
                step={1}
                value={idade}
                onChange={(e) => setIdade(e.target.value)}
                placeholder="0"
              />
            )}
          </Campo>
        </div>

        <Aviso tom="info">A idade do lote é contada a partir da chegada.</Aviso>

        <div className="fixed inset-x-0 bottom-0 z-10 flex justify-end gap-2.5 border-t border-line-soft bg-paper/95 p-4 backdrop-blur md:static md:border-0 md:bg-transparent md:p-0">
          <Link to="/lotes" className={classesBotao('fantasma', 'md', 'min-h-11')}>
            Cancelar
          </Link>
          <Botao type="submit" disabled={enviando} className="min-h-11">
            {enviando ? 'Criando...' : 'Criar lote'}
          </Botao>
        </div>
      </form>
    </Pagina>
  )
}
