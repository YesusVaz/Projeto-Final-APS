package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.model.Usuario;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Tudo que o handler precisa da requisicao. {@code usuario} e {@code token} sao nulos nas rotas publicas.
 * {@code consulta} sao os parametros depois do ? na URL (ex.: ?data=2026-11-17).
 */
record Requisicao(Usuario usuario, String token, List<String> parametros, JsonNode corpo,
                  Map<String, String> consulta) {

    /** Id numerico capturado no caminho da rota. */
    long id() {
        return Long.parseLong(parametros.get(0));
    }

    /** Data vinda da URL. Sem o parametro, vale hoje. */
    LocalDate dataDaConsulta(String nome) {
        String valor = consulta.get(nome);
        if (valor == null || valor.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Parâmetro " + nome + " deve ser uma data no formato AAAA-MM-DD.");
        }
    }
}
