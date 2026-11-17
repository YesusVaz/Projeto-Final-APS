package br.ufpb.aps.granja.model.excecao;

/** Regra violada por conflito com o estado atual (duplicidade, lote encerrado). Vira HTTP 409. */
public class ConflitoException extends RegraDeNegocioException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
