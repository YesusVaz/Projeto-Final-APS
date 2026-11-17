package br.ufpb.aps.granja.model.excecao;

/** RN22: o mesmo codigo de lancamento nao pode entrar duas vezes. */
public class LancamentoDuplicadoException extends RegraDeNegocioException {
    public LancamentoDuplicadoException(String codigo) {
        super("O lançamento " + codigo + " já foi registrado; nenhum efeito foi aplicado.");
    }
}
