import { useRef, useState, type FormEvent } from 'react'
import type { Insumo } from '../../api/estoque'
import { registrarCompra } from '../../api/financeiro'
import type { Fornecedor } from '../../api/fornecedores'
import { hoje, numero, paraCentavos, reais } from '../../api/formatar'
import { novoCodigoLancamento } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Botao, Campo, Entrada, Painel, Selecao, useToast } from '../../ui'
import { focarPrimeiroErro, inteiroPositivo } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'

type Props = {
  aberto: boolean
  onFechar: () => void
  insumos: Insumo[]
  fornecedores: Fornecedor[]
  onConcluir: () => void
}

type Erros = Partial<Record<'insumo' | 'fornecedor' | 'quantidade' | 'valor' | 'data', string>>

// compra de insumo: entra no estoque e vira despesa de uma vez so
export function PainelCompra({ aberto, onFechar, insumos, fornecedores, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())

  const ativos = insumos.filter((i) => i.ativo)
  const [insumoId, setInsumoId] = useState('')
  const [fornecedorId, setFornecedorId] = useState('')
  const [quantidade, setQuantidade] = useState('')
  const [valor, setValor] = useState('')
  const [data, setData] = useState(hoje())
  const [erros, setErros] = useState<Erros>({})

  const insumo = ativos.find((i) => String(i.id) === insumoId)
  const qtd = Number(quantidade)
  const centavos = paraCentavos(valor)

  function fechar() {
    setInsumoId('')
    setFornecedorId('')
    setQuantidade('')
    setValor('')
    setData(hoje())
    setErros({})
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos: Erros = {}
    if (!insumo) novos.insumo = 'Escolha o insumo.'
    if (!fornecedorId) novos.fornecedor = 'Escolha o fornecedor.'
    novos.quantidade = inteiroPositivo(quantidade)
    if (centavos === null) novos.valor = 'Informe o valor total da compra, em reais.'
    if (!data || data > hoje()) novos.data = 'Informe uma data que não seja no futuro.'
    const limpos = Object.fromEntries(Object.entries(novos).filter(([, v]) => v)) as Erros
    setErros(limpos)
    if (Object.keys(limpos).length > 0 || !insumo || centavos === null) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      await registrarCompra(token, {
        codigoLancamento: codigo.current,
        insumoId: insumo.id,
        fornecedorId: Number(fornecedorId),
        quantidade: qtd,
        valorCentavos: centavos,
        data,
      })
      avisar({ texto: `Compra de ${reais(centavos)} registrada. ${insumo.nome} passa a ${numero(insumo.saldo + qtd)} ${insumo.unidade}.` })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      fechar()
      onConcluir()
    }
  }

  const semDados = ativos.length === 0 || fornecedores.length === 0

  return (
    <Painel
      aberto={aberto}
      onFechar={fechar}
      titulo="Registrar compra"
      descricao="O que chegou entra no estoque e o valor vira despesa."
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-compra" disabled={enviando || semDados} className="min-h-11">
            {enviando ? 'Registrando...' : 'Registrar compra'}
          </Botao>
        </>
      }
    >
      <form id="form-compra" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        {fornecedores.length === 0 ? <Aviso tom="atencao">Cadastre um fornecedor antes de registrar a compra.</Aviso> : null}
        {ativos.length === 0 ? <Aviso tom="atencao">Cadastre um insumo no estoque antes de registrar a compra.</Aviso> : null}
        <Campo rotulo="Fornecedor" erro={erros.fornecedor}>
          {(c) => (
            <Selecao {...c} value={fornecedorId} onChange={(e) => setFornecedorId(e.target.value)}>
              <option value="">Escolha o fornecedor</option>
              {fornecedores.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.nome}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <Campo rotulo="Insumo" erro={erros.insumo}>
          {(c) => (
            <Selecao {...c} value={insumoId} onChange={(e) => setInsumoId(e.target.value)}>
              <option value="">Escolha o insumo</option>
              {ativos.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.nome} · saldo {numero(i.saldo)} {i.unidade}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo rotulo={`Quantidade${insumo ? ` (${insumo.unidade})` : ''}`} erro={erros.quantidade}>
            {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} placeholder="0" />}
          </Campo>
          <Campo rotulo="Valor total (R$)" erro={erros.valor}>
            {(c) => <Entrada {...c} mono inputMode="decimal" value={valor} onChange={(e) => setValor(e.target.value)} placeholder="0,00" />}
          </Campo>
        </div>
        <Campo rotulo="Data da compra" erro={erros.data}>
          {(c) => <Entrada {...c} type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />}
        </Campo>
        {insumo && Number.isInteger(qtd) && qtd > 0 ? (
          <Aviso tom="info" destaque={`O estoque passa de ${numero(insumo.saldo)} para ${numero(insumo.saldo + qtd)} ${insumo.unidade}.`}>
            {centavos !== null ? `A despesa de ${reais(centavos)} entra no financeiro.` : 'A despesa entra no financeiro.'}
          </Aviso>
        ) : null}
      </form>
    </Painel>
  )
}
