package br.ufpb.aps.granja.persistencia;

/** Falha tecnica no banco. Nao e regra de negocio, vira HTTP 500. */
public class ErroDePersistencia extends RuntimeException {
    public ErroDePersistencia(String operacao, Throwable causa) {
        super("Falha ao " + operacao + " no banco de dados.", causa);
    }
}
