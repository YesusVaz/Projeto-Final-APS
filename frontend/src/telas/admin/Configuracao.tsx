import { useState, type FormEvent } from 'react'
import { consultarConfiguracaoAlerta, consultarParametros, salvarConfiguracaoAlerta, salvarParametros, type ConfiguracaoAlerta, type Parametros } from '../../api/admin'
import { ROTULO_CRITERIO, type CriterioAlerta } from '../../api/tiposExtras'
import { useCarregar } from '../../hooks/useCarregar'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Botao, CabecalhoPagina, Campo, Cartao, Entrada, EsqueletoLista, Fichas, Pagina, useToast } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { inteiroPositivo } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'

export function Configuracao() {
  const alerta = useCarregar(consultarConfiguracaoAlerta)
  const parametros = useCarregar(consultarParametros)
  return (
    <Pagina>
      <CabecalhoPagina titulo="Parâmetros" descricao="Limites que o sistema usa para gerar os alertas." />
      <div className="grid max-w-[1040px] gap-6 lg:grid-cols-2">
        <Carregavel carga={alerta.carga} recarregar={alerta.recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
          {(dados) => <FormAlerta inicial={dados} />}
        </Carregavel>
        <Carregavel carga={parametros.carga} recarregar={parametros.recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
          {(dados) => <FormParametros inicial={dados} />}
        </Carregavel>
      </div>
    </Pagina>
  )
}

function FormAlerta({ inicial }: { inicial: ConfiguracaoAlerta }) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const [criterio, setCriterio] = useState<CriterioAlerta>(inicial.criterio)
  const [atencao, setAtencao] = useState(String(inicial.atencao))
  const [critico, setCritico] = useState(String(inicial.critico))
  const [erros, setErros] = useState<Partial<Record<'atencao' | 'critico', string>>>({})

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const a = Number(atencao.replace(',', '.'))
    const c = Number(critico.replace(',', '.'))
    const novos: typeof erros = {}
    if (atencao.trim() === '' || !Number.isFinite(a) || a <= 0 || a > 100) novos.atencao = 'Informe uma porcentagem entre 0 e 100.'
    if (critico.trim() === '' || !Number.isFinite(c) || c <= 0 || c > 100) novos.critico = 'Informe uma porcentagem entre 0 e 100.'
    else if (!novos.atencao && c <= a) novos.critico = 'O limite crítico precisa ser maior que o de atenção.'
    setErros(novos)
    if (Object.keys(novos).length > 0) return
    await enviar(async () => {
      await salvarConfiguracaoAlerta(token, { criterio, atencao: a, critico: c })
      avisar({ texto: 'Limites de mortalidade salvos.' })
    })
  }

  return (
    <Cartao>
      <h2 className="text-lg font-medium tracking-[-0.015em]">Alerta de mortalidade</h2>
      <p className="mt-1 mb-5 text-sm text-ink-3">Quando a mortalidade passa desses limites, o lote entra nos alertas.</p>
      <form onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <div className="flex flex-col gap-1.5">
          <span className="text-[13px] font-medium text-ink-hover">Como medir</span>
          <Fichas
            rotulo="Como medir"
            valor={criterio}
            onMudar={setCriterio}
            opcoes={(Object.keys(ROTULO_CRITERIO) as CriterioAlerta[]).map((c) => ({ id: c, rotulo: ROTULO_CRITERIO[c] }))}
          />
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo rotulo="Atenção (%)" erro={erros.atencao}>
            {(c) => <Entrada {...c} mono inputMode="decimal" value={atencao} onChange={(e) => setAtencao(e.target.value)} />}
          </Campo>
          <Campo rotulo="Crítico (%)" erro={erros.critico}>
            {(c) => <Entrada {...c} mono inputMode="decimal" value={critico} onChange={(e) => setCritico(e.target.value)} />}
          </Campo>
        </div>
        <Botao type="submit" disabled={enviando} className="min-h-11 self-start">
          {enviando ? 'Salvando...' : 'Salvar limites'}
        </Botao>
      </form>
    </Cartao>
  )
}

function FormParametros({ inicial }: { inicial: Parametros }) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const [vacina, setVacina] = useState(String(inicial.diasAvisoVacina))
  const [ocorrencia, setOcorrencia] = useState(String(inicial.diasOcorrenciaAberta))
  const [erros, setErros] = useState<Partial<Record<'vacina' | 'ocorrencia', string>>>({})

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos = {
      vacina: inteiroPositivo(vacina, undefined, 'O prazo'),
      ocorrencia: inteiroPositivo(ocorrencia, undefined, 'O prazo'),
    }
    setErros(novos)
    if (novos.vacina || novos.ocorrencia) return
    await enviar(async () => {
      await salvarParametros(token, { diasAvisoVacina: Number(vacina), diasOcorrenciaAberta: Number(ocorrencia) })
      avisar({ texto: 'Parâmetros de sanidade salvos.' })
    })
  }

  return (
    <Cartao>
      <h2 className="text-lg font-medium tracking-[-0.015em]">Alertas de sanidade</h2>
      <p className="mt-1 mb-5 text-sm text-ink-3">Com quantos dias de antecedência o sistema avisa e quando uma ocorrência vira alerta.</p>
      <form onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Avisar reforço de vacina (dias antes)" erro={erros.vacina}>
          {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={1} value={vacina} onChange={(e) => setVacina(e.target.value)} />}
        </Campo>
        <Campo rotulo="Ocorrência aberta vira alerta após (dias)" erro={erros.ocorrencia}>
          {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={1} value={ocorrencia} onChange={(e) => setOcorrencia(e.target.value)} />}
        </Campo>
        <Botao type="submit" disabled={enviando} className="min-h-11 self-start">
          {enviando ? 'Salvando...' : 'Salvar parâmetros'}
        </Botao>
      </form>
    </Cartao>
  )
}
