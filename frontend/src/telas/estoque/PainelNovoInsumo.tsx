import { useState, type FormEvent } from 'react'
import { cadastrarInsumo } from '../../api/estoque'
import type { CategoriaInsumo } from '../../api/tipos'
import { ROTULO_CATEGORIA_INSUMO } from '../../api/tiposExtras'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, Botao, Campo, Entrada, Painel, Selecao, useToast } from '../../ui'
import { useEnvio } from '../lotes/useEnvio'

type Props = { aberto: boolean; onFechar: () => void; onConcluir: () => void }

const CATEGORIAS = Object.keys(ROTULO_CATEGORIA_INSUMO) as CategoriaInsumo[]
type Erros = Partial<Record<'nome' | 'unidade' | 'minimo', string>>

// painel lateral pro gestor cadastrar insumo. o painel so monta o conteudo quando abre,
// entao o formulario sempre comeca limpo
export function PainelNovoInsumo({ aberto, onFechar, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const [nome, setNome] = useState('')
  const [categoria, setCategoria] = useState<CategoriaInsumo>('RACAO')
  const [unidade, setUnidade] = useState('kg')
  const [minimo, setMinimo] = useState('')
  const [erros, setErros] = useState<Erros>({})

  function limpar() {
    setNome('')
    setCategoria('RACAO')
    setUnidade('kg')
    setMinimo('')
    setErros({})
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos: Erros = {}
    if (!nome.trim()) novos.nome = 'Informe o nome do insumo.'
    if (!unidade.trim()) novos.unidade = 'Informe a unidade (kg, dose, frasco...).'
    const n = Number(minimo)
    if (minimo.trim() === '' || !Number.isInteger(n) || n < 0) novos.minimo = 'Informe um número inteiro, zero ou mais.'
    setErros(novos)
    if (Object.keys(novos).length > 0) return
    const ok = await enviar(async () => {
      const i = await cadastrarInsumo(token, { nome: nome.trim(), categoria, unidade: unidade.trim(), nivelMinimo: n })
      avisar({ texto: `${i.nome} cadastrado no estoque.` })
    })
    if (ok) {
      limpar()
      onFechar()
      onConcluir()
    }
  }

  return (
    <Painel
      aberto={aberto}
      onFechar={onFechar}
      titulo="Novo insumo"
      descricao="O saldo começa em zero. Dê entrada depois pela tela de movimentação."
      rodape={
        <>
          <Botao variante="fantasma" onClick={onFechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-insumo" disabled={enviando} className="min-h-11">
            {enviando ? 'Salvando...' : 'Cadastrar'}
          </Botao>
        </>
      }
    >
      <form id="form-insumo" onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Nome" erro={erros.nome}>
          {(c) => <Entrada {...c} value={nome} onChange={(e) => setNome(e.target.value)} maxLength={80} />}
        </Campo>
        <Campo rotulo="Categoria" dica="Define onde o insumo pode ser usado: ração na alimentação, vacina na vacinação.">
          {(c) => (
            <Selecao {...c} value={categoria} onChange={(e) => setCategoria(e.target.value as CategoriaInsumo)}>
              {CATEGORIAS.map((x) => (
                <option key={x} value={x}>
                  {ROTULO_CATEGORIA_INSUMO[x]}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <div className="grid gap-4 sm:grid-cols-2">
          <Campo rotulo="Unidade" erro={erros.unidade}>
            {(c) => <Entrada {...c} value={unidade} onChange={(e) => setUnidade(e.target.value)} maxLength={20} />}
          </Campo>
          <Campo rotulo="Nível mínimo" erro={erros.minimo}>
            {(c) => <Entrada {...c} mono type="number" inputMode="numeric" min={0} step={1} value={minimo} onChange={(e) => setMinimo(e.target.value)} placeholder="0" />}
          </Campo>
        </div>
      </form>
    </Painel>
  )
}
