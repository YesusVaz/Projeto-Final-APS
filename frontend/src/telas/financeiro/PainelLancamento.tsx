import { useRef, useState, type FormEvent } from 'react'
import {
  CATEGORIAS_DE_DESPESA,
  CATEGORIAS_DE_RECEITA,
  registrarDespesa,
  registrarReceita,
  ROTULO_CATEGORIA_FINANCEIRA,
  type CategoriaFinanceira,
  type TipoFinanceiro,
} from '../../api/financeiro'
import { hoje, paraCentavos, reais } from '../../api/formatar'
import type { Lote } from '../../api/lotes'
import { novoCodigoLancamento } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Botao, Campo, Entrada, Painel, Selecao, useToast } from '../../ui'
import { focarPrimeiroErro } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'

type Props = {
  // null = fechado. DESPESA e RECEITA abrem o mesmo painel, com textos e categorias de cada um
  tipo: TipoFinanceiro | null
  onFechar: () => void
  lotes: Lote[]
  onConcluir: () => void
}

type Erros = Partial<Record<'categoria' | 'descricao' | 'valor' | 'data', string>>

const TEXTOS: Record<TipoFinanceiro, { titulo: string; descricao: string; botao: string; exemplo: string }> = {
  DESPESA: {
    titulo: 'Nova despesa',
    descricao: 'Gasto da granja que não é compra de insumo, como luz, mão de obra ou manutenção.',
    botao: 'Registrar despesa',
    exemplo: 'Conta de luz de novembro',
  },
  RECEITA: {
    titulo: 'Nova receita',
    descricao: 'Dinheiro que entrou, como a venda de ovos ou de aves.',
    botao: 'Registrar receita',
    exemplo: 'Venda de 30 dúzias de ovos',
  },
}

export function PainelLancamento({ tipo, onFechar, lotes, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())

  const [categoria, setCategoria] = useState<CategoriaFinanceira | ''>('')
  const [descricao, setDescricao] = useState('')
  const [valor, setValor] = useState('')
  const [data, setData] = useState(hoje())
  const [loteId, setLoteId] = useState('')
  const [erros, setErros] = useState<Erros>({})

  // o painel fica montado e fechado. enquanto fecha, guarda o ultimo tipo pra nao trocar o texto no meio da animacao
  const [ultimo, setUltimo] = useState<TipoFinanceiro>('DESPESA')
  if (tipo && tipo !== ultimo) {
    setUltimo(tipo)
    setCategoria('')
  }
  const atual = tipo ?? ultimo
  const textos = TEXTOS[atual]
  const categorias = atual === 'DESPESA' ? CATEGORIAS_DE_DESPESA : CATEGORIAS_DE_RECEITA

  function fechar() {
    setCategoria('')
    setDescricao('')
    setValor('')
    setData(hoje())
    setLoteId('')
    setErros({})
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const centavos = paraCentavos(valor)
    const novos: Erros = {}
    if (!categoria) novos.categoria = 'Escolha a categoria.'
    if (!descricao.trim()) novos.descricao = 'Descreva o lançamento.'
    if (centavos === null) novos.valor = 'Informe o valor, em reais.'
    if (!data || data > hoje()) novos.data = 'Informe uma data que não seja no futuro.'
    setErros(novos)
    if (Object.keys(novos).length > 0 || !categoria || centavos === null) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      const corpo = {
        codigoLancamento: codigo.current,
        categoria,
        descricao: descricao.trim(),
        valorCentavos: centavos,
        data,
        loteId: loteId ? Number(loteId) : undefined,
      }
      if (atual === 'DESPESA') await registrarDespesa(token, corpo)
      else await registrarReceita(token, corpo)
      avisar({ texto: `${atual === 'DESPESA' ? 'Despesa' : 'Receita'} de ${reais(centavos)} registrada.` })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      fechar()
      onConcluir()
    }
  }

  return (
    <Painel
      aberto={tipo !== null}
      onFechar={fechar}
      titulo={textos.titulo}
      descricao={textos.descricao}
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-lancamento" disabled={enviando} className="min-h-11">
            {enviando ? 'Registrando...' : textos.botao}
          </Botao>
        </>
      }
    >
      <form id="form-lancamento" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Categoria" erro={erros.categoria}>
          {(c) => (
            <Selecao {...c} value={categoria} onChange={(e) => setCategoria(e.target.value as CategoriaFinanceira | '')}>
              <option value="">Escolha a categoria</option>
              {categorias.map((x) => (
                <option key={x} value={x}>
                  {ROTULO_CATEGORIA_FINANCEIRA[x]}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <Campo rotulo="Descrição" erro={erros.descricao}>
          {(c) => <Entrada {...c} value={descricao} onChange={(e) => setDescricao(e.target.value)} maxLength={200} placeholder={textos.exemplo} />}
        </Campo>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo rotulo="Valor (R$)" erro={erros.valor}>
            {(c) => <Entrada {...c} mono inputMode="decimal" value={valor} onChange={(e) => setValor(e.target.value)} placeholder="0,00" />}
          </Campo>
          <Campo rotulo="Data" erro={erros.data}>
            {(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}
          </Campo>
        </div>
        <Campo rotulo="Lote" dica="Opcional. Escolha se o lançamento é de um lote só.">
          {(c) => (
            <Selecao {...c} value={loteId} onChange={(e) => setLoteId(e.target.value)}>
              <option value="">Da granja toda</option>
              {lotes.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.codigo}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
      </form>
    </Painel>
  )
}
