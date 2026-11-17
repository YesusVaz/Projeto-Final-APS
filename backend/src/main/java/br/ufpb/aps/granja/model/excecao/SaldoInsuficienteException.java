package br.ufpb.aps.granja.model.excecao;

/** RN06 (revisada) e RN07: saldo nunca fica negativo. */
public class SaldoInsuficienteException extends RegraDeNegocioException {
    private final int disponivel;

    public SaldoInsuficienteException(int disponivel) {
        super("Saldo insuficiente. Disponível: " + disponivel);
        this.disponivel = disponivel;
    }

    public int disponivel() {
        return disponivel;
    }
}
