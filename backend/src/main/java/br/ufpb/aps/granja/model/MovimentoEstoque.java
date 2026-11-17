package br.ufpb.aps.granja.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Linha do extrato do insumo. {@code loteId} e opcional (consumo ligado a um lote). */
public record MovimentoEstoque(Long id,
                               Long insumoId,
                               TipoMovimentoEstoque tipo,
                               Quantidade quantidade,
                               CodigoLancamento codigo,
                               OrigemMovimento origem,
                               Long loteId,
                               LocalDateTime dataHora) {

    public MovimentoEstoque {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(quantidade, "quantidade");
        Objects.requireNonNull(codigo, "codigo");
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(dataHora, "dataHora");
        if (quantidade.ehZero()) {
            throw new IllegalArgumentException("A quantidade do movimento deve ser maior que zero");
        }
    }
}
