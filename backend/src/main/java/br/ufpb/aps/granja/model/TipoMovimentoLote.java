package br.ufpb.aps.granja.model;

/** Tipos de movimento que mexem no saldo de animais do lote. */
public enum TipoMovimentoLote {
    ENTRADA_INICIAL(1),
    /** Animais que chegaram depois do cadastro, de um novo fornecedor ou da mesma remessa. */
    ENTRADA_ADICIONAL(1),
    MORTALIDADE(-1),
    BAIXA_VENDA(-1),
    BAIXA_DESCARTE(-1),
    BAIXA_TRANSFERENCIA(-1);

    private final int sinal;

    TipoMovimentoLote(int sinal) {
        this.sinal = sinal;
    }

    public int sinal() {
        return sinal;
    }
}
