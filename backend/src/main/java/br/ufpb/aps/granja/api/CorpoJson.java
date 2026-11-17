package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.model.CodigoLancamento;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Le e valida os campos do JSON de entrada. Campo ruim vira IllegalArgumentException (422). */
final class CorpoJson {

    private CorpoJson() {
    }

    private static JsonNode campoObrigatorio(JsonNode corpo, String campo) {
        if (corpo == null || !corpo.hasNonNull(campo)) {
            throw new IllegalArgumentException("Campo obrigatório ausente: " + campo + ".");
        }
        return corpo.get(campo);
    }

    static String texto(JsonNode corpo, String campo) {
        JsonNode valor = campoObrigatorio(corpo, campo);
        if (!valor.isTextual() || valor.asText().isBlank()) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser um texto não vazio.");
        }
        return valor.asText().trim();
    }

    /** Senha vem sem trim: espaco no fim faz parte da senha. */
    static String senha(JsonNode corpo) {
        JsonNode valor = campoObrigatorio(corpo, "senha");
        if (!valor.isTextual() || valor.asText().isEmpty()) {
            throw new IllegalArgumentException("Campo senha deve ser um texto não vazio.");
        }
        return valor.asText();
    }

    static String textoOpcional(JsonNode corpo, String campo) {
        return corpo == null || !corpo.hasNonNull(campo) ? null : corpo.get(campo).asText();
    }

    static boolean booleanoOpcional(JsonNode corpo, String campo, boolean padrao) {
        if (corpo == null || !corpo.hasNonNull(campo)) {
            return padrao;
        }
        if (!corpo.get(campo).isBoolean()) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser verdadeiro ou falso.");
        }
        return corpo.get(campo).asBoolean();
    }

    static int inteiro(JsonNode corpo, String campo) {
        JsonNode valor = campoObrigatorio(corpo, campo);
        if (!valor.isIntegralNumber() || !valor.canConvertToInt()) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser um número inteiro.");
        }
        return valor.asInt();
    }

    static long longo(JsonNode corpo, String campo) {
        JsonNode valor = campoObrigatorio(corpo, campo);
        if (!valor.isIntegralNumber()) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser um número inteiro.");
        }
        return valor.asLong();
    }

    static double real(JsonNode corpo, String campo) {
        JsonNode valor = campoObrigatorio(corpo, campo);
        if (!valor.isNumber()) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser um número.");
        }
        return valor.asDouble();
    }

    static <E extends Enum<E>> E enumeracao(Class<E> tipo, JsonNode corpo, String campo) {
        String valor = texto(corpo, campo);
        try {
            return Enum.valueOf(tipo, valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valor inválido para " + campo + ": " + valor + ".");
        }
    }

    static CodigoLancamento codigo(JsonNode corpo) {
        return new CodigoLancamento(texto(corpo, "codigoLancamento"));
    }

    static LocalDate dataOpcional(JsonNode corpo, String campo) {
        String valor = textoOpcional(corpo, campo);
        if (valor == null) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser uma data no formato AAAA-MM-DD.");
        }
    }
}
