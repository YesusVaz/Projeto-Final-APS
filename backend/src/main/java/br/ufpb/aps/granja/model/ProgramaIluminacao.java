package br.ufpb.aps.granja.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * UC15: programa de iluminacao do lote, com as horas de luz por dia a partir de uma data.
 * Um programa novo vale a partir da sua data e o anterior fica no historico. A intensidade e opcional.
 */
public record ProgramaIluminacao(Long id, Long loteId, int horasDeLuz, Integer intensidadeLux, LocalDate dataInicio,
                                 String observacao, CodigoLancamento codigo) {

    public ProgramaIluminacao {
        Objects.requireNonNull(dataInicio, "dataInicio");
        Objects.requireNonNull(codigo, "codigo");
        if (horasDeLuz < 1 || horasDeLuz > 24) {
            throw new IllegalArgumentException("As horas de luz por dia devem ficar entre 1 e 24.");
        }
        if (intensidadeLux != null && intensidadeLux <= 0) {
            throw new IllegalArgumentException("A intensidade da luz deve ser maior que zero.");
        }
        observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
        if (observacao != null && observacao.length() > 300) {
            throw new IllegalArgumentException("A observação passou de 300 caracteres.");
        }
    }
}
