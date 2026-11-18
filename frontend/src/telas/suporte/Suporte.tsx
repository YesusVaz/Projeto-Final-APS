import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { dataHora } from '../../api/formatar'
import {
  listarMeusChamados,
  listarTodosChamados,
  ROTULO_CATEGORIA_CHAMADO,
  ROTULO_STATUS_CHAMADO,
  type Chamado,
  type StatusChamado,
} from '../../api/suporte'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import { Abas, Botao, CabecalhoPagina, EsqueletoLista, EstadoVazio, Pagina, propsItem, Selo, type NivelSelo } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { motion } from 'motion/react'
import { PainelChamado } from './PainelChamado'
import { PainelResposta } from './PainelResposta'

const NIVEL_STATUS: Record<StatusChamado, NivelSelo> = { ABERTO: 'atencao', EM_ATENDIMENTO: 'info', RESOLVIDO: 'normal' }

type Aba = 'meus' | 'todos'

// UC30: qualquer perfil abre chamados e acompanha os seus. O gestor tambem ve todos e responde
export function Suporte() {
  const { perfil } = useLogado()
  const gestor = permite(perfil, 'gerenciarChamados')
  const [params, setParams] = useSearchParams()
  const [aba, setAba] = useState<Aba>(gestor ? 'todos' : 'meus')
  const [respondendo, setRespondendo] = useState<Chamado | null>(null)

  const meus = useCarregar(listarMeusChamados)
  // so o gestor pode pedir todos. os outros perfis levariam 403
  const todos = useCarregar((t) => (gestor ? listarTodosChamados(t) : Promise.resolve([])))
  const lista = aba === 'todos' ? todos : meus
  const naoResolvidos = todos.carga.situacao === 'pronto' ? todos.carga.dados.filter((c) => c.status !== 'RESOLVIDO').length : undefined

  function recarregar() {
    meus.recarregar()
    todos.recarregar()
  }

  return (
    <Pagina>
      <CabecalhoPagina
        titulo="Suporte"
        descricao={gestor ? 'Os pedidos de ajuda da equipe e os seus.' : 'Tire uma dúvida, conte um problema ou mande uma sugestão.'}
        acoes={
          <Botao icone="mais" onClick={() => setParams({ novo: '1' })}>
            Abrir chamado
          </Botao>
        }
      />

      {gestor ? (
        <Abas
          rotulo="Quais chamados mostrar"
          valor={aba}
          onMudar={setAba}
          className="mb-6"
          opcoes={[
            { id: 'todos', rotulo: 'Todos os chamados', contador: naoResolvidos },
            { id: 'meus', rotulo: 'Meus chamados' },
          ]}
        />
      ) : null}

      <Carregavel carga={lista.carga} recarregar={lista.recarregar} esqueleto={<EsqueletoLista linhas={4} />}>
        {(chamados) =>
          chamados.length === 0 ? (
            <EstadoVazio
              icone="info"
              titulo={aba === 'todos' ? 'Nenhum chamado por enquanto' : 'Você ainda não abriu chamados'}
              texto={aba === 'todos' ? 'Quando alguém da equipe pedir ajuda, o chamado aparece aqui.' : 'Se tiver uma dúvida ou encontrar um problema, abra um chamado.'}
              acao={aba === 'meus' ? <Botao onClick={() => setParams({ novo: '1' })}>Abrir chamado</Botao> : undefined}
            />
          ) : (
            <ul>
              {chamados.map((c, i) => (
                <ItemChamado key={c.id} chamado={c} indice={i} mostrarAutor={aba === 'todos'} podeResponder={gestor} onResponder={() => setRespondendo(c)} />
              ))}
            </ul>
          )
        }
      </Carregavel>

      <PainelChamado aberto={params.has('novo')} onFechar={() => setParams({}, { replace: true })} onConcluir={recarregar} />
      <PainelResposta chamado={respondendo} onFechar={() => setRespondendo(null)} onConcluir={recarregar} />
    </Pagina>
  )
}

type ItemProps = {
  chamado: Chamado
  indice: number
  mostrarAutor: boolean
  podeResponder: boolean
  onResponder: () => void
}

function ItemChamado({ chamado: c, indice, mostrarAutor, podeResponder, onResponder }: ItemProps) {
  return (
    <motion.li {...propsItem(indice)} className="border-t border-line-soft py-5 first:border-t-0">
      <div className="flex flex-wrap items-start justify-between gap-x-4 gap-y-2">
        <div className="min-w-0 flex-1">
          <p className="font-medium">{c.assunto}</p>
          <p className="text-[13.5px] text-ink-3">
            {ROTULO_CATEGORIA_CHAMADO[c.categoria]}
            {mostrarAutor ? ` · ${c.solicitante}` : ''} · {dataHora(c.abertoEm)}
          </p>
        </div>
        <Selo nivel={NIVEL_STATUS[c.status]}>{ROTULO_STATUS_CHAMADO[c.status]}</Selo>
      </div>
      <p className="mt-2 max-w-[75ch] text-[14.5px] whitespace-pre-line text-ink-2">{c.descricao}</p>
      {c.resposta ? (
        <div className="mt-3 max-w-[75ch] rounded-2xl bg-trilho px-4 py-3">
          <p className="text-[12.5px] font-medium text-ink-3">Resposta do suporte</p>
          <p className="mt-0.5 text-[14.5px] whitespace-pre-line">{c.resposta}</p>
        </div>
      ) : null}
      {podeResponder && c.status !== 'RESOLVIDO' ? (
        <Botao variante="contorno" tamanho="sm" className="mt-3" onClick={onResponder}>
          Responder
        </Botao>
      ) : null}
    </motion.li>
  )
}
