import { useState } from 'react'
import { Abas, CabecalhoPagina, Pagina } from '../../ui'
import { Graficos } from './Graficos'
import { RelatorioGerencial } from './RelatorioGerencial'

type Aba = 'graficos' | 'relatorio'

// area do gestor: graficos de desempenho (UC26) e relatorio gerencial (UC25)
export function Relatorios() {
  const [aba, setAba] = useState<Aba>('graficos')
  return (
    <Pagina>
      <CabecalhoPagina titulo="Relatórios" descricao="Como a granja está indo, em gráficos e em números." />
      <Abas
        rotulo="O que mostrar"
        valor={aba}
        onMudar={setAba}
        className="mb-8"
        opcoes={[
          { id: 'graficos', rotulo: 'Gráficos' },
          { id: 'relatorio', rotulo: 'Relatório gerencial' },
        ]}
      />
      {aba === 'graficos' ? <Graficos /> : <RelatorioGerencial />}
    </Pagina>
  )
}
