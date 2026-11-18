import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { dataCompleta, numero } from '../../api/formatar'
import { consultarLote, historicoDoLote, idadeDoLote } from '../../api/lotes'
import { ROTULO_ESPECIE, ROTULO_STATUS_LOTE } from '../../api/tipos'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import {
  Aviso,
  Botao,
  CabecalhoPagina,
  EsqueletoLista,
  EstadoErro,
  EstadoVazio,
  Estatistica,
  Pagina,
  Selo,
  TituloSecao,
} from '../../ui'
import { DialogoBaixa, DialogoEncerrar, DialogoMortalidade } from './DialogosLote'
import { LinhaDoTempo } from './LinhaDoTempo'

type Aberto = 'mortalidade' | 'baixa' | 'encerrar' | null

// o key faz a tela recomecar do zero quando a pessoa troca de lote
export function DetalheLote() {
  const { id } = useParams()
  return <Conteudo key={id} id={Number(id)} />
}

function Conteudo({ id }: { id: number }) {
  const { perfil } = useLogado()
  const lote = useCarregar((token) => consultarLote(token, id))
  const idade = useCarregar((token) => idadeDoLote(token, id))
  const historico = useCarregar((token) => historicoDoLote(token, id))

  const [aberto, setAberto] = useState<Aberto>(null)
  // cada abertura monta o dialogo de novo: campos limpos e um codigoLancamento novo
  const [abertura, setAbertura] = useState(0)

  function abrir(qual: Exclude<Aberto, null>) {
    setAbertura((n) => n + 1)
    setAberto(qual)
  }
  function atualizar() {
    lote.recarregar()
    idade.recarregar()
    historico.recarregar()
  }

  if (Number.isNaN(id)) return <NaoEncontrado />
  if (lote.carga.situacao === 'erro') {
    return (
      <Pagina>
        <CabecalhoPagina titulo="Lote" />
        <EstadoErro mensagem={lote.carga.mensagem} onTentar={lote.recarregar} />
      </Pagina>
    )
  }
  if (lote.carga.situacao === 'carregando') {
    return (
      <Pagina>
        <CabecalhoPagina titulo="Lote" />
        <EsqueletoLista linhas={4} />
      </Pagina>
    )
  }

  const dados = lote.carga.dados
  const ativo = dados.status === 'ATIVO'
  const consistente = dados.consistente && (historico.carga.situacao !== 'pronto' || historico.carga.dados.consistente)
  const podeMortalidade = ativo && permite(perfil, 'registrarMortalidade')
  const podeBaixa = ativo && permite(perfil, 'registrarBaixa')
  const podeEncerrar = ativo && permite(perfil, 'encerrarLote')
  const dadosIdade = idade.carga.situacao === 'pronto' ? idade.carga.dados : null

  const propsDialogo = { lote: dados, onFechar: () => setAberto(null), onConcluir: atualizar }

  return (
    <Pagina>
      <CabecalhoPagina
        trilha={
          <>
            <Link to="/lotes" className="rounded text-accent hover:underline">
              Lotes
            </Link>{' '}
            / {dados.codigo}
          </>
        }
        titulo={dados.codigo}
        descricao={`${ROTULO_ESPECIE[dados.especie]}${dadosIdade ? ` · alojado em ${dataCompleta(dadosIdade.dataAlojamento)}` : ''}`}
        acoes={
          <>
            <Selo nivel={ativo ? 'normal' : 'neutro'}>{ROTULO_STATUS_LOTE[dados.status]}</Selo>
            {podeMortalidade ? (
              <Botao variante="contorno" className="min-h-11" onClick={() => abrir('mortalidade')}>
                Registrar mortalidade
              </Botao>
            ) : null}
            {podeBaixa ? (
              <Botao variante="contorno" className="min-h-11" onClick={() => abrir('baixa')}>
                Registrar baixa
              </Botao>
            ) : null}
            {podeEncerrar ? (
              <Botao variante="contorno" className="min-h-11" onClick={() => abrir('encerrar')}>
                Encerrar lote
              </Botao>
            ) : null}
          </>
        }
      />

      {!consistente ? (
        <Aviso tom="critico" destaque="O saldo não bate com os movimentos." className="mb-8">
          O saldo registrado do lote é diferente da soma dos movimentos. Avise o gestor antes de lançar mais dados.
        </Aviso>
      ) : null}

      <section className="grid grid-cols-2 gap-x-6 gap-y-8 pb-10 lg:grid-cols-4">
        <Estatistica indice={0} valor={dados.saldo} rotulo="Saldo" descricao={`de ${numero(dados.quantidadeInicial)} aves`} />
        <Estatistica
          indice={1}
          valor={dadosIdade ? dadosIdade.idadeDias : '—'}
          rotulo="Dias de idade"
          descricao={dadosIdade ? `${dadosIdade.semanas} semanas` : 'não carregou'}
        />
        <Estatistica indice={2} valor={dados.quantidadeInicial - dados.saldo} rotulo="Aves a menos" descricao="desde a entrada" />
      </section>

      <section aria-labelledby="titulo-historico" className="max-w-[640px]">
        <TituloSecao id="titulo-historico" className="mb-3">
          Histórico
        </TituloSecao>
        {historico.carga.situacao === 'carregando' ? <EsqueletoLista linhas={4} /> : null}
        {historico.carga.situacao === 'erro' ? (
          <EstadoErro mensagem={historico.carga.mensagem} onTentar={historico.recarregar} />
        ) : null}
        {historico.carga.situacao === 'pronto' ? (
          historico.carga.dados.itens.length === 0 ? (
            <EstadoVazio icone="calendario" titulo="Nada registrado ainda" texto="Mortalidade, baixas e outros lançamentos aparecem aqui." />
          ) : (
            <LinhaDoTempo itens={historico.carga.dados.itens} />
          )
        ) : null}
      </section>

      <DialogoMortalidade key={`m${abertura}`} {...propsDialogo} aberto={aberto === 'mortalidade'} />
      <DialogoBaixa key={`b${abertura}`} {...propsDialogo} aberto={aberto === 'baixa'} />
      <DialogoEncerrar key={`e${abertura}`} {...propsDialogo} aberto={aberto === 'encerrar'} />
    </Pagina>
  )
}

function NaoEncontrado() {
  return (
    <Pagina>
      <CabecalhoPagina titulo="Lote não encontrado" descricao="O endereço não aponta para nenhum lote." />
    </Pagina>
  )
}
