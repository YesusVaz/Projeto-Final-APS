import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { listarInsumos } from '../../api/estoque'
import { listarLotes } from '../../api/lotes'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import { Abas, Botao, CabecalhoPagina, EsqueletoLista, Pagina } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { ListaOcorrencias } from './ListaOcorrencias'
import { ListaVacinacoes } from './ListaVacinacoes'
import { PainelVacinacao } from './PainelVacinacao'

type Aba = 'ocorrencias' | 'vacinacoes'

export function Sanidade() {
  const { perfil } = useLogado()
  const lotes = useCarregar(listarLotes)
  const insumos = useCarregar(listarInsumos)
  const [aba, setAba] = useState<Aba>('ocorrencias')
  // o painel de registro abre por ?vacinar=1 (e &lote=ID pra ja vir escolhido)
  const [params, setParams] = useSearchParams()
  const podeVacinar = permite(perfil, 'registrarVacinacao')
  const listaLotes = lotes.carga.situacao === 'pronto' ? lotes.carga.dados.filter((l) => l.status === 'ATIVO') : []
  const vacinas = insumos.carga.situacao === 'pronto' ? insumos.carga.dados.filter((i) => i.categoria === 'VACINA' && i.ativo) : []

  return (
    <Pagina>
      <CabecalhoPagina
        titulo="Sanidade"
        descricao="Ocorrências, tratamentos e vacinas dos lotes."
        acoes={
          podeVacinar ? (
            <Botao icone="mais" onClick={() => setParams({ vacinar: '1' })}>
              Registrar
            </Botao>
          ) : undefined
        }
      />
      <Abas
        rotulo="Seção de sanidade"
        valor={aba}
        onMudar={setAba}
        className="mb-6"
        opcoes={[
          { id: 'ocorrencias', rotulo: 'Ocorrências' },
          { id: 'vacinacoes', rotulo: 'Vacinações' },
        ]}
      />
      <Carregavel carga={lotes.carga} recarregar={lotes.recarregar} esqueleto={<EsqueletoLista linhas={4} />}>
        {(listaLotes) => (
          <Carregavel carga={insumos.carga} recarregar={insumos.recarregar} esqueleto={<EsqueletoLista linhas={4} />}>
            {(listaInsumos) =>
              aba === 'ocorrencias' ? (
                <ListaOcorrencias lotes={listaLotes} insumos={listaInsumos} />
              ) : (
                <ListaVacinacoes lotes={listaLotes} insumos={listaInsumos} />
              )
            }
          </Carregavel>
        )}
      </Carregavel>
      {podeVacinar ? (
        <PainelVacinacao
          aberto={params.has('vacinar')}
          onFechar={() => setParams({}, { replace: true })}
          lotes={listaLotes}
          vacinas={vacinas}
          loteInicial={params.get('lote') ?? ''}
          onConcluir={() => {
            lotes.recarregar()
            insumos.recarregar()
          }}
        />
      ) : null}
    </Pagina>
  )
}
