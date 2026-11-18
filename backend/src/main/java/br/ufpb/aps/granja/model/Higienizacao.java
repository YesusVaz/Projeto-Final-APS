package br.ufpb.aps.granja.model;

import java.time.LocalDate;
import java.util.Objects;

/** UC14: uma higienizacao feita no galpao de um lote. */
public record Higienizacao(Long id, Long loteId, TipoHigienizacao tipo, LocalDate data, String observacao,
                           CodigoLancamento codigo) {

    public Higienizacao {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(codigo, "codigo");
        observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
        if (observacao != null && observacao.length() > 300) {
            throw new IllegalArgumentException("A observação passou de 300 caracteres.");
        }
    }
}
