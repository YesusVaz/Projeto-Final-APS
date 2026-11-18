import { useId, useState, type ReactNode } from 'react'
import { Cab, Cel, cn, Linha, Tabela, TabelaCabeca, TabelaCorpo, TituloSecao } from '../../../ui'
import { FUNDO_DA_SERIE, type Serie } from './series'

type Props = {
  titulo: string
  descricao: string
  // legenda so aparece com duas ou mais series. com uma, o titulo ja diz o que e
  series: Serie[]
  // linhas da versao em tabela, pra quem nao enxerga o grafico ou prefere os numeros
  rotulosDaTabela: string[]
  tituloDaPrimeiraColuna: string
  formatar: (valor: number) => string
  children: ReactNode
}

// moldura de um grafico: titulo, legenda, o grafico e o botao que troca ele pela tabela dos mesmos numeros
export function CartaoGrafico({ titulo, descricao, series, rotulosDaTabela, tituloDaPrimeiraColuna, formatar, children }: Props) {
  const [tabela, setTabela] = useState(false)
  const id = useId()
  return (
    <section aria-labelledby={id} className="rounded-3xl border border-line-card bg-surface p-5 md:p-6">
      <div className="mb-4 flex flex-wrap items-start justify-between gap-x-6 gap-y-2">
        <div>
          <TituloSecao id={id} className="md:text-lg md:tracking-[-0.015em] md:text-ink">
            {titulo}
          </TituloSecao>
          <p className="text-[13.5px] text-ink-3">{descricao}</p>
        </div>
        {series.length > 1 ? (
          <ul className="flex items-center gap-4 text-[13px] text-ink-2" aria-label="Legenda">
            {series.map((s) => (
              <li key={s.nome} className="flex items-center gap-2">
                <span className={cn('size-2.5 rounded-sm', FUNDO_DA_SERIE[s.tom])} aria-hidden />
                {s.nome}
              </li>
            ))}
          </ul>
        ) : null}
      </div>

      {tabela ? (
        <div className="max-h-[260px] overflow-y-auto">
          <Tabela>
            <TabelaCabeca>
              <tr>
                <Cab>{tituloDaPrimeiraColuna}</Cab>
                {series.map((s) => (
                  <Cab key={s.nome} numerica>
                    {s.nome}
                  </Cab>
                ))}
              </tr>
            </TabelaCabeca>
            <TabelaCorpo>
              {rotulosDaTabela.map((r, i) => (
                <Linha key={r}>
                  <Cel className="py-2.5 text-[14px]">{r}</Cel>
                  {series.map((s) => (
                    <Cel key={s.nome} numerica className="py-2.5 text-[14px]">
                      {formatar(s.valores[i])}
                    </Cel>
                  ))}
                </Linha>
              ))}
            </TabelaCorpo>
          </Tabela>
        </div>
      ) : (
        children
      )}

      <button
        type="button"
        onClick={() => setTabela((t) => !t)}
        aria-pressed={tabela}
        aria-label={`${tabela ? 'Ver como gráfico' : 'Ver como tabela'}: ${descricao}`}
        className="mt-3 min-h-11 rounded text-[13.5px] font-medium text-accent hover:underline hover:underline-offset-4"
      >
        {tabela ? 'Ver como gráfico' : 'Ver como tabela'}
      </button>
    </section>
  )
}
