package br.ufpb.aps.granja.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * UC16: um registro de producao. {@code valor} e a quantidade de ovos ou o peso medio em gramas.
 * {@code vivos} e quantos animais tinha no lote na hora (so na postura).
 * {@code indicador} e a taxa de postura em % ou o GMD em g/dia. Na primeira pesagem nao tem GMD, fica nulo.
 */
public record RegistroProducao(Long id,
                               Long loteId,
                               TipoProducao tipo,
                               LocalDate data,
                               int valor,
                               Integer vivos,
                               Double indicador,
                               CodigoLancamento codigo) {

    public RegistroProducao {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(codigo, "codigo");
        if (valor < 0) {
            throw new IllegalArgumentException("O valor da produção não pode ser negativo");
        }
    }
}
