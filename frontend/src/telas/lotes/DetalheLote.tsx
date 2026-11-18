import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { dataCompleta, numero } from '../../api/formatar'
import { consultarLote, historicoDoLote, idadeDoLote, listarAlertasMortalidade, type ItemHistorico } from '../../api/lotes'
import { ROTULO_ESPECIE, ROTULO_NIVEL, ROTULO_STATUS_LOTE } from '../../api/tipos'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import {
  Abas,
  Aviso,
  Botao,
  classesBotao,
  CabecalhoPagina,
  EsqueletoLista,
  EstadoErro,
  EstadoVazio,
  Estatistica,
  Menu,
  MenuItem,
  Pagina,
  Selo,
  TituloSecao,
} from '../../ui'
import { NIVEL_SELO, nivelPorLote } from '../comum/lotes'
import { DialogoBaixa, DialogoEncerrar, DialogoMortalidade } from './DialogosLote'
import { LinhaDoTempo } from './LinhaDoTempo'
import { PainelHigienizacao } from '../registros/PainelHigienizacao'
import { PainelIluminacao } from '../registros/PainelIluminacao'
import { PainelEntradaAves } from './PainelEntradaAves'
import { Produtividade } from './Produtividade'

type Aba = 'historico' | 'sanidade' | 'alimentacao' | 'producao' | 'manejo'

// cada aba mostra so as categorias dela no historico completo do lote
const CATEGORIAS_DA_ABA: Record<Exclude<Aba, 'historico'>, ItemHistorico['categoria'][]> = {
  sanidade: ['VACINACAO', 'OCORRENCIA', 'TRATAMENTO'],
  alimentacao: ['ALIMENTACAO'],
  producao: ['PRODUCAO'],
  manejo: ['HIGIENIZACAO', 'ILUMINACAO'],
}

