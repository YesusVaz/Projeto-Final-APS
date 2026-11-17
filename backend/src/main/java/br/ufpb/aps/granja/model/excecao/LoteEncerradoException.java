package br.ufpb.aps.granja.model.excecao;

/** RN05 e RN21: lote encerrado nao recebe nada e nao encerra de novo. */
public class LoteEncerradoException extends RegraDeNegocioException {
    public LoteEncerradoException(String codigoLote) {
        super("O lote " + codigoLote + " está encerrado e não aceita novos registros.");
    }
}
