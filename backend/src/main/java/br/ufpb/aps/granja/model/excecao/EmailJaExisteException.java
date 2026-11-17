package br.ufpb.aps.granja.model.excecao;

/** Ja existe usuario com esse e-mail. O e-mail tambem e unico. Vira HTTP 409. */
public class EmailJaExisteException extends ConflitoException {
    public EmailJaExisteException(String email) {
        super("Já existe um usuário com o e-mail " + email + ".");
    }
}
