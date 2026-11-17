package br.ufpb.aps.granja.model;

import java.time.LocalDate;

/**
 * UC24: alerta calculado pro dia consultado. {@code referencia} e a data que causou o alerta
 * (dia do reforco ou abertura da ocorrencia).
 */
public record AlertaSanitario(Tipo tipo, long loteId, String codigoLote, String descricao, LocalDate referencia) {

    public enum Tipo {
        /** Reforco vence dentro do prazo de aviso. */
        VACINA_PROXIMA,
        /** Reforco ja passou do dia e nao foi aplicado. */
        VACINA_ATRASADA,
        /** Ocorrencia aberta faz mais tempo que o parametro permite. */
        OCORRENCIA_PROLONGADA
    }
}
