package br.ufpb.aps.granja.model.excecao;

/** RN22: o mesmo codigo de lancamento nao pode entrar duas vezes. */
<<<<<<< HEAD
public class LancamentoDuplicadoException extends ConflitoException {
=======
public class LancamentoDuplicadoException extends RegraDeNegocioException {
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    public LancamentoDuplicadoException(String codigo) {
        super("O lançamento " + codigo + " já foi registrado; nenhum efeito foi aplicado.");
    }
}
