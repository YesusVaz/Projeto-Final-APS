package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.LancamentoFinanceiro;

/** Valores em centavos inteiros. {@code tipo} e DESPESA ou RECEITA. */
public record LancamentoFinanceiroDto(long id, String tipo, String categoria, String descricao, long valorCentavos,
                                      String data, Long loteId, Long fornecedorId, Long insumoId,
                                      Integer quantidade, String codigo) {

    public static LancamentoFinanceiroDto de(LancamentoFinanceiro l) {
        return new LancamentoFinanceiroDto(l.id(), l.tipo().name(), l.categoria().name(), l.descricao(),
                l.valorCentavos(), l.data().toString(), l.loteId(), l.fornecedorId(), l.insumoId(), l.quantidade(),
                l.codigo().valor());
    }
}
