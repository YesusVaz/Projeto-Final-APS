package br.ufpb.aps.granja.model;

/** RN21: encerrar exige motivo, e o motivo define a baixa do que sobrou. */
public enum MotivoEncerramento {
    VENDA(TipoMovimentoLote.BAIXA_VENDA),
    DESCARTE(TipoMovimentoLote.BAIXA_DESCARTE),
    TRANSFERENCIA(TipoMovimentoLote.BAIXA_TRANSFERENCIA);

    private final TipoMovimentoLote tipoDeBaixa;

    MotivoEncerramento(TipoMovimentoLote tipoDeBaixa) {
        this.tipoDeBaixa = tipoDeBaixa;
    }

    public TipoMovimentoLote tipoDeBaixa() {
        return tipoDeBaixa;
    }
}
