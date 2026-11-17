package br.ufpb.aps.granja.model.excecao;

/** Violacao de regra de negocio esperada. A mensagem e pro usuario final. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
