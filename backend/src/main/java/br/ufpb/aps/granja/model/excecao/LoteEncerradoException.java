package br.ufpb.aps.granja.model.excecao;

/** RN05 e RN21: lote encerrado nao recebe nada e nao encerra de novo. */
<<<<<<< HEAD
public class LoteEncerradoException extends ConflitoException {
=======
public class LoteEncerradoException extends RegraDeNegocioException {
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    public LoteEncerradoException(String codigoLote) {
        super("O lote " + codigoLote + " está encerrado e não aceita novos registros.");
    }
}
