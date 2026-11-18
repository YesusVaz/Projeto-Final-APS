import { useRef, useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router'
import { registrarAlimentacao } from '../../api/alimentacao'
import { listarInsumos, type Insumo } from '../../api/estoque'
import { hoje, numero } from '../../api/formatar'
import { listarLotes, type Lote } from '../../api/lotes'
import { novoCodigoLancamento } from '../../api/tipos'
import { useCarregar } from '../../hooks/useCarregar'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Campo, CabecalhoPagina, classesBotao, Entrada, EsqueletoLista, EstadoVazio, Pagina, Selecao, useToast } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { CampoLote } from '../comum/CampoLote'
import { focarPrimeiroErro, inteiroPositivo, soAtivos } from '../comum/formulario'
import { RodapeEnvio } from '../comum/RodapeEnvio'
import { Trilha } from '../comum/Trilha'
import { useEnvio } from '../lotes/useEnvio'

export function Alimentacao() {
  const lotes = useCarregar(listarLotes)
  const insumos = useCarregar(listarInsumos)
  const recarregar = () => {
    lotes.recarregar()
    insumos.recarregar()
  }
  return (
    <Pagina>
      <CabecalhoPagina
        trilha={<Trilha para="/" texto="Início" />}
        titulo="Registrar alimentação"
        descricao="Ração servida ao lote. A quantidade sai do estoque na hora."
      />
      <Carregavel carga={lotes.carga} recarregar={lotes.recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
        {(listaLotes) => (
          <Carregavel carga={insumos.carga} recarregar={insumos.recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
            {(listaInsumos) => (
              <Formulario
                lotes={soAtivos(listaLotes)}
                racoes={listaInsumos.filter((i) => i.categoria === 'RACAO' && i.ativo)}
                aoGravar={recarregar}
              />
            )}
          </Carregavel>
        )}
      </Carregavel>
    </Pagina>
  )
}

type Erros = Partial<Record<'lote' | 'insumo' | 'quantidade' | 'data', string>>

function Formulario({ lotes, racoes, aoGravar }: { lotes: Lote[]; racoes: Insumo[]; aoGravar: () => void }) {
  const { token } = useLogado()
  const avisar = useToast()
  const [params] = useSearchParams()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())

  const inicial = params.get('lote') ?? ''
  const [loteId, setLoteId] = useState(lotes.some((l) => String(l.id) === inicial) ? inicial : '')
  const [insumoId, setInsumoId] = useState('')
  const [quantidade, setQuantidade] = useState('')
  const [data, setData] = useState(hoje())
  const [erros, setErros] = useState<Erros>({})

  const racao = racoes.find((r) => String(r.id) === insumoId)
  const kg = Number(quantidade)

  if (lotes.length === 0 || racoes.length === 0) {
    return (
      <EstadoVazio
        icone={lotes.length === 0 ? 'lotes' : 'estoque'}
        titulo={lotes.length === 0 ? 'Não há lote ativo' : 'Não há ração cadastrada'}
        texto={lotes.length === 0 ? 'Só dá para registrar alimentação em lote ativo.' : 'Cadastre um insumo da categoria Ração no estoque antes de registrar.'}
        acao={
          <Link to={lotes.length === 0 ? '/lotes' : '/estoque'} className={classesBotao('contorno')}>
            {lotes.length === 0 ? 'Ver lotes' : 'Ir para o estoque'}
          </Link>
        }
      />
    )
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos: Erros = {}
    const lote = lotes.find((l) => String(l.id) === loteId)
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
      await registrarAlimentacao(token, lote.id, {
        codigoLancamento: codigo.current,
        insumoId: racao.id,
        quantidadeKg: kg,
        data,
      })
      avisar({ texto: `${numero(kg)} kg de ${racao.nome} registrados no lote ${lote.codigo}.` })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      setQuantidade('')
      aoGravar()
    }
  }

  return (
    <form ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex max-w-[480px] flex-col gap-5 pb-24 md:pb-0">
      {erro ? <Aviso tom="critico">{erro}</Aviso> : null}

      <CampoLote lotes={lotes} valor={loteId} onMudar={setLoteId} erro={erros.lote} />

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

      <div className="grid gap-5 sm:grid-cols-2">
        <Campo rotulo="Quantidade (kg)" erro={erros.quantidade}>
          {(c) => (
            <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} placeholder="0" />
          )}
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

      <RodapeEnvio rotulo="Registrar alimentação" rotuloEnviando="Registrando..." enviando={enviando} />
    </form>
  )
}
