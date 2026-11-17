package br.ufpb.aps.granja.model;

import java.time.LocalDate;
import java.util.Objects;

/** UC12: medicamento dado numa ocorrencia. Gasta o medicamento no estoque (RN09). */
public record Tratamento(Long id,
                         Long ocorrenciaId,
                         Long insumoId,
                         Quantidade quantidade,
                         LocalDate data,
                         CodigoLancamento codigo,
                         String observacao) {

    public Tratamento {
        Objects.requireNonNull(quantidade, "quantidade");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(codigo, "codigo");
        if (quantidade.ehZero()) {
            throw new IllegalArgumentException("A quantidade do medicamento deve ser maior que zero");
        }
    }
}
