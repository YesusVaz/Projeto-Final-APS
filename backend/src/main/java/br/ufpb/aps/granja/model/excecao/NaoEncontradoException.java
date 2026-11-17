package br.ufpb.aps.granja.model.excecao;

/** Registro pedido nao existe. Vira HTTP 404. */
public class NaoEncontradoException extends RegraDeNegocioException {
    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
