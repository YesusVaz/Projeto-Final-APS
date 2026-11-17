package br.ufpb.aps.granja.model.excecao;

/** RN19: insumo inativo nao recebe movimento. */
public class InsumoInativoException extends RegraDeNegocioException {
    public InsumoInativoException(String nomeInsumo) {
        super("O insumo " + nomeInsumo + " está inativo e não aceita movimentações.");
    }
}
