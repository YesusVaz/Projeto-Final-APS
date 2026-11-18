import { motion } from 'motion/react'
import { Link } from 'react-router'
import { numero, reais } from '../../api/formatar'
import { consultarPainel } from '../../api/gestao'
import { useCarregar } from '../../hooks/useCarregar'
import { Esqueleto, EstadoErro, Estatistica, propsItem, TituloSecao } from '../../ui'

// UC23: o mes ate hoje, so pro gestor. a busca e propria: se cair, o resto do Inicio segue de pe
export function NoMes() {
  const { carga, recarregar } = useCarregar(consultarPainel)
  return (
    <motion.section {...propsItem(2)} aria-labelledby="titulo-no-mes" className="mb-12">
      <div className="mb-5 flex items-center justify-between">
        <TituloSecao id="titulo-no-mes" className="md:text-lg md:tracking-[-0.015em] md:text-ink">
          Neste mês
        </TituloSecao>
        <Link to="/relatorios" className="rounded text-[13.5px] font-medium text-accent hover:underline hover:underline-offset-4">
          Ver relatórios
        </Link>
      </div>
      {carga.situacao === 'carregando' ? <Esqueleto className="h-24 w-full" /> : null}
      {carga.situacao === 'erro' ? <EstadoErro mensagem={carga.mensagem} onTentar={recarregar} /> : null}
      {carga.situacao === 'pronto' ? (
        <div className="flex flex-col gap-8">
          <div className="grid gap-x-7 gap-y-8 sm:grid-cols-3">
            <Estatistica tamanho="medio" indice={0} valor={reais(carga.dados.receitasDoMesCentavos)} rotulo="Receitas" descricao="O que entrou" />
            <Estatistica tamanho="medio" indice={1} valor={reais(carga.dados.despesasDoMesCentavos)} rotulo="Despesas" descricao="O que saiu" />
            <Estatistica
              tamanho="medio"
              indice={2}
              valor={reais(carga.dados.resultadoDoMesCentavos)}
              rotulo={carga.dados.resultadoDoMesCentavos >= 0 ? 'Lucro' : 'Prejuízo'}
              descricao="Receitas menos despesas"
            />
          </div>
          <div className="grid grid-cols-2 gap-x-7 gap-y-8 sm:grid-cols-3">
            <Estatistica tamanho="medio" indice={3} valor={numero(carga.dados.mortesNoMes)} rotulo="Aves perdidas" descricao="Em todos os lotes" />
            <Estatistica tamanho="medio" indice={4} valor={numero(carga.dados.racaoNoMesKg)} rotulo="Ração servida" descricao="Em quilos" />
            <Estatistica tamanho="medio" indice={5} valor={numero(carga.dados.ovosNoMes)} rotulo="Ovos coletados" descricao="Em todas as poedeiras" />
          </div>
        </div>
      ) : null}
    </motion.section>
  )
}