type Aberto = 'entrada' | 'mortalidade' | 'baixa' | 'encerrar' | 'higienizacao' | 'iluminacao' | null

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
  const alertas = useCarregar(listarAlertasMortalidade)

  const [aberto, setAberto] = useState<Aberto>(null)
  const [aba, setAba] = useState<Aba>('historico')
  // cada abertura monta o dialogo de novo: campos limpos e um codigoLancamento novo
  const [abertura, setAbertura] = useState(0)
  // sobe a cada atualizacao pra produtividade buscar de novo
  const [versao, setVersao] = useState(0)

  function abrir(qual: Exclude<Aberto, null>) {
    setAbertura((n) => n + 1)
    setAberto(qual)
  }
  function atualizar() {
    lote.recarregar()
    idade.recarregar()
    historico.recarregar()
    alertas.recarregar()
    setVersao((n) => n + 1)
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
  const podeEntrada = ativo && permite(perfil, 'registrarEntradaAnimais')
  // tudo que ja entrou no lote: a entrada do cadastro mais as que vieram depois
  const entrado = dados.historico.filter((m) => m.tipo.startsWith('ENTRADA')).reduce((soma, m) => soma + m.quantidade, 0) || dados.quantidadeInicial
  const podeEncerrar = ativo && permite(perfil, 'encerrarLote')
  const podeHigienizar = ativo && permite(perfil, 'registrarHigienizacao')
  const podeIluminar = ativo && permite(perfil, 'registrarIluminacao')
  const verProdutividade = permite(perfil, 'consultarProdutividade')
  const dadosIdade = idade.carga.situacao === 'pronto' ? idade.carga.dados : null
  // a situacao do lote ativo e o nivel da mortalidade, como na lista de lotes
  const nivel = nivelPorLote(alertas.carga.situacao === 'pronto' ? alertas.carga.dados : []).get(dados.id) ?? 'NORMAL'

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
            {podeEntrada ? (
              <Botao variante="contorno" className="min-h-11" onClick={() => abrir('entrada')}>
                Adicionar aves
              </Botao>
            ) : null}
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
            {podeHigienizar || podeIluminar ? (
              <Menu
                gatilho={(g) => (
                  <Botao variante="contorno" className="min-h-11" iconeDireita="baixo" {...g}>
                    Manejo
                  </Botao>
                )}
              >
                {podeHigienizar ? (
                  <MenuItem icone="sanidade" descricao="Limpeza e desinfecção do galpão" onSelecionar={() => abrir('higienizacao')}>
                    Registrar higienização
                  </MenuItem>
                ) : null}
                {podeIluminar ? (
                  <MenuItem icone="ajustes" descricao="Horas de luz por dia" onSelecionar={() => abrir('iluminacao')}>
                    Programa de iluminação
                  </MenuItem>
                ) : null}
              </Menu>
            ) : null}
            {podeEncerrar ? (
              <Botao variante="contorno" className="min-h-11" onClick={() => abrir('encerrar')}>
                Encerrar lote
              </Botao>
            ) : null}
          </>
        }
      />

      <div className="-mt-2 mb-8 flex items-center gap-2.5">
        {ativo ? <Selo nivel={NIVEL_SELO[nivel]}>{ROTULO_NIVEL[nivel]}</Selo> : <Selo nivel="neutro">{ROTULO_STATUS_LOTE[dados.status]}</Selo>}
      </div>

      {!consistente ? (
        <Aviso tom="critico" destaque="O saldo não bate com os movimentos." className="mb-8">
          O saldo registrado do lote é diferente da soma dos movimentos. Avise o gestor antes de lançar mais dados.
        </Aviso>
      ) : null}

      <section className="grid grid-cols-2 gap-x-7 gap-y-7 pb-9 lg:grid-cols-4">
        <Estatistica tamanho="medio" indice={0} valor={dados.saldo} rotulo="Saldo" descricao={`de ${numero(entrado)} aves`} />
        <Estatistica
          tamanho="medio"
          indice={1}
          valor={dadosIdade ? dadosIdade.idadeDias : '—'}
          rotulo="Dias de idade"
          descricao={dadosIdade ? `${dadosIdade.semanas} semanas` : 'não carregou'}
        />
        <Estatistica tamanho="medio" indice={2} valor={entrado - dados.saldo} rotulo="Aves a menos" descricao="desde a entrada" />
      </section>

      {verProdutividade ? <Produtividade loteId={dados.id} versao={versao} /> : null}

      <section aria-labelledby="titulo-historico" className="max-w-[640px]">
        <TituloSecao id="titulo-historico" className="mb-4 md:text-lg md:tracking-[-0.015em] md:text-ink">
          Histórico
        </TituloSecao>
        <Abas
          rotulo="Tipo de registro"
          valor={aba}
          onMudar={setAba}
          className="mb-6"
          opcoes={[
            { id: 'historico', rotulo: 'Tudo' },
            { id: 'sanidade', rotulo: 'Sanidade' },
            { id: 'alimentacao', rotulo: 'Alimentação' },
            { id: 'producao', rotulo: 'Produção' },
            { id: 'manejo', rotulo: 'Manejo' },
          ]}
        />
        {historico.carga.situacao === 'carregando' ? <EsqueletoLista linhas={4} /> : null}
        {historico.carga.situacao === 'erro' ? (
          <EstadoErro mensagem={historico.carga.mensagem} onTentar={historico.recarregar} />
        ) : null}
        {historico.carga.situacao === 'pronto' ? <HistoricoDaAba itens={historico.carga.dados.itens} aba={aba} loteId={dados.id} /> : null}
      </section>

      <PainelEntradaAves key={`a${abertura}`} {...propsDialogo} aberto={aberto === 'entrada'} />
      {podeHigienizar ? <PainelHigienizacao key={`h${abertura}`} aberto={aberto === 'higienizacao'} onFechar={() => setAberto(null)} lotes={[dados]} loteInicial={String(dados.id)} onConcluir={atualizar} /> : null}
      {podeIluminar ? <PainelIluminacao key={`i${abertura}`} aberto={aberto === 'iluminacao'} onFechar={() => setAberto(null)} lotes={[dados]} loteInicial={String(dados.id)} onConcluir={atualizar} /> : null}
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

function HistoricoDaAba({ itens, aba, loteId }: { itens: ItemHistorico[]; aba: Aba; loteId: number }) {
  const visiveis = aba === 'historico' ? itens : itens.filter((i) => CATEGORIAS_DA_ABA[aba].includes(i.categoria))
  if (visiveis.length === 0) {
    return (
      <EstadoVazio
        icone="calendario"
        titulo="Nada registrado ainda"
        texto={aba === 'historico' ? 'Mortalidade, baixas e outros lançamentos aparecem aqui.' : 'Quando houver registros dessa área, eles aparecem aqui.'}
      />
    )
  }
  return (
    <>
      <LinhaDoTempo itens={visiveis} />
      {aba === 'producao' ? (
        <Link to={`/producao?lote=${loteId}`} className={classesBotao('contorno', 'md', 'mt-6')}>
          Ver o gráfico em Produção
        </Link>
      ) : null}
    </>
  )
}
