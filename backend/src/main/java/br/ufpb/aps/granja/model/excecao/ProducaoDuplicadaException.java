package br.ufpb.aps.granja.model.excecao;

import java.time.LocalDate;

/** Um registro de cada tipo por lote por dia. Senao a taxa do dia fica ambigua. Vira HTTP 409. */
public class ProducaoDuplicadaException extends ConflitoException {
    public ProducaoDuplicadaException(String tipo, String codigoLote, LocalDate data) {
        super("Já existe registro de " + tipo + " do lote " + codigoLote + " em " + data + ".");
    }
}
