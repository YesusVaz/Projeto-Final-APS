package br.ufpb.aps.granja.model.excecao;

/** RN25: o perfil nao pode essa acao. Vira HTTP 403 no controller. */
public class AcessoNegadoException extends RegraDeNegocioException {
    public AcessoNegadoException(String perfil, String acao) {
        super("O perfil " + perfil + " não tem permissão para a ação " + acao + ".");
    }
}
