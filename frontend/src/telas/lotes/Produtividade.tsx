import { numero } from '../../api/formatar'
import { consultarProdutividade, type Produtividade as DadosProdutividade } from '../../api/manejo'
import { useCarregar } from '../../hooks/useCarregar'
import { EsqueletoLista, EstadoErro, Estatistica, TituloSecao } from '../../ui'

const decimal = (v: number | null, casas = 2) => (v === null ? '—' : v.toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas }))

// UC18: como o lote esta rendendo. so o gestor ve. o que ainda nao da pra calcular aparece como traco
export function Produtividade({ loteId, versao }: { loteId: number; versao: number }) {
  const { carga, recarregar } = useCarregar((token) => consultarProdutividade(token, loteId), versao)
  return (
    <section aria-labelledby="titulo-produtividade" className="pb-12">
      <TituloSecao id="titulo-produtividade" className="mb-5 md:text-lg md:tracking-[-0.015em] md:text-ink">
        Produtividade
      </TituloSecao>
      {carga.situacao === 'carregando' ? <EsqueletoLista linhas={2} /> : null}
      {carga.situacao === 'erro' ? <EstadoErro mensagem={carga.mensagem} onTentar={recarregar} /> : null}
      {carga.situacao === 'pronto' ? <Indicadores p={carga.dados} /> : null}
    </section>
  )
}

function Indicadores({ p }: { p: DadosProdutividade }) {
  const corte = p.especie === 'FRANGO_CORTE'
  return (
    <div className="grid grid-cols-2 gap-x-7 gap-y-8 lg:grid-cols-4">
      <Estatistica tamanho="medio" indice={0} valor={`${decimal(p.taxaDeMortalidade)}%`} rotulo="Mortalidade" descricao={`${numero(p.mortes)} de ${numero(p.totalEntrado)} aves`} />
      <Estatistica tamanho="medio" indice={1} valor={`${numero(p.racaoConsumidaKg)} kg`} rotulo="Ração consumida" descricao="Desde o alojamento" />
      {corte ? (
        <>
          <Estatistica
            tamanho="medio"
            indice={2}
            valor={decimal(p.conversaoAlimentar)}
            rotulo="Conversão alimentar"
            descricao={p.conversaoAlimentar === null ? 'Precisa de duas pesagens e ração no período' : 'kg de ração por kg ganho. Menor é melhor'}
          />
          <Estatistica
            tamanho="medio"
            indice={3}
            valor={p.ganhoDePesoKg === null ? '—' : `${decimal(p.ganhoDePesoKg, 1)} kg`}
            rotulo="Peso ganho"
            descricao={p.gmdMedio === null ? 'Precisa de duas pesagens' : `Ganho médio de ${decimal(p.gmdMedio, 1)} g por dia`}
          />
        </>
      ) : (
        <>
          <Estatistica
            tamanho="medio"
            indice={2}
            valor={p.ovosTotal === null ? '—' : numero(p.ovosTotal)}
            rotulo="Ovos coletados"
            descricao={p.taxaMediaDePostura === null ? 'Ainda sem postura registrada' : `Postura média de ${decimal(p.taxaMediaDePostura, 1)}%`}
          />
          <Estatistica
            tamanho="medio"
            indice={3}
            valor={decimal(p.racaoPorDuzia)}
            rotulo="Ração por dúzia"
            descricao={p.racaoPorDuzia === null ? 'Precisa de postura e ração no período' : 'kg de ração por dúzia de ovos'}
          />
        </>
      )}
    </div>
  )
}
