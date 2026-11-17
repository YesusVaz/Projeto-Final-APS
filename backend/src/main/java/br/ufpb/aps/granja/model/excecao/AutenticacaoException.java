package br.ufpb.aps.granja.model.excecao;

/**
 * RN02 e RNF09: credencial errada ou usuario inativo. A mensagem e a mesma nos dois casos,
 * pra nao revelar quais logins existem. Vira HTTP 401.
 */
public class AutenticacaoException extends RegraDeNegocioException {
    public AutenticacaoException() {
        super("Login recusado: usuário inativo ou credenciais inválidas.");
    }
}
