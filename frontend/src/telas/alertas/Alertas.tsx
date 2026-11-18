import { motion } from 'motion/react'
import { useState } from 'react'
import { listarAlertasEstoque } from '../../api/estoque'
import { dataCompleta, dataHora, numero } from '../../api/formatar'
import { listarAlertasMortalidade } from '../../api/lotes'
import { listarAlertasSanitarios } from '../../api/sanidade'
import { ROTULO_NIVEL } from '../../api/tipos'
import { ROTULO_ALERTA_SANITARIO } from '../../api/tiposExtras'
import { useCarregar } from '../../hooks/useCarregar'
import { permite } from '../../sessao/permissoes'
import { useLogado } from '../../sessao/sessaoContexto'
import { Abas, CabecalhoPagina, EsqueletoLista, EstadoVazio, ItemLista, Pagina, propsItem, Selo, type OpcaoAba } from '../../ui'
import { Carregavel } from '../comum/Carregavel'
import { NIVEL_SELO } from '../comum/lotes'

type Aba = 'mortalidade' | 'sanitarios' | 'estoque'

export function Alertas() {
  const { perfil } = useLogado()
  // cada perfil ve so as abas que a API deixa abrir (estoque: gestor; sanitarios: gestor e tecnico)
  const opcoes: OpcaoAba<Aba>[] = [{ id: 'mortalidade', rotulo: 'Mortalidade' }]
  if (permite(perfil, 'consultarAlertasSanitarios')) opcoes.push({ id: 'sanitarios', rotulo: 'Sanidade' })
  if (permite(perfil, 'consultarAlertasEstoque')) opcoes.push({ id: 'estoque', rotulo: 'Estoque' })
  const [aba, setAba] = useState<Aba>('mortalidade')

  return (
    <Pagina>
      <CabecalhoPagina titulo="Alertas" descricao="Cada alerta leva direto para a tela onde dá para resolver." />
      {opcoes.length > 1 ? <Abas rotulo="Tipo de alerta" opcoes={opcoes} valor={aba} onMudar={setAba} className="mb-6" /> : null}
      {aba === 'mortalidade' ? <AlertasMortalidade /> : null}
      {aba === 'sanitarios' ? <AlertasSanitarios /> : null}
      {aba === 'estoque' ? <AlertasEstoque /> : null}
    </Pagina>
  )
}

function SemAlertas({ texto }: { texto: string }) {
  return <EstadoVazio icone="ok" titulo="Nenhum alerta" texto={texto} />
}

function AlertasMortalidade() {
  const { carga, recarregar } = useCarregar(listarAlertasMortalidade)
  return (
    <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
      {(alertas) =>
        alertas.length === 0 ? (
          <SemAlertas texto="Nenhum lote passou dos limites de mortalidade." />
        ) : (
          <ul>
            {[...alertas]
              .sort((a, b) => Number(b.nivel === 'CRITICO') - Number(a.nivel === 'CRITICO'))
              .map((a, i) => (
                <motion.li key={`${a.loteId}-${a.dataHora}`} {...propsItem(i)}>
                  <ItemLista to={`/lotes/${a.loteId}`} className="min-h-14 flex-wrap gap-y-1">
                    <Selo nivel={NIVEL_SELO[a.nivel]}>{ROTULO_NIVEL[a.nivel]}</Selo>
                    <span className="flex-1">
                      <b className="font-mono font-medium">{a.codigoLote}</b>
                      <span className="text-ink-3"> · mortalidade em nível {ROTULO_NIVEL[a.nivel].toLowerCase()}</span>
                    </span>
                    <span className="text-[13px] text-ink-3">{dataHora(a.dataHora)}</span>
                  </ItemLista>
                </motion.li>
              ))}
          </ul>
        )
      }
    </Carregavel>
  )
}

function AlertasSanitarios() {
  const { carga, recarregar } = useCarregar(listarAlertasSanitarios)
  return (
    <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
      {(alertas) =>
        alertas.length === 0 ? (
          <SemAlertas texto="Nenhuma vacina vencendo e nenhuma ocorrência aberta há muito tempo." />
        ) : (
          <ul>
            {alertas.map((a, i) => (
              <motion.li key={`${a.tipo}-${a.loteId}-${a.referencia}`} {...propsItem(i)}>
                <ItemLista to={a.tipo === 'OCORRENCIA_PROLONGADA' ? '/sanidade' : `/sanidade?vacinar=1&lote=${a.loteId}`} className="min-h-14 flex-wrap gap-y-1">
                  <Selo nivel={a.tipo === 'VACINA_PROXIMA' ? 'atencao' : 'critico'}>{ROTULO_ALERTA_SANITARIO[a.tipo]}</Selo>
                  <span className="flex-1">
                    <b className="font-mono font-medium">{a.codigoLote}</b>
                    <span className="text-ink-3"> · {a.descricao}</span>
                  </span>
                  <span className="text-[13px] text-ink-3">{dataCompleta(a.referencia)}</span>
                </ItemLista>
              </motion.li>
            ))}
          </ul>
        )
      }
    </Carregavel>
  )
}

function AlertasEstoque() {
  const { carga, recarregar } = useCarregar(listarAlertasEstoque)
  return (
    <Carregavel carga={carga} recarregar={recarregar} esqueleto={<EsqueletoLista linhas={3} />}>
      {(insumos) =>
        insumos.length === 0 ? (
          <SemAlertas texto="Todos os insumos estão acima do nível mínimo." />
        ) : (
          <ul>
            {insumos.map((x, i) => (
              <motion.li key={x.id} {...propsItem(i)}>
                <ItemLista to={`/estoque?entrada=1&insumo=${x.id}`} className="min-h-14 flex-wrap gap-y-1">
                  <Selo nivel="atencao">Abaixo do mínimo</Selo>
                  <span className="flex-1">
                    <b className="font-medium">{x.nome}</b>
                    <span className="text-ink-3">
                      {' '}
                      · {numero(x.saldo)} de {numero(x.nivelMinimo)} {x.unidade}
                    </span>
                  </span>
                  <span className="text-[13px] text-accent">Dar entrada</span>
                </ItemLista>
              </motion.li>
            ))}
          </ul>
        )
      }
    </Carregavel>
  )
}
