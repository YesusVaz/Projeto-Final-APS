package br.ufpb.aps.granja.model.excecao;

/** Ocorrencia encerrada nao recebe tratamento nem encerra de novo. Vira HTTP 409. */
public class OcorrenciaEncerradaException extends ConflitoException {
    public OcorrenciaEncerradaException(Long id) {
        super("A ocorrência " + id + " já está encerrada.");
    }
}
