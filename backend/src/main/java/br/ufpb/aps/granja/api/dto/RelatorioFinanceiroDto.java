package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.RelatorioFinanceiro;

import java.util.List;

public record RelatorioFinanceiroDto(String de, String ate, long receitasCentavos, long despesasCentavos,
                                     long resultadoCentavos, List<TotalDto> porCategoria,
                                     List<LancamentoFinanceiroDto> lancamentos) {

    public record TotalDto(String categoria, String tipo, long totalCentavos) {
    }

    public static RelatorioFinanceiroDto de(RelatorioFinanceiro r) {
        return new RelatorioFinanceiroDto(r.de().toString(), r.ate().toString(), r.receitasCentavos(),
                r.despesasCentavos(), r.resultadoCentavos(),
                r.porCategoria().stream()
                        .map(t -> new TotalDto(t.categoria().name(), t.categoria().tipo().name(), t.totalCentavos()))
                        .toList(),
                r.lancamentos().stream().map(LancamentoFinanceiroDto::de).toList());
    }
}
