import { useRef, useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router'
import { numero } from '../../api/formatar'
import { listarLotes, registrarMortalidade, type Lote, type RegistroMortalidade } from '../../api/lotes'
import { novoCodigoLancamento, ROTULO_NIVEL } from '../../api/tipos'
import { useCarregar } from '../../hooks/useCarregar'
import { useLogado } from '../../sessao/sessaoContexto'
import { AreaTexto, Aviso, Campo, CabecalhoPagina, classesBotao, Contador, EsqueletoLista, EstadoVazio, Pagina, Selecao, useToast } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { CampoLote } from '../comum/CampoLote'
import { focarPrimeiroErro, soAtivos } from '../comum/formulario'
import { RodapeEnvio } from '../comum/RodapeEnvio'
import { Trilha } from '../comum/Trilha'
import { useEnvio } from '../lotes/useEnvio'

const CAUSAS = ['Doença respiratória', 'Estresse térmico', 'Acidente', 'Outra causa']

export function Mortalidade() {
  const { carga, recarregar } = useCarregar(listarLotes)
  return (
    <Pagina>
      <CabecalhoPagina
        trilha={<Trilha para="/" texto="Início" />}
        titulo="Registrar mortalidade"
        descricao="Conte as aves mortas do lote. O sistema recalcula o saldo e o alerta do dia."
      />
      <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
        {(lotes) => <Formulario lotes={soAtivos(lotes)} aoGravar={recarregar} />}
      </Carregavel>
    </Pagina>
  )
}

function Formulario({ lotes, aoGravar }: { lotes: Lote[]; aoGravar: () => void }) {
  const { token } = useLogado()
  const avisar = useToast()
  const [params] = useSearchParams()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  // chave de idempotencia: so troca depois de gravar, entao reenviar nao registra em dobro
  const codigo = useRef(novoCodigoLancamento())

  const inicial = params.get('lote') ?? ''
  const [loteId, setLoteId] = useState(lotes.some((l) => String(l.id) === inicial) ? inicial : '')
  const [quantidade, setQuantidade] = useState(0)
  const [causa, setCausa] = useState(CAUSAS[0])
  const [observacao, setObservacao] = useState('')
  const [erros, setErros] = useState<Partial<Record<'lote' | 'quantidade', string>>>({})
  const [resultado, setResultado] = useState<{ codigo: string; r: RegistroMortalidade } | null>(null)

  const lote = lotes.find((l) => String(l.id) === loteId)

  if (lotes.length === 0) {
    return (
      <EstadoVazio
        icone="lotes"
        titulo="Não há lote ativo"
        texto="Só dá para registrar mortalidade em lote ativo. Cadastre ou reabra um lote antes."
        acao={
          <Link to="/lotes" className={classesBotao('contorno')}>
            Ver lotes
          </Link>
        }
      />
    )
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
      setResultado({ codigo: lote.codigo, r })
      avisar({ texto: `Mortalidade registrada no lote ${lote.codigo}.` })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      setQuantidade(0)
      setObservacao('')
      aoGravar()
    }
  }

  return (
    <form ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex max-w-[480px] flex-col gap-5 pb-24 md:pb-0">
      {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
      {resultado ? (
        <Aviso
          tom={resultado.r.nivelAlerta === 'NORMAL' ? 'ok' : resultado.r.nivelAlerta === 'ATENCAO' ? 'atencao' : 'critico'}
          destaque={`Lote ${resultado.codigo}: saldo de ${numero(resultado.r.saldo)} aves.`}
        >
          {resultado.r.nivelAlerta === 'NORMAL'
            ? 'A mortalidade do dia está dentro do esperado.'
            : `Alerta de mortalidade em nível ${ROTULO_NIVEL[resultado.r.nivelAlerta].toLowerCase()}.`}
        </Aviso>
      ) : null}

      <CampoLote lotes={lotes} valor={loteId} onMudar={setLoteId} erro={erros.lote} />

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
        <p className="text-[13.5px] text-ink-3">
          O saldo passa de {numero(lote.saldo)} para {numero(lote.saldo - quantidade)}.
        </p>
      ) : null}

      <RodapeEnvio rotulo="Registrar mortalidade" rotuloEnviando="Registrando..." enviando={enviando} />
    </form>
  )
}
