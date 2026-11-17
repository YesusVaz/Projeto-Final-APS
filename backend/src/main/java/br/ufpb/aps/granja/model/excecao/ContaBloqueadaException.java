package br.ufpb.aps.granja.model.excecao;

/**
 * RN03: 5 tentativas erradas seguidas travam o login por 15 minutos. Vira HTTP 401 como as outras
 * falhas de login. Conta qualquer login digitado, existindo ou nao, pra nao entregar quem existe.
 */
public class ContaBloqueadaException extends AutenticacaoException {
    public ContaBloqueadaException(long minutosRestantes) {
        super("Muitas tentativas de login sem sucesso. Tente de novo em " + minutosRestantes + " minuto(s).");
    }
}
