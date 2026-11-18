package br.ufpb.aps.granja.model;

import static br.ufpb.aps.granja.model.TipoFinanceiro.DESPESA;
import static br.ufpb.aps.granja.model.TipoFinanceiro.RECEITA;

/** Categoria de cada lancamento. COMPRA_INSUMO so nasce da compra (RN13), nao do lancamento manual de despesa. */
public enum CategoriaFinanceira {
    COMPRA_INSUMO(DESPESA),
    MAO_DE_OBRA(DESPESA),
    ENERGIA_AGUA(DESPESA),
    MANUTENCAO(DESPESA),
    TRANSPORTE(DESPESA),
    OUTRA_DESPESA(DESPESA),
    VENDA_OVOS(RECEITA),
    VENDA_AVES(RECEITA),
    OUTRA_RECEITA(RECEITA);

    private final TipoFinanceiro tipo;

    CategoriaFinanceira(TipoFinanceiro tipo) {
        this.tipo = tipo;
    }

    public TipoFinanceiro tipo() {
        return tipo;
    }
}
