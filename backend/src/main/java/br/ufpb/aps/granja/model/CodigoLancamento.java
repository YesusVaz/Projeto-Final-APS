package br.ufpb.aps.granja.model;

/** Codigo do lancamento (RN22). Se repetir o codigo, o efeito nao se repete. */
public record CodigoLancamento(String valor) {
    public CodigoLancamento {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Código de lançamento é obrigatório");
        }
        valor = valor.trim();
    }
}
