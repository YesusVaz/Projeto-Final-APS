package br.ufpb.aps.granja.model.excecao;

/** Ja existe usuario com esse login. Login e unico (RN01). Vira HTTP 409. */
public class LoginJaExisteException extends ConflitoException {
    public LoginJaExisteException(String login) {
        super("Já existe um usuário com o login " + login + ".");
    }
}
