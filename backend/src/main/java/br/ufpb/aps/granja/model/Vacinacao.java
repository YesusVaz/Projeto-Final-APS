package br.ufpb.aps.granja.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * UC11: vacina aplicada no lote. Gasta {@code doses} unidades da vacina no estoque (RN08).
 * {@code proximaDose} e o reforco, quando tem. E dele que sai o alerta sanitario (RN17).
 */
public record Vacinacao(Long id,
                        Long loteId,
                        Long insumoId,
                        Quantidade doses,
                        LocalDate data,
                        LocalDate proximaDose,
                        CodigoLancamento codigo,
                        String observacao) {

    public Vacinacao {
        Objects.requireNonNull(doses, "doses");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(codigo, "codigo");
        if (doses.ehZero()) {
            throw new IllegalArgumentException("A quantidade de doses deve ser maior que zero");
        }
        if (proximaDose != null && !proximaDose.isAfter(data)) {
            throw new IllegalArgumentException("A próxima dose deve ser depois da data da vacinação.");
        }
    }
}
