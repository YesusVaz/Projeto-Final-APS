package br.ufpb.aps.granja.model;

import java.time.LocalDate;
import java.util.List;

/** UC22: totais do periodo, quebrados por categoria, mais os lancamentos que entraram na conta. */
public record RelatorioFinanceiro(LocalDate de, LocalDate ate, long receitasCentavos, long despesasCentavos,
                                  List<TotalPorCategoria> porCategoria, List<LancamentoFinanceiro> lancamentos) {

    public record TotalPorCategoria(CategoriaFinanceira categoria, long totalCentavos) {
    }

    public long resultadoCentavos() {
        return receitasCentavos - despesasCentavos;
    }

    public static RelatorioFinanceiro de(LocalDate de, LocalDate ate, List<LancamentoFinanceiro> lancamentos) {
        long receitas = soma(lancamentos, TipoFinanceiro.RECEITA);
        long despesas = soma(lancamentos, TipoFinanceiro.DESPESA);
        List<TotalPorCategoria> porCategoria = lancamentos.stream()
                .collect(java.util.stream.Collectors.groupingBy(LancamentoFinanceiro::categoria,
                        java.util.stream.Collectors.summingLong(LancamentoFinanceiro::valorCentavos)))
                .entrySet().stream()
                .map(e -> new TotalPorCategoria(e.getKey(), e.getValue()))
                .sorted(java.util.Comparator.comparing((TotalPorCategoria t) -> t.categoria().tipo())
                        .thenComparing(java.util.Comparator.comparingLong(TotalPorCategoria::totalCentavos).reversed()))
                .toList();
        return new RelatorioFinanceiro(de, ate, receitas, despesas, porCategoria, lancamentos);
    }

    private static long soma(List<LancamentoFinanceiro> lancamentos, TipoFinanceiro tipo) {
        return lancamentos.stream().filter(l -> l.tipo() == tipo).mapToLong(LancamentoFinanceiro::valorCentavos).sum();
    }
}
