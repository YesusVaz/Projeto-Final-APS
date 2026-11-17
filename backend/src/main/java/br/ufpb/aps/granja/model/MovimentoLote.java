package br.ufpb.aps.granja.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro imutavel de uma mudanca no saldo de animais do lote (historico).
 * {@code observacao} guarda a causa da morte ou o motivo da baixa. Pode ser nula.
 */
public record MovimentoLote(Long id,
                            Long loteId,
                            TipoMovimentoLote tipo,
                            Quantidade quantidade,
                            CodigoLancamento codigo,
                            LocalDateTime dataHora,
                            String observacao) {

    public MovimentoLote {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(quantidade, "quantidade");
        Objects.requireNonNull(codigo, "codigo");
        Objects.requireNonNull(dataHora, "dataHora");
        if (quantidade.ehZero()) {
            throw new IllegalArgumentException("A quantidade do movimento deve ser maior que zero");
        }
    }

    /** Efeito no saldo: positivo se entra, negativo se sai. */
    public int efeitoNoSaldo() {
        return tipo.sinal() * quantidade.valor();
    }
}
