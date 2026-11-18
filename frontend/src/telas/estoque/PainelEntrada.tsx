import { useRef, useState, type FormEvent } from 'react'
import { movimentarInsumo, type Insumo } from '../../api/estoque'
import { numero } from '../../api/formatar'
import { novoCodigoLancamento } from '../../api/tipos'
import { ROTULO_ORIGEM, type OrigemMovimento, type TipoMovimentoEstoque } from '../../api/tiposExtras'
import { useLogado } from '../../sessao/sessaoContexto'
import { Abas, Aviso, Botao, Campo, Entrada, Painel, Selecao, useToast } from '../../ui'
import { focarPrimeiroErro, inteiroPositivo } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'

type Props = {
  aberto: boolean
  onFechar: () => void
  insumos: Insumo[]
  // insumo que ja vem escolhido (link "Dar entrada" da lista e dos alertas)
  insumoInicial: string
  onConcluir: () => void
}

// a origem segue o tipo: compra entra, uso geral sai. alimentacao, vacinacao e tratamento
// baixam o estoque sozinhos nas telas deles, entao nao aparecem aqui pra nao contar duas vezes
const ORIGEM: Record<TipoMovimentoEstoque, OrigemMovimento> = { ENTRADA: 'COMPRA', SAIDA: 'USO_GERAL' }

// painel lateral sobre a lista de estoque, como no desenho
export function PainelEntrada({ aberto, onFechar, insumos, insumoInicial, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  // chave de idempotencia: so troca depois de sucesso
  const codigo = useRef(novoCodigoLancamento())

  const ativos = insumos.filter((i) => i.ativo)
  const [tipo, setTipo] = useState<TipoMovimentoEstoque>('ENTRADA')
  const [escolhido, setEscolhido] = useState<string | null>(null)
  const [quantidade, setQuantidade] = useState('')
  const [erros, setErros] = useState<Partial<Record<'insumo' | 'quantidade', string>>>({})

  const insumoId = escolhido ?? insumoInicial
  const insumo = ativos.find((i) => String(i.id) === insumoId)
  const qtd = Number(quantidade)
  const novoSaldo = insumo && Number.isInteger(qtd) && qtd > 0 ? insumo.saldo + (tipo === 'ENTRADA' ? qtd : -qtd) : null

  function limpar() {
    setTipo('ENTRADA')
    setEscolhido(null)
    setQuantidade('')
    setErros({})
  }
  function fechar() {
    limpar()
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos: typeof erros = {}
    if (!insumo) novos.insumo = 'Escolha o insumo.'
    novos.quantidade = inteiroPositivo(quantidade, tipo === 'SAIDA' ? insumo?.saldo : undefined)
    const limpos = Object.fromEntries(Object.entries(novos).filter(([, v]) => v)) as typeof erros
    setErros(limpos)
    if (Object.keys(limpos).length > 0 || !insumo) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      const r = await movimentarInsumo(token, insumo.id, { codigoLancamento: codigo.current, tipo, quantidade: qtd, origem: ORIGEM[tipo] })
      avisar({ texto: `${r.nome}: saldo de ${numero(r.saldo)} ${r.unidade}.` })
    })
    if (ok) {
      codigo.current = novoCodigoLancamento()
      fechar()
      onConcluir()
    }
  }

  return (
    <Painel
      aberto={aberto}
      onFechar={fechar}
      titulo={tipo === 'ENTRADA' ? 'Entrada de estoque' : 'Saída de estoque'}
      descricao={tipo === 'ENTRADA' ? 'Registre o que chegou da compra.' : 'Registre o que saiu por uso geral.'}
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-movimento" disabled={enviando || ativos.length === 0} className="min-h-11">
            {enviando ? 'Registrando...' : tipo === 'ENTRADA' ? 'Registrar entrada' : 'Registrar saída'}
          </Botao>
        </>
      }
    >
      <form id="form-movimento" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Abas
          rotulo="Tipo de movimento"
          valor={tipo}
          onMudar={setTipo}
          opcoes={[
            { id: 'ENTRADA', rotulo: 'Entrada' },
            { id: 'SAIDA', rotulo: 'Saída' },
          ]}
        />
        <Campo rotulo="Insumo" erro={erros.insumo}>
          {(c) => (
            <Selecao {...c} value={insumoId} onChange={(e) => setEscolhido(e.target.value)}>
              <option value="">Escolha o insumo</option>
              {ativos.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.nome}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <Campo rotulo={`Quantidade${insumo ? ` (${insumo.unidade})` : ''}`} erro={erros.quantidade} dica={`Origem: ${ROTULO_ORIGEM[ORIGEM[tipo]].toLowerCase()}.`}>
          {(c) => (
            <Entrada {...c} mono type="number" inputMode="numeric" min={1} step={1} value={quantidade} onChange={(e) => setQuantidade(e.target.value)} placeholder="0" />
          )}
        </Campo>
        {insumo && novoSaldo !== null && novoSaldo >= 0 ? (
          <Aviso tom={novoSaldo >= insumo.nivelMinimo ? 'ok' : 'atencao'} destaque={`O estoque passa de ${numero(insumo.saldo)} para ${numero(novoSaldo)} ${insumo.unidade}.`}>
            {novoSaldo >= insumo.nivelMinimo
              ? `Fica acima do mínimo de ${numero(insumo.nivelMinimo)} ${insumo.unidade}.`
              : `Fica abaixo do mínimo de ${numero(insumo.nivelMinimo)} ${insumo.unidade}.`}
          </Aviso>
        ) : null}
      </form>
    </Painel>
  )
}
