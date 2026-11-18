import { useRef, useState, type FormEvent } from 'react'
import { abrirChamado, ROTULO_CATEGORIA_CHAMADO, type CategoriaChamado } from '../../api/suporte'
import { novoCodigoLancamento } from '../../api/tipos'
import { useLogado } from '../../sessao/sessaoContexto'
import { Aviso, AreaTexto, Botao, Campo, Entrada, Painel, Selecao, useToast } from '../../ui'
import { focarPrimeiroErro } from '../comum/formulario'
import { useEnvio } from '../lotes/useEnvio'

type Props = {
  aberto: boolean
  onFechar: () => void
  onConcluir: () => void
}

type Erros = Partial<Record<'assunto' | 'descricao', string>>

// UC30: qualquer perfil abre um chamado
export function PainelChamado({ aberto, onFechar, onConcluir }: Props) {
  const { token } = useLogado()
  const avisar = useToast()
  const { enviando, erro, enviar } = useEnvio()
  const formulario = useRef<HTMLFormElement>(null)
  const codigo = useRef(novoCodigoLancamento())

  const [categoria, setCategoria] = useState<CategoriaChamado>('DUVIDA')
  const [assunto, setAssunto] = useState('')
  const [descricao, setDescricao] = useState('')
  const [erros, setErros] = useState<Erros>({})

  function fechar() {
    setCategoria('DUVIDA')
    setAssunto('')
    setDescricao('')
    setErros({})
    onFechar()
  }

  async function submeter(e: FormEvent) {
    e.preventDefault()
    const novos: Erros = {}
    if (assunto.trim().length < 3) novos.assunto = 'Diga, em poucas palavras, do que se trata.'
    if (descricao.trim().length < 10) novos.descricao = 'Conte um pouco mais, para a gente entender.'
    setErros(novos)
    if (Object.keys(novos).length > 0) {
      focarPrimeiroErro(formulario.current)
      return
    }
    const ok = await enviar(async () => {
      await abrirChamado(token, { codigoLancamento: codigo.current, categoria, assunto: assunto.trim(), descricao: descricao.trim() })
      avisar({ texto: 'Chamado aberto. O gestor vai responder por aqui.' })
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
      titulo="Abrir chamado"
      descricao="Tire uma dúvida, conte um problema ou mande uma sugestão."
      rodape={
        <>
          <Botao variante="fantasma" onClick={fechar} className="min-h-11">
            Cancelar
          </Botao>
          <Botao type="submit" form="form-chamado" disabled={enviando} className="min-h-11">
            {enviando ? 'Enviando...' : 'Abrir chamado'}
          </Botao>
        </>
      }
    >
      <form id="form-chamado" ref={formulario} onSubmit={(e) => void submeter(e)} noValidate className="flex flex-col gap-4">
        {erro ? <Aviso tom="critico">{erro}</Aviso> : null}
        <Campo rotulo="Tipo">
          {(c) => (
            <Selecao {...c} value={categoria} onChange={(e) => setCategoria(e.target.value as CategoriaChamado)}>
              {(Object.keys(ROTULO_CATEGORIA_CHAMADO) as CategoriaChamado[]).map((k) => (
                <option key={k} value={k}>
                  {ROTULO_CATEGORIA_CHAMADO[k]}
                </option>
              ))}
            </Selecao>
          )}
        </Campo>
        <Campo rotulo="Assunto" erro={erros.assunto}>
          {(c) => <Entrada {...c} value={assunto} onChange={(e) => setAssunto(e.target.value)} maxLength={80} placeholder="Ex.: Não consigo registrar a ração" />}
        </Campo>
        <Campo rotulo="O que aconteceu" erro={erros.descricao}>
          {(c) => (
            <AreaTexto
              {...c}
              className="h-40"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              maxLength={1000}
              placeholder="Conte o que você estava fazendo e o que esperava ver."
            />
          )}
        </Campo>
      </form>
    </Painel>
  )
}
