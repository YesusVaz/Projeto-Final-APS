package br.ufpb.aps.granja.model;

import java.time.LocalDate;
import java.util.Objects;

/** UC13: consumo de racao de um lote. Gera saida automatica no estoque (RN10), tudo ou nada (RN23). */
public record RegistroAlimentacao(Long id,
                                  Long loteId,
                                  Long insumoId,
                                  Quantidade quantidadeKg,
                                  LocalDate data,
                                  CodigoLancamento codigo) {
    public RegistroAlimentacao {
        Objects.requireNonNull(quantidadeKg, "quantidadeKg");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(codigo, "codigo");
        if (quantidadeKg.ehZero()) {
            throw new IllegalArgumentException("A quantidade de ração deve ser maior que zero");
        }
    }
}
