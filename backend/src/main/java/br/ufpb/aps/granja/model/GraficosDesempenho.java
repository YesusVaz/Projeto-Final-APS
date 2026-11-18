package br.ufpb.aps.granja.model;

import java.time.YearMonth;
import java.util.List;

/**
 * UC26: series pros graficos. O financeiro vem por mes (os ultimos seis) e o resto por dia (os ultimos trinta),
 * com os dias sem registro valendo zero pra linha ficar continua.
 */
public record GraficosDesempenho(List<FinanceiroDoMes> financeiroPorMes, List<PontoDia> mortesPorDia,
                                 List<PontoDia> racaoPorDia, List<PontoDia> ovosPorDia) {

    public record FinanceiroDoMes(YearMonth mes, long receitasCentavos, long despesasCentavos) {
    }
}
